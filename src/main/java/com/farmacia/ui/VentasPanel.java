package com.farmacia.ui;

import com.farmacia.model.DetalleVenta;
import com.farmacia.model.Producto;
import com.farmacia.model.Usuario;
import com.farmacia.model.Venta;
import com.farmacia.service.ProductoService;
import com.farmacia.service.VentaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentasPanel {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final Usuario usuarioActual; // Usuario que realiza las ventas
    private BorderPane content;
    
    // Componentes para nueva venta
    private TableView<ItemVenta> tablaCarrito;
    private ObservableList<ItemVenta> carritoData;
    private Label lblTotal;
    private Label lblSubtotal;
    private TextField txtDescuento;
    private ComboBox<String> cboMetodoPago;
    private TextField txtCliente;
    private TextArea txtObservaciones;
    
    // Componentes para historial
    private TableView<Venta> tablaVentas;
    private ObservableList<Venta> ventasData;
    private DatePicker dpFechaInicio;
    private DatePicker dpFechaFin;
    
    public VentasPanel(VentaService ventaService, ProductoService productoService, Usuario usuarioActual) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.usuarioActual = usuarioActual;
        this.carritoData = FXCollections.observableArrayList();
        this.ventasData = FXCollections.observableArrayList();
        inicializarContenido();
    }

    public BorderPane getContent() {
        return content;
    }

    private void inicializarContenido() {
        content = new BorderPane();
        content.setPadding(new Insets(15));

        // Crear TabPane para nueva venta e historial
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabNuevaVenta = new Tab("🛒 Nueva Venta");
        tabNuevaVenta.setContent(crearPanelNuevaVenta());

        Tab tabHistorial = new Tab("📋 Historial de Ventas");
        tabHistorial.setContent(crearPanelHistorial());

        tabPane.getTabs().addAll(tabNuevaVenta, tabHistorial);

        content.setCenter(tabPane);
    }

    private VBox crearPanelNuevaVenta() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        // Título
        Label titulo = new Label("💰 Nueva Venta");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Panel de búsqueda de productos
        HBox searchBox = crearPanelBusquedaProducto();

        // Tabla de carrito
        VBox carritoBox = crearTablaCarrito();

        // Panel de totales y datos de venta
        HBox bottomBox = crearPanelTotales();

        vbox.getChildren().addAll(titulo, searchBox, carritoBox, bottomBox);

        return vbox;
    }

    private HBox crearPanelBusquedaProducto() {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);

        Label lblBuscar = new Label("Buscar Producto:");
        ComboBox<Producto> cboProducto = new ComboBox<>();
        cboProducto.setPrefWidth(400);
        cboProducto.setPromptText("Seleccione un producto...");
        
        // Cargar productos activos
        List<Producto> productos = productoService.obtenerTodosActivos();
        cboProducto.getItems().addAll(productos);
        
        // Configurar cómo se muestran los productos
        cboProducto.setCellFactory(lv -> new ListCell<Producto>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                if (empty || producto == null) {
                    setText(null);
                } else {
                    setText(producto.getCodigo() + " - " + producto.getNombre() + 
                           " (Stock: " + producto.getStock() + ") - €" + producto.getPrecio());
                }
            }
        });
        
        cboProducto.setButtonCell(new ListCell<Producto>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                if (empty || producto == null) {
                    setText(null);
                } else {
                    setText(producto.getCodigo() + " - " + producto.getNombre());
                }
            }
        });

        TextField txtCantidad = new TextField("1");
        txtCantidad.setPrefWidth(80);
        txtCantidad.setPromptText("Cant.");

        Button btnAgregar = new Button("➕ Agregar al Carrito");
        btnAgregar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnAgregar.setOnAction(e -> {
            Producto producto = cboProducto.getValue();
            if (producto != null) {
                try {
                    int cantidad = Integer.parseInt(txtCantidad.getText());
                    if (cantidad <= 0) {
                        mostrarAlerta("Error", "La cantidad debe ser mayor a 0", Alert.AlertType.ERROR);
                        return;
                    }
                    if (cantidad > producto.getStock()) {
                        mostrarAlerta("Error", "Stock insuficiente. Disponible: " + producto.getStock(), Alert.AlertType.ERROR);
                        return;
                    }
                    agregarAlCarrito(producto, cantidad);
                    cboProducto.setValue(null);
                    txtCantidad.setText("1");
                } catch (NumberFormatException ex) {
                    mostrarAlerta("Error", "Cantidad inválida", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Advertencia", "Debe seleccionar un producto", Alert.AlertType.WARNING);
            }
        });

        hbox.getChildren().addAll(lblBuscar, cboProducto, new Label("Cantidad:"), txtCantidad, btnAgregar);

        return hbox;
    }

    private VBox crearTablaCarrito() {
        VBox vbox = new VBox(10);

        Label lblCarrito = new Label("🛒 Carrito de Compra");
        lblCarrito.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        tablaCarrito = new TableView<>();
        tablaCarrito.setPrefHeight(300);

        TableColumn<ItemVenta, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setPrefWidth(100);

        TableColumn<ItemVenta, String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProducto.setPrefWidth(250);

        TableColumn<ItemVenta, Integer> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setPrefWidth(80);

        TableColumn<ItemVenta, BigDecimal> colPrecio = new TableColumn<>("Precio Unit.");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colPrecio.setPrefWidth(100);

        TableColumn<ItemVenta, BigDecimal> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setPrefWidth(120);

        TableColumn<ItemVenta, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(100);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = new Button("🗑️");
            {
                btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                btnEliminar.setOnAction(e -> {
                    ItemVenta item = getTableView().getItems().get(getIndex());
                    carritoData.remove(item);
                    actualizarTotales();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });

        tablaCarrito.getColumns().addAll(colCodigo, colProducto, colCantidad, colPrecio, colSubtotal, colAcciones);
        tablaCarrito.setItems(carritoData);

        Button btnLimpiarCarrito = new Button("🗑️ Limpiar Carrito");
        btnLimpiarCarrito.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        btnLimpiarCarrito.setOnAction(e -> {
            if (!carritoData.isEmpty()) {
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Confirmar");
                confirmacion.setHeaderText("¿Limpiar el carrito?");
                confirmacion.setContentText("Se eliminarán todos los productos del carrito");
                
                Optional<ButtonType> resultado = confirmacion.showAndWait();
                if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                    carritoData.clear();
                    actualizarTotales();
                }
            }
        });

        vbox.getChildren().addAll(lblCarrito, tablaCarrito, btnLimpiarCarrito);

        return vbox;
    }

    private HBox crearPanelTotales() {
        HBox hbox = new HBox(20);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(15, 0, 0, 0));

        // Panel izquierdo: Datos de la venta
        VBox datosBox = new VBox(10);
        datosBox.setPrefWidth(400);

        Label lblDatos = new Label("📝 Datos de la Venta");
        lblDatos.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox clienteBox = new HBox(10);
        clienteBox.setAlignment(Pos.CENTER_LEFT);
        Label lblCliente = new Label("Cliente (opcional):");
        lblCliente.setPrefWidth(150);
        txtCliente = new TextField();
        txtCliente.setPromptText("Nombre del cliente");
        txtCliente.setPrefWidth(230);
        clienteBox.getChildren().addAll(lblCliente, txtCliente);

        HBox metodoPagoBox = new HBox(10);
        metodoPagoBox.setAlignment(Pos.CENTER_LEFT);
        Label lblMetodoPago = new Label("Método de Pago:");
        lblMetodoPago.setPrefWidth(150);
        cboMetodoPago = new ComboBox<>();
        cboMetodoPago.getItems().addAll("Efectivo", "Tarjeta", "Transferencia");
        cboMetodoPago.setValue("Efectivo");
        cboMetodoPago.setPrefWidth(230);
        metodoPagoBox.getChildren().addAll(lblMetodoPago, cboMetodoPago);

        Label lblObserv = new Label("Observaciones:");
        txtObservaciones = new TextArea();
        txtObservaciones.setPrefRowCount(3);
        txtObservaciones.setPromptText("Notas adicionales sobre la venta...");

        datosBox.getChildren().addAll(lblDatos, clienteBox, metodoPagoBox, lblObserv, txtObservaciones);

        // Panel derecho: Totales y botones
        VBox totalesBox = new VBox(10);
        totalesBox.setPrefWidth(300);
        totalesBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblTotalesTitle = new Label("💵 Totales");
        lblTotalesTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox subtotalBox = new HBox(10);
        subtotalBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblSubtotalLabel = new Label("Subtotal:");
        lblSubtotal = new Label("€0.00");
        lblSubtotal.setStyle("-fx-font-size: 16px;");
        subtotalBox.getChildren().addAll(lblSubtotalLabel, lblSubtotal);

        HBox descuentoBox = new HBox(10);
        descuentoBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblDescuentoLabel = new Label("Descuento:");
        txtDescuento = new TextField("0");
        txtDescuento.setPrefWidth(100);
        txtDescuento.textProperty().addListener((obs, old, newVal) -> actualizarTotales());
        descuentoBox.getChildren().addAll(lblDescuentoLabel, txtDescuento);

        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblTotalLabel = new Label("TOTAL:");
        lblTotalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        lblTotal = new Label("€0.00");
        lblTotal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        totalBox.getChildren().addAll(lblTotalLabel, lblTotal);

        Button btnConfirmarVenta = new Button("✅ Confirmar Venta");
        btnConfirmarVenta.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-min-width: 200px; " +
            "-fx-min-height: 50px;"
        );
        btnConfirmarVenta.setOnAction(e -> confirmarVenta());

        Button btnCancelar = new Button("❌ Cancelar");
        btnCancelar.setStyle(
            "-fx-background-color: #f44336; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-min-width: 200px; " +
            "-fx-min-height: 40px;"
        );
        btnCancelar.setOnAction(e -> cancelarVenta());

        totalesBox.getChildren().addAll(
            lblTotalesTitle, subtotalBox, descuentoBox, totalBox, 
            btnConfirmarVenta, btnCancelar
        );

        hbox.getChildren().addAll(datosBox, new Separator(javafx.geometry.Orientation.VERTICAL), totalesBox);

        return hbox;
    }

    private VBox crearPanelHistorial() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        // Título
        Label titulo = new Label("📋 Historial de Ventas");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Panel de filtros
        HBox filtrosBox = new HBox(10);
        filtrosBox.setAlignment(Pos.CENTER_LEFT);

        Label lblFiltros = new Label("Filtrar:");
        
        Button btnHoy = new Button("📅 Hoy");
        btnHoy.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnHoy.setOnAction(e -> cargarVentasDelDia());

        Button btnTodas = new Button("📋 Todas");
        btnTodas.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        btnTodas.setOnAction(e -> cargarTodasLasVentas());

        Label lblEstadisticas = new Label("💰 Total del día: €0.00 | 📊 Ventas: 0");
        lblEstadisticas.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        lblEstadisticas.setId("lblEstadisticas");

        filtrosBox.getChildren().addAll(lblFiltros, btnHoy, btnTodas, new Label("     "), lblEstadisticas);

        // Tabla de ventas
        tablaVentas = new TableView<>();
        tablaVentas.setPrefHeight(450);

        TableColumn<Venta, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Venta, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(cellData.getValue().getFecha().format(formatter));
        });
        colFecha.setPrefWidth(150);

        TableColumn<Venta, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colCliente.setPrefWidth(200);

        TableColumn<Venta, String> colMetodoPago = new TableColumn<>("Método Pago");
        colMetodoPago.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colMetodoPago.setPrefWidth(120);

        TableColumn<Venta, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue().getUsuario();
            String nombreUsuario = usuario != null ? usuario.getUsername() : "N/A";
            return new SimpleStringProperty(nombreUsuario);
        });
        colUsuario.setPrefWidth(120);

        TableColumn<Venta, BigDecimal> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(100);

        TableColumn<Venta, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            String estado = cellData.getValue().getActivo() ? "✅ Activa" : "❌ Anulada";
            return new SimpleStringProperty(estado);
        });
        colEstado.setPrefWidth(100);

        TableColumn<Venta, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(150);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁️");
            private final Button btnAnular = new Button("❌");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Venta venta = (Venta) getTableRow().getItem();
                    
                    btnVer.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                    btnVer.setOnAction(e -> {
                        if (venta != null) {
                            verDetalleVenta(venta);
                        }
                    });

                    btnAnular.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
                    btnAnular.setOnAction(e -> {
                        if (venta != null) {
                            if (venta.getActivo()) {
                                anularVenta(venta);
                            } else {
                                mostrarAlerta("Advertencia", "Esta venta ya está anulada", Alert.AlertType.WARNING);
                            }
                        }
                    });
                    
                    HBox buttons = new HBox(5, btnVer, btnAnular);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        tablaVentas.getColumns().addAll(colId, colFecha, colCliente, colMetodoPago, colUsuario, colTotal, colEstado, colAcciones);
        tablaVentas.setItems(ventasData);

        vbox.getChildren().addAll(titulo, filtrosBox, tablaVentas);

        // Cargar ventas del día por defecto
        cargarVentasDelDia();

        return vbox;
    }

    private void agregarAlCarrito(Producto producto, int cantidad) {
        // Verificar si el producto ya está en el carrito
        for (ItemVenta item : carritoData) {
            if (item.getProducto().getId().equals(producto.getId())) {
                item.setCantidad(item.getCantidad() + cantidad);
                item.calcularSubtotal();
                tablaCarrito.refresh();
                actualizarTotales();
                return;
            }
        }

        // Si no está, agregarlo
        ItemVenta item = new ItemVenta(producto, cantidad);
        carritoData.add(item);
        actualizarTotales();
    }

    private void actualizarTotales() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemVenta item : carritoData) {
            subtotal = subtotal.add(item.getSubtotal());
        }

        BigDecimal descuento = BigDecimal.ZERO;
        try {
            descuento = new BigDecimal(txtDescuento.getText());
            if (descuento.compareTo(BigDecimal.ZERO) < 0) {
                descuento = BigDecimal.ZERO;
                txtDescuento.setText("0");
            }
        } catch (NumberFormatException e) {
            txtDescuento.setText("0");
        }

        BigDecimal total = subtotal.subtract(descuento);

        lblSubtotal.setText(String.format("€%.2f", subtotal));
        lblTotal.setText(String.format("€%.2f", total));
    }

    private void confirmarVenta() {
        if (carritoData.isEmpty()) {
            mostrarAlerta("Advertencia", "El carrito está vacío", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Crear venta
            Venta venta = new Venta();
            venta.setCliente(txtCliente.getText().isEmpty() ? "Cliente General" : txtCliente.getText());
            venta.setMetodoPago(cboMetodoPago.getValue());
            venta.setObservaciones(txtObservaciones.getText());
            
            BigDecimal descuento = BigDecimal.ZERO;
            try {
                descuento = new BigDecimal(txtDescuento.getText());
            } catch (NumberFormatException e) {
                descuento = BigDecimal.ZERO;
            }
            venta.setDescuento(descuento);

            // Agregar detalles
            for (ItemVenta item : carritoData) {
                DetalleVenta detalle = new DetalleVenta();
                detalle.setProducto(item.getProducto());
                detalle.setCantidad(item.getCantidad());
                detalle.setPrecioUnitario(item.getPrecioUnitario());
                venta.agregarDetalle(detalle);
            }

            // Guardar venta (pasar usuario actual)
            Venta ventaGuardada = ventaService.crearVenta(venta, usuarioActual);

            mostrarAlerta("Éxito", 
                "Venta registrada correctamente\n" +
                "ID: " + ventaGuardada.getId() + "\n" +
                "Total: €" + String.format("%.2f", ventaGuardada.getTotal()), 
                Alert.AlertType.INFORMATION);

            // Limpiar formulario
            limpiarFormularioVenta();
            
            // Recargar historial si está visible
            cargarVentasDelDia();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al registrar la venta: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void cancelarVenta() {
        if (!carritoData.isEmpty()) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Cancelación");
            confirmacion.setHeaderText("¿Cancelar la venta actual?");
            confirmacion.setContentText("Se perderán todos los datos ingresados");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                limpiarFormularioVenta();
            }
        }
    }

    private void limpiarFormularioVenta() {
        carritoData.clear();
        txtCliente.clear();
        txtObservaciones.clear();
        txtDescuento.setText("0");
        cboMetodoPago.setValue("Efectivo");
        actualizarTotales();
    }

    private void cargarVentasDelDia() {
        ventasData.clear();
        List<Venta> ventas = ventaService.obtenerVentasDelDia();
        ventasData.addAll(ventas);
        actualizarEstadisticas();
    }

    private void cargarTodasLasVentas() {
        ventasData.clear();
        List<Venta> ventas = ventaService.obtenerTodasActivas();
        ventasData.addAll(ventas);
        actualizarEstadisticas();
    }

    private void actualizarEstadisticas() {
        VentaService.EstadisticasVentas stats = ventaService.obtenerEstadisticasDelDia();
        Label lblEstadisticas = (Label) content.lookup("#lblEstadisticas");
        if (lblEstadisticas != null) {
            lblEstadisticas.setText(String.format(
                "💰 Total del día: €%.2f | 📊 Ventas: %d",
                stats.getTotalVentas(), stats.getCantidadVentas()
            ));
        }
    }

    private void verDetalleVenta(Venta venta) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle de Venta");
        alert.setHeaderText("Venta #" + venta.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder contenido = new StringBuilder();
        contenido.append("Fecha: ").append(venta.getFecha().format(formatter)).append("\n");
        contenido.append("Cliente: ").append(venta.getCliente()).append("\n");
        contenido.append("Método de Pago: ").append(venta.getMetodoPago()).append("\n\n");
        contenido.append("PRODUCTOS:\n");
        contenido.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        for (DetalleVenta detalle : venta.getDetalles()) {
            contenido.append(String.format("• %s\n", detalle.getProducto().getNombre()));
            contenido.append(String.format("  Cantidad: %d x €%.2f = €%.2f\n", 
                detalle.getCantidad(), 
                detalle.getPrecioUnitario(), 
                detalle.getSubtotal()));
        }

        contenido.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        contenido.append(String.format("Subtotal: €%.2f\n", venta.getSubtotal()));
        contenido.append(String.format("Descuento: €%.2f\n", venta.getDescuento()));
        contenido.append(String.format("TOTAL: €%.2f\n", venta.getTotal()));

        if (venta.getObservaciones() != null && !venta.getObservaciones().isEmpty()) {
            contenido.append("\nObservaciones:\n").append(venta.getObservaciones());
        }

        alert.setContentText(contenido.toString());
        alert.showAndWait();
    }

    private void anularVenta(Venta venta) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Anulación");
        confirmacion.setHeaderText("¿Anular esta venta?");
        confirmacion.setContentText(
            "Venta #" + venta.getId() + "\n" +
            "Total: €" + String.format("%.2f", venta.getTotal()) + "\n\n" +
            "El stock de los productos será restaurado"
        );

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                ventaService.anularVenta(venta.getId());
                mostrarAlerta("Éxito", "Venta anulada correctamente", Alert.AlertType.INFORMATION);
                cargarVentasDelDia();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al anular la venta: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clase interna para items del carrito
    public static class ItemVenta {
        private Producto producto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public ItemVenta(Producto producto, Integer cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = producto.getPrecio();
            calcularSubtotal();
        }

        public void calcularSubtotal() {
            this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }

        // Getters y setters
        public Producto getProducto() { return producto; }
        public void setProducto(Producto producto) { this.producto = producto; }
        
        public String getCodigo() { return producto.getCodigo(); }
        public String getNombre() { return producto.getNombre(); }
        
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { 
            this.cantidad = cantidad;
            calcularSubtotal();
        }
        
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { 
            this.precioUnitario = precioUnitario;
            calcularSubtotal();
        }
        
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}

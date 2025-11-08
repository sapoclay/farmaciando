package com.farmacia.ui;

import com.farmacia.model.DetallePedido;
import com.farmacia.model.Pedido;
import com.farmacia.model.Producto;
import com.farmacia.model.Proveedor;
import com.farmacia.service.PedidoService;
import com.farmacia.service.ProductoService;
import com.farmacia.service.ProveedorService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana para crear un nuevo pedido a proveedor
 */
public class CrearPedidoWindow {

    private final ProveedorService proveedorService;
    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private final Runnable onSuccess;
    
    private Stage stage;
    private Pedido pedido;
    
    // Componentes del formulario
    private ComboBox<Proveedor> cbProveedor;
    private DatePicker dpFechaEntregaEstimada;
    private TextArea txtObservaciones;
    
    // Tabla de productos del pedido
    private TableView<DetallePedido> tablaDetalles;
    private ObservableList<DetallePedido> detallesData;
    
    // Labels de totales
    private Label lblSubtotal;
    private Label lblIVA;
    private Label lblDescuento;
    private Label lblTotal;
    
    private TextField txtDescuento;

    public CrearPedidoWindow(ProveedorService proveedorService, PedidoService pedidoService, 
                            ProductoService productoService, Runnable onSuccess) {
        this.proveedorService = proveedorService;
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.onSuccess = onSuccess;
        this.detallesData = FXCollections.observableArrayList();
        inicializar();
    }

    private void inicializar() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Crear Nuevo Pedido");
        stage.setWidth(1000);
        stage.setHeight(700);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Título
        Label titulo = new Label("📦 Crear Nuevo Pedido a Proveedor");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        VBox topBox = new VBox(10, titulo, new Separator());
        topBox.setPadding(new Insets(0, 0, 15, 0));
        root.setTop(topBox);

        // Formulario central
        VBox centerBox = new VBox(15);
        centerBox.getChildren().addAll(
                crearSeccionProveedor(),
                crearSeccionProductos(),
                crearSeccionTotales()
        );
        ScrollPane scrollPane = new ScrollPane(centerBox);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        // Botones inferiores
        HBox bottomBox = crearBotones();
        root.setBottom(bottomBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    private VBox crearSeccionProveedor() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);");

        Label lblTitulo = new Label("1️⃣ Seleccionar Proveedor");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        // Proveedor
        Label lblProveedor = new Label("Proveedor:*");
        cbProveedor = new ComboBox<>();
        cbProveedor.setPromptText("Seleccione un proveedor");
        cbProveedor.setPrefWidth(350);
        List<Proveedor> proveedores = proveedorService.obtenerTodosActivos();
        cbProveedor.setItems(FXCollections.observableArrayList(proveedores));
        
        // Formato personalizado para mostrar nombre y empresa
        cbProveedor.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s", item.getEmpresa(), item.getNombre()));
                }
            }
        });
        cbProveedor.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s", item.getEmpresa(), item.getNombre()));
                }
            }
        });

        // Fecha de entrega estimada
        Label lblFecha = new Label("Fecha Entrega Estimada:");
        dpFechaEntregaEstimada = new DatePicker();
        dpFechaEntregaEstimada.setPromptText("Seleccione fecha");
        dpFechaEntregaEstimada.setPrefWidth(200);

        // Observaciones
        Label lblObs = new Label("Observaciones:");
        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones adicionales del pedido...");
        txtObservaciones.setPrefHeight(60);
        txtObservaciones.setWrapText(true);

        grid.add(lblProveedor, 0, 0);
        grid.add(cbProveedor, 1, 0);
        grid.add(lblFecha, 0, 1);
        grid.add(dpFechaEntregaEstimada, 1, 1);
        grid.add(lblObs, 0, 2);
        grid.add(txtObservaciones, 1, 2);

        section.getChildren().addAll(lblTitulo, grid);
        return section;
    }

    private VBox crearSeccionProductos() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label("2️⃣ Productos del Pedido");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAgregarProducto = new Button("➕ Agregar Producto");
        btnAgregarProducto.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnAgregarProducto.setOnAction(e -> mostrarDialogoAgregarProducto());

        header.getChildren().addAll(lblTitulo, spacer, btnAgregarProducto);

        // Tabla de detalles
        tablaDetalles = new TableView<>();
        tablaDetalles.setItems(detallesData);
        tablaDetalles.setPrefHeight(250);

        TableColumn<DetallePedido, String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(data -> {
            if (data.getValue().getProducto() != null) {
                return new SimpleStringProperty(data.getValue().getProducto().getNombre());
            } else {
                return new SimpleStringProperty(data.getValue().getNombreProducto());
            }
        });
        colProducto.setPrefWidth(250);

        TableColumn<DetallePedido, Integer> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setPrefWidth(100);

        TableColumn<DetallePedido, BigDecimal> colPrecioUnitario = new TableColumn<>("Precio Unit.");
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colPrecioUnitario.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", precio));
                }
            }
        });
        colPrecioUnitario.setPrefWidth(120);

        TableColumn<DetallePedido, BigDecimal> colDescuentoItem = new TableColumn<>("Descuento %");
        colDescuentoItem.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        colDescuentoItem.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal descuento, boolean empty) {
                super.updateItem(descuento, empty);
                if (empty || descuento == null || descuento.compareTo(BigDecimal.ZERO) == 0) {
                    setText("-");
                } else {
                    setText(String.format("%.1f%%", descuento));
                }
            }
        });
        colDescuentoItem.setPrefWidth(120);

        TableColumn<DetallePedido, BigDecimal> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty || subtotal == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", subtotal));
                }
            }
        });
        colSubtotal.setPrefWidth(120);

        TableColumn<DetallePedido, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = new Button("🗑️");

            {
                btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                btnEliminar.setOnAction(event -> {
                    DetallePedido detalle = getTableView().getItems().get(getIndex());
                    detallesData.remove(detalle);
                    recalcularTotales();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnEliminar);
                }
            }
        });
        colAcciones.setPrefWidth(100);

        tablaDetalles.getColumns().addAll(colProducto, colCantidad, colPrecioUnitario, 
                colDescuentoItem, colSubtotal, colAcciones);

        section.getChildren().addAll(header, tablaDetalles);
        return section;
    }

    private VBox crearSeccionTotales() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);");

        Label lblTitulo = new Label("3️⃣ Resumen del Pedido");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_RIGHT);

        // Subtotal
        Label lblSubtotalLabel = new Label("Subtotal:");
        lblSubtotalLabel.setStyle("-fx-font-size: 14px;");
        lblSubtotal = new Label("€0.00");
        lblSubtotal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // IVA (21%)
        Label lblIVALabel = new Label("IVA (21%):");
        lblIVALabel.setStyle("-fx-font-size: 14px;");
        lblIVA = new Label("€0.00");
        lblIVA.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Descuento adicional
        Label lblDescuentoLabel = new Label("Descuento adicional (€):");
        lblDescuentoLabel.setStyle("-fx-font-size: 14px;");
        txtDescuento = new TextField("0");
        txtDescuento.setPrefWidth(100);
        txtDescuento.textProperty().addListener((obs, old, newVal) -> recalcularTotales());

        lblDescuento = new Label("€0.00");
        lblDescuento.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f44336;");

        // Total
        Label lblTotalLabel = new Label("TOTAL:");
        lblTotalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        lblTotal = new Label("€0.00");
        lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        grid.add(lblSubtotalLabel, 0, 0);
        grid.add(lblSubtotal, 1, 0);
        grid.add(lblIVALabel, 0, 1);
        grid.add(lblIVA, 1, 1);
        grid.add(lblDescuentoLabel, 0, 2);
        grid.add(txtDescuento, 1, 2);
        grid.add(new Separator(javafx.geometry.Orientation.HORIZONTAL), 0, 3, 2, 1);
        grid.add(lblTotalLabel, 0, 4);
        grid.add(lblTotal, 1, 4);

        section.getChildren().addAll(lblTitulo, grid);
        return section;
    }

    private HBox crearBotones() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setPadding(new Insets(15, 0, 0, 0));

        Button btnCancelar = new Button("❌ Cancelar");
        btnCancelar.setStyle("-fx-background-color: #757575; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        btnCancelar.setOnAction(e -> stage.close());

        Button btnGuardarBorrador = new Button("💾 Guardar Borrador");
        btnGuardarBorrador.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        btnGuardarBorrador.setOnAction(e -> guardarPedido(Pedido.EstadoPedido.BORRADOR));

        Button btnEnviar = new Button("✅ Crear y Enviar");
        btnEnviar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        btnEnviar.setOnAction(e -> guardarPedido(Pedido.EstadoPedido.ENVIADO));

        box.getChildren().addAll(btnCancelar, btnGuardarBorrador, btnEnviar);
        return box;
    }

    private void mostrarDialogoAgregarProducto() {
        Dialog<DetallePedido> dialog = new Dialog<>();
        dialog.setTitle("Agregar Producto al Pedido");
        dialog.setHeaderText("Seleccione el producto y la cantidad");

        ButtonType btnAgregar = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAgregar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Producto
        Label lblProducto = new Label("Producto:*");
        ComboBox<Producto> cbProducto = new ComboBox<>();
        List<Producto> productos = productoService.obtenerTodosActivos();
        cbProducto.setItems(FXCollections.observableArrayList(productos));
        cbProducto.setPromptText("Seleccione un producto");
        cbProducto.setPrefWidth(300);
        
        cbProducto.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (Stock: %d)", item.getNombre(), item.getStock()));
                }
            }
        });
        cbProducto.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre());
                }
            }
        });

        // Cantidad
        Label lblCantidad = new Label("Cantidad:*");
        TextField txtCantidad = new TextField("1");
        txtCantidad.setPrefWidth(100);

        // Precio unitario
        Label lblPrecio = new Label("Precio Unitario:*");
        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("0.00");
        txtPrecio.setPrefWidth(100);

        // Auto-rellenar precio cuando se selecciona producto
        cbProducto.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                txtPrecio.setText(newVal.getPrecio().toString());
            }
        });

        // Descuento
        Label lblDescuento = new Label("Descuento %:");
        TextField txtDescuentoItem = new TextField("0");
        txtDescuentoItem.setPrefWidth(100);

        grid.add(lblProducto, 0, 0);
        grid.add(cbProducto, 1, 0);
        grid.add(lblCantidad, 0, 1);
        grid.add(txtCantidad, 1, 1);
        grid.add(lblPrecio, 0, 2);
        grid.add(txtPrecio, 1, 2);
        grid.add(lblDescuento, 0, 3);
        grid.add(txtDescuentoItem, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAgregar) {
                try {
                    Producto producto = cbProducto.getValue();
                    if (producto == null) {
                        mostrarError("Debe seleccionar un producto");
                        return null;
                    }

                    int cantidad = Integer.parseInt(txtCantidad.getText());
                    if (cantidad <= 0) {
                        mostrarError("La cantidad debe ser mayor a 0");
                        return null;
                    }

                    BigDecimal precio = new BigDecimal(txtPrecio.getText());
                    if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                        mostrarError("El precio debe ser mayor a 0");
                        return null;
                    }

                    BigDecimal descuento = new BigDecimal(txtDescuentoItem.getText());
                    if (descuento.compareTo(BigDecimal.ZERO) < 0 || descuento.compareTo(new BigDecimal("100")) > 0) {
                        mostrarError("El descuento debe estar entre 0 y 100");
                        return null;
                    }

                    DetallePedido detalle = new DetallePedido();
                    detalle.setProducto(producto);
                    detalle.setCantidad(cantidad);
                    detalle.setPrecioUnitario(precio);
                    detalle.setDescuento(descuento);
                    detalle.getSubtotal(); // Esto calculará el subtotal automáticamente

                    return detalle;

                } catch (NumberFormatException e) {
                    mostrarError("Valores numéricos inválidos");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(detalle -> {
            detallesData.add(detalle);
            recalcularTotales();
        });
    }

    private void recalcularTotales() {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (DetallePedido detalle : detallesData) {
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        BigDecimal iva = subtotal.multiply(new BigDecimal("0.21"));
        
        BigDecimal descuento = BigDecimal.ZERO;
        try {
            descuento = new BigDecimal(txtDescuento.getText());
            if (descuento.compareTo(BigDecimal.ZERO) < 0) {
                descuento = BigDecimal.ZERO;
            }
        } catch (NumberFormatException e) {
            descuento = BigDecimal.ZERO;
        }

        BigDecimal total = subtotal.add(iva).subtract(descuento);

        lblSubtotal.setText(String.format("€%.2f", subtotal));
        lblIVA.setText(String.format("€%.2f", iva));
        lblDescuento.setText(String.format("-€%.2f", descuento));
        lblTotal.setText(String.format("€%.2f", total));
    }

    private void guardarPedido(Pedido.EstadoPedido estado) {
        try {
            // Validaciones
            if (cbProveedor.getValue() == null) {
                mostrarError("Debe seleccionar un proveedor");
                return;
            }

            if (detallesData.isEmpty()) {
                mostrarError("Debe agregar al menos un producto al pedido");
                return;
            }

            // Crear pedido
            Pedido pedido = new Pedido();
            pedido.setProveedor(cbProveedor.getValue());
            pedido.setEstado(estado);
            pedido.setObservaciones(txtObservaciones.getText());
            
            if (dpFechaEntregaEstimada.getValue() != null) {
                pedido.setFechaEntregaEstimada(dpFechaEntregaEstimada.getValue().atStartOfDay());
            }

            // Agregar detalles
            for (DetallePedido detalle : detallesData) {
                pedido.agregarDetalle(detalle);
            }

            // Calcular totales
            BigDecimal subtotal = new BigDecimal(lblSubtotal.getText().replace("€", ""));
            BigDecimal iva = new BigDecimal(lblIVA.getText().replace("€", ""));
            BigDecimal descuento = new BigDecimal(lblDescuento.getText().replace("-€", ""));
            
            pedido.setSubtotal(subtotal);
            pedido.setIva(iva);
            pedido.setDescuento(descuento);
            pedido.calcularTotal();

            // Guardar
            pedidoService.crearPedido(pedido);

            mostrarExito("Pedido creado exitosamente");
            if (onSuccess != null) {
                onSuccess.run();
            }
            stage.close();

        } catch (Exception e) {
            mostrarError("Error al guardar el pedido: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void mostrar() {
        stage.showAndWait();
    }
}

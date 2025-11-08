package com.farmacia.ui;

import com.farmacia.model.Pedido;
import com.farmacia.model.Pedido.EstadoPedido;
import com.farmacia.model.Proveedor;
import com.farmacia.service.PedidoService;
import com.farmacia.service.ProductoService;
import com.farmacia.service.ProveedorService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ProveedoresPanel {

    private final ProveedorService proveedorService;
    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private BorderPane content;
    
    // Componentes para gestión de proveedores
    private TableView<Proveedor> tablaProveedores;
    private ObservableList<Proveedor> proveedoresData;
    private TextField txtBuscarProveedor;
    
    // Componentes para gestión de pedidos
    private TableView<Pedido> tablaPedidos;
    private ObservableList<Pedido> pedidosData;
    
    public ProveedoresPanel(ProveedorService proveedorService, PedidoService pedidoService, ProductoService productoService) {
        this.proveedorService = proveedorService;
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.proveedoresData = FXCollections.observableArrayList();
        this.pedidosData = FXCollections.observableArrayList();
        inicializarContenido();
    }

    public BorderPane getContent() {
        return content;
    }

    private void inicializarContenido() {
        content = new BorderPane();
        content.setPadding(new Insets(15));

        // Crear TabPane para proveedores y pedidos
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabProveedores = new Tab("🏢 Gestión de Proveedores");
        tabProveedores.setContent(crearPanelProveedores());

        Tab tabPedidos = new Tab("📦 Gestión de Pedidos");
        tabPedidos.setContent(crearPanelPedidos());

        tabPane.getTabs().addAll(tabProveedores, tabPedidos);

        content.setCenter(tabPane);
    }

    // ==================== PANEL DE PROVEEDORES ====================
    
    private VBox crearPanelProveedores() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        // Título y estadísticas
        Label titulo = new Label("🏢 Gestión de Proveedores");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label lblEstadisticas = new Label("📊 Cargando estadísticas...");
        lblEstadisticas.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        lblEstadisticas.setId("lblEstadisticasProveedores");

        // Barra de herramientas
        HBox toolBar = new HBox(10);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        Button btnNuevoProveedor = new Button("➕ Nuevo Proveedor");
        btnNuevoProveedor.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        btnNuevoProveedor.setOnAction(e -> mostrarFormularioProveedor(null));

        txtBuscarProveedor = new TextField();
        txtBuscarProveedor.setPromptText("🔍 Buscar por nombre o empresa...");
        txtBuscarProveedor.setPrefWidth(300);
        txtBuscarProveedor.textProperty().addListener((obs, old, newVal) -> buscarProveedores(newVal));

        Button btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnRefrescar.setOnAction(e -> cargarProveedores());

        toolBar.getChildren().addAll(btnNuevoProveedor, new Separator(javafx.geometry.Orientation.VERTICAL), 
                                     txtBuscarProveedor, btnRefrescar);

        // Tabla de proveedores
        tablaProveedores = crearTablaProveedores();

        vbox.getChildren().addAll(titulo, lblEstadisticas, toolBar, tablaProveedores);

        // Cargar datos iniciales
        cargarProveedores();
        actualizarEstadisticasProveedores();

        return vbox;
    }

    private TableView<Proveedor> crearTablaProveedores() {
        TableView<Proveedor> tabla = new TableView<>();
        tabla.setPrefHeight(500);

        TableColumn<Proveedor, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Proveedor, String> colEmpresa = new TableColumn<>("Empresa");
        colEmpresa.setCellValueFactory(new PropertyValueFactory<>("empresa"));
        colEmpresa.setPrefWidth(200);

        TableColumn<Proveedor, String> colNombre = new TableColumn<>("Contacto");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(150);

        TableColumn<Proveedor, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(120);

        TableColumn<Proveedor, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        TableColumn<Proveedor, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colCiudad.setPrefWidth(120);

        TableColumn<Proveedor, Integer> colCalificacion = new TableColumn<>("⭐");
        colCalificacion.setCellValueFactory(new PropertyValueFactory<>("calificacion"));
        colCalificacion.setPrefWidth(60);
        colCalificacion.setCellFactory(col -> new TableCell<Proveedor, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("⭐".repeat(Math.max(0, item)));
                }
            }
        });

        TableColumn<Proveedor, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(200);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁️");
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Proveedor proveedor = (Proveedor) getTableRow().getItem();
                    
                    btnVer.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                    btnVer.setOnAction(e -> verDetalleProveedor(proveedor));

                    btnEditar.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");
                    btnEditar.setOnAction(e -> mostrarFormularioProveedor(proveedor));

                    btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
                    btnEliminar.setOnAction(e -> eliminarProveedor(proveedor));
                    
                    HBox buttons = new HBox(5, btnVer, btnEditar, btnEliminar);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        tabla.getColumns().addAll(colId, colEmpresa, colNombre, colTelefono, colEmail, colCiudad, colCalificacion, colAcciones);
        tabla.setItems(proveedoresData);

        return tabla;
    }

    private void cargarProveedores() {
        proveedoresData.clear();
        List<Proveedor> proveedores = proveedorService.obtenerTodosActivos();
        proveedoresData.addAll(proveedores);
    }

    private void buscarProveedores(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            cargarProveedores();
        } else {
            proveedoresData.clear();
            List<Proveedor> proveedores = proveedorService.buscar(criterio);
            proveedoresData.addAll(proveedores);
        }
    }

    private void mostrarFormularioProveedor(Proveedor proveedor) {
        Dialog<Proveedor> dialog = new Dialog<>();
        dialog.setTitle(proveedor == null ? "Nuevo Proveedor" : "Editar Proveedor");
        dialog.setHeaderText(proveedor == null ? "Ingrese los datos del nuevo proveedor" : "Modificar datos del proveedor");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Campos del formulario
        TextField txtEmpresa = new TextField(proveedor != null ? proveedor.getEmpresa() : "");
        txtEmpresa.setPromptText("Nombre de la empresa *");
        
        TextField txtNombre = new TextField(proveedor != null ? proveedor.getNombre() : "");
        txtNombre.setPromptText("Persona de contacto *");
        
        TextField txtEmail = new TextField(proveedor != null ? proveedor.getEmail() : "");
        txtEmail.setPromptText("email@ejemplo.com");
        
        TextField txtTelefono = new TextField(proveedor != null ? proveedor.getTelefono() : "");
        txtTelefono.setPromptText("Teléfono principal");
        
        TextField txtTelefono2 = new TextField(proveedor != null ? proveedor.getTelefonoSecundario() : "");
        txtTelefono2.setPromptText("Teléfono secundario");
        
        TextField txtDireccion = new TextField(proveedor != null ? proveedor.getDireccion() : "");
        txtDireccion.setPromptText("Dirección");
        
        TextField txtCiudad = new TextField(proveedor != null ? proveedor.getCiudad() : "");
        txtCiudad.setPromptText("Ciudad");
        
        TextField txtCP = new TextField(proveedor != null ? proveedor.getCodigoPostal() : "");
        txtCP.setPromptText("Código Postal");
        
        TextField txtPais = new TextField(proveedor != null ? proveedor.getPais() : "España");
        
        TextField txtNIF = new TextField(proveedor != null ? proveedor.getNif() : "");
        txtNIF.setPromptText("NIF/CIF");
        
        TextField txtWeb = new TextField(proveedor != null ? proveedor.getSitioWeb() : "");
        txtWeb.setPromptText("www.ejemplo.com");
        
        TextArea txtProductos = new TextArea(proveedor != null ? proveedor.getProductosQueOfrece() : "");
        txtProductos.setPromptText("Tipos de productos que suministra");
        txtProductos.setPrefRowCount(3);
        
        TextField txtCondiciones = new TextField(proveedor != null ? proveedor.getCondicionesPago() : "");
        txtCondiciones.setPromptText("Ej: 30 días, Pago al contado");
        
        TextField txtDiasEntrega = new TextField(proveedor != null && proveedor.getDiasEntrega() != null ? 
            proveedor.getDiasEntrega().toString() : "");
        txtDiasEntrega.setPromptText("Días de entrega");
        
        ComboBox<Integer> cboCalificacion = new ComboBox<>();
        cboCalificacion.getItems().addAll(0, 1, 2, 3, 4, 5);
        cboCalificacion.setValue(proveedor != null && proveedor.getCalificacion() != null ? proveedor.getCalificacion() : 0);
        
        TextArea txtObservaciones = new TextArea(proveedor != null ? proveedor.getObservaciones() : "");
        txtObservaciones.setPromptText("Observaciones adicionales");
        txtObservaciones.setPrefRowCount(3);

        // Agregar campos al grid
        int row = 0;
        grid.add(new Label("Empresa: *"), 0, row);
        grid.add(txtEmpresa, 1, row++);
        grid.add(new Label("Contacto: *"), 0, row);
        grid.add(txtNombre, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(txtEmail, 1, row++);
        grid.add(new Label("Teléfono:"), 0, row);
        grid.add(txtTelefono, 1, row++);
        grid.add(new Label("Teléfono 2:"), 0, row);
        grid.add(txtTelefono2, 1, row++);
        grid.add(new Label("Dirección:"), 0, row);
        grid.add(txtDireccion, 1, row++);
        grid.add(new Label("Ciudad:"), 0, row);
        grid.add(txtCiudad, 1, row++);
        grid.add(new Label("Código Postal:"), 0, row);
        grid.add(txtCP, 1, row++);
        grid.add(new Label("País:"), 0, row);
        grid.add(txtPais, 1, row++);
        grid.add(new Label("NIF/CIF:"), 0, row);
        grid.add(txtNIF, 1, row++);
        grid.add(new Label("Sitio Web:"), 0, row);
        grid.add(txtWeb, 1, row++);
        grid.add(new Label("Productos:"), 0, row);
        grid.add(txtProductos, 1, row++);
        grid.add(new Label("Condiciones Pago:"), 0, row);
        grid.add(txtCondiciones, 1, row++);
        grid.add(new Label("Días Entrega:"), 0, row);
        grid.add(txtDiasEntrega, 1, row++);
        grid.add(new Label("Calificación:"), 0, row);
        grid.add(cboCalificacion, 1, row++);
        grid.add(new Label("Observaciones:"), 0, row);
        grid.add(txtObservaciones, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                try {
                    Proveedor p = proveedor != null ? proveedor : new Proveedor();
                    p.setEmpresa(txtEmpresa.getText().trim());
                    p.setNombre(txtNombre.getText().trim());
                    p.setEmail(txtEmail.getText().trim());
                    p.setTelefono(txtTelefono.getText().trim());
                    p.setTelefonoSecundario(txtTelefono2.getText().trim());
                    p.setDireccion(txtDireccion.getText().trim());
                    p.setCiudad(txtCiudad.getText().trim());
                    p.setCodigoPostal(txtCP.getText().trim());
                    p.setPais(txtPais.getText().trim());
                    p.setNif(txtNIF.getText().trim());
                    p.setSitioWeb(txtWeb.getText().trim());
                    p.setProductosQueOfrece(txtProductos.getText().trim());
                    p.setCondicionesPago(txtCondiciones.getText().trim());
                    
                    if (!txtDiasEntrega.getText().trim().isEmpty()) {
                        p.setDiasEntrega(Integer.parseInt(txtDiasEntrega.getText().trim()));
                    }
                    
                    p.setCalificacion(cboCalificacion.getValue());
                    p.setObservaciones(txtObservaciones.getText().trim());
                    
                    return p;
                } catch (Exception e) {
                    mostrarAlerta("Error", "Error en los datos: " + e.getMessage(), Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<Proveedor> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                proveedorService.guardar(p);
                mostrarAlerta("Éxito", "Proveedor guardado correctamente", Alert.AlertType.INFORMATION);
                cargarProveedores();
                actualizarEstadisticasProveedores();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void verDetalleProveedor(Proveedor proveedor) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle del Proveedor");
        alert.setHeaderText(proveedor.getEmpresa());

        StringBuilder contenido = new StringBuilder();
        contenido.append("ID: ").append(proveedor.getId()).append("\n");
        contenido.append("Empresa: ").append(proveedor.getEmpresa()).append("\n");
        contenido.append("Contacto: ").append(proveedor.getNombre()).append("\n");
        contenido.append("Email: ").append(proveedor.getEmail() != null ? proveedor.getEmail() : "N/A").append("\n");
        contenido.append("Teléfono: ").append(proveedor.getTelefono() != null ? proveedor.getTelefono() : "N/A").append("\n");
        if (proveedor.getTelefonoSecundario() != null && !proveedor.getTelefonoSecundario().isEmpty()) {
            contenido.append("Teléfono 2: ").append(proveedor.getTelefonoSecundario()).append("\n");
        }
        contenido.append("Dirección: ").append(proveedor.getDireccion() != null ? proveedor.getDireccion() : "N/A").append("\n");
        contenido.append("Ciudad: ").append(proveedor.getCiudad() != null ? proveedor.getCiudad() : "N/A").append("\n");
        contenido.append("CP: ").append(proveedor.getCodigoPostal() != null ? proveedor.getCodigoPostal() : "N/A").append("\n");
        contenido.append("País: ").append(proveedor.getPais() != null ? proveedor.getPais() : "N/A").append("\n");
        contenido.append("NIF: ").append(proveedor.getNif() != null ? proveedor.getNif() : "N/A").append("\n");
        contenido.append("Web: ").append(proveedor.getSitioWeb() != null ? proveedor.getSitioWeb() : "N/A").append("\n");
        contenido.append("Calificación: ").append("⭐".repeat(Math.max(0, proveedor.getCalificacion() != null ? proveedor.getCalificacion() : 0))).append("\n\n");
        
        if (proveedor.getProductosQueOfrece() != null && !proveedor.getProductosQueOfrece().isEmpty()) {
            contenido.append("Productos que ofrece:\n").append(proveedor.getProductosQueOfrece()).append("\n\n");
        }
        
        if (proveedor.getCondicionesPago() != null && !proveedor.getCondicionesPago().isEmpty()) {
            contenido.append("Condiciones de pago: ").append(proveedor.getCondicionesPago()).append("\n");
        }
        
        if (proveedor.getDiasEntrega() != null) {
            contenido.append("Días de entrega: ").append(proveedor.getDiasEntrega()).append(" días\n");
        }
        
        if (proveedor.getObservaciones() != null && !proveedor.getObservaciones().isEmpty()) {
            contenido.append("\nObservaciones:\n").append(proveedor.getObservaciones());
        }

        alert.setContentText(contenido.toString());
        alert.showAndWait();
    }

    private void eliminarProveedor(Proveedor proveedor) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar proveedor?");
        confirmacion.setContentText("Se desactivará el proveedor: " + proveedor.getEmpresa());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                proveedorService.desactivar(proveedor.getId());
                mostrarAlerta("Éxito", "Proveedor eliminado correctamente", Alert.AlertType.INFORMATION);
                cargarProveedores();
                actualizarEstadisticasProveedores();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void actualizarEstadisticasProveedores() {
        ProveedorService.EstadisticasProveedores stats = proveedorService.obtenerEstadisticas();
        Label lblEstadisticas = (Label) content.lookup("#lblEstadisticasProveedores");
        if (lblEstadisticas != null) {
            lblEstadisticas.setText(String.format(
                "📊 Total Proveedores: %d | ⭐ Excelentes (5★): %d",
                stats.getTotalProveedores(), stats.getProveedoresExcelentes()
            ));
        }
    }

    // ==================== PANEL DE PEDIDOS ====================
    
    private VBox crearPanelPedidos() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label titulo = new Label("📦 Gestión de Pedidos");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label lblEstadisticas = new Label("📊 Cargando estadísticas...");
        lblEstadisticas.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        lblEstadisticas.setId("lblEstadisticasPedidos");

        // Barra de herramientas
        HBox toolBar = new HBox(10);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        Button btnNuevoPedido = new Button("➕ Nuevo Pedido");
        btnNuevoPedido.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        btnNuevoPedido.setOnAction(e -> abrirFormularioNuevoPedido());

        ComboBox<String> cboFiltroEstado = new ComboBox<>();
        cboFiltroEstado.getItems().addAll("Todos", "Pendientes", "Recibidos", "Cancelados");
        cboFiltroEstado.setValue("Todos");
        cboFiltroEstado.setOnAction(e -> filtrarPedidosPorEstado(cboFiltroEstado.getValue()));

        Button btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnRefrescar.setOnAction(e -> cargarPedidos());

        toolBar.getChildren().addAll(btnNuevoPedido, new Separator(javafx.geometry.Orientation.VERTICAL),
                                     new Label("Filtrar:"), cboFiltroEstado, btnRefrescar);

        // Tabla de pedidos
        tablaPedidos = crearTablaPedidos();

        vbox.getChildren().addAll(titulo, lblEstadisticas, toolBar, tablaPedidos);

        // Cargar datos iniciales
        cargarPedidos();
        actualizarEstadisticasPedidos();

        return vbox;
    }

    private TableView<Pedido> crearTablaPedidos() {
        TableView<Pedido> tabla = new TableView<>();
        tabla.setPrefHeight(500);

        TableColumn<Pedido, String> colNumero = new TableColumn<>("Nº Pedido");
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroPedido"));
        colNumero.setPrefWidth(150);

        TableColumn<Pedido, String> colProveedor = new TableColumn<>("Proveedor");
        colProveedor.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProveedor().getEmpresa()));
        colProveedor.setPrefWidth(200);

        TableColumn<Pedido, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return new SimpleStringProperty(cellData.getValue().getFechaPedido().format(formatter));
        });
        colFecha.setPrefWidth(100);

        TableColumn<Pedido, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEstado().getDescripcion()));
        colEstado.setPrefWidth(120);

        TableColumn<Pedido, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("€%.2f", cellData.getValue().getTotal())));
        colTotal.setPrefWidth(100);

        TableColumn<Pedido, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(150);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁️");
            private final Button btnRecibir = new Button("✅");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Pedido pedido = (Pedido) getTableRow().getItem();
                    
                    btnVer.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                    btnVer.setOnAction(e -> verDetallePedido(pedido));

                    btnRecibir.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                    btnRecibir.setOnAction(e -> marcarPedidoRecibido(pedido));
                    btnRecibir.setDisable(pedido.getEstado() == EstadoPedido.RECIBIDO || 
                                         pedido.getEstado() == EstadoPedido.CANCELADO);
                    
                    HBox buttons = new HBox(5, btnVer, btnRecibir);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        tabla.getColumns().addAll(colNumero, colProveedor, colFecha, colEstado, colTotal, colAcciones);
        tabla.setItems(pedidosData);

        return tabla;
    }

    private void cargarPedidos() {
        pedidosData.clear();
        List<Pedido> pedidos = pedidoService.obtenerTodosActivos();
        pedidosData.addAll(pedidos);
    }

    private void filtrarPedidosPorEstado(String filtro) {
        pedidosData.clear();
        List<Pedido> pedidos;
        
        switch (filtro) {
            case "Pendientes":
                pedidos = pedidoService.obtenerPendientes();
                break;
            case "Recibidos":
                pedidos = pedidoService.obtenerPorEstado(EstadoPedido.RECIBIDO);
                break;
            case "Cancelados":
                pedidos = pedidoService.obtenerPorEstado(EstadoPedido.CANCELADO);
                break;
            default:
                pedidos = pedidoService.obtenerTodosActivos();
        }
        
        pedidosData.addAll(pedidos);
    }

    private void verDetallePedido(Pedido pedido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle del Pedido");
        alert.setHeaderText("Pedido: " + pedido.getNumeroPedido());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder contenido = new StringBuilder();
        contenido.append("Proveedor: ").append(pedido.getProveedor().getEmpresa()).append("\n");
        contenido.append("Fecha: ").append(pedido.getFechaPedido().format(formatter)).append("\n");
        contenido.append("Estado: ").append(pedido.getEstado().getDescripcion()).append("\n\n");
        
        contenido.append("PRODUCTOS:\n");
        contenido.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        pedido.getDetalles().forEach(detalle -> {
            String nombre = detalle.getNombreProducto() != null ? detalle.getNombreProducto() :
                           (detalle.getProducto() != null ? detalle.getProducto().getNombre() : "Sin nombre");
            contenido.append(String.format("• %s\n", nombre));
            contenido.append(String.format("  Cantidad: %d x €%.2f = €%.2f\n",
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()));
        });
        
        contenido.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        contenido.append(String.format("Subtotal: €%.2f\n", pedido.getSubtotal()));
        if (pedido.getDescuento() != null && pedido.getDescuento().compareTo(java.math.BigDecimal.ZERO) > 0) {
            contenido.append(String.format("Descuento: €%.2f\n", pedido.getDescuento()));
        }
        if (pedido.getIva() != null && pedido.getIva().compareTo(java.math.BigDecimal.ZERO) > 0) {
            contenido.append(String.format("IVA: €%.2f\n", pedido.getIva()));
        }
        contenido.append(String.format("TOTAL: €%.2f\n", pedido.getTotal()));
        
        if (pedido.getObservaciones() != null && !pedido.getObservaciones().isEmpty()) {
            contenido.append("\nObservaciones:\n").append(pedido.getObservaciones());
        }

        alert.setContentText(contenido.toString());
        alert.showAndWait();
    }

    private void marcarPedidoRecibido(Pedido pedido) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Recepción");
        confirmacion.setHeaderText("¿Marcar pedido como recibido?");
        confirmacion.setContentText("Se actualizará el stock de los productos incluidos en el pedido.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                pedidoService.marcarComoRecibido(pedido.getId());
                mostrarAlerta("Éxito", "Pedido marcado como recibido y stock actualizado", Alert.AlertType.INFORMATION);
                cargarPedidos();
                actualizarEstadisticasPedidos();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al marcar pedido: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void actualizarEstadisticasPedidos() {
        PedidoService.EstadisticasPedidos stats = pedidoService.obtenerEstadisticas();
        Label lblEstadisticas = (Label) content.lookup("#lblEstadisticasPedidos");
        if (lblEstadisticas != null) {
            lblEstadisticas.setText(String.format(
                "📊 Pendientes: %d | ✅ Recibidos: %d | ❌ Cancelados: %d",
                stats.getPedidosPendientes(), stats.getPedidosRecibidos(), stats.getPedidosCancelados()
            ));
        }
    }

    private void abrirFormularioNuevoPedido() {
        CrearPedidoWindow ventana = new CrearPedidoWindow(
            proveedorService, 
            pedidoService, 
            productoService, 
            () -> {
                cargarPedidos();
                actualizarEstadisticasPedidos();
            }
        );
        ventana.mostrar();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

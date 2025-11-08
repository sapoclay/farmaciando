package com.farmacia.ui;

import com.farmacia.model.Cliente;
import com.farmacia.service.ClienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Panel de gestión de clientes
 */
public class ClientesPanel extends VBox {

    private final ClienteService clienteService;

    // Componentes del formulario
    private TextField txtNombre;
    private TextField txtApellido;
    private TextField txtDocumento;
    private ComboBox<String> cbTipoDocumento;
    private TextField txtTelefono;
    private TextField txtEmail;
    private TextField txtDireccion;
    private TextField txtCiudad;
    private TextField txtCodigoPostal;
    private TextArea txtObservaciones;

    // Tabla de clientes
    private TableView<Cliente> tablaClientes;
    private ObservableList<Cliente> listaClientes;

    // Campo de búsqueda
    private TextField txtBusqueda;
    private ComboBox<String> cbFiltroBusqueda;

    // Cliente seleccionado para edición
    private Cliente clienteActual = null;

    // Formato de fecha
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ClientesPanel(ClienteService clienteService) {
        this.clienteService = clienteService;
        this.listaClientes = FXCollections.observableArrayList();

        initUI();
        cargarClientes();
    }

    private void initUI() {
        setPadding(new Insets(20));
        setSpacing(15);

        // Título
        Label lblTitulo = new Label("📋 Gestión de Clientes");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Crear TabPane con dos pestañas
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Pestaña 1: Registro de Cliente
        Tab tabRegistro = new Tab("➕ Registro de Cliente");
        tabRegistro.setContent(crearPanelRegistro());

        // Pestaña 2: Listado de Clientes
        Tab tabListado = new Tab("📑 Listado de Clientes");
        tabListado.setContent(crearPanelListado());

        tabPane.getTabs().addAll(tabRegistro, tabListado);

        getChildren().addAll(lblTitulo, tabPane);
    }

    /**
     * Crear panel de registro/edición de clientes
     */
    private VBox crearPanelRegistro() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        // Título del formulario
        Label lblFormulario = new Label("Datos del Cliente");
        lblFormulario.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Grid para el formulario
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        // Fila 0: Nombre y Apellido
        grid.add(new Label("Nombre:*"), 0, 0);
        txtNombre = new TextField();
        txtNombre.setPromptText("Ingrese el nombre");
        txtNombre.setPrefWidth(250);
        grid.add(txtNombre, 1, 0);

        grid.add(new Label("Apellido:*"), 2, 0);
        txtApellido = new TextField();
        txtApellido.setPromptText("Ingrese el apellido");
        txtApellido.setPrefWidth(250);
        grid.add(txtApellido, 3, 0);

        // Fila 1: Tipo de Documento y Documento
        grid.add(new Label("Tipo Doc:*"), 0, 1);
        cbTipoDocumento = new ComboBox<>();
        cbTipoDocumento.getItems().addAll("DNI", "NIE", "PASAPORTE", "CIF");
        cbTipoDocumento.setValue("DNI");
        cbTipoDocumento.setPrefWidth(250);
        grid.add(cbTipoDocumento, 1, 1);

        grid.add(new Label("Documento:*"), 2, 1);
        txtDocumento = new TextField();
        txtDocumento.setPromptText("Número de documento");
        txtDocumento.setPrefWidth(250);
        grid.add(txtDocumento, 3, 1);

        // Fila 2: Teléfono y Email
        grid.add(new Label("Teléfono:"), 0, 2);
        txtTelefono = new TextField();
        txtTelefono.setPromptText("Número de teléfono");
        txtTelefono.setPrefWidth(250);
        grid.add(txtTelefono, 1, 2);

        grid.add(new Label("Email:"), 2, 2);
        txtEmail = new TextField();
        txtEmail.setPromptText("correo@ejemplo.com");
        txtEmail.setPrefWidth(250);
        grid.add(txtEmail, 3, 2);

        // Fila 3: Dirección
        grid.add(new Label("Dirección:"), 0, 3);
        txtDireccion = new TextField();
        txtDireccion.setPromptText("Calle, número, piso...");
        txtDireccion.setPrefWidth(560);
        GridPane.setColumnSpan(txtDireccion, 3);
        grid.add(txtDireccion, 1, 3);

        // Fila 4: Ciudad y Código Postal
        grid.add(new Label("Ciudad:"), 0, 4);
        txtCiudad = new TextField();
        txtCiudad.setPromptText("Ciudad");
        txtCiudad.setPrefWidth(250);
        grid.add(txtCiudad, 1, 4);

        grid.add(new Label("C.P.:"), 2, 4);
        txtCodigoPostal = new TextField();
        txtCodigoPostal.setPromptText("Código Postal");
        txtCodigoPostal.setPrefWidth(250);
        grid.add(txtCodigoPostal, 3, 4);

        // Fila 5: Observaciones
        grid.add(new Label("Observaciones:"), 0, 5);
        txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Notas adicionales sobre el cliente");
        txtObservaciones.setPrefRowCount(3);
        txtObservaciones.setPrefWidth(560);
        txtObservaciones.setWrapText(true);
        GridPane.setColumnSpan(txtObservaciones, 3);
        grid.add(txtObservaciones, 1, 5);

        // Botones
        HBox botonesBox = new HBox(10);
        botonesBox.setAlignment(Pos.CENTER_LEFT);
        botonesBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnGuardar = new Button("💾 Guardar Cliente");
        btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGuardar.setPrefWidth(150);
        btnGuardar.setOnAction(e -> guardarCliente());

        Button btnLimpiar = new Button("🔄 Limpiar Formulario");
        btnLimpiar.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLimpiar.setPrefWidth(160);
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        botonesBox.getChildren().addAll(btnGuardar, btnLimpiar);

        // Nota de campos obligatorios
        Label lblNota = new Label("* Campos obligatorios");
        lblNota.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

        panel.getChildren().addAll(lblFormulario, grid, botonesBox, lblNota);
        return panel;
    }

    /**
     * Crear panel de listado de clientes
     */
    private VBox crearPanelListado() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        // Panel de búsqueda
        HBox panelBusqueda = new HBox(10);
        panelBusqueda.setAlignment(Pos.CENTER_LEFT);

        Label lblBusqueda = new Label("🔍 Buscar:");
        lblBusqueda.setFont(Font.font("System", FontWeight.BOLD, 14));

        cbFiltroBusqueda = new ComboBox<>();
        cbFiltroBusqueda.getItems().addAll("Todos", "Nombre", "Documento", "Email", "Teléfono", "Ciudad");
        cbFiltroBusqueda.setValue("Todos");
        cbFiltroBusqueda.setPrefWidth(120);

        txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Ingrese texto de búsqueda");
        txtBusqueda.setPrefWidth(300);
        txtBusqueda.textProperty().addListener((obs, old, newVal) -> filtrarClientes());

        Button btnBuscar = new Button("🔍 Buscar");
        btnBuscar.setOnAction(e -> filtrarClientes());

        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setOnAction(e -> cargarClientes());

        panelBusqueda.getChildren().addAll(lblBusqueda, cbFiltroBusqueda, txtBusqueda, btnBuscar, btnActualizar);

        // Tabla de clientes
        tablaClientes = new TableView<>();
        tablaClientes.setItems(listaClientes);
        tablaClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columna ID
        TableColumn<Cliente, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId().toString()));
        colId.setPrefWidth(50);

        // Columna Nombre Completo
        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre Completo");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colNombre.setPrefWidth(200);

        // Columna Tipo Documento
        TableColumn<Cliente, String> colTipoDoc = new TableColumn<>("Tipo Doc");
        colTipoDoc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoDocumento()));
        colTipoDoc.setPrefWidth(80);

        // Columna Documento
        TableColumn<Cliente, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumento()));
        colDocumento.setPrefWidth(120);

        // Columna Teléfono
        TableColumn<Cliente, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTelefono() != null ? data.getValue().getTelefono() : "-"));
        colTelefono.setPrefWidth(120);

        // Columna Email
        TableColumn<Cliente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEmail() != null ? data.getValue().getEmail() : "-"));
        colEmail.setPrefWidth(200);

        // Columna Ciudad
        TableColumn<Cliente, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCiudad() != null ? data.getValue().getCiudad() : "-"));
        colCiudad.setPrefWidth(120);

        // Columna Fecha Registro
        TableColumn<Cliente, String> colFecha = new TableColumn<>("Fecha Registro");
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaRegistro().format(formatter)));
        colFecha.setPrefWidth(140);

        // Columna Acciones
        TableColumn<Cliente, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️ Editar");
            private final Button btnEliminar = new Button("🗑️ Eliminar");
            private final HBox pane = new HBox(5, btnEditar, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px;");
                btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");

                btnEditar.setOnAction(e -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());
                    editarCliente(cliente);
                });

                btnEliminar.setOnAction(e -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());
                    eliminarCliente(cliente);
                });

                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        colAcciones.setPrefWidth(180);

        tablaClientes.getColumns().addAll(colId, colNombre, colTipoDoc, colDocumento, 
                colTelefono, colEmail, colCiudad, colFecha, colAcciones);

        // Panel de estadísticas
        HBox panelStats = new HBox(20);
        panelStats.setAlignment(Pos.CENTER_LEFT);
        panelStats.setPadding(new Insets(10));
        panelStats.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        Label lblTotalClientes = new Label("👥 Total Clientes: 0");
        lblTotalClientes.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTotalClientes.setId("lblTotalClientes");

        panelStats.getChildren().addAll(lblTotalClientes);

        panel.getChildren().addAll(panelBusqueda, tablaClientes, panelStats);
        return panel;
    }

    /**
     * Guardar o actualizar cliente
     */
    private void guardarCliente() {
        try {
            // Crear o actualizar cliente
            Cliente cliente = clienteActual != null ? clienteActual : new Cliente();

            cliente.setNombre(txtNombre.getText().trim());
            cliente.setApellido(txtApellido.getText().trim());
            cliente.setDocumento(txtDocumento.getText().trim());
            cliente.setTipoDocumento(cbTipoDocumento.getValue());
            cliente.setTelefono(txtTelefono.getText().trim().isEmpty() ? null : txtTelefono.getText().trim());
            cliente.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            cliente.setDireccion(txtDireccion.getText().trim().isEmpty() ? null : txtDireccion.getText().trim());
            cliente.setCiudad(txtCiudad.getText().trim().isEmpty() ? null : txtCiudad.getText().trim());
            cliente.setCodigoPostal(txtCodigoPostal.getText().trim().isEmpty() ? null : txtCodigoPostal.getText().trim());
            cliente.setObservaciones(txtObservaciones.getText().trim().isEmpty() ? null : txtObservaciones.getText().trim());

            // Guardar
            if (clienteActual == null) {
                clienteService.registrarCliente(cliente);
                mostrarMensaje("Cliente registrado exitosamente", Alert.AlertType.INFORMATION);
            } else {
                clienteService.actualizarCliente(cliente);
                mostrarMensaje("Cliente actualizado exitosamente", Alert.AlertType.INFORMATION);
            }

            limpiarFormulario();
            cargarClientes();

        } catch (Exception e) {
            mostrarMensaje("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Editar un cliente
     */
    private void editarCliente(Cliente cliente) {
        clienteActual = cliente;

        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido());
        txtDocumento.setText(cliente.getDocumento());
        cbTipoDocumento.setValue(cliente.getTipoDocumento());
        txtTelefono.setText(cliente.getTelefono());
        txtEmail.setText(cliente.getEmail());
        txtDireccion.setText(cliente.getDireccion());
        txtCiudad.setText(cliente.getCiudad());
        txtCodigoPostal.setText(cliente.getCodigoPostal());
        txtObservaciones.setText(cliente.getObservaciones());

        mostrarMensaje("Editando cliente: " + cliente.getNombreCompleto() + 
                "\nModifique los datos y presione 'Guardar Cliente'", Alert.AlertType.INFORMATION);
    }

    /**
     * Eliminar un cliente
     */
    private void eliminarCliente(Cliente cliente) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este cliente?");
        confirmacion.setContentText(cliente.getNombreCompleto() + " - " + cliente.getDocumento());

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                clienteService.eliminarCliente(cliente.getId());
                mostrarMensaje("Cliente eliminado exitosamente", Alert.AlertType.INFORMATION);
                cargarClientes();
            } catch (Exception e) {
                mostrarMensaje("Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Limpiar el formulario
     */
    private void limpiarFormulario() {
        clienteActual = null;
        txtNombre.clear();
        txtApellido.clear();
        txtDocumento.clear();
        cbTipoDocumento.setValue("DNI");
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        txtCiudad.clear();
        txtCodigoPostal.clear();
        txtObservaciones.clear();
        txtNombre.requestFocus();
    }

    /**
     * Cargar todos los clientes
     */
    private void cargarClientes() {
        listaClientes.clear();
        listaClientes.addAll(clienteService.obtenerClientesActivos());
        actualizarEstadisticas();
    }

    /**
     * Filtrar clientes según criterio de búsqueda
     */
    private void filtrarClientes() {
        String busqueda = txtBusqueda.getText().trim();
        String filtro = cbFiltroBusqueda.getValue();

        listaClientes.clear();

        try {
            if (busqueda.isEmpty()) {
                listaClientes.addAll(clienteService.obtenerClientesActivos());
            } else {
                switch (filtro) {
                    case "Nombre":
                        listaClientes.addAll(clienteService.buscarPorNombre(busqueda));
                        break;
                    case "Documento":
                        Optional<Cliente> cliente = clienteService.obtenerClientePorDocumento(busqueda);
                        cliente.ifPresent(listaClientes::add);
                        break;
                    case "Email":
                        listaClientes.addAll(clienteService.buscarPorEmail(busqueda));
                        break;
                    case "Teléfono":
                        listaClientes.addAll(clienteService.buscarPorTelefono(busqueda));
                        break;
                    case "Ciudad":
                        listaClientes.addAll(clienteService.buscarPorCiudad(busqueda));
                        break;
                    default:
                        listaClientes.addAll(clienteService.busquedaGeneral(busqueda));
                }
            }
        } catch (Exception e) {
            mostrarMensaje("Error en búsqueda: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        actualizarEstadisticas();
    }

    /**
     * Actualizar estadísticas
     */
    private void actualizarEstadisticas() {
        Label lblTotal = (Label) lookup("#lblTotalClientes");
        if (lblTotal != null) {
            lblTotal.setText("👥 Total Clientes: " + listaClientes.size());
        }
    }

    /**
     * Mostrar mensaje al usuario
     */
    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(tipo == Alert.AlertType.ERROR ? "Error" : "Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

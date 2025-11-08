package com.farmacia.ui;

import com.farmacia.model.Usuario;
import com.farmacia.service.UsuarioService;
import javafx.beans.property.SimpleObjectProperty;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class GestionUsuariosPanel extends VBox {
    
    private final UsuarioService usuarioService;
    private final Usuario usuarioActual;
    
    private TableView<Usuario> tablaUsuarios;
    private ObservableList<Usuario> listaUsuarios;
    private TextField searchField;
    
    public GestionUsuariosPanel(UsuarioService usuarioService, Usuario usuarioActual) {
        this.usuarioService = usuarioService;
        this.usuarioActual = usuarioActual;
        
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f5f5f5;");
        
        initUI();
        cargarUsuarios();
    }
    
    private void initUI() {
        // Header
        HBox header = crearHeader();
        
        // Tabla de usuarios (PRIMERO crear la tabla)
        tablaUsuarios = crearTabla();
        VBox.setVgrow(tablaUsuarios, Priority.ALWAYS);
        
        // Barra de herramientas (DESPUÉS crear toolbar que depende de la tabla)
        HBox toolbar = crearToolbar();
        
        getChildren().addAll(header, toolbar, tablaUsuarios);
    }
    
    private HBox crearHeader() {
        Label titleLabel = new Label("👥 Gestión de Usuarios");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        Label infoLabel = new Label("Total de usuarios: ");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        Label countLabel = new Label("0");
        countLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        countLabel.textProperty().bind(new SimpleStringProperty("0"));
        
        // Actualizar contador
        listaUsuarios = FXCollections.observableArrayList();
        listaUsuarios.addListener((javafx.collections.ListChangeListener.Change<? extends Usuario> c) -> {
            countLabel.setText(String.valueOf(listaUsuarios.size()));
        });
        
        HBox infoBox = new HBox(5, infoLabel, countLabel);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox header = new HBox(20, titleLabel, spacer, infoBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        return header;
    }
    
    private HBox crearToolbar() {
        // Búsqueda
        searchField = new TextField();
        searchField.setPromptText("🔍 Buscar por nombre o usuario...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, old, nuevo) -> filtrarUsuarios(nuevo));
        
        // Botones
        Button btnNuevo = new Button("➕ Nuevo Usuario");
        btnNuevo.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnNuevo.setOnAction(e -> mostrarFormularioNuevo());
        
        Button btnEditar = new Button("✏️ Editar");
        btnEditar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnEditar.setOnAction(e -> editarSeleccionado());
        btnEditar.disableProperty().bind(tablaUsuarios.getSelectionModel().selectedItemProperty().isNull());
        
        Button btnCambiarPassword = new Button("🔑 Cambiar Contraseña");
        btnCambiarPassword.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnCambiarPassword.setOnAction(e -> cambiarPasswordSeleccionado());
        btnCambiarPassword.disableProperty().bind(tablaUsuarios.getSelectionModel().selectedItemProperty().isNull());
        
        Button btnToggleActivo = new Button("🔄 Activar/Desactivar");
        btnToggleActivo.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnToggleActivo.setOnAction(e -> toggleActivoSeleccionado());
        btnToggleActivo.disableProperty().bind(tablaUsuarios.getSelectionModel().selectedItemProperty().isNull());
        
        Button btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnRefrescar.setOnAction(e -> cargarUsuarios());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        toolbar.getChildren().addAll(searchField, spacer, btnNuevo, btnEditar, btnCambiarPassword, btnToggleActivo, btnRefrescar);
        
        return toolbar;
    }
    
    private TableView<Usuario> crearTabla() {
        TableView<Usuario> tabla = new TableView<>();
        tabla.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        // Columnas
        TableColumn<Usuario, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);
        
        TableColumn<Usuario, String> colUsername = new TableColumn<>("Usuario");
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUsername.setPrefWidth(150);
        
        TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre Completo");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colNombre.setPrefWidth(250);
        
        TableColumn<Usuario, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRol().toString()));
        colRol.setPrefWidth(120);
        colRol.setCellFactory(col -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Administrador")) {
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        TableColumn<Usuario, String> colActivo = new TableColumn<>("Estado");
        colActivo.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getActivo() ? "✓ Activo" : "✗ Inactivo"));
        colActivo.setPrefWidth(100);
        colActivo.setCellFactory(col -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("✓")) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        TableColumn<Usuario, String> colFechaCreacion = new TableColumn<>("Fecha Creación");
        colFechaCreacion.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFechaCreacion().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colFechaCreacion.setPrefWidth(150);
        
        TableColumn<Usuario, String> colUltimoAcceso = new TableColumn<>("Último Acceso");
        colUltimoAcceso.setCellValueFactory(cellData -> {
            var ultimo = cellData.getValue().getUltimoAcceso();
            return new SimpleStringProperty(ultimo != null ? 
                ultimo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Nunca");
        });
        colUltimoAcceso.setPrefWidth(150);
        
        tabla.getColumns().addAll(colId, colUsername, colNombre, colRol, colActivo, colFechaCreacion, colUltimoAcceso);
        tabla.setItems(listaUsuarios);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return tabla;
    }
    
    private void cargarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        listaUsuarios.setAll(usuarios);
    }
    
    private void filtrarUsuarios(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarUsuarios();
            return;
        }
        
        String filtroLower = filtro.toLowerCase();
        List<Usuario> usuarios = usuarioService.listarTodos();
        List<Usuario> filtrados = usuarios.stream()
            .filter(u -> u.getUsername().toLowerCase().contains(filtroLower) ||
                        u.getNombreCompleto().toLowerCase().contains(filtroLower))
            .toList();
        
        listaUsuarios.setAll(filtrados);
    }
    
    private void mostrarFormularioNuevo() {
        UsuarioFormDialog dialog = new UsuarioFormDialog(null, usuarioService);
        dialog.showAndWait().ifPresent(usuario -> cargarUsuarios());
    }
    
    private void editarSeleccionado() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            UsuarioFormDialog dialog = new UsuarioFormDialog(seleccionado, usuarioService);
            dialog.showAndWait().ifPresent(usuario -> cargarUsuarios());
        }
    }
    
    private void cambiarPasswordSeleccionado() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            CambiarPasswordDialog dialog = new CambiarPasswordDialog(seleccionado, usuarioService);
            dialog.showAndWait();
        }
    }
    
    private void toggleActivoSeleccionado() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        
        // No permitir desactivar al propio usuario
        if (seleccionado.getId().equals(usuarioActual.getId())) {
            mostrarAlerta("No puedes desactivar tu propio usuario", Alert.AlertType.WARNING);
            return;
        }
        
        String accion = seleccionado.getActivo() ? "desactivar" : "activar";
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar acción");
        confirmacion.setHeaderText("¿Está seguro de " + accion + " este usuario?");
        confirmacion.setContentText("Usuario: " + seleccionado.getUsername());
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (seleccionado.getActivo()) {
                    usuarioService.desactivarUsuario(seleccionado.getId());
                } else {
                    usuarioService.activarUsuario(seleccionado.getId());
                }
                cargarUsuarios();
            }
        });
    }
    
    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    // ===== DIÁLOGO DE FORMULARIO =====
    
    private static class UsuarioFormDialog extends Dialog<Usuario> {
        
        private final Usuario usuario; // null si es nuevo
        private final UsuarioService usuarioService;
        
        private TextField usernameField;
        private TextField nombreField;
        private PasswordField passwordField;
        private PasswordField confirmPasswordField;
        private ComboBox<Usuario.Rol> rolComboBox;
        
        public UsuarioFormDialog(Usuario usuario, UsuarioService usuarioService) {
            this.usuario = usuario;
            this.usuarioService = usuarioService;
            
            setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            setHeaderText(usuario == null ? "Crear un nuevo usuario" : "Modificar usuario: " + usuario.getUsername());
            
            // Botones
            ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
            
            // Formulario
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            
            usernameField = new TextField();
            usernameField.setPromptText("usuario123");
            if (usuario != null) {
                usernameField.setText(usuario.getUsername());
                usernameField.setDisable(true); // No cambiar username en edición
            }
            
            nombreField = new TextField();
            nombreField.setPromptText("Juan Pérez");
            if (usuario != null) {
                nombreField.setText(usuario.getNombreCompleto());
            }
            
            passwordField = new PasswordField();
            passwordField.setPromptText(usuario == null ? "********" : "(dejar vacío para no cambiar)");
            
            confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText(usuario == null ? "********" : "(dejar vacío para no cambiar)");
            
            rolComboBox = new ComboBox<>();
            rolComboBox.getItems().addAll(Usuario.Rol.values());
            if (usuario != null) {
                rolComboBox.setValue(usuario.getRol());
            } else {
                rolComboBox.setValue(Usuario.Rol.CAJERO);
            }
            
            grid.add(new Label("Usuario:"), 0, 0);
            grid.add(usernameField, 1, 0);
            
            grid.add(new Label("Nombre Completo:"), 0, 1);
            grid.add(nombreField, 1, 1);
            
            if (usuario == null) { // Solo en creación
                grid.add(new Label("Contraseña:"), 0, 2);
                grid.add(passwordField, 1, 2);
                
                grid.add(new Label("Confirmar Contraseña:"), 0, 3);
                grid.add(confirmPasswordField, 1, 3);
                
                grid.add(new Label("Rol:"), 0, 4);
                grid.add(rolComboBox, 1, 4);
            } else {
                grid.add(new Label("Rol:"), 0, 2);
                grid.add(rolComboBox, 1, 2);
            }
            
            getDialogPane().setContent(grid);
            
            // Validación y resultado
            setResultConverter(dialogButton -> {
                if (dialogButton == btnGuardar) {
                    try {
                        if (usuario == null) {
                            // Crear nuevo
                            String username = usernameField.getText().trim();
                            String nombre = nombreField.getText().trim();
                            String password = passwordField.getText();
                            String confirmPassword = confirmPasswordField.getText();
                            Usuario.Rol rol = rolComboBox.getValue();
                            
                            // Validaciones
                            if (username.isEmpty() || nombre.isEmpty() || password.isEmpty()) {
                                mostrarError("Todos los campos son obligatorios");
                                return null;
                            }
                            
                            if (!password.equals(confirmPassword)) {
                                mostrarError("Las contraseñas no coinciden");
                                return null;
                            }
                            
                            if (password.length() < 6) {
                                mostrarError("La contraseña debe tener al menos 6 caracteres");
                                return null;
                            }
                            
                            return usuarioService.crearUsuario(username, password, nombre, rol);
                            
                        } else {
                            // Editar existente
                            String nombre = nombreField.getText().trim();
                            Usuario.Rol rol = rolComboBox.getValue();
                            
                            if (nombre.isEmpty()) {
                                mostrarError("El nombre no puede estar vacío");
                                return null;
                            }
                            
                            return usuarioService.actualizarUsuario(usuario.getId(), nombre, rol, usuario.getActivo());
                        }
                    } catch (Exception e) {
                        mostrarError("Error al guardar: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });
        }
        
        private void mostrarError(String mensaje) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }
    }
    
    // ===== DIÁLOGO CAMBIAR PASSWORD =====
    
    private static class CambiarPasswordDialog extends Dialog<Void> {
        
        public CambiarPasswordDialog(Usuario usuario, UsuarioService usuarioService) {
            setTitle("Cambiar Contraseña");
            setHeaderText("Cambiar contraseña de: " + usuario.getUsername());
            
            ButtonType btnCambiar = new ButtonType("Cambiar", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(btnCambiar, ButtonType.CANCEL);
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            
            PasswordField nuevaPasswordField = new PasswordField();
            nuevaPasswordField.setPromptText("Nueva contraseña");
            
            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirmar nueva contraseña");
            
            grid.add(new Label("Nueva Contraseña:"), 0, 0);
            grid.add(nuevaPasswordField, 1, 0);
            
            grid.add(new Label("Confirmar Contraseña:"), 0, 1);
            grid.add(confirmPasswordField, 1, 1);
            
            getDialogPane().setContent(grid);
            
            setResultConverter(dialogButton -> {
                if (dialogButton == btnCambiar) {
                    String nuevaPassword = nuevaPasswordField.getText();
                    String confirmPassword = confirmPasswordField.getText();
                    
                    if (nuevaPassword.isEmpty()) {
                        mostrarError("La contraseña no puede estar vacía");
                        return null;
                    }
                    
                    if (!nuevaPassword.equals(confirmPassword)) {
                        mostrarError("Las contraseñas no coinciden");
                        return null;
                    }
                    
                    if (nuevaPassword.length() < 6) {
                        mostrarError("La contraseña debe tener al menos 6 caracteres");
                        return null;
                    }
                    
                    try {
                        usuarioService.cambiarPassword(usuario.getId(), nuevaPassword);
                        mostrarInfo("Contraseña cambiada exitosamente");
                    } catch (Exception e) {
                        mostrarError("Error al cambiar contraseña: " + e.getMessage());
                    }
                }
                return null;
            });
        }
        
        private void mostrarError(String mensaje) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }
        
        private void mostrarInfo(String mensaje) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }
    }
}

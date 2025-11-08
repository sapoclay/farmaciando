package com.farmacia.ui;

import com.farmacia.model.Usuario;
import com.farmacia.service.UsuarioService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

public class LoginWindow {
    
    private final Stage stage;
    private final UsuarioService usuarioService;
    private Consumer<Usuario> onLoginSuccess;
    
    private TextField usernameField;
    private PasswordField passwordField;
    private Label mensajeLabel;
    private Button loginButton;
    
    public LoginWindow(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        this.stage = new Stage();
        initUI();
    }
    
    private void initUI() {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Inicio de Sesión - FarmaCiando");
        
        // Contenedor principal
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 3;");
        
        // Header con logo
        ImageView logoView = null;
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
            logoView = new ImageView(logo);
            logoView.setFitHeight(120);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }
        
        Label subtitleLabel = new Label("Sistema de Gestión de Farmacia");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        if (logoView != null) {
            headerBox.getChildren().add(logoView);
        }
        headerBox.getChildren().add(subtitleLabel);
        
        // Separador
        Separator separator = new Separator();
        separator.setMaxWidth(300);
        
        // Formulario
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(300);
        
        // Usuario
        Label usernameLabel = new Label("Usuario:");
        usernameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        
        usernameField = new TextField();
        usernameField.setPromptText("Ingrese su usuario");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-font-size: 14px; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        VBox usernameBox = new VBox(5, usernameLabel, usernameField);
        
        // Password
        Label passwordLabel = new Label("Contraseña:");
        passwordLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Ingrese su contraseña");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 14px; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        VBox passwordBox = new VBox(5, passwordLabel, passwordField);
        
        // Mensaje de error/éxito
        mensajeLabel = new Label();
        mensajeLabel.setWrapText(true);
        mensajeLabel.setMaxWidth(300);
        mensajeLabel.setStyle("-fx-font-size: 12px;");
        mensajeLabel.setVisible(false);
        
        formBox.getChildren().addAll(usernameBox, passwordBox, mensajeLabel);
        
        // Botones
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        
        loginButton = new Button("🔐 Iniciar Sesión");
        loginButton.setPrefWidth(200);
        loginButton.setPrefHeight(45);
        loginButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 5;"
        );
        
        Button cancelButton = new Button("❌ Salir");
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(45);
        cancelButton.setStyle(
            "-fx-background-color: #f44336; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 5;"
        );
        
        buttonsBox.getChildren().addAll(loginButton, cancelButton);
        
        // Eventos
        loginButton.setOnAction(e -> intentarLogin());
        cancelButton.setOnAction(e -> {
            stage.close();
            System.exit(0); // Salir de la aplicación
        });
        
        // Enter para login
        passwordField.setOnAction(e -> intentarLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        
        // Hover effects
        loginButton.setOnMouseEntered(e -> 
            loginButton.setStyle(loginButton.getStyle() + "-fx-background-color: #45a049;"));
        loginButton.setOnMouseExited(e -> 
            loginButton.setStyle(loginButton.getStyle().replace("-fx-background-color: #45a049;", "-fx-background-color: #4CAF50;")));
        
        cancelButton.setOnMouseEntered(e -> 
            cancelButton.setStyle(cancelButton.getStyle() + "-fx-background-color: #da190b;"));
        cancelButton.setOnMouseExited(e -> 
            cancelButton.setStyle(cancelButton.getStyle().replace("-fx-background-color: #da190b;", "-fx-background-color: #f44336;")));
        
        // Info de usuario por defecto
        Label infoLabel = new Label("💡 Usuario por defecto: admin / admin123");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999; -fx-font-style: italic;");
        
        // Ensamblar
        root.getChildren().addAll(
            headerBox,
            separator,
            formBox,
            buttonsBox,
            infoLabel
        );
        
        Scene scene = new Scene(root, 450, 550);
        stage.setScene(scene);
        stage.setResizable(false);
    }
    
    private void intentarLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validar campos
        if (username.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor, ingrese usuario y contraseña", false);
            return;
        }
        
        // Deshabilitar botón mientras se procesa
        loginButton.setDisable(true);
        mensajeLabel.setVisible(false);
        
        try {
            // Intentar login
            Usuario usuario = usuarioService.login(username, password);
            
            if (usuario != null) {
                mostrarMensaje("✓ Inicio de sesión exitoso", true);
                
                // Esperar un momento antes de cerrar
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        javafx.application.Platform.runLater(() -> {
                            if (onLoginSuccess != null) {
                                onLoginSuccess.accept(usuario);
                            }
                            stage.close();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                mostrarMensaje("❌ Usuario o contraseña incorrectos", false);
                loginButton.setDisable(false);
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (Exception e) {
            mostrarMensaje("❌ Error al conectar: " + e.getMessage(), false);
            loginButton.setDisable(false);
        }
    }
    
    private void mostrarMensaje(String mensaje, boolean esExito) {
        mensajeLabel.setText(mensaje);
        mensajeLabel.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + (esExito ? "#4CAF50" : "#f44336") + ";"
        );
        mensajeLabel.setVisible(true);
    }
    
    /**
     * Establecer callback para cuando el login sea exitoso
     */
    public void setOnLoginSuccess(Consumer<Usuario> callback) {
        this.onLoginSuccess = callback;
    }
    
    /**
     * Mostrar ventana de login
     */
    public void show() {
        usernameField.requestFocus();
        stage.showAndWait();
    }
    
    /**
     * Cerrar ventana
     */
    public void close() {
        stage.close();
    }
}

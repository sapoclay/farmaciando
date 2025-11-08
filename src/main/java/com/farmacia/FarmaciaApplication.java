package com.farmacia;

import com.farmacia.service.ProductoService;
import com.farmacia.service.VentaService;
import com.farmacia.service.ClienteService;
import com.farmacia.service.ProveedorService;
import com.farmacia.service.PedidoService;
import com.farmacia.service.AlertaService;
import com.farmacia.ui.InventarioPanel;
import com.farmacia.ui.VentasPanel;
import com.farmacia.ui.ClientesPanel;
import com.farmacia.ui.ReportesPanel;
import com.farmacia.ui.ProveedoresPanel;
import com.farmacia.ui.AlertasPanel;
import com.farmacia.ui.SplashScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class FarmaciaApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private Stage primaryStage;
    private com.farmacia.model.Usuario usuarioActual; // Usuario que hizo login

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        // Iniciar Spring Boot en segundo plano
        springContext = SpringApplication.run(FarmaciaApplication.class);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Mostrar ventana de login PRIMERO
        com.farmacia.service.UsuarioService usuarioService = springContext.getBean(com.farmacia.service.UsuarioService.class);
        com.farmacia.ui.LoginWindow loginWindow = new com.farmacia.ui.LoginWindow(usuarioService);
        
        loginWindow.setOnLoginSuccess(usuario -> {
            // Guardar usuario que hizo login
            this.usuarioActual = usuario;
            System.out.println("✓ Usuario autenticado: " + usuario.getUsername() + " (" + usuario.getRol() + ")");
        });
        
        loginWindow.show(); // Mostrar login (bloquea hasta que se haga login o se cierre)
        
        // DESPUÉS del login, verificar si se autenticó exitosamente
        if (usuarioActual != null) {
            System.out.println("Iniciando aplicación para usuario: " + usuarioActual.getUsername());
            
            // Mostrar splash screen
            SplashScreen splash = new SplashScreen();
            splash.show(() -> {
                System.out.println("=== CALLBACK DEL SPLASH EJECUTADO ===");
                System.out.println("Usuario actual: " + (usuarioActual != null ? usuarioActual.getUsername() : "NULL"));
                System.out.println("PrimaryStage: " + (primaryStage != null ? "OK" : "NULL"));
                
                // Después del splash, mostrar ventana principal
                try {
                    mostrarVentanaPrincipal();
                    System.out.println("=== mostrarVentanaPrincipal() EJECUTADO ===");
                } catch (Exception e) {
                    System.err.println("ERROR al mostrar ventana principal:");
                    e.printStackTrace();
                }
            });
        } else {
            // Si no hay usuario (cerró la ventana de login), salir de la aplicación
            System.out.println("Login cancelado. Cerrando aplicación...");
            Platform.exit();
            springContext.close();
        }
    }

    private void mostrarVentanaPrincipal() {
        System.out.println(">>> Entrando a mostrarVentanaPrincipal()");
        
        primaryStage.setTitle("FarmaCiando - Sistema de Gestión - Usuario: " + usuarioActual.getNombreCompleto() + " (" + usuarioActual.getRol() + ")");

        // Configurar ícono de la aplicación
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/logo.png")
            );
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("No se pudo cargar el logo: " + e.getMessage());
        }

        // Crear TabPane principal
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // No permitir cerrar pestañas

        // Obtener servicios
        AlertaService alertaService = springContext.getBean(AlertaService.class);
        ProductoService productoService = springContext.getBean(ProductoService.class);
        VentaService ventaService = springContext.getBean(VentaService.class);
        ClienteService clienteService = springContext.getBean(ClienteService.class);
        ProveedorService proveedorService = springContext.getBean(ProveedorService.class);
        PedidoService pedidoService = springContext.getBean(PedidoService.class);
        com.farmacia.service.UsuarioService usuarioService = springContext.getBean(com.farmacia.service.UsuarioService.class);

        // Pestaña 1: Inicio (con alertas y usuario)
        Tab tabInicio = new Tab();
        tabInicio.setGraphic(crearTabConTextoEstilizado("H", "Inicio"));
        tabInicio.setContent(crearPanelInicio(alertaService));

        // Pestaña 2: Inventario
        Tab tabInventario = new Tab();
        tabInventario.setGraphic(crearTabConTextoEstilizado("I", "Inventario"));
        InventarioPanel inventarioPanel = new InventarioPanel(productoService);
        tabInventario.setContent(inventarioPanel.getContent());

        // Pestaña 3: Ventas (PASAR USUARIO ACTUAL)
        Tab tabVentas = new Tab();
        tabVentas.setGraphic(crearTabConTextoEstilizado("V", "Ventas"));
        VentasPanel ventasPanel = new VentasPanel(ventaService, productoService, usuarioActual);
        tabVentas.setContent(ventasPanel.getContent());

        // Pestaña 4: Clientes
        Tab tabClientes = new Tab();
        tabClientes.setGraphic(crearTabConTextoEstilizado("C", "Clientes"));
        ClientesPanel clientesPanel = new ClientesPanel(clienteService);
        tabClientes.setContent(clientesPanel);

        // Pestaña 5: Proveedores
        Tab tabProveedores = new Tab();
        tabProveedores.setGraphic(crearTabConTextoEstilizado("P", "Proveedores"));
        ProveedoresPanel proveedoresPanel = new ProveedoresPanel(proveedorService, pedidoService, productoService);
        tabProveedores.setContent(proveedoresPanel.getContent());

        // Pestaña 6: Reportes
        Tab tabReportes = new Tab();
        tabReportes.setGraphic(crearTabConTextoEstilizado("R", "Reportes"));
        ReportesPanel reportesPanel = new ReportesPanel(ventaService, productoService, clienteService);
        tabReportes.setContent(reportesPanel);

        // Pestaña 7: Alertas
        Tab tabAlertas = new Tab();
        tabAlertas.setGraphic(crearTabConTextoEstilizado("A", "Alertas"));
        AlertasPanel alertasPanel = springContext.getBean(AlertasPanel.class);
        alertasPanel.initialize();
        tabAlertas.setContent(alertasPanel);

        // Pestaña 8: Gestión de Usuarios (SOLO PARA ADMIN)
        Tab tabUsuarios = null;
        if (usuarioActual.getRol() == com.farmacia.model.Usuario.Rol.ADMIN) {
            tabUsuarios = new Tab();
            tabUsuarios.setGraphic(crearTabConTextoEstilizado("U", "Usuarios"));
            com.farmacia.ui.GestionUsuariosPanel gestionUsuariosPanel = new com.farmacia.ui.GestionUsuariosPanel(usuarioService, usuarioActual);
            tabUsuarios.setContent(gestionUsuariosPanel);
        }

        // Añadir pestañas según el rol
        if (tabUsuarios != null) {
            tabPane.getTabs().addAll(tabInicio, tabInventario, tabVentas, tabClientes, tabProveedores, tabReportes, tabAlertas, tabUsuarios);
        } else {
            tabPane.getTabs().addAll(tabInicio, tabInventario, tabVentas, tabClientes, tabProveedores, tabReportes, tabAlertas);
        }

        Scene scene = new Scene(tabPane, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // Iniciar en pantalla completa
        primaryStage.setOnCloseRequest(e -> {
            System.out.println(">>> Usuario cerró la ventana principal");
            Platform.exit();
            springContext.close();
        });
        primaryStage.show();

        System.out.println("========================================");
        System.out.println("FarmaCiando - Sistema iniciado");
        System.out.println("Usuario: " + usuarioActual.getUsername() + " (" + usuarioActual.getRol() + ")");
        System.out.println("Interfaz JavaFX con pestañas cargada correctamente");
        System.out.println("PrimaryStage visible: " + primaryStage.isShowing());
        System.out.println("========================================");
    }

    @Override
    public void stop() {
        System.out.println(">>> MÉTODO stop() LLAMADO - Aplicación cerrándose");
        springContext.close();
    }

    private BorderPane crearPanelInicio(AlertaService alertaService) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(30));

        VBox centerBox = new VBox(25);
        centerBox.setAlignment(Pos.CENTER);

        // Panel de alertas resumido en el inicio
        VBox alertasResumen = crearResumenAlertas(alertaService);
        centerBox.getChildren().add(alertasResumen);

        // Logo más grande con borde
        try {
            javafx.scene.image.Image logoImage = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/logo.png")
            );
            javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(logoImage);
            logoView.setFitHeight(200);
            logoView.setFitWidth(200);
            logoView.setPreserveRatio(true);
            
            // Envolver el ImageView en un StackPane para aplicar el borde correctamente
            StackPane logoContainer = new StackPane(logoView);
            logoContainer.setMaxWidth(200);
            logoContainer.setMaxHeight(200);
            logoContainer.setStyle(
                "-fx-border-color: #cccccc;" +
                "-fx-border-width: 3;" +
                "-fx-border-radius: 10;" +
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 2);"
            );
            
            centerBox.getChildren().add(logoContainer);
        } catch (Exception e) {
            System.out.println("Logo no disponible en la interfaz");
        }

        // Subtítulo (título eliminado ya que está en el logo)
        Label welcomeLabel = new Label("Sistema de Gestión de Farmacia");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");

        // Información técnica
        Label infoLabel = new Label(
            "✓ Java 21 LTS\n" +
            "✓ Spring Boot 3.2.11\n" +
            "✓ JavaFX 21\n" +
            "✓ Base de datos H2 embebida\n" +
            "✓ Multiplataforma (Windows/Linux)"
        );
        infoLabel.setStyle("-fx-font-size: 16px; -fx-line-spacing: 5px;");
        infoLabel.setAlignment(Pos.CENTER);

        // Botón de GitHub
        Button btnGitHub = new Button("🌐 Ver proyecto en GitHub");
        btnGitHub.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-min-width: 300px; " +
            "-fx-min-height: 50px; " +
            "-fx-background-color: #24292e; " +
            "-fx-text-fill: white; " +
            "-fx-cursor: hand;"
        );
        btnGitHub.setOnAction(e -> abrirEnlaceGitHub());

        // Efecto hover para el botón
        btnGitHub.setOnMouseEntered(e -> 
            btnGitHub.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-min-width: 300px; " +
                "-fx-min-height: 50px; " +
                "-fx-background-color: #0366d6; " +
                "-fx-text-fill: white; " +
                "-fx-cursor: hand;"
            )
        );
        btnGitHub.setOnMouseExited(e -> 
            btnGitHub.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-min-width: 300px; " +
                "-fx-min-height: 50px; " +
                "-fx-background-color: #24292e; " +
                "-fx-text-fill: white; " +
                "-fx-cursor: hand;"
            )
        );

        centerBox.getChildren().addAll(welcomeLabel, infoLabel, btnGitHub);

        // Panel inferior con información del sistema
        Label footerLabel = new Label("Sistema iniciado correctamente | Base de datos: ./data/farmacia_db");
        footerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        VBox bottomBox = new VBox(footerLabel);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        root.setCenter(centerBox);
        root.setBottom(bottomBox);

        return root;
    }

    private HBox crearTabConTextoEstilizado(String letra, String texto) {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(3, 8, 3, 8));
        
        // Crear icono circular con letra
        Label iconoLabel = new Label(letra);
        iconoLabel.setMinSize(24, 24);
        iconoLabel.setMaxSize(24, 24);
        iconoLabel.setAlignment(Pos.CENTER);
        iconoLabel.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: #2196F3; " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: white;"
        );
        
        Label textLabel = new Label(texto);
        textLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        container.getChildren().addAll(iconoLabel, textLabel);
        
        return container;
    }
    
    private HBox crearTabConBadge(String texto, AlertaService alertaService) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER);
        
        Label label = new Label(texto);
        
        int numCriticas = alertaService.obtenerAlertasCriticas().size();
        if (numCriticas > 0) {
            Label badge = new Label(String.valueOf(numCriticas));
            badge.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                    "-fx-padding: 2 6; -fx-background-radius: 10; -fx-font-size: 10px; " +
                    "-fx-font-weight: bold; -fx-min-width: 18px; -fx-alignment: center;");
            container.getChildren().addAll(label, badge);
        } else {
            container.getChildren().add(label);
        }
        
        return container;
    }

    private VBox crearResumenAlertas(AlertaService alertaService) {
        VBox container = new VBox(15);
        container.setMaxWidth(800);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // Título
        Label titulo = new Label("⚠️ Resumen de Alertas del Sistema");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Obtener estadísticas
        AlertaService.EstadisticasAlertas stats = alertaService.obtenerEstadisticas();

        // Crear grid con estadísticas
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        int col = 0;
        agregarEstadistica(grid, col++, "📦 Stock Bajo", stats.stockBajo, "#ffc107");
        agregarEstadistica(grid, col++, "🔴 Caducados", stats.caducados, "#dc3545");
        agregarEstadistica(grid, col++, "⚠️ Por Caducar", stats.proximosCaducar, "#ffc107");
        agregarEstadistica(grid, col++, "📋 Pedidos", stats.pedidosPendientes + stats.pedidosRetrasados, "#17a2b8");

        // Mensaje según criticidad
        Label mensaje;
        if (stats.criticas > 0) {
            mensaje = new Label(String.format("⚠️ Hay %d alertas críticas que requieren atención inmediata", stats.criticas));
            mensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
        } else if (stats.total > 0) {
            mensaje = new Label(String.format("✓ Hay %d alertas informativas", stats.total));
            mensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #28a745;");
        } else {
            mensaje = new Label("✓ No hay alertas activas");
            mensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
        }

        container.getChildren().addAll(titulo, grid, mensaje);
        return container;
    }

    private void agregarEstadistica(GridPane grid, int col, String titulo, int valor, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        box.setPrefWidth(150);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        Label lblValor = new Label(String.valueOf(valor));
        lblValor.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        box.getChildren().addAll(lblTitulo, lblValor);
        grid.add(box, col, 0);
    }

    private VBox crearPanelEnDesarrollo(String nombreModulo) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(50));

        Label iconLabel = new Label("🚧");
        iconLabel.setStyle("-fx-font-size: 72px;");

        Label titleLabel = new Label(nombreModulo);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label messageLabel = new Label("Este módulo está en desarrollo");
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");

        box.getChildren().addAll(iconLabel, titleLabel, messageLabel);

        return box;
    }

    private void abrirEnlaceGitHub() {
        try {
            String url = "https://github.com/sapoclay/farmaciando";
            
            // Intentar abrir con Desktop API (funciona en la mayoría de sistemas)
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback: usar comandos del sistema
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder processBuilder;
                
                if (os.contains("win")) {
                    // Windows
                    processBuilder = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
                } else if (os.contains("mac")) {
                    // macOS
                    processBuilder = new ProcessBuilder("open", url);
                } else {
                    // Linux/Unix
                    processBuilder = new ProcessBuilder("xdg-open", url);
                }
                
                processBuilder.start();
            }
            
            System.out.println("Abriendo enlace en el navegador: " + url);
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("GitHub");
            alert.setHeaderText("Proyecto FarmaCiando");
            alert.setContentText("No se pudo abrir el navegador automáticamente.\n\n" +
                               "Puedes visitar el proyecto en:\n" +
                               "https://github.com/sapoclay/farmaciando");
            alert.showAndWait();
            
            System.err.println("Error al abrir el navegador: " + e.getMessage());
        }
    }
    
    /**
     * Inicializar usuario admin por defecto si no existe ningún usuario
     */
    @org.springframework.context.annotation.Bean
    org.springframework.boot.CommandLineRunner initDefaultUser(com.farmacia.service.UsuarioService usuarioService) {
        return args -> {
            if (!usuarioService.existeAlgunUsuario()) {
                System.out.println("=".repeat(60));
                System.out.println("No se encontraron usuarios en el sistema.");
                System.out.println("Creando usuario administrador por defecto...");
                
                com.farmacia.model.Usuario admin = usuarioService.crearUsuario(
                    "admin",
                    "admin123",
                    "Administrador del Sistema",
                    com.farmacia.model.Usuario.Rol.ADMIN
                );
                
                System.out.println("✓ Usuario administrador creado exitosamente!");
                System.out.println("  Usuario: admin");
                System.out.println("  Contraseña: admin123");
                System.out.println("  IMPORTANTE: Cambia la contraseña en el primer inicio de sesión.");
                System.out.println("=".repeat(60));
            } else {
                System.out.println("✓ Sistema de usuarios ya inicializado.");
            }
        };
    }
}

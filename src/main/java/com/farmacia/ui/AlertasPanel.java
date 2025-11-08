package com.farmacia.ui;

import com.farmacia.model.Alerta;
import com.farmacia.service.AlertaService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Panel de alertas del sistema
 */
@Component
public class AlertasPanel extends BorderPane {

    @Autowired
    private AlertaService alertaService;

    private ListView<Alerta> listaAlertas;
    private Label lblTotalAlertas;
    private Label lblCriticas;
    private Label lblStockBajo;
    private Label lblCaducados;
    private Label lblProximosCaducar;
    private Label lblPedidosPendientes;
    
    private ComboBox<String> filtroTipo;
    private Timer actualizacionTimer;

    public void initialize() {
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #f5f5f5;");

        // Título y resumen
        VBox topContainer = crearResumen();
        setTop(topContainer);

        // Lista de alertas
        VBox centerContainer = crearListaAlertas();
        setCenter(centerContainer);

        // Cargar alertas iniciales
        cargarAlertas();

        // Actualización automática cada 2 minutos
        iniciarActualizacionAutomatica();
    }

    private VBox crearResumen() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(0, 0, 15, 0));

        // Título
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("🔔 Sistema de Alertas");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 24));

        Button btnRefrescar = new Button("🔄 Actualizar");
        btnRefrescar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-cursor: hand;");
        btnRefrescar.setOnAction(e -> cargarAlertas());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(titulo, spacer, btnRefrescar);

        // Tarjetas de resumen
        HBox tarjetasBox = new HBox(10);
        tarjetasBox.setAlignment(Pos.CENTER_LEFT);

        lblTotalAlertas = new Label("0");
        lblCriticas = new Label("0");
        lblStockBajo = new Label("0");
        lblCaducados = new Label("0");
        lblProximosCaducar = new Label("0");
        lblPedidosPendientes = new Label("0");

        tarjetasBox.getChildren().addAll(
                crearTarjetaResumen("Total", lblTotalAlertas, "#6c757d"),
                crearTarjetaResumen("Críticas", lblCriticas, "#dc3545"),
                crearTarjetaResumen("Stock Bajo", lblStockBajo, "#ffc107"),
                crearTarjetaResumen("Caducados", lblCaducados, "#dc3545"),
                crearTarjetaResumen("Por Caducar", lblProximosCaducar, "#ffc107"),
                crearTarjetaResumen("Pedidos", lblPedidosPendientes, "#17a2b8")
        );

        container.getChildren().addAll(headerBox, tarjetasBox);
        return container;
    }

    private VBox crearTarjetaResumen(String titulo, Label valorLabel, String color) {
        VBox tarjeta = new VBox(5);
        tarjeta.setPadding(new Insets(15));
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        tarjeta.setPrefWidth(130);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        valorLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        tarjeta.getChildren().addAll(lblTitulo, valorLabel);
        return tarjeta;
    }

    private VBox crearListaAlertas() {
        VBox container = new VBox(10);
        VBox.setVgrow(container, Priority.ALWAYS);

        // Filtros
        HBox filtrosBox = new HBox(10);
        filtrosBox.setAlignment(Pos.CENTER_LEFT);
        filtrosBox.setPadding(new Insets(10));
        filtrosBox.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label lblFiltro = new Label("Filtrar por:");
        lblFiltro.setStyle("-fx-font-weight: bold;");

        filtroTipo = new ComboBox<>();
        filtroTipo.setItems(FXCollections.observableArrayList(
                "Todas",
                "Críticas",
                "Stock Bajo",
                "Productos Caducados",
                "Próximos a Caducar",
                "Pedidos Pendientes",
                "Pedidos Retrasados"
        ));
        filtroTipo.setValue("Todas");
        filtroTipo.setOnAction(e -> aplicarFiltro());
        filtroTipo.setPrefWidth(200);

        filtrosBox.getChildren().addAll(lblFiltro, filtroTipo);

        // Lista de alertas
        listaAlertas = new ListView<>();
        listaAlertas.setCellFactory(param -> new AlertaCell());
        VBox.setVgrow(listaAlertas, Priority.ALWAYS);

        container.getChildren().addAll(filtrosBox, listaAlertas);
        return container;
    }

    private void cargarAlertas() {
        List<Alerta> alertas = alertaService.obtenerTodasLasAlertas();
        listaAlertas.setItems(FXCollections.observableArrayList(alertas));

        // Actualizar estadísticas
        AlertaService.EstadisticasAlertas stats = alertaService.obtenerEstadisticas();
        lblTotalAlertas.setText(String.valueOf(stats.total));
        lblCriticas.setText(String.valueOf(stats.criticas));
        lblStockBajo.setText(String.valueOf(stats.stockBajo));
        lblCaducados.setText(String.valueOf(stats.caducados));
        lblProximosCaducar.setText(String.valueOf(stats.proximosCaducar));
        lblPedidosPendientes.setText(String.valueOf(stats.pedidosPendientes + stats.pedidosRetrasados));
    }

    private void aplicarFiltro() {
        String filtro = filtroTipo.getValue();
        List<Alerta> alertas = alertaService.obtenerTodasLasAlertas();

        if (!"Todas".equals(filtro)) {
            alertas = alertas.stream()
                    .filter(a -> {
                        return switch (filtro) {
                            case "Críticas" -> a.isCritica();
                            case "Stock Bajo" -> a.getTipo() == Alerta.TipoAlerta.STOCK_BAJO;
                            case "Productos Caducados" -> a.getTipo() == Alerta.TipoAlerta.PRODUCTO_CADUCADO;
                            case "Próximos a Caducar" -> a.getTipo() == Alerta.TipoAlerta.PROXIMO_CADUCAR;
                            case "Pedidos Pendientes" -> a.getTipo() == Alerta.TipoAlerta.PEDIDO_PENDIENTE;
                            case "Pedidos Retrasados" -> a.getTipo() == Alerta.TipoAlerta.PEDIDO_RETRASADO;
                            default -> true;
                        };
                    })
                    .toList();
        }

        listaAlertas.setItems(FXCollections.observableArrayList(alertas));
    }

    private void iniciarActualizacionAutomatica() {
        actualizacionTimer = new Timer(true);
        actualizacionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> cargarAlertas());
            }
        }, 120000, 120000); // Cada 2 minutos
    }

    /**
     * Obtiene el número total de alertas críticas
     */
    public int getNumeroAlertasCriticas() {
        return alertaService.obtenerAlertasCriticas().size();
    }

    /**
     * Cell personalizada para mostrar alertas
     */
    private static class AlertaCell extends ListCell<Alerta> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        @Override
        protected void updateItem(Alerta alerta, boolean empty) {
            super.updateItem(alerta, empty);

            if (empty || alerta == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                VBox content = new VBox(5);
                content.setPadding(new Insets(10));
                content.setStyle("-fx-background-color: white; -fx-background-radius: 5; " +
                        "-fx-border-color: " + alerta.getNivelColor() + "; " +
                        "-fx-border-width: 0 0 0 4; -fx-border-radius: 5;");

                // Cabecera
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label icono = new Label(alerta.getIcono());
                icono.setStyle("-fx-font-size: 20px;");

                Label tipo = new Label(alerta.getTipo().getDescripcion());
                tipo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                if (alerta.isCritica()) {
                    Label badge = new Label("CRÍTICA");
                    badge.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                            "-fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; " +
                            "-fx-font-weight: bold;");
                    header.getChildren().add(badge);
                }

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label fecha = new Label(alerta.getFecha().format(formatter));
                fecha.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

                header.getChildren().addAll(icono, tipo, spacer, fecha);

                // Mensaje
                Label mensaje = new Label(alerta.getMensaje());
                mensaje.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                mensaje.setWrapText(true);

                // Detalle
                Label detalle = new Label(alerta.getDetalle());
                detalle.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;");
                detalle.setWrapText(true);

                content.getChildren().addAll(header, mensaje, detalle);
                setGraphic(content);
            }
        }
    }
}

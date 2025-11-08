package com.farmacia.ui;

import com.farmacia.model.Venta;
import com.farmacia.model.Producto;
import com.farmacia.service.VentaService;
import com.farmacia.service.ProductoService;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.SwingUtilities;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Panel con gráficos interactivos usando JFreeChart
 */
public class GraficosPanel extends VBox {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private DatePicker dpFechaInicio;
    private DatePicker dpFechaFin;
    private VBox contenedorGraficos;

    public GraficosPanel(VentaService ventaService, ProductoService productoService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        
        inicializar();
    }

    private void inicializar() {
        setPadding(new Insets(20));
        setSpacing(15);

        // Título
        Label titulo = new Label("📈 Gráficos y Estadísticas");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Filtros
        HBox filtros = crearFiltros();

        // Contenedor de gráficos
        contenedorGraficos = new VBox(20);
        ScrollPane scroll = new ScrollPane(contenedorGraficos);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(titulo, filtros, scroll);

        // Cargar gráficos iniciales
        actualizarGraficos();
    }

    private HBox crearFiltros() {
        HBox filtros = new HBox(15);
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.setPadding(new Insets(10));
        filtros.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        Label lblFiltro = new Label("Rango de fechas:");
        lblFiltro.setStyle("-fx-font-weight: bold;");

        dpFechaInicio = new DatePicker(LocalDate.now().minusDays(30));
        dpFechaFin = new DatePicker(LocalDate.now());

        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnActualizar.setOnAction(e -> actualizarGraficos());

        Button btn7Dias = new Button("7 días");
        btn7Dias.setOnAction(e -> {
            dpFechaInicio.setValue(LocalDate.now().minusDays(7));
            dpFechaFin.setValue(LocalDate.now());
            actualizarGraficos();
        });

        Button btn30Dias = new Button("30 días");
        btn30Dias.setOnAction(e -> {
            dpFechaInicio.setValue(LocalDate.now().minusDays(30));
            dpFechaFin.setValue(LocalDate.now());
            actualizarGraficos();
        });

        filtros.getChildren().addAll(lblFiltro, dpFechaInicio, new Label("a"), dpFechaFin, 
                btnActualizar, btn7Dias, btn30Dias);
        return filtros;
    }

    private void actualizarGraficos() {
        contenedorGraficos.getChildren().clear();

        LocalDateTime fechaInicio = dpFechaInicio.getValue().atStartOfDay();
        LocalDateTime fechaFin = dpFechaFin.getValue().atTime(23, 59, 59);

        // Gráfico 1: Ventas por día
        VBox graficoVentasDiarias = crearGraficoVentasDiarias(fechaInicio, fechaFin);
        
        // Gráfico 2: Productos más vendidos
        VBox graficoProductos = crearGraficoProductosMasVendidos();
        
        // Gráfico 3: Métodos de pago
        VBox graficoMetodos = crearGraficoMetodosPago();

        contenedorGraficos.getChildren().addAll(graficoVentasDiarias, graficoProductos, graficoMetodos);
    }

    private VBox crearGraficoVentasDiarias(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);");

        Label titulo = new Label("📊 Evolución de Ventas Diarias");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        SwingNode swingNode = new SwingNode();

        SwingUtilities.invokeLater(() -> {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            List<Venta> ventas = ventaService.obtenerVentasPorRango(fechaInicio, fechaFin);
            
            // Agrupar ventas por fecha
            Map<LocalDate, Double> ventasPorDia = ventas.stream()
                    .collect(Collectors.groupingBy(
                            v -> v.getFecha().toLocalDate(),
                            Collectors.summingDouble(v -> v.getTotal().doubleValue())
                    ));

            // Ordenar y agregar al dataset
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            ventasPorDia.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        dataset.addValue(entry.getValue(), "Ventas (€)", entry.getKey().format(formatter));
                    });

            JFreeChart chart = ChartFactory.createLineChart(
                    null,
                    "Fecha",
                    "Ventas (€)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    false
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 350));
            swingNode.setContent(chartPanel);
        });

        container.getChildren().addAll(titulo, swingNode);
        return container;
    }

    private VBox crearGraficoProductosMasVendidos() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);");

        Label titulo = new Label("🏆 Top 10 Productos Más Vendidos");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        SwingNode swingNode = new SwingNode();

        SwingUtilities.invokeLater(() -> {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            List<Venta> ventas = ventaService.obtenerVentasActivas();
            Map<String, Long> productosVendidos = new HashMap<>();
            
            ventas.forEach(venta -> {
                venta.getDetalles().forEach(detalle -> {
                    String nombreProducto = detalle.getProducto().getNombre();
                    Long cantidad = (long) detalle.getCantidad();
                    productosVendidos.merge(nombreProducto, cantidad, Long::sum);
                });
            });

            // Top 10
            productosVendidos.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        String nombreCorto = entry.getKey().length() > 20 
                                ? entry.getKey().substring(0, 17) + "..." 
                                : entry.getKey();
                        dataset.addValue(entry.getValue(), "Unidades", nombreCorto);
                    });

            JFreeChart chart = ChartFactory.createBarChart(
                    null,
                    "Producto",
                    "Unidades Vendidas",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    false
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 350));
            swingNode.setContent(chartPanel);
        });

        container.getChildren().addAll(titulo, swingNode);
        return container;
    }

    private VBox crearGraficoMetodosPago() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);");

        Label titulo = new Label("💳 Distribución por Método de Pago");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        SwingNode swingNode = new SwingNode();

        SwingUtilities.invokeLater(() -> {
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

            List<Venta> ventas = ventaService.obtenerVentasActivas();
            Map<String, Double> porMetodo = ventas.stream()
                    .collect(Collectors.groupingBy(
                            Venta::getMetodoPago,
                            Collectors.summingDouble(v -> v.getTotal().doubleValue())
                    ));

            porMetodo.forEach((metodo, total) -> {
                String label = String.format("%s (€%.2f)", metodo, total);
                dataset.setValue(label, total);
            });

            JFreeChart chart = ChartFactory.createPieChart(
                    null,
                    dataset,
                    true,
                    true,
                    false
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 350));
            swingNode.setContent(chartPanel);
        });

        container.getChildren().addAll(titulo, swingNode);
        return container;
    }
}

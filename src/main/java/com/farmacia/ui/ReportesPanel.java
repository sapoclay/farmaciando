package com.farmacia.ui;

import com.farmacia.model.Cliente;
import com.farmacia.model.Producto;
import com.farmacia.model.Venta;
import com.farmacia.service.ClienteService;
import com.farmacia.service.ProductoService;
import com.farmacia.service.VentaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Panel de reportes y estadísticas
 */
public class ReportesPanel extends VBox {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final ClienteService clienteService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Componentes para filtros de fecha
    private DatePicker dpFechaInicio;
    private DatePicker dpFechaFin;

    public ReportesPanel(VentaService ventaService, ProductoService productoService, ClienteService clienteService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.clienteService = clienteService;

        initUI();
    }

    private void initUI() {
        setPadding(new Insets(20));
        setSpacing(15);

        // Título
        Label lblTitulo = new Label("📊 Reportes y Estadísticas");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Crear TabPane con cuatro pestañas
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Pestaña 1: Reportes de Ventas
        Tab tabVentas = new Tab("💰 Reportes de Ventas");
        tabVentas.setContent(crearPanelReporteVentas());

        // Pestaña 2: Reportes de Inventario
        Tab tabInventario = new Tab("📦 Reportes de Inventario");
        tabInventario.setContent(crearPanelReporteInventario());

        // Pestaña 3: Reportes de Clientes
        Tab tabClientes = new Tab("👥 Reportes de Clientes");
        tabClientes.setContent(crearPanelReporteClientes());

        // Pestaña 4: Gráficos
        Tab tabGraficos = new Tab("📈 Gráficos");
        GraficosPanel graficosPanel = new GraficosPanel(ventaService, productoService);
        tabGraficos.setContent(graficosPanel);

        // Pestaña 5: Exportar Reportes
        Tab tabExportar = new Tab("📤 Exportar Reportes");
        tabExportar.setContent(crearPanelExportacion());

        tabPane.getTabs().addAll(tabVentas, tabInventario, tabClientes, tabGraficos, tabExportar);

        getChildren().addAll(lblTitulo, tabPane);
    }

    /**
     * Panel de reportes de ventas
     */
    private VBox crearPanelReporteVentas() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        // Título
        Label lblTitulo = new Label("Estadísticas de Ventas");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Panel de estadísticas del día
        VBox statsDelDia = crearEstadisticasDelDia();

        // Panel de filtros de fecha
        HBox filtroFechas = crearFiltroFechas(() -> actualizarReporteVentas(panel));

        // Contenedor para estadísticas personalizadas
        VBox statsPersonalizadas = new VBox(15);
        statsPersonalizadas.setId("statsPersonalizadas");

        // Productos más vendidos
        VBox productosMasVendidos = crearSeccionProductosMasVendidos();

        // Métodos de pago
        VBox metodosPago = crearSeccionMetodosPago();

        ScrollPane scroll = new ScrollPane();
        VBox contenido = new VBox(20, lblTitulo, statsDelDia, filtroFechas, statsPersonalizadas, 
                                    productosMasVendidos, metodosPago);
        contenido.setPadding(new Insets(10));
        scroll.setContent(contenido);
        scroll.setFitToWidth(true);

        panel.getChildren().add(scroll);
        return panel;
    }

    /**
     * Estadísticas del día actual
     */
    private VBox crearEstadisticasDelDia() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 10; -fx-border-color: #2196F3; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("📅 Estadísticas del Día: " + LocalDate.now().format(formatter));
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Obtener estadísticas
        VentaService.EstadisticasVentas stats = ventaService.obtenerEstadisticasDelDia();

        // Grid de estadísticas
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        // Total ventas
        VBox boxTotal = crearCajaEstadistica("💵 Total Ventas", 
                String.format("€%.2f", stats.getTotalVentas()), "#4CAF50");
        grid.add(boxTotal, 0, 0);

        // Número de ventas
        VBox boxNumero = crearCajaEstadistica("🛒 Número de Ventas", 
                stats.getNumeroVentas().toString(), "#2196F3");
        grid.add(boxNumero, 1, 0);

        // Promedio por venta
        VBox boxPromedio = crearCajaEstadistica("📊 Promedio por Venta", 
                String.format("€%.2f", stats.getPromedioVenta()), "#FF9800");
        grid.add(boxPromedio, 2, 0);

        // Productos vendidos
        VBox boxProductos = crearCajaEstadistica("📦 Productos Vendidos", 
                stats.getTotalProductosVendidos().toString(), "#9C27B0");
        grid.add(boxProductos, 3, 0);

        panel.getChildren().addAll(lblTitulo, grid);
        return panel;
    }

    /**
     * Crear una caja de estadística
     */
    private VBox crearCajaEstadistica(String titulo, String valor, String color) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + color + "; -fx-border-radius: 8; -fx-border-width: 2;");
        box.setPrefWidth(180);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblValor.setStyle("-fx-text-fill: " + color + ";");

        box.getChildren().addAll(lblTitulo, lblValor);
        return box;
    }

    /**
     * Crear filtro de fechas
     */
    private HBox crearFiltroFechas(Runnable onUpdate) {
        HBox filtro = new HBox(15);
        filtro.setAlignment(Pos.CENTER_LEFT);
        filtro.setPadding(new Insets(10));
        filtro.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        Label lblFiltro = new Label("📅 Filtrar por rango de fechas:");
        lblFiltro.setFont(Font.font("System", FontWeight.BOLD, 14));

        dpFechaInicio = new DatePicker(LocalDate.now().minusDays(7));
        dpFechaInicio.setPrefWidth(150);

        Label lblA = new Label(" a ");

        dpFechaFin = new DatePicker(LocalDate.now());
        dpFechaFin.setPrefWidth(150);

        Button btnAplicar = new Button("🔍 Aplicar Filtro");
        btnAplicar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAplicar.setOnAction(e -> onUpdate.run());

        Button btnHoy = new Button("Hoy");
        btnHoy.setOnAction(e -> {
            dpFechaInicio.setValue(LocalDate.now());
            dpFechaFin.setValue(LocalDate.now());
            onUpdate.run();
        });

        Button btnSemana = new Button("Última Semana");
        btnSemana.setOnAction(e -> {
            dpFechaInicio.setValue(LocalDate.now().minusDays(7));
            dpFechaFin.setValue(LocalDate.now());
            onUpdate.run();
        });

        Button btnMes = new Button("Último Mes");
        btnMes.setOnAction(e -> {
            dpFechaInicio.setValue(LocalDate.now().minusMonths(1));
            dpFechaFin.setValue(LocalDate.now());
            onUpdate.run();
        });

        filtro.getChildren().addAll(lblFiltro, dpFechaInicio, lblA, dpFechaFin, btnAplicar, btnHoy, btnSemana, btnMes);
        return filtro;
    }

    /**
     * Actualizar reporte de ventas con fechas personalizadas
     */
    private void actualizarReporteVentas(VBox panel) {
        VBox statsBox = (VBox) panel.lookup("#statsPersonalizadas");
        if (statsBox == null) return;

        statsBox.getChildren().clear();

        LocalDateTime fechaInicio = dpFechaInicio.getValue().atStartOfDay();
        LocalDateTime fechaFin = dpFechaFin.getValue().atTime(23, 59, 59);

        List<Venta> ventas = ventaService.obtenerVentasPorRango(fechaInicio, fechaFin);

        // Calcular estadísticas
        double total = ventas.stream().mapToDouble(v -> v.getTotal().doubleValue()).sum();
        long numVentas = ventas.size();
        double promedio = numVentas > 0 ? total / numVentas : 0;

        // Mostrar estadísticas
        Label lblResultados = new Label(String.format(
                "📊 Resultados del %s al %s",
                dpFechaInicio.getValue().format(formatter),
                dpFechaFin.getValue().format(formatter)
        ));
        lblResultados.setFont(Font.font("System", FontWeight.BOLD, 16));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        VBox boxTotal = crearCajaEstadistica("💵 Total Periodo", String.format("€%.2f", total), "#4CAF50");
        VBox boxNum = crearCajaEstadistica("🛒 Ventas", String.valueOf(numVentas), "#2196F3");
        VBox boxProm = crearCajaEstadistica("📊 Promedio", String.format("€%.2f", promedio), "#FF9800");

        grid.add(boxTotal, 0, 0);
        grid.add(boxNum, 1, 0);
        grid.add(boxProm, 2, 0);

        statsBox.getChildren().addAll(lblResultados, grid);
    }

    /**
     * Sección de productos más vendidos
     */
    private VBox crearSeccionProductosMasVendidos() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 10; -fx-border-color: #FF9800; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("🏆 Top 10 Productos Más Vendidos");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        TableView<Map.Entry<String, Long>> tabla = new TableView<>();
        
        TableColumn<Map.Entry<String, Long>, String> colPosicion = new TableColumn<>("#");
        colPosicion.setCellValueFactory(data -> {
            int index = tabla.getItems().indexOf(data.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });
        colPosicion.setPrefWidth(50);

        TableColumn<Map.Entry<String, Long>, String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        colProducto.setPrefWidth(400);

        TableColumn<Map.Entry<String, Long>, String> colCantidad = new TableColumn<>("Cantidad Vendida");
        colCantidad.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue().toString()));
        colCantidad.setPrefWidth(150);

        tabla.getColumns().addAll(colPosicion, colProducto, colCantidad);
        tabla.setPrefHeight(300);

        // Cargar datos
        List<Venta> ventas = ventaService.obtenerVentasActivas();
        Map<String, Long> productosVendidos = new HashMap<>();
        
        ventas.forEach(venta -> {
            venta.getDetalles().forEach(detalle -> {
                String nombreProducto = detalle.getProducto().getNombre();
                productosVendidos.merge(nombreProducto, detalle.getCantidad().longValue(), Long::sum);
            });
        });

        List<Map.Entry<String, Long>> top10 = productosVendidos.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        tabla.setItems(FXCollections.observableArrayList(top10));

        panel.getChildren().addAll(lblTitulo, tabla);
        return panel;
    }

    /**
     * Sección de métodos de pago
     */
    private VBox crearSeccionMetodosPago() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #f3e5f5; -fx-background-radius: 10; -fx-border-color: #9C27B0; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("💳 Distribución por Método de Pago");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        // Obtener estadísticas por método de pago
        List<Venta> ventas = ventaService.obtenerVentasActivas();
        Map<String, Double> porMetodo = ventas.stream()
                .collect(Collectors.groupingBy(
                        Venta::getMetodoPago,
                        Collectors.summingDouble(v -> v.getTotal().doubleValue())
                ));

        int col = 0;
        for (Map.Entry<String, Double> entry : porMetodo.entrySet()) {
            String icono = getIconoMetodoPago(entry.getKey());
            VBox box = crearCajaEstadistica(
                    icono + " " + entry.getKey(),
                    String.format("€%.2f", entry.getValue()),
                    "#9C27B0"
            );
            grid.add(box, col++, 0);
        }

        panel.getChildren().addAll(lblTitulo, grid);
        return panel;
    }

    /**
     * Panel de reportes de inventario
     */
    private VBox crearPanelReporteInventario() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane();
        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(10));

        // Título
        Label lblTitulo = new Label("Estadísticas de Inventario");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Estadísticas generales
        VBox statsGenerales = crearEstadisticasInventarioGeneral();

        // Productos con stock bajo
        VBox stockBajo = crearSeccionStockBajo();

        // Productos próximos a vencer
        VBox proximosVencer = crearSeccionProximosVencer();

        // Distribución por categorías
        VBox categorias = crearSeccionCategoriasInventario();

        contenido.getChildren().addAll(lblTitulo, statsGenerales, stockBajo, proximosVencer, categorias);
        scroll.setContent(contenido);
        scroll.setFitToWidth(true);

        panel.getChildren().add(scroll);
        return panel;
    }

    /**
     * Estadísticas generales de inventario
     */
    private VBox crearEstadisticasInventarioGeneral() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 10; -fx-border-color: #4CAF50; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("📊 Estadísticas Generales");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        List<Producto> productos = productoService.obtenerTodos();
        long totalProductos = productos.size();
        long stockTotal = productos.stream().mapToLong(Producto::getStock).sum();
        double valorTotal = productos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue() * p.getStock())
                .sum();
        long stockBajo = productos.stream().filter(Producto::isStockBajo).count();
        long productosVencidos = productos.stream().filter(Producto::isVencido).count();

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(crearCajaEstadistica("📦 Total Productos", String.valueOf(totalProductos), "#4CAF50"), 0, 0);
        grid.add(crearCajaEstadistica("📊 Stock Total", String.valueOf(stockTotal), "#2196F3"), 1, 0);
        grid.add(crearCajaEstadistica("💰 Valor Total", String.format("€%.2f", valorTotal), "#FF9800"), 2, 0);
        grid.add(crearCajaEstadistica("⚠️ Stock Bajo", String.valueOf(stockBajo), "#f44336"), 0, 1);
        grid.add(crearCajaEstadistica("❌ Vencidos", String.valueOf(productosVencidos), "#9C27B0"), 1, 1);

        panel.getChildren().addAll(lblTitulo, grid);
        return panel;
    }

    /**
     * Sección de productos con stock bajo
     */
    private VBox crearSeccionStockBajo() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #ffebee; -fx-background-radius: 10; -fx-border-color: #f44336; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("⚠️ Productos con Stock Bajo");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        TableView<Producto> tabla = new TableView<>();

        TableColumn<Producto, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colNombre.setPrefWidth(300);

        TableColumn<Producto, String> colStock = new TableColumn<>("Stock Actual");
        colStock.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStock())));
        colStock.setPrefWidth(100);

        TableColumn<Producto, String> colStockMin = new TableColumn<>("Stock Mínimo");
        colStockMin.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStockMinimo())));
        colStockMin.setPrefWidth(100);

        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoria()));
        colCategoria.setPrefWidth(150);

        tabla.getColumns().addAll(colNombre, colStock, colStockMin, colCategoria);
        tabla.setPrefHeight(250);

        List<Producto> stockBajo = productoService.obtenerProductosStockBajo();
        tabla.setItems(FXCollections.observableArrayList(stockBajo));

        panel.getChildren().addAll(lblTitulo, tabla);
        return panel;
    }

    /**
     * Sección de productos próximos a vencer
     */
    private VBox crearSeccionProximosVencer() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 10; -fx-border-color: #FF9800; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("⏰ Productos Próximos a Vencer (30 días)");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        TableView<Producto> tabla = new TableView<>();

        TableColumn<Producto, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colNombre.setPrefWidth(300);

        TableColumn<Producto, String> colFecha = new TableColumn<>("Fecha Vencimiento");
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaVencimiento() != null ? 
                data.getValue().getFechaVencimiento().format(formatter) : "-"));
        colFecha.setPrefWidth(150);

        TableColumn<Producto, String> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStock())));
        colStock.setPrefWidth(100);

        TableColumn<Producto, String> colLab = new TableColumn<>("Laboratorio");
        colLab.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLaboratorio()));
        colLab.setPrefWidth(150);

        tabla.getColumns().addAll(colNombre, colFecha, colStock, colLab);
        tabla.setPrefHeight(250);

        List<Producto> proximosVencer = productoService.obtenerProductosProximosVencer(30);
        tabla.setItems(FXCollections.observableArrayList(proximosVencer));

        panel.getChildren().addAll(lblTitulo, tabla);
        return panel;
    }

    /**
     * Sección de distribución por categorías
     */
    private VBox crearSeccionCategoriasInventario() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 10; -fx-border-color: #2196F3; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("📂 Distribución por Categorías");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        TableView<Map.Entry<String, Long>> tabla = new TableView<>();

        TableColumn<Map.Entry<String, Long>, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        colCategoria.setPrefWidth(300);

        TableColumn<Map.Entry<String, Long>, String> colCantidad = new TableColumn<>("Cantidad de Productos");
        colCantidad.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue().toString()));
        colCantidad.setPrefWidth(200);

        tabla.getColumns().addAll(colCategoria, colCantidad);
        tabla.setPrefHeight(250);

        Map<String, Long> porCategoria = productoService.obtenerTodos().stream()
                .collect(Collectors.groupingBy(Producto::getCategoria, Collectors.counting()));

        tabla.setItems(FXCollections.observableArrayList(porCategoria.entrySet()));

        panel.getChildren().addAll(lblTitulo, tabla);
        return panel;
    }

    /**
     * Panel de reportes de clientes
     */
    private VBox crearPanelReporteClientes() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane();
        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(10));

        // Título
        Label lblTitulo = new Label("Estadísticas de Clientes");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Estadísticas generales
        VBox statsGenerales = crearEstadisticasClientesGeneral();

        // Distribución por tipo de documento
        VBox tiposDoc = crearSeccionTiposDocumento();

        // Últimos clientes registrados
        VBox ultimosClientes = crearSeccionUltimosClientes();

        // Distribución por ciudad
        VBox ciudades = crearSeccionCiudades();

        contenido.getChildren().addAll(lblTitulo, statsGenerales, tiposDoc, ultimosClientes, ciudades);
        scroll.setContent(contenido);
        scroll.setFitToWidth(true);

        panel.getChildren().add(scroll);
        return panel;
    }

    /**
     * Estadísticas generales de clientes
     */
    private VBox crearEstadisticasClientesGeneral() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #f3e5f5; -fx-background-radius: 10; -fx-border-color: #9C27B0; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("📊 Estadísticas Generales");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        ClienteService.EstadisticasClientes stats = clienteService.obtenerEstadisticas();

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        grid.add(crearCajaEstadistica("👥 Clientes Activos", String.valueOf(stats.getTotalClientes()), "#4CAF50"), 0, 0);
        grid.add(crearCajaEstadistica("❌ Clientes Inactivos", String.valueOf(stats.getTotalInactivos()), "#f44336"), 1, 0);
        grid.add(crearCajaEstadistica("📈 Total General", String.valueOf(stats.getTotalClientes() + stats.getTotalInactivos()), "#2196F3"), 2, 0);

        panel.getChildren().addAll(lblTitulo, grid);
        return panel;
    }

    /**
     * Sección de tipos de documento
     */
    private VBox crearSeccionTiposDocumento() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 10; -fx-border-color: #4CAF50; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("📄 Distribución por Tipo de Documento");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        Map<String, Long> porTipo = clienteService.obtenerClientesActivos().stream()
                .collect(Collectors.groupingBy(Cliente::getTipoDocumento, Collectors.counting()));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int col = 0;
        for (Map.Entry<String, Long> entry : porTipo.entrySet()) {
            VBox box = crearCajaEstadistica(
                    entry.getKey(),
                    entry.getValue().toString(),
                    "#4CAF50"
            );
            grid.add(box, col++, 0);
        }

        panel.getChildren().addAll(lblTitulo, grid);
        return panel;
    }

    /**
     * Sección de últimos clientes registrados
     */
    private VBox crearSeccionUltimosClientes() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 10; -fx-border-color: #2196F3; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("🆕 Últimos 10 Clientes Registrados");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        TableView<Cliente> tabla = new TableView<>();

        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre Completo");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colNombre.setPrefWidth(250);

        TableColumn<Cliente, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumento()));
        colDocumento.setPrefWidth(120);

        TableColumn<Cliente, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTelefono() != null ? data.getValue().getTelefono() : "-"));
        colTelefono.setPrefWidth(120);

        TableColumn<Cliente, String> colFecha = new TableColumn<>("Fecha Registro");
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaRegistro().format(formatterTime)));
        colFecha.setPrefWidth(150);

        tabla.getColumns().addAll(colNombre, colDocumento, colTelefono, colFecha);
        tabla.setPrefHeight(300);

        List<Cliente> ultimos = clienteService.obtenerUltimosClientes();
        tabla.setItems(FXCollections.observableArrayList(ultimos));

        panel.getChildren().addAll(lblTitulo, tabla);
        return panel;
    }

    /**
     * Sección de distribución por ciudad
     */
    private VBox crearSeccionCiudades() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 10; -fx-border-color: #FF9800; -fx-border-radius: 10; -fx-border-width: 2;");

        Label lblTitulo = new Label("🏙️ Distribución por Ciudad");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));

        TableView<Map.Entry<String, Long>> tabla = new TableView<>();

        TableColumn<Map.Entry<String, Long>, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getKey() != null && !data.getValue().getKey().isEmpty() ? 
                data.getValue().getKey() : "Sin especificar"));
        colCiudad.setPrefWidth(300);

        TableColumn<Map.Entry<String, Long>, String> colCantidad = new TableColumn<>("Cantidad de Clientes");
        colCantidad.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue().toString()));
        colCantidad.setPrefWidth(200);

        tabla.getColumns().addAll(colCiudad, colCantidad);
        tabla.setPrefHeight(250);

        Map<String, Long> porCiudad = clienteService.obtenerClientesActivos().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCiudad() != null && !c.getCiudad().isEmpty() ? c.getCiudad() : "Sin especificar",
                        Collectors.counting()
                ));

        tabla.setItems(FXCollections.observableArrayList(porCiudad.entrySet()));

        panel.getChildren().addAll(lblTitulo, tabla);
        return panel;
    }

    /**
     * Panel de exportación de reportes
     */
    private VBox crearPanelExportacion() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);

        Label lblTitulo = new Label("Exportar Reportes a CSV");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Opciones de exportación
        VBox opciones = new VBox(15);
        opciones.setAlignment(Pos.CENTER);
        opciones.setPadding(new Insets(20));
        opciones.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        opciones.setMaxWidth(600);

        Button btnExportarVentas = crearBotonExportacion("💰 Exportar Reporte de Ventas", "#4CAF50", 
                () -> exportarVentas());
        
        Button btnExportarInventario = crearBotonExportacion("📦 Exportar Reporte de Inventario", "#2196F3",
                () -> exportarInventario());
        
        Button btnExportarClientes = crearBotonExportacion("👥 Exportar Reporte de Clientes", "#9C27B0",
                () -> exportarClientes());
        
        Button btnExportarCompleto = crearBotonExportacion("📊 Exportar Reporte Completo", "#FF9800",
                () -> exportarReporteCompleto());

        opciones.getChildren().addAll(btnExportarVentas, btnExportarInventario, btnExportarClientes, btnExportarCompleto);

        panel.getChildren().addAll(lblTitulo, opciones);
        return panel;
    }

    /**
     * Crear botón de exportación
     */
    private Button crearBotonExportacion(String texto, String color, Runnable accion) {
        Button btn = new Button(texto);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 15;", color));
        btn.setPrefWidth(500);
        btn.setOnAction(e -> accion.run());
        return btn;
    }

    /**
     * Exportar reporte de ventas
     */
    private void exportarVentas() {
        try {
            File file = seleccionarArchivoDestino("reporte_ventas.csv");
            if (file == null) return;

            try (FileWriter writer = new FileWriter(file)) {
                // Encabezados
                writer.write("ID,Fecha,Cliente,Subtotal,Descuento,Total,Metodo Pago,Estado\n");

                // Datos
                List<Venta> ventas = ventaService.obtenerVentasActivas();
                for (Venta venta : ventas) {
                    writer.write(String.format("%d,%s,%s,%.2f,%.2f,%.2f,%s,%s\n",
                            venta.getId(),
                            venta.getFecha().format(formatterTime),
                            escaparCSV(venta.getCliente()),
                            venta.getSubtotal(),
                            venta.getDescuento(),
                            venta.getTotal(),
                            venta.getMetodoPago(),
                            venta.getActivo() ? "Activa" : "Anulada"
                    ));
                }
            }

            mostrarMensaje("Reporte exportado exitosamente a:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            mostrarMensaje("Error al exportar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Exportar reporte de inventario
     */
    private void exportarInventario() {
        try {
            File file = seleccionarArchivoDestino("reporte_inventario.csv");
            if (file == null) return;

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Codigo,Nombre,Categoria,Laboratorio,Stock,Stock Minimo,Precio,Fecha Vencimiento,Estado\n");

                List<Producto> productos = productoService.obtenerTodos();
                for (Producto p : productos) {
                    writer.write(String.format("%s,%s,%s,%s,%d,%d,%.2f,%s,%s\n",
                            escaparCSV(p.getCodigo()),
                            escaparCSV(p.getNombre()),
                            escaparCSV(p.getCategoria()),
                            escaparCSV(p.getLaboratorio()),
                            p.getStock(),
                            p.getStockMinimo(),
                            p.getPrecio(),
                            p.getFechaVencimiento() != null ? p.getFechaVencimiento().format(formatter) : "N/A",
                            p.isStockBajo() ? "Stock Bajo" : (p.isVencido() ? "Vencido" : "Normal")
                    ));
                }
            }

            mostrarMensaje("Reporte exportado exitosamente a:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            mostrarMensaje("Error al exportar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Exportar reporte de clientes
     */
    private void exportarClientes() {
        try {
            File file = seleccionarArchivoDestino("reporte_clientes.csv");
            if (file == null) return;

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("ID,Nombre,Apellido,Tipo Doc,Documento,Telefono,Email,Ciudad,CP,Fecha Registro\n");

                List<Cliente> clientes = clienteService.obtenerClientesActivos();
                for (Cliente c : clientes) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            c.getId(),
                            escaparCSV(c.getNombre()),
                            escaparCSV(c.getApellido()),
                            c.getTipoDocumento(),
                            c.getDocumento(),
                            c.getTelefono() != null ? escaparCSV(c.getTelefono()) : "",
                            c.getEmail() != null ? escaparCSV(c.getEmail()) : "",
                            c.getCiudad() != null ? escaparCSV(c.getCiudad()) : "",
                            c.getCodigoPostal() != null ? c.getCodigoPostal() : "",
                            c.getFechaRegistro().format(formatterTime)
                    ));
                }
            }

            mostrarMensaje("Reporte exportado exitosamente a:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            mostrarMensaje("Error al exportar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Exportar reporte completo
     */
    private void exportarReporteCompleto() {
        try {
            File file = seleccionarArchivoDestino("reporte_completo.csv");
            if (file == null) return;

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("=== REPORTE COMPLETO DE FARMACIA ===\n");
                writer.write("Fecha: " + LocalDateTime.now().format(formatterTime) + "\n\n");

                // Estadísticas de ventas
                writer.write("=== VENTAS ===\n");
                VentaService.EstadisticasVentas statsVentas = ventaService.obtenerEstadisticasDelDia();
                writer.write(String.format("Total del Dia: %.2f EUR\n", statsVentas.getTotalVentas()));
                writer.write(String.format("Numero de Ventas: %d\n", statsVentas.getNumeroVentas()));
                writer.write(String.format("Promedio por Venta: %.2f EUR\n\n", statsVentas.getPromedioVenta()));

                // Estadísticas de inventario
                writer.write("=== INVENTARIO ===\n");
                List<Producto> productos = productoService.obtenerTodos();
                writer.write(String.format("Total Productos: %d\n", productos.size()));
                writer.write(String.format("Stock Total: %d\n", productos.stream().mapToLong(Producto::getStock).sum()));
                writer.write(String.format("Productos Stock Bajo: %d\n", productos.stream().filter(Producto::isStockBajo).count()));
                writer.write(String.format("Productos Vencidos: %d\n\n", productos.stream().filter(Producto::isVencido).count()));

                // Estadísticas de clientes
                writer.write("=== CLIENTES ===\n");
                ClienteService.EstadisticasClientes statsClientes = clienteService.obtenerEstadisticas();
                writer.write(String.format("Clientes Activos: %d\n", statsClientes.getTotalClientes()));
                writer.write(String.format("Clientes Inactivos: %d\n", statsClientes.getTotalInactivos()));
            }

            mostrarMensaje("Reporte completo exportado exitosamente a:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            mostrarMensaje("Error al exportar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Seleccionar archivo destino
     */
    private File seleccionarArchivoDestino(String nombrePredeterminado) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte");
        fileChooser.setInitialFileName(nombrePredeterminado);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );
        return fileChooser.showSaveDialog(getScene().getWindow());
    }

    /**
     * Escapar texto para CSV
     */
    private String escaparCSV(String texto) {
        if (texto == null) return "";
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    /**
     * Obtener icono según método de pago
     */
    private String getIconoMetodoPago(String metodo) {
        return switch (metodo.toLowerCase()) {
            case "efectivo" -> "💵";
            case "tarjeta" -> "💳";
            case "transferencia" -> "🏦";
            default -> "💰";
        };
    }

    /**
     * Mostrar mensaje
     */
    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(tipo == Alert.AlertType.ERROR ? "Error" : "Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

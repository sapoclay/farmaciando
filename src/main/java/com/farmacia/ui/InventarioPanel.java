package com.farmacia.ui;

import com.farmacia.model.Producto;
import com.farmacia.service.ProductoService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InventarioPanel {

    private final ProductoService productoService;
    private TableView<Producto> tablaProductos;
    private ObservableList<Producto> productosData;
    private TextField txtBuscar;
    private BorderPane content;

    public InventarioPanel(ProductoService productoService) {
        this.productoService = productoService;
        inicializarContenido();
    }

    public BorderPane getContent() {
        return content;
    }

    private void inicializarContenido() {
        content = new BorderPane();
        content.setPadding(new Insets(15));

        // Panel superior: Título y búsqueda
        VBox topBox = createTopPanel();
        content.setTop(topBox);

        // Panel central: Tabla de productos
        VBox centerBox = createCenterPanel();
        content.setCenter(centerBox);

        // Panel inferior: Botones de acción
        HBox bottomBox = createBottomPanel();
        content.setBottom(bottomBox);

        // Cargar datos iniciales
        cargarProductos();
    }

    private VBox createTopPanel() {
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(0, 0, 15, 0));

        Label titulo = new Label("📦 Gestión de Inventario");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Panel de búsqueda y filtros
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label lblBuscar = new Label("Buscar:");
        txtBuscar = new TextField();
        txtBuscar.setPromptText("Nombre o código del producto...");
        txtBuscar.setPrefWidth(300);
        txtBuscar.textProperty().addListener((obs, old, newVal) -> filtrarProductos(newVal));

        Button btnStockBajo = new Button("⚠️ Stock Bajo");
        btnStockBajo.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        btnStockBajo.setOnAction(e -> mostrarProductosStockBajo());

        Button btnVencidos = new Button("🚫 Vencidos");
        btnVencidos.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        btnVencidos.setOnAction(e -> mostrarProductosVencidos());

        Button btnTodos = new Button("📋 Ver Todos");
        btnTodos.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnTodos.setOnAction(e -> cargarProductos());

        searchBox.getChildren().addAll(lblBuscar, txtBuscar, btnStockBajo, btnVencidos, btnTodos);

        topBox.getChildren().addAll(titulo, searchBox);
        return topBox;
    }

    private VBox createCenterPanel() {
        VBox centerBox = new VBox(10);

        // Crear tabla
        tablaProductos = new TableView<>();
        tablaProductos.setPrefHeight(450);

        // Columnas
        TableColumn<Producto, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Producto, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setPrefWidth(100);

        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCategoria.setPrefWidth(120);

        TableColumn<Producto, BigDecimal> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setPrefWidth(80);

        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setPrefWidth(70);

        TableColumn<Producto, Integer> colStockMin = new TableColumn<>("Stock Mín");
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colStockMin.setPrefWidth(90);

        TableColumn<Producto, LocalDate> colVencimiento = new TableColumn<>("Vencimiento");
        colVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colVencimiento.setPrefWidth(110);

        TableColumn<Producto, String> colLaboratorio = new TableColumn<>("Laboratorio");
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colLaboratorio.setPrefWidth(130);

        TableColumn<Producto, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            String estado = "";
            if (p.isVencido()) {
                estado = "🚫 VENCIDO";
            } else if (p.isProximoAVencer()) {
                estado = "⚠️ Próximo a vencer";
            } else if (p.isStockBajo()) {
                estado = "⚠️ Stock bajo";
            } else {
                estado = "✓ OK";
            }
            return new SimpleStringProperty(estado);
        });
        colEstado.setPrefWidth(150);

        // Aplicar colores de fondo según el estado
        tablaProductos.setRowFactory(tv -> new TableRow<Producto>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                if (producto == null || empty) {
                    setStyle("");
                } else {
                    if (producto.isVencido() || producto.isStockBajo()) {
                        setStyle("-fx-background-color: #ffcccc;");
                    } else if (producto.isProximoAVencer()) {
                        setStyle("-fx-background-color: #ffe6cc;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        tablaProductos.getColumns().addAll(
            colId, colCodigo, colNombre, colCategoria, colPrecio,
            colStock, colStockMin, colVencimiento, colLaboratorio, colEstado
        );

        productosData = FXCollections.observableArrayList();
        tablaProductos.setItems(productosData);

        centerBox.getChildren().add(tablaProductos);
        return centerBox;
    }

    private HBox createBottomPanel() {
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));

        Button btnNuevo = new Button("➕ Nuevo Producto");
        btnNuevo.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        btnNuevo.setOnAction(e -> abrirFormularioNuevo());

        Button btnEditar = new Button("✏️ Editar");
        btnEditar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
        btnEditar.setOnAction(e -> editarProducto());

        Button btnEliminar = new Button("🗑️ Eliminar");
        btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px;");
        btnEliminar.setOnAction(e -> eliminarProducto());

        Button btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-size: 14px;");
        btnRefrescar.setOnAction(e -> cargarProductos());

        bottomBox.getChildren().addAll(btnNuevo, btnEditar, btnEliminar, btnRefrescar);
        return bottomBox;
    }

    private void cargarProductos() {
        productosData.clear();
        List<Producto> productos = productoService.obtenerTodosActivos();
        productosData.addAll(productos);
        txtBuscar.clear();
    }

    private void filtrarProductos(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            cargarProductos();
            return;
        }

        productosData.clear();
        List<Producto> productos = productoService.buscarPorNombre(texto);
        productosData.addAll(productos);
    }

    private void mostrarProductosStockBajo() {
        productosData.clear();
        List<Producto> productos = productoService.obtenerProductosConStockBajo();
        productosData.addAll(productos);
        txtBuscar.clear();
    }

    private void mostrarProductosVencidos() {
        productosData.clear();
        List<Producto> productos = productoService.obtenerProductosVencidos();
        productosData.addAll(productos);
        txtBuscar.clear();
    }

    private void abrirFormularioNuevo() {
        ProductoFormWindow form = new ProductoFormWindow(productoService, null);
        form.showAndWait();
        cargarProductos();
    }

    private void editarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Advertencia", "Debes seleccionar un producto para editar", Alert.AlertType.WARNING);
            return;
        }

        ProductoFormWindow form = new ProductoFormWindow(productoService, seleccionado);
        form.showAndWait();
        cargarProductos();
    }

    private void eliminarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Advertencia", "Debes seleccionar un producto para eliminar", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Estás seguro de eliminar este producto?");
        confirmacion.setContentText(seleccionado.getNombre() + " (Código: " + seleccionado.getCodigo() + ")");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                productoService.eliminarProducto(seleccionado.getId());
                mostrarAlerta("Éxito", "Producto eliminado correctamente", Alert.AlertType.INFORMATION);
                cargarProductos();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
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
}

package com.farmacia.ui;

import com.farmacia.model.Producto;
import com.farmacia.service.ProductoService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InventarioWindow {

    private final ProductoService productoService;
    private final Stage primaryStage;
    private TableView<Producto> tablaProductos;
    private ObservableList<Producto> productosData;
    private TextField txtBuscar;

    public InventarioWindow(ProductoService productoService, Stage primaryStage) {
        this.productoService = productoService;
        this.primaryStage = primaryStage;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(primaryStage);
        stage.setTitle("FarmaCiando - Gestión de Inventario");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Panel superior: Título y búsqueda
        VBox topBox = createTopPanel();
        root.setTop(topBox);

        // Panel central: Tabla de productos
        VBox centerBox = createCenterPanel();
        root.setCenter(centerBox);

        // Panel inferior: Botones de acción
        HBox bottomBox = createBottomPanel(stage);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.show();

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
        productosData = FXCollections.observableArrayList();
        tablaProductos.setItems(productosData);

        // Columnas
        TableColumn<Producto, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Producto, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setPrefWidth(100);

        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(250);

        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCategoria.setPrefWidth(120);

        TableColumn<Producto, String> colLaboratorio = new TableColumn<>("Laboratorio");
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colLaboratorio.setPrefWidth(150);

        TableColumn<Producto, BigDecimal> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setPrefWidth(100);
        colPrecio.setCellFactory(col -> new TableCell<Producto, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("€" + item.toString());
                }
            }
        });

        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setPrefWidth(80);
        colStock.setCellFactory(col -> new TableCell<Producto, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    Producto producto = getTableView().getItems().get(getIndex());
                    if (producto.isStockBajo()) {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        TableColumn<Producto, Integer> colStockMin = new TableColumn<>("Stock Mín");
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colStockMin.setPrefWidth(90);

        TableColumn<Producto, LocalDate> colVencimiento = new TableColumn<>("Vencimiento");
        colVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colVencimiento.setPrefWidth(120);
        colVencimiento.setCellFactory(col -> new TableCell<Producto, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    Producto producto = getTableView().getItems().get(getIndex());
                    if (producto.isVencido()) {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                    } else if (producto.isProximoAVencer()) {
                        setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #e65100;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        TableColumn<Producto, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            String estado = "";
            if (p.isVencido()) {
                estado = "⚠️ VENCIDO";
            } else if (p.isProximoAVencer()) {
                estado = "⏰ Por vencer";
            } else if (p.isStockBajo()) {
                estado = "📉 Stock bajo";
            } else {
                estado = "✅ Normal";
            }
            return new SimpleStringProperty(estado);
        });
        colEstado.setPrefWidth(120);

        tablaProductos.getColumns().addAll(colId, colCodigo, colNombre, colCategoria, 
                                           colLaboratorio, colPrecio, colStock, 
                                           colStockMin, colVencimiento, colEstado);

        centerBox.getChildren().add(tablaProductos);
        VBox.setVgrow(tablaProductos, Priority.ALWAYS);

        return centerBox;
    }

    private HBox createBottomPanel(Stage stage) {
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));

        Button btnNuevo = new Button("➕ Nuevo Producto");
        btnNuevo.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 150px;");
        btnNuevo.setOnAction(e -> abrirFormularioProducto(null));

        Button btnEditar = new Button("✏️ Editar");
        btnEditar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 150px;");
        btnEditar.setOnAction(e -> {
            Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                abrirFormularioProducto(seleccionado);
            } else {
                mostrarAlerta("Selección", "Por favor selecciona un producto para editar.");
            }
        });

        Button btnEliminar = new Button("🗑️ Eliminar");
        btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 150px;");
        btnEliminar.setOnAction(e -> eliminarProducto());

        Button btnCerrar = new Button("❌ Cerrar");
        btnCerrar.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 150px;");
        btnCerrar.setOnAction(e -> stage.close());

        bottomBox.getChildren().addAll(btnNuevo, btnEditar, btnEliminar, btnCerrar);
        return bottomBox;
    }

    private void cargarProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodosActivos();
            productosData.clear();
            productosData.addAll(productos);
        } catch (Exception e) {
            mostrarError("Error al cargar productos", e.getMessage());
        }
    }

    private void filtrarProductos(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            cargarProductos();
        } else {
            try {
                List<Producto> productos = productoService.buscarPorNombre(texto);
                productosData.clear();
                productosData.addAll(productos);
            } catch (Exception e) {
                mostrarError("Error al buscar productos", e.getMessage());
            }
        }
    }

    private void mostrarProductosStockBajo() {
        try {
            List<Producto> productos = productoService.obtenerProductosConStockBajo();
            productosData.clear();
            productosData.addAll(productos);
            if (productos.isEmpty()) {
                mostrarInfo("Stock Bajo", "No hay productos con stock bajo.");
            }
        } catch (Exception e) {
            mostrarError("Error", e.getMessage());
        }
    }

    private void mostrarProductosVencidos() {
        try {
            List<Producto> productos = productoService.obtenerProductosVencidos();
            productosData.clear();
            productosData.addAll(productos);
            if (productos.isEmpty()) {
                mostrarInfo("Productos Vencidos", "No hay productos vencidos.");
            }
        } catch (Exception e) {
            mostrarError("Error", e.getMessage());
        }
    }

    private void abrirFormularioProducto(Producto producto) {
        ProductoFormWindow form = new ProductoFormWindow(productoService, producto);
        form.showAndWait();
        cargarProductos(); // Recargar tabla después de guardar
    }

    private void eliminarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección", "Por favor selecciona un producto para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar producto?");
        confirmacion.setContentText("¿Estás seguro de eliminar: " + seleccionado.getNombre() + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                productoService.eliminarProducto(seleccionado.getId());
                cargarProductos();
                mostrarInfo("Éxito", "Producto eliminado correctamente.");
            } catch (Exception e) {
                mostrarError("Error al eliminar", e.getMessage());
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

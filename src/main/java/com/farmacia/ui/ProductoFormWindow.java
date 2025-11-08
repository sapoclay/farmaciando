package com.farmacia.ui;

import com.farmacia.model.Producto;
import com.farmacia.service.ProductoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductoFormWindow extends Stage {

    private final ProductoService productoService;
    private final Producto producto;
    private boolean guardado = false;

    // Campos del formulario
    private TextField txtCodigo;
    private TextField txtNombre;
    private TextArea txtDescripcion;
    private TextField txtPrecio;
    private TextField txtStock;
    private TextField txtStockMinimo;
    private TextField txtLaboratorio;
    private ComboBox<String> cmbCategoria;
    private DatePicker dpVencimiento;
    private CheckBox chkRequiereReceta;

    public ProductoFormWindow(ProductoService productoService, Producto producto) {
        this.productoService = productoService;
        this.producto = producto != null ? producto : new Producto();

        initModality(Modality.APPLICATION_MODAL);
        setTitle(producto == null ? "FarmaCiando - Nuevo Producto" : "FarmaCiando - Editar Producto");
        setResizable(false);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Título
        Label titulo = new Label(producto == null ? "➕ Nuevo Producto" : "✏️ Editar Producto");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Formulario
        GridPane grid = createForm();

        // Botones
        HBox buttonBox = createButtonBox();

        root.getChildren().addAll(titulo, grid, buttonBox);

        Scene scene = new Scene(root, 600, 650);
        setScene(scene);

        // Cargar datos si es edición
        if (producto != null && producto.getId() != null) {
            cargarDatos();
        }
    }

    private GridPane createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        int row = 0;

        // Código
        Label lblCodigo = new Label("*Código:");
        txtCodigo = new TextField();
        txtCodigo.setPromptText("Ej: MED001");
        grid.add(lblCodigo, 0, row);
        grid.add(txtCodigo, 1, row);
        row++;

        // Nombre
        Label lblNombre = new Label("*Nombre:");
        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del producto");
        txtNombre.setPrefWidth(350);
        grid.add(lblNombre, 0, row);
        grid.add(txtNombre, 1, row);
        row++;

        // Descripción
        Label lblDescripcion = new Label("Descripción:");
        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Descripción del producto");
        txtDescripcion.setPrefRowCount(3);
        txtDescripcion.setWrapText(true);
        grid.add(lblDescripcion, 0, row);
        grid.add(txtDescripcion, 1, row);
        row++;

        // Categoría
        Label lblCategoria = new Label("Categoría:");
        cmbCategoria = new ComboBox<>();
        cmbCategoria.getItems().addAll(
            "Analgésicos",
            "Antibióticos",
            "Antiinflamatorios",
            "Vitaminas",
            "Suplementos",
            "Dermatológicos",
            "Cardiovasculares",
            "Digestivos",
            "Respiratorios",
            "Otro"
        );
        cmbCategoria.setEditable(true);
        cmbCategoria.setPrefWidth(350);
        grid.add(lblCategoria, 0, row);
        grid.add(cmbCategoria, 1, row);
        row++;

        // Laboratorio
        Label lblLaboratorio = new Label("Laboratorio:");
        txtLaboratorio = new TextField();
        txtLaboratorio.setPromptText("Nombre del laboratorio");
        grid.add(lblLaboratorio, 0, row);
        grid.add(txtLaboratorio, 1, row);
        row++;

        // Precio
        Label lblPrecio = new Label("*Precio:");
        txtPrecio = new TextField();
        txtPrecio.setPromptText("0.00");
        grid.add(lblPrecio, 0, row);
        grid.add(txtPrecio, 1, row);
        row++;

        // Stock
        Label lblStock = new Label("*Stock:");
        txtStock = new TextField();
        txtStock.setPromptText("Cantidad en inventario");
        grid.add(lblStock, 0, row);
        grid.add(txtStock, 1, row);
        row++;

        // Stock Mínimo
        Label lblStockMinimo = new Label("Stock Mínimo:");
        txtStockMinimo = new TextField();
        txtStockMinimo.setPromptText("10");
        txtStockMinimo.setText("10");
        grid.add(lblStockMinimo, 0, row);
        grid.add(txtStockMinimo, 1, row);
        row++;

        // Fecha de Vencimiento
        Label lblVencimiento = new Label("Vencimiento:");
        dpVencimiento = new DatePicker();
        dpVencimiento.setPromptText("Seleccionar fecha");
        grid.add(lblVencimiento, 0, row);
        grid.add(dpVencimiento, 1, row);
        row++;

        // Requiere Receta
        chkRequiereReceta = new CheckBox("Requiere Receta Médica");
        grid.add(chkRequiereReceta, 1, row);
        row++;

        // Nota
        Label lblNota = new Label("* Campos obligatorios");
        lblNota.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
        grid.add(lblNota, 1, row);

        return grid;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnGuardar = new Button("💾 Guardar");
        btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 120px;");
        btnGuardar.setOnAction(e -> guardarProducto());

        Button btnCancelar = new Button("❌ Cancelar");
        btnCancelar.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 120px;");
        btnCancelar.setOnAction(e -> close());

        buttonBox.getChildren().addAll(btnGuardar, btnCancelar);
        return buttonBox;
    }

    private void cargarDatos() {
        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());
        txtDescripcion.setText(producto.getDescripcion());
        txtPrecio.setText(producto.getPrecio() != null ? producto.getPrecio().toString() : "");
        txtStock.setText(producto.getStock() != null ? producto.getStock().toString() : "");
        txtStockMinimo.setText(producto.getStockMinimo() != null ? producto.getStockMinimo().toString() : "10");
        txtLaboratorio.setText(producto.getLaboratorio());
        cmbCategoria.setValue(producto.getCategoria());
        dpVencimiento.setValue(producto.getFechaVencimiento());
        chkRequiereReceta.setSelected(producto.getRequiereReceta() != null ? producto.getRequiereReceta() : false);
    }

    private void guardarProducto() {
        try {
            // Validar campos obligatorios
            if (txtCodigo.getText().trim().isEmpty()) {
                mostrarError("El código es obligatorio");
                txtCodigo.requestFocus();
                return;
            }

            if (txtNombre.getText().trim().isEmpty()) {
                mostrarError("El nombre es obligatorio");
                txtNombre.requestFocus();
                return;
            }

            if (txtPrecio.getText().trim().isEmpty()) {
                mostrarError("El precio es obligatorio");
                txtPrecio.requestFocus();
                return;
            }

            if (txtStock.getText().trim().isEmpty()) {
                mostrarError("El stock es obligatorio");
                txtStock.requestFocus();
                return;
            }

            // Validar formato de precio
            BigDecimal precio;
            try {
                precio = new BigDecimal(txtPrecio.getText().trim());
                if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                    mostrarError("El precio debe ser mayor a 0");
                    txtPrecio.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("El precio debe ser un número válido");
                txtPrecio.requestFocus();
                return;
            }

            // Validar formato de stock
            int stock;
            try {
                stock = Integer.parseInt(txtStock.getText().trim());
                if (stock < 0) {
                    mostrarError("El stock no puede ser negativo");
                    txtStock.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("El stock debe ser un número entero válido");
                txtStock.requestFocus();
                return;
            }

            // Validar stock mínimo
            int stockMinimo = 10;
            if (!txtStockMinimo.getText().trim().isEmpty()) {
                try {
                    stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
                    if (stockMinimo < 0) {
                        mostrarError("El stock mínimo no puede ser negativo");
                        txtStockMinimo.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarError("El stock mínimo debe ser un número entero válido");
                    txtStockMinimo.requestFocus();
                    return;
                }
            }

            // Asignar valores al producto
            producto.setCodigo(txtCodigo.getText().trim().toUpperCase());
            producto.setNombre(txtNombre.getText().trim());
            producto.setDescripcion(txtDescripcion.getText().trim());
            producto.setPrecio(precio);
            producto.setStock(stock);
            producto.setStockMinimo(stockMinimo);
            producto.setLaboratorio(txtLaboratorio.getText().trim());
            producto.setCategoria(cmbCategoria.getValue());
            producto.setFechaVencimiento(dpVencimiento.getValue());
            producto.setRequiereReceta(chkRequiereReceta.isSelected());

            // Guardar en la base de datos
            productoService.guardarProducto(producto);

            guardado = true;
            mostrarInfo("Producto guardado correctamente");
            close();

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error al guardar el producto: " + e.getMessage());
            e.printStackTrace();
        }
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

    public boolean isGuardado() {
        return guardado;
    }
}

package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.model.Producto;
import dulceria.model.ProductoImagen;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoController {

    @FXML
    private TableView<Producto> tableProductos;

    @FXML
    private TableColumn<Producto, Integer> colId; // Asegúrate de que el tipo sea correcto

    @FXML
    private TableColumn<Producto, String> colNombre, colDescripcion, colCategoria;

    @FXML
    private TableColumn<Producto, Double> colPrecio;

    @FXML
    private TableColumn<Producto, Integer> colExistencia;

    @FXML
    private TextField txtNombre, txtCategoria, txtPrecio, txtCosto, txtExistencia;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private Button btnGuardar, btnActualizar, btnEliminar, btnCancelar;

    @FXML
    private HBox galleryContainer;

    private ObservableList<Producto> productos;
    private ObservableList<ProductoImagen> imagenes;

    private final DatabaseConnection dbConnection = new DatabaseConnection();

    private Producto productoSeleccionado;
    private List<File> imagenesSeleccionadas;

    @FXML
    public void initialize() {
        configureTable();
        loadProductos();
        configureGallery();

        tableProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadProductoDetails(newSelection);
            }
        });
    }

    private void configureTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNombre()));
        colDescripcion.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDescripcion()));
        colCategoria.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCategoria()));
        colPrecio.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrecio()));
        colExistencia.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getExistencia()));
    }

    private void loadProductos() {
        productos = FXCollections.observableArrayList();
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM producto")) {

            while (rs.next()) {
                Producto producto = new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getDouble("costo"),
                        rs.getInt("existencia")
                );
                productos.add(producto);
            }
        } catch (SQLException e) {
            showError("Error cargando productos", e.getMessage());
        }
        tableProductos.setItems(productos);
    }

    private void configureGallery() {
        galleryContainer.getChildren().clear();
        if (productoSeleccionado != null) {
            imagenes = FXCollections.observableArrayList(loadImagenes(productoSeleccionado.getId()));
            for (ProductoImagen imagen : imagenes) {
                ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(imagen.getImagen())));
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                galleryContainer.getChildren().add(imageView);
            }
        }
    }

    private List<ProductoImagen> loadImagenes(int productoId) {
        List<ProductoImagen> listaImagenes = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM producto_imagen WHERE producto_id = ?")) {

            pstmt.setInt(1, productoId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                listaImagenes.add(new ProductoImagen(
                        rs.getInt("id"),
                        rs.getInt("producto_id"),
                        rs.getBytes("imagen"),
                        rs.getString("descripcion")
                ));
            }
        } catch (SQLException e) {
            showError("Error cargando imágenes", e.getMessage());
        }
        return listaImagenes;
    }

    private void loadProductoDetails(Producto producto) {
        this.productoSeleccionado = producto;
        txtNombre.setText(producto.getNombre());
        txtDescripcion.setText(producto.getDescripcion());
        txtCategoria.setText(producto.getCategoria());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
        txtCosto.setText(String.valueOf(producto.getCosto()));
        txtExistencia.setText(String.valueOf(producto.getExistencia()));
        configureGallery();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void onGuardar() {
        if (validarCampos()) {
            String nombre = txtNombre.getText();
            String descripcion = txtDescripcion.getText();
            String categoria = txtCategoria.getText();
            double precio = Double.parseDouble(txtPrecio.getText());
            double costo = Double.parseDouble(txtCosto.getText());
            int existencia = Integer.parseInt(txtExistencia.getText());

            String sql = "INSERT INTO producto (nombre, descripcion, categoria, precio, costo, existencia) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombre);
                stmt.setString(2, descripcion);
                stmt.setString(3, categoria);
                stmt.setDouble(4, precio);
                stmt.setDouble(5, costo);
                stmt.setInt(6, existencia);

                stmt.executeUpdate();
                mostrarAlerta("Éxito", "Producto guardado exitosamente", Alert.AlertType.INFORMATION);
                cargarProductos();
                clearForm();
            } catch (SQLException e) {
                mostrarAlerta("Error", "Ocurrió un error al guardar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void onActualizar() {
        if (productoSeleccionado != null && validarCampos()) {
            String nombre = txtNombre.getText();
            String descripcion = txtDescripcion.getText();
            String categoria = txtCategoria.getText();
            double precio = Double.parseDouble(txtPrecio.getText());
            double costo = Double.parseDouble(txtCosto.getText());
            int existencia = Integer.parseInt(txtExistencia.getText());

            String sql = "UPDATE producto SET nombre = ?, descripcion = ?, categoria = ?, precio = ?, costo = ?, existencia = ? WHERE id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombre);
                stmt.setString(2, descripcion);
                stmt.setString(3, categoria);
                stmt.setDouble(4, precio);
                stmt.setDouble(5, costo);
                stmt.setInt(6, existencia);
                stmt.setInt(7, productoSeleccionado.getId());

                stmt.executeUpdate();
                mostrarAlerta("Éxito", "Producto actualizado exitosamente", Alert.AlertType.INFORMATION);
                cargarProductos();
                clearForm();
            } catch (SQLException e) {
                mostrarAlerta("Error", "Ocurrió un error al actualizar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Advertencia", "Debe seleccionar un producto para actualizar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void onEliminar() {
        if (productoSeleccionado != null) {
            String sql = "DELETE FROM producto WHERE id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, productoSeleccionado.getId());

                stmt.executeUpdate();
                mostrarAlerta("Éxito", "Producto eliminado exitosamente", Alert.AlertType.INFORMATION);
                cargarProductos();
                clearForm();
            } catch (SQLException e) {
                mostrarAlerta("Error", "Ocurrió un error al eliminar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Advertencia", "Debe seleccionar un producto para eliminar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void onCancelar() {
        clearForm();
    }

    private void clearForm() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtCategoria.clear();
        txtPrecio.clear();
        txtCosto.clear();
        txtExistencia.clear();
        galleryContainer.getChildren().clear();
        productoSeleccionado = null;
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtCategoria.getText().isEmpty() || txtPrecio.getText().isEmpty()
                || txtCosto.getText().isEmpty() || txtExistencia.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
            return false;
        }

        try {
            Double.parseDouble(txtPrecio.getText());
            Double.parseDouble(txtCosto.getText());
            Integer.parseInt(txtExistencia.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "Precio, costo y existencia deben ser valores numéricos", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void cargarProductos() {
        tableProductos.getItems().clear();
        String sql = "SELECT * FROM producto";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Producto producto = new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getDouble("costo"),
                        rs.getInt("existencia")
                );
                tableProductos.getItems().add(producto);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Ocurrió un error al cargar los productos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

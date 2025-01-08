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
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoController {

    @FXML
    private TableView<Producto> tableProductos;

    @FXML
    private TableView<ProductoImagen> tblImagenes;

    @FXML
    private TableColumn<ProductoImagen, Integer> colId_img;

    @FXML
    private TableColumn<ProductoImagen, String> colDescripcion_img;

    @FXML
    private TableColumn<ProductoImagen, ImageView> colVistaPrevia;

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

    private final DatabaseConnection dbConnection = new DatabaseConnection();

    private Producto productoSeleccionado;

    private ObservableList<ProductoImagen> listaImagenes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        loadProductos();

        tableProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadProductoDetails(newSelection);
            }
        });

        // Configurar la lista observable y vincularla a la tabla
        tblImagenes.setItems(listaImagenes);

        // Configurar columnas
        colId_img.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colDescripcion_img.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());

        // Configurar la columna de Vista Previa con ImageView
        colVistaPrevia.setCellValueFactory(param -> {
            Image imagen = param.getValue().getImagen();
            ImageView imageView = new ImageView(imagen);
            
            // Ajustar el tamaño de la imagen
            imageView.setFitWidth(30);  // Ajustar el ancho
            imageView.setFitHeight(30); // Ajustar la altura
            imageView.setPreserveRatio(true); // Mantener la relación de aspecto
    
            return new SimpleObjectProperty<>(imageView);  // Retornar el ImageView
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

    // Método para cargar imágenes asociadas a un producto
    private ObservableList<ProductoImagen> loadImagenes(int productoId) {
        ObservableList<ProductoImagen> imagenes = FXCollections.observableArrayList();

        String sql = "SELECT * FROM producto_imagen WHERE producto_id = ?";
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasImages = false;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String descripcion = rs.getString("descripcion"); // Obtener la descripción
                    byte[] imagenBytes = rs.getBytes("imagen"); // Obtener la imagen en formato binario

                    // Convertir el arreglo de bytes a una imagen
                    Image imagen = new Image(new ByteArrayInputStream(imagenBytes));

                    // Crear el objeto ProductoImagen con los campos id, imagen y descripcion
                    ProductoImagen productoImagen = new ProductoImagen(id, imagen, descripcion);
                    imagenes.add(productoImagen);
                    hasImages = true;
                }

                if (!hasImages) {
                    showInfo("No se encontraron imágenes para este producto.");
                }

            }
        } catch (SQLException e) {
            showError("Error cargando imágenes", e.getMessage());
        }

        return imagenes;
    }

    // Método para mostrar mensaje de información
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadProductoDetails(Producto producto) {
        this.productoSeleccionado = producto;
        txtNombre.setText(producto.getNombre());
        txtDescripcion.setText(producto.getDescripcion());
        txtCategoria.setText(producto.getCategoria());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
        txtCosto.setText(String.valueOf(producto.getCosto()));
        txtExistencia.setText(String.valueOf(producto.getExistencia()));
        
        // Cargar imágenes asociadas al producto
        listaImagenes.clear();  // Limpiar la lista antes de cargar los nuevos datos
        listaImagenes = loadImagenes(producto.getId());  // Cargar las imágenes del producto seleccionado
        tblImagenes.setItems(listaImagenes);  // Cargar las imágenes en la tabla

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
        listaImagenes.clear();  // Elimina todos los elementos de la lista
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

    @FXML
    private void onAgregarImagen() {
        // Paso 1: Mostrar un FileChooser para seleccionar una imagen
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        
        File file = fileChooser.showOpenDialog(null);
        
        if (file != null) {
            try {
                // Paso 2: Convertir la imagen a un arreglo de bytes
                byte[] imageBytes = Files.readAllBytes(file.toPath());

                // Paso 3: Pedir al usuario una descripción opcional
                TextInputDialog descripcionDialog = new TextInputDialog();
                descripcionDialog.setTitle("Descripción de la Imagen");
                descripcionDialog.setHeaderText("Ingrese una descripción para la imagen (opcional):");
                descripcionDialog.setContentText("Descripción:");

                Optional<String> result = descripcionDialog.showAndWait();
                String descripcion = result.orElse("Sin descripción");

                // Paso 4: Mostrar la imagen en la vista previa
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                ProductoImagen nuevoProductoImagen = new ProductoImagen(0, image, descripcion);
                
                // Agregar el nuevo producto imagen a la tabla (TableView)
                tblImagenes.getItems().add(nuevoProductoImagen);

                // Paso 5: Guardar la imagen en la base de datos
                guardarImagenEnBaseDeDatos(imageBytes, descripcion, productoSeleccionado.getId()); // Asumiendo que productoId está disponible

            } catch (IOException e) {
                showError("Error al cargar la imagen", e.getMessage());
            }
        }
    }

    private void guardarImagenEnBaseDeDatos(byte[] imageBytes, String descripcion, int productoId) {
        String sql = "INSERT INTO producto_imagen (producto_id, imagen, descripcion) VALUES (?, ?, ?)";
    
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setInt(1, productoId);  // Suponiendo que ya tienes el productoId disponible
            stmt.setBytes(2, imageBytes); // Los datos de la imagen en formato binario
            stmt.setString(3, descripcion); // Descripción de la imagen
    
            stmt.executeUpdate();
            showInfo("Imagen guardada correctamente.");
            tblImagenes.setItems(loadImagenes(productoId));
    
        } catch (SQLException e) {
            showError("Error al guardar la imagen", e.getMessage());
        }
    }
    
    @FXML
    private void onEliminarImagen() {
        System.out.print("antes: "+ listaImagenes.size() + " ");
        ProductoImagen seleccionada = tblImagenes.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar esta imagen?");
            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                listaImagenes.remove(seleccionada);
                // Eliminar la imagen de la base de datos
                eliminarImagenDeBaseDeDatos(seleccionada.getId());
            }
        } else {
            mostrarAlerta("Seleccione una imagen", "Por favor, seleccione una imagen para eliminar.", Alert.AlertType.WARNING);
        }
    }

    private void eliminarImagenDeBaseDeDatos(int imagenId) {
        String sql = "DELETE FROM producto_imagen WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, imagenId);  // Establecer el ID de la imagen a eliminar
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                showInfo("La imagen ha sido eliminada correctamente.");
            } else {
                showError("Error al eliminar", "No se pudo eliminar la imagen.");
            }
        } catch (SQLException e) {
            showError("Error de base de datos", e.getMessage());
        }
    }

}

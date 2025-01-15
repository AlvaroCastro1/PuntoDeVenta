package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.app.App;
import dulceria.model.Producto;
import dulceria.model.ProductoImagen;
import dulceria.model.Usuario;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
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
    private TableColumn<Producto, Integer> colId;

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
        Usuario u = App.getUsuarioAutenticado();
        System.out.println(u.getPermisos().get(0));
    }

    private void configureTable() {
        // tabla productos
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNombre()));
        colDescripcion.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDescripcion()));
        colCategoria.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCategoria()));
        colPrecio.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrecio()));
        colExistencia.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getExistencia()));
        
        //tabla imagenes
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

        // Configurar doble clic
        tblImagenes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ProductoImagen selected = tblImagenes.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    mostrarImagen(selected.getImagen());
                }
            }
        });
    }

    // Método para mostrar la imagen en una nueva ventana
    private void mostrarImagen(Image image) {
        Stage stage = new Stage();
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
    
        // Ajustar el tamaño de la ventana al tamaño original de la imagen
        Scene scene = new Scene(new javafx.scene.layout.StackPane(imageView));
        stage.setScene(scene);
        stage.setTitle("Vista Previa de Imagen");
    
        // Configurar el tamaño de la ventana según el tamaño de la imagen
        stage.setWidth(image.getWidth());
        stage.setHeight(image.getHeight());
        
        stage.show();
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
            mostrarAlerta("Error", "Error cargando productos", Alert.AlertType.ERROR);
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
                    mostrarAlerta("Información", "No se encontraron imágenes para este producto.", Alert.AlertType.INFORMATION);
                }

            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error cargando imágenes", Alert.AlertType.ERROR);
        }

        return imagenes;
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

    @FXML
    public void onGuardar() {

        if (validarCampos()) {
            if (productoSeleccionado != null) { // ACTUALIZAR PRODUCTO
                actualizarProductoConImagenes(productoSeleccionado, listaImagenes);
            } else { //GUARDAR PRODUCTO NUEVO
                Producto nuevoProducto = new Producto(
                    txtNombre.getText(),
                    txtDescripcion.getText(),
                    txtCategoria.getText(),
                    Double.parseDouble(txtPrecio.getText()),
                    Double.parseDouble(txtCosto.getText()),
                    Integer.parseInt(txtExistencia.getText())
                );
                guardarProductoConImagenes(nuevoProducto, listaImagenes);
            }
        }
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
                 // Obtener la URL del archivo seleccionado
                String fileUrl = file.toURI().toString();

                // Crear la imagen desde la URL del archivo
                Image image = new Image(fileUrl);
                ProductoImagen nuevoProductoImagen = new ProductoImagen(0, image, descripcion);
                
                // Agregar el nuevo producto imagen a la tabla (TableView)
                tblImagenes.getItems().add(nuevoProductoImagen);

                // // Paso 5: Guardar la imagen en la base de datos
                // guardarImagenEnBaseDeDatos(imageBytes, descripcion, productoSeleccionado.getId()); // Asumiendo que productoId está disponible

            } catch (IOException e) {
                mostrarAlerta("Error", "Ocurrió un error al cargar la imagen " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onEliminarImagen() {
        ProductoImagen seleccionada = tblImagenes.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar esta imagen?");
            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                listaImagenes.remove(seleccionada);
                // Eliminar la imagen de la base de datos
                // eliminarImagenDeBaseDeDatos(seleccionada.getId());
            }
        } else {
            mostrarAlerta("Seleccione una imagen", "Por favor, seleccione una imagen para eliminar.", Alert.AlertType.WARNING);
        }
    }

    public void guardarProductoConImagenes(Producto producto, ObservableList<ProductoImagen> listaImagenes) {
        String sqlProducto = "INSERT INTO producto (nombre, descripcion, categoria, precio, costo, existencia) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlImagen = "INSERT INTO producto_imagen (producto_id, imagen, descripcion) VALUES (?, ?, ?)";
    
        try (Connection conn = dbConnection.getConnection()) {
            // Desactivar auto-commit para iniciar la transacción
            conn.setAutoCommit(false);
    
            // Guardar el producto
            int productoId;
            try (PreparedStatement stmtProducto = conn.prepareStatement(sqlProducto, Statement.RETURN_GENERATED_KEYS)) {
                stmtProducto.setString(1, producto.getNombre());
                stmtProducto.setString(2, producto.getDescripcion());
                stmtProducto.setString(3, producto.getCategoria());
                stmtProducto.setDouble(4, producto.getPrecio());
                stmtProducto.setDouble(5, producto.getCosto());
                stmtProducto.setInt(6, producto.getExistencia());
    
                int filasProducto = stmtProducto.executeUpdate();
                if (filasProducto == 0) {
                    mostrarAlerta("Error al guardarel producto", "Hay un error al conectar a la base de datos", Alert.AlertType.ERROR);
                    throw new SQLException("No se pudo insertar el producto.");
                }
    
                // Obtener el ID generado del producto
                try (ResultSet rs = stmtProducto.getGeneratedKeys()) {
                    if (rs.next()) {
                        productoId = rs.getInt(1);
                    } else {
                        mostrarAlerta("Error con el ID del producto", "Hay un error al conectar a la base de datos", Alert.AlertType.ERROR);
                        throw new SQLException("No se pudo obtener el ID del producto.");
                    }
                }
            }
    
            // Guardar las imágenes asociadas al producto
            try (PreparedStatement stmtImagen = conn.prepareStatement(sqlImagen)) {
                for (ProductoImagen productoImagen : listaImagenes) {
                    if (productoImagen.getImagen() != null) { // Validar que la imagen no sea nula
                        Image imagen = productoImagen.getImagen();
                        byte[] imagenBytes;
    
                        // Convertir la imagen a bytes
                        try {
                            // Obtener la URL y quitar el prefijo 'file:'
                            String filePath = imagen.getUrl().replaceFirst("file:", "");

                            // Crear el archivo desde la ruta local
                            Path path = Paths.get(filePath);
                            
                            // Leer los bytes de la imagen
                            imagenBytes = Files.readAllBytes(path);
                        } catch (Exception e) {
                            mostrarAlerta("Error con la imagen del producto", "Hay un error al intentar guardar la imagen\n"+imagen.getUrl(), Alert.AlertType.ERROR);
                            throw new IOException("Error al leer la imagen desde: " + imagen.getUrl(), e);
                        }
    
                        stmtImagen.setInt(1, productoId);
                        stmtImagen.setBytes(2, imagenBytes);
                        stmtImagen.setString(3, productoImagen.getDescripcion());
                        stmtImagen.addBatch();
                    } else {
                        mostrarAlerta("Advertencia", "La imagen esta vacia", Alert.AlertType.WARNING);
                        System.err.println("Advertencia: La imagen es nula para una de las imágenes.");
                    }
                }
                stmtImagen.executeBatch(); // Ejecutar todas las inserciones de imágenes
            }
    
            // Confirmar la transacción
            conn.commit();
            mostrarAlerta("Información", "Producto guardado con éxito", Alert.AlertType.INFORMATION);
            clearForm();

        } catch (SQLException | IOException e) {
            try {
                // Si algo falla, deshacer todos los cambios
                dbConnection.getConnection().rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error al hacer rollback: " + rollbackEx.getMessage());
                mostrarAlerta("Error", "Error al hacer rollback", Alert.AlertType.ERROR);
            }
            System.err.println("Error al guardar el producto y las imágenes: " + e.getMessage());
            mostrarAlerta("Error", "al guardar el producto", Alert.AlertType.ERROR);
        }
    }
    
    public void actualizarProductoConImagenes(Producto producto, ObservableList<ProductoImagen> listaImagenes) {
        String sqlActualizarProducto = "UPDATE producto SET nombre = ?, descripcion = ?, categoria = ?, precio = ?, costo = ?, existencia = ? WHERE id = ?";
        String sqlEliminarImagenes = "DELETE FROM producto_imagen WHERE producto_id = ?";
        String sqlInsertarImagen = "INSERT INTO producto_imagen (producto_id, imagen, descripcion) VALUES (?, ?, ?)";
    
        try (Connection conn = dbConnection.getConnection()) {
            // Desactivar auto-commit para iniciar la transacción
            conn.setAutoCommit(false);
    
            // Actualizar el producto
            try (PreparedStatement stmtActualizar = conn.prepareStatement(sqlActualizarProducto)) {
                stmtActualizar.setString(1, producto.getNombre());
                stmtActualizar.setString(2, producto.getDescripcion());
                stmtActualizar.setString(3, producto.getCategoria());
                stmtActualizar.setDouble(4, producto.getPrecio());
                stmtActualizar.setDouble(5, producto.getCosto());
                stmtActualizar.setInt(6, producto.getExistencia());
                stmtActualizar.setInt(7, producto.getId());
    
                int filasActualizadas = stmtActualizar.executeUpdate();
                if (filasActualizadas == 0) {
                    mostrarAlerta("Error", "No se pudo actualizar el producto", Alert.AlertType.ERROR);
                    throw new SQLException("No se encontró el producto con ID: " + producto.getId());
                }
            }
    
            // Eliminar imágenes existentes del producto
            try (PreparedStatement stmtEliminar = conn.prepareStatement(sqlEliminarImagenes)) {
                stmtEliminar.setInt(1, producto.getId());
                stmtEliminar.executeUpdate();
            }
    
            // Insertar las nuevas imágenes
            try (PreparedStatement stmtInsertarImagen = conn.prepareStatement(sqlInsertarImagen)) {
                for (ProductoImagen productoImagen : listaImagenes) {
                    if (productoImagen.getImagen() != null) { // Validar que la imagen no sea nula
                        Image imagen = productoImagen.getImagen();
                        byte[] imagenBytes;
    
                        // Convertir la imagen a bytes
                        try {
                            String filePath = imagen.getUrl().replaceFirst("file:", "");
                            Path path = Paths.get(filePath);
                            imagenBytes = Files.readAllBytes(path);
                        } catch (Exception e) {
                            mostrarAlerta("Error con la imagen del producto", "Error al leer la imagen desde: " + productoImagen.getImagen().getUrl(), Alert.AlertType.ERROR);
                            throw new IOException("Error al leer la imagen desde: " + productoImagen.getImagen().getUrl(), e);
                        }
    
                        stmtInsertarImagen.setInt(1, producto.getId());
                        stmtInsertarImagen.setBytes(2, imagenBytes);
                        stmtInsertarImagen.setString(3, productoImagen.getDescripcion());
                        stmtInsertarImagen.addBatch();
                    } else {
                        System.err.println("Advertencia: La imagen es nula para una de las imágenes.");
                    }
                }
                stmtInsertarImagen.executeBatch(); // Ejecutar todas las inserciones de imágenes
            }
    
            // Confirmar la transacción
            conn.commit();
            mostrarAlerta("Información", "Producto actualizado con éxito", Alert.AlertType.INFORMATION);
    
        } catch (SQLException | IOException e) {
            try {
                // Si algo falla, deshacer todos los cambios
                if (dbConnection.getConnection() != null) {
                    dbConnection.getConnection().rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error al hacer rollback: " + rollbackEx.getMessage());
                mostrarAlerta("Error", "Error al hacer rollback", Alert.AlertType.ERROR);
            }
            System.err.println("Error al actualizar el producto y las imágenes: " + e.getMessage());
            mostrarAlerta("Error", "Error al actualizar el producto", Alert.AlertType.ERROR);
        }
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
        cargarProductos();

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

package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.app.App;
import dulceria.model.Producto;
import dulceria.model.ProductoImagen;
import dulceria.model.Usuario;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    private TextField txtNombre, txtCodigo, txtCategoria, txtPrecio, txtCosto, txtBusqueda;

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

    Usuario usuario;
    @FXML
    public void initialize() {
        configureTable();
        loadProductos();
        usuario = App.getUsuarioAutenticado();

        // Envolver la lista en un FilteredList
    FilteredList<Producto> filteredData = new FilteredList<>(productos, p -> true);

    // Escuchar cambios en el campo de búsqueda
    txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
        filteredData.setPredicate(producto -> {
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = newValue.toLowerCase();
            return producto.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                   producto.getCategoria().toLowerCase().contains(lowerCaseFilter) ||
                   String.valueOf(producto.getPrecio()).contains(lowerCaseFilter);
        });
    });

    // Enlazar la lista filtrada con una SortedList
    SortedList<Producto> sortedData = new SortedList<>(filteredData);
    sortedData.comparatorProperty().bind(tableProductos.comparatorProperty());

    // Asignar los datos a la tabla
    tableProductos.setItems(sortedData);
        
    }

    private void configureTable() {
        // tabla productos
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNombre()));
        colDescripcion.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDescripcion()));
        colCategoria.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCategoria()));
        colPrecio.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrecio()));
        
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
            // Obtener el Byte[] de la fila actual
            byte[] byteArray = param.getValue().getImagen();
        
            // Verificar si el byteArray no es nulo
            if (byteArray != null) {
                // Convertir Byte[] a Image
                byte[] primitiveBytes = new byte[byteArray.length];
                for (int i = 0; i < byteArray.length; i++) {
                    primitiveBytes[i] = byteArray[i];
                }
                ByteArrayInputStream inputStream = new ByteArrayInputStream(primitiveBytes);
                Image imagen = new Image(inputStream);
        
                // Crear el ImageView
                ImageView imageView = new ImageView(imagen);
        
                // Ajustar el tamaño de la imagen
                imageView.setFitWidth(30);  // Ajustar el ancho
                imageView.setFitHeight(30); // Ajustar la altura
                imageView.setPreserveRatio(true); // Mantener la relación de aspecto
        
                // Retornar el ImageView como una propiedad
                return new SimpleObjectProperty<>(imageView);
            } else {
                // Si no hay imagen, retornar nulo
                return new SimpleObjectProperty<>(null);
            }
        });        

        // Configurar doble clic
        tblImagenes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ProductoImagen selected = tblImagenes.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Image img = convertToImage(selected.getImagen());
                    mostrarImagen(img);
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
                        rs.getString("codigo"),
                        rs.getString("descripcion"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getDouble("costo")
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

                    // Crear el objeto ProductoImagen con los campos id, imagen y descripcion
                    ProductoImagen productoImagen = new ProductoImagen(id, imagenBytes, descripcion);
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
        txtCodigo.setText(producto.getCodigo());
        txtDescripcion.setText(producto.getDescripcion());
        txtCategoria.setText(producto.getCategoria());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
        txtCosto.setText(String.valueOf(producto.getCosto()));
        
        // Cargar imágenes asociadas al producto
        listaImagenes.clear();  // Limpiar la lista antes de cargar los nuevos datos
        listaImagenes = loadImagenes(producto.getId());  // Cargar las imágenes del producto seleccionado
        tblImagenes.setItems(listaImagenes);  // Cargar las imágenes en la tabla

    }

    @FXML
    public void onActualizar() {
        if (productoSeleccionado != null && validarCampos()) {
            String nombre = txtNombre.getText();
            String codigo = txtCodigo.getText();
            String descripcion = txtDescripcion.getText();
            String categoria = txtCategoria.getText();
            double precio = Double.parseDouble(txtPrecio.getText());
            double costo = Double.parseDouble(txtCosto.getText());

            String sql = "UPDATE producto SET nombre = ?, codigo = ?, descripcion = ?, categoria = ?, precio = ?, costo = ? WHERE id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombre);
                stmt.setString(2, codigo);
                stmt.setString(3, descripcion);
                stmt.setString(4, categoria);
                stmt.setDouble(5, precio);
                stmt.setDouble(6, costo);
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
                int id = productoSeleccionado.getId();

                productoSeleccionado = new Producto(
                    id, 
                    txtNombre.getText(), 
                    txtCodigo.getText(),
                    txtDescripcion.getText(), 
                    txtCategoria.getText(), 
                    Double.parseDouble(txtPrecio.getText()), 
                    Double.parseDouble(txtCosto.getText())
                );

                actualizarProductoConImagenes(productoSeleccionado, listaImagenes);
            } else { //GUARDAR PRODUCTO NUEVO
                Producto nuevoProducto = new Producto(
                    txtNombre.getText(),
                    txtCodigo.getText(),
                    txtDescripcion.getText(),
                    txtCategoria.getText(),
                    Double.parseDouble(txtPrecio.getText()),
                    Double.parseDouble(txtCosto.getText())
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

                ProductoImagen nuevoProductoImagen = new ProductoImagen(0, imageBytes, descripcion);
                
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
        String sqlProducto = "INSERT INTO producto (nombre, codigo, descripcion, categoria, precio, costo, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlImagen = "INSERT INTO producto_imagen (producto_id, imagen, descripcion) VALUES (?, ?, ?)";
    
        try (Connection conn = dbConnection.getConnection()) {
            // Desactivar auto-commit para iniciar la transacción
            conn.setAutoCommit(false);
    
            // Guardar el producto
            int productoId;
            try (PreparedStatement stmtProducto = conn.prepareStatement(sqlProducto, Statement.RETURN_GENERATED_KEYS)) {
                stmtProducto.setString(1, producto.getNombre());
                stmtProducto.setString(2, producto.getCodigo());
                stmtProducto.setString(3, producto.getDescripcion());
                stmtProducto.setString(4, producto.getCategoria());
                stmtProducto.setDouble(5, producto.getPrecio());
                stmtProducto.setDouble(6, producto.getCosto());
                stmtProducto.setDouble(7, usuario.getId());
    
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
                        byte[] imagenBytes = productoImagen.getImagen();
    
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

        } catch (SQLException e) {
            System.err.println("Error al guardar el producto y las imágenes: " + e.getMessage());
            mostrarAlerta("Error", "al guardar el producto", Alert.AlertType.ERROR);
        }
    }
    
    public void actualizarProductoConImagenes(Producto producto, ObservableList<ProductoImagen> listaImagenes) {
        String sqlActualizarProducto = "UPDATE producto SET nombre = ?, codigo = ?, descripcion = ?, categoria = ?, precio = ?, costo = ? WHERE id = ?";
        String sqlEliminarImagenes = "DELETE FROM producto_imagen WHERE producto_id = ?";
        String sqlInsertarImagen = "INSERT INTO producto_imagen (producto_id, imagen, descripcion) VALUES (?, ?, ?)";
    
        try (Connection conn = dbConnection.getConnection()) {
            // Desactivar auto-commit para iniciar la transacción
            conn.setAutoCommit(false);
    
            // Actualizar el producto
            try (PreparedStatement stmtActualizar = conn.prepareStatement(sqlActualizarProducto)) {
                stmtActualizar.setString(1, producto.getNombre());
                stmtActualizar.setString(2, producto.getCodigo());
                stmtActualizar.setString(3, producto.getDescripcion());
                stmtActualizar.setString(4, producto.getCategoria());
                stmtActualizar.setDouble(5, producto.getPrecio());
                stmtActualizar.setDouble(6, producto.getCosto());
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
                        byte[] imagenBytes = productoImagen.getImagen();
    
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
            cargarProductos();
    
        } catch (SQLException e) {
            System.err.println("Error al actualizar el producto y las imágenes: " + e.getMessage());
            mostrarAlerta("Error", "Error al actualizar el producto", Alert.AlertType.ERROR);
        }
    }
    
    private void clearForm() {
        txtNombre.clear();
        txtCodigo.clear();
        txtDescripcion.clear();
        txtCategoria.clear();
        txtPrecio.clear();
        txtCosto.clear();
        listaImagenes.clear();  // Elimina todos los elementos de la lista
        productoSeleccionado = null;
        cargarProductos();

    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtCodigo.getText().isEmpty() || txtCategoria.getText().isEmpty() || txtPrecio.getText().isEmpty()
                || txtCosto.getText().isEmpty() ) {
            mostrarAlerta("Advertencia", "Todos los campos son obligatorios", Alert.AlertType.WARNING);
            return false;
        }

        try {
            Double.parseDouble(txtPrecio.getText());
            Double.parseDouble(txtCosto.getText());
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
                        rs.getString("codigo"),
                        rs.getString("descripcion"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getDouble("costo")
                );
                tableProductos.getItems().add(producto);
            }
        } catch (SQLException e) {
            System.out.println("aqui");
            mostrarAlerta("Error", "Ocurrió un error al cargar los productos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static Image convertToImage(byte[] byteArray) {
        // Convertir Byte[] a byte[]
        byte[] primitiveBytes = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            primitiveBytes[i] = byteArray[i];
        }

        // Crear un ByteArrayInputStream a partir del byte[]
        ByteArrayInputStream inputStream = new ByteArrayInputStream(primitiveBytes);

        // Crear y devolver la imagen
        return new Image(inputStream);
    }
}

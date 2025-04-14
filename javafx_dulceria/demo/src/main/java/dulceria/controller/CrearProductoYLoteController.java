package dulceria.controller;

import dulceria.model.Producto;
import dulceria.model.Usuario;
import dulceria.DatabaseConnection;
import dulceria.app.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;
import javafx.animation.PauseTransition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CrearProductoYLoteController {

    @FXML
    private TextField searchField;

    @FXML
    private TextField nombreField;

    @FXML
    private TextField codigoField;

    @FXML
    private TextField categoriaField;

    @FXML
    private TextField precioField;

    @FXML
    private TextField costoField;

    @FXML
    private TextField cantidadField;

    @FXML
    private DatePicker fechaCaducidadPicker;

    @FXML
    private javafx.scene.control.ListView<String> searchResultsListView;

    @FXML
    private javafx.scene.control.CheckBox sinFechaCaducidadCheck;

    private Usuario usuario;

    private final List<Producto> productos = new ArrayList<>();

    @FXML
    private void onGuardar(ActionEvent event) {
        String nombre = nombreField.getText();
        String codigo = codigoField.getText();
        String categoria = categoriaField.getText();
        String precio = precioField.getText();
        String costo = costoField.getText();
        String cantidad = cantidadField.getText();
        LocalDate fechaCaducidad = fechaCaducidadPicker.getValue();

        if (nombre.isEmpty() || codigo.isEmpty() || categoria.isEmpty() || precio.isEmpty() || cantidad.isEmpty()) {
            showAlert("Error", "Por favor, complete todos los campos obligatorios.", AlertType.ERROR);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Iniciar la transacción
            connection.setAutoCommit(false);

            try {
                // Verificar si se seleccionó un producto
                String selected = searchResultsListView.getSelectionModel().getSelectedItem();
                int idProducto;

                if (selected != null) {
                    // Actualizar el producto existente
                    String codigoSeleccionado = selected.split(" - ")[0];
                    String updateProductoQuery = "UPDATE producto SET nombre = ?, codigo = ?, categoria = ?, precio = ?, costo = ?, updated_at = CURRENT_TIMESTAMP WHERE codigo = ?";
                    try (PreparedStatement updateProductoStmt = connection.prepareStatement(updateProductoQuery)) {
                        updateProductoStmt.setString(1, nombre);
                        updateProductoStmt.setString(2, codigo);
                        updateProductoStmt.setString(3, categoria);
                        updateProductoStmt.setBigDecimal(4, new java.math.BigDecimal(precio));
                        updateProductoStmt.setBigDecimal(5, new java.math.BigDecimal(costo));
                        updateProductoStmt.setString(6, codigoSeleccionado);
                        updateProductoStmt.executeUpdate();
                    }

                    // Obtener el ID del producto actualizado
                    idProducto = obtenerIdProductoPorCodigo(connection, codigoSeleccionado);
                } else {
                    // Crear un nuevo producto
                    String insertProductoQuery = "INSERT INTO producto (nombre, codigo, categoria, precio, costo, id_usuario, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                    try (PreparedStatement insertProductoStmt = connection.prepareStatement(insertProductoQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        insertProductoStmt.setString(1, nombre);
                        insertProductoStmt.setString(2, codigo);
                        insertProductoStmt.setString(3, categoria);
                        insertProductoStmt.setBigDecimal(4, new java.math.BigDecimal(precio));
                        insertProductoStmt.setBigDecimal(5, new java.math.BigDecimal(costo));
                        insertProductoStmt.setInt(6, usuario.getId());
                        insertProductoStmt.executeUpdate();

                        // Obtener el ID del producto recién creado
                        try (ResultSet generatedKeys = insertProductoStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                idProducto = generatedKeys.getInt(1);
                            } else {
                                throw new Exception("No se pudo obtener el ID del producto recién creado.");
                            }
                        }
                    }
                }

                // Crear el nuevo lote
                crearLote(connection, idProducto, cantidad, fechaCaducidad);

                // Confirmar la transacción
                connection.commit();
                showAlert("Éxito", "Producto y lote guardados correctamente.", AlertType.INFORMATION);
                clearFields();
            } catch (Exception e) {
                // Revertir la transacción en caso de error
                connection.rollback();
                e.printStackTrace();
                showAlert("Error", "Ocurrió un error al guardar el producto y el lote.", AlertType.ERROR);
            } finally {
                // Restaurar el modo de auto-commit
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo establecer la conexión con la base de datos.", AlertType.ERROR);
        }
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        // Lógica para cancelar la operación
        System.out.println("Operación cancelada.");
        clearFields();
    }

    private void clearFields() {
        searchField.clear();
        nombreField.clear();
        codigoField.clear();
        categoriaField.clear();
        precioField.clear();
        costoField.clear();
        cantidadField.clear();
        fechaCaducidadPicker.setValue(null);
    }

    private void showAlert(String title, String content, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void cargarProductosDesdeBaseDeDatos() {
        String query = "SELECT id, nombre, codigo, descripcion, categoria, precio, costo FROM producto";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            productos.clear(); // Limpia la lista antes de cargar nuevos datos

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                String codigo = resultSet.getString("codigo");
                String descripcion = resultSet.getString("descripcion");
                String categoria = resultSet.getString("categoria");
                double precio = resultSet.getDouble("precio");
                double costo = resultSet.getDouble("costo");

                Producto producto = new Producto(id, nombre, codigo, descripcion, categoria, precio, costo);
                productos.add(producto);
            }

            System.out.println("Productos cargados desde la base de datos: " + productos.size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar los productos desde la base de datos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void initialize() {
        usuario = App.getUsuarioAutenticado();

        cargarProductosDesdeBaseDeDatos();

        // Crear un PauseTransition para optimizar las búsquedas
        PauseTransition pause = new PauseTransition(javafx.util.Duration.millis(300));

        // Configurar el comportamiento del campo de búsqueda con una expresión lambda
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                searchResultsListView.setVisible(false);
                return;
            }

            // Configurar el evento que se ejecutará después de la pausa
            pause.setOnFinished(event -> {
                List<String> results = productos.stream()
                        .filter(producto -> producto.getNombre().toLowerCase().contains(newValue.toLowerCase()) ||
                                producto.getCodigo().toLowerCase().contains(newValue.toLowerCase()))
                        .map(producto -> producto.getCodigo() + " - " + producto.getNombre())
                        .collect(Collectors.toList());

                if (results.isEmpty()) {
                    searchResultsListView.setVisible(false);
                } else {
                    searchResultsListView.setItems(FXCollections.observableArrayList(results));
                    searchResultsListView.setVisible(true);
                }
            });

            pause.playFromStart(); // Reinicia la pausa cada vez que se escribe algo
        });

        // Configurar el evento de selección en el ListView
        searchResultsListView.setOnMouseClicked(event -> {
            String selected = searchResultsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String codigo = selected.split(" - ")[0];
                Producto producto = productos.stream()
                        .filter(p -> p.getCodigo().equals(codigo))
                        .findFirst()
                        .orElse(null);

                if (producto != null) {
                    // Rellena los campos con los datos del producto seleccionado
                    nombreField.setText(producto.getNombre());
                    codigoField.setText(producto.getCodigo());
                    categoriaField.setText(producto.getCategoria());
                    precioField.setText(String.valueOf(producto.getPrecio()));
                    costoField.setText(String.valueOf(producto.getCosto()));
                }

                // Oculta la lista de resultados
                searchResultsListView.setVisible(false);
            }
        });
    }

    @FXML
    public void onSelectProduct(javafx.scene.input.MouseEvent event) {
        String selected = searchResultsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String codigo = selected.split(" - ")[0];
            Producto producto = productos.stream()
                    .filter(p -> p.getCodigo().equals(codigo))
                    .findFirst()
                    .orElse(null);

            if (producto != null) {
                // Rellena los campos con los datos del producto seleccionado
                nombreField.setText(producto.getNombre());
                codigoField.setText(producto.getCodigo());
                categoriaField.setText(producto.getCategoria());
                precioField.setText(String.valueOf(producto.getPrecio()));
                costoField.setText(String.valueOf(producto.getCosto()));
            }

            // Oculta la lista de resultados
            searchResultsListView.setVisible(false);
        }
    }

    private int obtenerIdProductoPorCodigo(Connection connection, String codigo) throws Exception {
        String query = "SELECT id FROM producto WHERE codigo = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, codigo);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                } else {
                    throw new Exception("Producto no encontrado con el código: " + codigo);
                }
            }
        }
    }

    private void crearLote(Connection connection, int idProducto, String cantidad, LocalDate fechaCaducidad) throws Exception {
        String insertLoteQuery = "INSERT INTO lote (id_producto, id_usuario, cantidad, fecha_caducidad, fecha_entrada, id_state, created_at, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement insertLoteStmt = connection.prepareStatement(insertLoteQuery)) {
            insertLoteStmt.setInt(1, idProducto);
            insertLoteStmt.setInt(2, usuario.getId()); // Reemplaza con el ID del usuario actual
            insertLoteStmt.setInt(3, Integer.parseInt(cantidad));
            
            // Verificar si el CheckBox está marcado
            if (sinFechaCaducidadCheck.isSelected()) {
                insertLoteStmt.setNull(4, java.sql.Types.DATE); // Insertar NULL si no hay fecha de caducidad
            } else if (fechaCaducidad != null) {
                insertLoteStmt.setDate(4, java.sql.Date.valueOf(fechaCaducidad));
            } else {
                insertLoteStmt.setNull(4, java.sql.Types.DATE); // Manejo adicional por si no se selecciona fecha
            }
            
            insertLoteStmt.executeUpdate();
        }
    }
}
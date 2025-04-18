package dulceria.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

import dulceria.DatabaseConnection;
import dulceria.app.App;
import dulceria.model.Producto;
import dulceria.model.Usuario;
import dulceria.model.Lote;
import dulceria.model.Estado;

public class LoteController {

    @FXML
    private TextField txtCantidad;
    @FXML
    private ComboBox<Producto> cmbIdProducto;  // Usamos ComboBox de Producto
    @FXML
    private ComboBox<Estado> cmbIdState;  // Usamos ComboBox de Estado
    @FXML
    private DatePicker datePickerFechaCaducidad;  // Usamos DatePicker para la fecha de caducidad
    @FXML
    private DatePicker datePickerFechaEntrada;
    @FXML
    private TableView<Lote> tableLote;
    @FXML
    private TableColumn<Lote, Integer> colId, colCantidad;
    @FXML
    private TableColumn<Lote, String> colProducto;
    @FXML
    private TableColumn<Lote, String> colEstado;
    @FXML
    private TableColumn<Lote, Integer> colIdState;
    @FXML
    private TableColumn<Lote, String> colFechaCaducidad;
    @FXML
    private TableColumn<Lote, String> colCodigo; // Columna para el código del producto
    @FXML
    private Button btnAdd, btnUpdate, btnDelete, btnClear;
    @FXML
    private TextField txtSearch; // Campo de búsqueda

    private Usuario usuario;

    private ObservableList<Lote> loteList = FXCollections.observableArrayList();
    private ObservableList<Producto> productoList = FXCollections.observableArrayList();  // Lista de productos
    private ObservableList<Estado> stateList = FXCollections.observableArrayList();
    private FilteredList<Lote> filteredLoteList; // Lista filtrada para la tabla

    private DatabaseConnection dbConnection;

    public LoteController() {
        dbConnection = new DatabaseConnection();
    }

    public void initialize() {
        usuario = App.getUsuarioAutenticado();

        // Configurar las columnas de la tabla
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        colFechaCaducidad.setCellValueFactory(cellData -> {
            java.util.Date fecha = cellData.getValue().getFechaCaducidad();
            return new SimpleStringProperty(fecha != null ? fecha.toString() : "Sin fecha");
        });

        colProducto.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue().getProducto();
            return new SimpleStringProperty(producto != null ? producto.getNombre() : "Sin producto");
        });

        colCodigo.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue().getProducto();
            return new SimpleStringProperty(producto != null ? producto.getCodigo() : "Sin código");
        });

        colEstado.setCellValueFactory(cellData -> {
            Estado estado = cellData.getValue().getEstadoLote();
            return new SimpleStringProperty(estado != null ? estado.getNombre() : "Sin estado");
        });

        // Ajustar automáticamente el ancho de las columnas para ocupar todo el espacio disponible
        tableLote.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cargar datos en los ComboBox y la tabla
        loadComboBoxData();
        loadData();

        // Configurar eventos de los botones
        btnAdd.setOnAction(event -> addLote());
        btnUpdate.setOnAction(event -> updateLote());
        btnDelete.setOnAction(event -> deleteLote());
        btnClear.setOnAction(event -> clearFields());

        // Listener para la selección de un lote en la tabla
        tableLote.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadSelectedLoteData(newValue); // Cargar los datos del lote seleccionado
        });

        // Configurar el buscador
        configureSearch();
    }

    private void loadSelectedLoteData(Lote lote) {
        if (lote != null) {
            // Rellenar los campos con los datos del lote seleccionado
            txtCantidad.setText(String.valueOf(lote.getCantidad()));
            datePickerFechaEntrada.setValue(new java.sql.Date(lote.getFechaEntrada().getTime()).toLocalDate()); 
            if (lote.getFechaCaducidad() != null) {
                datePickerFechaCaducidad.setValue(new java.sql.Date(lote.getFechaCaducidad().getTime()).toLocalDate());
            } else {
                // Si es null, se puede dejar el campo vacío o poner una fecha por defecto
                datePickerFechaCaducidad.setValue(null);  // O puedes poner un valor por defecto
            }
            cmbIdProducto.setValue(productoList.stream().filter(p -> p.getId() == lote.getIdProducto()).findFirst().orElse(null));
            cmbIdState.setValue(stateList.stream().filter(s -> s.getId() == lote.getIdState()).findFirst().orElse(null));
        }
    }

    private void loadComboBoxData() {
        try (Connection conn = dbConnection.getConnection()) {
            // Cargar productos desde la base de datos
            String sqlProducto = "SELECT id, nombre, codigo FROM producto";
            Statement stmt = conn.createStatement();
            ResultSet rsProducto = stmt.executeQuery(sqlProducto);
            while (rsProducto.next()) {
                // Crear productos y agregarlos a la lista
                Producto producto = new Producto(rsProducto.getInt("id"), rsProducto.getString("nombre"), rsProducto.getString("codigo"),"", "", 0, 0);
                productoList.add(producto);
            }
            cmbIdProducto.setItems(productoList);

            // Cargar estados desde la base de datos
            String sqlState = "SELECT id, nombre_estado FROM cState";
            ResultSet rsState = stmt.executeQuery(sqlState);
            while (rsState.next()) {
                // Aquí agregamos los estados (esto se deja igual que antes)
                stateList.add(new Estado(rsState.getInt("id"), rsState.getString("nombre_estado")));
            }
            cmbIdState.setItems(stateList);

            // Configuramos el ComboBox para mostrar tanto el ID como el nombre
            cmbIdProducto.setCellFactory(param -> new ListCell<Producto>() {
                @Override
                protected void updateItem(Producto item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getId() + " - " + item.getNombre());
                    } else {
                        setText(null);
                    }
                }
            });
            cmbIdProducto.setButtonCell(new ListCell<Producto>() {
                @Override
                protected void updateItem(Producto item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getId() + " - " + item.getNombre());
                    } else {
                        setText(null);
                    }
                }
            });

            cmbIdState.setCellFactory(param -> new ListCell<Estado>() {
                @Override
                protected void updateItem(Estado item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getId() + " - " + item.getNombre());
                    } else {
                        setText(null);
                    }
                }
            });
            cmbIdState.setButtonCell(new ListCell<Estado>() {
                @Override
                protected void updateItem(Estado item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getId() + " - " + item.getNombre());
                    } else {
                        setText(null);
                    }
                }
            });

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar ComboBox "+ e.getMessage());
        }
    }
    private void loadData() {
        try (Connection conn = dbConnection.getConnection()) {
            // Consulta SQL para obtener los datos del lote, producto y estado
            String sql = "SELECT l.id, l.id_producto, l.cantidad, l.fecha_caducidad, l.fecha_entrada, l.id_state, " +
                         "p.nombre AS producto_nombre, p.codigo AS producto_codigo, p.descripcion AS producto_descripcion, " +
                         "p.categoria AS producto_categoria, p.precio AS producto_precio, p.costo AS producto_costo, " +
                         "s.nombre_estado AS estado_nombre " +
                         "FROM lote l " +
                         "JOIN producto p ON l.id_producto = p.id " +
                         "JOIN cstate s ON l.id_state = s.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // Limpiar la lista de lotes antes de cargar nuevos datos
            loteList.clear();

            while (rs.next()) {
                // Construir el objeto Producto
                Producto producto = new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("producto_nombre"),
                    rs.getString("producto_codigo"),
                    rs.getString("producto_descripcion"),
                    rs.getString("producto_categoria"),
                    rs.getDouble("producto_precio"),
                    rs.getDouble("producto_costo")
                );

                // Construir el objeto Estado
                Estado estado = new Estado(
                    rs.getInt("id_state"),
                    rs.getString("estado_nombre")
                );

                // Construir el objeto Lote
                Lote lote = new Lote(
                    rs.getInt("id"),
                    rs.getInt("id_producto"),
                    rs.getInt("cantidad"),
                    rs.getDate("fecha_caducidad"),
                    rs.getDate("fecha_entrada"),
                    rs.getInt("id_state")
                );

                // Asignar el Producto y Estado al Lote
                lote.setProducto(producto);
                lote.setEstadoLote(estado);

                // Agregar el lote a la lista
                loteList.add(lote);
            }

            // Asignar la lista de lotes a la tabla
            tableLote.setItems(loteList);
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar datos: " + e.getMessage());
        }
    }

    private void addLote() {
        // Validar los campos antes de realizar el insert
        if (cmbIdProducto.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Debe seleccionar un producto.");
            return;
        }
        if (txtCantidad.getText().isEmpty() || !txtCantidad.getText().matches("\\d+")) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número válido.");
            return;
        }
        if (cmbIdState.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Debe seleccionar un estado.");
            return;
        }
        if (datePickerFechaEntrada.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Debe seleccionar una fecha de entrada.");
            return;
        }
    
        try (Connection conn = dbConnection.getConnection()) {
            String sql = "INSERT INTO lote (id_producto, cantidad, fecha_caducidad, fecha_entrada, id_state, id_usuario) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
    
            // Obtener el id del producto seleccionado
            stmt.setInt(1, cmbIdProducto.getValue().getId());
            // Validar que la cantidad sea un número entero válido
            stmt.setInt(2, Integer.parseInt(txtCantidad.getText()));
    
            // Validar y convertir la fecha de caducidad
            LocalDate fechaCaducidad = datePickerFechaCaducidad.getValue();
            if (fechaCaducidad != null) {
                stmt.setDate(3, Date.valueOf(fechaCaducidad));  // Convertir LocalDate a Date
            } else {
                stmt.setDate(3, null);  // Si no se seleccionó ninguna fecha, ponemos null
            }
    
            // Validar y convertir la fecha de entrada
            LocalDate fechaEntrada = datePickerFechaEntrada.getValue();
            stmt.setDate(4, Date.valueOf(fechaEntrada));  // Convertir LocalDate a Date
    
            // Obtener el id del estado seleccionado
            stmt.setInt(5, cmbIdState.getValue().getId());

            stmt.setInt(6, usuario.getId());
    
            stmt.executeUpdate();
            loadData();
            clearFields();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Información", "Se guardó el nuevo lote.");
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al agregar el lote: " + e.getMessage());
        }
    }

    private void updateLote() {
        // Validar que se haya seleccionado un lote
        Lote selectedLote = tableLote.getSelectionModel().getSelectedItem();  // Obtener el lote seleccionado de la tabla
        if (selectedLote == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección inválida", "Selecciona un lote para actualizar");
            return;
        }
    
        // Validar que el producto esté seleccionado
        if (cmbIdProducto.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Debe seleccionar un producto.");
            return;
        }
    
        // Validar que la cantidad sea un número válido
        if (txtCantidad.getText().isEmpty() || !txtCantidad.getText().matches("\\d+")) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número válido.");
            return;
        }
    
        // Validar que el estado esté seleccionado
        if (cmbIdState.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Debe seleccionar un estado.");
            return;
        }
    
        // Validar que la fecha de entrada esté seleccionada
        if (datePickerFechaEntrada.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Debe seleccionar una fecha de entrada.");
            return;
        }
    
        try (Connection conn = dbConnection.getConnection()) {
            String sql = "UPDATE lote SET id_producto = ?, cantidad = ?, fecha_caducidad = ?, fecha_entrada = ?, id_state = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
    
            // Obtener el id del producto seleccionado
            stmt.setInt(1, cmbIdProducto.getValue().getId());
            // Validar que la cantidad sea un número entero válido
            stmt.setInt(2, Integer.parseInt(txtCantidad.getText()));
    
            // Validar y convertir la fecha de caducidad
            LocalDate fechaCaducidad = datePickerFechaCaducidad.getValue();
            if (fechaCaducidad != null) {
                stmt.setDate(3, Date.valueOf(fechaCaducidad));  // Convertir LocalDate a Date
            } else {
                stmt.setDate(3, null);  // Si no se seleccionó ninguna fecha, ponemos null
            }
    
            // Convertir la fecha de entrada
            LocalDate fechaEntrada = datePickerFechaEntrada.getValue();
            stmt.setDate(4, Date.valueOf(fechaEntrada));  // Convertir LocalDate a Date
    
            // Obtener el id del estado seleccionado
            stmt.setInt(5, cmbIdState.getValue().getId());
            stmt.setInt(6, selectedLote.getId());  // Usamos el ID del lote seleccionado en la tabla
    
            stmt.executeUpdate();
            loadData();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Lote Actualizado", "El lote fue actualizado correctamente.");
            clearFields();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al actualizar: " + e.getMessage());
        }
    }

    private void deleteLote() {
        // Obtener el lote seleccionado en la tabla
        Lote selectedLote = tableLote.getSelectionModel().getSelectedItem();
        
        if (selectedLote == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección inválida", "Selecciona un lote para eliminar");
            return;
        }

        // Crear una alerta de confirmación antes de eliminar
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este lote?");
        alert.setContentText("Esta acción no se puede deshacer.");

        // Mostrar el cuadro de confirmación y esperar la respuesta del usuario
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = dbConnection.getConnection()) {
                String sql = "DELETE FROM lote WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, selectedLote.getId());

                stmt.executeUpdate();
                loadData();  // Recargar los datos después de eliminar
            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al eliminar "+  e.getMessage());
            }
        } else {
            // Si el usuario cancela la eliminación, no hacemos nada
        }
    }

    private void clearFields() {
        txtCantidad.clear();
        datePickerFechaCaducidad.setValue(null);  // Limpiar el DatePicker
        datePickerFechaEntrada.setValue(null);  // Limpiar el DatePicker
        cmbIdProducto.getSelectionModel().clearSelection();
        cmbIdState.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void configureSearch() {
        // Crear una lista filtrada basada en la lista original
        filteredLoteList = new FilteredList<>(loteList, b -> true);

        // Agregar un listener al campo de búsqueda
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredLoteList.setPredicate(lote -> {
                // Si el texto de búsqueda está vacío, mostrar todos los lotes
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Filtrar por cualquier columna
                if (String.valueOf(lote.getId()).contains(lowerCaseFilter)) {
                    return true; // Coincide con el ID
                } else if (lote.getProducto() != null && lote.getProducto().getNombre().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Coincide con el nombre del producto
                } else if (lote.getProducto() != null && lote.getProducto().getCodigo().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Coincide con el código del producto
                } else if (String.valueOf(lote.getCantidad()).contains(lowerCaseFilter)) {
                    return true; // Coincide con la cantidad
                } else if (lote.getFechaCaducidad() != null && lote.getFechaCaducidad().toString().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Coincide con la fecha de caducidad
                } else if (lote.getEstadoLote() != null && lote.getEstadoLote().getNombre().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Coincide con el estado
                }

                return false; // No coincide con ningún criterio
            });
        });

        // Envolver la lista filtrada en una lista ordenada y vincularla a la tabla
        SortedList<Lote> sortedLoteList = new SortedList<>(filteredLoteList);
        sortedLoteList.comparatorProperty().bind(tableLote.comparatorProperty());
        tableLote.setItems(sortedLoteList);
    }
}
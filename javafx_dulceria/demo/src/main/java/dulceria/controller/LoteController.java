package dulceria.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import dulceria.DatabaseConnection;
import dulceria.model.Producto;
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
    private TableColumn<Lote, Integer> colId, colIdProducto, colCantidad, colIdState;
    @FXML
    private TableColumn<Lote, Date> colFechaCaducidad;
    @FXML
    private Button btnAdd, btnUpdate, btnDelete, btnClear;

    private ObservableList<Lote> loteList = FXCollections.observableArrayList();
    private ObservableList<Producto> productoList = FXCollections.observableArrayList();  // Lista de productos
    private ObservableList<Estado> stateList = FXCollections.observableArrayList();

    private DatabaseConnection dbConnection;

    public LoteController() {
        dbConnection = new DatabaseConnection();
    }

    public void initialize() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colIdProducto.setCellValueFactory(cellData -> cellData.getValue().idProductoProperty().asObject());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        colFechaCaducidad.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<java.sql.Date>(
                new java.sql.Date(cellData.getValue().getFechaCaducidad().getTime())
            )
        );
        colIdState.setCellValueFactory(cellData -> cellData.getValue().idStateProperty().asObject());

        loadComboBoxData();
        loadData();

        btnAdd.setOnAction(event -> addLote());
        btnUpdate.setOnAction(event -> updateLote());
        btnDelete.setOnAction(event -> deleteLote());
        btnClear.setOnAction(event -> clearFields());

        // Listener para la selección de un lote en la tabla
        tableLote.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadSelectedLoteData(newValue); // Cargar los datos del lote seleccionado
        });
    }

    private void loadSelectedLoteData(Lote lote) {
        if (lote != null) {
            // Rellenar los campos con los datos del lote seleccionado
            txtCantidad.setText(String.valueOf(lote.getCantidad()));
            datePickerFechaEntrada.setValue(new java.sql.Date(lote.getFechaEntrada().getTime()).toLocalDate()); 
            datePickerFechaCaducidad.setValue(new java.sql.Date(lote.getFechaCaducidad().getTime()).toLocalDate());
            cmbIdProducto.setValue(productoList.stream().filter(p -> p.getId() == lote.getIdProducto()).findFirst().orElse(null));
            cmbIdState.setValue(stateList.stream().filter(s -> s.getId() == lote.getIdState()).findFirst().orElse(null));
        }
    }

    private void loadComboBoxData() {
        try (Connection conn = dbConnection.getConnection()) {
            // Cargar productos desde la base de datos
            String sqlProducto = "SELECT id, nombre FROM producto";
            Statement stmt = conn.createStatement();
            ResultSet rsProducto = stmt.executeQuery(sqlProducto);
            while (rsProducto.next()) {
                // Crear productos y agregarlos a la lista
                Producto producto = new Producto(rsProducto.getInt("id"), rsProducto.getString("nombre"), "", "", 0, 0);
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
            String sql = "SELECT * FROM lote";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            loteList.clear();
            while (rs.next()) {
                loteList.add(new Lote(
                        rs.getInt("id"),
                        rs.getInt("id_producto"),
                        rs.getInt("cantidad"),
                        rs.getDate("fecha_caducidad"),
                        rs.getDate("fecha_entrada"),
                        rs.getInt("id_state")
                ));
            }
            tableLote.setItems(loteList);
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar datos "+ e.getMessage());
        }
    }

    private void addLote() {
        try (Connection conn = dbConnection.getConnection()) {
            String sql = "INSERT INTO lote (id_producto, cantidad, fecha_caducidad, fecha_entrada, id_state) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cmbIdProducto.getValue().getId());  // Obtener el id del producto seleccionado
            stmt.setInt(2, Integer.parseInt(txtCantidad.getText()));

            // Convertimos la fecha seleccionada en DatePicker a LocalDate
            LocalDate fechaCaducidad = datePickerFechaCaducidad.getValue();
            if (fechaCaducidad != null) {
                stmt.setDate(3, Date.valueOf(fechaCaducidad));  // Convertir LocalDate a Date
            } else {
                stmt.setDate(3, null);  // Si no se seleccionó ninguna fecha, ponemos null
            }
            LocalDate fechaEntrada = datePickerFechaEntrada.getValue();
            if (fechaEntrada != null) {
                stmt.setDate(4, Date.valueOf(fechaEntrada));  // Convertir LocalDate a Date
            } else {
                stmt.setDate(4, null);  // Si no se seleccionó ninguna fecha, ponemos null
            }

            stmt.setInt(5, cmbIdState.getValue().getId());  // Obtener el id del estado seleccionado

            stmt.executeUpdate();
            loadData();
            clearFields();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al agregar "+ e.getMessage());
        }
    }

    private void updateLote() {
        try (Connection conn = dbConnection.getConnection()) {
            Lote selectedLote = tableLote.getSelectionModel().getSelectedItem();  // Obtener el lote seleccionado de la tabla
            if (selectedLote == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selección inválida", "Selecciona un lote para actualizar");
                return;
            }

            String sql = "UPDATE lote SET id_producto = ?, cantidad = ?, fecha_caducidad = ?, fecha_entrada = ?, id_state = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cmbIdProducto.getValue().getId());
            stmt.setInt(2, Integer.parseInt(txtCantidad.getText()));

            // Convertimos la fecha seleccionada en DatePicker a LocalDate
            LocalDate fechaCaducidad = datePickerFechaCaducidad.getValue();
            if (fechaCaducidad != null) {
                stmt.setDate(3, Date.valueOf(fechaCaducidad));  // Convertir LocalDate a Date
            } else {
                stmt.setDate(3, null);  // Si no se seleccionó ninguna fecha, ponemos null
            }
            LocalDate fechaEntrada = datePickerFechaEntrada.getValue();
            if (fechaEntrada != null) {
                stmt.setDate(4, Date.valueOf(fechaEntrada));  // Convertir LocalDate a Date
            } else {
                stmt.setDate(4, null);  // Si no se seleccionó ninguna fecha, ponemos null
            }

            stmt.setInt(5, cmbIdState.getValue().getId());
            stmt.setInt(6, selectedLote.getId());  // Usamos el ID del lote seleccionado en la tabla

            stmt.executeUpdate();
            loadData();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Lote Actualizado", "El lote fue actualizado correctamente.");
            clearFields();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al actualizar.");
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
}
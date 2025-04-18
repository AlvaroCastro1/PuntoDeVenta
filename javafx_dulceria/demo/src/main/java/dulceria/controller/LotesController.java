package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.model.Estado;
import dulceria.model.Lote;
import dulceria.model.Producto;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class LotesController {

    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> searchResultsListView;
    @FXML
    private TextField nombreField;
    @FXML
    private TextField codigoField;
    @FXML
    private TableView<Lote> lotesTableView;
    @FXML
    private TableColumn<Lote, Integer> idColumn;
    @FXML
    private TableColumn<Lote, Integer> cantidadColumn;
    @FXML
    private TableColumn<Lote, String> fechaCaducidadColumn;
    @FXML
    private TableColumn<Lote, String> estadoColumn;
    @FXML
    private Label cantidadTotalLabel;

    private ObservableList<Lote> lotes = FXCollections.observableArrayList();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

        // Columna editable para cantidad
        cantidadColumn.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        cantidadColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        cantidadColumn.setOnEditCommit(event -> {
            Lote lote = event.getRowValue();
            lote.setCantidad(event.getNewValue());
        });

        // Columna editable para fecha de caducidad
        fechaCaducidadColumn.setCellValueFactory(cellData -> {
            Date fecha = cellData.getValue().getFechaCaducidad();
            if (fecha != null) {
                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy");
                return new SimpleStringProperty(formatter.format(fecha));
            } else {
                return new SimpleStringProperty("Sin caducidad");
            }
        });
        fechaCaducidadColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fechaCaducidadColumn.setOnEditCommit(event -> {
            Lote lote = event.getRowValue();
            try {
                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy");
                Date nuevaFecha = formatter.parse(event.getNewValue());
                lote.setFechaCaducidad(nuevaFecha);
            } catch (Exception e) {
                showAlert("Error", "Formato de fecha inválido. Use DD/MM/AAAA.", Alert.AlertType.ERROR);
            }
        });

        // Columna editable para estado
        estadoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstadoLote().getNombre()));
        estadoColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Disponible", "Caducado"));
        estadoColumn.setOnEditCommit(event -> {
            Lote lote = event.getRowValue();
            Estado nuevoEstado = new Estado(
                "Disponible".equals(event.getNewValue()) ? 1 : 8, // Asignar el ID según el estado
                event.getNewValue() // Asignar el nombre del estado
            );
            lote.setEstadoLote(nuevoEstado);
        });

        // Enlazar la tabla con la lista de lotes
        lotesTableView.setItems(lotes);
        lotesTableView.setEditable(true); // Hacer la tabla editable

        // Cargar productos desde la base de datos
        cargarProductosDesdeBaseDeDatos();

        // Configurar el buscador con un PauseTransition
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                searchResultsListView.setVisible(false);
                return;
            }

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

            pause.playFromStart();
        });

        // Configurar el evento de selección en el ListView
        searchResultsListView.setOnMouseClicked(event -> onSelectProduct());

        lotesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void cargarProductosDesdeBaseDeDatos() {
        String query = "SELECT id, nombre, codigo FROM producto";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            productos.clear(); // Limpia la lista antes de cargar nuevos datos

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                String codigo = resultSet.getString("codigo");

                Producto producto = new Producto(id, nombre, codigo, null, null, 0, 0);
                productos.add(producto);
            }

            System.out.println("Productos cargados desde la base de datos: " + productos.size());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar los productos desde la base de datos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onSelectProduct() {
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

                // Cargar lotes asociados al producto
                cargarLotesPorProducto(producto.getId());
            }

            // Oculta la lista de resultados
            searchResultsListView.setVisible(false);
        }
    }

    private void cargarLotesPorProducto(int idProducto) {
        String query = "SELECT l.id, l.id_producto, l.cantidad, l.fecha_caducidad, l.fecha_entrada, l.id_state, s.nombre_estado " +
                       "FROM lote l " +
                       "JOIN cState s ON l.id_state = s.id " +
                       "WHERE l.id_producto = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idProducto);
            ResultSet resultSet = statement.executeQuery();

            lotes.clear(); // Limpia la lista de lotes antes de cargar nuevos datos

            while (resultSet.next()) {
                // Manejar valores NULL para fecha_caducidad
                java.sql.Date sqlFechaCaducidad = resultSet.getDate("fecha_caducidad");
                Date fechaCaducidad = (sqlFechaCaducidad != null) ? new Date(sqlFechaCaducidad.getTime()) : null;

                java.sql.Date sqlFechaEntrada = resultSet.getDate("fecha_entrada");
                Date fechaEntrada = (sqlFechaEntrada != null) ? new Date(sqlFechaEntrada.getTime()) : null;

                // Crear el estado del lote
                Estado estado = new Estado(
                    resultSet.getInt("id_state"),
                    resultSet.getString("nombre_estado")
                );

                // Crear el lote y asignar el estado
                Lote lote = new Lote(
                        resultSet.getInt("id"),
                        resultSet.getInt("id_producto"),
                        resultSet.getInt("cantidad"),
                        fechaCaducidad,
                        fechaEntrada,
                        resultSet.getInt("id_state")
                );
                lote.setEstadoLote(estado);
                lotes.add(lote);
            }

            calcularCantidadTotalDisponible();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar los lotes del producto seleccionado.", Alert.AlertType.ERROR);
        }
    }

    private void calcularCantidadTotalDisponible() {
        int totalDisponible = lotes.stream()
                .filter(lote -> {
                    // Verificar si el lote no está caducado
                    Date fechaCaducidad = lote.getFechaCaducidad();
                    return (fechaCaducidad == null || fechaCaducidad.after(new Date())) // No caducado
                            && "Disponible".equals(lote.getEstadoLote().getNombre()); // Estado "Disponible"
                })
                .mapToInt(Lote::getCantidad)
                .sum();

        // Mostrar la sumatoria en la etiqueta
        cantidadTotalLabel.setText(String.valueOf(totalDisponible));
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onGuardar() {
        // Validar que se haya seleccionado un producto
        if (nombreField.getText().isEmpty() || codigoField.getText().isEmpty()) {
            showAlert("Error", "Debe seleccionar un producto antes de guardar los lotes.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            for (Lote lote : lotes) {
                String query = "UPDATE lote SET cantidad = ?, fecha_caducidad = ?, id_state = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, lote.getCantidad());
                    statement.setDate(2, lote.getFechaCaducidad() != null ? new java.sql.Date(lote.getFechaCaducidad().getTime()) : null);
                    statement.setInt(3, lote.getEstadoLote().getId());
                    statement.setInt(4, lote.getId());
                    statement.executeUpdate();
                }
            }
            showAlert("Éxito", "Los cambios se han guardado correctamente.", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron guardar los cambios.", Alert.AlertType.ERROR);
        }
        initialize();
        // Limpiar los campos de texto
        nombreField.clear();
        codigoField.clear();

        // Limpiar la tabla de lotes
        lotes.clear();

        // Restablecer la etiqueta de cantidad total
        cantidadTotalLabel.setText("0");

        // Ocultar la lista de resultados
        searchResultsListView.setVisible(false);

    }

    @FXML
    private void onCancelar() {
        // Limpiar los campos de texto
        nombreField.clear();
        codigoField.clear();

        // Limpiar la tabla de lotes
        lotes.clear();

        // Restablecer la etiqueta de cantidad total
        cantidadTotalLabel.setText("0");

        // Ocultar la lista de resultados
        searchResultsListView.setVisible(false);
        initialize();
    }
}
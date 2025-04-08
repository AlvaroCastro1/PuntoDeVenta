package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.model.Perdida;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PerdidaController {

    @FXML
    private TableView<Perdida> tablePerdidas;

    @FXML
    private TableColumn<Perdida, Integer> colId, colCantidad, colLote;
    @FXML
    private TableColumn<Perdida, String> colProducto;
    @FXML
    private TableColumn<Perdida, Double> colCostoUnitario, colTotal;
    @FXML
    private TableColumn<Perdida, LocalDate> colFecha;
    @FXML
    private TextField txtBusqueda;
    private ObservableList<Perdida> listaPerdidas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colProducto.setCellValueFactory(cellData -> cellData.getValue().nombreProductoProperty());
        colLote.setCellValueFactory(cellData -> cellData.getValue().idLoteProperty().asObject());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        colCostoUnitario.setCellValueFactory(cellData -> cellData.getValue().costoUnitarioProperty().asObject());
        colTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
        // Configurar la columna de fecha
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        // Formatear la fecha para mostrarla correctamente
        colFecha.setCellFactory(column -> new TableCell<Perdida, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "" : date.format(formatter));
            }
        });


        // Cargar las pérdidas desde la base de datos
        cargarPerdidas();
        tablePerdidas.setItems(listaPerdidas);

        // Envolver la lista en un FilteredList
        FilteredList<Perdida> filteredData = new FilteredList<>(listaPerdidas, p -> true);

        // Escuchar cambios en el campo de búsqueda
        txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(perdida -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return perdida.getNombreProducto().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(perdida.getFecha()).contains(lowerCaseFilter) ||
                        String.valueOf(perdida.getIdLote()).contains(lowerCaseFilter) ||
                        String.valueOf(perdida.getCantidad()).contains(lowerCaseFilter) ||
                        String.valueOf(perdida.getCostoUnitario()).contains(lowerCaseFilter) ||
                        String.valueOf(perdida.getTotal()).contains(lowerCaseFilter);
            });
        });

        // Enlazar la lista filtrada con una SortedList
        SortedList<Perdida> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablePerdidas.comparatorProperty());

        // Asignar los datos a la tabla
        tablePerdidas.setItems(sortedData);
        tablePerdidas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    }

    private void cargarPerdidas() {
        String query = "SELECT p.id, pr.nombre AS producto, l.id AS lote, " +
                "p.cantidad, p.costo_unitario, p.cantidad * p.costo_unitario AS total, " +
                "l.fecha_caducidad " +
                "FROM perdida p " +
                "JOIN producto pr ON p.id_producto = pr.id " +
                "JOIN lote l ON p.id_lote = l.id";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/punto_de_venta", "root", "root");
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            List<Perdida> perdidas = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombreProducto = rs.getString("producto");
                int idLote = rs.getInt("lote");
                int cantidad = rs.getInt("cantidad");
                double costoUnitario = rs.getDouble("costo_unitario");
                LocalDate fecha = rs.getDate("fecha_caducidad").toLocalDate();
                Perdida perdida = new Perdida(id, nombreProducto, idLote, cantidad, costoUnitario, fecha);
                perdidas.add(perdida);
            }

            listaPerdidas.setAll(perdidas);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar las pérdidas.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

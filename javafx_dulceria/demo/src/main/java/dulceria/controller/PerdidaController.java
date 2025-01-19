package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.model.Perdida;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
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

    private ObservableList<Perdida> listaPerdidas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colProducto.setCellValueFactory(cellData -> cellData.getValue().nombreProductoProperty());
        colLote.setCellValueFactory(cellData -> cellData.getValue().idLoteProperty().asObject());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        colCostoUnitario.setCellValueFactory(cellData -> cellData.getValue().costoUnitarioProperty().asObject());
        colTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());

        // Cargar las pérdidas desde la base de datos
        cargarPerdidas();
        tablePerdidas.setItems(listaPerdidas);
    }

    private void cargarPerdidas() {
        String query = "SELECT p.id, pr.nombre AS producto, l.id AS lote, " +
                       "p.cantidad, p.costo_unitario, p.cantidad * p.costo_unitario AS total " +
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
                Perdida perdida = new Perdida(id, nombreProducto, idLote, cantidad, costoUnitario);
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

package dulceria.controller;

import dulceria.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dulceria.model.State;

public class CStateCRUDController {

    @FXML
    private TableView<State> stateTable;

    @FXML
    private TableColumn<State, Integer> idColumn;

    @FXML
    private TableColumn<State, String> nombreEstadoColumn;

    @FXML
    private TextField nombreEstadoField;

    private ObservableList<State> stateList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreEstadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEstado"));
        cargarEstados();
    }

    private void cargarEstados() {
        stateList.clear();
        String sql = "SELECT id, nombre_estado FROM cState";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                stateList.add(new State(resultSet.getInt("id"), resultSet.getString("nombre_estado")));
            }
            stateTable.setItems(stateList);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los estados: " + e.getMessage());
        }
    }

    @FXML
    private void guardarEstado() {
        String nombreEstado = nombreEstadoField.getText().trim();
        if (nombreEstado.isEmpty()) {
            mostrarAlerta("Error", "El nombre del estado no puede estar vacío.");
            return;
        }

        String sql = "INSERT INTO cState (nombre_estado) VALUES (?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nombreEstado);
            statement.executeUpdate();
            mostrarAlerta("Éxito", "El estado se ha guardado correctamente.");
            nombreEstadoField.clear();
            cargarEstados();
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo guardar el estado: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarEstado() {
        State selectedState = stateTable.getSelectionModel().getSelectedItem();
        if (selectedState == null) {
            mostrarAlerta("Error", "Debes seleccionar un estado para actualizar.");
            return;
        }

        String nuevoNombre = nombreEstadoField.getText().trim();
        if (nuevoNombre.isEmpty()) {
            mostrarAlerta("Error", "El nombre del estado no puede estar vacío.");
            return;
        }

        String sql = "UPDATE cState SET nombre_estado = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nuevoNombre);
            statement.setInt(2, selectedState.getId());
            statement.executeUpdate();
            mostrarAlerta("Éxito", "El estado se ha actualizado correctamente.");
            nombreEstadoField.clear();
            cargarEstados();
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo actualizar el estado: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarEstado() {
        State selectedState = stateTable.getSelectionModel().getSelectedItem();
        if (selectedState == null) {
            mostrarAlerta("Error", "Debes seleccionar un estado para eliminar.");
            return;
        }

        String sql = "DELETE FROM cState WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, selectedState.getId());
            statement.executeUpdate();
            mostrarAlerta("Éxito", "El estado se ha eliminado correctamente.");
            cargarEstados();
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo eliminar el estado: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

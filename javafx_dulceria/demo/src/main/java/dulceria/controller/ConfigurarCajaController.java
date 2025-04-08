package dulceria.controller;

import dulceria.app.App;
import dulceria.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ConfigurarCajaController {

    @FXML
    private TextField baseInicialField;

    public void guardarBaseCaja() {
        String input = baseInicialField.getText();

        // Validar que el campo no esté vacío
        if (input == null || input.trim().isEmpty()) {
            showAlert("Error", "El campo de la base inicial no puede estar vacío.", Alert.AlertType.ERROR);
            return;
        }

        // Validar que el valor ingresado sea un número flotante
        double baseInicial;
        try {
            baseInicial = Double.parseDouble(input);
            if (baseInicial < 0) {
                showAlert("Error", "La base inicial no puede ser un valor negativo.", Alert.AlertType.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Ingrese un valor numérico válido para la base inicial.", Alert.AlertType.ERROR);
            return;
        }

        String query = "INSERT INTO caja (fecha, base_inicial, estado, id_usuario) VALUES (CURDATE(), ?, 'Abierta', ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Obtener el ID del usuario autenticado
            int idUsuario = App.getUsuarioAutenticado().getId();

            // Configurar los parámetros de la consulta
            statement.setDouble(1, baseInicial);
            statement.setInt(2, idUsuario);

            // Ejecutar la consulta
            statement.executeUpdate();

            // Mostrar mensaje de éxito
            showAlert("Éxito", "La base de la caja se configuró correctamente.", Alert.AlertType.INFORMATION);

            // Cambiar a la vista principal
            App app = new App();
            app.showMainView(App.getPrimaryStage());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Ocurrió un error al guardar la base de la caja.", Alert.AlertType.ERROR);
        } finally {
            DatabaseConnection.closeConnection(); // Asegurarse de cerrar la conexión
        }
    }

    private boolean verificarBaseCaja() {
        String query = "SELECT COUNT(*) FROM caja WHERE estado = 'Abierta'";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                if (count > 0) {
                    showAlert("Información", "Número de cajas abiertas: " + count, Alert.AlertType.INFORMATION);
                    return true; // Si hay una caja abierta, retorna true
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Ocurrió un error al verificar la base de la caja.", Alert.AlertType.ERROR);
        } finally {
            DatabaseConnection.closeConnection(); // Asegurarse de cerrar la conexión
        }
        showAlert("Advertencia", "No hay cajas abiertas.", Alert.AlertType.WARNING);
        return false; // Retorna false si ocurre un error o no hay cajas abiertas
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
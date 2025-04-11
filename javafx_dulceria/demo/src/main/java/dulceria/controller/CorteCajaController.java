package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.app.App;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class CorteCajaController {

    @FXML
    private Label lblTotalIngresos;

    @FXML
    private Label lblTotalEgresos;

    @FXML
    private Label lblTotalVentas;

    @FXML
    private Label lblTotalFinal;

    private int idCaja;

    @FXML
    public void initialize() {
        cargarDatosCaja();
    }

    private void cargarDatosCaja() {
        String query = "SELECT id, total_ingresos, total_egresos, total_ventas, total_final FROM caja WHERE estado = 'Abierta' LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                idCaja = resultSet.getInt("id");
                lblTotalIngresos.setText(String.format("$%.2f", resultSet.getDouble("total_ingresos")));
                lblTotalEgresos.setText(String.format("$%.2f", resultSet.getDouble("total_egresos")));
                lblTotalVentas.setText(String.format("$%.2f", resultSet.getDouble("total_ventas")));
                lblTotalFinal.setText(String.format("$%.2f", resultSet.getDouble("total_final")));
            } else {
                mostrarAlerta("Error", "No hay una caja abierta.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar los datos de la caja: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void realizarCorteCaja() {
        if (!confirmarCorteCaja()) {
            return;
        }

        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            // 1. Actualizar el estado de la caja a 'Cerrada'
            String updateCaja = "UPDATE caja SET estado = 'Cerrada', updated_at = NOW() WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateCaja)) {
                stmt.setInt(1, idCaja);
                stmt.executeUpdate();
            }

            // 2. Registrar el movimiento de corte de caja
            String insertMovimiento = "INSERT INTO movimientos_caja (id_caja, tipo, descripcion, monto, id_usuario, created_at) " +
                                    "VALUES (?, 'Corte de Caja', 'Cierre de caja', ?, ?, NOW())";
            try (PreparedStatement stmt = connection.prepareStatement(insertMovimiento)) {
                double totalFinal = Double.parseDouble(lblTotalFinal.getText().replace("$", ""));
                stmt.setInt(1, idCaja);
                stmt.setDouble(2, totalFinal);
                stmt.setInt(3, App.getUsuarioAutenticado().getId());
                stmt.executeUpdate();
            }

            connection.commit();
            mostrarAlerta("Éxito", "Corte de caja realizado correctamente.", Alert.AlertType.INFORMATION);
            
            // Redirigir al usuario a la vista principal
            App app = new App();
            app.showMainView(App.getPrimaryStage());

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            mostrarAlerta("Error", "Error al realizar el corte de caja: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean confirmarCorteCaja() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Corte de Caja");
        alert.setHeaderText("¿Está seguro de realizar el corte de caja?");
        alert.setContentText("Esta acción cerrará la caja actual y no se podrá realizar más ventas hasta abrir una nueva caja.");
        return alert.showAndWait().filter(response -> response == javafx.scene.control.ButtonType.OK).isPresent();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

package dulceria.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class PantallaConfiguracionController {

    @FXML
    private void handleIniciarVenta() {
        showAlert("Iniciar Venta", "Funcionalidad en desarrollo.");
    }

    @FXML
    private void handleAdministrarInventario() {
        showAlert("Administrar Inventario", "Funcionalidad en desarrollo.");
    }

    @FXML
    private void handleSalir() {
        System.exit(0);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

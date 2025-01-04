package dulceria.controller;

import dulceria.app.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class SidebarController {

    private App app; // Referencia a la instancia de App

    // Método para establecer la referencia de App
    public void setApp(App app) {
        this.app = app;
    }

    // Métodos de navegación
    @FXML
    private void navigateToHome(ActionEvent event) {
        System.out.println("Navegando a Pantalla de inicio");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/pantallaInicio.fxml");
    }

    @FXML
    private void navigateToConfiguracion(ActionEvent event) {
        System.out.println("Navegando a Pantalla Configuración");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/pantallaConfiguracion.fxml");
    }

    @FXML
    private void navigateToEstados(ActionEvent event) {
        System.out.println("Navegando a Pantalla de Estados");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/cStateCRUD.fxml");
    }

    @FXML
    private void navigateToScreen3(ActionEvent event) {
        System.out.println("Navegando a Pantalla 3");
        // Lógica de navegación
    }
}

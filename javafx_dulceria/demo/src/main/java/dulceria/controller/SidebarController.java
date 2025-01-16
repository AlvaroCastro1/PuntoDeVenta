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
    private void navigateToProductos(ActionEvent event) {
        System.out.println("Navegando a Pantalla Productos");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/ProductoCRUD.fxml");
    }

    @FXML
    private void navigateToRoles(ActionEvent event) {
        System.out.println("Navegando a Pantalla roles");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/rolCRUD.fxml");
    }

    @FXML
    private void navigateToPermisos(ActionEvent event) {
        System.out.println("Navegando a Pantalla Permisos");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/permisoCRUD.fxml");
    }

    @FXML
    private void navigateToUsuarios(ActionEvent event) {
        System.out.println("Navegando a Pantalla Usuarios");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/usuarioCRUD.fxml");
    }
    @FXML
    private void navigateToCrearUsuario(ActionEvent event) {
        System.out.println("Navegando a Pantalla Crear Usuario");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/crearUsuario.fxml");
    }

    @FXML
    private void navigateToRolUsuario(ActionEvent event) {
        System.out.println("Navegando a Pantalla Rol Usuario");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/rol_usuario.fxml");
    }

    @FXML
    private void navigateToRolPermiso(ActionEvent event) {
        System.out.println("Navegando a Pantalla Rol Permiso");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/rol_permiso.fxml");
    }

    @FXML
    private void navigateToLotes(ActionEvent event) {
        System.out.println("Navegando a Pantalla Lotes");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/loteCRUD.fxml");
    }
}

package dulceria.controller;

import dulceria.app.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;

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
        app.changeView("/dulceria/fxml/dashboard.fxml");
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

    @FXML
    private void navigateToEntradas(ActionEvent event) {
        System.out.println("Navegando a Pantalla Detalle Entradas");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/detalleEntrada.fxml");
    }

    @FXML
    private void navigateToEntrada(ActionEvent event) {
        System.out.println("Navegando a Pantalla Entradas");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/entrada.fxml");
    }

    @FXML
    private void navigateToPerdidas(ActionEvent event) {
        System.out.println("Navegando a Pantalla Entradas");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/perdidas.fxml");
    }

    @FXML
    private void navigateToVenta(ActionEvent event) {
        System.out.println("Navegando a Pantalla Venta");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/venta.fxml");
    }

    @FXML
    private void navigateToVentas(ActionEvent event) {
        System.out.println("Navegando a Pantalla Ventas");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/ventas.fxml");
    }

    @FXML
    private void navigateToPromocion(ActionEvent event) {
        System.out.println("Navegando a Pantalla Promocion");
        // Lógica de navegación
        app.changeView("/dulceria/fxml/promocionesCRUD.fxml");
    }

    @FXML
    public void toggleModoOscuro(ActionEvent event) {
        Button button = (Button) event.getSource();  // Obtiene el botón que disparó el evento
        Scene scene = button.getScene();             // Obtiene la escena desde el botón

        // Alterna entre el modo claro y el modo oscuro en la raíz de la escena
        if (scene.getRoot().getStyleClass().contains("dark-mode")) {
            scene.getRoot().getStyleClass().remove("dark-mode");
        } else {
            scene.getRoot().getStyleClass().add("dark-mode");
        }

        // Asegúrate de que el cambio de modo se aplique a todos los elementos si es necesario
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/dulceria/css/estilos.css").toExternalForm());
    }

}

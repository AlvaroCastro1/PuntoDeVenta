package dulceria.controller;

import dulceria.app.App;
import dulceria.model.Rol;
import dulceria.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public class SidebarController {

    @FXML private Button btnInicio;
    // @FXML private Button btnConfiguracion;
    @FXML private Button btnPromociones;
    // @FXML private Button btnEstados;
    @FXML private Button btnProductos;
    @FXML private Button btnLotes;
    @FXML private Button btnEntradas;
    @FXML private Button btnNuevaEntrada;
    @FXML private Button btnPerdidas;
    @FXML private Button btnNuevaVenta;
    @FXML private Button btnVentas;
    // @FXML private Button btnRoles;
    // @FXML private Button btnPermisos;
    @FXML private Button btnUsuarios;
    @FXML private Button btnCrearUsuario;
    @FXML private Button btnRolUsuario;
    // @FXML private Button btnRolPermiso;
    @FXML private Button btnModoOscuro;

    private App app;
    private Usuario usuario;

    public void setApp(App app) {
        this.app = app;
        usuario = App.getUsuarioAutenticado();
        configurarBotonesSegunRol();
    }

    private void configurarBotonesSegunRol() {
        deshabilitarTodo(); // Deshabilitamos todos los botones por defecto

        for (Rol rol : usuario.getRoles()) {
            switch (rol.getNombreRol()) {
                case "Desarrollador":
                case "Soporte Técnico":
                    habilitarBotonesCompletos();
                    break;
                case "Administrador":
                    habilitarAdministrador();
                    break;
                case "Vendedor":
                    habilitarVendedor();
                    break;
                default:
                    // ya están deshabilitados por defecto
                    break;
            }
        }
    }

    private void habilitarAdministrador() {
        habilitarBotonesCompletos();
        btnRolUsuario.setDisable(true);
        // btnRolPermiso.setDisable(true);
        // btnRoles.setDisable(true);
        // btnPermisos.setDisable(true);
    }

    private void habilitarVendedor() {
        btnInicio.setDisable(false);
        // btnConfiguracion.setDisable(true);
        btnPromociones.setDisable(false);
        // btnEstados.setDisable(true);
        btnProductos.setDisable(false);
        btnLotes.setDisable(false);
        btnEntradas.setDisable(false);
        btnNuevaEntrada.setDisable(false);
        btnPerdidas.setDisable(true);
        btnNuevaVenta.setDisable(false);
        btnVentas.setDisable(true);
        // btnRoles.setDisable(true);
        // btnPermisos.setDisable(true);
        btnUsuarios.setDisable(true);
        btnCrearUsuario.setDisable(true);
        btnRolUsuario.setDisable(true);
        // btnRolPermiso.setDisable(true);
        btnModoOscuro.setDisable(false);
    }

    private void habilitarBotonesCompletos() {
        btnInicio.setDisable(false);
        // btnConfiguracion.setDisable(false);
        btnPromociones.setDisable(false);
        // btnEstados.setDisable(false);
        btnProductos.setDisable(false);
        btnLotes.setDisable(false);
        btnEntradas.setDisable(false);
        btnNuevaEntrada.setDisable(false);
        btnPerdidas.setDisable(false);
        btnNuevaVenta.setDisable(false);
        btnVentas.setDisable(false);
        // btnRoles.setDisable(false);
        // btnPermisos.setDisable(false);
        btnUsuarios.setDisable(false);
        btnCrearUsuario.setDisable(false);
        btnRolUsuario.setDisable(false);
        // btnRolPermiso.setDisable(false);
        btnModoOscuro.setDisable(false);
    }

    private void deshabilitarTodo() {
        // Deshabilitar todos los botones al inicio
        btnInicio.setDisable(true);
        // btnConfiguracion.setDisable(true);
        btnPromociones.setDisable(true);
        // btnEstados.setDisable(true);
        btnProductos.setDisable(true);
        btnLotes.setDisable(true);
        btnEntradas.setDisable(true);
        btnNuevaEntrada.setDisable(true);
        btnPerdidas.setDisable(true);
        btnNuevaVenta.setDisable(true);
        btnVentas.setDisable(true);
        // btnRoles.setDisable(true);
        // btnPermisos.setDisable(true);
        btnUsuarios.setDisable(true);
        btnCrearUsuario.setDisable(true);
        btnRolUsuario.setDisable(true);
        // btnRolPermiso.setDisable(true);
        btnModoOscuro.setDisable(true);
    }

    // Métodos de navegación
    @FXML
    private void navigateToHome(ActionEvent event) {
        System.out.println("Navegando a Pantalla de inicio");
        app.changeView("/dulceria/fxml/dashboard.fxml");
    }

    @FXML
    private void navigateToConfiguracion(ActionEvent event) {
        System.out.println("Navegando a Pantalla Configuración");
        app.changeView("/dulceria/fxml/pantallaConfiguracion.fxml");
    }

    @FXML
    private void navigateToEstados(ActionEvent event) {
        if (tienePermiso("Administrador")) {
            System.out.println("Navegando a Pantalla de Estados");
            app.changeView("/dulceria/fxml/cStateCRUD.fxml");
        } else {
            mostrarAlerta("Error", "No tienes permisos para acceder a esta pantalla.", AlertType.ERROR);
        }
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

    private boolean tienePermiso(String rolNombre) {
        for (Rol rol : usuario.getRoles()) {
            if (rol.getNombreRol().equals(rolNombre)) {
                return true;
            }
        }
        return false;
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

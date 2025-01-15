package dulceria.app;

import dulceria.controller.LoginController;
import dulceria.controller.SidebarController;
import dulceria.model.Usuario;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Duration;


public class App extends Application {

    private boolean isSidebarVisible = false; // Estado inicial del Sidebar
    private TranslateTransition slideAnimation; // Animación de barrido
    private BorderPane root; // Declaración de root como campo de clase


    private static Usuario usuarioAutenticado;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el archivo FXML para la pantalla de login
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/dulceria/fxml/login.fxml"));
        Parent loginView = loginLoader.load();
        
        // Configurar la escena del login
        Scene loginScene = new Scene(loginView, 400, 300);
        primaryStage.setTitle("Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
        
        // Suponemos que el controlador de login tiene una forma de validar el login
        LoginController loginController = loginLoader.getController();
        loginController.setApp(this, primaryStage); // Pasamos la instancia de App y el Stage
    }

    // Método para cambiar a la vista principal con Sidebar
    public void showMainView(Stage primaryStage) {
        // Cargar el archivo FXML para el Sidebar
        try {
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/dulceria/fxml/sidebar.fxml"));
            Parent sidebar = sidebarLoader.load(); // Cargar el sidebar

            // Obtener el controlador del Sidebar
            SidebarController sidebarController = sidebarLoader.getController();
            sidebarController.setApp(this); // Pasar la instancia de App

            // Configurar el contenedor principal
            root = new BorderPane(); // Asegurarte de usar el campo de clase root
            root.setLeft(sidebar);
            root.setCenter(createView("Pantalla de inicio"));

            // Configurar la animación del Sidebar
            slideAnimation = new TranslateTransition(Duration.millis(300), sidebar);

            // Configurar los eventos del mouse para mostrar/ocultar el Sidebar
            root.setOnMouseMoved(event -> {
                if (event.getX() < 10 && !isSidebarVisible) {
                    showSidebar(sidebar); // Muestra el sidebar cuando el mouse está cerca del borde izquierdo
                } else if (event.getX() > 200 && isSidebarVisible) {
                    hideSidebar(sidebar); // Oculta el sidebar cuando el mouse está en el área de contenido
                }
            });

            // Cambiar a la vista principal
            Scene mainScene = new Scene(root, 800, 600);
            mainScene.getStylesheets().add(getClass().getResource("/dulceria/css/styles.css").toExternalForm()); // Aplicar CSS
            primaryStage.setTitle("Sidebar con animación");
            primaryStage.setScene(mainScene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            root.setCenter(view); // Reemplazar el contenido central
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("No se pudo cargar la vista: " + fxmlPath);
        }
    }

    // Mostrar el Sidebar con animación
    private void showSidebar(Parent sidebar) {
        slideAnimation.setToX(0); // Mover el Sidebar a su posición visible
        slideAnimation.play();
        isSidebarVisible = true;
    }

    // Ocultar el Sidebar con animación
    private void hideSidebar(Parent sidebar) {
        slideAnimation.setToX(-200); // Mover el Sidebar fuera de la pantalla (ocultarlo)
        slideAnimation.play();
        isSidebarVisible = false;
    }

    // Crear la vista para una pantalla específica
    private StackPane createView(String screenName) {
        StackPane view = new StackPane();
        view.setStyle("-fx-background-color: #F4F4F4; -fx-border-color: #CCCCCC; -fx-border-width: 1;");
        view.getChildren().add(new javafx.scene.control.Button(screenName)); // Contenido de prueba
        return view;
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Getter y Setter para acceder al usuario autenticado
    public static Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public static void setUsuarioAutenticado(Usuario usuario) {
        usuarioAutenticado = usuario;
    }
}

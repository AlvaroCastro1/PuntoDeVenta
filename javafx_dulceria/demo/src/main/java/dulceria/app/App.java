package dulceria.app;

import dulceria.controller.SidebarController;
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


    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el archivo FXML para el Sidebar
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

        // Configurar la escena y el escenario
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/dulceria/css/styles.css").toExternalForm()); // Aplicar CSS
        primaryStage.setTitle("Sidebar con animación");
        primaryStage.setScene(scene);
        primaryStage.show();
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
}

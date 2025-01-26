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
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class App extends Application {

    private boolean isSidebarVisible = false; // Estado inicial del Sidebar
    private TranslateTransition slideAnimation; // Animación de barrido
    private BorderPane root; // Declaración de root como campo de clase


    private static Usuario usuarioAutenticado;

    private boolean modoOscuro = true; // Variable para guardar el estado del tema

    public boolean isModoOscuro() {
        return modoOscuro;
    }

    public void setModoOscuro(boolean modoOscuro) {
        this.modoOscuro = modoOscuro;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el archivo FXML para la pantalla de login
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/dulceria/fxml/login.fxml"));
        Parent loginView = loginLoader.load();

        // Configurar la escena del login
        Scene loginScene = new Scene(loginView, 700, 300);
    
        // Verificar y aplicar el tema oscuro si está habilitado
        if (isModoOscuro()) { // Suponiendo que tienes un método isModoOscuro() en la clase App
            loginScene.getRoot().getStyleClass().add("dark-mode"); // Añadir la clase CSS para el tema oscuro
        }
    
        // Añadir la hoja de estilos principal
        loginScene.getStylesheets().add(getClass().getResource("/dulceria/css/login.css").toExternalForm());
    
        // Configurar el título y mostrar la ventana
        primaryStage.setTitle("Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
        
        // Suponemos que el controlador de login tiene una forma de validar el login
        LoginController loginController = loginLoader.getController();
        loginController.setApp(this, primaryStage); // Pasamos la instancia de App y el Stage
    }

    // Método para cambiar a la vista principal con Sidebar
    public void showMainView(Stage primaryStage) {
        try {
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/dulceria/fxml/sidebar.fxml"));
            Parent sidebar = sidebarLoader.load();
    
            SidebarController sidebarController = sidebarLoader.getController();
            sidebarController.setApp(this);
    
            root = new BorderPane();
            root.setLeft(sidebar);
            root.setCenter(createView("Pantalla de inicio"));
    
            slideAnimation = new TranslateTransition(Duration.millis(300), sidebar);
    
            root.setOnMouseMoved(event -> {
                if (event.getX() < 10 && !isSidebarVisible) {
                    showSidebar(sidebar);
                } else if (event.getX() > 200 && isSidebarVisible) {
                    hideSidebar(sidebar);
                }
            });
    
            Scene mainScene = new Scene(root, 800, 600);
    
            // Verificar el estado del tema y aplicar el CSS correspondiente
            if (isModoOscuro()) {
                mainScene.getRoot().getStyleClass().add("dark-mode");
            }
    
            mainScene.getStylesheets().add(getClass().getResource("/dulceria/css/styles.css").toExternalForm());
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

    // Mostrar el Sidebar con animación suave
    private void showSidebar(Parent sidebar) {
        if (!root.getChildren().contains(sidebar)) {
            root.setLeft(sidebar); // Añadir el Sidebar nuevamente al layout
        }
        
        slideAnimation.setToX(0); // Mover el Sidebar a su posición visible
        slideAnimation.play();
    
        // Animación para expandir el espacio del Sidebar
        Timeline expandAnimation = new Timeline(
            new KeyFrame(Duration.millis(300), 
                new KeyValue(((Region) sidebar).prefWidthProperty(), 200)) // Ancho final
        );
        expandAnimation.setOnFinished(event -> isSidebarVisible = true); // Actualizar estado
        expandAnimation.play();
    }
    
    // Ocultar el Sidebar con animación suave
    private void hideSidebar(Parent sidebar) {
        slideAnimation.setToX(-200); // Mover el Sidebar fuera de la pantalla (ocultarlo)
        slideAnimation.play();
    
        // Animación para colapsar el espacio del Sidebar
        Timeline collapseAnimation = new Timeline(
            new KeyFrame(Duration.millis(300), 
                new KeyValue(((Region) sidebar).prefWidthProperty(), 0)) // Ancho final
        );
        collapseAnimation.setOnFinished(event -> {
            root.setLeft(null); // Remover el Sidebar del diseño después de la animación
            isSidebarVisible = false; // Actualizar estado
        });
        collapseAnimation.play();
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

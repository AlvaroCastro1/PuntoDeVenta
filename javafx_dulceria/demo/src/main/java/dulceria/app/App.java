package dulceria.app;

import dulceria.controller.LoginController;
import dulceria.controller.SidebarController;
import dulceria.model.Usuario;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class App extends Application {

    private boolean isSidebarVisible = false; // Estado inicial del Sidebar
    private TranslateTransition slideAnimation; // Animación de barrido
    private BorderPane root; // Declaración de root como campo de clase
    private boolean modoOscuro = true; // Variable para guardar el estado del tema

    private static Usuario usuarioAutenticado;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicializar root
        root = new BorderPane();

        // Cargar el archivo FXML para la pantalla de login
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/dulceria/fxml/login.fxml"));
        Parent loginView = loginLoader.load();

        // Configurar la escena del login
        Scene loginScene = new Scene(loginView, 700, 300);

        // Aplicar el tema según el valor de modoOscuro
        aplicarTema(loginScene);

        // Añadir la hoja de estilos principal
        loginScene.getStylesheets().add(getClass().getResource("/dulceria/css/estilos.css").toExternalForm());

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
            // Mostrar el indicador de carga
            showLoadingIndicator();
    
            // Cargar el Sidebar
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/dulceria/fxml/sidebar.fxml"));
            Parent sidebar = sidebarLoader.load();
    
            SidebarController sidebarController = sidebarLoader.getController();
            sidebarController.setApp(this);
    
            root.setLeft(sidebar); // Añadir el Sidebar a la izquierda
    
            // Cargar el archivo FXML específico (por ejemplo, dashboard.fxml)
            FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/dulceria/fxml/dashboard.fxml"));
            Parent dashboardView = dashboardLoader.load();
    
            // Establecer el archivo FXML cargado en el centro del BorderPane
            root.setCenter(dashboardView);
    
            // Configurar la animación del Sidebar
            slideAnimation = new TranslateTransition(Duration.millis(300), sidebar);
    
            // Configurar eventos para mostrar/ocultar el Sidebar
            root.setOnMouseMoved(event -> {
                if (event.getX() < 10 && !isSidebarVisible) {
                    showSidebar(sidebar);
                } else if (event.getX() > 200 && isSidebarVisible) {
                    hideSidebar(sidebar);
                }
            });
    
            // Crear la escena principal
            Scene mainScene = new Scene(root, 800, 600);
    
            // Aplicar el tema según el valor de modoOscuro
            aplicarTema(mainScene);
    
            // Añadir la hoja de estilos principal
            mainScene.getStylesheets().add(getClass().getResource("/dulceria/css/estilos.css").toExternalForm());
    
            // Agregar una transición de desvanecimiento antes de cambiar la escena
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                primaryStage.setTitle("Vista Principal");
                primaryStage.setScene(mainScene);
                fadeIn(mainScene);
            });
    
            fadeOut.play();
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Función para hacer un fade-in en la escena
    private void fadeIn(Scene scene) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), scene.getRoot());
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public void changeView(String fxmlPath) {
        try {
            // Mostrar el indicador de carga
            showLoadingIndicator();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Aplicar una transición de desvanecimiento suave
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root.getCenter());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                root.setCenter(view);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root.getCenter());
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("No se pudo cargar la vista: " + fxmlPath);
        }
    }

    // Método para mostrar el indicador de carga
    private void showLoadingIndicator() {
        if (root == null) {
            root = new BorderPane(); // Asegurarse de que root esté inicializado si aún no lo está
        }

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.getStyleClass().add("loading-indicator"); // Aplicar clase CSS

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.getStyleClass().add("loading-pane"); // Aplicar clase CSS
        root.setCenter(loadingPane);

        // Simular una carga más larga con una pausa
        PauseTransition pause = new PauseTransition(Duration.seconds(5)); // Duración de 5 segundos
        pause.play();
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

    // Aplicar el tema según el valor de modoOscuro
    private void aplicarTema(Scene scene) {
        if (isModoOscuro()) {
            scene.getRoot().getStyleClass().add("dark-mode");
        } else {
            scene.getRoot().getStyleClass().add("light-mode");
        }
    
        // Añadir la hoja de estilos principal
        scene.getStylesheets().add(getClass().getResource("/dulceria/css/estilos.css").toExternalForm());
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

    public boolean isModoOscuro() {
        return modoOscuro;
    }

    public void setModoOscuro(boolean modoOscuro) {
        this.modoOscuro = modoOscuro;
    }
}

package dulceria;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public class DatabaseConnection {
    private static String url;
    private static String user;
    private static String password;
    private static Connection connection = null;
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String CONFIG_FILE_PATH;

    static {
        CONFIG_FILE_PATH = determineConfigFilePath();
        checkAndCreateConfigFile();
        loadProperties();
    }

    private static String determineConfigFilePath() {
        String os = System.getProperty("os.name").toLowerCase();
        String configDir;

        if (os.contains("win")) {
            // En Windows, usar C:\ProgramData\DulceriTeddy
            configDir = System.getenv("ProgramData") + File.separator + "DulceriaTeddy";
        } else {
            // En Linux/Unix, usar /etc/DulceriTeddy
            configDir = "/etc/DulceriTeddy";
        }

        File dir = new File(configDir);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Directorio de configuración creado en: " + configDir);
                // showAlert(AlertType.INFORMATION, "Información", "Directorio de configuración creado en: " + configDir);
            } else {
                showAlert(AlertType.ERROR, "Error", "Error al crear el directorio de configuración en: " + configDir);
            }
        }

        return configDir + File.separator + CONFIG_FILE_NAME;
    }

    private static void checkAndCreateConfigFile() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            // showAlert(AlertType.INFORMATION, "Información", "El archivo config.properties no existe. Se procederá a crearlo.");
            try (FileOutputStream fos = new FileOutputStream(configFile)) {

                Properties properties = new Properties();

                String dbHost = showInputDialog("Ingrese el host de la base de datos (por defecto localhost):", "localhost");
                String dbPort = showInputDialog("Ingrese el puerto de la base de datos (por defecto 3306):", "3306");
                String dbName = showInputDialog("Ingrese el nombre de la base de datos (por defecto punto_de_venta):", "punto_de_venta");
                String dbUser = showInputDialog("Ingrese el usuario de la base de datos (por defecto root):", "root");
                String dbPassword = showInputDialog("Ingrese la contraseña de la base de datos (por defecto root):", "root");

                // Construir la URL de conexión
                String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;

                properties.setProperty("db.url", dbUrl);
                properties.setProperty("db.user", dbUser);
                properties.setProperty("db.password", dbPassword);

                properties.store(fos, "Archivo de configuración de la base de datos");
                showAlert(AlertType.INFORMATION, "Información", "El archivo de configuracion fue creado exitosamente en: " + CONFIG_FILE_PATH);

            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Error", "Error al crear el archivo config.properties: " + e.getMessage());
            }
        }
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            props.load(input);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Error al cargar config.properties: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            return connection;
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Error al conectar a la base de datos: " + e.getMessage());
            return null;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                // showAlert(AlertType.INFORMATION, "Información", "Conexión cerrada.");
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Error", "Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    private static void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static String showInputDialog(String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle("Configuración de la base de datos");
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        Optional<String> result = dialog.showAndWait();
        return result.orElse(defaultValue).trim();
    }
}

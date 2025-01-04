module dulceria {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;    // Requiere el controlador JDBC para MySQL


    opens dulceria.controller to javafx.fxml;  // Abierto para los controladores
    exports dulceria.app;  // Exporta el paquete donde está tu clase App
    exports dulceria.model;
}

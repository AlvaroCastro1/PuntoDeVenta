module dulceria {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;    // Requiere el controlador JDBC para MySQL
    requires spring.security.crypto;  // Este es un ejemplo; asegúrate de que este módulo esté disponible
    requires org.apache.pdfbox;



    opens dulceria.controller to javafx.fxml;  // Abierto para los controladores
    exports dulceria.app;  // Exporta el paquete donde está tu clase App
    exports dulceria.model;
}

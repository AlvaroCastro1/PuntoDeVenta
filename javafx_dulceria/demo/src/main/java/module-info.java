module dulceria {
    requires javafx.controls;
    requires javafx.fxml;

    opens dulceria.controller to javafx.fxml;  // Abierto para los controladores
    exports dulceria.app;  // Exporta el paquete donde está tu clase App
}

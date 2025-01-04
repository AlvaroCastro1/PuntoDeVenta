package dulceria;

import java.sql.Connection;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        // Intentamos obtener una conexión
        Connection connection = DatabaseConnection.getConnection();

        // Verificamos si la conexión es válida
        if (connection != null) {
            System.out.println("La conexión a la base de datos fue exitosa.");
        } else {
            System.err.println("La conexión a la base de datos falló.");
        }

        // Cerramos la conexión
        DatabaseConnection.closeConnection();
    }
}

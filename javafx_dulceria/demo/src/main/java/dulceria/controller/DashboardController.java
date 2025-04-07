package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.app.App;
import dulceria.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class DashboardController {

    @FXML
    private BarChart<String, Number> ventasPorMesChart;

    @FXML
    private BarChart<String, Number> productoMasVendidoChart;

    @FXML
    private PieChart gananciasVsPerdidasChart;

    @FXML
    private BarChart<String, Number> productoMasExistenciasChart;

    @FXML
    private BarChart<String, Number> productoPocasExistenciasChart;

    @FXML
    private Label usuarioLogueadoLabel;

    @FXML
    private DatePicker fechaInicioPicker;

    @FXML
    private DatePicker fechaFinPicker;

    Usuario user;

    @FXML
    public void initialize() {
        user = App.getUsuarioAutenticado();
        cargarDatosVentasPorMes();
        cargarDatosProductoMasVendido();
        cargarDatosGananciasVsPerdidas();
        cargarDatosProductoMasExistencias();
        cargarDatosProductoPocasExistencias();
        cargarUsuarioLogueado();
    }

    public void cargarDatosVentasPorMes() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas Mensuales");

        String query = "SELECT MONTH(fecha) AS mes, SUM(total) AS total_ventas FROM venta WHERE fecha >= CURDATE() - INTERVAL 3 MONTH GROUP BY mes ORDER BY mes";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int mesNumero = rs.getInt("mes");
                double totalVentas = rs.getDouble("total_ventas");
                
                String nombreMes = Month.of(mesNumero).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                series.getData().add(new XYChart.Data<>(nombreMes, totalVentas));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo de errores
        }

        ventasPorMesChart.getData().add(series);
    }

    private void cargarDatosVentasPorMes(LocalDate fechaInicio, LocalDate fechaFin) {
        ventasPorMesChart.getData().clear(); // Limpiar datos anteriores

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas Mensuales");

        String query = "SELECT MONTH(fecha) AS mes, SUM(total) AS total_ventas " +
                       "FROM venta " +
                       "WHERE fecha BETWEEN ? AND ? " +
                       "GROUP BY mes " +
                       "ORDER BY mes";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, java.sql.Date.valueOf(fechaInicio));
            stmt.setDate(2, java.sql.Date.valueOf(fechaFin));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int mesNumero = rs.getInt("mes");
                double totalVentas = rs.getDouble("total_ventas");

                String nombreMes = Month.of(mesNumero).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                series.getData().add(new XYChart.Data<>(nombreMes, totalVentas));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo de errores
        }

        ventasPorMesChart.getData().add(series);
    }

    private void cargarDatosProductoMasVendido() {
        // Consulta SQL para obtener los productos más vendidos en los últimos 3 meses, excluyendo los caducados
        String query = "SELECT p.nombre, SUM(l.cantidad) AS total_vendido " +
                       "FROM lote l " +
                       "JOIN producto p ON l.id_producto = p.id " +
                       "WHERE l.id_state = 1 " +  // Solo lotes activos
                       "AND l.fecha_caducidad >= CURDATE() " +  // Solo los lotes no caducados
                       "GROUP BY p.id " +
                       "ORDER BY total_vendido DESC " +
                       "LIMIT 5";  // Solo los 5 productos más vendidos
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            // Limpiamos los datos anteriores en el gráfico
            productoMasVendidoChart.getData().clear();
    
            // Creamos una nueva serie de datos para el gráfico de barras
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Productos Más Vendidos");
    
            // Iteramos sobre los resultados obtenidos y los agregamos a la serie
            while (rs.next()) {
                String nombreProducto = rs.getString("nombre");
                int totalVendido = rs.getInt("total_vendido");
    
                // Añadimos cada producto y su cantidad al gráfico de barras
                series.getData().add(new XYChart.Data<>(nombreProducto, totalVendido));
            }
    
            // Añadimos la serie al gráfico
            productoMasVendidoChart.getData().add(series);
    
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo de errores
        }
    }

    private void cargarDatosProductoMasVendido(LocalDate fechaInicio, LocalDate fechaFin) {
        productoMasVendidoChart.getData().clear(); // Limpiar datos anteriores

        String query = "SELECT p.nombre, SUM(l.cantidad) AS total_vendido " +
                       "FROM lote l " +
                       "JOIN producto p ON l.id_producto = p.id " +
                       "WHERE l.fecha_caducidad >= CURDATE() " +
                       "AND l.fecha_caducidad BETWEEN ? AND ? " +
                       "GROUP BY p.id " +
                       "ORDER BY total_vendido DESC " +
                       "LIMIT 5";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, java.sql.Date.valueOf(fechaInicio));
            stmt.setDate(2, java.sql.Date.valueOf(fechaFin));

            ResultSet rs = stmt.executeQuery();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Productos Más Vendidos");

            while (rs.next()) {
                String nombreProducto = rs.getString("nombre");
                int totalVendido = rs.getInt("total_vendido");
                series.getData().add(new XYChart.Data<>(nombreProducto, totalVendido));
            }

            productoMasVendidoChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo de errores
        }
    }
    
    private void cargarDatosGananciasVsPerdidas() {
        // Consultas SQL para obtener las ganancias y las pérdidas totales en los últimos 3 meses
        String queryGanancias = "SELECT SUM(v.total) AS total_ganancias " +
                             "FROM venta v " +
                             "WHERE MONTH(v.created_at) = MONTH(CURDATE()) " +
                             "AND YEAR(v.created_at) = YEAR(CURDATE())";
    String queryPerdidas = "SELECT SUM(p.total) AS total_perdidas " +
                           "FROM perdida p " +
                           "WHERE MONTH(p.created_at) = MONTH(CURDATE()) " +
                           "AND YEAR(p.created_at) = YEAR(CURDATE())";
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtGanancias = conn.prepareStatement(queryGanancias);
             PreparedStatement stmtPerdidas = conn.prepareStatement(queryPerdidas)) {
    
            // Ejecutamos las consultas y obtenemos los resultados
            ResultSet rsGanancias = stmtGanancias.executeQuery();
            ResultSet rsPerdidas = stmtPerdidas.executeQuery();
    
            // Obtenemos las ganancias
            double totalGanancias = 0;
            if (rsGanancias.next()) {
                totalGanancias = rsGanancias.getDouble("total_ganancias");
            }
    
            // Obtenemos las pérdidas
            double totalPerdidas = 0;
            if (rsPerdidas.next()) {
                totalPerdidas = rsPerdidas.getDouble("total_perdidas");
            }
    
            // Limpiamos los datos anteriores en el gráfico
            gananciasVsPerdidasChart.getData().clear();
    
            // Añadimos los datos al gráfico de torta
            gananciasVsPerdidasChart.getData().add(new PieChart.Data("Ganancias", totalGanancias));
            gananciasVsPerdidasChart.getData().add(new PieChart.Data("Pérdidas", totalPerdidas));
    
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo de errores
        }
    }

    private void cargarDatosGananciasVsPerdidas(LocalDate fechaInicio, LocalDate fechaFin) {
        gananciasVsPerdidasChart.getData().clear(); // Limpiar datos anteriores

        String queryGanancias = "SELECT SUM(v.total) AS total_ganancias " +
                                "FROM venta v " +
                                "WHERE v.fecha BETWEEN ? AND ?";
        String queryPerdidas = "SELECT SUM(p.total) AS total_perdidas " +
                               "FROM perdida p " +
                               "WHERE p.created_at BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtGanancias = conn.prepareStatement(queryGanancias);
             PreparedStatement stmtPerdidas = conn.prepareStatement(queryPerdidas)) {

            stmtGanancias.setDate(1, java.sql.Date.valueOf(fechaInicio));
            stmtGanancias.setDate(2, java.sql.Date.valueOf(fechaFin));
            stmtPerdidas.setDate(1, java.sql.Date.valueOf(fechaInicio));
            stmtPerdidas.setDate(2, java.sql.Date.valueOf(fechaFin));

            ResultSet rsGanancias = stmtGanancias.executeQuery();
            ResultSet rsPerdidas = stmtPerdidas.executeQuery();

            double totalGanancias = rsGanancias.next() ? rsGanancias.getDouble("total_ganancias") : 0;
            double totalPerdidas = rsPerdidas.next() ? rsPerdidas.getDouble("total_perdidas") : 0;

            gananciasVsPerdidasChart.getData().add(new PieChart.Data("Ganancias", totalGanancias));
            gananciasVsPerdidasChart.getData().add(new PieChart.Data("Pérdidas", totalPerdidas));
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo de errores
        }
    }
    
    private void cargarDatosProductoMasExistencias() {
        // Consulta SQL para obtener los productos con más existencias
        String query = "SELECT p.nombre, SUM(l.cantidad) AS total_existencias " +
                       "FROM lote l " +
                       "JOIN producto p ON l.id_producto = p.id " +
                       "WHERE l.id_state = 1 " +  // Solo lotes activos
                       "AND l.fecha_caducidad >= CURDATE() " +  // Solo los lotes no caducados
                       "GROUP BY p.id " +
                       "ORDER BY total_existencias DESC " +
                       "LIMIT 5";  // Los 5 productos con más existencias
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            // Limpiamos los datos anteriores en el gráfico
            productoMasExistenciasChart.getData().clear();
    
            // Creamos una nueva serie de datos para el gráfico
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Productos con Más Existencias");
    
            // Iteramos sobre los resultados obtenidos y los agregamos a la serie
            while (rs.next()) {
                String nombreProducto = rs.getString("nombre");
                int totalExistencias = rs.getInt("total_existencias");
    
                // Añadimos cada producto y su cantidad al gráfico de barras
                series.getData().add(new XYChart.Data<>(nombreProducto, totalExistencias));
            }
    
            // Añadimos la serie al gráfico
            productoMasExistenciasChart.getData().add(series);
    
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo de errores
        }
    }

    private void cargarDatosProductoPocasExistencias() {
        // Consulta SQL para obtener los productos con menos existencias
        String query = "SELECT p.nombre, SUM(l.cantidad) AS total_existencias " +
                       "FROM lote l " +
                       "JOIN producto p ON l.id_producto = p.id " +
                       "WHERE l.id_state = 1 " +  // Solo lotes activos
                       "AND l.fecha_caducidad >= CURDATE() " +  // Solo los lotes no caducados
                       "GROUP BY p.id " +
                       "ORDER BY total_existencias ASC " +  // Ordenar por existencias de menor a mayor
                       "LIMIT 5";  // Los 5 productos con menos existencias
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            // Limpiamos los datos anteriores en el gráfico
            productoPocasExistenciasChart.getData().clear();
    
            // Creamos una nueva serie de datos para el gráfico
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Productos con Pocas Existencias");
    
            // Iteramos sobre los resultados obtenidos y los agregamos a la serie
            while (rs.next()) {
                String nombreProducto = rs.getString("nombre");
                int totalExistencias = rs.getInt("total_existencias");
    
                // Añadimos cada producto y su cantidad al gráfico de barras
                series.getData().add(new XYChart.Data<>(nombreProducto, totalExistencias));
            }
    
            // Añadimos la serie al gráfico
            productoPocasExistenciasChart.getData().add(series);
    
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo de errores
        }
    }    

    private void cargarUsuarioLogueado() {
        // Simulación de usuario logueado
        usuarioLogueadoLabel.setText("¡Bienvenido "+ user.getNombre()+"!");
    }

    @FXML
    private void filtrarPorFechas() {
        LocalDate fechaInicio = fechaInicioPicker.getValue();
        LocalDate fechaFin = fechaFinPicker.getValue();

        if (fechaInicio != null && fechaFin != null) {
            // Validar que la fecha de inicio no sea posterior a la fecha de fin
            if (fechaInicio.isAfter(fechaFin)) {
                mostrarAlerta("Rango de Fechas Inválido", "La fecha de inicio no puede ser posterior a la fecha de fin.", Alert.AlertType.WARNING);
                return;
            }

            // Llamar a los métodos de carga de datos con el rango de fechas
            cargarDatosVentasPorMes(fechaInicio, fechaFin);
            cargarDatosProductoMasVendido(fechaInicio, fechaFin);
            cargarDatosGananciasVsPerdidas(fechaInicio, fechaFin);

            mostrarAlerta("Filtrado Exitoso", "Los datos han sido filtrados desde " + fechaInicio + " hasta " + fechaFin + ".", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Fechas No Seleccionadas", "Por favor selecciona ambas fechas para aplicar el filtro.", Alert.AlertType.WARNING);
        }
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}

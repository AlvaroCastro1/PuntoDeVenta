package dulceria.reports;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import dulceria.DatabaseConnection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GeneradorPDF {

    // Método principal para generar el reporte
    public static void generarReporte(String nombreArchivo, String titulo, List<String[]> datos, boolean incluirTotales) {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
    
            PDPageContentStream contenido = new PDPageContentStream(documento, pagina);
    
            // Agregar encabezado, fecha y tabla
            agregarEncabezado(contenido, pagina, titulo, documento);
            dibujarTabla(contenido, pagina, datos, documento, incluirTotales);
    
            contenido.close();
    
            // Pie de página decorativo
            agregarPieDePagina(documento);
    
            documento.save(nombreArchivo);
            System.out.println("Reporte generado exitosamente: " + nombreArchivo);
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Función para agregar el encabezado (título + fecha)
    private static void agregarEncabezado(PDPageContentStream contenido, PDPage pagina, String titulo, PDDocument documento) throws IOException {
        // Membrete con imagen
        agregarMembrete(contenido, pagina, documento);
        
        // Fecha actual
        agregarFecha(contenido, pagina);
        
        // Título del reporte
        float anchoPagina = pagina.getMediaBox().getWidth();
        contenido.beginText();
        contenido.setFont(PDType1Font.HELVETICA_BOLD, 26);
        contenido.setNonStrokingColor(50, 100, 200); // Color azul
        contenido.newLineAtOffset(anchoPagina / 2 - 100, 700);
        contenido.showText(titulo);
        contenido.endText();
    }

    // Función para agregar una imagen al encabezado
    private static void agregarMembrete(PDPageContentStream contenido, PDPage pagina, PDDocument documento) throws IOException {
        InputStream inputStream = GeneradorPDF.class.getResourceAsStream("/dulceria/images/head.jpg");
        byte[] imageBytes = inputStream.readAllBytes();
        PDImageXObject imagen = PDImageXObject.createFromByteArray(documento, imageBytes, "head.jpg");
        
        float anchoPagina = pagina.getMediaBox().getWidth();
        float alturaPagina = pagina.getMediaBox().getHeight();
        float alturaImagen = 150;  // Altura de la imagen en puntos
        contenido.drawImage(imagen, 0, alturaPagina - alturaImagen, anchoPagina, alturaImagen);
    }

    // Función para agregar la fecha en la parte superior derecha
    private static void agregarFecha(PDPageContentStream contenido, PDPage pagina) throws IOException {
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        contenido.beginText();
        contenido.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
        contenido.setNonStrokingColor(100, 100, 100); // Gris
        contenido.newLineAtOffset(pagina.getMediaBox().getWidth() - 120, pagina.getMediaBox().getHeight() - 50);
        contenido.showText("Fecha: " + fecha);
        contenido.endText();
    }

    // Función para agregar el pie de página con numeración
    private static void agregarPieDePagina(PDDocument documento) throws IOException {
        int totalPaginas = documento.getNumberOfPages();
        for (int i = 0; i < totalPaginas; i++) {
            PDPage pagina = documento.getPage(i);
            PDPageContentStream contenido = new PDPageContentStream(documento, pagina, PDPageContentStream.AppendMode.APPEND, true);

            // Línea decorativa
            contenido.setLineWidth(1);
            contenido.setStrokingColor(150, 100, 200); // Morado
            contenido.moveTo(50, 50);
            contenido.lineTo(pagina.getMediaBox().getWidth() - 50, 50);
            contenido.stroke();

            // Numeración
            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            contenido.setNonStrokingColor(100, 100, 100); // Gris
            contenido.newLineAtOffset(pagina.getMediaBox().getWidth() - 100, 30);
            contenido.showText((i + 1) + " / " + totalPaginas);
            contenido.endText();

            contenido.close();
        }
    }

    // Función para dibujar la tabla en el reporte con totales
    private static void dibujarTabla(PDPageContentStream contenido, PDPage pagina, List<String[]> datos, PDDocument documento, boolean incluirTotales) throws IOException {
        float margenIzquierdo = 70;
        float margenDerecho = 70;
        float y = 600; // Posición inicial en el eje Y
        float anchoDisponible = pagina.getMediaBox().getWidth() - margenIzquierdo - margenDerecho;
        float alturaFila = 25;
        int numColumnas = datos.get(0).length;
        float anchoColumna = anchoDisponible / numColumnas;

        double[] totales = new double[numColumnas];

        for (int i = 0; i < datos.size(); i++) {
            if (y - alturaFila < 50) {  // Nueva página si no hay espacio
                contenido.close();
                PDPage nuevaPagina = new PDPage(PDRectangle.A4);
                documento.addPage(nuevaPagina);
                contenido = new PDPageContentStream(documento, nuevaPagina);
                y = 800;  
                agregarEncabezado(contenido, nuevaPagina, "Reporte", documento);
            }

            if (i == 0) {
                contenido.setNonStrokingColor(38, 166, 154);
                contenido.addRect(margenIzquierdo, y, anchoDisponible, alturaFila);
                contenido.fill();
                contenido.setNonStrokingColor(255, 255, 255);
                contenido.setFont(PDType1Font.HELVETICA_BOLD, 12);
            } else {
                contenido.setNonStrokingColor(i % 2 == 0 ? 224 : 255, 242, 241);
                contenido.addRect(margenIzquierdo, y, anchoDisponible, alturaFila);
                contenido.fill();
                contenido.setNonStrokingColor(33, 33, 33);
                contenido.setFont(PDType1Font.HELVETICA, 10);
                for (int j = 0; j < numColumnas; j++) {
                    try {
                        totales[j] += Double.parseDouble(datos.get(i)[j]);
                    } catch (NumberFormatException e) {
                        // Ignorar si no es un número
                    }
                }
            }

            contenido.setStrokingColor(120, 144, 156);
            for (int j = 0; j < numColumnas; j++) {
                contenido.addRect(margenIzquierdo + j * anchoColumna, y, anchoColumna, alturaFila);
            }
            contenido.stroke();

            for (int j = 0; j < numColumnas; j++) {
                contenido.beginText();
                float textoX = margenIzquierdo + j * anchoColumna + 5;
                float textoY = y + 7;
                contenido.newLineAtOffset(textoX, textoY);

                if (i == 0) {
                    float textoAncho = PDType1Font.HELVETICA_BOLD.getStringWidth(datos.get(i)[j]) / 1000 * 12;
                    float centrarX = (anchoColumna - textoAncho) / 2;
                    contenido.newLineAtOffset(centrarX - 5, 0);
                }
                contenido.showText(datos.get(i)[j]);
                contenido.endText();
            }

            y -= alturaFila;
        }

        if (incluirTotales) {
            contenido.setNonStrokingColor(38, 166, 154);
            contenido.addRect(margenIzquierdo, y, anchoDisponible, alturaFila);
            contenido.fill();
            contenido.setNonStrokingColor(255, 255, 255);
            contenido.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contenido.beginText();
            contenido.newLineAtOffset(margenIzquierdo + 5, y + 7);
            contenido.showText("Totales:");
            contenido.endText();

            for (int j = 0; j < numColumnas; j++) {
                contenido.beginText();
                float textoX = margenIzquierdo + j * anchoColumna + 5;
                contenido.newLineAtOffset(textoX, y + 7);
                if (j != 0) {
                    contenido.showText(String.format("%.2f", totales[j]));
                }
                contenido.endText();
            }
        }
    }

        // Método para obtener los datos desde la base de datos (ejemplo para inventario)
        public static List<String[]> obtenerDatosInventario() {
            List<String[]> datosInventario = new ArrayList<>();
            String query = "SELECT p.nombre, p.codigo, p.categoria, SUM(l.cantidad) AS cantidad_total FROM producto p " +
                           "LEFT JOIN lote l ON p.id = l.id_producto GROUP BY p.id ORDER BY p.nombre";
    
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
    
                String[] encabezado = {"Producto", "Código", "Categoría", "Cantidad"};
                datosInventario.add(encabezado);
                while (rs.next()) {
                    String[] fila = new String[4];
                    fila[0] = rs.getString("nombre");
                    fila[1] = rs.getString("codigo");
                    fila[2] = rs.getString("categoria");
                    fila[3] = String.valueOf(rs.getInt("cantidad_total"));
                    datosInventario.add(fila);
                }
    
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
            return datosInventario;
        }
    
        public static List<String[]> obtenerDatosPerdida() {
            List<String[]> datos = new ArrayList<>();
            
            // Consulta SQL para obtener los datos
            String query = "SELECT p.nombre AS producto, l.id AS lote, pe.cantidad, pe.costo_unitario, pe.total " +
                           "FROM perdida pe " +
                           "JOIN producto p ON pe.id_producto = p.id " +
                           "JOIN lote l ON pe.id_lote = l.id " +
                           "WHERE " + 
                           "MONTH(pe.created_at) = MONTH(CURRENT_DATE) " +  // Filtro para el mes actual
                           "AND YEAR(pe.created_at) = YEAR(CURRENT_DATE) " +  // Filtro para el año actual
                           "ORDER BY p.nombre";
            
            // Encabezado para el reporte
            String[] encabezado = {"Producto", "Lote", "Cantidad", "Costo Unitario", "Total"};
            
            // Agregar encabezado a la lista de datos
            datos.add(encabezado);
            
            // Conexión a la base de datos
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
        
                // Procesar cada fila de resultados
                while (rs.next()) {
                    String[] fila = new String[5];  // La fila tiene 5 columnas según la consulta
                    fila[0] = rs.getString("producto");  // Nombre del producto
                    fila[1] = String.valueOf(rs.getInt("lote"));  // ID del lote
                    fila[2] = String.valueOf(rs.getInt("cantidad"));  // Cantidad
                    fila[3] = String.format("%.2f", rs.getDouble("costo_unitario"));  // Costo unitario
                    fila[4] = String.format("%.2f", rs.getDouble("total"));  // Total
                    
                    // Agregar la fila a la lista de datos
                    datos.add(fila);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return datos;
        }    
    
        public static List<String[]> obtenerDatosVentas() {
            List<String[]> datos = new ArrayList<>();
            
            // Consulta SQL para obtener los datos de ventas con el nombre del usuario
            String query = "SELECT " +
                           "v.fecha, " +
                           "v.total AS total_venta, " +
                           "COUNT(dv.id_producto) AS cantidad_productos, " +
                           "u.nombre AS nombre_usuario " +
                           "FROM venta v " +
                           "JOIN detalle_venta dv ON v.id = dv.id_venta " +
                           "JOIN usuario u ON v.id_usuario = u.id " +
                           "WHERE " + 
                           "MONTH(v.created_at) = MONTH(CURRENT_DATE) " +  // Filtro para el mes actual
                           "AND YEAR(v.created_at) = YEAR(CURRENT_DATE) " +  // Filtro para el año actual
                           "GROUP BY v.fecha, v.total, u.nombre " +
                           "ORDER BY v.fecha DESC";
            
            // Encabezado para el reporte
            String[] encabezado = {"Fecha", "Total Venta", "Cantidad de Productos", "Vendedor"};
            
            // Agregar encabezado a la lista de datos
            datos.add(encabezado);
            
            // Conexión a la base de datos
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
        
                // Procesar cada fila de resultados
                while (rs.next()) {
                    String[] fila = new String[4];  // 4 columnas
                    fila[0] = rs.getString("fecha");  // Fecha de la venta
                    fila[1] = String.format("%.2f", rs.getDouble("total_venta"));  // Total de la venta
                    fila[2] = String.valueOf(rs.getInt("cantidad_productos"));  // Cantidad de productos vendidos
                    fila[3] = rs.getString("nombre_usuario");  // Nombre del usuario que registró la venta
                    
                    // Agregar la fila a la lista de datos
                    datos.add(fila);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return datos;
        }
    
        public static List<String[]> obtenerDatosPromociones() {
            List<String[]> datos = new ArrayList<>();
            
            // Consulta SQL para obtener los datos de promociones activas
            String query = "SELECT " +
                           "p.nombre AS promocion, " +
                           "COUNT(dv.id_promocion) AS cantidad_vendida " +
                           "FROM promocion p " +
                           "LEFT JOIN detalle_venta dv ON p.id = dv.id_promocion " +
                           "LEFT JOIN venta v ON dv.id_venta = v.id " +
                           "WHERE " + 
                           "MONTH(dv.created_at) = MONTH(CURRENT_DATE) " +  // Filtro para el mes actual
                           "AND YEAR(dv.created_at) = YEAR(CURRENT_DATE) " +  // Filtro para el año actual
                           "GROUP BY p.nombre " +
                           "ORDER BY cantidad_vendida DESC";
            
            // Encabezado para el reporte
            String[] encabezado = {"Promoción", "Cantidad Vendida"};
            
            // Agregar encabezado a la lista de datos
            datos.add(encabezado);
            
            // Conexión a la base de datos
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
        
                // Procesar cada fila de resultados
                while (rs.next()) {
                    String[] fila = new String[2];  // 2 columnas
                    fila[0] = rs.getString("promocion");  // Nombre de la promoción
                    fila[1] = String.valueOf(rs.getInt("cantidad_vendida"));  // Cantidad vendida de la promoción
                    
                    // Agregar la fila a la lista de datos
                    datos.add(fila);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return datos;
        }
        
        public static List<String[]> obtenerDatosProductosMasVendidos() {
            List<String[]> datos = new ArrayList<>();
            
            // Consulta SQL para obtener los productos más vendidos (ventas pagadas)
            String query = "SELECT " +
                           "p.nombre AS producto, " +
                           "SUM(dv.cantidad) AS cantidad_vendida " +
                           "FROM detalle_venta dv " +
                           "JOIN producto p ON dv.id_producto = p.id " +
                           "JOIN venta v ON dv.id_venta = v.id " +
                           "WHERE v.id_state = 6 " +  // Solo ventas pagadas
                           "AND MONTH(dv.created_at) = MONTH(CURRENT_DATE) " +  // Filtro para el mes actual
                           "AND YEAR(dv.created_at) = YEAR(CURRENT_DATE) " +  // Filtro para el año actual
                           "GROUP BY p.nombre " +
                           "ORDER BY cantidad_vendida DESC";
            
            // Encabezado para el reporte
            String[] encabezado = {"Producto", "Cantidad Vendida"};
            
            // Agregar encabezado a la lista de datos
            datos.add(encabezado);
            
            // Conexión a la base de datos
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
        
                // Procesar cada fila de resultados
                while (rs.next()) {
                    String[] fila = new String[2];  // La fila tiene 2 columnas según la consulta
                    fila[0] = rs.getString("producto");  // Nombre del producto
                    fila[1] = String.valueOf(rs.getInt("cantidad_vendida"));  // Cantidad vendida
                    
                    // Agregar la fila a la lista de datos
                    datos.add(fila);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return datos;
        }
    
        public static List<String[]> obtenerDatosProductosMenosVendidos() {
            List<String[]> datos = new ArrayList<>();
            
            // Consulta SQL para obtener los productos menos vendidos (ventas pagadas)
            String query = "SELECT " +
                           "p.nombre AS producto, " +
                           "SUM(dv.cantidad) AS cantidad_vendida " +
                           "FROM detalle_venta dv " +
                           "JOIN producto p ON dv.id_producto = p.id " +
                           "JOIN venta v ON dv.id_venta = v.id " +
                           "WHERE v.id_state = 6 " +  // Solo ventas pagadas
                           "AND MONTH(dv.created_at) = MONTH(CURRENT_DATE) " +  // Filtro para el mes actual
                           "AND YEAR(dv.created_at) = YEAR(CURRENT_DATE) " +  // Filtro para el año actual
                           "GROUP BY p.nombre " +
                           "ORDER BY cantidad_vendida ASC";  // Orden ascendente
            
            // Encabezado para el reporte
            String[] encabezado = {"Producto", "Cantidad Vendida"};
            
            // Agregar encabezado a la lista de datos
            datos.add(encabezado);
            
            // Conexión a la base de datos
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
        
                // Procesar cada fila de resultados
                while (rs.next()) {
                    String[] fila = new String[2];  // La fila tiene 2 columnas según la consulta
                    fila[0] = rs.getString("producto");  // Nombre del producto
                    fila[1] = String.valueOf(rs.getInt("cantidad_vendida"));  // Cantidad vendida
                    
                    // Agregar la fila a la lista de datos
                    datos.add(fila);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return datos;
        }
        
        
        public static void main(String[] args) {
    
            List<String[]> datos_inventario = obtenerDatosInventario();
            generarReporte("reporte_inventario.pdf", "Reporte de Inventario", datos_inventario, false);
    
            List<String[]> datos_perdidas = obtenerDatosPerdida();
            generarReporte("reporte_Perdidas.pdf", "Reporte de Perdidas", datos_perdidas, true);
    
            List<String[]> datos_Ventas = obtenerDatosVentas();
            generarReporte("reporte_Ventas.pdf", "Reporte de Ventas", datos_Ventas, true);
    
            List<String[]> datos_Promociones = obtenerDatosPromociones();
            generarReporte("reporte_Promociones.pdf", "Reporte de Promociones", datos_Promociones, true);
            
            List<String[]> datos_MasVendidos = obtenerDatosProductosMasVendidos();
            generarReporte("reporte_MasVendidos.pdf", "Reporte de Mas Vendidos", datos_MasVendidos, true);
    
            List<String[]> datos_MenosVendidos = obtenerDatosProductosMenosVendidos();
            generarReporte("reporte_MenosVendidos.pdf", "Reporte de Menos Vendidos", datos_MenosVendidos, true);
    
        }
    }

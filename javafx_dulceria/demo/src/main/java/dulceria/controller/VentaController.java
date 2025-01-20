package dulceria.controller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import dulceria.DatabaseConnection;
import dulceria.model.Producto;
import dulceria.model.VentaProducto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

public class VentaController {

    private DatabaseConnection dbConnection;


    @FXML
    private ComboBox<Producto> comboProducto;
    @FXML
    private TextField txtPrecioUnitario;
    @FXML
    private TableView<VentaProducto> tablaVenta;
    @FXML
    private TableColumn<VentaProducto, Integer> colConsecutivo;
    @FXML
    private TableColumn<VentaProducto, String> colProducto;
    @FXML
    private TableColumn<VentaProducto, Integer> colCantidad;
    @FXML
    private TableColumn<VentaProducto, Double> colPrecioUnitario;
    @FXML
    private TableColumn<VentaProducto, Double> colTotal;
    @FXML
    private Label lblTotal;

    private final ObservableList<VentaProducto> listaVenta = FXCollections.observableArrayList();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        configurarContextMenu();
        // Configurar columnas de la tabla
        colConsecutivo.setCellValueFactory(new PropertyValueFactory<>("num"));
        colProducto.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Llenar ComboBox con productos desde la base de datos
        cargarProductosDesdeBD();

        // Asignar lista de venta a la tabla
        tablaVenta.setItems(listaVenta);

        // Actualizar precio unitario al seleccionar un producto
        comboProducto.setOnAction(event -> {
            Producto seleccionado = comboProducto.getValue();
            if (seleccionado != null) {
                txtPrecioUnitario.setText(String.format("%.2f", seleccionado.getPrecio()));
            }
        });

        actualizarTotal();
    }

    private void cargarProductosDesdeBD() {
        try (Connection conn = dbConnection.getConnection()) {
            // Consulta para cargar productos con sus existencias totales
            String sqlProducto = "SELECT p.id, p.nombre, p.codigo, p.descripcion,\n"+
                       "p.categoria, p.precio, p.costo,\n"+
                       "COALESCE(SUM(l.cantidad), 0) AS total_existencias\n"+
                "FROM producto p\n"+
                "LEFT JOIN lote l ON p.id = l.id_producto\n"+
                "GROUP BY p.id, p.nombre, p.codigo, p.descripcion, p.categoria, p.precio, p.costo\n"+
                "HAVING total_existencias > 0\n";
    
            Statement stmt = conn.createStatement();
            ResultSet rsProducto = stmt.executeQuery(sqlProducto);
    
            while (rsProducto.next()) {
                // Crear productos y agregarlos a la lista sólo si tienen existencias
                Producto producto = new Producto(
                    rsProducto.getInt("id"),
                    rsProducto.getString("nombre"),
                    rsProducto.getString("codigo"),
                    rsProducto.getString("descripcion"),
                    rsProducto.getString("categoria"),
                    rsProducto.getDouble("precio"),
                    rsProducto.getDouble("costo")
                );
                productos.add(producto);
            }

            // Configurar los productos en el ComboBox
            comboProducto.setItems(productos);
    
            // Configurar cómo se muestran los productos en el ComboBox
            comboProducto.setCellFactory(param -> new ListCell<Producto>() {
                @Override
                protected void updateItem(Producto item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getId() + " - " + item.getNombre());
                    } else {
                        setText(null);
                    }
                }
            });
            comboProducto.setButtonCell(new ListCell<Producto>() {
                @Override
                protected void updateItem(Producto item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getId() + " - " + item.getNombre());
                    } else {
                        setText(null);
                    }
                }
            });
    
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar ComboBox: " + e.getMessage());
        }
    }

    @FXML
    private void agregarProducto(ActionEvent event) {
        Producto productoSeleccionado = comboProducto.getValue();
        if (productoSeleccionado == null) {
            mostrarAlerta("Error", "Seleccione un producto antes de agregar.");
            return;
        }
    
        // Validar existencia del producto en la base de datos
        int existenciasDisponibles = obtenerExistenciasProducto(productoSeleccionado.getId());
        if (existenciasDisponibles <= 0) {
            mostrarAlerta("Error", "El producto seleccionado no tiene existencias disponibles.");
            return;
        }
    
        // Calcular la cantidad total ya añadida al detalle de la venta
        int cantidadEnDetalle = listaVenta.stream()
            .filter(vp -> vp.getProducto().getId() == productoSeleccionado.getId())
            .mapToInt(VentaProducto::getCantidad)
            .sum();
    
        // Validar si se puede añadir al detalle sin exceder las existencias
        if (cantidadEnDetalle >= existenciasDisponibles) {
            mostrarAlerta("Error", "No se pueden añadir más unidades de este producto. Existencias disponibles: " + existenciasDisponibles);
            return;
        }
    
        // Verificar si ya existe en la lista y actualizar su cantidad
        Optional<VentaProducto> productoExistente = listaVenta.stream()
            .filter(vp -> vp.getProducto().getId() == productoSeleccionado.getId())
            .findFirst();
    
        if (productoExistente.isPresent()) {
            VentaProducto ventaProducto = productoExistente.get();
            int nuevaCantidad = ventaProducto.getCantidad() + 1;
    
            if (nuevaCantidad > existenciasDisponibles) {
                mostrarAlerta("Error", "No puedes agregar más de las existencias disponibles: " + existenciasDisponibles);
                return;
            }
    
            ventaProducto.setCantidad(nuevaCantidad);
            ventaProducto.setTotal(nuevaCantidad * ventaProducto.getPrecioUnitario());
        } else {
            // Si no existe, añadir un nuevo producto con cantidad inicial de 1
            VentaProducto ventaProducto = new VentaProducto(
                listaVenta.size() + 1,
                productoSeleccionado,
                1,
                productoSeleccionado.getPrecio()
            );
            listaVenta.add(ventaProducto);
        }
    
        actualizarTotal();
    }

    private int obtenerExistenciasProducto(int idProducto) {
        try (Connection conn = dbConnection.getConnection()) {
            String sql = "SELECT COALESCE(SUM(cantidad), 0) AS total_existencias FROM lote WHERE id_producto = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idProducto);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total_existencias");
            }
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al obtener existencias del producto: " + e.getMessage());
        }
        return 0;
    }

    @FXML
    private void guardarVenta() {
        Connection connection = null;
    
        try {
            // Iniciar conexión y transacción
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);
    
            // Validar existencias
            validarExistencias(connection);
    
            // Insertar venta y obtener ID
            int idVenta = insertarVenta(connection);
            // Procesar detalles de venta y actualizar lotes
            procesarDetallesVenta(connection, idVenta);
    
            // Confirmar transacción
            connection.commit();
            mostrarAlerta("Información", "Venta registrada correctamente.", Alert.AlertType.CONFIRMATION);
            limpiarFormulario();
        } catch (SQLException e) {
            manejarErrorTransaccion(connection, e);
        } finally {
            cerrarConexion(connection);
        }
    }
    
    private void validarExistencias(Connection connection) throws SQLException {
        String sql = "SELECT SUM(cantidad) FROM lote WHERE id_producto = ? AND cantidad >= ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (VentaProducto producto : tablaVenta.getItems()) {
                stmt.setInt(1, producto.getProducto().getId());
                stmt.setInt(2, producto.getCantidad());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) < producto.getCantidad()) {
                        throw new SQLException("No hay suficiente stock para el producto: " + producto.getProducto().getNombre());
                    }
                }
            }
        }
    }
    
    private int insertarVenta(Connection connection) throws SQLException {
        String sql = "INSERT INTO venta (total, fecha, id_state) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, calcularTotalVenta());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now())); // Fecha actual
            stmt.setInt(3, 1); // Estado inicial
            stmt.executeUpdate();
    
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idVenta = generatedKeys.getInt(1);
                    // "ID de la venta generada: " + idVenta
                    return idVenta;
                } else {
                    throw new SQLException("Error al obtener el ID de la venta.");
                }
            }
        }
    }
    
    private void procesarDetallesVenta(Connection connection, int idVenta) throws SQLException {
        String sqlDetalle = "INSERT INTO detalle_venta (" +
                "id_venta, id_producto, id_lote, costo_unitario, " +
                "precio_unitario, cantidad, id_promocion, descuento_aplicado" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlBuscarLote = "SELECT id, cantidad, fecha_caducidad FROM lote " +
                "WHERE id_producto = ? AND cantidad >= ? ORDER BY fecha_caducidad ASC LIMIT 1";
        String sqlActualizarLote = "UPDATE lote SET cantidad = cantidad - ? WHERE id = ?";
        String sqlBuscarCostoProducto = "SELECT precio FROM producto WHERE id = ?";
    
        try (
                PreparedStatement detalleStmt = connection.prepareStatement(sqlDetalle);
                PreparedStatement buscarLoteStmt = connection.prepareStatement(sqlBuscarLote);
                PreparedStatement actualizarLoteStmt = connection.prepareStatement(sqlActualizarLote);
                PreparedStatement buscarCostoProductoStmt = connection.prepareStatement(sqlBuscarCostoProducto)
        ) {
            for (VentaProducto producto : tablaVenta.getItems()) {
                int idLote = obtenerLote(connection, buscarLoteStmt, producto);
                double costoUnitario = obtenerCostoProducto(connection, buscarCostoProductoStmt, producto);
    
                // Insertar detalle de venta
                detalleStmt.setInt(1, idVenta);
                detalleStmt.setInt(2, producto.getProducto().getId());
                detalleStmt.setInt(3, idLote);
                detalleStmt.setDouble(4, costoUnitario);
                detalleStmt.setDouble(5, producto.getPrecioUnitario());
                detalleStmt.setInt(6, producto.getCantidad());
                detalleStmt.setObject(7, null); // Puede ser null
                detalleStmt.setObject(8, null); // Puede ser null
                detalleStmt.addBatch();
    
                // Actualizar lote
                actualizarLoteStmt.setInt(1, producto.getCantidad());
                actualizarLoteStmt.setInt(2, idLote);
                actualizarLoteStmt.addBatch();
            }
    
            detalleStmt.executeBatch();
            actualizarLoteStmt.executeBatch();
        }
    }
    
    private int obtenerLote(Connection connection, PreparedStatement stmt, VentaProducto producto) throws SQLException {
        stmt.setInt(1, producto.getProducto().getId());
        stmt.setInt(2, producto.getCantidad());
    
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("No se encontró un lote disponible para el producto: " + producto.getProducto().getNombre());
            }
        }
    }
    
    private double obtenerCostoProducto(Connection connection, PreparedStatement stmt, VentaProducto producto) throws SQLException {
        stmt.setInt(1, producto.getProducto().getId());
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("precio");
            } else {
                throw new SQLException("No se encontró el costo del producto: " + producto.getProducto().getNombre());
            }
        }
    }
    
    private void manejarErrorTransaccion(Connection connection, SQLException e) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        mostrarAlerta("Error", "Error al guardar la venta: " + e.getMessage(), Alert.AlertType.ERROR);
    }
    
    private void cerrarConexion(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    

    private void actualizarTotal() {
        double total = listaVenta.stream().mapToDouble(VentaProducto::getTotal).sum();
        lblTotal.setText(String.format("Total: $%.2f", total));
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // Modelo para un producto en la tabla
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void actualizarConsecutivos() {
        int num = 1;
        for (VentaProducto producto : tablaVenta.getItems()) {
            producto.setNum(num);
            num++;
        }
    }

    private void configurarContextMenu() {
        // Crear el menú contextual
        ContextMenu contextMenu = new ContextMenu();
        MenuItem eliminarItem = new MenuItem("Eliminar");
    
        // Configurar la acción del menú "Eliminar"
        eliminarItem.setOnAction(event -> {
            VentaProducto productoSeleccionado = tablaVenta.getSelectionModel().getSelectedItem();
            if (productoSeleccionado != null) {
                tablaVenta.getItems().remove(productoSeleccionado); // Eliminar el producto seleccionado
                actualizarConsecutivos(); // Actualizar consecutivos después de eliminar
                actualizarTotal();
            }
        });
    
        contextMenu.getItems().add(eliminarItem);
    
        // Asignar el menú contextual a las filas de la tabla
        tablaVenta.setRowFactory(tv -> {
            TableRow<VentaProducto> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                } else {
                    contextMenu.hide();
                }
            });
            return row;
        });
    }
    
    private double calcularTotalVenta() {
        return tablaVenta.getItems().stream()
                .mapToDouble(VentaProducto::getTotal)
                .sum();
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    private void limpiarFormulario() {
        tablaVenta.getItems().clear();
        lblTotal.setText("Total: $0.00");
    }
    

}

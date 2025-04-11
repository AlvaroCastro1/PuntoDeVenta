package dulceria.controller;

import dulceria.model.Venta;
import dulceria.model.VentaProducto;
import dulceria.model.DetalleVenta;
import dulceria.model.Producto;
import dulceria.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.DatePicker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentasController {
    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, Integer> colIdVenta;
    @FXML private TableColumn<Venta, String> colFechaVenta;
    @FXML private TableColumn<Venta, Double> colTotalVenta;
    @FXML private TableColumn<Venta, String> colEstadoVenta;
    @FXML private TableColumn<Venta, String> colUsuarioVenta; // Nueva columna para usuario
    @FXML private TableColumn<DetalleVenta, String> colEstadoProd;
    @FXML private TableView<DetalleVenta> tablaDetalles;
    @FXML private TableColumn<DetalleVenta, String> colProducto;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, Integer> colLote;
    @FXML private TableColumn<DetalleVenta, Double> colPrecioUnitario;
    @FXML private TableColumn<DetalleVenta, Double> colCostoUnitario;
    @FXML private TextField txtBusqueda;
    @FXML private DatePicker datePickerInicio;
    @FXML private DatePicker datePickerFin;

    private ContextMenu contextMenu;
    private final ObservableList<Venta> ventas = FXCollections.observableArrayList();
    private final ObservableList<DetalleVenta> detalles = FXCollections.observableArrayList();

    public void initialize() {
        configurarTablas();
        cargarVentas();
        configurarContextMenu();

        // Listener para cargar detalles al seleccionar una venta
        tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDetalles(newSelection.getId());
            }
        });

        // Envolver la lista en un FilteredList
        FilteredList<Venta> filteredData = new FilteredList<>(ventas, p -> true);

        // Escuchar cambios en el campo de búsqueda
        txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(venta -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Mostrar todas las ventas si no hay filtro
                }
                String lowerCaseFilter = newValue.toLowerCase();

                // Comparar con los atributos de Venta
                return String.valueOf(venta.getId()).contains(lowerCaseFilter) ||
                    venta.getFecha().toLowerCase().contains(lowerCaseFilter) ||
                    String.valueOf(venta.getTotal()).contains(lowerCaseFilter) ||
                    venta.getEstado().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Escuchar cambios en los DatePicker
        datePickerInicio.valueProperty().addListener((obs, oldDate, newDate) -> aplicarFiltroFecha(filteredData));
        datePickerFin.valueProperty().addListener((obs, oldDate, newDate) -> aplicarFiltroFecha(filteredData));

        // Enlazar la lista filtrada con una SortedList para mantener el ordenamiento
        SortedList<Venta> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaVentas.comparatorProperty());

        // Asignar los datos filtrados a la tabla
        tablaVentas.setItems(sortedData);
        tablaVentas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurarTablas() {
        colIdVenta.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFechaVenta.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTotalVenta.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstadoVenta.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colUsuarioVenta.setCellValueFactory(new PropertyValueFactory<>("usuario")); // Configuración de la nueva columna

        colProducto.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colLote.setCellValueFactory(new PropertyValueFactory<>("lote"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colCostoUnitario.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        colEstadoProd.setCellValueFactory(new PropertyValueFactory<>("estado"));
        // Suponiendo que tienes una columna llamada "estadoCol" en tu tabla
        colEstadoProd.setCellValueFactory(cellData -> {
            // Aquí usamos el idState para obtener el nombre
            DetalleVenta detalle = cellData.getValue();
            return new SimpleStringProperty(obtenerNombreEstado(detalle.getIdState()));
        });

        tablaVentas.setItems(ventas);
        tablaDetalles.setItems(detalles);
        tablaVentas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurarContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem cancelarItem = new MenuItem("Cancelar Producto");
        cancelarItem.setOnAction(event -> cancelarProducto());
        contextMenu.getItems().add(cancelarItem);
        tablaDetalles.setContextMenu(contextMenu);
    }

    private void cargarVentas() {
        ventas.clear();
        String sql = "SELECT v.id, v.fecha, v.total, s.nombre_estado AS estado, u.nombre AS usuario " +
                     "FROM venta v " +
                     "JOIN cState s ON v.id_state = s.id " +
                     "JOIN usuario u ON v.id_usuario = u.id"; // Asegúrate de que la tabla y columna sean correctas

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ventas.add(new Venta(
                        rs.getInt("id"),
                        rs.getString("fecha"),
                        rs.getDouble("total"),
                        rs.getString("estado"),
                        rs.getString("usuario") // Añadir el usuario
                ));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar las ventas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarDetalles(int idVenta) {
        detalles.clear();
        String sql = "SELECT dv.id, dv.id_venta, dv.id_producto, dv.id_lote, dv.cantidad, dv.precio_unitario, dv.costo_unitario, dv.id_state, p.nombre, p.codigo " +
        "FROM detalle_venta dv " +
        "JOIN producto p ON dv.id_producto = p.id " +
        "WHERE dv.id_venta = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto(
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getString("codigo"),
                        "",
                        "",
                        rs.getDouble("precio_unitario"), 
                        rs.getDouble("costo_unitario")
                        );
                    detalles.add(new DetalleVenta(
                            rs.getInt("id"),
                            p,
                            rs.getInt("cantidad"),
                            rs.getInt("id_lote"),
                            rs.getDouble("precio_unitario"),
                            rs.getDouble("costo_unitario"),
                            rs.getInt("id_state")
                    ));
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar los detalles de la venta: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancelarVenta() {
        Venta ventaSeleccionada = tablaVentas.getSelectionModel().getSelectedItem();
        if (ventaSeleccionada == null) {
            mostrarAlerta("Advertencia", "Seleccione una venta para cancelar.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de cancelar esta venta?", ButtonType.YES, ButtonType.NO);
        confirmacion.showAndWait();

        if (confirmacion.getResult() == ButtonType.YES) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);


                // Verificar el estado de la venta
                String sqlVerificarEstado = "SELECT id_state FROM venta WHERE id = ?";
                PreparedStatement verificarStmt = connection.prepareStatement(sqlVerificarEstado);
                verificarStmt.setInt(1, ventaSeleccionada.getId());
                ResultSet resultado = verificarStmt.executeQuery();

                if (!resultado.next()) {
                    mostrarAlerta("Error", "La venta con ID " + ventaSeleccionada.getId() + " no existe.", Alert.AlertType.ERROR);
                    return;
                }

                int estadoActual = resultado.getInt("id_state");
                if (estadoActual == 2) { // Estado 2 = Cancelada
                    mostrarAlerta("Información", "La venta ya está cancelada.", Alert.AlertType.WARNING);
                    return;
                }
                
                // Cambiar estado de la venta
                String sqlActualizarVenta = "UPDATE venta SET id_state = ? WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sqlActualizarVenta)) {
                    stmt.setInt(1, 2); // Estado "cancelado"
                    stmt.setInt(2, ventaSeleccionada.getId());
                    stmt.executeUpdate();
                }

                // Revertir productos al lote
                String sqlActualizarLote = "UPDATE lote SET cantidad = cantidad + ? WHERE id = ?";
                String sqlObtenerDetalles = "SELECT id_lote, cantidad FROM detalle_venta WHERE id_venta = ?";
                try (PreparedStatement obtenerDetallesStmt = connection.prepareStatement(sqlObtenerDetalles);
                     PreparedStatement actualizarLoteStmt = connection.prepareStatement(sqlActualizarLote)) {

                    obtenerDetallesStmt.setInt(1, ventaSeleccionada.getId());
                    try (ResultSet rs = obtenerDetallesStmt.executeQuery()) {
                        while (rs.next()) {
                            actualizarLoteStmt.setInt(1, rs.getInt("cantidad"));
                            actualizarLoteStmt.setInt(2, rs.getInt("id_lote"));
                            actualizarLoteStmt.addBatch();
                        }
                        actualizarLoteStmt.executeBatch();
                    }
                }

                connection.commit();
                mostrarAlerta("Éxito", "La venta fue cancelada exitosamente.", Alert.AlertType.INFORMATION);
                cargarVentas();
                detalles.clear();
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al cancelar la venta: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void cancelarProducto() {
        DetalleVenta detalleSeleccionado = tablaDetalles.getSelectionModel().getSelectedItem();
        Venta ventaSeleccionada = tablaVentas.getSelectionModel().getSelectedItem();
    
        if (detalleSeleccionado == null) {
            mostrarAlerta("Error", "No se ha seleccionado ningún producto.", Alert.AlertType.ERROR);
            return;
        }
    
        if (detalleSeleccionado.getIdState() == 2) {
            mostrarAlerta("Error", "El producto ya está cancelado.", Alert.AlertType.ERROR);
            return;
        }
    
        // Confirmar la cancelación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cancelación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de que deseas cancelar este producto?");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
    
        // Procesar cancelación
        String sqlCancelarProducto = "UPDATE detalle_venta SET id_state = 2 WHERE id = ? AND id_venta = ? AND id_producto = ? AND id_lote = ?";
        String sqlActualizarLote = "UPDATE lote SET cantidad = cantidad + ? WHERE id = ?";
    
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
    
            // Actualizar el estado del detalle de la venta
            try (PreparedStatement stmtCancelar = connection.prepareStatement(sqlCancelarProducto)) {
                stmtCancelar.setInt(1, detalleSeleccionado.getId());
                stmtCancelar.setInt(2, ventaSeleccionada.getId());
                stmtCancelar.setInt(3, detalleSeleccionado.getProducto().getId());
                stmtCancelar.setInt(4, detalleSeleccionado.getLote());
                stmtCancelar.executeUpdate();
            }
    
            // Devolver la cantidad al lote correspondiente
            try (PreparedStatement stmtActualizarLote = connection.prepareStatement(sqlActualizarLote)) {
                stmtActualizarLote.setInt(1, detalleSeleccionado.getCantidad());
                stmtActualizarLote.setInt(2, detalleSeleccionado.getLote());
                stmtActualizarLote.executeUpdate();
            }
    
            connection.commit();
            mostrarAlerta("Éxito", "El producto ha sido cancelado correctamente.", Alert.AlertType.INFORMATION);
            cargarDetalleVenta(detalleSeleccionado.getId()); // Refrescar la tabla
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cancelar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    

    private void cargarDetalleVenta(int idVenta) {
        ObservableList<DetalleVenta> detalles = FXCollections.observableArrayList();
        String sql = "SELECT dv.id, dv.id_venta, dv.id_producto, dv.id_lote, dv.cantidad, dv.precio_unitario, dv.costo_unitario, dv.id_state, p.nombre " +
                     "FROM detalle_venta dv " +
                     "JOIN producto p ON dv.id_producto = p.id " +
                     "WHERE dv.id_venta = ? AND dv.id_state = 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idVenta);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("id_producto"),
                        rs.getString("producto"),
                        rs.getString("codigo"),
                        "",
                        "",
                        rs.getDouble("precio_unitario"), 
                        rs.getDouble("costo_unitario")
                        );
                detalles.add(new DetalleVenta(
                    rs.getInt("id"),
                    p,
                    rs.getInt("cantidad"),
                    rs.getInt("id_lote"),
                    rs.getDouble("precio_unitario"),
                    rs.getInt("costo_unitario"),
                    rs.getInt("id_state")
                ));
            }
            tablaDetalles.setItems(detalles);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private String obtenerNombreEstado(int idState) {
        String nombreEstado = "";
        String sqlEstado = "SELECT nombre_estado FROM cState WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sqlEstado)) {
            stmt.setInt(1, idState);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nombreEstado = rs.getString("nombre_estado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombreEstado;
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(contenido);
        alerta.show();
    }

    private void aplicarFiltroFecha(FilteredList<Venta> filteredData) {
        filteredData.setPredicate(venta -> {
            if (datePickerInicio.getValue() == null && datePickerFin.getValue() == null) {
                return true;
            }
            if (datePickerInicio.getValue() != null && datePickerFin.getValue() != null) {
                return venta.getFecha().compareTo(datePickerInicio.getValue().toString()) >= 0 &&
                       venta.getFecha().compareTo(datePickerFin.getValue().toString()) <= 0;
            }
            if (datePickerInicio.getValue() != null) {
                return venta.getFecha().compareTo(datePickerInicio.getValue().toString()) >= 0;
            }
            return venta.getFecha().compareTo(datePickerFin.getValue().toString()) <= 0;
        });
    }
}

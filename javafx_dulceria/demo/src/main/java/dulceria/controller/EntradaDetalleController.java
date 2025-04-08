package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.model.Entrada;
import dulceria.model.DetalleEntrada;
import dulceria.model.Estado;
import dulceria.model.Lote;
import dulceria.model.Producto;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class EntradaDetalleController {

    @FXML
    private TableView<Entrada> tablaEntradas;

    @FXML
    private TableColumn<Entrada, String> colFecha;

    @FXML
    private TableColumn<Entrada, String> colUsuario;

    @FXML
    private TableColumn<Entrada, Double> colTotal;

    @FXML
    private TableColumn<Entrada, String> colEstado;

    @FXML
    private TableView<DetalleEntrada> tablaDetalles;

    @FXML
    private TableColumn<DetalleEntrada, String> colProducto;

    @FXML
    private TableColumn<DetalleEntrada, String> colLote;

    @FXML
    private TableColumn<DetalleEntrada, Integer> colCantidad;

    @FXML
    private TableColumn<DetalleEntrada, Double> colCosto;

    @FXML
    private TableColumn<DetalleEntrada, Double> colSubtotal;

    @FXML
    private Label totalLabel;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnCambiar;

    @FXML
    private DatePicker datePickerDesde;

    @FXML
    private DatePicker datePickerHasta;

    private ObservableList<Entrada> listaEntradas = FXCollections.observableArrayList();
    private ObservableList<DetalleEntrada> listaDetalles = FXCollections.observableArrayList();
    private FilteredList<Entrada> filteredEntradas;

    @FXML
    public void initialize() {
        // Configurar las columnas de la tabla de entradas
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colUsuario.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsuarioNombre()));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstado.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEstado().getNombre()));

        // Configurar la lista filtrada
        filteredEntradas = new FilteredList<>(listaEntradas, p -> true);
        tablaEntradas.setItems(filteredEntradas);

        // Cargar las entradas desde la base de datos
        cargarEntradas();

        // Configurar el evento de selección de la tabla de entradas
        tablaEntradas.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    cargarDetalles(newValue); // Llamar al método que carga los detalles
                    actualizarTotal();
                }
            }
        );

        // Establecer eventos de los botones
        setupBotones();

        // Ajustar columnas de tablaEntradas
        ajustarColumnas(tablaEntradas);

        // Ajustar columnas de tablaDetalles
        ajustarColumnas(tablaDetalles);
    }

    private void ajustarColumnas(TableView<?> tableView) {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().forEach(column -> {
            column.setPrefWidth(100); // Ajusta el ancho inicial si es necesario
        });
    }

    private void cargarEntradas() {
        String queryEntradas = "SELECT " +
            "e.id, " +
            "e.fecha, " +
            "e.total, " +
            "c.id AS estado_id, " +
            "c.nombre_estado AS estado_nombre, " +
            "u.nombre AS usuario_nombre, " + // Obtener el nombre del usuario
            "e.created_at, " +
            "e.updated_at " +
        "FROM entrada e " +
        "JOIN cState c ON e.id_state = c.id " +
        "JOIN usuario u ON e.id_usuario = u.id"; // Unir con la tabla usuario

        ObservableList<Entrada> listaEntradas = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtEntradas = connection.prepareStatement(queryEntradas);
             ResultSet rsEntradas = stmtEntradas.executeQuery()) {

            while (rsEntradas.next()) {
                // Crear instancia de Entrada
                Entrada entrada = new Entrada(
                    rsEntradas.getInt("id"),
                    rsEntradas.getTimestamp("fecha").toLocalDateTime(),
                    new Estado(rsEntradas.getInt("estado_id"), rsEntradas.getString("estado_nombre")),
                    rsEntradas.getDouble("total"),
                    rsEntradas.getString("usuario_nombre") // Agregar el nombre del usuario
                );

                listaEntradas.add(entrada);
            }

            // Asignar la lista al TableView
            tablaEntradas.setItems(listaEntradas);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void cargarDetalles(Entrada entradaSeleccionada) {
        String queryDetalles = "SELECT " +
            "de.id, " +
            "de.id_producto, " +
            "de.id_lote, " +
            "de.cantidad, " +
            "p.nombre AS producto_nombre, " +
            "p.codigo AS producto_codigo, " +
            "p.descripcion AS producto_descripcion, " +
            "p.categoria AS producto_categoria, " +
            "p.precio AS producto_precio, " +
            "p.costo AS producto_costo, " +
            "l.id AS lote_id, " +
            "l.cantidad AS lote_cantidad, " +
            "l.fecha_caducidad AS lote_fecha_caducidad, " +
            "l.fecha_entrada AS lote_fecha_entrada, " +
            "l.id_state AS lote_id_state " +
        "FROM detalle_entrada de " +
        "JOIN producto p ON de.id_producto = p.id " +
        "JOIN lote l ON de.id_lote = l.id " +
        "WHERE de.id_entrada = ?";
    
        ObservableList<DetalleEntrada> listaDetalles = FXCollections.observableArrayList();
    
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtDetalles = connection.prepareStatement(queryDetalles)) {
    
            // Establecer el parámetro de la consulta (id de la entrada seleccionada)
            stmtDetalles.setInt(1, entradaSeleccionada.getId());
    
            try (ResultSet rsDetalles = stmtDetalles.executeQuery()) {
                while (rsDetalles.next()) {
                    // Crear instancia de Producto
                    Producto producto = new Producto(
                        rsDetalles.getString("producto_nombre"),
                        rsDetalles.getString("producto_codigo"),
                        rsDetalles.getString("producto_descripcion"),
                        rsDetalles.getString("producto_categoria"),
                        rsDetalles.getDouble("producto_precio"),
                        rsDetalles.getDouble("producto_costo")
                    );
    
                    // Crear instancia de Lote
                    Lote lote = new Lote(
                        rsDetalles.getInt("lote_id"),
                        rsDetalles.getInt("id_producto"), // idProducto del lote
                        rsDetalles.getInt("lote_cantidad"),
                        rsDetalles.getDate("lote_fecha_caducidad"),
                        rsDetalles.getDate("lote_fecha_entrada"),
                        rsDetalles.getInt("lote_id_state")
                    );
    
                    // Crear instancia de DetalleEntrada
                    DetalleEntrada detalle = new DetalleEntrada();
                    detalle.setId(rsDetalles.getInt("id"));
                    detalle.setProducto(producto);  // Asignar el producto
                    detalle.setLote(lote);          // Asignar el lote
                    detalle.setCantidad(rsDetalles.getInt("cantidad"));
    
                    // Agregar a la lista de detalles
                    listaDetalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        // Asignar la lista de detalles al TableView de detalles
        tablaDetalles.setItems(listaDetalles);
    }

    private void actualizarTotal() {
        double total = 0;
    
        // Recorrer la lista de detalles de la entrada seleccionada
        for (DetalleEntrada detalle : tablaDetalles.getItems()) {
            // Obtener el costo del producto y la cantidad
            double costoProducto = detalle.getProducto().getCosto();
            int cantidad = detalle.getCantidad();
    
            // Sumar el subtotal de cada detalle (precio * cantidad)
            total += costoProducto * cantidad;
        }
    
        // Mostrar el total en el label
        totalLabel.setText(String.format("%.2f", total)); // Formatear a 2 decimales
    }
    
    private void setupBotones() {
        // Evento para el botón de cancelar
        btnCancelar.setOnMouseClicked(event -> handleCancelar(event));
    }

    private void handleCancelar(MouseEvent event) {
        limpiar();
    }

    @FXML
    private void handleCambiar() {
        // Paso 1: Validar si se ha seleccionado una entrada
        Entrada entradaSeleccionada = tablaEntradas.getSelectionModel().getSelectedItem();
        if (entradaSeleccionada == null) {
            // Si no se selecciona ninguna entrada, mostrar una advertencia
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("No se ha seleccionado ninguna entrada");
            alert.setContentText("Por favor, seleccione una entrada para cambiar su estado.");
            alert.showAndWait();
            return;
        }

        // Paso 2: Advertir al usuario sobre el cambio de estado
        Alert confirmacionAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacionAlert.setTitle("Confirmación");
        confirmacionAlert.setHeaderText("¿Estás seguro de cambiar el estado de esta entrada?");
        confirmacionAlert.setContentText("Este cambio no se puede deshacer.");

        // Botones de la alerta de confirmación
        ButtonType buttonAceptar = new ButtonType("Aceptar");
        ButtonType buttonCancelar = new ButtonType("Cancelar");
        confirmacionAlert.getButtonTypes().setAll(buttonAceptar, buttonCancelar);

        // Mostrar alerta de confirmación
        Optional<ButtonType> resultado = confirmacionAlert.showAndWait();
        if (resultado.isPresent() && resultado.get() == buttonAceptar) {

            // Paso 3: Obtener los estados desde la base de datos
            ObservableList<String> listaEstados = FXCollections.observableArrayList();
            String queryEstados = "SELECT nombre_estado FROM cState";

            try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(queryEstados);
                ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    listaEstados.add(rs.getString("nombre_estado"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Crear el ComboBox con los estados posibles
            ComboBox<String> comboBoxEstados = new ComboBox<>(listaEstados);
            comboBoxEstados.setValue(entradaSeleccionada.getEstado().getNombre()); // Preestablecer el estado actual

            // Crear el alert con el ComboBox
            Alert estadoAlert = new Alert(Alert.AlertType.INFORMATION);
            estadoAlert.setTitle("Seleccionar nuevo estado");
            estadoAlert.setHeaderText("Selecciona un nuevo estado para la entrada");
            estadoAlert.getDialogPane().setContent(comboBoxEstados);

            // Botón de Aceptar
            ButtonType buttonGuardar = new ButtonType("Guardar");
            ButtonType buttonCancelarEstado = new ButtonType("Cancelar");
            estadoAlert.getButtonTypes().setAll(buttonGuardar, buttonCancelarEstado);

            // Mostrar alerta para elegir el nuevo estado
            Optional<ButtonType> resultadoEstado = estadoAlert.showAndWait();
            if (resultadoEstado.isPresent() && resultadoEstado.get() == buttonGuardar) {

                // Paso 4: Obtener el nuevo estado seleccionado
                String nuevoEstado = comboBoxEstados.getValue();

                // Actualizar el estado en el objeto Entrada
                entradaSeleccionada.getEstado().setNombre(nuevoEstado);

                // Actualizar el estado en la base de datos
                actualizarEstadoEnBaseDeDatos(entradaSeleccionada.getId(), nuevoEstado);

                // Actualizar la tabla
                tablaEntradas.refresh();

                mostrarAlerta("Informacion", "Se eliminó el rol", Alert.AlertType.CONFIRMATION);
            }
        }
    }

    private void actualizarEstadoEnBaseDeDatos(int idEntrada, String nuevoEstado) {
        String queryActualizarEstado = "UPDATE entrada SET id_state = (SELECT id FROM cState WHERE nombre_estado = ?) WHERE id = ?";
    
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(queryActualizarEstado)) {
    
            // Establecer parámetros en la consulta
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idEntrada);
    
            // Ejecutar la actualización
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                // Estado actualizado exitosamente
            } else {
                mostrarAlerta("Error", "No se encontró la entrada para actualizar", Alert.AlertType.ERROR);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void filtrarPorFecha() {
        LocalDate fechaDesde = datePickerDesde.getValue();
        LocalDate fechaHasta = datePickerHasta.getValue();

        if (fechaDesde == null || fechaHasta == null) {
            mostrarAlerta("Error", "Por favor selecciona ambas fechas.", Alert.AlertType.ERROR);
            return;
        }

        String queryFiltrar = "SELECT " +
            "e.id, " +
            "e.fecha, " +
            "e.total, " +
            "c.id AS estado_id, " +
            "c.nombre_estado AS estado_nombre, " +
            "u.nombre AS usuario_nombre, " +
            "e.created_at, " +
            "e.updated_at " +
        "FROM entrada e " +
        "JOIN cState c ON e.id_state = c.id " +
        "JOIN usuario u ON e.id_usuario = u.id " +
        "WHERE e.updated_at BETWEEN ? AND ?";

        ObservableList<Entrada> listaFiltrada = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtFiltrar = connection.prepareStatement(queryFiltrar)) {

            stmtFiltrar.setTimestamp(1, Timestamp.valueOf(fechaDesde.atStartOfDay()));
            stmtFiltrar.setTimestamp(2, Timestamp.valueOf(fechaHasta.atTime(23, 59, 59)));

            try (ResultSet rsFiltrar = stmtFiltrar.executeQuery()) {
                while (rsFiltrar.next()) {
                    Entrada entrada = new Entrada(
                        rsFiltrar.getInt("id"),
                        rsFiltrar.getTimestamp("fecha").toLocalDateTime(),
                        new Estado(rsFiltrar.getInt("estado_id"), rsFiltrar.getString("estado_nombre")),
                        rsFiltrar.getDouble("total"),
                        rsFiltrar.getString("usuario_nombre")
                    );
                    listaFiltrada.add(entrada);
                }
            }

            tablaEntradas.setItems(listaFiltrada);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error al filtrar las entradas.", Alert.AlertType.ERROR);
        }
    }

    private void limpiar(){
        totalLabel.setText("0.00"); // Restablecer el total
        tablaEntradas.getSelectionModel().clearSelection(); // Limpiar la selección de la tabla de entradas
        tablaDetalles.getItems().clear(); // Limpiar la tabla de detalles
    }

    public void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}

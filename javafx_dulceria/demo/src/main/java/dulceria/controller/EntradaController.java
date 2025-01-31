package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.model.DetalleEntrada;
import dulceria.model.Entrada;
import dulceria.model.Lote;
import dulceria.model.Producto;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;

public class EntradaController {

    // FXML Elements
    @FXML
    private DatePicker fechaEntradaPicker;
    @FXML
    private ComboBox<Producto> productoComboBox;
    @FXML
    private TextField cantidadTextField;
    @FXML
    private DatePicker fechaCaducidadPicker;
    @FXML
    private Button agregarButton;
    @FXML
    private TableView<DetalleEntrada> Entradas;
    @FXML
    private TableColumn<DetalleEntrada, String> productoColumn;
    @FXML
    private TableColumn<DetalleEntrada, Double> precioColumn;
    @FXML
    private TableColumn<DetalleEntrada, Integer> cantidadColumn;
    @FXML
    private TableColumn<DetalleEntrada, Double> costoColumn;
    @FXML
    private Label totalLabel;
    @FXML
    private CheckBox checkSinCaducidad;
    @FXML
    private Button guardarButton;

    private DatabaseConnection dbConnection;

    // List to store products added
    private List<Producto> productosAgregados = new ArrayList<>();

    private ObservableList<Producto> productoList = FXCollections.observableArrayList();  // Lista de productos
    private ObservableList<DetalleEntrada> detallesEntrada = FXCollections.observableArrayList();
    private DetalleEntrada detalleSeleccionado; // Detalle seleccionado para modificar
    

    @FXML
    private void initialize() {
        fechaEntradaPicker.setValue(LocalDate.now());
        CargarProdComboBox();
        configurarMenuTabla();

        // Configurar columnas
        productoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        precioColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getProducto().getPrecio()).asObject());
        cantidadColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getCantidad()).asObject());
        costoColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getProducto().getCosto()).asObject());

    
        checkSinCaducidad.setOnAction(event -> {
            if (checkSinCaducidad.isSelected()) {
                fechaCaducidadPicker.setDisable(true);
                fechaCaducidadPicker.setValue(null); // Limpiar la fecha
            } else {
                fechaCaducidadPicker.setDisable(false);
            }
        });

        // Asignar la lista de detalles a la tabla
        Entradas.setItems(detallesEntrada);

    }

    private void configurarMenuTabla(){
        // Crear el ContextMenu
        ContextMenu contextMenu = new ContextMenu();

        // Opción de Modificar
        MenuItem modificarItem = new MenuItem("Modificar");
        modificarItem.setOnAction(event -> modificarDetalle());

        // Opción de Eliminar
        MenuItem eliminarItem = new MenuItem("Eliminar");
        eliminarItem.setOnAction(event -> eliminarDetalleSeleccionado());

        // Opción de editar producto
        MenuItem modificarProd = new MenuItem("Edición Producto");
        modificarProd.setOnAction(event -> editarPrecioCosto());

        // Añadir las opciones al ContextMenu
        contextMenu.getItems().addAll(modificarItem, eliminarItem, modificarProd);

        // Configurar el menú contextual para los elementos de la TableView
        Entradas.setContextMenu(contextMenu);
        
        // Configurar acción de clic derecho en cada fila de la TableView
        Entradas.setRowFactory(tv -> {
        TableRow<DetalleEntrada> row = new TableRow<>();
        
        // Establecer el menú contextual en cada fila de la tabla
        row.setContextMenu(contextMenu);

        // Retornar la fila
        return row;
    });

    }

    private void eliminarDetalleSeleccionado() {
        DetalleEntrada detalleSeleccionado = Entradas.getSelectionModel().getSelectedItem();
    
        if (detalleSeleccionado != null) {
            // Eliminar el detalle de la lista
            detallesEntrada.remove(detalleSeleccionado);
    
            // Actualizar el total
            actualizarTotal();
            Limpiar();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección inválida", "Debe seleccionar un detalle para eliminar.");
        }
    }
    
    
    private void modificarDetalle() {
        // Obtener el detalle seleccionado de la tabla
        DetalleEntrada detalleSeleccionado = Entradas.getSelectionModel().getSelectedItem();
    
        if (detalleSeleccionado != null) {
            // Cargar los datos del detalle seleccionado en los campos del formulario
            productoComboBox.setValue(detalleSeleccionado.getProducto());
            cantidadTextField.setText(String.valueOf(detalleSeleccionado.getCantidad()));
            fechaCaducidadPicker.setValue(detalleSeleccionado.getLote().getFechaCaducidad().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            fechaEntradaPicker.setValue(detalleSeleccionado.getLote().getFechaEntrada().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            
            // Eliminar el detalle de la tabla (se eliminará temporalmente, ya que lo vamos a agregar después con los nuevos valores)
            detallesEntrada.remove(detalleSeleccionado);
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección inválida", "Debe seleccionar un detalle para modificar.");
        }
    }
    
    // Método para manejar la opción de "Editar Precio/Costo"
    private void editarPrecioCosto() {
        // Obtener el detalle seleccionado
        detalleSeleccionado = Entradas.getSelectionModel().getSelectedItem();

        if (detalleSeleccionado != null) {
            // Mostrar un cuadro de entrada para editar el precio y el costo
            mostrarCuadroEditarPrecioCosto();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección inválida", "Debe seleccionar un detalle para modificar.");
        }
    }

    // Método para mostrar el cuadro de edición del precio y costo
    private void mostrarCuadroEditarPrecioCosto() {
        // Crear un cuadro de texto para ingresar el nuevo precio y costo
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Actualizar Precio y Costo");
        dialog.setHeaderText("Ingrese los nuevos valores de Precio y Costo");

        // Formato para ingresar los valores
        dialog.setContentText("Nuevo Precio y Costo (separados por coma):");

        Optional<String> resultado = dialog.showAndWait();

        if (resultado.isPresent()) {
            try {
                // Obtener los nuevos valores de precio y costo
                String[] valores = resultado.get().split(",");
                if (valores.length != 2) {
                    throw new IllegalArgumentException("Debe ingresar ambos valores.");
                }

                double nuevoPrecio = Double.parseDouble(valores[0].trim());
                double nuevoCosto = Double.parseDouble(valores[1].trim());

                // Validar los valores (puedes agregar validaciones adicionales)
                if (nuevoPrecio <= 0 || nuevoCosto <= 0) {
                    throw new IllegalArgumentException("El precio y el costo deben ser mayores que cero.");
                }

                // Actualizar el producto en el detalle seleccionado
                detalleSeleccionado.getProducto().setPrecio(nuevoPrecio);
                detalleSeleccionado.getProducto().setCosto(nuevoCosto);

                // Actualizar el precio y costo en el detalle (Lote)
                detalleSeleccionado.getProducto().setPrecio(nuevoPrecio);
                detalleSeleccionado.getProducto().setCosto(nuevoCosto);

                // Actualizar la base de datos (aquí asumes que tienes una capa de servicio o repositorio)
                actualizarBaseDeDatos(detalleSeleccionado);

                // Actualizar la tabla y el total
                Entradas.refresh();
                actualizarTotal();

                // Mostrar éxito
                mostrarAlerta(Alert.AlertType.INFORMATION, "Actualización Exitosa", "El precio y el costo del producto han sido actualizados.");
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Entrada inválida: " + e.getMessage());
            }
        }
    }

    // Método para actualizar la base de datos
    private void actualizarBaseDeDatos(DetalleEntrada detalle) {
        // productoService.actualizarProducto(detalle.getProducto());
        Producto p = detalle.getProducto();

        String sql = "UPDATE producto SET precio = ?, costo = ? WHERE id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, p.getPrecio());
                stmt.setDouble(2, p.getCosto());
                stmt.setInt(3, p.getId());

                stmt.executeUpdate();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado exitosamente");
            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error al actualizar el producto: " + e.getMessage());
            }
    }

    private void Limpiar(){
        productoComboBox.getSelectionModel().clearSelection(); // Limpia la selección del ComboBox
        cantidadTextField.clear(); // Limpia el texto ingresado
        fechaCaducidadPicker.setValue(null); // Limpia la fecha seleccionada
    }

    // Método que se llama cuando se hace click en "Agregar"
    @FXML
    private void agregarDetalle() {
        // Validación de la entrada de datos
        Producto productoSeleccionado = productoComboBox.getValue();
        String cantidadTexto = cantidadTextField.getText();
        Date fechaEntrada = Date.from(fechaEntradaPicker.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date fechaCaducidad = null;

        // Validar que se haya seleccionado un producto
        if (productoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Debe seleccionar un producto.");
            return;
        }

        // Validar que los campos no estén vacíos
        if (cantidadTexto.isEmpty() || fechaEntrada == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Debe completar todos los campos.");
            return;
        }

        // Validar que la cantidad sea un número entero positivo
        int cantidad = 0;
        try {
            cantidad = Integer.parseInt(cantidadTexto);
            if (cantidad <= 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número válido.");
            return;
        }

        if (!checkSinCaducidad.isSelected()) { // Solo validar si el producto tiene fecha de caducidad
            fechaCaducidad = Date.from(fechaCaducidadPicker.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            if (fechaEntrada.after(fechaCaducidad)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "La fecha de entrada no puede ser posterior a la fecha de caducidad.");
                return;
            }
        }

        // Crear el objeto Lote
        Lote l = new Lote(
            0,
            productoSeleccionado.getId(),
            cantidad,
            fechaCaducidad,
            fechaEntrada,
            1 // disponible
        );

        // Crear el objeto DetalleEntrada
        DetalleEntrada de = new DetalleEntrada(
            0,
            null,
            productoSeleccionado,
            l,
            cantidad
        );

        // Agregar el detalle de entrada a la lista
        detallesEntrada.add(de);
        actualizarTotal();
        Limpiar();
    }

    // Método para manejar el botón de guardar
    @FXML
    private void guardarEntrada(ActionEvent event) {
        // Aquí puedes realizar las acciones para guardar o procesar la entrada
    }

    // Método para manejar el botón de cancelar
    @FXML
    private void cancelarEntrada(ActionEvent event) {
        // Limpiar los campos y la tabla
        fechaEntradaPicker.setValue(null);
        fechaCaducidadPicker.setValue(null);
        cantidadTextField.clear();
        productoComboBox.getSelectionModel().clearSelection();
        Entradas.getItems().clear();
        totalLabel.setText("0.00");
        productosAgregados.clear();
    }

    private void actualizarTotal() {
        double total = 0.0;
    
        // Recorremos todos los detalles de entrada y sumamos el total
        for (DetalleEntrada detalle : detallesEntrada) {
            Producto producto = detalle.getProducto(); // Obtén el producto
            int cantidad = detalle.getCantidad(); // Obtén la cantidad del detalle
            double costo = producto.getCosto(); // Obtén el precio del producto
    
            // Sumar al total (Precio * Cantidad)
            total += costo * cantidad;
        }
    
        // Actualizar el texto del label con el total calculado
        totalLabel.setText(String.format("%.2f", total));
    }

    private void CargarProdComboBox() {
        try (Connection conn = dbConnection.getConnection()) {
            // Cargar productos desde la base de datos
            String sqlProducto = "SELECT id, nombre, codigo, descripcion, categoria, precio, costo FROM producto";
            Statement stmt = conn.createStatement();
            ResultSet rsProducto = stmt.executeQuery(sqlProducto);
            while (rsProducto.next()) {
                // Crear productos y agregarlos a la lista
                Producto producto = new Producto(
                    rsProducto.getInt("id"),
                    rsProducto.getString("nombre"),
                    rsProducto.getString("codigo"),
                    rsProducto.getString("descripcion"),
                    rsProducto.getString("categoria"),
                    rsProducto.getDouble("precio"),
                    rsProducto.getDouble("costo")
                );
                productoList.add(producto);
            }
            productoComboBox.setItems(productoList);

            // Configuramos el ComboBox para mostrar tanto el ID como el nombre
            productoComboBox.setCellFactory(param -> new ListCell<Producto>() {
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
            productoComboBox.setButtonCell(new ListCell<Producto>() {
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
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar ComboBox "+ e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    public void guardarEntrada_bd() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Iniciar transacción
            connection.setAutoCommit(false);  // Desactivar autocommit para manejar la transacción manualmente

            try {
                // Paso 1: Insertar los lotes y obtener sus IDs
                for (DetalleEntrada detalle : detallesEntrada) {
                    Lote lote = detalle.getLote();

                    // Insertar lote
                    String insertLoteSQL = "INSERT INTO lote (id_producto, cantidad, fecha_caducidad, fecha_entrada, id_state) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmtLote = connection.prepareStatement(insertLoteSQL, Statement.RETURN_GENERATED_KEYS)) {
                        pstmtLote.setInt(1, lote.getIdProducto());
                        pstmtLote.setInt(2, lote.getCantidad());
                        if (lote.getFechaCaducidad() != null) {
                            pstmtLote.setDate(3, new java.sql.Date(lote.getFechaCaducidad().getTime()));
                        } else {
                            pstmtLote.setNull(3, java.sql.Types.DATE); // Si es null, se inserta como NULL en la BD
                        }                        
                        pstmtLote.setTimestamp(4, new Timestamp(lote.getFechaEntrada().getTime()));
                        pstmtLote.setInt(5, lote.getIdState());

                        pstmtLote.executeUpdate();
                        ResultSet rs = pstmtLote.getGeneratedKeys();
                        if (rs.next()) {
                            lote.setId(rs.getInt(1)); // Asignar el ID del lote recién insertado
                        }
                    }
                }

                // Paso 2: Insertar entrada
                String insertEntradaSQL = "INSERT INTO entrada (fecha, total, id_state) VALUES (?, ?, ?)";
                int entradaId = 0; // Variable para almacenar el ID de la entrada recién insertada
                try (PreparedStatement pstmtEntrada = connection.prepareStatement(insertEntradaSQL, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtEntrada.setTimestamp(1, new Timestamp(System.currentTimeMillis()));  // Fecha actual
                    pstmtEntrada.setDouble(2, Double.parseDouble(totalLabel.getText()));  // Asumimos que tienes un método para calcular el total
                    pstmtEntrada.setInt(3, 1);  // El estado de la entrada ("disponible")

                    pstmtEntrada.executeUpdate();
                    ResultSet rsEntrada = pstmtEntrada.getGeneratedKeys();
                    if (rsEntrada.next()) {
                        entradaId = rsEntrada.getInt(1);  // Obtener el ID de la entrada recién insertada
                    }
                }

                // Paso 3: Insertar detalles de entrada
                String insertDetalleSQL = "INSERT INTO detalle_entrada (id_entrada, id_producto, id_lote, cantidad) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmtDetalle = connection.prepareStatement(insertDetalleSQL)) {
                    for (DetalleEntrada detalle : detallesEntrada) {
                        pstmtDetalle.setInt(1, entradaId);  // Usamos el ID de la entrada recién insertada
                        pstmtDetalle.setInt(2, detalle.getProducto().getId());
                        pstmtDetalle.setInt(3, detalle.getLote().getId());
                        pstmtDetalle.setInt(4, detalle.getCantidad());
                        pstmtDetalle.addBatch();  // Agregar a batch para optimizar
                    }

                    pstmtDetalle.executeBatch();  // Ejecutar todos los inserts en un solo batch
                }

                // Paso 4: Confirmar la transacción
                connection.commit();
                mostrarAlerta(Alert.AlertType.CONFIRMATION, "Información", "Entrada guardada exitosamente.");
                detallesEntrada.clear();
                totalLabel.setText(String.format("%.2f", 0.0));

            } catch (SQLException e) {
                // Si ocurre algún error, hacer rollback
                connection.rollback();
                mostrarAlerta(Alert.AlertType.ERROR,"Error","Error al guardar la entrada: " + e.getMessage());
            }

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR,"Error","Error de conexión a la base de datos: " + e.getMessage());
        }
    }

}

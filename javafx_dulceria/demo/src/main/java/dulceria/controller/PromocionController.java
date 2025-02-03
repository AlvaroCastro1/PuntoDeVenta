package dulceria.controller;

import dulceria.model.Promocion;
import dulceria.model.Usuario;
import dulceria.model.Producto;
import dulceria.DatabaseConnection;
import dulceria.app.App;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.util.Optional;

public class PromocionController {

    // Tabla de promociones
    @FXML
    private TableView<Promocion> tblPromociones;
    @FXML
    private TableColumn<Promocion, Integer> colPromocionID;
    @FXML
    private TableColumn<Promocion, String> colPromocionNombre;
    @FXML
    private TableColumn<Promocion, String> colProducto;
    @FXML
    private TableColumn<Promocion, String> colPromocionTipo;
    @FXML
    private TableColumn<Promocion, Double> colPromocionDescuento;
    @FXML
    private TableColumn<Promocion, String> colPromocionActivo;

    // Detalles de promoción
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private TextField txtValorDescuento;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtBusqueda;
    @FXML
    private CheckBox chkActivo;

    // Productos asociados
    @FXML
    private ComboBox<Producto> cmbProducto;
    @FXML
    private TextField txtCantidadNecesaria;
    @FXML
    private Label totalLabel;

    private ObservableList<Promocion> listaPromociones;
    private ObservableList<Producto> listaProductos;
    private Promocion promocionSeleccionada;
    private Usuario usuario;

    @FXML
    public void initialize() {
        usuario = App.getUsuarioAutenticado();
        // Inicializar listas
        listaPromociones = FXCollections.observableArrayList();
        listaProductos = FXCollections.observableArrayList(); // Llenar con productos disponibles

        // Configurar tabla de promociones
        colPromocionID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPromocionNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProducto.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colPromocionTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colPromocionDescuento.setCellValueFactory(new PropertyValueFactory<>("valorDescuento"));
        colPromocionActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colPromocionActivo.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().isActivo() ? "Activo" : "Inactivo");
        });

        cmbTipo.setItems(FXCollections.observableArrayList("Descuento", "2x1", "Combo"));
        tblPromociones.setItems(listaPromociones);

        // Listener para cuando se seleccione un producto en el ComboBox
        cmbProducto.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && tblPromociones.getSelectionModel().getSelectedItem() == null) {
                // Establecer el precio del producto seleccionado
                txtPrecio.setText(String.valueOf(newValue.getPrecio()));
                actualizarPrecioTotal();
            } else {
                txtPrecio.clear();
            }
        });

        // Listener para cambios en txtCantidadNecesaria
        txtCantidadNecesaria.textProperty().addListener((observable, oldValue, newValue) -> {
            if (tblPromociones.getSelectionModel().getSelectedItem() == null) {
                actualizarPrecioTotal(); // Actualizar el precio total
            }
        });

        // Listener para seleccionar una promoción existente
        tblPromociones.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarDetallesPromocion(newValue); // Cargar detalles si hay una promoción seleccionada
            }
        });

        // Listener para el ComboBox de tipos de promoción
        cmbTipo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (tblPromociones.getSelectionModel().getSelectedItem() == null) {
                configurarDescuentoPorTipo(newValue);
            }
        });

        // Listener para cambios en txtValorDescuento
        txtValorDescuento.textProperty().addListener((observable, oldValue, newValue) -> {
            if (tblPromociones.getSelectionModel().getSelectedItem() == null) {
                actualizarPrecioConDescuento(newValue);
            }
        });

        CargarProdComboBox();
        cargarPromociones();

        // Envolver la lista en un FilteredList
    FilteredList<Promocion> filteredData = new FilteredList<>(listaPromociones, p -> true);

    // Escuchar cambios en el campo de búsqueda
    txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
        filteredData.setPredicate(promocion -> {
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = newValue.toLowerCase();
            return promocion.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                   promocion.getTipo().toLowerCase().contains(lowerCaseFilter) ||
                   promocion.getTipo().toLowerCase().contains(lowerCaseFilter) ||
                   String.valueOf(promocion.getCantidadNecesaria()).contains(lowerCaseFilter) ||
                   String.valueOf(promocion.getPrecioFinal()).contains(lowerCaseFilter) ||
                   String.valueOf(promocion.getValorDescuento()).contains(lowerCaseFilter);
        });
    });

    // Enlazar la lista filtrada con una SortedList
    SortedList<Promocion> sortedData = new SortedList<>(filteredData);
    sortedData.comparatorProperty().bind(tblPromociones.comparatorProperty());

    // Asignar los datos a la tabla
    tblPromociones.setItems(sortedData);

    }


    private void cargarPromociones() {
        listaPromociones.clear();
        // Consulta SQL para obtener todas las promociones
        String query = "SELECT p.id, p.id_producto, p.nombre, p.tipo, p.valor_descuento, p.precio_final, " +
                       "p.cantidad_necesaria, p.activo, p.created_at, p.updated_at, " +
                       "pr.id AS id_producto, pr.nombre AS nombre_producto, pr.codigo AS codigo_producto, " +
                       "pr.precio AS precio_producto, pr.costo AS costo_producto " +
                       "FROM promocion p " +
                       "JOIN producto pr ON p.id_producto = pr.id";
    
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                // Obtener los datos de cada promoción
                int id = rs.getInt("id");
                int idProducto = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                String tipo = rs.getString("tipo");
                double valorDescuento = rs.getDouble("valor_descuento");
                double precioFinal = rs.getDouble("precio_final");
                int cantidadNecesaria = rs.getInt("cantidad_necesaria");
                boolean activo = rs.getBoolean("activo");
    
    
                // Crear el objeto Producto
                Producto producto = new Producto(
                    idProducto,
                    rs.getString("nombre_producto"),
                    rs.getString("codigo_producto"),
                    "",  // Se asume que no necesitas estos valores por ahora
                    "",
                    rs.getDouble("precio_producto"),
                    rs.getDouble("costo_producto")
                );
    
                // Crear el objeto Promocion y agregarlo a la lista
                Promocion promocion = new Promocion(id, producto, nombre, tipo, valorDescuento, cantidadNecesaria, precioFinal, activo);
                listaPromociones.add(promocion);
            }
        } catch (SQLException e) {
            // Mostrar alerta con el error
            mostrarAlerta("Error", "Ocurrió un error al cargar las promociones: " + e.getMessage(), Alert.AlertType.ERROR);
            // También puedes loguear el error si es necesario:
            e.printStackTrace();  // o usar un logger como Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error cargando promociones", e);
        }
    }    

    @FXML
    private void guardarPromocion(ActionEvent event) {

        // Validar nombre de la promoción
        String nombrePromocion = txtNombre.getText();
        if (nombrePromocion == null || nombrePromocion.trim().isEmpty()) {
            mostrarAlerta("Información", "Por favor, ingresa un nombre para la promoción.", Alert.AlertType.WARNING);
            return;
        }
        // Validar producto
        Producto productoSeleccionado = cmbProducto.getValue();
        if (productoSeleccionado == null) {
            mostrarAlerta("Información", "Por favor, seleccione un producto para la promoción.", Alert.AlertType.WARNING);
            return;
        }

        // Validar tipo de promoción
        String tipoPromocion = cmbTipo.getValue();
        if (tipoPromocion == null || tipoPromocion.trim().isEmpty()) {
            mostrarAlerta("Información", "Por favor, selecciona un tipo de promoción.", Alert.AlertType.WARNING);
            return;
        }

        // Validar valor del descuento
        String descuentoTexto = txtValorDescuento.getText();
        double descuento;
        try {
            descuento = Double.parseDouble(descuentoTexto.replace("%", "").trim());
            if (descuento < 0 || descuento > 100) {
                mostrarAlerta("Información", "El descuento debe ser mayor a 0.", Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Información", "El descuento debe ser un valor numérico válido.", Alert.AlertType.WARNING);
            return;
        }
    
        // Validar precio final
        String precioTexto = txtPrecio.getText();
        double precio;
        try {
            precio = Double.parseDouble(precioTexto.trim());
            if (precio < 0) {
                mostrarAlerta("Información", "El precio debe ser mayor a 0.", Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Información", "El precio debe ser un valor numérico válido.", Alert.AlertType.WARNING);
            return;
        }
    
        // Validar cantidad necesaria
        String cantidadTexto = txtCantidadNecesaria.getText();
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto.trim());
            if (cantidad < 0) {
                mostrarAlerta("Información", "La cantidad debe ser mayor a 0.", Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Información", "La cantidad debe ser un valor numérico válido.", Alert.AlertType.WARNING);
            return;
        }

        if (!chkActivo.isSelected()){
            mostrarAlerta("Información", "La promocion debe estar activa.", Alert.AlertType.WARNING);
            return;
        }
    
        // Crear objeto de Promoción
        Promocion promocion = new Promocion(
            0,
            productoSeleccionado,
            nombrePromocion,
            tipoPromocion,
            descuento,
            cantidad,
            precio,
            chkActivo.isSelected()
        );
    
        // Guardar la promoción en la base de datos
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Iniciar una transacción
            connection.setAutoCommit(false);
    
            try {
                // 1. Desactivar todas las promociones activas para este producto
                String desactivarSQL = "UPDATE promocion SET activo = 0 WHERE id_producto = ?";
                try (PreparedStatement desactivarStmt = connection.prepareStatement(desactivarSQL)) {
                    desactivarStmt.setInt(1, productoSeleccionado.getId());
                    desactivarStmt.executeUpdate();  // No es necesario verificar filas afectadas
                }
    
                // 2. Insertar la nueva promoción
                String queryPromocion = "INSERT INTO promocion (id_producto, nombre, tipo, valor_descuento, precio_final, cantidad_necesaria, activo, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(queryPromocion, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, productoSeleccionado.getId());
                    stmt.setString(2, promocion.getNombre());
                    stmt.setString(3, promocion.getTipo());
                    stmt.setDouble(4, promocion.getValorDescuento());
                    stmt.setDouble(5, promocion.getPrecioFinal());
                    stmt.setInt(6, cantidad);  // Cantidad mínima necesaria
                    stmt.setBoolean(7, promocion.isActivo());  // Esta es la nueva promoción activa
                    stmt.setInt(8, usuario.getId());
    
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        connection.rollback();
                        mostrarAlerta("Error", "No se pudo guardar la promoción.", Alert.AlertType.ERROR);
                        return;
                    }
    
                    // Obtener el ID generado para la promoción
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int promocionId = generatedKeys.getInt(1);
                        promocion.setId(promocionId);
    
                        // Confirmar la transacción
                        connection.commit();
    
                        // Actualizar la lista de promociones
                        listaPromociones.add(promocion);
                        mostrarAlerta("Éxito", "Promoción guardada exitosamente y promociones anteriores desactivadas.", Alert.AlertType.INFORMATION);
                        limpiarFormularioPromocion();
                    } else {
                        connection.rollback();
                        mostrarAlerta("Error", "No se pudo obtener el ID de la promoción.", Alert.AlertType.ERROR);
                    }
                }
    
            } catch (SQLException e) {
                connection.rollback();
                mostrarAlerta("Error", "Ocurrió un error al guardar la promoción: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo establecer la conexión con la base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        limpiarFormularioPromocion();
        cargarPromociones();
    }

    @FXML
    private void cambiarEstadoPromocion() {
        // Obtener la promoción seleccionada
        promocionSeleccionada = tblPromociones.getSelectionModel().getSelectedItem();

        if (promocionSeleccionada == null) {
            mostrarAlerta("Información", "Por favor, selecciona una promoción para cambiar su estado.", Alert.AlertType.WARNING);
            return;
        }

        // Confirmar la acción con el usuario
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmación");
        confirmacion.setHeaderText("Cambiar estado de la promoción");
        confirmacion.setContentText("¿Estás seguro de que deseas cambiar el estado de esta promoción?");
        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Cambiar estado
            boolean nuevoEstado = !promocionSeleccionada.isActivo();
            promocionSeleccionada.setActivo(nuevoEstado);

            // Actualizar en la base de datos
            actualizarEstadoPromocion(promocionSeleccionada);

            // Refrescar la tabla
            tblPromociones.refresh();

            mostrarAlerta("Información", "El estado de la promoción se ha actualizado correctamente.", Alert.AlertType.INFORMATION);
        }
    }

    private void actualizarEstadoPromocion(Promocion promocion) {
        String desactivarTodasSql = "UPDATE promocion SET activo = false WHERE id_producto = ?";
        String activarSeleccionadaSql = "UPDATE promocion SET activo = true WHERE id = ?";
    
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);  // Iniciar transacción
    
            // Desactivar todas las promociones del producto
            try (PreparedStatement desactivarStmt = connection.prepareStatement(desactivarTodasSql)) {
                desactivarStmt.setInt(1, promocion.getProducto().getId());
                desactivarStmt.executeUpdate();
            }
    
            // Si la promoción seleccionada debe activarse, la activamos después de desactivar todas
            if (promocion.isActivo()) {
                try (PreparedStatement activarStmt = connection.prepareStatement(activarSeleccionadaSql)) {
                    activarStmt.setInt(1, promocion.getId());
                    activarStmt.executeUpdate();
                }
            }
    
            connection.commit();  // Confirmar cambios
    
            // Mensajes informativos
            if (promocion.isActivo()) {
                mostrarAlerta("Información", "La promoción ha sido activada y las demás promociones del producto fueron desactivadas.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Información", "Todas las promociones del producto han sido desactivadas.", Alert.AlertType.INFORMATION);
            }
    
        } catch (SQLException e) {
            mostrarAlerta("Error", "Ocurrió un error al actualizar el estado de las promociones: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        cargarPromociones();
    }

    private void cargarDetallesPromocion(Promocion promocionSeleccionada) {
        limpiarFormularioPromocion();
        if (promocionSeleccionada != null) {
            // Rellenar los campos del formulario con la información de la promoción seleccionada
            txtNombre.setText(promocionSeleccionada.getNombre());
            for (Producto producto : listaProductos) {
                if (producto.getId() == promocionSeleccionada.getProducto().getId()) {
                    cmbProducto.setValue(producto); // Establecer el producto seleccionado
                    break;
                }
            }
            cmbTipo.setValue(promocionSeleccionada.getTipo());
            txtValorDescuento.setText(String.valueOf(promocionSeleccionada.getValorDescuento()) + "%"); // Si es porcentaje
            txtPrecio.setText(String.valueOf(promocionSeleccionada.getPrecioFinal()));
            txtCantidadNecesaria.setText(String.valueOf(promocionSeleccionada.getCantidadNecesaria()));
            chkActivo.setSelected(promocionSeleccionada.isActivo());
            
        } else {
            mostrarAlerta("Información", "No se ha seleccionado ninguna promoción.", Alert.AlertType.WARNING);
        }
    }
    
    @FXML
    private void cancelarPromocion(ActionEvent event) {
        limpiarFormularioPromocion();
    }

    // Método para actualizar el precio total basado en cantidad y precio del producto
    private void actualizarPrecioTotal() {
        try {
            int cantidad = 1; // Valor por defecto
            // Verificar si el campo no está vacío
            String texto = txtCantidadNecesaria.getText().trim();
            if (!texto.isEmpty()) {
                try {
                    cantidad = Integer.parseInt(texto);
                } catch (NumberFormatException e) {
                    // Si la conversión falla, dejamos el valor en 1
                    cantidad = 1;
                }
            }
            Producto productoSeleccionado = cmbProducto.getValue();
            if (productoSeleccionado != null && cantidad > 0) {
                double precioProducto = productoSeleccionado.getPrecio();
                double precioTotal = precioProducto * cantidad;
                txtPrecio.setText(String.format("%.2f", precioTotal)); // Formatear a dos decimales
            }
        } catch (NumberFormatException e) {
            txtPrecio.setText("0.00");
        }
    }

    // Método para configurar el descuento dependiendo del tipo de promoción
    private void configurarDescuentoPorTipo(String tipoPromocion) {
        if (tipoPromocion == null || tipoPromocion.trim().isEmpty()) {
            txtValorDescuento.setText("0%");
            txtPrecio.setText("0.00");
            return;
        }
        switch (tipoPromocion) {
            case "Descuento":
                txtValorDescuento.setText("5%");
                break;
            case "2x1":
                txtValorDescuento.setText("50%");
                break;
            case "Combo":
                txtValorDescuento.setText("3%");
                break;
            default:
                txtValorDescuento.setText("0%");
                break;
        }
        actualizarPrecioConDescuento(txtValorDescuento.getText());
    }

    // Método para actualizar el precio aplicando un descuento
    private void actualizarPrecioConDescuento(String descuentoStr) {
        try {
            double descuento = 0.0; // Valor por defecto

            // Verificar si la cadena no está vacía
            if (!descuentoStr.isEmpty()) {
                // Eliminar el símbolo de porcentaje y hacer el trim
                descuentoStr = descuentoStr.replace("%", "").trim();
                
                try {
                    // Intentar convertir a double
                    descuento = Double.parseDouble(descuentoStr);
                } catch (NumberFormatException e) {
                    // Si la conversión falla, dejamos el descuento en 0.0
                    descuento = 0.0;
                }
            }
            Producto productoSeleccionado = cmbProducto.getValue();
            if(productoSeleccionado!= null){

                int cantidad = 1; // Valor por defecto
                // Verificar si el campo no está vacío
                String texto = txtCantidadNecesaria.getText().trim();
                if (!texto.isEmpty()) {
                    try {
                        cantidad = Integer.parseInt(texto);
                    } catch (NumberFormatException e) {
                        // Si la conversión falla, dejamos el valor en 1
                        cantidad = 1;
                    }
                }

                double precioTotal = productoSeleccionado.getPrecio() * cantidad;
                if (descuento >= 0 && descuento <= 100) {
                    double precioConDescuento = precioTotal - (precioTotal * descuento / 100);
                    txtPrecio.setText(String.format("%.2f", precioConDescuento)); // Actualizar precio con descuento
                }
            } else {
                txtPrecio.setText("0.00");
            }
        } catch (NumberFormatException e) {
            // Si el valor no es válido, limpiar el campo
            txtPrecio.setText("0.00");
        }
    }

    public double calcularTotal(){
        return promocionSeleccionada.getProducto().getPrecio() * Integer.parseInt(txtCantidadNecesaria.getText().trim());
    }

    private void CargarProdComboBox() {
        try (Connection conn = DatabaseConnection.getConnection()) {
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
                listaProductos.add(producto);
            }
            cmbProducto.setItems(listaProductos);

            // Configuramos el ComboBox para mostrar tanto el ID como el nombre
            cmbProducto.setCellFactory(param -> new ListCell<Producto>() {
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
            cmbProducto.setButtonCell(new ListCell<Producto>() {
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
            mostrarAlerta("Error","Error al cargar ComboBox "+ e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void limpiarFormularioPromocion() {
        txtNombre.clear();
        cmbTipo.getSelectionModel().clearSelection();
        txtValorDescuento.clear();
        cmbProducto.getSelectionModel().clearSelection();
        txtPrecio.clear();
        chkActivo.setSelected(false);
        tblPromociones.refresh();
        txtCantidadNecesaria.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}

package dulceria.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dulceria.DatabaseConnection;
import dulceria.app.App;
import dulceria.app.ImpresoraTicket;
import dulceria.model.Producto;
import dulceria.model.Promocion;
import dulceria.model.Usuario;
import dulceria.model.VentaProducto;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

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

    private Usuario usuario;

    private ContextMenu menuSugerencias;

    @FXML
    public void initialize() {
        usuario = App.getUsuarioAutenticado();
        configurarContextMenu();
        // Configurar columnas de la tabla
        colConsecutivo.setCellValueFactory(new PropertyValueFactory<>("num"));
        colProducto.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNombre()));
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

        configurarLectorCodigoBarras();

        // Capturar eventos de clic en la escena
        tablaVenta.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                    Platform.runLater(() -> txtCodigoBarras.requestFocus());
                });
            }
        });

         // Listener para cuando se abre/cierra el ComboBox
        comboProducto.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (!isNowShowing) {
                Platform.runLater(() -> txtCodigoBarras.requestFocus());
            }
        });
        
        
        configurarFocoAutomatico();
        actualizarTotal();
    }

    @FXML
    private TextField txtCodigoBarras;

    private void configurarLectorCodigoBarras() {
        menuSugerencias = new ContextMenu();

        txtCodigoBarras.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                menuSugerencias.hide();
                return;
            }

            // Convertir el texto ingresado a minúsculas para una comparación insensible a mayúsculas
            String textoIngresado = newValue.toLowerCase();

            // Filtrar productos que coincidan con el texto ingresado (en código o nombre)
            List<Producto> coincidencias = productos.stream()
                .filter(p -> p.getCodigo().toLowerCase().contains(textoIngresado) || 
                             p.getNombre().toLowerCase().contains(textoIngresado))
                .collect(Collectors.toList());

            if (coincidencias.isEmpty()) {
                menuSugerencias.hide();
            } else {
                // Crear elementos del menú con las coincidencias
                menuSugerencias.getItems().clear();
                for (Producto producto : coincidencias) {
                    MenuItem item = new MenuItem(producto.getCodigo() + " - " + producto.getNombre());
                    item.setOnAction(event -> {
                        txtCodigoBarras.setText(producto.getCodigo());
                        buscarProductoPorCodigo(producto.getCodigo());
                        txtCodigoBarras.clear();
                        menuSugerencias.hide();
                    });
                    menuSugerencias.getItems().add(item);
                }

                // Mostrar el menú debajo del campo de texto
                if (!menuSugerencias.isShowing()) {
                    javafx.geometry.Bounds bounds = txtCodigoBarras.localToScreen(txtCodigoBarras.getBoundsInLocal());
                    if (bounds != null) {
                        menuSugerencias.show(txtCodigoBarras, bounds.getMinX(), bounds.getMaxY());
                    }
                }
            }
        });

        txtCodigoBarras.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String codigo = txtCodigoBarras.getText().trim();
                if (!codigo.isEmpty()) {
                    buscarProductoPorCodigo(codigo);
                    txtCodigoBarras.clear();
                    menuSugerencias.hide();
                }
                event.consume();
                Platform.runLater(() -> txtCodigoBarras.requestFocus());
            }
        });
    }

    private void configurarFocoAutomatico() {
        txtCodigoBarras.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !comboProducto.isShowing()) { // Solo recuperar foco si el ComboBox no está abierto
                Platform.runLater(() -> {
                    if (tablaVenta.getScene() != null) { // Verificar que la escena no sea null
                        Node focusedNode = tablaVenta.getScene().getFocusOwner();
                        if (focusedNode != null && !esComponenteVenta(focusedNode)) {
                            txtCodigoBarras.requestFocus();
                        }
                    }
                });
            }
        });
    }
    
    private boolean esComponenteVenta(Node node) {
        return node == comboProducto || 
               node == txtPrecioUnitario || 
               node == tablaVenta || 
               node.getParent() == comboProducto.getEditor(); // Considerar el editor interno del ComboBox
    }
    
    // Método para buscar producto por código
    private void buscarProductoPorCodigo(String codigo) {
        System.out.println(productos);
        Optional<Producto> producto = productos.stream()
            .filter(p -> codigo.equals(p.getCodigo()))
            .findFirst();

        if (producto.isPresent()) {
            comboProducto.setValue(producto.get());
            agregarProductoSeleccionado();
        } else {
            mostrarAlerta("Error", "Producto no encontrado con código: " + codigo, Alert.AlertType.ERROR);
        }
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
            mostrarAlerta("Error", "Error al cargar ComboBox: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void agregarProducto(ActionEvent event) {
        agregarProductoSeleccionado();
    }

    public void agregarProductoSeleccionado(){
        // Validar que se haya seleccionado un producto
        Producto productoSeleccionado = comboProducto.getValue();

        int cantidad_producto = 0;
        for (VentaProducto ventaProducto : listaVenta) {
            if(productoSeleccionado.getId() == ventaProducto.getProducto().getId() ){
                cantidad_producto += ventaProducto.getCantidad();
            }
        }

        System.out.println(cantidad_producto);

        if (productoSeleccionado == null) {
            mostrarAlerta("Error", "Seleccione un producto antes de agregar.", Alert.AlertType.ERROR);
            return;
        }
    
        // Validar existencia del producto en la base de datos
        int existenciasDisponibles = obtenerExistenciasProducto(productoSeleccionado.getId());
        if (existenciasDisponibles <= 0) {
            mostrarAlerta("Error", "El producto seleccionado no tiene existencias disponibles.", Alert.AlertType.ERROR);
            return;
        }
    
        // Calcular la cantidad total ya añadida al detalle de la venta, considerando productos y promociones
        int cantidadEnDetalle = listaVenta.stream()
            .filter(vp -> vp.getProducto().getId() == productoSeleccionado.getId() && !vp.isPromocion())
            .mapToInt(VentaProducto::getCantidad)
            .sum();
        
        System.out.println(productoSeleccionado.getNombre() + " - " + cantidadEnDetalle);
    
        // Validar si se puede añadir al detalle sin exceder las existencias
        if (cantidad_producto >= existenciasDisponibles) {
            mostrarAlerta("Error", "No se pueden añadir más unidades de este producto. Existencias disponibles: " + existenciasDisponibles, Alert.AlertType.ERROR);
            return;
        }
    
        // Verificar si ya existe en la lista y actualizar su cantidad
        Optional<VentaProducto> productoExistente = listaVenta.stream()
            .filter(vp -> vp.getProducto().getId() == productoSeleccionado.getId() && !vp.isPromocion())
            .findFirst();
    
        if (productoExistente.isPresent()) {
            VentaProducto ventaProducto = productoExistente.get();
            int nuevaCantidad = ventaProducto.getCantidad() + 1;
    
            if (nuevaCantidad > existenciasDisponibles) {
                mostrarAlerta("Error", "No puedes agregar más de las existencias disponibles: " + existenciasDisponibles, Alert.AlertType.ERROR);
                return;
            }
    
            ventaProducto.setCantidad(nuevaCantidad);
            ventaProducto.setTotal(nuevaCantidad * ventaProducto.getPrecioUnitario());
        } else {
            // Si no existe, añadir un nuevo producto con cantidad inicial de 1
            VentaProducto ventaProducto = new VentaProducto(
                listaVenta.size() + 1,
                productoSeleccionado,
                productoSeleccionado.getNombre(),
                1,
                productoSeleccionado.getPrecio(),
                false
            );
            listaVenta.add(ventaProducto);
        }
    
        // Verificar si hay promociones activas para el producto
        List<Promocion> promociones = obtenerPromocionesActivasParaProducto(productoSeleccionado);
        for (Promocion promocion : promociones) {
            if (productoSeleccionado.getId() == promocion.getProducto().getId()) {
                // Si la cantidad de productos añadidos alcanza la cantidad necesaria para la promoción
                if (cantidadEnDetalle + 1 >= promocion.getCantidadNecesaria()) {
                    // Si no se ha añadido aún la promoción, agregarla
                    Optional<VentaProducto> promocionExistente = listaVenta.stream()
                        .filter(vp -> vp.getProducto().getId() == promocion.getProducto().getId() && vp.isPromocion())
                        .findFirst();
    
                    // añadimos la promoción 
                    
                        // Eliminar los productos originales relacionados con la promoción si ya alcanzamos la cantidad necesaria
                        listaVenta.removeIf(vp -> vp.getProducto().getId() == productoSeleccionado.getId() && !vp.isPromocion());
    
                        // Añadir la promoción
                        VentaProducto ventaProductoPromocion = new VentaProducto(
                            listaVenta.size() + 1,
                            productoSeleccionado,  // Puede ser el mismo producto, pero este es para la promoción
                            "Promo \"" +promocion.getNombre()+"\" "+ promocion.getCantidadNecesaria() + " "+ promocion.getProducto().getNombre(),
                            promocion.getCantidadNecesaria(),  // La promoción se aplica solo una vez por cada vez que la condición se cumple
                            promocion.getProducto().getPrecio(),
                            true,  // Es una promoción
                            promocion.getPrecioFinal()
                        );
                        ventaProductoPromocion.setId_promocion(promocion.getId());
                        listaVenta.add(ventaProductoPromocion);
                    
                }
            }
        }
    
        actualizarTotal();
    }

    private List<Promocion> obtenerPromocionesActivasParaProducto(Producto producto) {
        List<Promocion> promociones = new ArrayList<>();
        String query = "SELECT id, id_producto, nombre, tipo, valor_descuento, precio_final, cantidad_necesaria, " + 
            "activo " + 
            "FROM promocion " + 
            "WHERE id_producto = ? " + 
            "AND activo = 1 " ;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
    
            statement.setInt(1, producto.getId());
            
            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Promocion promocion = new Promocion(
                        resultSet.getInt("id"),
                        producto,
                        resultSet.getString("nombre"),
                        resultSet.getString("tipo"),
                        resultSet.getDouble("valor_descuento"),
                        resultSet.getInt("cantidad_necesaria"),
                        resultSet.getDouble("precio_final"),
                        resultSet.getBoolean("activo")
                    );
                    promociones.add(promocion);
                }
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error al consultar las promociones activas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    
        return promociones;
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
            mostrarAlerta("Error", "Error al obtener existencias del producto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        return 0;
    }

    @FXML
    private void guardarVenta() {
        if (listaVenta.isEmpty()) {
            mostrarAlerta("Error", "No hay productos para vender", Alert.AlertType.ERROR);
            return;
        }
        
        Connection connection = null;
        PreparedStatement stmtVenta = null;
        PreparedStatement stmtDetalle = null;
        PreparedStatement stmtLote = null;
        PreparedStatement stmtPromocion = null;
        ResultSet generatedKeys = null;
        ResultSet loteResult = null;
        ResultSet promocionResult = null;
    
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false); // Inicia la transacción
    
            // 1. Insertar la venta
            String sqlVenta = "INSERT INTO venta (total, fecha, id_state, id_usuario) VALUES (?, ?, ?, ?)";
            stmtVenta = connection.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            stmtVenta.setDouble(1, calcularTotalVenta());
            stmtVenta.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmtVenta.setInt(3, 6); // Estado pagado
            stmtVenta.setInt(4, usuario.getId());
            int rowsInserted = stmtVenta.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("Error al guardar la venta. No se insertó ninguna fila.");
            }
    
            // Obtener el ID generado para la venta
            generatedKeys = stmtVenta.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Error al obtener el ID de la venta.");
            }
            int idVenta = generatedKeys.getInt(1);
    
            // 2. Insertar los detalles de la venta
            String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, id_lote, id_state, costo_unitario, precio_unitario, cantidad, id_promocion, descuento_aplicado, subtotal) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmtDetalle = connection.prepareStatement(sqlDetalle);
    
            String sqlLote = "SELECT id FROM lote WHERE id_producto = ? AND fecha_caducidad > NOW() ORDER BY fecha_caducidad ASC LIMIT 1";
            stmtLote = connection.prepareStatement(sqlLote);
    
            String sqlPromocion = "SELECT valor_descuento FROM promocion WHERE id = ?";
            stmtPromocion = connection.prepareStatement(sqlPromocion);
    
            for (VentaProducto ventaProducto : listaVenta) {
                // Buscar el lote con la fecha de caducidad más próxima
                stmtLote.setInt(1, ventaProducto.getProducto().getId());
                loteResult = stmtLote.executeQuery();
                Integer idLote = null;
                if (loteResult.next()) {
                    idLote = loteResult.getInt("id");
                }
    
                // Buscar el descuento aplicado desde la tabla promocion
                Double descuentoAplicado = 0.0;
                if (ventaProducto.getId_promocion() != 0) {
                    stmtPromocion.setInt(1, ventaProducto.getId_promocion());
                    promocionResult = stmtPromocion.executeQuery();
                    if (promocionResult.next()) {
                        descuentoAplicado = promocionResult.getDouble("valor_descuento");
                    }
                }
    
                // Insertar el detalle de la venta
                stmtDetalle.setInt(1, idVenta);
                stmtDetalle.setInt(2, ventaProducto.getProducto().getId());
                stmtDetalle.setObject(3, idLote); // Puede ser null si no hay lotes disponibles
                stmtDetalle.setInt(4, 6); // Estado pagado
                stmtDetalle.setDouble(5, ventaProducto.getProducto().getCosto());
                stmtDetalle.setDouble(6, ventaProducto.getProducto().getPrecio());
                stmtDetalle.setInt(7, ventaProducto.getCantidad());
                stmtDetalle.setObject(8, ventaProducto.getId_promocion() == 0 ? null : ventaProducto.getId_promocion()); // Puede ser null
                stmtDetalle.setDouble(9, descuentoAplicado);
                stmtDetalle.setDouble(10, ventaProducto.getTotal());
    
                stmtDetalle.addBatch();
    
                // Actualizar el stock del lote (si hay un lote asignado)
                if (idLote != null) {
                    String sqlActualizarLote = "UPDATE lote SET cantidad = cantidad - ? WHERE id = ?";
                    try (PreparedStatement stmtActualizarLote = connection.prepareStatement(sqlActualizarLote)) {
                        stmtActualizarLote.setInt(1, ventaProducto.getCantidad());
                        stmtActualizarLote.setInt(2, idLote);
                        stmtActualizarLote.executeUpdate();
                    }
                }
            }
    
            // Ejecutar los detalles de la venta en batch
            int[] rowsDetalles = stmtDetalle.executeBatch();
            System.out.println("Detalles guardados: " + rowsDetalles.length);
    
            // Confirmar la transacción
            connection.commit();
            mostrarAlerta("Éxito", "La venta se guardó correctamente con todos sus detalles.", Alert.AlertType.INFORMATION);
    
            // Generar e imprimir el ticket
            String contenidoTicket = generarContenidoTicket();
            imprimirTicket(contenidoTicket);
            
            mostrarAlerta("Éxito", "La venta se guardó correctamente y se imprimió el ticket.", Alert.AlertType.INFORMATION);
            // Limpiar la lista de la venta actual
            listaVenta.clear();
            actualizarTotal();
    
        } catch (SQLException e) {
            // Si algo falla, hacer rollback
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar la venta: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            // Cerrar recursos
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException ignored) {}
            if (loteResult != null) try { loteResult.close(); } catch (SQLException ignored) {}
            if (promocionResult != null) try { promocionResult.close(); } catch (SQLException ignored) {}
            if (stmtVenta != null) try { stmtVenta.close(); } catch (SQLException ignored) {}
            if (stmtDetalle != null) try { stmtDetalle.close(); } catch (SQLException ignored) {}
            if (stmtLote != null) try { stmtLote.close(); } catch (SQLException ignored) {}
            if (stmtPromocion != null) try { stmtPromocion.close(); } catch (SQLException ignored) {}
            if (connection != null) try { connection.close(); } catch (SQLException ignored) {}
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

    private void actualizarTotal() {
        double total = listaVenta.stream().mapToDouble(VentaProducto::getTotal).sum();
        lblTotal.setText(String.format("Total: $%.2f", total));
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
        MenuItem reducirCantidadItem = new MenuItem("Reducir cantidad");
    
        // Configurar la acción del menú "Eliminar"
        eliminarItem.setOnAction(event -> {
            VentaProducto productoSeleccionado = tablaVenta.getSelectionModel().getSelectedItem();
            if (productoSeleccionado != null) {
                tablaVenta.getItems().remove(productoSeleccionado); // Eliminar el producto seleccionado
                actualizarConsecutivos(); // Actualizar consecutivos después de eliminar
                actualizarTotal();
            }
        });
    
        // Configurar la acción del menú "Reducir cantidad"
        reducirCantidadItem.setOnAction(event -> {
            VentaProducto productoSeleccionado = tablaVenta.getSelectionModel().getSelectedItem();
            if (productoSeleccionado != null) {
                int cantidadActual = productoSeleccionado.getCantidad();
                
                if (cantidadActual > 1) {
                    // Reducir la cantidad del producto
                    productoSeleccionado.setCantidad(cantidadActual - 1);
    
                    // Si el producto es una promoción, desactivar el estado de promoción
                    if (productoSeleccionado.isPromocion()) {
                        productoSeleccionado.setPromocion(false);
                        productoSeleccionado.setNombre(productoSeleccionado.getProducto().getNombre());
                    }
    
                    tablaVenta.refresh(); // Refrescar la tabla para mostrar los cambios
                } else {
                    // Si la cantidad es 1, eliminar el producto de la tabla
                    tablaVenta.getItems().remove(productoSeleccionado);
                }
                actualizarTotal();
            }
        });
    
        contextMenu.getItems().addAll(reducirCantidadItem, eliminarItem);
    
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
    

    private String generarContenidoTicket() {
        StringBuilder ticket = new StringBuilder();
        
        // Detalles de los productos
        ticket.append(String.format("%-20s %5s %10s %10s\n", "Producto", "Cant.", "P. Unit.", "Subtotal"));
        for (VentaProducto item : listaVenta) {
            String nombre = item.getNombre().length() > 20 ? item.getNombre().substring(0, 17) + "..." : item.getNombre();
            ticket.append(String.format("%-20s %5d %10.2f %10.2f\n", 
                nombre,
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getTotal()));
            
            if(item.isPromocion()) {
                ticket.append("  (Promoción aplicada)\n");
            }
        }
        
        // Totales
        ticket.append("\n================================================\n");
        ticket.append(String.format("TOTAL: $%.2f\n", calcularTotalVenta()));
        ticket.append("================================================\n");
        ticket.append("¡Gracias por su compra!\n");
        
        return ticket.toString();
    }

    private void imprimirTicket(String contenido) {
        try {
            // Usar la clase ImpresoraTicket que ya tienes
            dulceria.app.ImpresoraTicket.imprimirTicket(contenido);
        } catch (Exception e) {
            mostrarAlerta("Advertencia", "La venta se guardó pero no se pudo imprimir el ticket: " + e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    
    
}

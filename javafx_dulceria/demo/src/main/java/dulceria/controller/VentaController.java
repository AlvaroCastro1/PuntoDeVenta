package dulceria.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dulceria.DatabaseConnection;
import dulceria.app.App;
import dulceria.model.Producto;
import dulceria.model.Promocion;
import dulceria.model.Usuario;
import dulceria.model.VentaProducto;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

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
    @FXML
    private Button btnGuardarVenta;
    private boolean imprimirTicket = false; // Variable para controlar la impresión del ticket

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
        
        tablaVenta.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        tablaVenta.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.CONTROL) {
                        imprimirTicket = !imprimirTicket; // Alternar el estado de imprimirTicket
                        if (!imprimirTicket) {
                            btnGuardarVenta.setStyle("-fx-background-color: #ffcc00;"); // Cambiar el color del botón
                        } else {
                            btnGuardarVenta.setStyle(""); // Restaurar el color original del botón
                        }
                    }
                });
            }
        });
        
        configurarFocoAutomatico();
        actualizarTotal();
    }

    @FXML
    private TextField txtCodigoBarras;

    @FXML
    private void configurarLectorCodigoBarras() {
        menuSugerencias = new ContextMenu();

        // Usar PauseTransition para reducir la frecuencia de búsqueda
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        txtCodigoBarras.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                menuSugerencias.hide();
                return;
            }

            pause.setOnFinished(event -> buscarCoincidencias(newValue.toLowerCase()));
            pause.playFromStart();
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

        // Crear un Timeline para validar constantemente si el campo está vacío
        Timeline validarCampoVacio = new Timeline(
            new KeyFrame(Duration.millis(100), event -> {
                if (txtCodigoBarras.getText().trim().isEmpty()) {
                    menuSugerencias.hide();
                }
            })
        );
        validarCampoVacio.setCycleCount(Timeline.INDEFINITE); // Repetir indefinidamente
        validarCampoVacio.play(); // Iniciar el Timeline
    }

    private void buscarCoincidencias(String textoIngresado) {
        Task<List<Producto>> task = new Task<>() {
            @Override
            protected List<Producto> call() {
                // Filtrar productos que coincidan con el texto ingresado
                return productos.stream()
                    .filter(p -> p.getCodigo().toLowerCase().contains(textoIngresado) ||
                                 p.getNombre().toLowerCase().contains(textoIngresado))
                    .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(event -> {
            List<Producto> coincidencias = task.getValue();
            if (coincidencias.isEmpty()) {
                menuSugerencias.hide();
            } else {
                menuSugerencias.getItems().clear();
                for (Producto producto : coincidencias) {
                    MenuItem item = new MenuItem(producto.getCodigo() + " - " + producto.getNombre());
                    item.setOnAction(e -> {
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
                        double x = bounds.getMinX(); // Coordenada X del campo de texto
                        double y = bounds.getMaxY(); // Coordenada Y justo debajo del campo de texto
                        menuSugerencias.show(txtCodigoBarras, x, y);
                    }
                }
            }
        });

        task.setOnFailed(event -> {
            menuSugerencias.hide();
            event.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
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


        if (productoSeleccionado == null) {
            mostrarAlerta("Error", "Seleccione un producto antes de agregar.", Alert.AlertType.ERROR);
            return;
        }
    
        // Validar existencia del producto en la base de datos
        // int existenciasDisponibles = obtenerExistenciasProducto(productoSeleccionado.getId());
        // if (existenciasDisponibles <= 0) {
        //     mostrarAlerta("Error", "El producto seleccionado no tiene existencias disponibles.", Alert.AlertType.ERROR);
        //     return;
        // }
    
        // Calcular la cantidad total ya añadida al detalle de la venta, considerando productos y promociones
        int cantidadEnDetalle = listaVenta.stream()
            .filter(vp -> vp.getProducto().getId() == productoSeleccionado.getId() && !vp.isPromocion())
            .mapToInt(VentaProducto::getCantidad)
            .sum();
        // Validar si se puede añadir al detalle sin exceder las existencias
        // if (cantidad_producto >= existenciasDisponibles) {
        //     mostrarAlerta("Error", "No se pueden añadir más unidades de este producto. Existencias disponibles: " + existenciasDisponibles, Alert.AlertType.ERROR);
        //     return;
        // }
    
        // Verificar si ya existe en la lista y actualizar su cantidad
        Optional<VentaProducto> productoExistente = listaVenta.stream()
            .filter(vp -> vp.getProducto().getId() == productoSeleccionado.getId() && !vp.isPromocion())
            .findFirst();
    
        if (productoExistente.isPresent()) {
            VentaProducto ventaProducto = productoExistente.get();
            int nuevaCantidad = ventaProducto.getCantidad() + 1;
    
            // if (nuevaCantidad > existenciasDisponibles) {
            //     mostrarAlerta("Error", "No puedes agregar más de las existencias disponibles: " + existenciasDisponibles, Alert.AlertType.ERROR);
            //     return;
            // }
    
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

        menuSugerencias.hide();
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

        double totalVenta = calcularTotalVenta();

        // Solicitar el monto pagado
        Optional<Double> montoPagadoOpt = solicitarMontoPagado(totalVenta);
        if (!montoPagadoOpt.isPresent()) {
            return; // Si no se ingresó un monto válido, cancelar la operación
        }

        double montoPagado = montoPagadoOpt.get();
        double cambio = montoPagado - totalVenta;

        Connection connection = null;
        PreparedStatement stmtVenta = null;
        PreparedStatement stmtDetalle = null;
        PreparedStatement stmtActualizarLote = null;
        PreparedStatement stmtActualizarCaja = null;
        PreparedStatement stmtMovimientoCaja = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false); // Inicia la transacción

            // Obtener el ID de la caja abierta
            String sqlCaja = "SELECT id, total_ingresos, total_egresos, total_ventas, total_final, base_inicial FROM caja WHERE estado = 'Abierta' LIMIT 1";
            int idCaja;
            double totalIngresos, totalEgresos, totalVentas, totalFinal, baseInicial;

            try (PreparedStatement stmtCaja = connection.prepareStatement(sqlCaja);
                 ResultSet rsCaja = stmtCaja.executeQuery()) {
                if (!rsCaja.next()) {
                    mostrarAlerta("Error", "No hay una caja abierta. No se puede guardar la venta.", Alert.AlertType.ERROR);
                    return;
                }
                idCaja = rsCaja.getInt("id");
                totalIngresos = rsCaja.getDouble("total_ingresos");
                totalEgresos = rsCaja.getDouble("total_egresos");
                totalVentas = rsCaja.getDouble("total_ventas");
                totalFinal = rsCaja.getDouble("total_final");
                baseInicial = rsCaja.getDouble("base_inicial");
            }

            // 1. Insertar la venta
            String sqlVenta = "INSERT INTO venta (total, fecha, id_state, id_usuario, id_caja) VALUES (?, ?, ?, ?, ?)";
            stmtVenta = connection.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            stmtVenta.setDouble(1, totalVenta);
            stmtVenta.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmtVenta.setInt(3, 6); // Estado pagado
            stmtVenta.setInt(4, usuario.getId());
            stmtVenta.setInt(5, idCaja);
            stmtVenta.executeUpdate();

            // Obtener el ID generado para la venta
            generatedKeys = stmtVenta.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Error al obtener el ID de la venta.");
            }
            int idVenta = generatedKeys.getInt(1);

            // 2. Insertar los detalles de la venta y actualizar lotes
            String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, id_lote, id_state, costo_unitario, precio_unitario, cantidad, id_promocion, descuento_aplicado, subtotal) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmtDetalle = connection.prepareStatement(sqlDetalle);

            String sqlObtenerLotes = "SELECT id, cantidad FROM lote WHERE id_producto = ? AND cantidad > 0 ORDER BY fecha_caducidad ASC";
            String sqlActualizarLote = "UPDATE lote SET cantidad = cantidad - ? WHERE id = ?";
            stmtActualizarLote = connection.prepareStatement(sqlActualizarLote);

            for (VentaProducto ventaProducto : listaVenta) {
                int cantidadRestante = ventaProducto.getCantidad();

                try (PreparedStatement stmtObtenerLotes = connection.prepareStatement(sqlObtenerLotes)) {
                    stmtObtenerLotes.setInt(1, ventaProducto.getProducto().getId());
                    try (ResultSet rsLotes = stmtObtenerLotes.executeQuery()) {
                        while (rsLotes.next() && cantidadRestante > 0) {
                            int idLote = rsLotes.getInt("id");
                            int cantidadLote = rsLotes.getInt("cantidad");

                            int cantidadADescontar = Math.min(cantidadRestante, cantidadLote);
                            cantidadRestante -= cantidadADescontar;

                            // Actualizar el lote
                            stmtActualizarLote.setInt(1, cantidadADescontar);
                            stmtActualizarLote.setInt(2, idLote);
                            stmtActualizarLote.executeUpdate();

                            // Insertar detalle de la venta con el lote utilizado
                            stmtDetalle.setInt(1, idVenta);
                            stmtDetalle.setInt(2, ventaProducto.getProducto().getId());
                            stmtDetalle.setInt(3, idLote);
                            stmtDetalle.setInt(4, 6); // Estado pagado
                            stmtDetalle.setDouble(5, ventaProducto.getProducto().getCosto());
                            stmtDetalle.setDouble(6, ventaProducto.getProducto().getPrecio());
                            stmtDetalle.setInt(7, cantidadADescontar);
                            stmtDetalle.setObject(8, null); // Promoción puede ser null
                            stmtDetalle.setDouble(9, 0.0); // Descuento aplicado
                            stmtDetalle.setDouble(10, cantidadADescontar * ventaProducto.getProducto().getPrecio());
                            stmtDetalle.addBatch();
                        }
                    }
                }

                // if (cantidadRestante > 0) {
                //     throw new SQLException("No hay suficiente stock para el producto: " + ventaProducto.getProducto().getNombre());
                // }
            }

            stmtDetalle.executeBatch();

            // 3. Actualizar los totales en la tabla caja
            String sqlActualizarCaja = "UPDATE caja SET total_ingresos = ?, total_egresos = ?, total_ventas = ?, total_final = ? WHERE id = ?";
            stmtActualizarCaja = connection.prepareStatement(sqlActualizarCaja);
            stmtActualizarCaja.setDouble(1, totalIngresos + montoPagado); // Actualizar total_ingresos con el monto pagado
            stmtActualizarCaja.setDouble(2, totalEgresos + cambio); // Actualizar total_egresos con el cambio
            stmtActualizarCaja.setDouble(3, totalVentas + totalVenta); // Actualizar total_ventas con la venta actual
            stmtActualizarCaja.setDouble(4, baseInicial + totalVentas + totalVenta); // Actualizar total_final como base + ventas
            stmtActualizarCaja.setInt(5, idCaja);
            stmtActualizarCaja.executeUpdate();

            // 4. Insertar un movimiento en la tabla movimientos_caja (Ingreso)
            String sqlMovimientoCaja = "INSERT INTO movimientos_caja (id_caja, tipo, monto, descripcion, id_usuario, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            stmtMovimientoCaja = connection.prepareStatement(sqlMovimientoCaja);
            stmtMovimientoCaja.setInt(1, idCaja);
            stmtMovimientoCaja.setString(2, "Ingreso");
            stmtMovimientoCaja.setDouble(3, montoPagado);
            stmtMovimientoCaja.setString(4, "Venta realizada");
            stmtMovimientoCaja.setInt(5, usuario.getId());
            stmtMovimientoCaja.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmtMovimientoCaja.executeUpdate();

            // 5. Insertar un movimiento en la tabla movimientos_caja (Egreso) si hay cambio
            if (cambio > 0) {
                stmtMovimientoCaja.setInt(1, idCaja);
                stmtMovimientoCaja.setString(2, "Egreso");
                stmtMovimientoCaja.setDouble(3, cambio);
                stmtMovimientoCaja.setString(4, "Cambio entregado");
                stmtMovimientoCaja.setInt(5, usuario.getId());
                stmtMovimientoCaja.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                stmtMovimientoCaja.executeUpdate();
            }

            // Confirmar la transacción
            connection.commit();
            mostrarAlerta("Éxito", String.format("La venta se guardó correctamente. Cambio: $%.2f", cambio), Alert.AlertType.INFORMATION);

            // Generar e imprimir el ticket
            if (imprimirTicket) {
                String contenidoTicket = generarContenidoTicket(montoPagado, cambio);
                imprimirTicket(contenidoTicket);
            } else {
                Alert resumenVenta = new Alert(Alert.AlertType.INFORMATION);
                resumenVenta.setTitle("Resumen de Venta");
                resumenVenta.setHeaderText("Venta Guardada Exitosamente (Sin Ticket)");
                resumenVenta.setContentText(String.format(
                    "Cantidad de Productos: %d\nTotal de la Venta: $%.2f\nCambio: $%.2f",
                    listaVenta.size(),
                    totalVenta,
                    cambio
                ));
                resumenVenta.showAndWait();
            }

            // Limpiar la lista de la venta actual
            listaVenta.clear();
            actualizarTotal();

        } catch (SQLException e) {
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
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException ignored) {}
            if (stmtVenta != null) try { stmtVenta.close(); } catch (SQLException ignored) {}
            if (stmtDetalle != null) try { stmtDetalle.close(); } catch (SQLException ignored) {}
            if (stmtActualizarLote != null) try { stmtActualizarLote.close(); } catch (SQLException ignored) {}
            if (stmtActualizarCaja != null) try { stmtActualizarCaja.close(); } catch (SQLException ignored) {}
            if (stmtMovimientoCaja != null) try { stmtMovimientoCaja.close(); } catch (SQLException ignored) {}
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
        MenuItem modificarTotalItem = new MenuItem("Modificar total"); // Nueva opción

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

        // Configurar la acción del menú "Modificar total"
        modificarTotalItem.setOnAction(event -> {
            VentaProducto productoSeleccionado = tablaVenta.getSelectionModel().getSelectedItem();
            if (productoSeleccionado != null) {
                TextInputDialog dialog = new TextInputDialog(String.format("%.2f", productoSeleccionado.getTotal()));
                dialog.setTitle("Modificar Subtotal");
                dialog.setHeaderText("Modificar el subtotal del producto");
                dialog.setContentText("Ingrese el nuevo subtotal:");

                // Validar que el valor ingresado sea un número válido
                dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                        dialog.getEditor().setText(oldValue);
                    }
                });

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    try {
                        double nuevoSubtotal = Double.parseDouble(result.get());
                        if (nuevoSubtotal <= 0) {
                            mostrarAlerta("Error", "El subtotal debe ser mayor a 0.", Alert.AlertType.ERROR);
                            return;
                        }

                        // Actualizar el subtotal directamente
                        productoSeleccionado.setTotal(nuevoSubtotal);

                        // Actualizar la lista de productos añadidos
                        int index = listaVenta.indexOf(productoSeleccionado);
                        if (index != -1) {
                            listaVenta.set(index, productoSeleccionado); // Actualizar el producto en la lista
                        }

                        tablaVenta.refresh(); // Refrescar la tabla para mostrar los cambios
                        actualizarTotal(); // Actualizar el total general
                    } catch (NumberFormatException e) {
                        mostrarAlerta("Error", "Ingrese un subtotal válido.", Alert.AlertType.ERROR);
                    }
                }
            }
        });

        contextMenu.getItems().addAll(reducirCantidadItem, modificarTotalItem, eliminarItem);

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

    private String generarContenidoTicket(double montoPagado, double cambio) {
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
        ticket.append(String.format("Monto pagado: $%.2f\n", montoPagado));
        ticket.append(String.format("Cambio: $%.2f\n", cambio));
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

    private Optional<Double> solicitarMontoPagado(double totalVenta) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Monto Pagado");
        dialog.setHeaderText("Ingrese el monto pagado por el cliente");
        dialog.setContentText(String.format("Total a pagar: $%.2f\nMonto pagado:", totalVenta));

        // Validar que el monto ingresado sea un número válido
        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                dialog.getEditor().setText(oldValue);
            }
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double montoPagado = Double.parseDouble(result.get());
                if (montoPagado < totalVenta) {
                    mostrarAlerta("Error", "El monto pagado no es suficiente para cubrir el total de la venta.", Alert.AlertType.ERROR);
                    return Optional.empty();
                }
                return Optional.of(montoPagado);
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Ingrese un monto válido.", Alert.AlertType.ERROR);
            }
        }
        return Optional.empty();
    }
}

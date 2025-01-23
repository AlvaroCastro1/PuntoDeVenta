package dulceria.model;

import javafx.beans.property.*;

public class VentaProducto {

    private final IntegerProperty num = new SimpleIntegerProperty();
    private Producto producto;
    private StringProperty nombre = new SimpleStringProperty();
    private final IntegerProperty cantidad = new SimpleIntegerProperty();
    private final DoubleProperty precioUnitario = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private boolean Promocion;
    private int id_promocion;

    public int getId_promocion() {
        return id_promocion;
    }

    public void setId_promocion(int id_promocion) {
        this.id_promocion = id_promocion;
    }

    public boolean isPromocion() {
        return Promocion;
    }

    public void setPromocion(boolean promocion) {
        Promocion = promocion;
    }

    public VentaProducto(int num, Producto producto, String nombre, int cantidad, double precioUnitario, boolean promocion) {
        setNum(num);
        setProducto(producto);
        setNombre(nombre);
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
        this.Promocion = promocion;
        calcularTotal();
    }

    // para promocion
    public VentaProducto(int num, Producto producto, String nombre, int cantidad, double precioUnitario, boolean promocion, double total) {
        setNum(num);
        setProducto(producto);
        setNombre(nombre);
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
        this.Promocion = promocion;
        setTotal(total);
        
    }
    // Getter y Setter
    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public Producto getProducto(){
        return producto;
    }

    private void setProducto(Producto p){
        this.producto = p;

    }
    private void calcularTotal() {
        setTotal(getCantidad() * getPrecioUnitario());
    }

    public int getNum() {
        return num.get();
    }

    public void setNum(int value) {
        num.set(value);
    }

    public IntegerProperty numProperty() {
        return num;
    }


    public int getCantidad() {
        return cantidad.get();
    }

    public void setCantidad(int value) {
        cantidad.set(value);
        calcularTotal(); // Recalcular el total si cambia la cantidad
    }

    public IntegerProperty cantidadProperty() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario.get();
    }

    public void setPrecioUnitario(double value) {
        precioUnitario.set(value);
    }

    public DoubleProperty precioUnitarioProperty() {
        return precioUnitario;
    }

    public double getTotal() {
        return total.get();
    }

    public void setTotal(double value) {
        total.set(value);
    }

    public DoubleProperty totalProperty() {
        return total;
    }
}

package dulceria.model;

import javafx.beans.property.*;

public class VentaProducto {
    private final IntegerProperty num = new SimpleIntegerProperty();
    private Producto producto;
    private final IntegerProperty cantidad = new SimpleIntegerProperty();
    private final DoubleProperty precioUnitario = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();

    public VentaProducto(int num, Producto producto, int cantidad, double precioUnitario) {
        setNum(num);
        setProducto(producto);
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
        calcularTotal();
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

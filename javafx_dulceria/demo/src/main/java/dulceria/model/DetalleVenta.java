package dulceria.model;

import javafx.beans.property.*;

public class DetalleVenta {
    private final IntegerProperty id;
    private Producto producto;
    private final IntegerProperty cantidad;
    private final IntegerProperty lote;
    private final DoubleProperty precioUnitario;
    private final DoubleProperty costoUnitario;
    private final IntegerProperty idState;

    public DetalleVenta(int id, Producto producto,  int cantidad, int lote, double precioUnitario, double costoUnitario, int idState) {
        this.id = new SimpleIntegerProperty(id);
        this.producto = producto;
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.lote = new SimpleIntegerProperty(lote);
        this.precioUnitario = new SimpleDoubleProperty(precioUnitario);
        this.costoUnitario = new SimpleDoubleProperty(costoUnitario);
        this.idState = new SimpleIntegerProperty(idState);
    }

    public int getId() {
        return id.get();
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad.get();
    }

    public IntegerProperty cantidadProperty() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad.set(cantidad);
    }

    public int getLote() {
        return lote.get();
    }

    public IntegerProperty loteProperty() {
        return lote;
    }

    public void setLote(int lote) {
        this.lote.set(lote);
    }

    public double getPrecioUnitario() {
        return precioUnitario.get();
    }

    public DoubleProperty precioUnitarioProperty() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario.set(precioUnitario);
    }

    public double getCostoUnitario() {
        return costoUnitario.get();
    }

    public DoubleProperty costoUnitarioProperty() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario.set(costoUnitario);
    }
    public int getIdState() {
        return idState.get();
    }

    public IntegerProperty estadoProperty() {
        return idState;
    }

    public void setEstado(int estado) {
        this.idState.set(estado);
    }
}

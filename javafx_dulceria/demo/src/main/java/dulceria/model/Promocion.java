package dulceria.model;

import javafx.beans.property.*;

public class Promocion {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty tipo = new SimpleStringProperty();
    private final DoubleProperty valorDescuento = new SimpleDoubleProperty();
    private final IntegerProperty cantidadNecesaria = new SimpleIntegerProperty();
    private final DoubleProperty precioFinal = new SimpleDoubleProperty();
    private final BooleanProperty activo = new SimpleBooleanProperty();
    private Producto producto;

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Promocion(int id, Producto p, String nombre, String tipo, double valorDescuento, int cantidadNecesaria,double precioFinal, boolean activo) {
        this.id.set(id);
        this.producto = p;
        this.nombre.set(nombre);
        this.tipo.set(tipo);
        this.valorDescuento.set(valorDescuento);
        this.cantidadNecesaria.set(cantidadNecesaria);
        this.precioFinal.set(precioFinal);
        this.activo.set(activo);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty tipoProperty() {
        return tipo;
    }

    public DoubleProperty valorDescuentoProperty() {
        return valorDescuento;
    }

    public IntegerProperty cantidadNecesariaProperty() {
        return id;
    }

    public DoubleProperty precioFinalProperty() {
        return precioFinal;
    }

    public BooleanProperty activoProperty() {
        return activo;
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public String getTipo() {
        return tipo.get();
    }

    public void setTipo(String tipo) {
        this.tipo.set(tipo);
    }

    public double getValorDescuento() {
        return valorDescuento.get();
    }

    public int getCantidadNecesaria() {
        return cantidadNecesaria.get();
    }

    public void setValorDescuento(double valorDescuento) {
        this.valorDescuento.set(valorDescuento);
    }

    public void setCantidadNecesaria(int cantidad_necesaria) {
        this.cantidadNecesaria.set(cantidad_necesaria);
    }

    public double getPrecioFinal() {
        return precioFinal.get();
    }

    public void setPrecioFinal(double precioFinal) {
        this.precioFinal.set(precioFinal);
    }

    public boolean isActivo() {
        return activo.get();
    }

    public void setActivo(boolean activo) {
        this.activo.set(activo);
    }
}

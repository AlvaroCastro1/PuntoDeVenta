package dulceria.model;

import java.util.Date;
import javafx.beans.property.*;

public class Lote {
    private IntegerProperty id;
    private IntegerProperty idProducto;
    private IntegerProperty cantidad;
    private ObjectProperty<Date> fechaCaducidad;

    @Override
    public String toString() {
    return "Lote [id=" + id.get() + ", idProducto=" + idProducto.get() + ", cantidad=" + cantidad.get() + 
           ", fechaCaducidad=" + fechaCaducidad.get() + ", fechaEntrada=" + fechaEntrada.get() + 
           ", idState=" + idState.get() + "]";
    }


    private ObjectProperty<Date> fechaEntrada;
    private IntegerProperty idState;

    // Constructor
    public Lote(){}

    public Lote(int id, int idProducto, int cantidad, Date fechaCaducidad, Date fechaEntrada, int idState) {
        this.id = new SimpleIntegerProperty(id);
        this.idProducto = new SimpleIntegerProperty(idProducto);
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.fechaCaducidad = new SimpleObjectProperty<>(fechaCaducidad);
        this.fechaEntrada = new SimpleObjectProperty<>(fechaEntrada);
        this.idState = new SimpleIntegerProperty(idState);
    }

    // Getters y Setters
    public IntegerProperty idProperty() {
        return id;
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProductoProperty() {
        return idProducto;
    }

    public int getIdProducto() {
        return idProducto.get();
    }

    public void setIdProducto(int idProducto) {
        this.idProducto.set(idProducto);
    }

    public IntegerProperty cantidadProperty() {
        return cantidad;
    }

    public int getCantidad() {
        return cantidad.get();
    }

    public void setCantidad(int cantidad) {
        this.cantidad.set(cantidad);
    }

    public ObjectProperty<Date> fechaCaducidadProperty() {
        return fechaCaducidad;
    }

    public ObjectProperty<Date> fechaEntradaProperty() {
        return fechaEntrada;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad.get();
    }

    public Date getFechaEntrada() {
        return fechaEntrada.get();
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad.set(fechaCaducidad);
    }

    public void setFechaEntrada(Date fechaEntrada) {
        this.fechaEntrada.set(fechaEntrada);
    }

    public IntegerProperty idStateProperty() {
        return idState;
    }

    public int getIdState() {
        return idState.get();
    }

    public void setIdState(int idState) {
        this.idState.set(idState);
    }
}

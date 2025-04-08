package dulceria.model;

import javafx.beans.property.*;

public class Venta {
    private final IntegerProperty id;
    private final StringProperty fecha;
    private final DoubleProperty total;
    private final StringProperty estado;
    private String usuario;

    // Constructor actualizado
    public Venta(int id, String fecha, double total, String estado, String usuario) {
        this.id = new SimpleIntegerProperty(id);
        this.fecha = new SimpleStringProperty(fecha);
        this.total = new SimpleDoubleProperty(total);
        this.estado = new SimpleStringProperty(estado);
        this.usuario = usuario;
    }

    public Venta(int id, String fecha, double total, String estado) {
        this.id = new SimpleIntegerProperty(id);
        this.fecha = new SimpleStringProperty(fecha);
        this.total = new SimpleDoubleProperty(total);
        this.estado = new SimpleStringProperty(estado);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getFecha() {
        return fecha.get();
    }

    public StringProperty fechaProperty() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha.set(fecha);
    }

    public double getTotal() {
        return total.get();
    }

    public DoubleProperty totalProperty() {
        return total;
    }

    public void setTotal(double total) {
        this.total.set(total);
    }

    public String getEstado() {
        return estado.get();
    }

    public StringProperty estadoProperty() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado.set(estado);
    }

    public String getUsuario() {
        return usuario;
    }
}

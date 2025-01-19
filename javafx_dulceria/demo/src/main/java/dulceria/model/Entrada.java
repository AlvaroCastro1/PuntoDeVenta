package dulceria.model;

import dulceria.model.Estado;
import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Entrada {


    private IntegerProperty id = new SimpleIntegerProperty();

    private ObjectProperty<LocalDateTime> fecha = new SimpleObjectProperty<>();

    private DoubleProperty total = new SimpleDoubleProperty();

    private Estado estado;

    private ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    private ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Constructor vacío para JPA
    public Entrada(){}

    public Entrada(int id, LocalDateTime fecha, Estado estado, double total) {
        this.id.set(id);
        this.fecha.set(fecha);
        this.estado = estado;
        this.total.set(total);
    }

    // Constructor con parámetros
    public Entrada(LocalDateTime fecha, double total, Estado estado, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.fecha.set(fecha);
        this.total.set(total);
        this.estado = estado;
        this.createdAt.set(createdAt);
        this.updatedAt.set(updatedAt);
    }

    // Getters y setters usando Properties
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha.get();
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha.set(fecha);
    }

    public ObjectProperty<LocalDateTime> fechaProperty() {
        return fecha;
    }

    public double getTotal() {
        return total.get();
    }

    public void setTotal(double total) {
        this.total.set(total);
    }

    public DoubleProperty totalProperty() {
        return total;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }
}

package dulceria.model;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class Permiso {
    private int id;
    private StringProperty nombre;
    private StringProperty descripcion;

    // Constructor
    public Permiso(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = new SimpleStringProperty(nombre);
        this.descripcion = new SimpleStringProperty(descripcion);
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public String getDescripcion() {
        return descripcion.get();
    }

    public void setDescripcion(String descripcion) {
        this.descripcion.set(descripcion);
    }

    // Métodos Property
    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty descripcionProperty() {
        return descripcion;
    }
}

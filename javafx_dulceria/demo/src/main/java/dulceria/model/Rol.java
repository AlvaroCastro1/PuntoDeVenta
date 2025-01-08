package dulceria.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Rol {
    private int id;
    private StringProperty nombreRol;
    private StringProperty descripcion;

    // Constructor
    public Rol(int id, String nombreRol, String descripcion) {
        this.id = id;
        this.nombreRol = new SimpleStringProperty(nombreRol);
        this.descripcion = new SimpleStringProperty(descripcion);
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreRol() {
        return nombreRol.get();
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol.set(nombreRol);
    }

    public StringProperty nombreRolProperty() {
        return nombreRol;
    }

    public String getDescripcion() {
        return descripcion.get();
    }

    public void setDescripcion(String descripcion) {
        this.descripcion.set(descripcion);
    }

    public StringProperty descripcionProperty() {
        return descripcion;
    }
}

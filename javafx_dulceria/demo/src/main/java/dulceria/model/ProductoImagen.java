package dulceria.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProductoImagen {
    private final IntegerProperty id;
    private final ObjectProperty<byte[]> imagen;
    private final StringProperty descripcion;  // Nueva propiedad para la descripción

    // Constructor
    public ProductoImagen(int id, byte[] imagen, String descripcion) {
        this.id = new SimpleIntegerProperty(id);
        this.imagen = new SimpleObjectProperty<>(imagen);
        this.descripcion = new SimpleStringProperty(descripcion);  // Inicializamos la descripción
    }

    // Getters y setters para el ID
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public byte[] getImagen() {
        return imagen.get();
    }

    public void setImagen(byte[] imagen) {
        this.imagen.set(imagen);
    }    

    // Getters y setters para la descripción
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

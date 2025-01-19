package dulceria.model;

public class Estado {
    private int id;
    private String nombre;

    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Constructor
    public Estado(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return id + " - " + nombre;  // Mostrar el ID y el nombre en el ComboBox
    }
}

package dulceria.model;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private double precio;
    private double costo;
    private int existencia;

    public Producto(int id, String nombre, String descripcion, String categoria, double precio, double costo, int existencia) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.costo = costo;
        this.existencia = existencia;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public double getCosto() {
        return costo;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    @Override
    public String toString() {
        return "Producto [id=" + id + ", nombre=" + nombre + ", descripcion=" + descripcion + ", categoria=" + categoria
                + ", precio=" + precio + ", costo=" + costo + ", existencia=" + existencia + "]";
    }
}

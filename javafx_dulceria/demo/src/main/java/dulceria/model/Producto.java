package dulceria.model;

import java.util.List;

public class Producto {
    private int id;
    private String nombre;
    private String codigo;
    private String descripcion;
    private String categoria;
    private double precio;
    private double costo;
    private List<ProductoImagen> imagenes;

    public Producto(String nombre, String codigo, String descripcion, String categoria, double precio, double costo) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.costo = costo;
    }

    public Producto(int id, String nombre, String codigo, String descripcion, String categoria, double precio, double costo) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.costo = costo;
    }
    

    public Producto(int id, String nombre, String descripcion, String categoria, double precio, double costo,
            List<ProductoImagen> imagenes) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.costo = costo;
        this.imagenes = imagenes;
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

    public List<ProductoImagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ProductoImagen> imagenes) {
        this.imagenes = imagenes;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return "Producto [id=" + id + ", nombre=" + nombre + ", descripcion=" + descripcion + ", categoria=" + categoria
                + ", precio=" + precio + ", costo=" + costo + ", codigo=" + codigo + "]";
    }
}

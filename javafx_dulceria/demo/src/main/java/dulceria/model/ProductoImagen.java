package dulceria.model;

public class ProductoImagen {
    private int id;
    private int productoId;
    private byte[] imagen;
    private String descripcion;

    public ProductoImagen(int id, int productoId, byte[] imagen, String descripcion) {
        this.id = id;
        this.productoId = productoId;
        this.imagen = imagen;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public int getProductoId() {
        return productoId;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

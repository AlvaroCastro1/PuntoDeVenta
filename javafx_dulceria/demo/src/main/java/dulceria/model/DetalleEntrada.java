package dulceria.model;

public class DetalleEntrada {

    private int id ;

    private Entrada entrada;

    private Producto producto;

    private Lote lote;

    private int cantidad ;

    @Override
    public String toString() {
        return "DetalleEntrada [id=" + id + ", entrada=" + entrada + ", producto=" + producto + ", lote=" + lote
                + ", cantidad=" + cantidad + "]";
    }


    // Constructor vacío para JPA
    public DetalleEntrada() {
    }

    public DetalleEntrada(int id, Entrada entrada, Producto producto, Lote lote, int cantidad) {
        this.id = id;
        this.entrada = entrada;
        this.producto = producto;
        this.lote = lote;
        this.cantidad = cantidad;
    }


    // Constructor con parámetros
    public DetalleEntrada(Entrada entrada, Producto producto, Lote lote, int cantidad) {
        this.entrada = entrada;
        this.producto = producto;
        this.lote = lote;
        this.cantidad = cantidad;
    }




    public Entrada getEntrada() {
        return entrada;
    }

    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public int getCantidad(){
        return cantidad;
    }

}

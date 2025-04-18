package dulceria.model;

import java.time.LocalDate;
import javafx.beans.property.*;

public class Perdida {
    private final IntegerProperty id;
    private final StringProperty nombreProducto;
    private final IntegerProperty idLote;
    private final IntegerProperty cantidad;
    private final DoubleProperty costoUnitario;
    private final DoubleProperty total;
    private LocalDate fecha; // Fecha como LocalDate


    public Perdida(int id, String nombreProducto, int idLote, int cantidad, double costoUnitario, LocalDate fecha) {
        this.id = new SimpleIntegerProperty(id);
        this.nombreProducto = new SimpleStringProperty(nombreProducto);
        this.idLote = new SimpleIntegerProperty(idLote);
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.costoUnitario = new SimpleDoubleProperty(costoUnitario);
        this.total = new SimpleDoubleProperty(cantidad * costoUnitario); // Total = cantidad * costo_unitario
        this.fecha = fecha;
    }

    // Getters and Setters

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nombreProductoProperty() {
        return nombreProducto;
    }

    public IntegerProperty idLoteProperty() {
        return idLote;
    }

    public IntegerProperty cantidadProperty() {
        return cantidad;
    }

    public DoubleProperty costoUnitarioProperty() {
        return costoUnitario;
    }

    public DoubleProperty totalProperty() {
        return total;
    }

    public int getId() {
        return id.get();
    }

    public String getNombreProducto() {
        return nombreProducto.get();
    }

    public int getIdLote() {
        return idLote.get();
    }

    public int getCantidad() {
        return cantidad.get();
    }

    public double getCostoUnitario() {
        return costoUnitario.get();
    }

    public double getTotal() {
        return total.get();
    }
}

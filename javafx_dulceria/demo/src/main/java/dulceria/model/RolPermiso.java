package dulceria.model;

public class RolPermiso {
    private int idRol;
    private String nombreRol;
    private int idPermiso;
    private String nombrePermiso;

    public RolPermiso(int idRol, String nombreRol, int idPermiso, String nombrePermiso) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.idPermiso = idPermiso;
        this.nombrePermiso = nombrePermiso;
    }

    // Getters y setters
    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }
    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
    public int getIdPermiso() { return idPermiso; }
    public void setIdPermiso(int idPermiso) { this.idPermiso = idPermiso; }
    public String getNombrePermiso() { return nombrePermiso; }
    public void setNombrePermiso(String nombrePermiso) { this.nombrePermiso = nombrePermiso; }
}

package dulceria.model;

public class UsuarioRol {
    private final String nombreUsuario;
    private final String nombreRol;

    public UsuarioRol(String nombreUsuario, String nombreRol) {
        this.nombreUsuario = nombreUsuario;
        this.nombreRol = nombreRol;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombreRol() {
        return nombreRol;
    }
}

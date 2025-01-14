package dulceria.model;

import javafx.beans.property.*;

public class Usuario {
    private IntegerProperty id;
    private StringProperty nombre;
    private StringProperty email;
    private StringProperty telefono;
    private StringProperty rol;
    private StringProperty contrasena;
    private BooleanProperty estado;

    // Constructor vacío
    public Usuario() {
        this.id = new SimpleIntegerProperty();
        this.nombre = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.telefono = new SimpleStringProperty();
        this.rol = new SimpleStringProperty();
        this.contrasena = new SimpleStringProperty();
        this.estado = new SimpleBooleanProperty();
    }

    // Constructor completo
    public Usuario(int id, String nombre, String email, String telefono, String contrasena, boolean estado) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.email = new SimpleStringProperty(email);
        this.telefono = new SimpleStringProperty(telefono);
        this.contrasena = new SimpleStringProperty(contrasena);
        this.estado = new SimpleBooleanProperty(estado);
    }

    public Usuario(int id, String nombre, String email, String telefono, boolean estado) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.email = new SimpleStringProperty(email);
        this.telefono = new SimpleStringProperty(telefono);
        this.estado = new SimpleBooleanProperty(estado);
    }

    public Usuario(int id, String nombre) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
    }

    // Getters y Setters
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getTelefono() {
        return telefono.get();
    }

    public void setTelefono(String telefono) {
        this.telefono.set(telefono);
    }

    public String getRol() {
        return rol.get();
    }

    public void setRol(String rol) {
        this.rol.set(rol);
    }

    public String getContrasena() {
        return contrasena.get();
    }

    public void setContrasena(String contrasena) {
        this.contrasena.set(contrasena);
    }

    public boolean isEstado() {
        return estado.get();
    }

    public void setEstado(boolean estado) {
        this.estado.set(estado);
    }

    // Métodos Property
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty telefonoProperty() {
        return telefono;
    }

    public StringProperty rolProperty() {
        return rol;
    }

    public StringProperty contrasenaProperty() {
        return contrasena;
    }

    public BooleanProperty estadoProperty() {
        return estado;
    }

    // Método toString (opcional, útil para depuración)
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id.get() +
                ", nombre='" + nombre.get() + '\'' +
                ", email='" + email.get() + '\'' +
                ", rol='" + rol.get() + '\'' +
                ", estado=" + estado.get() +
                '}';
    }
}

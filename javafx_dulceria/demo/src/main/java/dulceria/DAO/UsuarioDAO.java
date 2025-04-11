package dulceria.DAO;

import dulceria.DatabaseConnection;
import dulceria.model.Permiso;
import dulceria.model.Rol;
import dulceria.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UsuarioDAO {
    private Connection connection;
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioDAO() {
        try {
            connection = DatabaseConnection.getConnection();
            if (connection == null) {
                mostrarError("Error de conexión", "No se pudo establecer la conexión con la base de datos");
                throw new SQLException("No se pudo establecer la conexión");
            }
        } catch (SQLException e) {
            mostrarError("Error", "Error al inicializar UsuarioDAO: " + e.getMessage());
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private boolean validarUsuario(Usuario usuario, boolean esActualizacion) {
        if (usuario == null) {
            mostrarError("Error de validación", "El usuario no puede ser null");
            return false;
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            mostrarError("Error de validación", "El nombre es requerido");
            return false;
        }
        if (usuario.getEmail() == null || !usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            mostrarError("Error de validación", "Email inválido");
            return false;
        }
        if (usuario.getTelefono() == null || !usuario.getTelefono().matches("^[0-9]{10}$")) {
            mostrarError("Error de validación", "El teléfono debe tener 10 dígitos");
            return false;
        }
        // Solo validar contraseña si no es una actualización o si se está cambiando la contraseña
        if (!esActualizacion && (usuario.getContrasena() == null || usuario.getContrasena().length() < 6)) {
            mostrarError("Error de validación", "La contraseña debe tener al menos 6 caracteres");
            return false;
        }
        return true;
    }

    public boolean guardarUsuario(Usuario usuario) {
        if (!validarUsuario(usuario, false)) {
            return false;
        }

        String query = "INSERT INTO usuario (nombre, correo, telefono, contrasena, estado) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefono());
            String contrasenaEncriptada = encoder.encode(usuario.getContrasena());
            stmt.setString(4, contrasenaEncriptada);
            stmt.setBoolean(5, usuario.isEstado());
            
            boolean resultado = stmt.executeUpdate() > 0;
            if (resultado) {
                mostrarInformacion("Éxito", "Usuario guardado correctamente");
            }
            return resultado;
        } catch (SQLException e) {
            mostrarError("Error", "Error al guardar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarUsuario(Usuario usuario) {
        System.out.println(usuario);
        if (!validarUsuario(usuario, true)) {
            return false;
        }

        String query = "UPDATE usuario SET nombre = ?, correo = ?, telefono = ?, estado = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefono());
            stmt.setBoolean(4, usuario.isEstado());
            stmt.setInt(5, usuario.getId());
            
            boolean resultado = stmt.executeUpdate() > 0;
            if (resultado) {
                mostrarInformacion("Éxito", "Usuario actualizado correctamente");
            } else {
                mostrarError("Error", "No se encontró el usuario para actualizar");
            }
            return resultado;
        } catch (SQLException e) {
            mostrarError("Error", "Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }    

    public boolean eliminarUsuario(int id) {
        String query = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            mostrarError("Error", "Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
    
    public List<Usuario> obtenerTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, nombre, correo, telefono, estado FROM usuario");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                usuarios.add(new Usuario(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("correo"),
                    rs.getString("telefono"),
                    rs.getBoolean("estado")
                ));
            }
        } catch (Exception e) {
            mostrarError("Error", "Error al obtener todos los usuarios: " + e.getMessage());
        }

        return usuarios;
    }
    
    public boolean cambiarContrasena(int usuarioId, String nuevaContrasena) {
        String query = "UPDATE usuario SET contrasena = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String contrasenaEncriptada = encoder.encode(nuevaContrasena);
            stmt.setString(1, contrasenaEncriptada);
            stmt.setInt(2, usuarioId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            mostrarError("Error", "Error al cambiar contraseña: " + e.getMessage());
            return false;
        }
    }

    public Usuario validarCredenciales(String email, String contrasenaIngresada) {
        if (email == null || email.trim().isEmpty() || contrasenaIngresada == null || contrasenaIngresada.trim().isEmpty()) {
            mostrarError("Error de validación", "Email y contraseña son requeridos");
            return null;
        }

        String query = "SELECT id, nombre, correo, telefono, estado, contrasena FROM usuario WHERE correo = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String contrasenaEncriptada = rs.getString("contrasena");
                
                if (encoder.matches(contrasenaIngresada, contrasenaEncriptada)) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setEmail(rs.getString("correo"));
                    usuario.setTelefono(rs.getString("telefono"));
                    usuario.setEstado(rs.getBoolean("estado"));
                    
                    obtenerRoles(usuario);
                    obtenerPermisos(usuario);
                    
                    return usuario;
                } else {
                    mostrarError("Error de autenticación", "Contraseña incorrecta");
                }
            } else {
                mostrarError("Error de autenticación", "Usuario no encontrado");
            }
        } catch (SQLException e) {
            mostrarError("Error", "Error al validar credenciales: " + e.getMessage());
        }
        
        return null;
    }
    
    private void obtenerRoles(Usuario usuario) {
        String queryRoles = "SELECT r.id, r.nombre, r.descripcion\n"+
            "FROM rol r\n"+
            "JOIN usuario_rol ur ON ur.id_rol = r.id\n"+
            "WHERE ur.id_usuario = ?;\n";
        
        try (PreparedStatement stmtRoles = connection.prepareStatement(queryRoles)) {
            stmtRoles.setInt(1, usuario.getId());
            ResultSet rsRoles = stmtRoles.executeQuery();
            
            List<Rol> roles = new ArrayList<>();
            while (rsRoles.next()) {
                Rol rol = new Rol(
                    rsRoles.getInt("id"),
                    rsRoles.getString("nombre"),
                    rsRoles.getString("descripcion")
                );
                
                roles.add(rol);
            }
            usuario.setRoles(roles); // Establecer los roles en el objeto Usuario
        } catch (SQLException e) {
            mostrarError("Error", "Error al obtener roles: " + e.getMessage());
        }
    }
    
    private void obtenerPermisos(Usuario usuario) {
        String queryPermisos = "SELECT p.id, p.nombre, p.descripcion\n"+
            "FROM permiso p\n"+
            "JOIN rol_permiso rp ON rp.id_permiso = p.id\n"+
            "JOIN rol r ON r.id = rp.id_rol\n"+
            "WHERE r.id IN (\n"+
            "    SELECT ur.id_rol\n"+
            "    FROM usuario_rol ur\n"+
            "    WHERE ur.id_usuario = ?\n"+
            ");\n";
        
        try (PreparedStatement stmtPermisos = connection.prepareStatement(queryPermisos)) {
            stmtPermisos.setInt(1, usuario.getId());
            ResultSet rsPermisos = stmtPermisos.executeQuery();
            
            List<Permiso> permisos = new ArrayList<>();
            while (rsPermisos.next()) {
                Permiso permiso = new Permiso(
                    rsPermisos.getInt("id"),
                    rsPermisos.getString("nombre"),
                    rsPermisos.getString("descripcion")
                );
                
                permisos.add(permiso);
            }
            usuario.setPermisos(permisos); // Establecer los permisos en el objeto Usuario
        } catch (SQLException e) {
            mostrarError("Error", "Error al obtener permisos: " + e.getMessage());
        }
    }
    
    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE correo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            mostrarError("Error", "Error al verificar existencia de email: " + e.getMessage());
        }
        return false;
    }

    public boolean existeTelefono(String telefono) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE telefono = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, telefono);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            mostrarError("Error", "Error al verificar existencia de teléfono: " + e.getMessage());
        }
        return false;
    }
}

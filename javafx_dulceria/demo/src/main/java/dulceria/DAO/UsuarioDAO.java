package dulceria.DAO;

import dulceria.DatabaseConnection;
import dulceria.model.Permiso;
import dulceria.model.Rol;
import dulceria.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class UsuarioDAO {
    private Connection connection;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioDAO() {
        connection = DatabaseConnection.getConnection();
    }


    public boolean guardarUsuario(Usuario usuario) {
        String query = "INSERT INTO usuario (nombre, correo, telefono, contrasena, estado) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefono());
            String contrasenaEncriptada = encoder.encode(usuario.getContrasena());
            stmt.setString(4, contrasenaEncriptada);
            stmt.setBoolean(5, usuario.isEstado());

            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarUsuario(Usuario usuario) {
        String query = "UPDATE usuario SET nombre = ?, correo = ?, telefono = ?, estado = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefono());
            stmt.setBoolean(4, usuario.isEstado());
            
            stmt.setInt(5, usuario.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }    

    public boolean eliminarUsuario(int id) {
        String query = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
    }

    public Usuario validarCredenciales(String email, String contrasenaIngresada) {
        String query = "SELECT id, nombre, correo, telefono, estado, contrasena FROM usuario WHERE correo = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String contrasenaEncriptada = rs.getString("contrasena");
                
                // Verificar la contraseña ingresada
                if (encoder.matches(contrasenaIngresada, contrasenaEncriptada)) {
                    // Crear el objeto Usuario
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setEmail(rs.getString("correo"));
                    usuario.setTelefono(rs.getString("telefono"));
                    usuario.setEstado(rs.getBoolean("estado"));
                    
                    // Obtener los roles del usuario
                    obtenerRoles(usuario);
    
                    // Obtener los permisos asociados a los roles del usuario
                    obtenerPermisos(usuario);
                    
                    return usuario;
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        
        return null; // Devuelve null si las credenciales no son válidas
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
            System.out.println(e);
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
            System.out.println(e);
        }
    }
    
    
}

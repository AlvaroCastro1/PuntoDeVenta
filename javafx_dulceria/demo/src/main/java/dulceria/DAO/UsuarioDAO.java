package dulceria.DAO;

import dulceria.DatabaseConnection;
import dulceria.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UsuarioDAO {
    private Connection connection;

    public UsuarioDAO() {
        connection = DatabaseConnection.getConnection();
    }


    public boolean guardarUsuario(Usuario usuario) {
        String query = "INSERT INTO usuario (nombre, correo, telefono, contrasena, estado, id_rol) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefono());
            stmt.setString(4, usuario.getContrasena());
            stmt.setBoolean(5, usuario.isEstado());
            stmt.setInt(6, 3); // el id 3 de invitado

            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarUsuario(Usuario usuario) {
        String query = "UPDATE usuario SET nombre = ?, correo = ?, telefono = ?, estado = ?, id_rol = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefono());
            stmt.setBoolean(4, usuario.isEstado());
            
            // Obtener el id_rol desde el nombre del rol
            int idRol = obtenerIdRol(usuario.getRol());  // Método que ya tienes para obtener el id del rol
            stmt.setInt(5, idRol);
            
            stmt.setInt(6, usuario.getId());
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
                "SELECT u.id, u.nombre, u.correo, u.telefono, u.estado, r.id AS idRol, r.nombre AS rol " +
                "FROM usuario u " +
                "JOIN rol r ON u.id_rol = r.id"
            );
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                usuarios.add(new Usuario(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("correo"),
                    rs.getString("telefono"),
                    rs.getBoolean("estado"),
                    rs.getInt("idRol"),
                    rs.getString("rol")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return usuarios;
    }
    
    private int obtenerIdRol(String rol) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM rol WHERE nombre = ?");
            stmt.setString(1, rol);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;  // Si no se encuentra el rol
    }
    
    
}

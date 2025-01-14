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
        String query = "INSERT INTO usuario (nombre, correo, telefono, contrasena, estado) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefono());
            stmt.setString(4, usuario.getContrasena());
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
        String query = "SELECT id, nombre, correo, telefono, estado FROM usuario";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Usuario usuario = new Usuario(
                        resultSet.getInt("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("correo"),
                        resultSet.getString("telefono"),
                        resultSet.getBoolean("estado")
                );
                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }
    
}

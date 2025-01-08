package dulceria.DAO;

import dulceria.DatabaseConnection;

import dulceria.model.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {

    // Método para obtener la conexión a la base de datos
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection(); // Asume que tienes la clase DatabaseConnection para obtener la conexión.
    }

    // Crear un nuevo rol
    public boolean crearRol(Rol rol) {
        String query = "INSERT INTO rol (nombre, descripcion) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, rol.getNombreRol());
            stmt.setString(2, rol.getDescripcion());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener todos los roles
    public List<Rol> obtenerRoles() {
        List<Rol> roles = new ArrayList<>();
        String query = "SELECT * FROM rol";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roles.add(new Rol(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    // Actualizar un rol
    public boolean actualizarRol(Rol rol) {
        String query = "UPDATE rol SET nombre = ?, descripcion = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, rol.getNombreRol());
            stmt.setString(2, rol.getDescripcion());
            stmt.setInt(3, rol.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar un rol
    public boolean eliminarRol(int id) {
        String query = "DELETE FROM rol WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

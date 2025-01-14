package dulceria.DAO;

import dulceria.DatabaseConnection;
import dulceria.model.Permiso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermisoDAO {

    // Crear un permiso
    public boolean crearPermiso(Permiso permiso) {
        String query = "INSERT INTO permiso (nombre, descripcion) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, permiso.getNombre());
            stmt.setString(2, permiso.getDescripcion());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Leer todos los permisos
    public List<Permiso> obtenerPermisos() {
        List<Permiso> permisos = new ArrayList<>();
        String query = "SELECT * FROM permiso";
        try (Connection conn = DatabaseConnection.getConnection(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Permiso permiso = new Permiso(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion"));
                permisos.add(permiso);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permisos;
    }

    // Actualizar un permiso
    public boolean actualizarPermiso(Permiso permiso) {
        String query = "UPDATE permiso SET nombre = ?, descripcion = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, permiso.getNombre());
            stmt.setString(2, permiso.getDescripcion());
            stmt.setInt(3, permiso.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar un permiso
    public boolean eliminarPermiso(int id) {
        String query = "DELETE FROM permiso WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

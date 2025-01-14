package dulceria.controller;

import dulceria.DatabaseConnection;
import dulceria.model.UsuarioRol;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class UsuarioRolController {

    @FXML
    private ComboBox<String> comboUsuario;
    @FXML
    private ComboBox<String> comboRol;
    @FXML
    private TableView<UsuarioRol> tablaUsuarioRol;
    @FXML
    private TableColumn<UsuarioRol, String> columnaUsuario;
    @FXML
    private TableColumn<UsuarioRol, String> columnaRol;

    private final ObservableList<UsuarioRol> relaciones = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        cargarUsuarios();
        cargarRoles();
        cargarRelaciones();
    }

    private void configurarTabla() {
        columnaUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        columnaRol.setCellValueFactory(new PropertyValueFactory<>("nombreRol"));
        tablaUsuarioRol.setItems(relaciones);
    }

    private void cargarUsuarios() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, nombre FROM usuario";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                comboUsuario.getItems().add(rs.getString("nombre") + " (" + rs.getInt("id") + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarRoles() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, nombre FROM rol";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                comboRol.getItems().add(rs.getString("nombre") + " (" + rs.getInt("id") + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarRelaciones() {
        relaciones.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.nombre AS nombreUsuario, r.nombre AS nombreRol\n"+
                "FROM usuario_rol ur\n"+
                "JOIN usuario u ON ur.id_usuario = u.id\n"+
                "JOIN rol r ON ur.id_rol = r.id\n";

            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                relaciones.add(new UsuarioRol(rs.getString("nombreUsuario"), rs.getString("nombreRol")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void asignarRol() {
        String usuarioSeleccionado = comboUsuario.getValue();
        String rolSeleccionado = comboRol.getValue();
        if (usuarioSeleccionado == null || rolSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Debe seleccionar un usuario y un rol.");
            return;
        }

        int idUsuario = extraerId(usuarioSeleccionado);
        int idRol = extraerId(rolSeleccionado);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario);
            ps.setInt(2, idRol);
            ps.executeUpdate();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Rol Asignado", "El rol fue asignado correctamente.");
            cargarRelaciones();
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo asignar el rol. Verifique que la relación no exista.");
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarRol() {
        UsuarioRol seleccion = tablaUsuarioRol.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Vacía", "Debe seleccionar una relación para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmación");
        confirmacion.setHeaderText("Eliminar Relación");
        confirmacion.setContentText("¿Está seguro de que desea eliminar esta relación?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            int idUsuario = extraerIdDesdeTabla(seleccion.getNombreUsuario());
            int idRol = extraerIdDesdeTabla(seleccion.getNombreRol());

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM usuario_rol WHERE id_usuario = ? AND id_rol = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setInt(1, idUsuario);
                ps.setInt(2, idRol);
                ps.executeUpdate();

                mostrarAlerta(Alert.AlertType.INFORMATION, "Relación Eliminada", "La relación fue eliminada correctamente.");
                cargarRelaciones();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la relación.");
                e.printStackTrace();
            }
        }
    }

    private int extraerId(String valor) {
        return Integer.parseInt(valor.replaceAll(".*\\((\\d+)\\)", "$1"));
    }

    private int extraerIdDesdeTabla(String nombre) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id FROM usuario WHERE nombre = ? UNION ALL SELECT id FROM rol WHERE nombre = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, nombre);
            ps.setString(2, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void limpiarCampos() {
        comboUsuario.getSelectionModel().clearSelection();
        comboRol.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

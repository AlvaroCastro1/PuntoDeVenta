package dulceria.controller;

import dulceria.model.RolPermiso;
import dulceria.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RolPermisoController {
    @FXML
    private ComboBox<String> comboBoxRol;
    @FXML
    private ComboBox<String> comboBoxPermiso;
    @FXML
    private TableView<RolPermiso> tablaRolPermiso;
    @FXML
    private TableColumn<RolPermiso, String> columnaRol;
    @FXML
    private TableColumn<RolPermiso, String> columnaPermiso;

    private ObservableList<RolPermiso> relaciones = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cargarComboBox();
        configurarTabla();
        cargarRelaciones();
    }

    private void cargarComboBox() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Cargar roles
            PreparedStatement stmtRol = conn.prepareStatement("SELECT id, nombre FROM rol");
            ResultSet rsRol = stmtRol.executeQuery();
            while (rsRol.next()) {
                comboBoxRol.getItems().add(rsRol.getInt("id") + " - " + rsRol.getString("nombre"));
            }

            // Cargar permisos
            PreparedStatement stmtPermiso = conn.prepareStatement("SELECT id, nombre FROM permiso");
            ResultSet rsPermiso = stmtPermiso.executeQuery();
            while (rsPermiso.next()) {
                comboBoxPermiso.getItems().add(rsPermiso.getInt("id") + " - " + rsPermiso.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurarTabla() {
        columnaRol.setCellValueFactory(new PropertyValueFactory<>("nombreRol"));
        columnaPermiso.setCellValueFactory(new PropertyValueFactory<>("nombrePermiso"));
        tablaRolPermiso.setItems(relaciones);
    }

    private void cargarRelaciones() {
        relaciones.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT rp.id_rol AS idRol, r.nombre AS rol, " +
                    "rp.id_permiso AS idPermiso, p.nombre AS permiso " +
                    "FROM rol_permiso rp " +
                    "JOIN rol r ON rp.id_rol = r.id " +
                    "JOIN permiso p ON rp.id_permiso = p.id"
            );
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                relaciones.add(new RolPermiso(
                        rs.getInt("idRol"),       // ID del Rol
                        rs.getString("rol"),      // Nombre del Rol
                        rs.getInt("idPermiso"),   // ID del Permiso
                        rs.getString("permiso")   // Nombre del Permiso
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    @FXML
    public void agregarRelacion(ActionEvent event) {
        String rolSeleccionado = comboBoxRol.getValue();
        String permisoSeleccionado = comboBoxPermiso.getValue();

        if (rolSeleccionado == null || permisoSeleccionado == null) {
            mostrarAlerta("Error", "Debes seleccionar un rol y un permiso.", Alert.AlertType.ERROR);
            return;
        }

        int idRol = Integer.parseInt(rolSeleccionado.split(" - ")[0]);
        int idPermiso = Integer.parseInt(permisoSeleccionado.split(" - ")[0]);

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO rol_permiso (id_rol, id_permiso) VALUES (?, ?)"
            );
            stmt.setInt(1, idRol);
            stmt.setInt(2, idPermiso);
            stmt.executeUpdate();
            mostrarAlerta("Relación Creada", "La relación fue guardada correctamente.", Alert.AlertType.CONFIRMATION);
            cargarRelaciones();
            limpiarCampos();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo agregar la relación.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void eliminarRelacion(ActionEvent event) {
        RolPermiso seleccion = tablaRolPermiso.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            mostrarAlerta("Error", "Debes seleccionar una relación.", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de eliminar la relación?", ButtonType.YES, ButtonType.NO);
        confirmacion.showAndWait();

        if (confirmacion.getResult() == ButtonType.YES) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM rol_permiso WHERE id_rol = ? AND id_permiso = ?"
                );
                stmt.setInt(1, seleccion.getIdRol());
                stmt.setInt(2, seleccion.getIdPermiso());
                stmt.executeUpdate();
                mostrarAlerta("Relación Eliminada", "La relación fue eliminada correctamente.", Alert.AlertType.CONFIRMATION);
                cargarRelaciones();

            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo eliminar la relación.", Alert.AlertType.ERROR);
            }
        }
    }

    private void limpiarCampos() {
        comboBoxRol.getSelectionModel().clearSelection();
        comboBoxPermiso.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}

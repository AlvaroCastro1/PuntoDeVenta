package dulceria.controller;

import dulceria.model.Rol;
import dulceria.DAO.RolDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class RolController {

    @FXML
    private TextField txtNombreRol;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private TableView<Rol> tblRoles;
    @FXML
    private TableColumn<Rol, String> colNombreRol;
    @FXML
    private TableColumn<Rol, String> colDescripcion;

    private RolDAO rolDAO = new RolDAO();
    private ObservableList<Rol> rolesList = FXCollections.observableArrayList();

    // Inicializar la tabla
    @FXML
    public void initialize() {
        // Establecer la acción al seleccionar un rol de la tabla
        tblRoles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Cargar los datos del rol seleccionado en los campos
                txtNombreRol.setText(newValue.getNombreRol());
                txtDescripcion.setText(newValue.getDescripcion());
            }
        });

        colNombreRol.setCellValueFactory(cellData -> cellData.getValue().nombreRolProperty());
        colDescripcion.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());

        cargarRoles();
    }

    // Cargar roles en la tabla
    private void cargarRoles() {
        rolesList.setAll(rolDAO.obtenerRoles());
        tblRoles.setItems(rolesList);
    }

    // Crear un nuevo rol
    @FXML
    public void crearRol() {
        String nombreRol = txtNombreRol.getText();
        String descripcion = txtDescripcion.getText();

        if (!nombreRol.isEmpty() && !descripcion.isEmpty()) {
            Rol rol = new Rol(0, nombreRol, descripcion);
            if (rolDAO.crearRol(rol)) {
                cargarRoles();
                limpiarCampos();
                mostrarAlerta("Información", "Se agregó el rol.", Alert.AlertType.CONFIRMATION);
            } else {
                mostrarAlerta("Error", "No se pudo crear el rol.",Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Error", "Los campos no pueden estar vacíos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void actualizarRol() {
        Rol rolSeleccionado = tblRoles.getSelectionModel().getSelectedItem();
        
        if (rolSeleccionado != null) {
            // Obtener los valores de los campos
            String nombreRol = txtNombreRol.getText();
            String descripcion = txtDescripcion.getText();

            if (!nombreRol.isEmpty() && !descripcion.isEmpty()) {
                // Actualizar los valores en el objeto Rol
                rolSeleccionado.setNombreRol(nombreRol);
                rolSeleccionado.setDescripcion(descripcion);
                
                // Llamar al método de actualización en RolDAO
                if (rolDAO.actualizarRol(rolSeleccionado)) {
                    cargarRoles();  // Recargar los roles en la tabla
                    limpiarCampos(); // Limpiar los campos de texto
                    mostrarAlerta("Información", "Se actualizó el rol.", Alert.AlertType.CONFIRMATION);
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar el rol.", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "Los campos no pueden estar vacíos.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Error", "Por favor, selecciona un rol para actualizar.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void eliminarRol() {
        Rol rolSeleccionado = tblRoles.getSelectionModel().getSelectedItem();
        
        if (rolSeleccionado != null) {
            // Confirmar la eliminación con el usuario
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación de eliminación");
            alert.setHeaderText("¿Estás seguro de que quieres eliminar este rol?");
            alert.setContentText("Esta acción no puede deshacerse.");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Eliminar el rol de la base de datos
                    if (rolDAO.eliminarRol(rolSeleccionado.getId())) {
                        cargarRoles();  // Recargar los roles en la tabla
                        limpiarCampos(); // Limpiar los campos de texto
                        mostrarAlerta("Informacion", "Se eliminó el rol", Alert.AlertType.CONFIRMATION);
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el rol.", Alert.AlertType.ERROR);
                    }
                }
            });
        } else {
            mostrarAlerta("Error", "Por favor, selecciona un rol para eliminar.", Alert.AlertType.ERROR);
        }
    }

    // Mostrar un mensaje
    public void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void limpiarCampos() {
        txtNombreRol.clear();
        txtDescripcion.clear();
    }
}

package dulceria.controller;

import dulceria.model.Permiso;
import dulceria.DAO.PermisoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PermisoController {

    @FXML
    private TableView<Permiso> tblPermisos;
    @FXML
    private TableColumn<Permiso, String> colNombre;
    @FXML
    private TableColumn<Permiso, String> colDescripcion;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private Button btnCrear;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;

    private final PermisoDAO permisoDAO = new PermisoDAO();
    private ObservableList<Permiso> listaPermisos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colDescripcion.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        
        cargarPermisos();
        
        tblPermisos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> cargarDatosFormulario(newValue));
    }

    private void cargarPermisos() {
        listaPermisos.clear();
        listaPermisos.addAll(permisoDAO.obtenerPermisos());
        tblPermisos.setItems(listaPermisos);
    }

    private void cargarDatosFormulario(Permiso permiso) {
        if (permiso != null) {
            txtNombre.setText(permiso.getNombre());
            txtDescripcion.setText(permiso.getDescripcion());
        }
    }

    @FXML
    private void crearPermiso() {
        String nombre = txtNombre.getText();
        String descripcion = txtDescripcion.getText();

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            mostrarAlerta("Error", "Todos los campos deben ser llenados.", Alert.AlertType.ERROR);
            return;
        }

        Permiso permiso = new Permiso(0, nombre, descripcion);
        if (permisoDAO.crearPermiso(permiso)) {
            limpiarCampos();
            mostrarAlerta("Éxito", "Permiso creado con éxito.", Alert.AlertType.INFORMATION);
            cargarPermisos();
        } else {
            mostrarAlerta("Error", "No se pudo crear el permiso.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void actualizarPermiso() {
        Permiso permisoSeleccionado = tblPermisos.getSelectionModel().getSelectedItem();
        if (permisoSeleccionado != null) {
            permisoSeleccionado.setNombre(txtNombre.getText());
            permisoSeleccionado.setDescripcion(txtDescripcion.getText());

            if (permisoDAO.actualizarPermiso(permisoSeleccionado)) {
                limpiarCampos();
                mostrarAlerta("Éxito", "Permiso actualizado con éxito.", Alert.AlertType.INFORMATION);
                cargarPermisos();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el permiso.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Error", "Por favor, selecciona un permiso para actualizar.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarPermiso() {
        Permiso permisoSeleccionado = tblPermisos.getSelectionModel().getSelectedItem();

        if (permisoSeleccionado != null) {
            // Crear la alerta de confirmación
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmación");
            confirmacion.setHeaderText("¿Estás seguro de eliminar este permiso?");
            confirmacion.setContentText("Este permiso será eliminado permanentemente.");

            // Mostrar la alerta y esperar la respuesta del usuario
            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    // Si el usuario confirma, proceder con la eliminación
                    if (permisoDAO.eliminarPermiso(permisoSeleccionado.getId())) {
                        limpiarCampos();
                        mostrarAlerta("Éxito", "Permiso eliminado con éxito.", Alert.AlertType.INFORMATION);
                        cargarPermisos();
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el permiso.", Alert.AlertType.ERROR);
                    }
                } else {
                    // Si el usuario cancela la eliminación
                    mostrarAlerta("Cancelado", "La eliminación del permiso ha sido cancelada.", Alert.AlertType.INFORMATION);
                }
            });
        } else {
            mostrarAlerta("Error", "Por favor, selecciona un permiso para eliminar.", Alert.AlertType.ERROR);
        }
    }


    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void limpiarCampos() {
        // Limpiar los campos de texto y área de texto
        txtNombre.clear();
        txtDescripcion.clear();
    }

}

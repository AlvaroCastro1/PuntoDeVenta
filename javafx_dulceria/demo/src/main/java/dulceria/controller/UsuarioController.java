package dulceria.controller;

import dulceria.model.Usuario;

import java.util.Optional;

import dulceria.DAO.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class UsuarioController {

    @FXML
    private TableView<Usuario> tblUsuarios;
    @FXML
    private TableColumn<Usuario, Integer> colId;
    @FXML
    private TableColumn<Usuario, String> colNombre;
    @FXML
    private TableColumn<Usuario, String> colEmail;
    @FXML
    private TableColumn<Usuario, String> colTel;
    @FXML
    private TableColumn<Usuario, String> colRol;

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtTel;
    @FXML
    private TextField txtRol;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnEliminar;
    @FXML
    private ComboBox<String> cmbEstado;


    private UsuarioDAO usuarioDAO;
    private ObservableList<Usuario> listaUsuarios;

    @FXML
    public void initialize() {

        cmbEstado.getItems().addAll("Activo", "Inactivo");

        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mostrarDetallesUsuario(newValue);
            }
        });

        usuarioDAO = new UsuarioDAO();
        listaUsuarios = FXCollections.observableArrayList();
        configurarTabla();
        cargarUsuarios();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
    }

    private void cargarUsuarios() {
        listaUsuarios.clear();
        listaUsuarios.addAll(usuarioDAO.obtenerTodos());
        tblUsuarios.setItems(listaUsuarios);
    }

    private void mostrarDetallesUsuario(Usuario usuario) {
        txtNombre.setText(usuario.getNombre());
        txtEmail.setText(usuario.getEmail());
        txtTel.setText(usuario.getTelefono());
        String estadoTexto = usuario.isEstado() == true ? "Activo" : "Inactivo";
        cmbEstado.setValue(estadoTexto);
    }

    @FXML
    private void actualizarUsuario() {
        Usuario usuarioSeleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        String estadoSeleccionado = cmbEstado.getValue();
        if (usuarioSeleccionado != null && estadoSeleccionado != null) {
            usuarioSeleccionado.setNombre(txtNombre.getText());
            usuarioSeleccionado.setEmail(txtEmail.getText());
            usuarioSeleccionado.setTelefono(txtTel.getText());
            // usuarioSeleccionado.setRol(txtRol.getText());
            usuarioSeleccionado.setEstado(estadoSeleccionado.equals("Activo") ? true : false);
            if (usuarioDAO.actualizarUsuario(usuarioSeleccionado)) {
                mostrarAlerta("Éxito", "Usuario actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarUsuarios();
                limpiarCampos();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el usuario.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Error", "Por favor, selecciona un usuario para actualizar.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarUsuario() {
        Usuario usuarioSeleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar este usuario?");
            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (usuarioDAO.eliminarUsuario(usuarioSeleccionado.getId())) {
                    mostrarAlerta("Éxito", "Usuario eliminado correctamente.", Alert.AlertType.INFORMATION);
                    cargarUsuarios();
                    limpiarCampos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el usuario.", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Error", "Por favor, selecciona un usuario para eliminar.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void limpiarCampos() {
        txtNombre.clear();
        txtEmail.clear();
        txtTel.clear();
        txtRol.clear();
        cmbEstado.getSelectionModel().clearSelection(); // Limpia la selección
        cmbEstado.setValue(null); // Limpia también el valor mostrado
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

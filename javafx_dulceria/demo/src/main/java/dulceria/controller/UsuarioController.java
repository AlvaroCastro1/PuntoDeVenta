package dulceria.controller;

import dulceria.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

import dulceria.DatabaseConnection;
import dulceria.DAO.UsuarioDAO;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.TextInputDialog;
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
    private ComboBox<String> cmbRol;
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
        cargarRoles();  // Cargar los roles disponibles
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
        colRol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRol()));
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
        cmbRol.setValue(usuario.getRol());
        cmbEstado.setValue(estadoTexto);
    }

    @FXML
    private void actualizarUsuario() {
        Usuario usuarioSeleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        String estadoSeleccionado = cmbEstado.getValue();
        String rolSeleccionado = cmbRol.getValue();  // Obtener el rol seleccionado

        if (usuarioSeleccionado != null && estadoSeleccionado != null && rolSeleccionado != null) {
            usuarioSeleccionado.setNombre(txtNombre.getText());
            usuarioSeleccionado.setEmail(txtEmail.getText());
            usuarioSeleccionado.setTelefono(txtTel.getText());
            usuarioSeleccionado.setEstado(estadoSeleccionado.equals("Activo"));

            // Aquí actualizamos el rol en la base de datos
            usuarioSeleccionado.setRol(rolSeleccionado);

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
        cmbRol.getSelectionModel().clearSelection(); // Limpia la selección
        cmbRol.setValue(null); // Limpia también el valor mostrado
        cmbEstado.getSelectionModel().clearSelection(); // Limpia la selección
        cmbEstado.setValue(null); // Limpia también el valor mostrado
    }

    @FXML
    private void cambiarContrasena() {
        Usuario usuarioSeleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            mostrarAlerta("Error", "Por favor, selecciona un usuario primero.", Alert.AlertType.ERROR);
            return;
        }

        // Solicitar la nueva contraseña
        TextInputDialog dialogNuevaContrasena = new TextInputDialog();
        dialogNuevaContrasena.setTitle("Cambiar Contraseña");
        dialogNuevaContrasena.setHeaderText("Nueva Contraseña");
        dialogNuevaContrasena.setContentText("Ingresa la nueva contraseña:");
        Optional<String> resultadoNuevaContrasena = dialogNuevaContrasena.showAndWait();

        if (!resultadoNuevaContrasena.isPresent() || resultadoNuevaContrasena.get().isEmpty()) {
            mostrarAlerta("Error", "Debe ingresar una contraseña válida.", Alert.AlertType.ERROR);
            return;
        }
        String nuevaContrasena = resultadoNuevaContrasena.get();

        // Confirmar la nueva contraseña
        TextInputDialog dialogConfirmarContrasena = new TextInputDialog();
        dialogConfirmarContrasena.setTitle("Confirmar Contraseña");
        dialogConfirmarContrasena.setHeaderText("Confirmar Contraseña");
        dialogConfirmarContrasena.setContentText("Reingresa la nueva contraseña:");
        Optional<String> resultadoConfirmarContrasena = dialogConfirmarContrasena.showAndWait();

        if (!resultadoConfirmarContrasena.isPresent() || !resultadoConfirmarContrasena.get().equals(nuevaContrasena)) {
            mostrarAlerta("Error", "Las contraseñas no coinciden. Intenta de nuevo.", Alert.AlertType.ERROR);
            return;
        }

        // Validar longitud mínima
        if (nuevaContrasena.length() < 6) {
            mostrarAlerta("Error", "La contraseña debe tener al menos 6 caracteres.", Alert.AlertType.ERROR);
            return;
        }

        // Actualizar la contraseña en la base de datos
        boolean exito = usuarioDAO.cambiarContrasena(usuarioSeleccionado.getId(), nuevaContrasena);
        if (exito) {
            mostrarAlerta("Éxito", "Contraseña actualizada correctamente.", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "No se pudo actualizar la contraseña. Intenta de nuevo.", Alert.AlertType.ERROR);
        }
    }

    private void cargarRoles() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT nombre FROM rol");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cmbRol.getItems().add(rs.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

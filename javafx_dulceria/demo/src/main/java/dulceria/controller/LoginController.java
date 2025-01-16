package dulceria.controller;

import dulceria.DAO.UsuarioDAO;
import dulceria.app.App;
import dulceria.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtContrasena;
    @FXML
    private TextField txtContrasenaVisible;
    @FXML
    private Button btnMostrarContrasena, btnEntrar;
    @FXML
    private Label lblMensajeError;

    private UsuarioDAO usuarioDAO;

    private App app;
    private Stage primaryStage;


    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    public void setApp(App app, Stage primaryStage) {
        this.app = app;
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        sincronizarCampos();
        txtContrasena.setOnAction(event -> {
            // Cuando se presiona Enter en el campo de contraseña, disparar el clic en el botón "Entrar"
            btnEntrar.fire();
        });
    }

    private void sincronizarCampos() {
        // Sincroniza los campos de texto para mostrar y ocultar contraseñas
        txtContrasena.textProperty().bindBidirectional(txtContrasenaVisible.textProperty());
    }

    @FXML
    private void toggleMostrarContrasena() {
        // Alternar visibilidad de los campos de contraseña
        boolean mostrar = txtContrasena.isVisible();
        txtContrasena.setVisible(!mostrar);
        txtContrasenaVisible.setVisible(mostrar);
        btnMostrarContrasena.setText(mostrar ? "👁" : "🙈");
    }

    @FXML
    private void login() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            lblMensajeError.setText("Por favor, completa todos los campos.");
            return;
        }

        Usuario usuarioValidado = usuarioDAO.validarCredenciales(usuario, contrasena);
        if (usuarioValidado != null) {
            lblMensajeError.setText("");
            mostrarAlerta("Éxito", "Bienvenido, " + usuarioValidado.getNombre(), Alert.AlertType.INFORMATION);
            // redirigir a la siguiente ventana de la aplicación y añadir el usuario
            App.setUsuarioAutenticado(usuarioValidado);
            app.showMainView(primaryStage);
        } else {
            lblMensajeError.setText("Usuario o contraseña incorrectos.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

package dulceria.controller;

import dulceria.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import dulceria.DAO.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CrearUsuarioController {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelefono;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private PasswordField txtConfirmarContrasena;

    @FXML
    private TextField txtContrasenaVisible;
    
    @FXML
    private TextField txtConfirmarContrasenaVisible;
    
    @FXML
    private Button btnToggleContrasena;
    
    @FXML
    private Button btnToggleConfirmarContrasena;
    
    private UsuarioDAO usuarioDAO;

    @FXML
    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        setButtonImage(btnToggleContrasena, "/dulceria/images/eye.png");
        setButtonImage(btnToggleConfirmarContrasena, "/dulceria/images/eye.png");
    }

    private void setButtonImage(Button button, String imagePath) {
        Image imagen = new Image(getClass().getResource(imagePath).toString());
        ImageView imageView = new ImageView(imagen);
        button.setGraphic(imageView);
    }

    @FXML
    private void guardarUsuario() {
        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String contrasena = txtContrasena.getText().trim();
        String confirmarContrasena = txtConfirmarContrasena.getText().trim();
        
        // Validar campos
        if (!validarCampos(nombre, email, telefono, contrasena, confirmarContrasena)) {
            return;
        }

        boolean estadoPredeterminado = true;
        
        Usuario nuevoUsuario = new Usuario(0,
            nombre,
            email,
            telefono,
            contrasena,
            estadoPredeterminado
        );

        boolean exito = usuarioDAO.guardarUsuario(nuevoUsuario);
        if (exito) {
            mostrarAlerta("Éxito", "Usuario creado exitosamente.", AlertType.INFORMATION);
            limpiarCampos();
        } else {
            mostrarAlerta("Error", "Ocurrió un error al guardar el usuario.", AlertType.ERROR);
        }
    }

    private boolean validarCampos(String nombre, String email, String telefono, String contrasena, String confirmarContrasena) {
        // Validación de campos vacíos
        if (nombre.isEmpty() || email.isEmpty() || telefono.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor, llena todos los campos.", AlertType.WARNING);
            return false;
        }

        // Validación de formato de nombre
        if (!validarNombre(nombre)) {
            return false;
        }

        // Validación de correo
        if (!validarEmail(email)) {
            return false;
        }

        // Validación de teléfono
        if (!validarTelefono(telefono)) {
            return false;
        }

        // Validación de contraseña
        if (!validarContrasena(contrasena, confirmarContrasena)) {
            return false;
        }

        // Validación de duplicados
        if (!validarDuplicados(email, telefono)) {
            return false;
        }

        return true;
    }

    private boolean validarNombre(String nombre) {
        if (nombre.length() < 3) {
            mostrarAlerta("Nombre inválido", "El nombre debe tener al menos 3 caracteres.", AlertType.WARNING);
            return false;
        }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            mostrarAlerta("Nombre inválido", "El nombre solo debe contener letras y espacios.", AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validarEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            mostrarAlerta("Correo inválido", "Por favor, ingresa un correo electrónico válido.", AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validarTelefono(String telefono) {
        if (!telefono.matches("^\\d{10}$")) {
            mostrarAlerta("Teléfono inválido", "Por favor, ingresa un teléfono válido de 10 dígitos.", AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validarContrasena(String contrasena, String confirmarContrasena) {
        if (contrasena.length() < 6) {
            mostrarAlerta("Contraseña débil", "La contraseña debe tener al menos 6 caracteres.", AlertType.WARNING);
            return false;
        }
        
        if (!contrasena.matches(".*\\d.*")) {
            mostrarAlerta("Contraseña débil", "La contraseña debe contener al menos un número.", AlertType.WARNING);
            return false;
        }
        
        if (!contrasena.equals(confirmarContrasena)) {
            mostrarAlerta("Contraseñas no coinciden", "La contraseña y su confirmación no coinciden.", AlertType.WARNING);
            return false;
        }
        
        return true;
    }

    private boolean validarDuplicados(String email, String telefono) {
        try {
            // Verificar si existe el email
            if (usuarioDAO.existeEmail(email)) {
                mostrarAlerta("Email duplicado", "Ya existe un usuario registrado con este correo electrónico.", AlertType.WARNING);
                return false;
            }

            // Verificar si existe el teléfono
            if (usuarioDAO.existeTelefono(telefono)) {
                mostrarAlerta("Teléfono duplicado", "Ya existe un usuario registrado con este número de teléfono.", AlertType.WARNING);
                return false;
            }

            return true;
        } catch (Exception e) {
            mostrarAlerta("Error de validación", "Error al verificar duplicados: " + e.getMessage(), AlertType.ERROR);
            return false;
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtContrasena.clear();
        txtConfirmarContrasena.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    private void cancelar() {
        limpiarCampos();
        // Aquí puedes agregar lógica para cerrar la ventana o regresar a la pantalla anterior.
    }

    @FXML
    private void toggleContrasena() {
        togglePasswordVisibility(txtContrasena, txtContrasenaVisible);
    }

    @FXML
    private void toggleConfirmarContrasena() {
        togglePasswordVisibility(txtConfirmarContrasena, txtConfirmarContrasenaVisible);
    }

    // Método generalizado para alternar visibilidad de contraseñas
    private void togglePasswordVisibility(PasswordField contrasenaField, TextField contrasenaVisibleField) {
        if (contrasenaField.isVisible()) {
            // Mostrar contraseña
            contrasenaVisibleField.setText(contrasenaField.getText());
            contrasenaField.setVisible(false);
            contrasenaField.setManaged(false);
            contrasenaVisibleField.setVisible(true);
            contrasenaVisibleField.setManaged(true);
        } else {
            // Ocultar contraseña
            contrasenaField.setText(contrasenaVisibleField.getText());
            contrasenaVisibleField.setVisible(false);
            contrasenaVisibleField.setManaged(false);
            contrasenaField.setVisible(true);
            contrasenaField.setManaged(true);
        }
    }
}

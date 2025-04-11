package dulceria.controller;

import dulceria.model.Rol;
import dulceria.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import dulceria.DatabaseConnection;
import dulceria.DAO.UsuarioDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
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
    private TextField txtNombre;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtTel;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnEliminar;
    @FXML
    private ComboBox<String> cmbEstado;
    @FXML 
    private ComboBox<Rol> cmbRoles;  // ComboBox para roles
    @FXML 
    private ListView<Rol> listRolesUsuario;  // ListView para roles asignados
    @FXML
    private TextField txtBusqueda;


    private UsuarioDAO usuarioDAO;
    private ObservableList<Usuario> listaUsuarios;

    @FXML
    public void initialize() {

        cmbEstado.getItems().addAll("Activo", "Inactivo");
        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mostrarDetallesUsuario(newValue);
                cargarRolesUsuario(newValue);
            }
        });

        usuarioDAO = new UsuarioDAO();
        listaUsuarios = FXCollections.observableArrayList();
        configurarTabla();
        cargarUsuarios();
        cargarRolesDisponibles();

        // Envolver la lista en un FilteredList
        FilteredList<Usuario> filteredData = new FilteredList<>(listaUsuarios, p -> true);

        // Escuchar cambios en el campo de búsqueda
        txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(usuario -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Mostrar todos los usuarios si no hay filtro
                }
                String lowerCaseFilter = newValue.toLowerCase();

                // Comparar con los atributos de Usuario
                return String.valueOf(usuario.getId()).contains(lowerCaseFilter) ||
                    usuario.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                    usuario.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                    usuario.getTelefono().toLowerCase().contains(lowerCaseFilter) ||
                    (usuario.isEstado() ? "activo" : "inactivo").contains(lowerCaseFilter);
            });
        });

        // Enlazar la lista filtrada con una SortedList para mantener el ordenamiento
        SortedList<Usuario> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblUsuarios.comparatorProperty());

        // Asignar los datos filtrados a la tabla
        tblUsuarios.setItems(sortedData);
        tblUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));
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
            usuarioSeleccionado.setEstado(estadoSeleccionado.equals("Activo"));

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

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cargarRolesDisponibles() {
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM rol"; // Consulta para obtener todos los roles
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
    
            cmbRoles.getItems().clear();
            while (rs.next()) {
                cmbRoles.getItems().add(new Rol(rs.getInt("id"),
                 rs.getString("nombre"), 
                 rs.getString("descripcion"))); // Crear objeto Rol y agregarlo
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los roles.", Alert.AlertType.ERROR);
        }
    }
    
    // Método para cargar los roles asignados a un usuario
    private void cargarRolesUsuario(Usuario usuario) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "SELECT r.id, r.nombre, r.descripcion FROM rol r " +
                         "JOIN usuario_rol ur ON r.id = ur.id_rol " +
                         "WHERE ur.id_usuario = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, usuario.getId());
            ResultSet rs = stmt.executeQuery();
    
            listRolesUsuario.getItems().clear();
            while (rs.next()) {
                listRolesUsuario.getItems().add(new Rol(
                                                    rs.getInt("id"),
                                                    rs.getString("nombre"),
                                                    rs.getString("descripcion"))
                                                ); // Crear objeto Rol y agregarlo
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los roles asignados.", Alert.AlertType.ERROR);
        }
    }
    
    // Método para asignar un rol al usuario seleccionado
    @FXML
    private void asignarRol() {
        Usuario usuarioSeleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        Rol rolSeleccionado = cmbRoles.getValue();
    
        if (usuarioSeleccionado != null && rolSeleccionado != null) {
            try (Connection con = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?)";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setInt(1, usuarioSeleccionado.getId());
                stmt.setInt(2, rolSeleccionado.getId());
                int rowsAffected = stmt.executeUpdate();
    
                if (rowsAffected > 0) {
                    mostrarAlerta("Éxito", "Rol asignado correctamente.", Alert.AlertType.INFORMATION);
                    cargarRolesUsuario(usuarioSeleccionado);  // Recargar los roles asignados
                } else {
                    mostrarAlerta("Error", "No se pudo asignar el rol.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo asignar el rol.", Alert.AlertType.ERROR);
            }
        }
    }
    
    // Método para eliminar un rol del usuario seleccionado
    @FXML
    private void eliminarRol() {
        Usuario usuarioSeleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        Rol rolSeleccionado = listRolesUsuario.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado != null && rolSeleccionado != null) {
            // Mostrar un cuadro de confirmación antes de eliminar
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText("¿Estás seguro de eliminar este rol?");
            confirmacion.setContentText("El rol asociado a este usuario será eliminado.");

            // Mostrar el cuadro de confirmación y esperar la respuesta del usuario
            Optional<ButtonType> resultado = confirmacion.showAndWait();

            // Si el usuario confirma, proceder con la eliminación
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try (Connection con = DatabaseConnection.getConnection()) {
                    String sql = "DELETE FROM usuario_rol WHERE id_usuario = ? AND id_rol = ?";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setInt(1, usuarioSeleccionado.getId());
                    stmt.setInt(2, rolSeleccionado.getId());
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        mostrarAlerta("Éxito", "Rol eliminado correctamente.", Alert.AlertType.INFORMATION);
                        cargarRolesUsuario(usuarioSeleccionado);  // Recargar los roles asignados
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el rol.", Alert.AlertType.ERROR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error", "No se pudo eliminar el rol.", Alert.AlertType.ERROR);
                }
            } else {
                // Si el usuario cancela, no hacer nada
                System.out.println("Eliminación de rol cancelada.");
            }
        } else {
            mostrarAlerta("Error", "Por favor, selecciona un usuario y un rol para eliminar.", Alert.AlertType.ERROR);
        }
    }

}

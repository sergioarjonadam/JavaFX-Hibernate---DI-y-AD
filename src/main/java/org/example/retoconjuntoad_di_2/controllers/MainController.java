package org.example.retoconjuntoad_di_2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.retoconjuntoad_di_2.model.copia.Copia;
import org.example.retoconjuntoad_di_2.model.copia.CopiaRepository;
import org.example.retoconjuntoad_di_2.model.user.User;
import org.example.retoconjuntoad_di_2.session.SimpleSessionService;
import org.example.retoconjuntoad_di_2.utils.DataProvider;
import org.example.retoconjuntoad_di_2.utils.JavaFXUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador principal de la aplicación.
 * Gestiona la vista principal, incluyendo la tabla de copias y las acciones del usuario.
 */
public class MainController implements Initializable {

    public Button btnAñadir;
    public Button btnBorrar;
    public Button btnDetalle;

    @FXML
    private Button btnLogout; // Botón para cerrar sesión.

    @FXML
    private Label welcomeText; // Etiqueta para mostrar un mensaje de bienvenida.

    @FXML
    private Label lblUsuario; // Etiqueta para mostrar el nombre del usuario logueado.

    @FXML
    private Label lblTotalCopias; // Etiqueta para mostrar el total de copias del usuario.

    @FXML
    private TableView<Copia> tabla; // Tabla para mostrar las copias.

    @FXML
    private TableColumn<Copia, String> cId; // Columna para mostrar el ID de la copia.

    @FXML
    private TableColumn<Copia, String> cTitulo; // Columna para mostrar el título de la película.

    @FXML
    private TableColumn<Copia, String> cGenero; // Columna para mostrar el género de la película.

    @FXML
    private TableColumn<Copia, String> cAnio; // Columna para mostrar el año de la película.

    @FXML
    private TableColumn<Copia, String> cEstado; // Columna para mostrar el estado de la copia.

    @FXML
    private TableColumn<Copia, String> cSoporte; // Columna para mostrar el soporte de la copia.

    @FXML
    private Button btnAddPelicula; // Botón para añadir una nueva película.

    // Servicios
    private SimpleSessionService simpleSessionService; // Servicio para gestionar la sesión del usuario.
    private CopiaRepository copiaRepository; // Repositorio para gestionar las copias.

    /**
     * Inicializa el controlador y configura la vista principal.
     *
     * @param url URL de inicialización.
     * @param resourceBundle Recursos de inicialización.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        simpleSessionService = new SimpleSessionService();
        copiaRepository = new CopiaRepository(DataProvider.getSessionFactory());

        // Si no hay usuario logueado, redirige al login.
        if (!simpleSessionService.isLoggedIn()) {
            JavaFXUtil.showModal(
                    Alert.AlertType.WARNING,
                    "Sesión",
                    "No hay usuario logueado",
                    "Vuelve a iniciar sesión."
            );
            JavaFXUtil.setScene("/org/example/retoconjuntoad_di_2/login-view.fxml");
            return;
        }

        User user = simpleSessionService.getActive();
        lblUsuario.setText("Usuario: " + user.getNombreUsuario());

        if (!user.isEsAdmin()) {
            btnAddPelicula.setVisible(false);
            btnAddPelicula.setManaged(false); // Oculta el botón si el usuario no es administrador.
        }

        configurarTabla();
        cargarCopiasUsuario(user);
    }

    /**
     * Configura las columnas de la tabla para mostrar los datos de las copias.
     */
    private void configurarTabla() {
        cId.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getId() != null
                                ? cellData.getValue().getId().toString()
                                : ""
                )
        );

        cTitulo.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getPelicula() != null
                                ? cellData.getValue().getPelicula().getTitulo()
                                : ""
                )
        );

        cGenero.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getPelicula() != null
                                ? cellData.getValue().getPelicula().getGenero()
                                : ""
                )
        );

        cAnio.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        (cellData.getValue().getPelicula() != null
                                && cellData.getValue().getPelicula().getAnio() != null)
                                ? cellData.getValue().getPelicula().getAnio().toString()
                                : ""
                )
        );

        cEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getEstado() != null
                                ? cellData.getValue().getEstado()
                                : ""
                )
        );

        cSoporte.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getSoporte() != null
                                ? cellData.getValue().getSoporte()
                                : ""
                )
        );
    }

    /**
     * Carga las copias asociadas al usuario logueado en la tabla.
     *
     * @param user Usuario logueado.
     */
    private void cargarCopiasUsuario(User user) {
        tabla.getItems().clear();
        List<Copia> copias = copiaRepository.findByUser(user);
        tabla.getItems().addAll(copias);
        lblTotalCopias.setText("Total de copias: " + copias.size());
    }
    // ====== Botones ======

    /**
     * Maneja el evento de borrar una copia seleccionada.
     *
     * @param actionEvent Evento de acción generado al presionar el botón de borrar.
     */
    @FXML
    public void borrar(ActionEvent actionEvent) {
        Copia seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            JavaFXUtil.showModal(
                    Alert.AlertType.INFORMATION,
                    "Borrar copia",
                    "Ninguna copia seleccionada",
                    "Selecciona una copia en la tabla."
            );
            return;
        }

        // Diálogo de confirmación antes de borrar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar borrado");
        confirmacion.setHeaderText("¿Seguro que quieres borrar esta copia?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        // Mostrar y esperar respuesta del usuario
        confirmacion.showAndWait()
                .filter(boton -> boton == ButtonType.OK)
                .ifPresent(botonOk -> {
                    // Borra la copia seleccionada del repositorio solo si confirma
                    copiaRepository.delete(seleccionada);
                    cargarCopiasUsuario(simpleSessionService.getActive());
                });
    }


    /**
     * Maneja el evento de añadir una nueva copia.
     *
     * @param actionEvent Evento de acción generado al presionar el botón de añadir.
     */
    @FXML
    public void añadir(ActionEvent actionEvent) {
        var user = simpleSessionService.getActive();
        if (user == null) {
            JavaFXUtil.showModal(
                    Alert.AlertType.WARNING,
                    "Sesión",
                    "No hay usuario logueado",
                    "Vuelve a iniciar sesión."
            );
            JavaFXUtil.setScene("/org/example/retoconjuntoad_di_2/login-view.fxml");
            return;
        }

        Copia nueva = new Copia();
        nueva.setUser(user);

        try {
            FXMLLoader loader = new FXMLLoader(
                    JavaFXUtil.class.getResource("/org/example/retoconjuntoad_di_2/copy-detail-view.fxml")
            );
            Parent root = loader.load();

            CopyDetailController controller = loader.getController();
            controller.setCopia(nueva);

            Stage detailStage = new Stage();
            detailStage.setTitle("Nueva copia");
            detailStage.initOwner(JavaFXUtil.getStage());
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.setScene(new Scene(root));
            detailStage.showAndWait();

            cargarCopiasUsuario(simpleSessionService.getActive());

        } catch (Exception e) {
            e.printStackTrace();
            JavaFXUtil.showModal(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo abrir la ventana de detalle",
                    e.getMessage()
            );
        }
    }

    /**
     * Maneja el evento de ver el detalle de una copia seleccionada.
     *
     * @param actionEvent Evento de acción generado al presionar el botón de ver detalle.
     */
    @FXML
    public void verDetalle(ActionEvent actionEvent) {
        Copia seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            JavaFXUtil.showModal(
                    Alert.AlertType.INFORMATION,
                    "Detalle de copia",
                    "Ninguna copia seleccionada",
                    "Selecciona una copia en la tabla."
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    JavaFXUtil.class.getResource("/org/example/retoconjuntoad_di_2/copy-detail-view.fxml")
            );
            Parent root = loader.load();

            CopyDetailController controller = loader.getController();
            controller.setCopia(seleccionada);

            Stage detailStage = new Stage();
            detailStage.setTitle("Detalle de copia");
            detailStage.initOwner(JavaFXUtil.getStage());
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.setScene(new Scene(root));
            detailStage.showAndWait();

            cargarCopiasUsuario(simpleSessionService.getActive());

        } catch (Exception e) {
            e.printStackTrace();
            JavaFXUtil.showModal(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo abrir la ventana de detalle",
                    e.getMessage()
            );
        }
    }

    /**
     * Maneja el evento de añadir una nueva película.
     *
     * @param actionEvent Evento de acción generado al presionar el botón de añadir película.
     */
    @FXML
    public void añadirPelicula(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    JavaFXUtil.class.getResource("/org/example/retoconjuntoad_di_2/pelicula-detail-view.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initOwner(JavaFXUtil.getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Nueva película");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            JavaFXUtil.showModal(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo abrir la ventana de película",
                    e.getMessage()
            );
        }
    }

    /**
     * Maneja el evento de cerrar sesión.
     *
     * @param event Evento de acción generado al presionar el botón de logout.
     */
    @FXML
    public void logout(ActionEvent event) {
        JavaFXUtil.showModal(
                Alert.AlertType.INFORMATION,
                "Cerrar sesión",
                "Sesión cerrada",
                "Has cerrado la sesión correctamente."
        );

        simpleSessionService.logout();
        JavaFXUtil.setScene("/org/example/retoconjuntoad_di_2/login-view.fxml");
    }
}

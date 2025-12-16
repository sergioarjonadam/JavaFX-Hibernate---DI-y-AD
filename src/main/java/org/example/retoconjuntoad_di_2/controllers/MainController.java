package org.example.retoconjuntoad_di_2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

public class MainController implements Initializable {

    public Button btnAñadir;
    public Button btnBorrar;
    public Button btnDetalle;

    @FXML
    private Button btnLogout;

    @FXML
    private Label welcomeText;

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblTotalCopias;

    @FXML
    private TableView<Copia> tabla;

    @FXML
    private TableColumn<Copia, String> cId;

    @FXML
    private TableColumn<Copia, String> cTitulo;

    @FXML
    private TableColumn<Copia, String> cGenero;

    @FXML
    private TableColumn<Copia, String> cAnio;

    @FXML
    private TableColumn<Copia, String> cEstado;

    @FXML
    private TableColumn<Copia, String> cSoporte;

    @FXML
    private Button btnAddPelicula;

    @FXML
    private TextField txtBuscar; // Campo de búsqueda por título

    private SimpleSessionService simpleSessionService;
    private CopiaRepository copiaRepository;

    // Lista completa y lista filtrada para la tabla
    private final ObservableList<Copia> copiasUsuario = FXCollections.observableArrayList();
    private FilteredList<Copia> copiasFiltradas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        simpleSessionService = new SimpleSessionService();
        copiaRepository = new CopiaRepository(DataProvider.getSessionFactory());

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
            btnAddPelicula.setManaged(false);
        }

        configurarTabla();

        // Configurar lista filtrada y búsqueda
        copiasFiltradas = new FilteredList<>(copiasUsuario, copia -> true);
        tabla.setItems(copiasFiltradas);

        configurarBusqueda();

        cargarCopiasUsuario(user);
    }

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
     * Configura el filtro de búsqueda en tiempo real por título de película.
     */
    private void configurarBusqueda() {
        if (txtBuscar == null) {
            return; // por si el FXML aún no tiene el campo
        }

        txtBuscar.textProperty().addListener((obs, oldValue, newValue) -> {
            String filtro = newValue != null ? newValue.trim().toLowerCase() : "";

            copiasFiltradas.setPredicate(copia -> {
                if (filtro.isEmpty()) {
                    return true;
                }
                if (copia.getPelicula() == null || copia.getPelicula().getTitulo() == null) {
                    return false;
                }
                String titulo = copia.getPelicula().getTitulo().toLowerCase();
                // Empieza por el texto escrito, p.ej. "el pa"
                return titulo.startsWith(filtro);
            });

            // Actualizar contador con las filas visibles
            lblTotalCopias.setText("Total de copias: " + copiasFiltradas.size());
        });
    }

    /**
     * Carga las copias del usuario en la lista base y actualiza el total.
     */
    private void cargarCopiasUsuario(User user) {
        copiasUsuario.clear();
        List<Copia> copias = copiaRepository.findByUser(user);
        copiasUsuario.addAll(copias);

        // Al recargar, se aplica el filtro actual automáticamente
        lblTotalCopias.setText("Total de copias: " + copiasFiltradas.size());
    }

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

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar borrado");
        confirmacion.setHeaderText("¿Seguro que quieres borrar esta copia?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait()
                .filter(boton -> boton == ButtonType.OK)
                .ifPresent(botonOk -> {
                    copiaRepository.delete(seleccionada);
                    cargarCopiasUsuario(simpleSessionService.getActive());
                });
    }

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

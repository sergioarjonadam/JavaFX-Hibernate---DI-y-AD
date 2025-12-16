package org.example.retoconjuntoad_di_2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.retoconjuntoad_di_2.model.copia.Copia;
import org.example.retoconjuntoad_di_2.model.copia.CopiaRepository;
import org.example.retoconjuntoad_di_2.model.pelicula.Pelicula;
import org.example.retoconjuntoad_di_2.model.pelicula.PeliculaRepository;
import org.example.retoconjuntoad_di_2.utils.DataProvider;
import org.example.retoconjuntoad_di_2.utils.JavaFXUtil;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para gestionar los detalles de una copia.
 * Permite visualizar, editar, guardar y eliminar copias.
 */
public class CopyDetailController implements Initializable {

    public Button btnGuardar;
    public Button btnEliminar;
    public Button btnCancelar;

    @FXML
    private Label lblId; // Etiqueta para mostrar el ID de la copia.

    @FXML
    private ComboBox<Pelicula> comboPelicula; // ComboBox para seleccionar una película.

    @FXML
    private Label lblTitulo; // Etiqueta para mostrar el título de la película.

    @FXML
    private Label lblGenero; // Etiqueta para mostrar el género de la película.

    @FXML
    private Label lblAnio; // Etiqueta para mostrar el año de la película.

    @FXML
    private ComboBox<String> comboEstado; // ComboBox para seleccionar el estado de la copia.

    @FXML
    private ComboBox<String> comboSoporte; // ComboBox para seleccionar el soporte de la copia.

    private Copia copia; // Objeto Copia que se está gestionando.
    private CopiaRepository copiaRepository; // Repositorio para gestionar las copias.
    private PeliculaRepository peliculaRepository; // Repositorio para gestionar las películas.

    /**
     * Inicializa el controlador y configura los elementos de la interfaz.
     *
     * @param url URL de inicialización.
     * @param resourceBundle Recursos de inicialización.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        copiaRepository = new CopiaRepository(DataProvider.getSessionFactory());
        peliculaRepository = new PeliculaRepository(DataProvider.getSessionFactory());

        // Configurar opciones de estado y soporte.
        comboEstado.getItems().addAll("Nueva", "Buena", "Usada", "Deteriorada");
        comboSoporte.getItems().addAll("DVD", "Blu-ray", "VHS");

        // Cargar todas las películas en el ComboBox.
        comboPelicula.getItems().setAll(peliculaRepository.findAll());

        // Actualizar los campos de información al cambiar la película seleccionada.
        comboPelicula.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> actualizarDatosPelicula(newVal)
        );
    }

    /**
     * Establece la copia que se va a gestionar y rellena los campos con sus datos.
     *
     * @param copia Objeto Copia a gestionar.
     */
    public void setCopia(Copia copia) {
        this.copia = copia;
        rellenarCampos();
    }

    /**
     * Rellena los campos de la interfaz con los datos de la copia.
     */
    private void rellenarCampos() {
        if (copia == null) {
            return;
        }

        // Mostrar el ID de la copia.
        if (copia.getId() != null) {
            lblId.setText(copia.getId().toString());
        } else {
            lblId.setText("-");
        }

        // Configurar la película seleccionada.
        if (copia.getPelicula() != null) {
            comboPelicula.getSelectionModel().select(copia.getPelicula());
            if (copia.getId() != null) {
                // Si la copia ya existe, no se permite cambiar de película.
                comboPelicula.setDisable(true);
            }
            actualizarDatosPelicula(copia.getPelicula());
        } else {
            actualizarDatosPelicula(null);
        }

        // Configurar estado y soporte.
        if (copia.getEstado() != null) {
            comboEstado.getSelectionModel().select(copia.getEstado());
        }
        if (copia.getSoporte() != null) {
            comboSoporte.getSelectionModel().select(copia.getSoporte());
        }
    }

    /**
     * Actualiza los campos de información de la película seleccionada.
     *
     * @param pelicula Película seleccionada.
     */
    private void actualizarDatosPelicula(Pelicula pelicula) {
        if (pelicula == null) {
            lblTitulo.setText("-");
            lblGenero.setText("-");
            lblAnio.setText("-");
            return;
        }

        lblTitulo.setText(pelicula.getTitulo() != null ? pelicula.getTitulo() : "-");
        lblGenero.setText(pelicula.getGenero() != null ? pelicula.getGenero() : "-");
        lblAnio.setText(pelicula.getAnio() != null ? pelicula.getAnio().toString() : "-");
    }

    /**
     * Guarda los cambios realizados en la copia.
     *
     * @param actionEvent Evento de acción.
     */
    @FXML
    public void guardar(ActionEvent actionEvent) {
        if (copia == null) {
            return;
        }

        Pelicula peliculaSeleccionada = comboPelicula.getSelectionModel().getSelectedItem();
        String estado = comboEstado.getSelectionModel().getSelectedItem();
        String soporte = comboSoporte.getSelectionModel().getSelectedItem();

        // Validar que todos los campos requeridos estén completos.
        if (peliculaSeleccionada == null || estado == null || soporte == null) {
            JavaFXUtil.showModal(
                    Alert.AlertType.WARNING,
                    "Guardar copia",
                    "Datos incompletos",
                    "Debes seleccionar película, estado y soporte."
            );
            return;
        }

        // Actualizar los datos de la copia.
        copia.setPelicula(peliculaSeleccionada);
        copia.setEstado(estado);
        copia.setSoporte(soporte);

        // Guardar la copia en el repositorio.
        copiaRepository.save(copia);

        JavaFXUtil.showModal(
                Alert.AlertType.INFORMATION,
                "Guardar copia",
                "Operación realizada",
                "La copia se ha guardado correctamente."
        );

        cerrarVentana();
    }

    /**
     * Elimina la copia gestionada.
     *
     * @param actionEvent Evento de acción.
     */
    @FXML
    public void eliminar(ActionEvent actionEvent) {
        if (copia == null || copia.getId() == null) {
            // Si la copia no está guardada, solo cerrar la ventana.
            cerrarVentana();
            return;
        }

        // Eliminar la copia del repositorio.
        copiaRepository.delete(copia);

        JavaFXUtil.showModal(
                Alert.AlertType.INFORMATION,
                "Eliminar copia",
                "Operación realizada",
                "La copia se ha eliminado correctamente."
        );

        cerrarVentana();
    }

    /**
     * Cancela la operación y cierra la ventana.
     *
     * @param actionEvent Evento de acción.
     */
    @FXML
    public void cancelar(ActionEvent actionEvent) {
        cerrarVentana();
    }

    /**
     * Cierra la ventana actual.
     */
    private void cerrarVentana() {
        Stage stage = (Stage) comboEstado.getScene().getWindow();
        stage.close();
    }
}

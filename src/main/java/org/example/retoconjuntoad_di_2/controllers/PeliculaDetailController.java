package org.example.retoconjuntoad_di_2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.retoconjuntoad_di_2.model.pelicula.Pelicula;
import org.example.retoconjuntoad_di_2.model.pelicula.PeliculaRepository;
import org.example.retoconjuntoad_di_2.utils.DataProvider;
import org.example.retoconjuntoad_di_2.utils.JavaFXUtil;

import java.net.URL;
import java.time.Year;
import java.util.ResourceBundle;

/**
 * Controlador para gestionar los detalles de una película.
 * Permite crear y guardar una nueva película en el sistema.
 */
public class PeliculaDetailController implements Initializable {

    @FXML private TextField txtTitulo;     // Campo de texto para ingresar el título de la película.
    @FXML private TextField txtGenero;     // Campo de texto para ingresar el género de la película.
    @FXML private TextField txtAnio;       // Campo de texto para ingresar el año de la película.
    @FXML private TextField txtDirector;   // Campo de texto para ingresar el director de la película.
    @FXML private TextArea txtDescripcion; // Área de texto para ingresar la descripción de la película.

    private PeliculaRepository peliculaRepository; // Repositorio para gestionar las películas.
    private static final short MIN_ANIO = 1900;     // Año mínimo permitido.

    /**
     * Inicializa el controlador y configura el repositorio de películas.
     *
     * @param url URL de inicialización.
     * @param resourceBundle Recursos de inicialización.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        peliculaRepository = new PeliculaRepository(DataProvider.getSessionFactory());

        int anioActual = Year.now().getValue();
        // Establecer texto de ayuda con el rango permitido, por ejemplo "1900 - 2025".
        txtAnio.setPromptText(MIN_ANIO + " - " + anioActual);
    }

    /**
     * Maneja el evento de guardar una nueva película.
     * Valida los campos obligatorios y guarda la película en el repositorio.
     *
     * @param actionEvent Evento de acción generado al presionar el botón de guardar.
     */
    @FXML
    public void guardar(ActionEvent actionEvent) {
        String titulo = txtTitulo.getText();
        String genero = txtGenero.getText();
        String anioStr = txtAnio.getText();
        String director = txtDirector.getText();
        String descripcion = txtDescripcion.getText();

        // Validar que los campos obligatorios no estén vacíos.
        if (titulo.isBlank() || genero.isBlank() || anioStr.isBlank()) {
            JavaFXUtil.showModal(
                    Alert.AlertType.ERROR,
                    "Datos incompletos",
                    "Faltan campos obligatorios",
                    "Título, género y año son obligatorios."
            );
            return;
        }

        Short anio;
        try {
            // Validar que el año sea un número válido.
            anio = Short.parseShort(anioStr);
        } catch (NumberFormatException e) {
            JavaFXUtil.showModal(
                    Alert.AlertType.ERROR,
                    "Año incorrecto",
                    "Formato inválido",
                    "Introduce un número válido para el año."
            );
            return;
        }

        int anioActual = Year.now().getValue();

        // Validar que el año esté dentro del rango permitido.
        if (anio < MIN_ANIO || anio > anioActual) {
            JavaFXUtil.showModal(
                    Alert.AlertType.ERROR,
                    "Año fuera de rango",
                    "Valor no realista",
                    "El año debe estar entre " + MIN_ANIO + " y " + anioActual + "."
            );
            return;
        }

        // Crear una nueva película con los datos ingresados.
        Pelicula pelicula = new Pelicula();
        pelicula.setTitulo(titulo);
        pelicula.setGenero(genero);
        pelicula.setAnio(anio);
        pelicula.setDirector(director);
        pelicula.setDescripcion(descripcion);

        // Guardar la película en el repositorio.
        peliculaRepository.save(pelicula);

        JavaFXUtil.showModal(
                Alert.AlertType.INFORMATION,
                "Película creada",
                "Operación completada",
                "La nueva película se ha registrado correctamente."
        );

        cerrarVentana();
    }

    /**
     * Maneja el evento de cancelar la operación.
     * Cierra la ventana actual sin guardar cambios.
     *
     * @param actionEvent Evento de acción generado al presionar el botón de cancelar.
     */
    @FXML
    public void cancelar(ActionEvent actionEvent) {
        cerrarVentana();
    }

    /**
     * Cierra la ventana actual.
     */
    private void cerrarVentana() {
        Stage st = (Stage) txtTitulo.getScene().getWindow();
        st.close();
    }
}

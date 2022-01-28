package mx.com.cuatronetworks.sensoresbpdp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.apache.log4j.Logger;
import software.amazon.awssdk.regions.Region;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import mx.com.cuatronetworks.sensoresbpdp.model.Pregunta;
import mx.com.cuatronetworks.sensoresbpdp.model.Respuesta;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javazoom.jl.decoder.JavaLayerException;
import software.amazon.awssdk.services.polly.model.OutputFormat;

/**
 * @author Fernando Sánchez Castro
 */
public class PrimaryController {
    static Logger logger = Logger.getLogger(PrimaryController.class);
    /** Elementos FXML de la interfaz gráfica*/
    @FXML
    private AnchorPane anchorPane;

	@FXML
	private Label preguntaLabel;
	
	@FXML
	private Label instruccionLabel;
	
	@FXML
	private Button botonSi;
	
	@FXML
	private Button botonNo;

    @FXML
    private Button botonIniciar;
	
	@FXML
	private ProgressBar barraProgreso;
	
	@FXML
	private Label tiempoLabel;

	/** Controlador de la ventana principal*/
    private AdminController parentController;
    private int intervaloCorrectas = 0;

	/** Elementos de las preguntas, tiempos y TextToSpeech */
	List<Respuesta> respuestasList = new ArrayList<>();
	List<Pregunta> preguntasList = new ArrayList<>();

    /** Variables asociadas a los elementos gráficos */
    private SimpleStringProperty preguntaActual;
    private SimpleStringProperty instruccionActual;

	Timer timer;
	TextToSpeech customPolly = null;
    int totalPreguntas = 0;
    int intQuestion = 0;
    Integer intQuestionFiltered = 1;
    double value = 0;
    
    boolean contesto = false;
    Integer minutos = 0, segundos = 0, milesimas = 0;
    private final int tiempoEspera = 7;
    Integer segundosxpregunta = tiempoEspera;
    String min = "", seg = "", mil = "";
    String tiempoInicio = "";
    String tiempoFinal = "";

    boolean isPreguntando = false;

    Timeline timeline;

    /**
     * Función que se ejecuta al inicializar la interfaz gráfica
     */
	@FXML
	private void initialize() {
        // Elementos Gráficos
        instruccionActual = new SimpleStringProperty("Bienvenido");
        preguntaActual = new SimpleStringProperty("Presione INICIAR cuando esté listo");
        instruccionLabel.textProperty().bind(instruccionActual);
        preguntaLabel.textProperty().bind(preguntaActual);
        botonNo.setVisible(false);
        botonSi.setVisible(false);
        // Inicializar amazon Polly
        customPolly = new TextToSpeech(Region.US_EAST_1);
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(barraProgreso.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(tiempoEspera), event -> {
                }, new KeyValue(barraProgreso.progressProperty(), 1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        logger.info("Clase: " + getClass().getName() + " Inicializada");
	}

    /**
     * Inicia las preguntas
     */
    @FXML
    private void start(){
        botonIniciar.setVisible(false);
        botonNo.setVisible(true);
        botonSi.setVisible(true);
        this.preguntasList = parentController.getPreguntasList();
        totalPreguntas = preguntasList.size();
        timer = new Timer(5, e -> {
            // Incrementa de 5 en 5 milisegundos
            milesimas += 5;
            // Cuando llega a 1000 = 1 segundo aumenta 1 segundo
            // y las milesimas de segundo de nuevo a 0
            if (milesimas == 1000) {
                milesimas = 0;
                segundos += 1;
                segundosxpregunta += 1;// segundo para cada pregunta
                // Si los segundos llegan a 60 entonces aumenta 1 los minutos
                // y los segundos vuelven a 0
                if (segundos == 60) {
                    segundos = 0;
                    minutos++;
                }
            }
            // Formato 00:00:000
            if (minutos < 10) {
                min = "0" + minutos;
            } else {
                min = minutos.toString();
            }
            if (segundos < 10) {
                seg = "0" + segundos;
            } else {
                seg = segundos.toString();
            }
            if (milesimas < 10) {
                mil = "00" + milesimas;
            } else if (milesimas < 100) {
                mil = "0" + milesimas;
            } else {
                mil = milesimas.toString();
            }
            // Se inseta el formato Tiempo : 00 : 00 : 00
            //tiempoLabel.setText("Tiempo : " + min + ":" + seg + ":" + mil);
            if (segundosxpregunta == tiempoEspera) {
                timeline.stop();
                segundosxpregunta = 0;
                barraProgreso.setProgress(0.001);
                if (contesto && intQuestion != 0) {
                    intQuestion++;
                }
                if (contesto && intQuestion == 0) {
                    Respuesta respuesta = new Respuesta();
                    respuesta.setCorrecto(0);
                    respuesta.setPregunta(intQuestion);
                    respuesta.setRespuesta(2);
                    respuestasList.add(respuesta);
                    tiempoFinal = min + ":" + seg + ":" + mil;
                    intQuestion++;
                }
                if (!contesto && intQuestion == 0 && segundos == tiempoEspera) {
                    Respuesta respuesta = new Respuesta();
                    respuesta.setCorrecto(0);
                    respuesta.setPregunta(intQuestion);
                    respuesta.setRespuesta(2);
                    respuestasList.add(respuesta);
                    tiempoFinal = min + ":" + seg + ":" + mil;
                    intQuestion++;
                }

                if (!contesto && intQuestion != 0 && segundos > tiempoEspera) {
                    enviarMarca(intQuestionFiltered, null);
                    Respuesta respuesta = new Respuesta();
                    respuesta.setCorrecto(0);
                    respuesta.setPregunta(intQuestion);
                    respuesta.setRespuesta(2);
                    respuestasList.add(respuesta);
                    tiempoFinal = min + ":" + seg + ":" + mil;
                    intQuestion++;
                }
                try {
                    GenerarPreguntas();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            value = segundosxpregunta * .14;
            if (minutos == 12) {
                ((Timer) (e.getSource())).stop();
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    /**
     * Función que se desencadena al hacer clic en el botón SI
     */
    @FXML
    private void contestaSi() {
        enviarMarca(intQuestionFiltered, true);
        contesto = true;
        String pregunta=preguntasList.get(intQuestion).getRespuesta_esperada();
        tiempoFinal = min + ":" + seg + ":" + mil;
        Respuesta respuesta = new Respuesta();
        respuesta.setCorrecto(pregunta.equals("si") || preguntasList.get(intQuestion).getRespuesta_esperada().equals("si")?1:0);
        respuesta.setPregunta(intQuestion);
        respuesta.setRespuesta(1);
        respuestasList.add(respuesta);
        segundosxpregunta = tiempoEspera;
        logger.info("Usuario contestó SI");
    }

    /**
     * Función que se desencadena al hacer clic en el botón NO
     */
    @FXML
    private void contestoNo() {
        enviarMarca(intQuestionFiltered, false);
        contesto = true;
        String pregunta = preguntasList.get(intQuestion).getReactivo();
        tiempoFinal = min + ":" + seg + ":" + mil;
        Respuesta respuesta = new Respuesta();
        respuesta.setCorrecto(preguntasList.get(intQuestion).getRespuesta_esperada().equals("no")?1:0);
        respuesta.setPregunta(intQuestion);
        respuesta.setRespuesta(0);
        respuestasList.add(respuesta);
        //intQuestion++;
        segundosxpregunta = tiempoEspera;
        logger.info("Usuario contestó NO");
    }

    /**
     * Obtiene la pregunta para mostrarla en pantalla y la reproduce
     */
    public void GenerarPreguntas() throws IOException, JavaLayerException {
        contesto = false;
        //TODO: Cuando terminan las preguntas da error aki
        Pregunta pregunta = preguntasList.get(intQuestion);
        Platform.runLater(() -> {
            if(intQuestion < totalPreguntas){
                preguntaActual.setValue(pregunta.getReactivo());
                //parentController.setPreguntaActual(pregunta.getReactivo());
                // Validar si es instrucción o espera
                if(pregunta.getTema().equalsIgnoreCase(Pregunta.INSTRUCCION)){
                    instruccionActual.setValue("Instrucción");
                    parentController.setNumPregunta(0);
                }else{
                    parentController.setNumPregunta(intQuestionFiltered);
                    intQuestionFiltered++;
                    if(intervaloCorrectas != 0){
                        if (intQuestion % intervaloCorrectas == 0) {
                            //preguntaLabel.setStyle("-fx-text-fill: #FF0000");
                            //instruccionLabel.setStyle("-fx-text-fill: #FF0000");
                            instruccionActual.setValue("Contesta con una mentira");
                        } else {
                            //preguntaLabel.setStyle("-fx-text-fill: #00FF00");
                            //instruccionLabel.setStyle("-fx-text-fill: #00FF00");
                            instruccionActual.setValue("Contesta con la verdad");
                        }
                    }else{
                        //preguntaLabel.setStyle("-fx-text-fill: #000000");
                        //instruccionLabel.setStyle("-fx-text-fill: #000000");
                        instruccionActual.setValue("Responde la siguiente pregunta:");
                    }
                }
            }
        });
        
        tiempoInicio = min + ":" + seg + ":" + mil;
        if (intQuestion < totalPreguntas) {
            InputStream speechStream = customPolly.synthesize(pregunta.getReactivo(), customPolly.getVoice() ,OutputFormat.MP3);
            AdvancedPlayer player = new AdvancedPlayer(speechStream,javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());
            player.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackStarted(PlaybackEvent evt) {
                    botonNo.setDisable(true);
                    botonSi.setDisable(true);

                }
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    boolean noPregunta = pregunta.getTema().equalsIgnoreCase(Pregunta.INSTRUCCION);
                    botonNo.setVisible(!noPregunta);
                    botonSi.setVisible(!noPregunta);
                    botonSi.setDisable(noPregunta);
                    botonNo.setDisable(noPregunta);
                    if(!noPregunta && !isPreguntando){
                        isPreguntando = true;
                        enviarBandera(isPreguntando);
                    }
                    timeline.play();
                }
            });
            Thread t1 = new Thread(() -> {
                try {
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                    logger.error(e);
                }
            });
            t1.run();
        } else {
            Platform.runLater(() -> {
                preguntaLabel.setText("Prueba Finalizada");
                parentController.getPreguntaLabel().setText("Prueba Finalizada");
                botonSi.setVisible(false);
                botonNo.setVisible(false);
            });
            timer.stop();
        }
    }

    /**
     * Envía los datos a la pantalla de Administración
     */
    private void enviarMarca(Integer numPregunta, Boolean respuesta){
        Platform.runLater(
            () -> {
                parentController.setBandera(true, respuesta);
                //graficas.setBanderas(true);
                //graficas.setContador_preguntas(numPregunta);
            }
        );
    }

    private void enviarBandera(boolean preguntando){
        Platform.runLater(
            () -> {
                logger.info("Bandera Enviada a la otra Clase");
            }
        );
    }

    /** GETTERS & SETTERS */

    public void setParentController(AdminController parentController) {
        this.parentController = parentController;
    }

    public Button getBotonSi() {
        return botonSi;
    }

    public Button getBotonNo() {
        return botonNo;
    }

    public void setIntervaloCorrectas(int intervaloCorrectas) {
        this.intervaloCorrectas = intervaloCorrectas;
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public void setAnchorPane(AnchorPane anchorPane) {
        this.anchorPane = anchorPane;
    }
}
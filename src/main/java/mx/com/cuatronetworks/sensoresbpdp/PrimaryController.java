package mx.com.cuatronetworks.sensoresbpdp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import com.amazonaws.services.polly.model.OutputFormat;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import mx.com.cuatronetworks.sensoresbpdp.model.Pregunta;
import mx.com.cuatronetworks.sensoresbpdp.model.Respuesta;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javazoom.jl.decoder.JavaLayerException;

/**
 * @author Fernando Sánchez Castro
 */
public class PrimaryController {
    /** Elementos FXML de la interfaz gráfica*/
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

	/** Elementos de las preguntas, tiempos y TextToSpeech */
	List<Respuesta> respuestasList = new ArrayList<>();
	List<Pregunta> preguntasList = new ArrayList<>();

    GraficasMedicion graficas;

	Timer timer;
	TextToSpeech customPolly = null;
    int totalPreguntas = 0;
    int intQuestion = 0;
    int modulo = 0;
    double value = 0;
    
    boolean contesto = false;
    Integer minutos = 0, segundos = 0, milesimas = 0;
    Integer segundosxpregunta = 5;
    String min = "", seg = "", mil = "";
    String tiempoInicio = "";
    String tiempoFinal = "";

    /**
     * Función que se ejecuta al inicializar la interfaz gráfica
     */
	@FXML
	private void initialize() {
        // Elementos Gráficos
		instruccionLabel.setText("Bienvenido");
		preguntaLabel.setText("Presione INICIAR cuando esté listo");
        botonNo.setVisible(false);
        botonSi.setVisible(false);
        // Inicializar amazon Polly
        customPolly = new TextToSpeech(Region.getRegion(Regions.US_EAST_1));
        graficas = new GraficasMedicion();
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
            //Incrementa de 5 en 5 milisegundos
            milesimas += 5;
            //Cuando llega a 1000 = 1 segundo aumenta 1 segundo
            //y las milesimas de segundo de nuevo a 0
            if (milesimas == 1000) {
                milesimas = 0;
                segundos += 1;
                segundosxpregunta += 1;//segundo para cada pregunta
                //Si los segundos llegan a 60 entonces aumenta 1 los minutos
                //y los segundos vuelven a 0
                if (segundos == 60) {
                    segundos = 0;
                    minutos++;
                }
            }
            //Formato 00:00:000
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
            //Se inseta el formato Tiempo : 00 : 00 : 00
            //tiempoLabel.setText("Tiempo : " + min + ":" + seg + ":" + mil);
            //System.out.println("Tiempo : " + min + ":" + seg + ":" + mil);
            // count = count + 1;
            if (segundosxpregunta == 5) {
                segundosxpregunta = 0;
                barraProgreso.setProgress(0.001);
                System.out.println("Tiempo inicial: " + barraProgreso.getProgress());
                if (contesto && intQuestion != 0) {
                    intQuestion++;
                }
                if (contesto && intQuestion == 0) {
                    System.out.println("No contesto true");
                    Respuesta respuesta = new Respuesta();
                    respuesta.setCorrecto(0);
                    respuesta.setPregunta(intQuestion);
                    respuesta.setRespuesta(2);
                    respuestasList.add(respuesta);
                    tiempoFinal = min + ":" + seg + ":" + mil;
                    System.out.println(preguntasList.get(intQuestion).getReactivo() + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " null" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
                    intQuestion++;
                }
                if (!contesto && intQuestion == 0 && segundos == 5) {
                    System.out.println("No contesto false pregunta 0");
                    Respuesta respuesta = new Respuesta();
                    respuesta.setCorrecto(0);
                    respuesta.setPregunta(intQuestion);
                    respuesta.setRespuesta(2);
                    respuestasList.add(respuesta);
                    tiempoFinal = min + ":" + seg + ":" + mil;
                    System.out.println(preguntasList.get(intQuestion).getReactivo() + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " null" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
                    intQuestion++;
                }

                if (!contesto && intQuestion != 0 && segundos > 5) {
                    System.out.println("No contesto false pregunta !=0 ");
                    Respuesta respuesta = new Respuesta();
                    respuesta.setCorrecto(0);
                    respuesta.setPregunta(intQuestion);
                    respuesta.setRespuesta(2);
                    respuestasList.add(respuesta);
                    tiempoFinal = min + ":" + seg + ":" + mil;
                    System.out.println(preguntasList.get(intQuestion).getReactivo() + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " null" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
                    intQuestion++;
                }
                try {
                    GenerarPreguntas();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            value = segundosxpregunta * .25;
            barraProgreso.setProgress(value);
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
        enviarMarca(intQuestion+1, botonSi.getText());
        contesto = true;
        String pregunta=preguntasList.get(intQuestion).getRespuesta_esperada();
        tiempoFinal = min + ":" + seg + ":" + mil;
        Respuesta respuesta = new Respuesta();
        respuesta.setCorrecto(pregunta.equals("si") || preguntasList.get(intQuestion).getRespuesta_esperada().equals("si")?1:0);
        respuesta.setPregunta(intQuestion);
        respuesta.setRespuesta(1);
        System.out.println(preguntasList.get(intQuestion).getReactivo() + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " si" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
        respuestasList.add(respuesta);
        segundosxpregunta = 5;
    }

    /**
     * Función que se desencadena al hacer clic en el botón NO
     */
    @FXML
    private void contestoNo() {
        enviarMarca(intQuestion+1, botonNo.getText());
        contesto = true;
        String pregunta=preguntasList.get(intQuestion).getReactivo();
        tiempoFinal = min + ":" + seg + ":" + mil;
        Respuesta respuesta = new Respuesta();
        // TODO: El verdadero y falso
        respuesta.setCorrecto(preguntasList.get(intQuestion).getRespuesta_esperada().equals("no")?1:0);
        respuesta.setPregunta(intQuestion);
        respuesta.setRespuesta(0);
        System.out.println(pregunta + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " no" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
        respuestasList.add(respuesta);
        //intQuestion++;
        segundosxpregunta = 5;
    }

    /**
     * Obtiene la pregunta para mostrarla en pantalla y la reproduce
     */
    public void GenerarPreguntas() throws IOException, JavaLayerException {
        System.out.println("Número de pregunta: " + intQuestion+1);
        contesto = false;
        String pregunta=preguntasList.get(intQuestion).getReactivo();
        //String pregunta = "Reproduciendo pregunta " + intQuestion ;
        Platform.runLater(() -> {
            preguntaLabel.setText(pregunta);
            parentController.getPreguntaLabel().setText(pregunta);
            if(intQuestion < totalPreguntas){
                modulo = intQuestion % 3;
                if (modulo == 0) {
                    preguntaLabel.setStyle("-fx-text-fill: #FF0000");
                    instruccionLabel.setStyle("-fx-text-fill: #FF0000");
                    instruccionLabel.setText("Contesta con una mentira");
                } else {
                    preguntaLabel.setStyle("-fx-text-fill: #00FF00");
                    instruccionLabel.setStyle("-fx-text-fill: #00FF00");
                    instruccionLabel.setText("Contesta con la verdad");
                }
            }
        });
        
        tiempoInicio = min + ":" + seg + ":" + mil;
        if (intQuestion < totalPreguntas) {
            InputStream speechStream = customPolly.synthesize(pregunta, OutputFormat.Mp3);
            AdvancedPlayer player = new AdvancedPlayer(speechStream,javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());

            player.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackStarted(PlaybackEvent evt) {
                    System.out.println("Inicia Reproduccion");
                    botonNo.setDisable(true);
                    botonSi.setDisable(true);
                }
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    System.out.println("Termina Reproduccion");
                    botonNo.setDisable(false);
                    botonSi.setDisable(false);
                }
            });
            player.play();
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
    private void enviarMarca(Integer numPregunta, String respuesta){
        Platform.runLater(
            () -> {
                parentController.getNumPreguntaLabel().setText(numPregunta.toString());
                parentController.getRespuestaLabel().setText(respuesta);
                parentController.setBandera(true);
                graficas.setBandera(true);
            }
        );
    }

    /** GETTERS & SETTERS */

    public AdminController getParentController() {
        return parentController;
    }

    public void setParentController(AdminController parentController) {
        this.parentController = parentController;
    }

    public Button getBotonSi() {
        return botonSi;
    }

    public void setBotonSi(Button botonSi) {
        this.botonSi = botonSi;
    }

    public Button getBotonNo() {
        return botonNo;
    }

    public void setBotonNo(Button botonNo) {
        this.botonNo = botonNo;
    }
}

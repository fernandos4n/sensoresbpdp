package mx.com.cuatronetworks.sensoresbpdp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.util.Duration;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
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
public class PrimaryController implements Runnable{
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
    double value = 0;
    
    boolean contesto = false;
    Integer minutos = 0, segundos = 0, milesimas = 0;
    private int tiempoEspera = 7;
    Integer segundosxpregunta = tiempoEspera;
    String min = "", seg = "", mil = "";
    String tiempoInicio = "";
    String tiempoFinal = "";

    boolean isPreguntando = false;

    MySwingWorker mySwingWorker;
    SwingWrapper<XYChart> sw;
    XYChart chart;

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
        // Descomentar customPolly
        customPolly = new TextToSpeech(Region.US_EAST_1);
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(barraProgreso.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(tiempoEspera), event -> {
                    System.out.println("5 segundosTermina2");
                }, new KeyValue(barraProgreso.progressProperty(), 1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
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
            //System.out.println("Tiempo : " + min + ":" + seg + ":" + mil);
            //count = count + 1;
            if (segundosxpregunta == tiempoEspera) {
                timeline.stop();
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
                if (!contesto && intQuestion == 0 && segundos == tiempoEspera) {
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

                if (!contesto && intQuestion != 0 && segundos > tiempoEspera) {
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
            value = segundosxpregunta * .14;
            //barraProgreso.setProgress(value);

            if (minutos == 12) {
                ((Timer) (e.getSource())).stop();
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private void go() {

        // Create Chart
        chart = QuickChart.getChart("SwingWorker XChart Real-time Demo", "Time", "Value", "randomWalk", new double[] { 0 }, new double[] { 0 });
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisTicksVisible(false);

        // Show it
        sw = new SwingWrapper<XYChart>(chart);
        sw.displayChart();

        mySwingWorker = new MySwingWorker();
        mySwingWorker.execute();
    }

    /**
     * Función que se desencadena al hacer clic en el botón SI
     */
    @FXML
    private void contestaSi() {
        //enviarMarca(intQuestion+1, botonSi.getText());
        contesto = true;
        String pregunta=preguntasList.get(intQuestion).getRespuesta_esperada();
        tiempoFinal = min + ":" + seg + ":" + mil;
        Respuesta respuesta = new Respuesta();
        respuesta.setCorrecto(pregunta.equals("si") || preguntasList.get(intQuestion).getRespuesta_esperada().equals("si")?1:0);
        respuesta.setPregunta(intQuestion);
        respuesta.setRespuesta(1);
        System.out.println(preguntasList.get(intQuestion).getReactivo() + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " si" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
        respuestasList.add(respuesta);
        segundosxpregunta = tiempoEspera;
    }

    /**
     * Función que se desencadena al hacer clic en el botón NO
     */
    @FXML
    private void contestoNo() {
        //enviarMarca(intQuestion+1, botonNo.getText());
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
        segundosxpregunta = tiempoEspera;
    }

    /**
     * Obtiene la pregunta para mostrarla en pantalla y la reproduce
     */
    public void GenerarPreguntas() throws IOException, JavaLayerException {
        contesto = false;
        Pregunta pregunta = preguntasList.get(intQuestion);
        Platform.runLater(() -> {
            if(intQuestion < totalPreguntas){
                preguntaActual.setValue(pregunta.getReactivo());
                parentController.setNumPregunta(intQuestion);
                //parentController.setPreguntaActual(pregunta.getReactivo());
                //parentController.getPreguntaLabel().setText(pregunta.getReactivo());
                //modificarLabelAdmin(pregunta.getReactivo());
                // Validar si es instrucción o espera
                if(pregunta.getTema().equalsIgnoreCase(Pregunta.INSTRUCCION)){
                    instruccionActual.setValue("Instrucción");
                }else{
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
                    System.out.println("Inicia Reproduccion");
                    botonNo.setDisable(true);
                    botonSi.setDisable(true);

                }
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    System.out.println("Termina Reproduccion");
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
            Thread t1 = new Thread() {
                public void run() {
                    try {
                        player.play();
                    } catch (JavaLayerException e) {
                        e.printStackTrace();
                    }
                }
            };
            t1.run();
            /*
            Platform.runLater(() -> {
                try { // TODO: revisar bien esto ggg
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            });*/

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
     * Modifica el Label en AdminController
     */
    private void modificarLabelAdmin(String valor){
        Platform.runLater(() -> {
            parentController.getPreguntaLabel().setText(valor);
        });
    }

    /**
     * Envía los datos a la pantalla de Administración
     */
    private void enviarMarca(Integer numPregunta, String respuesta){
        Platform.runLater(
            () -> {
                //parentController.getNumPreguntaLabel().setText(numPregunta.toString());
                //parentController.getRespuestaLabel().setText(respuesta);
                parentController.setBandera(true);
                //graficas.setBanderas(true);
                //graficas.setContador_preguntas(numPregunta);
            }
        );
    }

    private void enviarBandera(boolean preguntando){
        Platform.runLater(
                () -> {
                    System.out.println("Se envió la bandera xd");
                    //graficas.setBandera(preguntando);
                }
        );
    }

    @Override
    public void run() {

    }

    private class MySwingWorker extends SwingWorker<Boolean, double[]> {

        LinkedList<Double> fifo = new LinkedList<Double>();

        public MySwingWorker() {

            fifo.add(0.0);
        }



        @Override
        protected Boolean doInBackground() throws Exception {

            while (!isCancelled()) {

                fifo.add(fifo.get(fifo.size() - 1) + Math.random() - .5);
                if (fifo.size() > 500) {
                    fifo.removeFirst();
                }

                double[] array = new double[fifo.size()];
                for (int i = 0; i < fifo.size(); i++) {
                    array[i] = fifo.get(i);
                }
                publish(array);

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    // eat it. caught when interrupt is called
                    System.out.println("MySwingWorker shut down.");
                }

            }

            return true;
        }

        @Override
        protected void process(List<double[]> chunks) {

            System.out.println("number of chunks: " + chunks.size());

            double[] mostRecentDataSet = chunks.get(chunks.size() - 1);

            chart.updateXYSeries("randomWalk", null, mostRecentDataSet, null);
            sw.repaintChart();

            long start = System.currentTimeMillis();
            long duration = System.currentTimeMillis() - start;
            try {
                Thread.sleep(40 - duration); // 40 ms ==> 25fps
                // Thread.sleep(400 - duration); // 40 ms ==> 2.5fps
            } catch (InterruptedException e) {
            }

        }
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

    public int getIntervaloCorrectas() {
        return intervaloCorrectas;
    }

    public void setIntervaloCorrectas(int intervaloCorrectas) {
        this.intervaloCorrectas = intervaloCorrectas;
    }
}

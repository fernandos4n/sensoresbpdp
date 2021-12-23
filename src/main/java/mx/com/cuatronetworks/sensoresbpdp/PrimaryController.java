package mx.com.cuatronetworks.sensoresbpdp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Timer;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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

public class PrimaryController {
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

    private AdminController parentController;
	
	List<Respuesta> respuestasList = new ArrayList<Respuesta>();
	List<Pregunta> preguntasList = new ArrayList<Pregunta>();
	
	static final String CSV_FILENAME = "/home/fernandosanchez/Documentos/reactivos/4NReactivos.csv";
	
	Timer timer;
	TextToSpeech customPolly = null;
	int score = 0;
    int count = 0;
    int totalPreguntas = 0;
    int intQuestion = 0;
    int modulo = 0;
    double value = 0;
    
    boolean contesto = false;
    Integer minutos = 0, segundos = 0, milesimas = 0;
    Integer segundosxpregunta = 5;
    String min = "", seg = "", mil = "";
    int cadaTiempo = 5;
    String tiempoInicio = "";
    String tiempoFinal = "";
	
	@FXML
	private void initialize() {
		System.out.println("Inicializando");
        customPolly = new TextToSpeech(Region.getRegion(Regions.US_EAST_1));
        botonNo.setVisible(false);
        botonSi.setVisible(false);
	}

    @FXML
    private void start(){
        botonIniciar.setVisible(false);
        botonNo.setVisible(true);
        botonSi.setVisible(true);
        this.preguntasList = parentController.getPreguntasList();
        totalPreguntas = preguntasList.size();
        timer = new Timer(5, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                System.out.println("Tiempo : " + min + ":" + seg + ":" + mil);
                // count = count + 1;
                if (segundosxpregunta == 5) {
                    segundosxpregunta = 0;
                    barraProgreso.setProgress(0.001);
                    System.out.println("Tiempo inicial: " + barraProgreso.getProgress());
                    if (contesto == true && intQuestion != 0) {
                        intQuestion++;
                    }
                    if (contesto == true && intQuestion == 0) {
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
                    if (contesto == false && intQuestion == 0 && segundos == 5) {
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

                    if (contesto == false && intQuestion != 0 && segundos > 5) {
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
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }
	
    @FXML
    private void switchToSecondary() throws IOException {
        HelloApplication.setRoot("secondary");
    }

    @FXML
    private void contestaSi() throws IOException{
        contesto = true;
        String pregunta=preguntasList.get(intQuestion).getRespuesta_esperada();
        tiempoFinal = min + ":" + seg + ":" + mil;
        Respuesta respuesta = new Respuesta();
        if (pregunta == "si" || preguntasList.get(intQuestion).getRespuesta_esperada().equals("si")) {
            respuesta.setCorrecto(1);
            respuesta.setPregunta(intQuestion);
            respuesta.setRespuesta(1);
            System.out.println(preguntasList.get(intQuestion).getReactivo() + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " si" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
        } else {
            respuesta.setCorrecto(0);
            respuesta.setPregunta(intQuestion);
            respuesta.setRespuesta(1);
            System.out.println(preguntasList.get(intQuestion).getReactivo() + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " si" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
        }
        respuestasList.add(respuesta);
        intQuestion++;
        segundosxpregunta = 0;
        try {
            GenerarPreguntas();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void contestoNo() throws IOException{
        contesto = true;
        String pregunta=preguntasList.get(intQuestion).getReactivo();
        tiempoFinal = min + ":" + seg + ":" + mil;
        Respuesta respuesta = new Respuesta();
        if (preguntasList.get(intQuestion).getRespuesta_esperada() == "no" || preguntasList.get(intQuestion).getRespuesta_esperada().equals("no")) {
            respuesta.setCorrecto(1);
            respuesta.setPregunta(intQuestion);
            respuesta.setRespuesta(0);
            System.out.println(pregunta + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " no" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
        } else {
            respuesta.setCorrecto(0);
            respuesta.setPregunta(intQuestion);
            respuesta.setRespuesta(0);
            System.out.println(pregunta + " RE: " + preguntasList.get(intQuestion).getRespuesta_esperada() + " R: " + " no" + " Tiempo inicio : " + tiempoInicio + " Tiempo final : " + tiempoFinal);
        }
        respuestasList.add(respuesta);
        intQuestion++;
        segundosxpregunta = 0;
        try {
            GenerarPreguntas();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getPreguntas() throws IOException {
        preguntasList = (List<Pregunta>) new CsvToBeanBuilder(new FileReader(CSV_FILENAME))
                .withType(Pregunta.class).withSkipLines(1)
                .build()
                .parse();
        totalPreguntas = preguntasList.size();
    }

    public void GenerarPreguntas() throws IOException, JavaLayerException {
        contesto = false;
        String pregunta=preguntasList.get(intQuestion).getReactivo();
        //String pregunta = "Reproduciendo pregunta " + intQuestion ;
        Platform.runLater(new Runnable() { 
            @Override
            public void run() {
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
            }
        });
        
        tiempoInicio = min + ":" + seg + ":" + mil;
        barraProgreso.setProgress(1);
        if (intQuestion < totalPreguntas) {
           System.out.println(pregunta);
           customPolly.play(pregunta);
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    preguntaLabel.setText("Prueba finalizada");
                    botonSi.setVisible(false);
                    botonNo.setVisible(false);
                }
            });
            timer.stop();
        }
    }

    public AdminController getParentController() {
        return parentController;
    }

    public void setParentController(AdminController parentController) {
        this.parentController = parentController;
    }
}

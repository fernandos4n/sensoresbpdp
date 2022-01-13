package mx.com.cuatronetworks.sensoresbpdp;

import com.opencsv.bean.CsvToBeanBuilder;
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import mx.com.cuatronetworks.sensoresbpdp.model.Pregunta;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AdminController {
    // Elementos JAVAFX
    @FXML
    private Label nombreCSVLabel;

    @FXML
    private Label numPreguntaLabel;

    @FXML
    private Label respuestaLabel;

    @FXML
    private Label preguntaLabel;

    @FXML
    private Button cargaCSVButton;

    @FXML
    private Button iniciarButton;

    @FXML
    private Button salirButton;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TabPane tabPane;

    @FXML
    private CheckBox verdaderoFalso;

    @FXML
    private TextField tiempoLecturaField;

    @FXML
    private TextField intervaloCorrectasField;

    @FXML
    private NumberAxis xAxisTOBII;

    @FXML
    private NumberAxis yAxisTOBII;

    @FXML
    private LineChart<Number, Number> graficaTOBII;
    
    long time = System.currentTimeMillis();

    // Markers
    final List<XYSeries> markers = new ArrayList<XYSeries>();
    private boolean bandera = false;
    private boolean banderaCalculos = false;
    private Double sumaLecturasPPG;
    private List<Double> lecturasPPG;

    // Preguntas
    private int numPregunta;
    private List<Pregunta> preguntasList;
    private int totalPreguntas = 0;
    public static int contador_preguntas = 1;
    public static int respuesta = 0;

    // Tiempos
    private int tiempo_calibracion = 60;
    private Integer tiempo_lectura = 60;
    public long ultimoTiempo = 0;
    long date_ini;
    private Integer segundosLectura = 10;
    private Double media = 0.0;
    private Double sumaCuadrados = 0.0;
    private Double varianza = 0.0;

    // Controlador Padre
    HelloApplication mainApp;

    // Seleccionar archivo
    FileChooser fileChooser;

    // Archivo CSV para Lectura
    File csvFile;

    // Elementos Gráfica TOBII
    XYChart.Series<Number, Number> seriesTOBIIDerecho;
    XYChart.Series<Number, Number> seriesTOBIIIzquierdo;

    // Archivos CSV para Escritura
    private FileWriter csvET;
    private FileWriter contadorTiempos;
    private int contadorLineasPPG = 0;
    private int contadorLineasGSR = 0;
    private int contadorLineasET = 0;
    private int contadorLineasTiempos = 0;

    // Varios
    Integer i = 0, j = 0, k = 0, l = 0;
    int numLecturasPPG = 0;
    
    public void escribirET(double izquierdo, double derecho, double time, int numPregunta) {
    	try {
            if (contadorLineasET == 0) {
                //csvET.write("valor_promedio,ts,valor_promedio,valor_promedio,valor_promedio,evento\n");
                csvET.write("left,right,timestamp,pregunta\n");
                contadorLineasET += 1;
            } else {
            	if (izquierdo > 5 || derecho > 5) {
            		contadorLineasET += 1;
            	} else {
            		contadorLineasET += 1;
                    String dato = String.valueOf(izquierdo) + ","
                            + String.valueOf(derecho) + ","
                            + String.valueOf(time) + ","
                            + String.valueOf(numPregunta);
                    csvET.write(dato + "\n");
            	}
            }
        } catch (IOException ex) {
            System.out.println("Error al escribir archivo ET: " + ex);
        }
    }

    public void escribirET(String dato) {
        try {
            if (contadorLineasET == 0) {
                //csvET.write("valor_promedio,ts,valor_promedio,valor_promedio,valor_promedio,evento\n");
                csvET.write("left,right,timestamp,pregunta\n");
                contadorLineasET += 1;
            } else {
                contadorLineasET += 1;
                csvET.write(dato + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Error al escribir archivo ET: " + ex);
        }
    }

    @FXML
    private void initialize(){
        // Elementos de la interfaz gráfica
        nombreCSVLabel.setText("Ningún Archivo CSV seleccionado");
        iniciarButton.setDisable(true);
        tiempoLecturaField.setText(tiempo_lectura.toString());
        intervaloCorrectasField.setText("3");
        tabPane.getTabs().get(1).setDisable(true);
        tabPane.getTabs().get(2).setDisable(true);
        try {
            csvET = new FileWriter("et_" + time + ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMainApp(HelloApplication mainApp){
        this.mainApp = mainApp;
    }

    /**
     * Función para cargar el Archivo CSV al programa
     */
    @FXML
    private void loadCSV(){
        fileChooser = new FileChooser();
        // Extensión CSV
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV", "*.csv")
        );
        fileChooser.setTitle("Selecciona un archivo CSV");
        csvFile = fileChooser.showOpenDialog(anchorPane.getScene().getWindow());

        if(csvFile != null){
            nombreCSVLabel.setText(csvFile.getName());
            try {
                preguntasList = (List<Pregunta>) new CsvToBeanBuilder(new FileReader(csvFile.getAbsolutePath()))
                        .withType(Pregunta.class).withSkipLines(1)
                        .build()
                        .parse();
                totalPreguntas = preguntasList.size();
                nombreCSVLabel.setText(nombreCSVLabel.getText() + "\n" + totalPreguntas + " Reactivos Cargados");
                iniciarButton.setDisable(false);
                tabPane.getTabs().get(1).setDisable(false);
            }catch (Exception e){
                e.printStackTrace();
                nombreCSVLabel.setText("Archivo Inválido!");
            }
        }
    }
    
    int contadorLecturasET = 0;
    
    Process process;
    
    void detenerLectura() {
    	if (process.isAlive()) {
    		process.destroy();
    		System.out.println("Proceso detenido ET");
    	}
    	try {
			csvET.close();
			System.out.println("Archivo de lecturas de ET cerrado");
		} catch (IOException e) {
			System.out.println("Error al cerrar el archivo de lecturas");
		}
    }

    private void obtenerDatosTobii() {

        try {
            //Process process = new ProcessBuilder("/home/edgar/Documentos/Git4N/dpr-cabinas/codigos-eyetracker/full_script_v2").start();
            //process = Runtime.getRuntime().exec("/home/edgar/Documentos/Git4N/dpr-cabinas/codigos-eyetracker/full_script_v2");
            String path = "src/main/resources/script_tobii/full_script_v2";
            File file = new File(path);
            process = Runtime.getRuntime().exec(file.getAbsolutePath());
            InputStream processInputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(processInputStream));
            String line = reader.readLine();
            //double derecho = 0.0;
            //double izquierdo = 0.0;
            while ((line = reader.readLine()) != null && !reader.equals("exit")) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    System.out.println("Error en thread de eye tracker: " + ex);
                }
            	
                time = System.currentTimeMillis();
                String[] parts = line.split(",");
                double derecho = Double.valueOf(parts[0]);
                double izquierdo = Double.valueOf(parts[1]);
                //System.out.println("IZQ: " + izquierdo + ", DER: " + derecho);
                System.out.println("Contador: " + contadorLecturasET);
                contadorLecturasET++;
                    if (numPregunta == 0 || numPregunta == 1 || numPregunta == 2) {
                        /*escribirET(
                                String.valueOf(izquierdo) + ","
                                        + String.valueOf(derecho) + ","
                                        + String.valueOf(time) + ","
                                        + String.valueOf(0)
                        );*/
                        escribirET(izquierdo,derecho,time,0);
                    } else {
                        /*escribirET(
                                String.valueOf(izquierdo) + ","
                                        + String.valueOf(derecho) + ","
                                        + String.valueOf(time) + ","
                                        + String.valueOf(numPregunta)
                        );*/
                        escribirET(izquierdo,derecho,time,numPregunta);
                    }
                

                //double suma = (derecho + izquierdo) / 2;

                //SeriesTobbi.add(time, suma);
                Platform.runLater(
                        () -> {
                            if (seriesTOBIIDerecho.getData().size() == 0) {
                                if (derecho == 0.0) {
                                    seriesTOBIIDerecho.getData().add(new XYChart.Data<Number, Number>(time,2.5));
                                } else {
                                    seriesTOBIIDerecho.getData().add(new XYChart.Data<Number, Number>(time,derecho));
                                }
                                if (izquierdo == 0.0) {
                                    seriesTOBIIIzquierdo.getData().add(new XYChart.Data<Number, Number>(time,2.5));
                                } else {
                                    seriesTOBIIIzquierdo.getData().add(new XYChart.Data<Number, Number>(time,izquierdo));
                                }
                            } else {
                                if (derecho == 0.0) {
                                    XYChart.Data<Number, Number> previo = seriesTOBIIDerecho.getData().get(seriesTOBIIDerecho.getData().size() - 1);
                                    double previoY = previo.getYValue().doubleValue();
                                    seriesTOBIIDerecho.getData().add(new XYChart.Data<Number, Number>(time,previoY));
                                } else {
                                    seriesTOBIIDerecho.getData().add(new XYChart.Data<Number, Number>(time,derecho));
                                }
                                if (izquierdo == 0.0) {
                                    XYChart.Data<Number, Number> previo = seriesTOBIIIzquierdo.getData().get(seriesTOBIIIzquierdo.getData().size() - 1);
                                    double previoY = previo.getYValue().doubleValue();
                                    seriesTOBIIIzquierdo.getData().add(new XYChart.Data<Number, Number>(time,previoY));
                                } else {
                                    seriesTOBIIIzquierdo.getData().add(new XYChart.Data<Number, Number>(time,izquierdo));
                                }
                            }

                        	/*seriesTOBIIDerecho.getData().add(new XYChart.Data<Number, Number>(time,derecho));
                        	seriesTOBIIIzquierdo.getData().add(new XYChart.Data<Number, Number>(time,izquierdo));*/

                            XYChart.Data<Number, Number> minder = seriesTOBIIDerecho.getData().get(0);
                            xAxisTOBII.setLowerBound(minder.getXValue().doubleValue());
                            xAxisTOBII.setUpperBound(minder.getXValue().doubleValue() + 3000);

                            if(seriesTOBIIIzquierdo.getData().size()>120) {
                                seriesTOBIIIzquierdo.getData().remove(0,100);
                                seriesTOBIIDerecho.getData().remove(0,100);
                            }

                            if(graficaTOBII.getData().size()<1) {
                                graficaTOBII.getData().add(seriesTOBIIIzquierdo);
                                graficaTOBII.getData().add(seriesTOBIIDerecho);
                            }
                            
                            
                            	
                            /*XYChart.Data<Number, Number> minizq = seriesTOBIIIzquierdo.getData().get(0);
                            xAxisTOBII.setLowerBound(minizq.getXValue().doubleValue());
                            xAxisTOBII.setUpperBound(minizq.getXValue().doubleValue() + 500);*/
                        }
                );

            }
        } catch (IOException ex) {
            System.out.println("Error lectura de output Tobii: " + ex);
        }
    }

    /**
     * Inicia la interfaz de las preguntas y las gráficas
     */
    @FXML
    private void iniciarPreguntas() throws IOException {
        // Inicializar variables
        tiempo_lectura = Integer.parseInt(tiempoLecturaField.getText());
        numLecturasPPG = 0;
        sumaLecturasPPG = 0.0;
        sumaCuadrados = 0.0;
        media = 512.0;
        lecturasPPG = new ArrayList<>();
        long tiempo = (new Date()).getTime();
        ultimoTiempo = (new Date()).getTime();
        this.date_ini = (new Date()).getTime();

        // Elementos Gráficos
        tabPane.getSelectionModel().selectNext();
        iniciarButton.setDisable(true);
        cargaCSVButton.setDisable(true);
        verdaderoFalso.setDisable(true);

        // Gráfica TOBII
        seriesTOBIIDerecho = new XYChart.Series<Number, Number>();
        seriesTOBIIIzquierdo = new XYChart.Series<Number, Number>();
        seriesTOBIIDerecho.setName("TOBII Ojo Derecho");
        seriesTOBIIIzquierdo.setName("TOBII Ojo Izquierdo");

        xAxisTOBII.setForceZeroInRange(false);
        xAxisTOBII.setTickLabelsVisible(true);
        xAxisTOBII.setTickMarkVisible(true);
        xAxisTOBII.setAutoRanging(false);

        //yAxisTOBII.setAutoRanging(true);
        yAxisTOBII.setLowerBound(1.0);
        yAxisTOBII.setUpperBound(4.0);
        yAxisTOBII.setForceZeroInRange(false);

        //graficaTOBII.legendVisibleProperty().setValue(false); //Quizá necesitemos especificar qué es cada color :v
        graficaTOBII.setCreateSymbols(false);
        graficaTOBII.setAnimated(false);

        // Carga la segunda ventana
        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        // Si tenemos la segunda pantalla
        Screen pantalla2 = Screen.getScreens().size()>1?Screen.getScreens().get(1):Screen.getPrimary();
        Parent root = loader.load();
        PrimaryController preguntas = loader.getController();
        // Indicarle quien es su 'Padre'
        preguntas.setParentController(this);
        // Pasarle datos de configuración
        if(verdaderoFalso.isSelected()){
            preguntas.getBotonNo().setText("Falso");
            preguntas.getBotonSi().setText("Verdadero");
        }
        preguntas.setIntervaloCorrectas(Integer.parseInt(intervaloCorrectasField.getText()));

        Stage stage = new Stage();
        stage.setX(pantalla2.getVisualBounds().getMinX());
        stage.setY(pantalla2.getVisualBounds().getMinY());
        stage.setWidth(pantalla2.getVisualBounds().getWidth());
        stage.setHeight(pantalla2.getVisualBounds().getHeight());
        stage.setScene(new Scene(root));
        stage.setTitle("Preguntas");
        stage.show();
        stage.setMaximized(true);

        // Manejar el cierre de la ventana
        // Detener el contador
        stage.setOnCloseRequest( event -> {
            if(preguntas.timer != null)
                preguntas.timer.stop();
            iniciarButton.setDisable(false);
            cargaCSVButton.setDisable(false);
            verdaderoFalso.setDisable(false);
            tabPane.getSelectionModel().selectFirst();
            this.detenerLectura();
        });
        
        Thread t1 = new Thread() {
            public void run() {
                obtenerDatosTobii();
            }
        };
        t1.start();
    }

    @FXML
    private void salir(){
    	
        System.exit(0);
    }

    /* GETTERS & SETTERS */

    public List<Pregunta> getPreguntasList() {
        return preguntasList;
    }

    public void setPreguntasList(List<Pregunta> preguntasList) {
        this.preguntasList = preguntasList;
    }

    public int getTotalPreguntas() {
        return totalPreguntas;
    }

    public void setTotalPreguntas(int totalPreguntas) {
        this.totalPreguntas = totalPreguntas;
    }

    public Label getPreguntaLabel() {
        return preguntaLabel;
    }

    public void setPreguntaLabel(Label preguntaLabel) {
        this.preguntaLabel = preguntaLabel;
    }

    public Label getNumPreguntaLabel() {
        return numPreguntaLabel;
    }

    public void setNumPreguntaLabel(Label numPreguntaLabel) {
        this.numPreguntaLabel = numPreguntaLabel;
    }

    public Label getRespuestaLabel() {
        return respuestaLabel;
    }

    public void setRespuestaLabel(Label respuestaLabel) {
        this.respuestaLabel = respuestaLabel;
    }

    public boolean isBandera() {
        return bandera;
    }

    public void setBandera(boolean bandera) {
        this.bandera = bandera;
    }

    public int getNumPregunta() {
        return numPregunta;
    }

    public void setNumPregunta(int numPregunta) {
        this.numPregunta = numPregunta;
    }
}

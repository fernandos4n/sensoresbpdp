package mx.com.cuatronetworks.sensoresbpdp;

import com.opencsv.bean.CsvToBeanBuilder;
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import mx.com.cuatronetworks.sensoresbpdp.model.Pregunta;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;

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
import java.util.Optional;

public class AdminController {
    static Logger logger = Logger.getLogger(AdminController.class);
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
    private Button calibrarButton;

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
    private TextField intervaloCorrectasField;

    @FXML
    private NumberAxis xAxisTOBII;

    @FXML
    private NumberAxis yAxisTOBII;

    @FXML
    private LineChart<Number, Number> graficaTOBII;

    @FXML
    private NumberAxis xAxisTOBIIGaze;

    @FXML
    private NumberAxis yAxisTOBIIGaze;

    @FXML
    private LineChart<Number, Number> graficaTOBIIGaze;

    // Markers
    final List<XYSeries> markers = new ArrayList<XYSeries>();
    private boolean bandera = false;
    private boolean banderaCalculos = false;
    private Double sumaLecturasPPG;
    private List<Double> lecturasPPG;

    // Preguntas
    private SimpleIntegerProperty numPregunta = new SimpleIntegerProperty();
    private SimpleStringProperty preguntaActual = new SimpleStringProperty();
    private List<Pregunta> preguntasList;
    private int totalPreguntas = 0;
    public static int contador_preguntas = 1;
    private Boolean respuesta = false;
    Stage stagePreguntas;

    // Tiempos
    long date_ini;
    private Double media = 0.0;
    private Double sumaCuadrados = 0.0;
    private Double varianza = 0.0;

    // Controlador Padre
    HelloApplication mainApp;

    // Controlador Hijo
    PrimaryController preguntas;

    // Seleccionar archivo
    FileChooser fileChooser;

    // Archivo CSV para Lectura
    File csvFile;

    // Proceso
    Process tobiiProcess;

    // Elementos Gráfica TOBII
    XYChart.Series<Number, Number> seriesTOBIIDerecho;
    XYChart.Series<Number, Number> seriesTOBIIIzquierdo;

    // Elementos Gráfica Gaze
    XYChart.Series<Number, Number> seriesTOBIIRightGaze;
    XYChart.Series<Number, Number> seriesTOBIILeftGaze;

    // EyeTracker
    private boolean isCalibrado = false;

    // Arduino GSR y PPG
    PanamaHitek_Arduino arduinoPPG = new PanamaHitek_Arduino();
    PanamaHitek_Arduino arduinoGSR = new PanamaHitek_Arduino();

    // Puertos y Bauding Sensores GSR y PPG
    private final String puertoSerialGSR = "/dev/ttyACM0";
    private final String puertoSerialPPG = "/dev/ttyUSB0";
    private final int baudingPPG = 115200;
    private final int baudingGSR = 115200;

    // Archivos CSV para Escritura
    private FileWriter csvET;
    private FileWriter csvPPG;
    private FileWriter csvGSR;
    private int contadorLineasPPG = 0;
    private int contadorLineasGSR = 0;
    private int contadorLineasET = 0;

    // Tiempo para nombres de archivos
    long currentTime;

    // Varios
    Integer i = 0, k = 0;
    int numLecturasPPG = 0;
    
    public void escribirPPG(int valor, long time, int numPregunta) {
        try {
            if (contadorLineasPPG == 0) {
                csvPPG.write("valor,timestamp,pregunta\n");
                contadorLineasPPG += 1;
            } else {
            	String dato = String.valueOf(valor) + ","
                        + String.valueOf(time) + ","
                        + String.valueOf(numPregunta);
                System.out.println("dato: " + dato);
                csvPPG.write(dato + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Error al escribir archivo PPG: " + ex);
        }
    }

    public void escribirGSR(int valor, long time, int numPregunta) {
        try {
            if (contadorLineasGSR == 0) {
                csvGSR.write("valor,timestamp,pregunta\n");
                contadorLineasGSR += 1;
            } else {
                String dato = String.valueOf(valor) + ","
                        + String.valueOf(time) + ","
                        + String.valueOf(numPregunta);
                csvGSR.write(dato + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Error al escribir archivo GSR: " + ex);
        }
    }
    
    public void escribirET() throws IOException {
    	for (String dato : datosTobii) {
    		try {
				csvET.write(dato + "\n");
			} catch (IOException e) {
				System.out.println("Error al escribir archivo ET: " + e);
			}
    	}
    	csvET.close();
    }

    @FXML
    private void initialize(){
        // Elementos de la interfaz gráfica
        nombreCSVLabel.setText("Ningún Archivo CSV seleccionado");
        numPreguntaLabel.textProperty().bind(Bindings.convert(numPregunta));
        preguntaLabel.textProperty().bind(Bindings.convert(preguntaActual));
        iniciarButton.setDisable(true);
        intervaloCorrectasField.setText("0");
        verdaderoFalso.setSelected(true);
        tabPane.getTabs().get(1).setDisable(true);
        tabPane.getTabs().get(2).setDisable(true);
        logger.info("Clase: " + getClass().getName() + " Inicializada");
    }

    public void setMainApp(HelloApplication mainApp){
        this.mainApp = mainApp;
    }

    /**
     * Función para calibrar el EyeTracker
     */
    @FXML
    private void calibrarEyeTracker() throws IOException {
        Process procesoCalibracion = Runtime.getRuntime().exec("/opt/TobiiTechConfigurationApplication/tobiitechconfigurationapplication");
        while (procesoCalibracion.isAlive()){
            //Pos esperar ni pp
            //Da error en el IDE porque sale como loop "infinito"
        }
        isCalibrado = true;
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
                logger.info(nombreCSVLabel.getText());
                iniciarButton.setDisable(false);
                tabPane.getTabs().get(1).setDisable(false);
                tabPane.getTabs().get(2).setDisable(false);
            }catch (Exception e){
                e.printStackTrace();
                nombreCSVLabel.setText("Archivo Inválido!");
                logger.warn(nombreCSVLabel.getText());
            }
        }
    }
    
    void detenerLecturaTobii() throws IOException {
        if(tobiiProcess != null){
            if (tobiiProcess.isAlive()) {
                tobiiProcess.destroy();
                logger.info("Proceso EyeTracker detenido");
            }
            escribirET();
            logger.info("Archivo de lecturas de EyeTracker cerrado");
        }
    }

    /* SERIALPORT LISTENERS */
    /**
     * Serial Listener para GSR
     */
    SerialPortEventListener ListenerGSR = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                if (arduinoGSR.isMessageAvailable()) {
                    Double data;
                    currentTime = System.currentTimeMillis();
                    try {
                        data = Double.parseDouble(arduinoGSR.printMessage());
                        int value = (int) Math.round(data);
                        //escribirGSR(value, time, numPregunta);
                    } catch (ArduinoException | NumberFormatException | SerialPortException ex) {
                        System.out.println("Error data: " + ex);
                    }
                }
            } catch (ArduinoException | SerialPortException ex) {
                logger.error(ex);
            }
        }
    };

    /**
     * Serial Listener para PPG
     */
    SerialPortEventListener ListenerPPG = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            k += 1;
            try {
                if(arduinoPPG.isMessageAvailable()) {
                    i++;
                    Double data = 0.0;
                    data = Double.parseDouble(arduinoPPG.printMessage());
                    int value = (int) Math.round(data);
                    currentTime = System.currentTimeMillis();
                    //escribirPPG(value, time, numPregunta);
                }
            } catch (ArduinoException | SerialPortException ex) {
                logger.error(ex);
            }
        }
    };

    List<String> datosTobii = new ArrayList<String>();

    private void obtenerDatosTobii() {
        try {
            tobiiProcess = Runtime.getRuntime().exec("./full_script");
            InputStream tobiiProcessInputStream = tobiiProcess.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(tobiiProcessInputStream));
            String line;
            // Inicializamos variables de datos de los ojos

            while ((line = reader.readLine()) != null && !reader.equals("exit")) {
                currentTime = System.currentTimeMillis();
                String[] inputTobii = line.split(",");

                double rightPupilDiameter = Double.valueOf(inputTobii[0]);
                double leftPupilDiameter = Double.valueOf(inputTobii[1]);
                double rightGazePointOnDisplay_X = Double.valueOf(inputTobii[2]);
                double rightGazePointOnDisplay_Y = Double.valueOf(inputTobii[3]);
                double leftGazePointOnDisplay_X = Double.valueOf(inputTobii[4]);
                double leftGazePointOnDisplay_Y = Double.valueOf(inputTobii[5]);

                if (contadorLineasET < 1) {
                	datosTobii.add("rightPupilDiameter,leftPupilDiameter,rightGazePointOnDisplay_X,rightGazePointOnDisplay_Y,leftGazePointOnDisplay_X,leftGazePointOnDisplay_Y,timestamp,pregunta,respuesta");
                	contadorLineasET++;
                } else {
                	String dato = inputTobii[0] + ","
                            + inputTobii[1] + ","
                            + inputTobii[2] + ","
                            + inputTobii[3] + ","
                            + inputTobii[4] + ","
                            + inputTobii[5] + ","
                            + String.valueOf(currentTime) + ",";
                    if(numPregunta.get() > 0)
                        dato += String.valueOf(numPregunta.get());
                    // Agrega una respuesta si la hay, si no agrega una cadena vacía
                    dato += ",";
                    dato += respuesta!=null?(respuesta?"1":"0"):"";
                    // La respuesta vuelve a ser null
                    respuesta = null;
                	datosTobii.add(dato);
                }

                Platform.runLater(
                    () -> {
                        if (seriesTOBIIDerecho.getData().size() == 0) {
                            if (rightPupilDiameter == 0.0) {
                                seriesTOBIIDerecho.getData().add(new XYChart.Data<Number, Number>(currentTime,2.5));
                            } else {
                                seriesTOBIIDerecho.getData().add(new XYChart.Data<Number, Number>(currentTime,rightPupilDiameter));
                            }
                            if (leftPupilDiameter == 0.0) {
                                seriesTOBIIIzquierdo.getData().add(new XYChart.Data<Number, Number>(currentTime,2.5));
                            } else {
                                seriesTOBIIIzquierdo.getData().add(new XYChart.Data<Number, Number>(currentTime,leftPupilDiameter));
                            }
                        } else {
                            if (rightPupilDiameter == 0.0) {
                                XYChart.Data<Number, Number> previo = seriesTOBIIDerecho.getData().get(seriesTOBIIDerecho.getData().size() - 1);
                                double previoY = previo.getYValue().doubleValue();
                                seriesTOBIIDerecho.getData().add(new XYChart.Data<Number, Number>(currentTime,previoY));
                            } else {
                                seriesTOBIIDerecho.getData().add(new XYChart.Data<Number, Number>(currentTime,rightPupilDiameter));
                            }
                            if (leftPupilDiameter == 0.0) {
                                XYChart.Data<Number, Number> previo = seriesTOBIIIzquierdo.getData().get(seriesTOBIIIzquierdo.getData().size() - 1);
                                double previoY = previo.getYValue().doubleValue();
                                seriesTOBIIIzquierdo.getData().add(new XYChart.Data<Number, Number>(currentTime,previoY));
                            } else {
                                seriesTOBIIIzquierdo.getData().add(new XYChart.Data<Number, Number>(currentTime,leftPupilDiameter));
                            }
                        }

                        if(seriesTOBIIRightGaze.getData().size()==1)
                            seriesTOBIIRightGaze.getData().remove(0,1);
                        seriesTOBIIRightGaze.getData().add(new XYChart.Data<Number, Number>(rightGazePointOnDisplay_X, rightGazePointOnDisplay_Y));
                        if(seriesTOBIILeftGaze.getData().size()==1)
                            seriesTOBIILeftGaze.getData().remove(0,1);
                        seriesTOBIILeftGaze.getData().add(new XYChart.Data<Number, Number>(leftGazePointOnDisplay_X, leftGazePointOnDisplay_Y));
                        if(graficaTOBIIGaze.getData().size()<1) {
                            graficaTOBIIGaze.getData().add(seriesTOBIIRightGaze);
                            graficaTOBIIGaze.getData().add(seriesTOBIILeftGaze);
                        }

                        XYChart.Data<Number, Number> minder = seriesTOBIIDerecho.getData().get(0);
                        xAxisTOBII.setLowerBound(minder.getXValue().doubleValue());
                        xAxisTOBII.setUpperBound(minder.getXValue().doubleValue() + 1500);

                        if(seriesTOBIIIzquierdo.getData().size()>240) {
                            seriesTOBIIIzquierdo.getData().remove(0,1);
                            seriesTOBIIDerecho.getData().remove(0,1);
                        }

                        if(graficaTOBII.getData().size()<1) {
                            graficaTOBII.getData().add(seriesTOBIIIzquierdo);
                            graficaTOBII.getData().add(seriesTOBIIDerecho);
                        }

                    }
                );
                /*
                try {
                    Thread.sleep(8); //Sleep de 2 segundos
                } catch (InterruptedException ex) {
                    System.out.println("Error en thread de eye tracker: " + ex);
                }*/

            }
        } catch (IOException ex) {
            logger.info("Error lectura de output Tobii: " + ex);
        }
    }

    /**
     * Inicia la interfaz de las preguntas y las gráficas
     */
    @FXML
    private void iniciarPreguntas() throws IOException {
        if(!isCalibrado){
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("!EyeTracker no Calibrado!");
            String mensaje = "Eye Tracker no se ha calibrado, ¿Deseas continuar sin la calibración?";
            alerta.setContentText(mensaje);

            Optional<ButtonType> resultado = alerta.showAndWait();
            if ((resultado.isPresent() && (resultado.get() == ButtonType.CANCEL))){
                calibrarEyeTracker();
            }
        }
        this.date_ini = (new Date()).getTime();
        // Inicializar variables
        numLecturasPPG = 0;
        sumaLecturasPPG = 0.0;
        sumaCuadrados = 0.0;
        media = 512.0;
        lecturasPPG = new ArrayList<>();
        long tiempo = (new Date()).getTime();
        this.date_ini = (new Date()).getTime();

        // Elementos Gráficos
        tabPane.getSelectionModel().selectNext();
        iniciarButton.setDisable(true);
        cargaCSVButton.setDisable(true);
        calibrarButton.setDisable(true);
        verdaderoFalso.setDisable(true);

        // Gráfica TOBII
        seriesTOBIIDerecho = new XYChart.Series<Number, Number>();
        seriesTOBIIIzquierdo = new XYChart.Series<Number, Number>();
        seriesTOBIIDerecho.setName("Diámetro de Pupila Derecha");
        seriesTOBIIIzquierdo.setName("Diámetro de Pupila Izquierda");

        xAxisTOBII.setForceZeroInRange(false);
        xAxisTOBII.setTickLabelsVisible(true);
        xAxisTOBII.setTickMarkVisible(true);
        xAxisTOBII.setAutoRanging(false);

        //yAxisTOBII.setAutoRanging(true);
        yAxisTOBII.setLowerBound(1.0);
        yAxisTOBII.setUpperBound(4.0);
        yAxisTOBII.setForceZeroInRange(false);

        //graficaTOBII.legendVisibleProperty().setValue(false);
        graficaTOBII.setCreateSymbols(false);
        graficaTOBII.setAnimated(false);

        // Grafica TOBII Gaze
        seriesTOBIIRightGaze = new XYChart.Series<Number, Number>();
        seriesTOBIIRightGaze.setName("Posición de Ojo Derecho");
        seriesTOBIILeftGaze = new XYChart.Series<Number, Number>();
        seriesTOBIILeftGaze.setName("Posición de Ojo Izquierdo");

        xAxisTOBIIGaze.setForceZeroInRange(false);
        xAxisTOBIIGaze.setTickLabelsVisible(true);
        xAxisTOBIIGaze.setTickLabelsVisible(true);
        xAxisTOBIIGaze.setAutoRanging(false);
        xAxisTOBIIGaze.setLowerBound(-0.1);
        xAxisTOBIIGaze.setUpperBound(1.1);

        yAxisTOBIIGaze.setLowerBound(1.1);
        yAxisTOBIIGaze.setUpperBound(-0.1);
        yAxisTOBIIGaze.setForceZeroInRange(true);
        yAxisTOBIIGaze.setAutoRanging(false);

        graficaTOBIIGaze.setCreateSymbols(true);
        graficaTOBIIGaze.setAnimated(false);


        // Crear archivos CSV
        try {
            currentTime = System.currentTimeMillis();
            csvET = new FileWriter("et_" + currentTime + ".csv");
            /*csvGSR = new FileWriter("gsr_" + currentTime + ".csv");
            csvPPG = new FileWriter("ppg_" + currentTime + ".csv");*/
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Carga la segunda ventana
        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        // Si tenemos la segunda pantalla
        Screen pantalla2 = Screen.getScreens().size()>1?Screen.getScreens().get(1):Screen.getPrimary();
        Parent root = loader.load();
        preguntas = loader.getController();
        // Indicarle quien es su 'Padre'
        preguntas.setParentController(this);
        // Pasarle datos de configuración
        if(verdaderoFalso.isSelected()){
            preguntas.getBotonNo().setText("Falso");
            preguntas.getBotonSi().setText("Verdadero");
        }
        preguntas.setIntervaloCorrectas(Integer.parseInt(intervaloCorrectasField.getText()));

        stagePreguntas = new Stage();
        stagePreguntas.setX(pantalla2.getVisualBounds().getMinX());
        stagePreguntas.setY(pantalla2.getVisualBounds().getMinY());
        stagePreguntas.setWidth(pantalla2.getVisualBounds().getWidth());
        stagePreguntas.setHeight(pantalla2.getVisualBounds().getHeight());
        stagePreguntas.setScene(new Scene(root));
        stagePreguntas.setTitle("Preguntas");
        stagePreguntas.show();
        stagePreguntas.setMaximized(true);

        // Manejar el cierre de la ventana
        // Detener el contador
        stagePreguntas.setOnCloseRequest( event -> {
            if(preguntas.timer != null)
                preguntas.timer.stop();
            iniciarButton.setDisable(false);
            cargaCSVButton.setDisable(false);
            verdaderoFalso.setDisable(false);
            tabPane.getSelectionModel().selectFirst();
            try {
                this.detenerLecturaTobii();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread t1 = new Thread() {
            public void run() {
                obtenerDatosTobii();
            }
        };
        t1.start();
        // Sensores GSR y PPG
        /*try {
            //arduinoPPG.arduinoRX(puertoSerialPPG, baudingPPG, ListenerPPG);
            //arduinoGSR.arduinoRX(puertoSerialGSR, baudingGSR, ListenerGSR);
        } catch (ArduinoException ex) {
            Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SerialPortException ex) {
            Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    @FXML
    private void salir() throws IOException {
        if(stagePreguntas != null)
            new WindowEvent(stagePreguntas, WindowEvent.WINDOW_CLOSE_REQUEST);
        detenerLecturaTobii();
        System.exit(0);
    }

    /* GETTERS & SETTERS */

    public List<Pregunta> getPreguntasList() {
        return preguntasList;
    }

    public Label getPreguntaLabel() {
        return preguntaLabel;
    }

    public boolean isBandera() {
        return bandera;
    }

    public void setBandera(boolean bandera, Boolean respuesta) {
        this.bandera = bandera;
        this.respuesta = respuesta;
    }

    public void setNumPregunta(Integer numPregunta) {
        this.numPregunta.set(numPregunta);
    }
}
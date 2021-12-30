package mx.com.cuatronetworks.sensoresbpdp;

import com.opencsv.bean.CsvToBeanBuilder;
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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
import org.jfree.chart.axis.NumberAxis;
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    private AnchorPane anchorPane;

    @FXML
    private TabPane tabPane;

    @FXML
    private CheckBox verdaderoFalso;

    @FXML
    private TextField tiempoLecturaField;

    @FXML
    private TextField intervaloCorrectasField;

    // Sensores
    private final String puertoSerialGSR = "/dev/ttyUSB0";
    private final String puertoSerialPPG = "/dev/ttyACM0";
    private final int baudingPPG = 115200;
    private final int baudingGSR = 115200;

    // Arduino
    PanamaHitek_Arduino ino = new PanamaHitek_Arduino();
    PanamaHitek_Arduino ino2 = new PanamaHitek_Arduino();

    // VARIABLES sensor PPG
    final XYSeries SeriePPG = new XYSeries("PPG");
    final XYSeriesCollection ColeccionPPG = new XYSeriesCollection();
        // Media y varianza (+,-)
    final XYSeries mediaPPG = new XYSeries("Media PPG");
    final XYSeries varianzaPPG_p = new XYSeries("Varianza PPG (+)");
    final XYSeries varianzaPPG_n = new XYSeries("Varianza PPG (-)");

    // Create subplot 1
    final XYDataset data1 = null;
    final XYItemRenderer renderer1 = new StandardXYItemRenderer();
    final NumberAxis rangeAxis1 = new NumberAxis("Sensor PPG");
    final XYPlot subplot1 = new XYPlot(ColeccionPPG, null, rangeAxis1, renderer1);

    // VARIABLES sensor GSR
    final XYSeries SerieGSR = new XYSeries("GSR");
    final XYSeriesCollection ColeccionGSR = new XYSeriesCollection();
        // Media y varianza (+,-)
    final XYSeries mediaGSR = new XYSeries("Media GSR");
    final XYSeries varianzaGSR_p = new XYSeries("Varianza GSR (+)");
    final XYSeries varianzaGSR_n = new XYSeries("Varianza GSR (-)");

    // create subplot 2
    final XYItemRenderer renderer2 = new StandardXYItemRenderer();
    final NumberAxis rangeAxis2 = new NumberAxis("Sensor GSR");
    final XYPlot subplot2 = new XYPlot(ColeccionGSR, null, rangeAxis2, renderer2);

    // VARIABLES Tobii
    final XYSeries SeriesTobbiEL = new XYSeries("Tobii EL");
    final XYSeries SeriesTobbiER = new XYSeries("Tobii ER");
    final XYSeriesCollection ColeccionEyesTobbi = new XYSeriesCollection();
    // Create subplot 2
    final XYItemRenderer renderer3 = new StandardXYItemRenderer();
    final NumberAxis rangeAxis3 = new NumberAxis("Tobii ");
    final XYPlot subplot3 = new XYPlot(ColeccionEyesTobbi, null, rangeAxis3, renderer3);

    // Gráficas
    JFreeChart Grafica;
    final DateAxis timeAxis = new DateAxis("Time");
    final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(timeAxis);
    double rangoInferiorPPG = 490.0;
    double rangoSuperiorPPG = 550.0;
    double rangoInferiorGSR = 10.0;
    double rangoSuperiorGSR = 80.0;
    double rangoInferiorET = 2.0;
    double rangoSuperiorET = 5.0;

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

    // Controlador Papá
    HelloApplication mainApp;

    // Seleccionar archivo
    FileChooser fileChooser;

    // Archivo CSV para Lectura
    File csvFile;

    // Archivos CSV para Escritura
    private FileWriter csvPPG;
    private FileWriter csvGSR;
    private FileWriter csvET;
    private FileWriter contadorTiempos;
    private int contadorLineasPPG = 0;
    private int contadorLineasGSR = 0;
    private int contadorLineasET = 0;
    private int contadorLineasTiempos = 0;

    // Varios
    int i = 0, j = 0, k = 0, l = 0;
    int numLecturasPPG = 0;

    /* SERIALPORT LISTENERS */
    /**
     * Serial Listener para GSR
     */
    SerialPortEventListener ListenerGSR = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                if (ino2.isMessageAvailable() == true) {
                    Double data;
                    try {
                        data = Double.parseDouble(ino2.printMessage());
                        int value = (int) Math.round(data);
                        /*escribirGSR(String.valueOf(value) + ","
                                + String.valueOf((new Date()).getTime()) + ","
                                + contador_preguntas + "," + respuesta);*/
                        SerieGSR.add(i, value);
                    } catch (ArduinoException | NumberFormatException | SerialPortException ex) {
                        System.out.println("Error data: " + ex);
                    }
                }
            } catch (ArduinoException | SerialPortException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    /**
     * Serial Listener para PPG
     */
    SerialPortEventListener Listener = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            k += 1;
            try {
                if (ino.isMessageAvailable() == true) {
                    i++;
                    Double data = 0.0;
                    long current_time = (new Date()).getTime();
                    long calculo = (current_time - date_ini) / 1000;
                    if (calculo < tiempo_lectura) {
                        try {
                            data = Double.parseDouble(ino.printMessage());
                            int value = (int) Math.round(data);
                            /*escribirPPG(String.valueOf(value) + ","
                                    + String.valueOf((new Date()).getTime()) + ","
                                    + contador_preguntas + "," + respuesta);*/
                            SeriePPG.add(i, value);
                            if(data > 0 && data < 1000) {
                                numLecturasPPG++;
                                sumaLecturasPPG += data;
                                lecturasPPG.add(data);
                                sumaCuadrados += Math.pow((data - media),2);
                            }
                            if(calculo >= segundosLectura){ // Se espera a que pasen los n segundos (o los definidos)
                                if(calculo%segundosLectura == 0){ // Si es multiplo de n
                                    if(!banderaCalculos){
                                        // TODO: Cálculos aki
                                        // Debe entrar aqui una vez pasados 30 segundos");
                                        System.out.println("Numero de lecturas: " + numLecturasPPG);
                                        media = sumaLecturasPPG/numLecturasPPG;
                                        varianza = sumaCuadrados/numLecturasPPG;
                                        System.out.println("La media es: " + media + " y la Varianza es: " + varianza);
                                        sumaCuadrados = 0.0;
                                        sumaLecturasPPG = 0.0;
                                        numLecturasPPG = 0;
                                        lecturasPPG = new ArrayList<>();
                                        banderaCalculos = true;
                                    }
                                }else{
                                    banderaCalculos = false;
                                }
                                mediaPPG.add(i, media);
                                varianzaPPG_p.add(i, media + varianza);
                                varianzaPPG_n.add(i, media - varianza);
                            }

                            if(bandera) {
                                XYSeries temp = new XYSeries("Punto " + i);
                                temp.add(i, 1000);
                                temp.add(i + 5, 50);
                                markers.add(temp);
                                ColeccionPPG.addSeries(temp);
                                bandera = false;
                            }
                        } catch (ArduinoException | NumberFormatException | SerialPortException ex) {
                            System.out.println("Error data: " + ex);
                        }
                    } else {
                        /*try {
                            csvPPG.close();
                            csvGSR.close();
                            csvET.close();
                            contadorTiempos.close();
                        } catch (IOException ex) {
                            System.out.println("Error al cerrar el archivo: " + ex);
                        }*/
                        System.exit(0);
                    }
                }
            } catch (ArduinoException | SerialPortException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    @FXML
    private void initialize(){
        // Elementos de la interfaz gráfica
        nombreCSVLabel.setText("Ninún Archivo CSV seleccionado");
        iniciarButton.setDisable(true);
        tiempoLecturaField.setText(tiempo_lectura.toString());
        intervaloCorrectasField.setText("3");
        tabPane.getTabs().get(1).setDisable(true);
        tabPane.getTabs().get(2).setDisable(true);


        /*try {
            csvGSR = new FileWriter("gsr_" + tiempo + ".csv");
            csvPPG = new FileWriter("ppg_" + tiempo + ".csv");
            csvET = new FileWriter("et_" + tiempo + ".csv");
            contadorTiempos = new FileWriter("tiemposrespuesta_" + tiempo + ".csv");
        } catch (IOException ex) {
            System.out.println("Ocurrió un error al abrir archivos: " + ex);
        }*/
        rangeAxis1.setRange(rangoInferiorPPG, rangoSuperiorPPG);
        rangeAxis2.setRange(rangoInferiorGSR, rangoSuperiorGSR);
        rangeAxis3.setRange(rangoInferiorET, rangoSuperiorET);
        renderer3.setSeriesPaint(1, Color.BLACK);
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
        media = (rangoSuperiorPPG + rangoSuperiorPPG)/2;
        lecturasPPG = new ArrayList<>();
        long tiempo = (new Date()).getTime();
        ultimoTiempo = (new Date()).getTime();
        this.date_ini = (new Date()).getTime();

        // Elementos Gráficos
        tabPane.getSelectionModel().selectNext();
        iniciarButton.setDisable(true);
        cargaCSVButton.setDisable(true);
        verdaderoFalso.setDisable(true);

        // Agregar las Series a las Gráficas
        ColeccionPPG.addSeries(SeriePPG);
        ColeccionPPG.addSeries(mediaPPG);
        ColeccionPPG.addSeries(varianzaPPG_n);
        ColeccionPPG.addSeries(varianzaPPG_p);

        ColeccionGSR.addSeries(SerieGSR);
        ColeccionEyesTobbi.addSeries(SeriesTobbiEL);
        ColeccionEyesTobbi.addSeries(SeriesTobbiER);

        plot.setGap(10.0);

        plot.add(subplot1, 1);
        plot.add(subplot2, 1);
        plot.add(subplot3, 1);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setBackgroundPaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        renderer3.setSeriesPaint(1, Color.BLACK);

        Grafica = new JFreeChart("PPG / GSR", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

        ChartViewer viewer = new ChartViewer(Grafica);
        //Stage stage2 = new Stage();
        //stage2.setMaximized(true);
        //stage2.setScene(new Scene(viewer));
        //stage2.setTitle("Gráficas");
        // stage2.show();

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
        stage.setMaximized(true); // TODO: descomentar

        // Manejar el cierre de la ventana
        // Detener el contador
        stage.setOnCloseRequest( event -> {
            if(preguntas.timer != null)
                preguntas.timer.stop();
            iniciarButton.setDisable(false);
            cargaCSVButton.setDisable(false);
            verdaderoFalso.setDisable(false);
            tabPane.getSelectionModel().selectFirst();
        });

        /* HILOS */

        /*Thread t1 = new Thread() {
            public void run() {
                try {
                    ino.arduinoRX(puertoSerialPPG, baudingPPG, Listener);
                    //ino2.arduinoRX(puertoSerialPPG, baudingGSR, ListenerGSR);
                } catch (ArduinoException ex) {
                    Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SerialPortException ex) {
                    Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        t1.start();

        Thread t5 = new Thread(){
            @Override
            public void run() {
                System.out.println("Entre");
                while (t1.isAlive()){

                }
            }
        };
        t5.run();*/
        /*
        Thread t4 = new Thread() {
            public void run() {
                while (true) {
                    if(bandera){
                        System.out.println("Entreeee");
                        XYSeries temp = new XYSeries("Punto " + i);
                        temp.add(i, 1000);
                        temp.add(i+1, 0);
                        ColeccionPPG.addSeries(temp);
                        ColeccionGSR.addSeries(temp);
                        ColeccionEyesTobbi.addSeries(temp);
                        long tiempoActual = (new Date()).getTime();
                        System.out.println("CONTADOR: " + contador_preguntas);

                        if (contador_preguntas == 1) {
                            System.out.println("ENTRA INIT");
                            long tiempoTranscurrido = tiempoActual - date_ini;
                            String dato = contador_preguntas + "," + tiempoTranscurrido/1000;
                            ultimoTiempo = tiempoActual;
                            contador_preguntas += 1;
                            escribirTiempos(dato);
                        } else {
                            long tiempoTranscurrido = tiempoActual - ultimoTiempo;
                            String dato = contador_preguntas + "," + tiempoTranscurrido/1000;
                            ultimoTiempo = tiempoActual;
                            contador_preguntas += 1;
                            escribirTiempos(dato);
                        }
                    }

                    Scanner keyboard = new Scanner(System.in);
                    System.out.println("Responde: 1 Verdadero, 2 Falso");
                    int lectura = keyboard.nextInt(); //1 sí, 0 no
                    respuesta = lectura;
                }
            }
        };*/
        //t4.start();
        /*
        try{
            // TODO: Revisar los puertos correctos
            ino.arduinoRX(puertoSerialPPG, baudingPPG, Listener);
            // TODO: Descomentar
            // ino2.arduinoRX(puertoSerialPPG, baudingGSR, ListenerGSR);
        }catch(ArduinoException ex){
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
        }catch(SerialPortException ex){
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
        }
        */

    }

    /** ESCRIBIR ARCHIVOS DE SALIDA */

    public void escribirPPG(String dato) {
        try {
            if (contadorLineasPPG == 0) {
                csvPPG.write("valor,ts,pregunta,respuesta\n");
                contadorLineasPPG += 1;
            } else {
                csvPPG.write(dato + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Error al escribir archivo PPG: " + ex);
        }
    }

    public void escribirGSR(String dato) {
        try {
            if (contadorLineasGSR == 0) {
                csvGSR.write("valor,ts,pregunta,respuesta\n");
                contadorLineasGSR += 1;
            } else {
                csvGSR.write(dato + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Error al escribir archivo GSR: " + ex);
        }
    }

    public void escribirET(String dato) {
        try {
            if (contadorLineasET == 0) {
                csvET.write("valor_promedio,ts,pregunta,respuesta\n");
                contadorLineasET += 1;
            } else {
                csvET.write(dato + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Error al escribir archivo ET: " + ex);
        }
    }

    public void escribirTiempos(String dato) {
        System.out.println("DATO A ESCRIBIR: " + dato);
        try {
            if (contadorLineasTiempos == 0) {
                contadorTiempos.write("pregunta,tiempo\n");
                contadorTiempos.write(dato + "\n");
                contadorLineasTiempos += 1;
            } else {
                contadorTiempos.write(dato + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Error al escribir archivo TIEMPOS: " + ex);
        }
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
}

package mx.com.cuatronetworks.sensoresbpdp;

import com.opencsv.bean.CsvToBeanBuilder;
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
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

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AdminController {

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

    // Sensores
    private final String puertoSerialGSR = "/dev/ttyUSB0";
    private final String puertoSerialPPG = "/dev/ttyUSB0";
    private final int baudingPPG = 115200;
    private final int baudingGSR = 115200;

    //Arduino

    PanamaHitek_Arduino ino = new PanamaHitek_Arduino();
    PanamaHitek_Arduino ino2 = new PanamaHitek_Arduino();

    //VARIABLES sensor PPG
    final XYSeries Serie = new XYSeries("PPG");
    final XYSeriesCollection Coleccion = new XYSeriesCollection();

    // create subplot 1...
    final XYDataset data1 = null;
    final XYItemRenderer renderer1 = new StandardXYItemRenderer();
    final NumberAxis rangeAxis1 = new NumberAxis("Sensor PPG");
    final XYPlot subplot1 = new XYPlot(Coleccion, null, rangeAxis1, renderer1);

    //VARIABLES sensor GSR;
    final XYSeries SerieGSR = new XYSeries("GSR");
    final XYSeriesCollection Coleccion2 = new XYSeriesCollection();

    // create subplot 2...
    final XYItemRenderer renderer2 = new StandardXYItemRenderer();
    final NumberAxis rangeAxis2 = new NumberAxis("Sensor GSR");
    final XYPlot subplot2 = new XYPlot(Coleccion2, null, rangeAxis2, renderer2);

    //VARIABLES Tobii;
    final XYSeries SeriesTobbiEL = new XYSeries("Tobii EL");
    final XYSeries SeriesTobbiER = new XYSeries("Tobii ER");
    final XYSeriesCollection ColeccionEyesTobbi = new XYSeriesCollection();
    // create subplot 2...
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

    private int numPregunta;
    private List<Pregunta> preguntasList;
    HelloApplication mainApp;
    FileChooser fileChooser;
    File csvFile;
    private int totalPreguntas = 0;

    // Varios
    int i = 0, j = 0, k = 0;

    @FXML
    private void initialize(){
        nombreCSVLabel.setText("Ninún Archivo CSV seleccionado");
        iniciarButton.setDisable(true);
        tabPane.getTabs().get(1).setDisable(true);
        tabPane.getTabs().get(2).setDisable(true);
    }

    public void setMainApp(HelloApplication mainApp){
        this.mainApp = mainApp;
    }

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

    @FXML
    private void iniciarPreguntas() throws IOException {
        tabPane.getSelectionModel().selectNext();
        iniciarButton.setDisable(true);
        cargaCSVButton.setDisable(true);

        Coleccion.addSeries(Serie);
        Coleccion2.addSeries(SerieGSR);
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

        Grafica = new JFreeChart("PPG / GSR", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

        ChartViewer viewer = new ChartViewer(Grafica);
        Stage stage2 = new Stage();
        stage2.setScene(new Scene(viewer));
        stage2.setTitle("Gráficas");
        stage2.setMaximized(true);
        stage2.show();

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
        verdaderoFalso.setDisable(true);

        Stage stage = new Stage();
        stage.setX(pantalla2.getVisualBounds().getMinX());
        stage.setY(pantalla2.getVisualBounds().getMinY());
        stage.setWidth(pantalla2.getVisualBounds().getWidth());
        stage.setHeight(pantalla2.getVisualBounds().getHeight());
        stage.setScene(new Scene(root));
        stage.setTitle("Preguntas");
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
        });
        stage.show();

        try{
            // TODO: Revisar los puertos correctos
            ino.arduinoRX(puertoSerialGSR, baudingPPG, Listener);
            // TODO: Descomentar
            //ino2.arduinoRX(puertoSerialPPG, baudingPPG, ListenerGSR);
        }catch(ArduinoException ex){
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
        }catch(SerialPortException ex){
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Serial port gsr
    SerialPortEventListener ListenerGSR = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                if (ino2.isMessageAvailable() == true) {
                    //j++;
                    Double data;
                    try {
                        data = Double.parseDouble(ino2.printMessage());
                        int value = (int) Math.round(data);
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

    /** Listeners de sensores */
    //Serial port pulso
    SerialPortEventListener Listener = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                if (ino.isMessageAvailable() == true) {
                    i++;
                    Double data;
                    data = Double.parseDouble(ino.printMessage());
                    int value = (int) Math.round(data);
                    Serie.add(i, value);

                    if(bandera) {
                        XYSeries temp = new XYSeries("Punto " + i);
                        temp.add(i, 1000);
                        temp.add(i + 5, 50);
                        markers.add(temp);
                        //k++;
                        Coleccion.addSeries(temp);
                        bandera = false;
                    }
                }
            } catch (ArduinoException | SerialPortException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

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

package mx.com.cuatronetworks.sensoresbpdp;

import com.opencsv.bean.CsvToBeanBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mx.com.cuatronetworks.sensoresbpdp.model.Pregunta;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.CombinedDomainXYPlot;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


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

    // Gráficas
    JFreeChart Grafica;
    final DateAxis timeAxis = new DateAxis("Time");
    final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(timeAxis);

    private int numPregunta;
    private List<Pregunta> preguntasList;
    HelloApplication mainApp;
    FileChooser fileChooser;
    File csvFile;
    private int totalPreguntas = 0;

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


        Grafica = new JFreeChart("PPG / GSR", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        ChartViewer viewer = new ChartViewer(Grafica);
        /*group = new Group();
        group.getChildren().add(viewer);*/

        Stage stage2 = new Stage();
        stage2.setScene(new Scene(viewer));
        stage2.setTitle("hehe");
        stage2.show();
        // Si tenemos la segunda pantalla

        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        Screen pantalla2 = Screen.getScreens().size()>1?Screen.getScreens().get(1):Screen.getPrimary();
        Parent root = loader.load();
        PrimaryController preguntas = loader.getController();
        preguntas.setParentController(this);
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
            tabPane.getSelectionModel().selectFirst();
        } );
        stage.show();
    }

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
}

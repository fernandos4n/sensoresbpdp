package mx.com.cuatronetworks.sensoresbpdp;

import com.opencsv.bean.CsvToBeanBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mx.com.cuatronetworks.sensoresbpdp.model.Pregunta;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class AdminController {

    @FXML
    private Label nombreCSVLabel;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        Parent root = loader.load();
        PrimaryController preguntas = loader.getController();
        preguntas.setParentController(this);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Preguntas");
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
}

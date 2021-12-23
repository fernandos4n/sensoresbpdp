package mx.com.cuatronetworks.sensoresbpdp;

import com.opencsv.bean.CsvToBeanBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import mx.com.cuatronetworks.sensoresbpdp.model.Pregunta;

import java.io.File;
import java.io.FileReader;
import java.util.List;


public class AdminController {

    @FXML
    private Label nombreCSVLabel;

    @FXML
    private Button cargaCSVButton;

    @FXML
    private Button iniciarButton;

    @FXML
    private AnchorPane anchorPane;

    private List<Pregunta> preguntasList;

    HelloApplication mainApp;

    FileChooser fileChooser;

    File csvFile;

    @FXML
    private void initialize(){
        nombreCSVLabel.setText("Ninún Archivo CSV seleccionado");
        iniciarButton.setDisable(true);
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
            }catch (Exception e){
                e.printStackTrace();
                nombreCSVLabel.setText("Archivo Inválido!");
            }
        }
    }

    public List<Pregunta> getPreguntasList() {
        return preguntasList;
    }

    public void setPreguntasList(List<Pregunta> preguntasList) {
        this.preguntasList = preguntasList;
    }
}

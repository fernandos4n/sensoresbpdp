package mx.com.cuatronetworks.sensoresbpdp;

import java.io.IOException;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        HelloApplication.setRoot("primary");
    }
}
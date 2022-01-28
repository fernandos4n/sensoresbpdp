package mx.com.cuatronetworks.sensoresbpdp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

public class HelloApplication extends Application {
    static Logger logger = Logger.getLogger(HelloApplication.class);
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        String log4jConfigFile = getClass().getResource("log4j.properties").getPath();
        PropertyConfigurator.configure(log4jConfigFile);
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("adminView.fxml"));
        scene = new Scene(fxmlLoader.load(), 1200, 720);
        stage.setTitle("Consola de Administración");
        stage.setScene(scene);
        //stage.setAlwaysOnTop(true);
        // Si tenemos la segunda pantalla
        Screen pantalla2 = Screen.getScreens().size()>1?Screen.getScreens().get(0):Screen.getPrimary();
        stage.setX(pantalla2.getVisualBounds().getMinX());
        stage.setY(pantalla2.getVisualBounds().getMinY());
        stage.setWidth(pantalla2.getVisualBounds().getWidth());
        stage.setHeight(pantalla2.getVisualBounds().getHeight());
        stage.setMaximized(true);
        stage.show();
        AdminController adminController = fxmlLoader.getController();
        //AdminControllerBAK adminController = fxmlLoader.getController();
        stage.setOnCloseRequest( event -> {
            try {
                adminController.detenerLecturaTobii();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    public static Scene getScene() {
        return scene;
    }

    public static void setScene(Scene scene) {
        HelloApplication.scene = scene;
    }
}
package mx.com.cuatronetworks.sensoresbpdp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class LineChartSample extends Application {
    final int WINDOW_SIZE = 1000;
    private ScheduledExecutorService scheduledExecutorService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JavaFX Realtime Chart Demo");

        //defining the axes
        final NumberAxis xAxis = new NumberAxis("Time: s", 0, 4, 1); // we are gonna plot against time
        xAxis.setAutoRanging(false);
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time/s");
        xAxis.setAnimated(false); // axis animations are removed
        yAxis.setLabel("Value");
        yAxis.setAnimated(false); // axis animations are removed

        //creating the line chart with two axis created above
        LineChart lineChart = new LineChart(xAxis, yAxis);
        lineChart.setTitle("Realtime JavaFX Charts");
        lineChart.setAnimated(false); // disable animations

        //defining a series to display data
       // XYChart.Series<String, Number> series = new XYChart.Series<>();
        final ObservableList<Data> seriesData = FXCollections.observableArrayList();
        Series series = new Series("Rotation", seriesData);

        series.setName("Data Series");

        // add series to chart
        

        // ScrollPane root = new ScrollPane(lineChart);
        //root.setMinSize(1000,600);
        //lineChart.setMinSize(root.getMinWidth(),root.getMinHeight()-20);
      
        //Scene scene  = new Scene(root,800,600);
        //lineChart.getData().add(series);
         //setup scene
       

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        // show the stage
        primaryStage.show();

        // this is used to display time in HH:mm:ss format
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        // setup a scheduled executor to periodically put data into the chart
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        // put dummy data onto graph per second
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            // get a random integer between 0-10
            Integer random = ThreadLocalRandom.current().nextInt(10);

            // Update the chart
            Platform.runLater(() -> {
                // get current time
                Date now = new Date();
                // put random number with current time
               // series.add(new XYChart.Data<>(simpleDateFormat.format(now), random));
                seriesData.add(new Data(simpleDateFormat.format(now), random));
                //if (series.getData().size() > WINDOW_SIZE)
                  //  series.getData().remove(0);
            });
        }, 0, 1, TimeUnit.SECONDS);
        
        
        ScrollPane pane = new ScrollPane();
        pane.setContent(lineChart);
        pane.setPrefSize(600, 300);

        pane.setContent(lineChart);
        pane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observableValue, Bounds oldBounds, Bounds newBounds) {
                lineChart.setMinSize(Math.max(lineChart.getPrefWidth(), newBounds.getWidth()), Math.max(lineChart.getPrefHeight(), newBounds.getHeight()));
                pane.setPannable((lineChart.getPrefWidth() > newBounds.getWidth()) || (lineChart.getPrefHeight() > newBounds.getHeight()));
            }
        });
        lineChart.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent ev) {
                double zoomFactor = 1.05;
                double deltaY = ev.getDeltaY();

                if (deltaY < 0) {
                    zoomFactor = 2 - zoomFactor;
                }

                System.out.println("DeltaX = " + ev.getDeltaX());
                System.out.println("DeltaY = " + ev.getDeltaY());
                System.out.println("Zoomfactor = " + zoomFactor);
               // NumberAxis yAxis = new NumberAxis();
                NumberAxis xAxisLocal = ((NumberAxis) lineChart.getXAxis());

               
                xAxisLocal.setUpperBound(xAxisLocal.getUpperBound() * zoomFactor);
                xAxisLocal.setLowerBound(xAxisLocal.getLowerBound() * zoomFactor);
                xAxisLocal.setTickUnit(xAxisLocal.getTickUnit() * zoomFactor);

                ev.consume();
            }
        });
        
        lineChart.getData().add(series);
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        
    }
    
  

    @Override
    public void stop() throws Exception {
        super.stop();
        scheduledExecutorService.shutdownNow();
    }
}
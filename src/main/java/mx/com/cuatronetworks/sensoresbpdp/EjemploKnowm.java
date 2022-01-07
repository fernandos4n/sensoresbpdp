package mx.com.cuatronetworks.sensoresbpdp;
import java.io.IOException;
import java.util.Date;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;

import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class EjemploKnowm {
	
	public static void main(String[] args) {
		EjemploKnowm ejemplo = new EjemploKnowm();
	}
	
	private final String puertoSerialPPG = "/dev/ttyUSB0";
	private final int baudingPPG = 115200;
	PanamaHitek_Arduino ino = new PanamaHitek_Arduino();
	long time = System.currentTimeMillis();
	long date_ini;
	private int tiempo_lectura = 60;
	
	double[] data = {512};
    double[] data2 = {512};
    final XYChart chart = QuickChart.getChart("Simple XChart Real-time Demo", "Radians", "Sine", "PPG", data, data2);
    final SwingWrapper<XYChart> sw = new SwingWrapper<XYChart>(chart);
	
	public EjemploKnowm() {
		date_ini = (new Date()).getTime();
	    sw.displayChart();
		try {
			ino.arduinoRX(puertoSerialPPG, baudingPPG, Listener);
		} catch (ArduinoException ex) {
			System.out.println("Error: " + ex);
		} catch (SerialPortException ex) {
			System.out.println("Error: " + ex);
		}
	}
	
	int tamano = 10;
	double i = 0.0;
	int j = 0;
	double[] ejeX = new double[100];
    double[] ejeY = new double[100];
    
	SerialPortEventListener Listener = new SerialPortEventListener() {

        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                if (ino.isMessageAvailable() == true) {
                    time = System.currentTimeMillis();

                    Double data;
                    long current_time = (new Date()).getTime();
                    long calculo = (current_time - date_ini) / 1000;
                    if (calculo < tiempo_lectura) {
                        try {
                            data = Double.parseDouble(ino.printMessage());
                            //int value = (int) Math.round(data);
                            //System.out.println(value);
                            //while (true) {
                            	if (j < tamano) {
                            		ejeX[j] = i + 1.0;
                                	ejeY[j] = data;
                                	i++;
                                	j++;
                                } else {
                                	repintar(ejeX,ejeY);
                                	ejeX = new double[100];
                                	ejeY = new double[100];
                                	i++;
                                	j = 0;
                                }
                            //}
                            //SeriePPG.add(time, value);
                        } catch (ArduinoException | NumberFormatException | SerialPortException ex) {
                            System.out.println("Error data: " + ex);
                        }
                    } else {
                        System.exit(0);
                    }
                }
            } catch (ArduinoException | SerialPortException ex) {
                System.out.println("Error en lectura PPG: " + ex);
            }
        }
    };
    
    void repintar(double[] ejeX, double[] ejeY) {
    	/*for (double elemento : ejeY) {
    		System.out.println(elemento);
    	}
    	System.out.println("################################################");*/
	//public static void main(String[] args) throws Exception {
		 
	    //double phase = 0;
	    //double[][] initdata = getSineData(phase);
	 
	    //while (true) {
	 
	      //phase += 2 * Math.PI * 2 / 20.0;
	 
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 
	      //final double[][] data = getSineData(phase);
	 
	      chart.updateXYSeries("PPG", ejeX, ejeY, null);
	      sw.repaintChart();
	    //}
	 
	  }
	 
	  private static double[][] getSineData(double phase) {
	 
	    double[] xData = new double[100];
	    double[] yData = new double[100];
	    for (int i = 0; i < xData.length; i++) {
	      double radians = phase + (2 * Math.PI / xData.length * i);
	      xData[i] = radians;
	      yData[i] = Math.sin(radians);
	    }
	    return new double[][] { xData, yData };
	  }
}

 
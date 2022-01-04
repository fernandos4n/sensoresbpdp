/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mx.com.cuatronetworks.sensoresbpdp;

import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.UUID;

/**
 *
 * @author david
 */
public class GraficasMedicion extends JFrame {

	/**
	 * Creates new form GraficasMedicion
	 */
	long date_ini;
	private int tiempo_calibracion = 10;
	private int tiempo_lectura = 720;
	private boolean banderaPPG = false;
	private boolean banderaGSR = false;
	private boolean banderaTOBII = false;
	/**
	 * Creates new form Graficos
	 */

	// Variables arduinos
	private final String puertoSerialPPG = "/dev/ttyUSB0";
	private final String puertoSerialGSR = "/dev/ttyACM0";
	private final int baudingPPG = 115200;
	private final int baudingGSR = 115200;

	// VARIABLES sensor PPG
	final XYSeries SeriePPG = new XYSeries("PPG");
	XYSeriesCollection Coleccion = new XYSeriesCollection();
	// Media y varianza (+,-)
	final XYSeries mediaPPG = new XYSeries("Media PPG");
	final XYSeries varianzaPPG_p = new XYSeries("Varianza PPG (+)");
	final XYSeries varianzaPPG_n = new XYSeries("Varianza PPG (-)");

	final XYItemRenderer renderer1 = new StandardXYItemRenderer();
	final NumberAxis rangeAxis1 = new NumberAxis("Sensor PPG");
	final XYPlot subplot1 = new XYPlot(Coleccion, null, rangeAxis1, renderer1);

	// VARIABLES sensor GSR;
	final XYSeries SerieGSR = new XYSeries("GSR");
	final XYSeriesCollection Coleccion2 = new XYSeriesCollection();
	// create subplot 2...
	final XYItemRenderer renderer2 = new StandardXYItemRenderer();
	final NumberAxis rangeAxis2 = new NumberAxis("Sensor GSR");
	final XYPlot subplot2 = new XYPlot(Coleccion2, null, rangeAxis2, renderer2);

	// VARIABLES Tobii;
	// final XYSeries SeriesTobbiEL = new XYSeries("Tobbi EL");
	final XYSeries SeriesTobbiER = new XYSeries("Tobbi Promedio");
	final XYSeriesCollection ColeccionEyesTobbi = new XYSeriesCollection();

	// create subplot 2...
	final XYItemRenderer renderer3 = new StandardXYItemRenderer();
	final NumberAxis rangeAxis3 = new NumberAxis("Tobbi ");
	final XYPlot subplot3 = new XYPlot(ColeccionEyesTobbi, null, rangeAxis3, renderer3);
	final DateAxis Daxis = new DateAxis();

	// Markers
	final List<XYSeries> markers = new ArrayList<XYSeries>();

	JFreeChart Grafica;
	long time = System.currentTimeMillis();
	public static String s3_filename = "";
	private FileWriter contadorTiempos;
	private FileWriter s3;
	private int contadorLineasTiempos = 0;
	private int contadorLineasS3 = 0;

	int i = 0;
	int j = 0;
	int k = 0;

	int numLecturasPPG = 0;
	private boolean banderaCalculos = false;
	private Double sumaLecturasPPG;
	private List<Double> lecturasPPG;
	private Integer segundosLectura = 10;
	private Double media = 0.0;
	private Double sumaCuadrados = 0.0;
	private Double varianza = 0.0;
	private int segundos = 0;
	private int pulsos = 0;

	// conexion arduino
	PanamaHitek_Arduino ino = new PanamaHitek_Arduino();
	PanamaHitek_Arduino ino2 = new PanamaHitek_Arduino();
	final DateAxis timeAxis = new DateAxis("Time");
	final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(timeAxis);
	private int contador_preguntas = 1;
    public static int respuesta = 0;
    public long ultimoTiempo = 0;

	public void escribirTiempos(String dato) {
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

	public void escribirLecturaS3(String dato) {
		// timestamp,valor_ppg,valor_gsr,valor_et,evento
		try {
			if (contadorLineasS3 == 0) {
				s3.write("timestamp,valor_ppg,valor_gsr,valor_et,evento\n");
				s3.write(dato + "\n");
				contadorLineasS3 += 1;
			} else {
				s3.write(dato + "\n");
			}
		} catch (IOException ex) {
			System.out.println("Error al escribir archivo TIEMPOS: " + ex);
		}
	}

	private static void subirArchivoS3() throws IOException {
		File archivoCsv = new File(s3_filename);
		AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create("AKIAT6JLQQW37JVK4IFG",
				"c08zfntPoBnF5O0G3dvizMarT/nlGthf9dejfdB8");
		AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
		/*AWSCredentials credentials = new BasicAWSCredentials("AKIAT6JLQQW37JVK4IFG",
				"c08zfntPoBnF5O0G3dvizMarT/nlGthf9dejfdB8");*/
		S3AsyncClient s3AsyncClient = S3AsyncClient.builder().region(Region.US_EAST_1).credentialsProvider(awsCredentialsProvider).build();
		/*
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_2).build();

		// bucketName : el nombre del depósito donde queremos subir el objeto
		// clave : esta es la ruta completa al archivo
		// archivo : el archivo real que contiene los datos que se van a cargar
		String bucketName = "lecturasppg";
		String claveBucket = "lecturasppg/" + s3_filename;
		PutObjectRequest.builder().bucket(bucketName).key(claveBucket).
		s3AsyncClient.putObject(bucketName, claveBucket, archivoCsv);
		s3AsyncClient.put
		s3client.putObject(bucketName, claveBucket, archivoCsv);*/
	}

	/**
	 * Serial Listener para PPG
	 */
	SerialPortEventListener Listener = new SerialPortEventListener() {
		@Override
		public void serialEvent(SerialPortEvent spe) {
			k += 1;
			try {
				if (ino.isMessageAvailable() == true) {
					time = System.currentTimeMillis();
					// i++;
					Double data = 0.0;
					long current_time = (new Date()).getTime();
					long calculo = (current_time - date_ini) / 1000;
					if (calculo < tiempo_lectura) {
						try {
							data = Double.parseDouble(ino.printMessage());
							int value = (int) Math.round(data);
							/*
							 * escribirPPG(String.valueOf(value) + "," + String.valueOf((new
							 * Date()).getTime()) + "," + contador_preguntas + "," + respuesta);
							 */
							String dato = time + "," + value + "," + 0.0 + "," + 0.0 + "," + "'Sin datos'";
							escribirLecturaS3(dato);
							SeriePPG.add(time, value);
							if (data > 500 && data < 1000) {
								numLecturasPPG++;
								sumaLecturasPPG += data;
								lecturasPPG.add(data);
								sumaCuadrados += Math.pow((data - media), 2);
								if (data > varianza)
									pulsos++;
							}
							if (calculo >= segundosLectura) { // Se espera a que pasen los n segundos (o los definidos)
								if (calculo % segundosLectura == 0) { // Si es multiplo de n
									if (!banderaCalculos) {
										// TODO: Cálculos aki
										// Debe entrar aqui una vez pasados 30 segundos");
										System.out.println("Numero de lecturas: " + numLecturasPPG);
										media = sumaLecturasPPG / numLecturasPPG;
										varianza = sumaCuadrados / numLecturasPPG;
										System.out.println("La media es: " + media + " y la Varianza es: " + varianza);
										sumaCuadrados = 0.0;
										sumaLecturasPPG = 0.0;
										numLecturasPPG = 0;
										lecturasPPG = new ArrayList<>();
										banderaCalculos = true;
									}
									// Aki están los 6 segundos
									if (segundos < 6) {
										System.out.println("BPM: " + (pulsos / 10) * 6);
										segundos++;
									} else {
										pulsos = 0;
									}
								} else {
									banderaCalculos = false;
									segundos = 0;
								}
								mediaPPG.add(time, media);
								varianzaPPG_p.add(time, media + varianza);
								varianzaPPG_n.add(time, media - varianza);
							}

							if (banderaPPG) {
								XYSeries temp = new XYSeries(contador_preguntas);
								temp.add(time, 1000);
								temp.add(time + 5, 50);
								markers.add(temp);
								Coleccion.addSeries(temp);
								banderaPPG = false;
							}
						} catch (ArduinoException | NumberFormatException | SerialPortException ex) {
							System.out.println("Error data: " + ex);
						}
					} else {
						/*
						 * try { csvPPG.close(); csvGSR.close(); csvET.close(); contadorTiempos.close();
						 * } catch (IOException ex) { System.out.println("Error al cerrar el archivo: "
						 * + ex); }
						 */
						contadorTiempos.close();
						s3.close();
						subirArchivoS3();
						System.exit(0);
					}
				}
			} catch (ArduinoException | SerialPortException ex) {
				Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, e);
			}
		}
	};
	// Serial port gsr
	SerialPortEventListener ListenerGSR = new SerialPortEventListener() {
		@Override
		public void serialEvent(SerialPortEvent spe) {
			try {
				if (ino2.isMessageAvailable() == true) {
					time = System.currentTimeMillis();
					// j++;
					Double data;
					try {
						data = Double.parseDouble(ino2.printMessage());
						int value = (int) Math.round(data);
						SerieGSR.add(time, value);
						/*
						 * if(j%100 == 0) { XYSeries temp = new XYSeries("Punto " + j); temp.add(j,
						 * 1000); temp.add(j+1, 50); markers.add(temp); k++; Coleccion2.addSeries(temp);
						 * }
						 */
						if (banderaGSR) {
							XYSeries temp = new XYSeries(contador_preguntas);
							temp.add(time, 550);
							temp.add(time + 5, 0);
							markers.add(temp);
							Coleccion2.addSeries(temp);
							banderaGSR = false;
						}
					} catch (ArduinoException | NumberFormatException | SerialPortException ex) {
						System.out.println("Error data: " + ex);
					}
				}
			} catch (ArduinoException | SerialPortException ex) {
				Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	};

	public GraficasMedicion() {
		try {
            long tiempo = (new Date()).getTime();
            contadorTiempos = new FileWriter("tiemposrespuesta_" + tiempo + ".csv");
            s3_filename = "s3_" + UUID.randomUUID().toString().replace("-","") + ".csv";
            s3 = new FileWriter(s3_filename);
        } catch (IOException ex) {
            System.out.println("Ocurrió un error al abrir archivos: " + ex);
        }
        
		this.date_ini = (new Date()).getTime();
		numLecturasPPG = 0;
		sumaLecturasPPG = 0.0;
		sumaCuadrados = 0.0;
		media = 512.0;
		lecturasPPG = new ArrayList<>();
		initComponents();
		this.date_ini = (new Date()).getTime();
		System.out.println("TS: " + date_ini);
		initComponents();
		try {
			ino.arduinoRX(puertoSerialPPG, baudingPPG, Listener);
			ino2.arduinoRX(puertoSerialGSR, baudingGSR, ListenerGSR);
		} catch (ArduinoException ex) {
			Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SerialPortException ex) {
			Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		/*Thread t4 = new Thread() {
            public void run() {
                while (true) {
                    Scanner keyboard = new Scanner(System.in);
                    System.out.println("Responde: 1 Verdadero, 2 Falso");
                    int lectura = keyboard.nextInt(); //1 sí, 0 no
                    XYSeries temp = new XYSeries("Punto " + time);

                    temp.add(time, 1000);
                    temp.add(time, 0);
                    ColeccionPPG.addSeries(temp);
                    ColeccionGSR.addSeries(temp);
                    ColeccionEyesTobbi.addSeries(temp);
                    respuesta = lectura;
                    long tiempoActual = (new Date()).getTime();
                    System.out.println("CONTADOR: " + contador_preguntas);
                    if (contador_preguntas == 1) {
                        System.out.println("ENTRA INIT");
                        long tiempoTranscurrido = tiempoActual - date_ini;
                        String dato = contador_preguntas + "," + tiempoTranscurrido / 1000;
                        ultimoTiempo = tiempoActual;
                        contador_preguntas += 1;
                        escribirTiempos(dato);
                    } else {
                        long tiempoTranscurrido = tiempoActual - ultimoTiempo;
                        String dato = contador_preguntas + "," + tiempoTranscurrido / 1000;
                        ultimoTiempo = tiempoActual;
                        contador_preguntas += 1;
                        escribirTiempos(dato);
                    }
                }
            }
        };
        t4.start();*/

		// Gráfica de los datos que recibiremos
		// Serie.add(0, 0);
		renderer1.setSeriesPaint(0, Color.RED);
		renderer2.setSeriesPaint(0, Color.BLUE);
		renderer3.setSeriesPaint(0, Color.BLACK);
		renderer3.setSeriesPaint(1, Color.CYAN);

		Coleccion.addSeries(SeriePPG);
		Coleccion.addSeries(mediaPPG);
		Coleccion.addSeries(varianzaPPG_n);
		Coleccion.addSeries(varianzaPPG_p);
		Coleccion2.addSeries(SerieGSR);
		// ColeccionEyesTobbi.addSeries(Serie);

		// timeAxis.setAutoRange(true);
		// final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new
		// NumberAxis("Tiempo"));
		plot.setGap(10.0);

		// rangeAxis1 = (NumberAxis) subplot1.getDomainAxis();
		rangeAxis1.setRange(500, 520.00);
		rangeAxis1.setAutoRangeMinimumSize(450);
		// add the subplots...
		subplot1.setRenderer(renderer1);
		subplot2.setRenderer(renderer2);
		subplot3.setRenderer(renderer3);

		plot.add(subplot1, 1);
		plot.add(subplot2, 1);
		plot.add(subplot3, 1);

		plot.setOrientation(PlotOrientation.VERTICAL);

		Grafica = new JFreeChart("PPG / GSR", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

		ChartPanel Panel = new ChartPanel(Grafica);
		Panel.setBackground(Color.RED);
		JFrame graficos = new JFrame("Gráfica");
		graficos.getContentPane().add(Panel);
		graficos.setBackground(Color.black);
		graficos.pack();
		graficos.setVisible(true);
		graficos.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		graficos.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
		// (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the default
		 * look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null, ex);
		}
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new GraficasMedicion().setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	// End of variables declaration//GEN-END:variables

	public void setBanderas(boolean val){
		banderaGSR = val;
		banderaPPG = val;
		banderaTOBII = val;
	}

	public int getContador_preguntas() {
		return contador_preguntas;
	}

	public void setContador_preguntas(int contador_preguntas) {
		this.contador_preguntas = contador_preguntas;
	}
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mx.com.cuatronetworks.sensoresbpdp;

import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author david
 */
public class GraficasMedicion extends JFrame {

	long time = System.currentTimeMillis();
	public boolean banderaInicio = false;

	/**
	 * Creates new form GraficasMedicion
	 */
	long date_ini;
	private int tiempo_calibracion = 10;
	private int tiempo_lectura = 720;
	private boolean banderaPPG = false;
	private boolean banderaGSR = false;
	private boolean banderaTOBII = false;
	private int contador_pregunta = 1;
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
	final XYSeries SeriesTobbiEL = new XYSeries("Tobbi EL");
	final XYSeries SeriesTobbiER = new XYSeries("Tobii Promedio");
	final XYSeriesCollection ColeccionEyesTobbi = new XYSeriesCollection();

	// create subplot 2...
	final XYItemRenderer renderer3 = new StandardXYItemRenderer();
	final NumberAxis rangeAxis3 = new NumberAxis("Tobii");
	final XYPlot subplot3 = new XYPlot(ColeccionEyesTobbi, null, rangeAxis3, renderer3);
	final DateAxis Daxis = new DateAxis();

	// Markers
	final List<XYSeries> markers = new ArrayList<XYSeries>();

	JFreeChart Grafica;
	int prevdata = 0;
	int newdata = 0;
	int temp = 0;
	int maxi = 0;

	int prevdata_2 = 0;
	int newdata_2 = 0;
	int temp_2 = 0;
	int threshold = 10;
	int bpm_previous = 0;
	int bpm = 0;
	long last_beattime = 0;
	long current_beattime = 0;

	int i = 0;
	int j = 0;
	int k = 0;

	int numLecturasPPG = 0;
	private boolean banderaCalculos = false;
	private Double sumaLecturasPPG;
	private List<Double> lecturasPPG;
	private Integer segundosLectura = 2;
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
							if (banderaInicio != false) {
								SeriePPG.add(time, value);
							}
							//SeriePPG.add(time, value);
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
								if (banderaInicio != false) {
									mediaPPG.add(time, media);
									varianzaPPG_p.add(time, media + varianza);
									varianzaPPG_n.add(time, media - varianza);
								}
								
							}

							if (banderaPPG) {
								XYSeries temp = new XYSeries("PPG#" + contador_pregunta);
								temp.add(time, 510);
								temp.add(time + 5, 490);
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
						System.exit(0);
					}
				}
			} catch (ArduinoException | SerialPortException ex) {
				Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	};

	/* VERSIÓN 2 - 1 segundo */
	/*
	SerialPortEventListener Listener = new SerialPortEventListener() {

		int numLecturas = 0;
		long segundoActual = 0;

		@Override
		public void serialEvent(SerialPortEvent spe) {
			k += 1;
			try {
				if (ino.isMessageAvailable() == true) {
					time = System.currentTimeMillis();
					numLecturas++;
					Double data = 0.0;
					long current_time = (new Date()).getTime();
					long calculo = (current_time - date_ini) / 1000;
					if (calculo < tiempo_lectura) {
						try {
							data = Double.parseDouble(ino.printMessage());
							int value = (int) Math.round(data);
							String dato = time + "," + value + "," + 0.0 + "," + 0.0 + "," + "'Sin datos'";
							if (banderaInicio != false) {
								SeriePPG.add(time, value);
							}
							if (data > 500 && data < 1000) {
								numLecturasPPG++;
								sumaLecturasPPG += data;
								lecturasPPG.add(data);
								sumaCuadrados += Math.pow((data - media), 2);
								if (data > varianza)
									pulsos++;
							}
							if (calculo > 30) { 
								if (calculo % segundosLectura == 0) { 
									if (calculo > segundoActual)
										banderaCalculos = false;
									if (!banderaCalculos) {
										media = sumaLecturasPPG / numLecturasPPG;
										varianza = sumaCuadrados / numLecturasPPG;
										System.out.println("La media es: " + media + " y la Varianza es: " + varianza);
										sumaCuadrados = 0.0;
										sumaLecturasPPG = 0.0;
										numLecturasPPG = 0;
										if (lecturasPPG.size() > 600)
											lecturasPPG = lecturasPPG.subList(lecturasPPG.size() - 580,
													lecturasPPG.size() - 1);

										segundoActual = calculo;
										banderaCalculos = true;
									}
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
								if (banderaInicio != false) {
									mediaPPG.add(time, media);
									varianzaPPG_p.add(time, media + varianza);
									varianzaPPG_n.add(time, media - varianza);
								}
							}

							if (banderaPPG) {
								XYSeries temp = new XYSeries("PPG#" + contador_pregunta);
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
						System.exit(0);
					}
				}
			} catch (ArduinoException | SerialPortException ex) {
				System.out.println("ERROR: " + ex);
			}
		}
	};*/

	// Serial port gsr
	SerialPortEventListener ListenerGSR = new SerialPortEventListener() {
		@Override
		public void serialEvent(SerialPortEvent spe) {
			try {
				if (ino2.isMessageAvailable() == true) {
					time = System.currentTimeMillis();
					Double data;
					try {
						data = Double.parseDouble(ino2.printMessage());
						int value = (int) Math.round(data);
						if (banderaInicio != false) {
							SerieGSR.add(time, value);
						}
						/*
						 * if(j%100 == 0) { XYSeries temp = new XYSeries("Punto " + j); temp.add(j,
						 * 1000); temp.add(j+1, 50); markers.add(temp); k++; Coleccion2.addSeries(temp);
						 * }
						 */
						if (banderaGSR) {
							XYSeries temp = new XYSeries("GSR#" + contador_pregunta);
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

	// EYE TRACKER
	private void obtenerDatosTobii() {
		java.util.List<Double> valoresPromedio = new ArrayList<>();
		try {
			// Process process = new
			// ProcessBuilder("/home/edgar/Documentos/Git4N/dpr-cabinas/codigos-eyetracker/full_script_v2").start();
			Process process = Runtime.getRuntime()
					.exec("/home/edgar/Documentos/Git4N/dpr-cabinas/codigos-eyetracker/full_script_v2");
			InputStream processInputStream = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(processInputStream));
			String line = reader.readLine();
			double derecho = 0.0;
			double izquierdo = 0.0;
			while ((line = reader.readLine()) != null && !reader.equals("exit")) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException ex) {
					System.out.println("ERROR: " + ex);
				}
				time = System.currentTimeMillis();
				String[] parts = line.split(",");
				derecho = Double.valueOf(parts[0]);
				izquierdo = Double.valueOf(parts[1]);

				double suma = (derecho + izquierdo) / 2;

				/*
				 * escribirET(String.valueOf(suma) + "," + String.valueOf(time) + "," +
				 * String.valueOf(suma) + "," + String.valueOf(suma) + "," +
				 * String.valueOf(suma) + ",10");
				 */
				if (banderaTOBII) {
					XYSeries temp = new XYSeries("ET#" + contador_pregunta);
					temp.add(time, 5);
					temp.add(time + 5, 0);
					markers.add(temp);
					ColeccionEyesTobbi.addSeries(temp);
					banderaTOBII = false;
				}
				if (banderaInicio != false) {
					SeriesTobbiER.add(time, suma);
				}
				// rSeriesTobbiER.add(time, suma);
			}
		} catch (IOException ex) {
			System.out.println("Error IOException: " + ex);
		}
	}

	public GraficasMedicion() {
		// SeriePPG.add(time, 500);
		this.date_ini = (new Date()).getTime();
		numLecturasPPG = 0;
		sumaLecturasPPG = 0.0;
		sumaCuadrados = 0.0;
		media = 512.0;
		lecturasPPG = new ArrayList<>();
		// initComponents();
		this.date_ini = (new Date()).getTime();
		System.out.println("TS: " + date_ini);
		initComponents();
		/*
		 * try { ino.arduinoRX(puertoSerialPPG, baudingPPG, Listener);
		 * ino2.arduinoRX(puertoSerialGSR, baudingGSR, ListenerGSR); } catch
		 * (ArduinoException ex) {
		 * Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null,
		 * ex); } catch (SerialPortException ex) {
		 * Logger.getLogger(GraficasMedicion.class.getName()).log(Level.SEVERE, null,
		 * ex); }
		 */

		Thread t1 = new Thread() {
			public void run() {
				try {
					ino.arduinoRX(puertoSerialPPG, baudingPPG, Listener);
					ino2.arduinoRX(puertoSerialGSR, baudingGSR, ListenerGSR);
				} catch (ArduinoException ex) {
					System.out.println("Error: " + ex);
				} catch (SerialPortException ex) {
					System.out.println("Error: " + ex);
				}
			}
		};
		t1.start();

		Thread t2 = new Thread() {
			public void run() {
				obtenerDatosTobii();
			}
		};
		t2.start();

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
		ColeccionEyesTobbi.addSeries(SeriesTobbiER);

		// timeAxis.setAutoRange(true);
		// final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new
		// NumberAxis("Tiempo"));
		plot.setGap(10.0);

		// rangeAxis1 = (NumberAxis) subplot1.getDomainAxis();
		rangeAxis1.setRange(490, 550);
		rangeAxis2.setRange(0, 90);
		//rangeAxis2.setAutoRangeIncludesZero(false);
		// rangeAxis1.setAutoRangeMinimumSize(450);

		// rangeAxis1.setAutoRangeIncludesZero(false);
		// add the subplots...
		subplot1.setRenderer(renderer1);
		subplot2.setRenderer(renderer2);
		subplot3.setRenderer(renderer3);

		plot.add(subplot1, 1);
		plot.add(subplot2, 1);
		plot.add(subplot3, 1);

		plot.setOrientation(PlotOrientation.VERTICAL);

		Grafica = new JFreeChart("PPG / GSR / Eye Tracker", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

		XYPlot plot = Grafica.getXYPlot();
		ValueAxis xaxis = plot.getDomainAxis();
		xaxis.setAutoRange(true);

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

	public void setBandera(boolean bandera) {
		this.banderaPPG = bandera;
		this.banderaGSR = bandera;
		this.banderaTOBII = bandera;
	}

	public int getContador_pregunta() {
		return contador_pregunta;
	}

	public void setContador_pregunta(int contador_pregunta) {
		this.contador_pregunta = contador_pregunta;
	}

	public boolean isBanderaInicio() {
		return banderaInicio;
	}

	public void setBanderaInicio(boolean banderaInicio) {
		this.banderaInicio = banderaInicio;
	}

}

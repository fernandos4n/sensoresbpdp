/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.com.cuatronetworks.sensoresbpdp.model;

import com.opencsv.bean.CsvBindByPosition;

/**
 *
 * @author david
 */
public class Pregunta {
    // CONSTANTES
    public static final String INSTRUCCION = "Instruccion";

   //FSC 2021
   @CsvBindByPosition(position = 0)
   private String riesgo;

   @CsvBindByPosition(position = 1)
   private String tema;

   @CsvBindByPosition(position = 2)
   private String reactivo;

   @CsvBindByPosition(position = 3)
   private String respuesta_esperada;
    
    public Pregunta(){}

    /**
     * @return the id
     */
    public Pregunta(String tema,String reactivo,String respuesta_esperada,String riesgo){
        super();
        this.tema=tema;
        this.reactivo=reactivo;
        this.respuesta_esperada=respuesta_esperada;
        this.riesgo=riesgo;

    }
    
    /**
     * @return the tema
     */
    public String getTema() {
        return tema;
    }

    /**
     * @param tema the tema to set
     */
    public void setTema(String tema) {
        this.tema = tema;
    }

    /**
     * @return the reactivo
     */
    public String getReactivo() {
        return reactivo;
    }

    /**
     * @param reactivo the reactivo to set
     */
    public void setReactivo(String reactivo) {
        this.reactivo = reactivo;
    }

    /**
     * @return the respuesta_esperada
     */
    public String getRespuesta_esperada() {
        return respuesta_esperada;
    }

    /**
     * @param respuesta_esperada the respuesta_esperada to set
     */
    public void setRespuesta_esperada(String respuesta_esperada) {
        this.respuesta_esperada = respuesta_esperada;
    }
    
    
    @Override
    public String toString(){
      return tema+" "+reactivo+" "+respuesta_esperada;
    
    }
    /**
     * @return the riesgo
     */
    public String getRiesgo() {
        return riesgo;
    }

    /**
     * @param riesgo the riesgo to set
     */
    public void setRiesgo(String riesgo) {
        this.riesgo = riesgo;
    }
    
    
    
}

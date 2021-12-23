/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.com.cuatronetworks.sensoresbpdp.model;

/**
 *
 * @author david
 */
public class Respuesta {
    
    private int pregunta;
    private int respuesta;
    private int correcto;
    private int tiempoInicio;
    private int tiempoFinal;
    
    
    public Respuesta(){}

    
    public Respuesta(int pregunta,int respuesta,int correcto, int tiempoInicio, int tiempoFinal){
       this.pregunta=pregunta;
       this.respuesta=respuesta;
       this.correcto=correcto;
       this.tiempoInicio=tiempoInicio;
       this.tiempoFinal=tiempoFinal;
    }
    
    
    /**
     * @return the pregunta
     */
    public int getPregunta() {
        return pregunta;
    }

    /**
     * @param pregunta the pregunta to set
     */
    public void setPregunta(int pregunta) {
        this.pregunta = pregunta;
    }

    /**
     * @return the respuesta
     */
    public int getRespuesta() {
        return respuesta;
    }

    /**
     * @param respuesta the respuesta to set
     */
    public void setRespuesta(int respuesta) {
        this.respuesta = respuesta;
    }

    /**
     * @return the correcto
     */
    public int getCorrecto() {
        return correcto;
    }

    /**
     * @param correcto the correcto to set
     */
    public void setCorrecto(int correcto) {
        this.correcto = correcto;
    }

    /**
     * @return the tiempoInicio
     */
    public int getTiempoInicio() {
        return tiempoInicio;
    }

    /**
     * @param tiempoInicio the tiempoInicio to set
     */
    public void setTiempoInicio(int tiempoInicio) {
        this.tiempoInicio = tiempoInicio;
    }

    /**
     * @return the tiempoFinal
     */
    public int getTiempoFinal() {
        return tiempoFinal;
    }

    /**
     * @param tiempoFinal the tiempoFinal to set
     */
    public void setTiempoFinal(int tiempoFinal) {
        this.tiempoFinal = tiempoFinal;
    }

 

  

    
    
    
}

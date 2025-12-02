package com.emergencias.modelos;

/**
 * Enumeración que define los niveles de prioridad de las emergencias médicas.
 * Cada nivel tiene un valor numérico asociado para facilitar la comparación.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public enum Prioridad {
    CRITICO(4, "Riesgo de muerte inminente"),
    GRAVE(3, "Requiere atención urgente"),
    MODERADO(2, "Atención necesaria en breve"),
    LEVE(1, "Puede esperar");
    
    private final int valor;
    private final String descripcion;
    
    Prioridad(int valor, String descripcion) {
        this.valor = valor;
        this.descripcion = descripcion;
    }
    
    public int getValor() {
        return valor;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
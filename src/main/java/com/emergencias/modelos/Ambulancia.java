package com.emergencias.modelos;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Representa una ambulancia del sistema de emergencias.
 * Incluye información sobre disponibilidad, ubicación y capacidad.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public class Ambulancia {
    private final String id;
    private final AtomicBoolean disponible;
    private double latitud;
    private double longitud;
    private Emergencia emergenciaActual;
    private final int capacidadMedicos;
    
    public enum TipoAmbulancia {
        BASICA(2), AVANZADA(4), UCI_MOVIL(6);
        
        private final int capacidad;
        TipoAmbulancia(int capacidad) { this.capacidad = capacidad; }
        public int getCapacidad() { return capacidad; }
    }
    
    private final TipoAmbulancia tipo;
    
    public Ambulancia(String id, TipoAmbulancia tipo, double latitud, double longitud) {
        this.id = id;
        this.tipo = tipo;
        this.capacidadMedicos = tipo.getCapacidad();
        this.latitud = latitud;
        this.longitud = longitud;
        this.disponible = new AtomicBoolean(true);
    }
    
    /**
     * Intenta reservar la ambulancia de forma thread-safe.
     * @return true si se pudo reservar, false si ya estaba ocupada
     */
    public boolean reservar() {
        return disponible.compareAndSet(true, false);
    }
    
    /**
     * Libera la ambulancia para nuevas asignaciones.
     */
    public void liberar() {
        this.emergenciaActual = null;
        disponible.set(true);
    }
    
    /**
     * Calcula la distancia a una emergencia.
     * @param emergencia La emergencia objetivo
     * @return distancia calculada
     */
    public double calcularDistancia(Emergencia emergencia) {
        double deltaLat = emergencia.getLatitud() - this.latitud;
        double deltaLon = emergencia.getLongitud() - this.longitud;
        return Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);
    }
    
    /**
     * Mueve la ambulancia a la ubicación de la emergencia.
     * @param emergencia Emergencia destino
     */
    public void moverA(Emergencia emergencia) {
        this.latitud = emergencia.getLatitud();
        this.longitud = emergencia.getLongitud();
        this.emergenciaActual = emergencia;
    }
    
    // Getters
    public String getId() { return id; }
    public boolean isDisponible() { return disponible.get(); }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public Emergencia getEmergenciaActual() { return emergenciaActual; }
    public TipoAmbulancia getTipo() { return tipo; }
    public int getCapacidadMedicos() { return capacidadMedicos; }
    
    @Override
    public String toString() {
        return String.format("%s [%s] - %s", id, tipo, 
            disponible.get() ? "DISPONIBLE" : "OCUPADA");
    }
}
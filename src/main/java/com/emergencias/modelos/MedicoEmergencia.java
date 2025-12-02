package com.emergencias.modelos;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Representa un médico de emergencias en el sistema.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public class MedicoEmergencia {
    private final String id;
    private final String nombre;
    private final String especialidad;
    private final AtomicBoolean disponible;
    private Emergencia emergenciaAsignada;
    
    public enum Especialidad {
        PARAMEDICO("Paramédico"),
        MEDICO_GENERAL("Médico General"),
        URGENCIOLOGO("Urgenciólogo"),
        CARDIOLOGO("Cardiólogo"),
        TRAUMATOLOGO("Traumatólogo");
        
        private final String nombre;
        Especialidad(String nombre) { this.nombre = nombre; }
        public String getNombre() { return nombre; }
    }
    
    public MedicoEmergencia(String id, String nombre, String especialidad) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.disponible = new AtomicBoolean(true);
    }
    
    /**
     * Intenta asignar el médico de forma thread-safe.
     * @param emergencia La emergencia a atender
     * @return true si se pudo asignar, false si ya estaba ocupado
     */
    public boolean asignar(Emergencia emergencia) {
        if (disponible.compareAndSet(true, false)) {
            this.emergenciaAsignada = emergencia;
            return true;
        }
        return false;
    }
    
    /**
     * Libera al médico después de atender una emergencia.
     */
    public void liberar() {
        this.emergenciaAsignada = null;
        disponible.set(true);
    }
    
    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEspecialidad() { return especialidad; }
    public boolean isDisponible() { return disponible.get(); }
    public Emergencia getEmergenciaAsignada() { return emergenciaAsignada; }
    
    @Override
    public String toString() {
        return String.format("%s - %s [%s] - %s", 
            id, nombre, especialidad, 
            disponible.get() ? "DISPONIBLE" : "ATENDIENDO");
    }
}
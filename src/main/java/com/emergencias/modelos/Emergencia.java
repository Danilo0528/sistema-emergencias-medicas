package com.emergencias.modelos;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa una emergencia médica en el sistema.
 * Incluye información sobre ubicación, gravedad, tiempo de espera y estado.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public class Emergencia implements Comparable<Emergencia> {
    private static final AtomicInteger contadorId = new AtomicInteger(0);
    
    private final int id;
    private final String ubicacion;
    private final Prioridad prioridad;
    private final String descripcion;
    private final LocalDateTime horaLlamada;
    private final double latitud;
    private final double longitud;
    private EstadoEmergencia estado;
    private String ambulanciaAsignada;
    
    public enum EstadoEmergencia {
        PENDIENTE, EN_PROCESO, ATENDIDA, CANCELADA
    }
    
    public Emergencia(String ubicacion, Prioridad prioridad, String descripcion, 
                      double latitud, double longitud) {
        this.id = contadorId.incrementAndGet();
        this.ubicacion = ubicacion;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.horaLlamada = LocalDateTime.now();
        this.latitud = latitud;
        this.longitud = longitud;
        this.estado = EstadoEmergencia.PENDIENTE;
    }
    
    /**
     * Calcula la prioridad efectiva considerando gravedad y tiempo de espera.
     * @return valor de prioridad calculado
     */
    public double calcularPrioridadEfectiva() {
        long minutosEspera = java.time.Duration.between(horaLlamada, LocalDateTime.now()).toMinutes();
        // Factor de urgencia aumenta con el tiempo de espera
        double factorTiempo = 1 + (minutosEspera * 0.1);
        return prioridad.getValor() * factorTiempo;
    }
    
    /**
     * Calcula la distancia euclidiana a una ubicación dada.
     * @param lat Latitud destino
     * @param lon Longitud destino
     * @return distancia aproximada
     */
    public double calcularDistancia(double lat, double lon) {
        return Math.sqrt(Math.pow(latitud - lat, 2) + Math.pow(longitud - lon, 2));
    }
    
    @Override
    public int compareTo(Emergencia otra) {
        // Comparación inversa para que mayor prioridad vaya primero
        return Double.compare(otra.calcularPrioridadEfectiva(), this.calcularPrioridadEfectiva());
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public String getUbicacion() { return ubicacion; }
    public Prioridad getPrioridad() { return prioridad; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getHoraLlamada() { return horaLlamada; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public EstadoEmergencia getEstado() { return estado; }
    public void setEstado(EstadoEmergencia estado) { this.estado = estado; }
    public String getAmbulanciaAsignada() { return ambulanciaAsignada; }
    public void setAmbulanciaAsignada(String ambulanciaAsignada) { 
        this.ambulanciaAsignada = ambulanciaAsignada; 
    }
    
    @Override
    public String toString() {
        return String.format("EMG-%03d [%s] %s - %s (%.2f mins espera)", 
            id, prioridad, ubicacion, estado, 
            java.time.Duration.between(horaLlamada, LocalDateTime.now()).toMinutes() / 1.0);
    }
}
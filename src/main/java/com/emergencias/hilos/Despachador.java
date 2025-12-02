package com.emergencias.hilos;

import com.emergencias.modelos.*;
import com.emergencias.gestores.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Hilo que asigna recursos (ambulancias y médicos) a emergencias.
 * Implementa patrón Consumer procesando la cola de prioridad.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public class Despachador implements Runnable {
    private final String id;
    private final GestorEmergencias gestorEmergencias;
    private final GestorRecursos gestorRecursos;
    private volatile boolean activo;
    private int emergenciasDespachas;
    
    public Despachador(String id) {
        this.id = id;
        this.gestorEmergencias = GestorEmergencias.getInstancia();
        this.gestorRecursos = GestorRecursos.getInstancia();
        this.activo = true;
        this.emergenciasDespachas = 0;
    }
    
    @Override
    public void run() {
        System.out.println(id + " iniciado y esperando emergencias...");
        
        try {
            while (activo && !Thread.currentThread().isInterrupted()) {
                // Obtener siguiente emergencia (bloquea si no hay)
                Emergencia emergencia = gestorEmergencias.obtenerSiguienteEmergencia(3, TimeUnit.SECONDS);
                
                if (emergencia != null) {
                    procesarEmergencia(emergencia);
                }
            }
        } catch (InterruptedException e) {
            System.out.println(id + " interrumpido");
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(id + " finalizado. Total despachadas: " + emergenciasDespachas);
        }
    }
    
    /**
     * Procesa una emergencia asignando recursos necesarios.
     * @param emergencia Emergencia a procesar
     */
    private void procesarEmergencia(Emergencia emergencia) {
        System.out.println("\n" + id + " procesando: " + emergencia);
        
        try {
            // Paso 1: Asignar ambulancia
            Ambulancia ambulancia = gestorRecursos.asignarAmbulancia(emergencia);
            
            if (ambulancia == null) {
                System.out.println(id + ": No hay ambulancias disponibles para EMG-" +
                                 String.format("%03d", emergencia.getId()));
                // Reencolar la emergencia
                gestorEmergencias.registrarEmergencia(emergencia);
                return;
            }
            
            // Paso 2: Asignar médicos según capacidad de la ambulancia
            int medicosRequeridos = calcularMedicosRequeridos(emergencia, ambulancia);
            List<MedicoEmergencia> medicos = gestorRecursos.asignarMedicos(medicosRequeridos, emergencia);
            
            if (medicos.size() < medicosRequeridos) {
                System.out.println(id + ": Médicos insuficientes. Se asignaron " +
                                 medicos.size() + " de " + medicosRequeridos);
            }
            
            // Paso 3: Marcar emergencia como en proceso
            gestorEmergencias.marcarEnProceso(emergencia);
            emergenciasDespachas++;
            
            // Paso 4: Simular tiempo de atención y traslado
            simularAtencion(emergencia, ambulancia, medicos);
            
        } catch (Exception e) {
            System.err.println("Error en " + id + " procesando emergencia: " + e.getMessage());
        }
    }
    
    /**
     * Calcula cantidad de médicos necesarios según gravedad.
     * @param emergencia Emergencia a evaluar
     * @param ambulancia Ambulancia asignada
     * @return Número de médicos requeridos
     */
    private int calcularMedicosRequeridos(Emergencia emergencia, Ambulancia ambulancia) {
        int base = switch (emergencia.getPrioridad()) {
            case CRITICO -> 3;
            case GRAVE -> 2;
            case MODERADO -> 2;
            case LEVE -> 1;
        };
        
        // No exceder capacidad de la ambulancia
        return Math.min(base, ambulancia.getCapacidadMedicos());
    }
    
    /**
     * Simula el proceso completo de atención de la emergencia.
     * @param emergencia Emergencia atendida
     * @param ambulancia Ambulancia asignada
     * @param medicos Médicos asignados
     */
    private void simularAtencion(Emergencia emergencia, Ambulancia ambulancia, 
                                 List<MedicoEmergencia> medicos) {
        try {
            // Calcular tiempos según distancia y prioridad
            double distancia = ambulancia.calcularDistancia(emergencia);
            int tiempoLlegada = (int) (distancia * 2) + 1; // Segundos simulados
            int tiempoAtencion = switch (emergencia.getPrioridad()) {
                case CRITICO -> 8;
                case GRAVE -> 5;
                case MODERADO -> 3;
                case LEVE -> 2;
            };
            
            // Simular llegada
            System.out.println("  🚑 " + ambulancia.getId() + " en camino... " +
                             "(~" + tiempoLlegada + "s)");
            TimeUnit.SECONDS.sleep(tiempoLlegada);
            
            // Simular atención médica
            System.out.println("  Atendiendo EMG-" + String.format("%03d", emergencia.getId()) +
                             " con " + medicos.size() + " médicos...");
            TimeUnit.SECONDS.sleep(tiempoAtencion);
            
            // Completar emergencia
            gestorEmergencias.marcarAtendida(emergencia);
            
            // Liberar recursos
            gestorRecursos.liberarAmbulancia(ambulancia);
            gestorRecursos.liberarMedicos(medicos);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("⚠ Atención interrumpida para EMG-" + emergencia.getId());
        }
    }
    
    public void detener() {
        activo = false;
    }
    
    public String getId() {
        return id;
    }
    
    public int getEmergenciasDespachas() {
        return emergenciasDespachas;
    }
}
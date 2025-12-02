package com.emergencias.gestores;

import com.emergencias.modelos.*;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Gestor centralizado de recursos médicos (ambulancias y médicos).
 * Implementa patrón Singleton para acceso global thread-safe.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public class GestorRecursos {
    private static GestorRecursos instancia;
    private final ConcurrentHashMap<String, Ambulancia> ambulancias;
    private final ConcurrentHashMap<String, MedicoEmergencia> medicos;
    private final Semaphore semaforoAmbulancias;
    private final Semaphore semaforoMedicos;
    
    private GestorRecursos() {
        ambulancias = new ConcurrentHashMap<>();
        medicos = new ConcurrentHashMap<>();
        semaforoAmbulancias = new Semaphore(0);
        semaforoMedicos = new Semaphore(0);
        inicializarRecursos();
    }
    
    /**
     * Obtiene la instancia única del gestor (Singleton thread-safe).
     * @return instancia del gestor
     */
    public static synchronized GestorRecursos getInstancia() {
        if (instancia == null) {
            instancia = new GestorRecursos();
        }
        return instancia;
    }
    
    /**
     * Inicializa el pool de recursos del sistema.
     */
    private void inicializarRecursos() {
        // Crear 10 ambulancias en diferentes ubicaciones
        for (int i = 1; i <= 10; i++) {
            Ambulancia.TipoAmbulancia tipo = i <= 3 ? Ambulancia.TipoAmbulancia.UCI_MOVIL :
                                             i <= 6 ? Ambulancia.TipoAmbulancia.AVANZADA :
                                                     Ambulancia.TipoAmbulancia.BASICA;
            double lat = 6.0 + (Math.random() * 0.5);
            double lon = -75.0 + (Math.random() * 0.5);
            Ambulancia ambulancia = new Ambulancia("AMB-" + String.format("%03d", i), tipo, lat, lon);
            ambulancias.put(ambulancia.getId(), ambulancia);
            semaforoAmbulancias.release();
        }
        
        // Crear 20 médicos de emergencia
        String[] especialidades = {"Paramédico", "Médico General", "Urgenciólogo", "Cardiólogo", "Traumatólogo"};
        for (int i = 1; i <= 20; i++) {
            String especialidad = especialidades[(i - 1) % especialidades.length];
            MedicoEmergencia medico = new MedicoEmergencia(
                "MED-" + String.format("%03d", i),
                "Dr. Médico " + i,
                especialidad
            );
            medicos.put(medico.getId(), medico);
            semaforoMedicos.release();
        }
        
        System.out.println("✓ Sistema inicializado: " + ambulancias.size() + 
                          " ambulancias, " + medicos.size() + " médicos");
    }
    
    /**
     * Asigna la ambulancia más cercana disponible a una emergencia.
     * @param emergencia Emergencia que requiere ambulancia
     * @return Ambulancia asignada o null si no hay disponibles
     */
    public Ambulancia asignarAmbulancia(Emergencia emergencia) {
        try {
            // Intenta adquirir permiso (espera si no hay ambulancias)
            if (semaforoAmbulancias.tryAcquire(5, TimeUnit.SECONDS)) {
                // Busca la ambulancia disponible más cercana
                Ambulancia mejorAmbulancia = ambulancias.values().stream()
                    .filter(Ambulancia::isDisponible)
                    .min(Comparator.comparingDouble(a -> a.calcularDistancia(emergencia)))
                    .orElse(null);
                
                if (mejorAmbulancia != null && mejorAmbulancia.reservar()) {
                    mejorAmbulancia.moverA(emergencia);
                    emergencia.setAmbulanciaAsignada(mejorAmbulancia.getId());
                    System.out.println("  → " + mejorAmbulancia.getId() + 
                                     " asignada a EMG-" + String.format("%03d", emergencia.getId()));
                    return mejorAmbulancia;
                } else {
                    semaforoAmbulancias.release(); // Devolver permiso si falló
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("⚠ Interrupción al asignar ambulancia");
        }
        return null;
    }
    
    /**
     * Asigna médicos disponibles a una ambulancia.
     * @param cantidad Número de médicos requeridos
     * @return Lista de médicos asignados
     */
    public List<MedicoEmergencia> asignarMedicos(int cantidad, Emergencia emergencia) {
        List<MedicoEmergencia> medicosAsignados = new ArrayList<>();
        
        try {
            if (semaforoMedicos.tryAcquire(cantidad, 3, TimeUnit.SECONDS)) {
                List<MedicoEmergencia> disponibles = medicos.values().stream()
                    .filter(MedicoEmergencia::isDisponible)
                    .limit(cantidad)
                    .collect(Collectors.toList());
                
                for (MedicoEmergencia medico : disponibles) {
                    if (medico.asignar(emergencia)) {
                        medicosAsignados.add(medico);
                    } else {
                        semaforoMedicos.release();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("⚠ Interrupción al asignar médicos");
        }
        
        return medicosAsignados;
    }
    
    /**
     * Libera una ambulancia después de completar una emergencia.
     * @param ambulancia Ambulancia a liberar
     */
    public void liberarAmbulancia(Ambulancia ambulancia) {
        ambulancia.liberar();
        semaforoAmbulancias.release();
        System.out.println("  ← " + ambulancia.getId() + " liberada y disponible");
    }
    
    /**
     * Libera médicos después de atender una emergencia.
     * @param medicos Lista de médicos a liberar
     */
    public void liberarMedicos(List<MedicoEmergencia> medicos) {
        for (MedicoEmergencia medico : medicos) {
            medico.liberar();
            semaforoMedicos.release();
        }
    }
    
    // Métodos de consulta
    public int getAmbulanciasTotales() {
        return ambulancias.size();
    }
    
    public int getAmbulanciasDisponibles() {
        return (int) ambulancias.values().stream().filter(Ambulancia::isDisponible).count();
    }
    
    public int getMedicosTotales() {
        return medicos.size();
    }
    
    public int getMedicosDisponibles() {
        return (int) medicos.values().stream().filter(MedicoEmergencia::isDisponible).count();
    }
    
    public List<Ambulancia> getAmbulancias() {
        return new ArrayList<>(ambulancias.values());
    }
    
    public List<MedicoEmergencia> getMedicos() {
        return new ArrayList<>(medicos.values());
    }
}
package com.emergencias.gestores;

import com.emergencias.modelos.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

/**
 * Gestor principal del sistema de emergencias.
 * Maneja la cola de prioridad y coordina el procesamiento de emergencias.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public class GestorEmergencias {
    private static GestorEmergencias instancia;
    private final PriorityBlockingQueue<Emergencia> colaEmergencias;
    private final ConcurrentHashMap<Integer, Emergencia> emergenciasActivas;
    private final AtomicInteger emergenciasAtendidas;
    private final AtomicInteger emergenciasCanceladas;
    private final CopyOnWriteArrayList<EmergenciaListener> listeners;
    
    // Estadísticas
    private final ConcurrentHashMap<Prioridad, AtomicInteger> estadisticasPorPrioridad;
    private volatile long tiempoPromedioRespuesta;
    
    /**
     * Interface para notificaciones de cambios en emergencias.
     */
    public interface EmergenciaListener {
        void onEmergenciaCreada(Emergencia emergencia);
        void onEmergenciaAsignada(Emergencia emergencia);
        void onEmergenciaAtendida(Emergencia emergencia);
    }
    
    private GestorEmergencias() {
        colaEmergencias = new PriorityBlockingQueue<>(100);
        emergenciasActivas = new ConcurrentHashMap<>();
        emergenciasAtendidas = new AtomicInteger(0);
        emergenciasCanceladas = new AtomicInteger(0);
        listeners = new CopyOnWriteArrayList<>();
        estadisticasPorPrioridad = new ConcurrentHashMap<>();
        
        // Inicializar estadísticas para cada prioridad
        for (Prioridad p : Prioridad.values()) {
            estadisticasPorPrioridad.put(p, new AtomicInteger(0));
        }
    }
    
    public static synchronized GestorEmergencias getInstancia() {
        if (instancia == null) {
            instancia = new GestorEmergencias();
        }
        return instancia;
    }
    
    /**
     * Registra una nueva emergencia en el sistema.
     * @param emergencia Emergencia a registrar
     */
    public void registrarEmergencia(Emergencia emergencia) {
        emergenciasActivas.put(emergencia.getId(), emergencia);
        colaEmergencias.offer(emergencia);
        estadisticasPorPrioridad.get(emergencia.getPrioridad()).incrementAndGet();
        
        System.out.println("📞 NUEVA EMERGENCIA: " + emergencia);
        
        // Notificar a listeners
        for (EmergenciaListener listener : listeners) {
            listener.onEmergenciaCreada(emergencia);
        }
    }
    
    /**
     * Obtiene la siguiente emergencia de mayor prioridad.
     * Bloquea si no hay emergencias disponibles.
     * @return Siguiente emergencia a atender
     */
    public Emergencia obtenerSiguienteEmergencia() throws InterruptedException {
        return colaEmergencias.take();
    }
    
    /**
     * Obtiene la siguiente emergencia con timeout.
     * @param timeout Tiempo máximo de espera
     * @param unit Unidad de tiempo
     * @return Emergencia o null si timeout
     */
    public Emergencia obtenerSiguienteEmergencia(long timeout, TimeUnit unit) 
            throws InterruptedException {
        return colaEmergencias.poll(timeout, unit);
    }
    
    /**
     * Marca una emergencia como en proceso.
     * @param emergencia Emergencia siendo procesada
     */
    public void marcarEnProceso(Emergencia emergencia) {
        emergencia.setEstado(Emergencia.EstadoEmergencia.EN_PROCESO);
        System.out.println("🚑 EN CAMINO: EMG-" + String.format("%03d", emergencia.getId()) + 
                         " - " + emergencia.getUbicacion());
        
        for (EmergenciaListener listener : listeners) {
            listener.onEmergenciaAsignada(emergencia);
        }
    }
    
    /**
     * Marca una emergencia como atendida y actualiza estadísticas.
     * @param emergencia Emergencia completada
     */
    public void marcarAtendida(Emergencia emergencia) {
        emergencia.setEstado(Emergencia.EstadoEmergencia.ATENDIDA);
        emergenciasActivas.remove(emergencia.getId());
        emergenciasAtendidas.incrementAndGet();
        
        long tiempoRespuesta = java.time.Duration.between(
            emergencia.getHoraLlamada(), 
            java.time.LocalDateTime.now()
        ).toMinutes();
        
        System.out.println(" -COMPLETADA: EMG-" + String.format("%03d", emergencia.getId()) + 
                         " (Tiempo: " + tiempoRespuesta + " mins)");
        
        for (EmergenciaListener listener : listeners) {
            listener.onEmergenciaAtendida(emergencia);
        }
    }
    
    /**
     * Cancela una emergencia.
     * @param emergenciaId ID de la emergencia
     */
    public void cancelarEmergencia(int emergenciaId) {
        Emergencia emergencia = emergenciasActivas.get(emergenciaId);
        if (emergencia != null) {
            emergencia.setEstado(Emergencia.EstadoEmergencia.CANCELADA);
            emergenciasActivas.remove(emergenciaId);
            colaEmergencias.remove(emergencia);
            emergenciasCanceladas.incrementAndGet();
        }
    }
    
    /**
     * Registra un listener para eventos de emergencias.
     * @param listener Listener a registrar
     */
    public void agregarListener(EmergenciaListener listener) {
        listeners.add(listener);
    }
    
    // Métodos de consulta
    public int getEmergenciasPendientes() {
        return colaEmergencias.size();
    }
    
    public int getEmergenciasActivas() {
        return emergenciasActivas.size();
    }
    
    public int getEmergenciasAtendidas() {
        return emergenciasAtendidas.get();
    }
    
    public int getEmergenciasCanceladas() {
        return emergenciasCanceladas.get();
    }
    
    public List<Emergencia> getColaEmergencias() {
        return new ArrayList<>(colaEmergencias);
    }
    
    public List<Emergencia> getEmergenciasActivasList() {
        return new ArrayList<>(emergenciasActivas.values());
    }
    
    public Map<Prioridad, Integer> getEstadisticasPorPrioridad() {
        Map<Prioridad, Integer> stats = new HashMap<>();
        for (Map.Entry<Prioridad, AtomicInteger> entry : estadisticasPorPrioridad.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().get());
        }
        return stats;
    }
    
    /**
     * Obtiene un resumen de estadísticas del sistema.
     * @return String con estadísticas formateadas
     */
    public String getEstadisticas() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════╗\n");
        sb.append("║     ESTADÍSTICAS DEL SISTEMA               ║\n");
        sb.append("╠════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Emergencias Atendidas:     %15d ║\n", emergenciasAtendidas.get()));
        sb.append(String.format("║ Emergencias Activas:       %15d ║\n", emergenciasActivas.size()));
        sb.append(String.format("║ En Cola de Espera:         %15d ║\n", colaEmergencias.size()));
        sb.append(String.format("║ Canceladas:                %15d ║\n", emergenciasCanceladas.get()));
        sb.append("╠════════════════════════════════════════════╣\n");
        sb.append("║ Por Prioridad:                             ║\n");
        for (Prioridad p : Prioridad.values()) {
            sb.append(String.format("║   %-10s:               %15d ║\n", 
                p.name(), estadisticasPorPrioridad.get(p).get()));
        }
        sb.append("╚════════════════════════════════════════════╝\n");
        return sb.toString();
    }
}
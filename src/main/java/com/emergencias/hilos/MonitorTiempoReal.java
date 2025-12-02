package com.emergencias.hilos;

import com.emergencias.gestores.*;
import com.emergencias.modelos.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Hilo que monitorea y muestra el estado del sistema en tiempo real.
 * Actualiza periódicamente las estadísticas y el estado de recursos.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public class MonitorTiempoReal implements Runnable {
    private final GestorEmergencias gestorEmergencias;
    private final GestorRecursos gestorRecursos;
    private volatile boolean activo;
    private final int intervaloSegundos;
    
    public MonitorTiempoReal(int intervaloSegundos) {
        this.gestorEmergencias = GestorEmergencias.getInstancia();
        this.gestorRecursos = GestorRecursos.getInstancia();
        this.activo = true;
        this.intervaloSegundos = intervaloSegundos;
    }
    
    @Override
    public void run() {
        System.out.println("Monitor de tiempo real iniciado (actualizacion cada " +
                         intervaloSegundos + "s)");
        
        try {
            while (activo && !Thread.currentThread().isInterrupted()) {
                mostrarEstadoSistema();
                TimeUnit.SECONDS.sleep(intervaloSegundos);
            }
        } catch (InterruptedException e) {
            System.out.println("Monitor interrumpido");
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("Monitor finalizado");
        }
    }
    
    /**
     * Muestra un panel completo del estado del sistema.
     */
    private void mostrarEstadoSistema() {
        StringBuilder display = new StringBuilder();
        
        display.append("\n");
        display.append("╔══════════════════════════════════════════════════════════════════════════╗\n");
        display.append("║               SISTEMA DE GESTIÓN DE EMERGENCIAS MÉDICAS                 ║\n");
        display.append("║                    Estado en Tiempo Real                                 ║\n");
        display.append("╠══════════════════════════════════════════════════════════════════════════╣\n");
        
        // Sección de Emergencias
        display.append("║ EMERGENCIAS:                                                             ║\n");
        display.append(String.format("║   - En Cola (Pendientes):        %-40d ║\n",
            gestorEmergencias.getEmergenciasPendientes()));
        display.append(String.format("║   - En Proceso (Activas):        %-40d ║\n",
            gestorEmergencias.getEmergenciasActivas()));
        display.append(String.format("║   - Completadas:                 %-40d ║\n",
            gestorEmergencias.getEmergenciasAtendidas()));
        display.append(String.format("║   - Canceladas:                  %-40d ║\n",
            gestorEmergencias.getEmergenciasCanceladas()));
        
        display.append("╠══════════════════════════════════════════════════════════════════════════╣\n");
        
        // Sección de Recursos
        display.append("║ RECURSOS DISPONIBLES:                                                    ║\n");
        display.append(String.format("║   Ambulancias:  %d / %d disponibles                                   ║\n",
            gestorRecursos.getAmbulanciasDisponibles(),
            gestorRecursos.getAmbulanciasTotales()));
        display.append(String.format("║   Medicos:      %d / %d disponibles                                   ║\n",
            gestorRecursos.getMedicosDisponibles(),
            gestorRecursos.getMedicosTotales()));
        
        display.append("╠══════════════════════════════════════════════════════════════════════════╣\n");
        
        // Cola de emergencias pendientes (top 5)
        List<Emergencia> cola = gestorEmergencias.getColaEmergencias();
        if (!cola.isEmpty()) {
            display.append("║ PRÓXIMAS EMERGENCIAS (Top 5 por Prioridad):                              ║\n");
            int count = 0;
            for (Emergencia e : cola) {
                if (count >= 5) break;
                display.append(String.format("║   %d. EMG-%03d [%-8s] %-44s ║\n", 
                    ++count, e.getId(), e.getPrioridad(), e.getUbicacion()));
            }
            if (cola.size() > 5) {
                display.append(String.format("║   ... y %d más en cola                                                    ║\n", 
                    cola.size() - 5));
            }
        } else {
            display.append("║ PRÓXIMAS EMERGENCIAS: [Sin emergencias pendientes]                      ║\n");
        }
        
        display.append("╠══════════════════════════════════════════════════════════════════════════╣\n");
        
        // Ambulancias en servicio
        List<Ambulancia> ambulanciasOcupadas = gestorRecursos.getAmbulancias().stream()
            .filter(a -> !a.isDisponible())
            .limit(5)
            .toList();
        
        if (!ambulanciasOcupadas.isEmpty()) {
            display.append("║ AMBULANCIAS EN SERVICIO:                                                 ║\n");
            for (Ambulancia amb : ambulanciasOcupadas) {
                Emergencia emg = amb.getEmergenciaActual();
                if (emg != null) {
                display.append(String.format("║   - %-10s -> EMG-%03d [%-8s] %-32s ║\n",
                    amb.getId(), emg.getId(), emg.getPrioridad(), emg.getUbicacion()));
                }
            }
        }
        
        display.append("╠══════════════════════════════════════════════════════════════════════════╣\n");
        
        // Estadísticas por prioridad
        display.append("║ ESTADÍSTICAS POR PRIORIDAD:                                              ║\n");
        var stats = gestorEmergencias.getEstadisticasPorPrioridad();
        display.append(String.format("║   🔴 CRÍTICO:    %-56d ║\n", stats.getOrDefault(Prioridad.CRITICO, 0)));
        display.append(String.format("║   🟠 GRAVE:      %-56d ║\n", stats.getOrDefault(Prioridad.GRAVE, 0)));
        display.append(String.format("║   🟡 MODERADO:   %-56d ║\n", stats.getOrDefault(Prioridad.MODERADO, 0)));
        display.append(String.format("║   🟢 LEVE:       %-56d ║\n", stats.getOrDefault(Prioridad.LEVE, 0)));
        
        display.append("╚══════════════════════════════════════════════════════════════════════════╝\n");
        
        System.out.print(display.toString());
    }
    
    /**
     * Muestra un resumen final del sistema.
     */
    public void mostrarResumenFinal() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RESUMEN FINAL DEL SISTEMA");
        System.out.println("=".repeat(80));
        System.out.println(gestorEmergencias.getEstadisticas());
        System.out.println("\nRECURSOS FINALES:");
        System.out.println("  Ambulancias disponibles: " + gestorRecursos.getAmbulanciasDisponibles() + 
                         " / " + gestorRecursos.getAmbulanciasTotales());
        System.out.println("  Médicos disponibles: " + gestorRecursos.getMedicosDisponibles() + 
                         " / " + gestorRecursos.getMedicosTotales());
        System.out.println("=".repeat(80));
    }
    
    public void detener() {
        activo = false;
    }
}
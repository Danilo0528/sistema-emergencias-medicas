package com.emergencias;

import com.emergencias.hilos.*;
import com.emergencias.gestores.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Clase principal del Sistema de Gestión de Emergencias Médicas.
 * Coordina todos los componentes concurrentes del sistema.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public class Main {
    private static final int NUM_OPERADORES = 3;
    private static final int NUM_DESPACHADORES = 4;
    private static final int DURACION_SIMULACION_SEGUNDOS = 60; // 1 minuto
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                           ║");
        System.out.println("║        SISTEMA DE GESTIÓN DE EMERGENCIAS MÉDICAS                         ║");
        System.out.println("║        Implementación con Concurrencia y Multihilo                       ║");
        System.out.println("║                                                                           ║");
        System.out.println("║        IUDigital de Antioquia                                            ║");
        System.out.println("║                                                                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        Main sistema = new Main();
        sistema.iniciarSistema();
    }
    
    /**
     * Inicia y coordina todos los componentes del sistema.
     */
    public void iniciarSistema() {
        // Inicializar gestores (Singleton)
        GestorEmergencias gestorEmergencias = GestorEmergencias.getInstancia();
        GestorRecursos gestorRecursos = GestorRecursos.getInstancia();
        
        System.out.println("\n🚀 Iniciando Sistema de Emergencias Médicas...\n");
        
        // ExecutorService para manejo eficiente de hilos
        ExecutorService executorOperadores = Executors.newFixedThreadPool(NUM_OPERADORES);
        ExecutorService executorDespachadores = Executors.newFixedThreadPool(NUM_DESPACHADORES);
        ExecutorService executorMonitor = Executors.newSingleThreadExecutor();
        
        // Crear operadores de llamadas
        List<OperadorLlamadas> operadores = new ArrayList<>();
        for (int i = 1; i <= NUM_OPERADORES; i++) {
            OperadorLlamadas operador = new OperadorLlamadas("Operador-" + i);
            operadores.add(operador);
            executorOperadores.submit(operador);
        }
        
        // Crear despachadores
        List<Despachador> despachadores = new ArrayList<>();
        for (int i = 1; i <= NUM_DESPACHADORES; i++) {
            Despachador despachador = new Despachador("Despachador-" + i);
            despachadores.add(despachador);
            executorDespachadores.submit(despachador);
        }
        
        // Crear monitor en tiempo real
        MonitorTiempoReal monitor = new MonitorTiempoReal(10); // Actualiza cada 10 segundos
        executorMonitor.submit(monitor);
        
        System.out.println("✓ Sistema completamente inicializado\n");
        System.out.println("  • " + NUM_OPERADORES + " operadores activos");
        System.out.println("  • " + NUM_DESPACHADORES + " despachadores activos");
        System.out.println("  • Monitor en tiempo real activo");
        System.out.println("  • " + gestorRecursos.getAmbulanciasTotales() + " ambulancias disponibles");
        System.out.println("  • " + gestorRecursos.getMedicosTotales() + " médicos disponibles\n");
        
        // Modo de ejecución
        ejecutarModoAutomatico(operadores, despachadores, monitor, 
                               executorOperadores, executorDespachadores, executorMonitor);
    }
    
    /**
     * Ejecuta el sistema en modo automático por tiempo definido.
     */
    private void ejecutarModoAutomatico(List<OperadorLlamadas> operadores,
                                       List<Despachador> despachadores,
                                       MonitorTiempoReal monitor,
                                       ExecutorService execOperadores,
                                       ExecutorService execDespachadores,
                                       ExecutorService execMonitor) {
        
        System.out.println("🔄 Sistema ejecutándose en modo AUTOMÁTICO");
        System.out.println("⏱  Duración: " + DURACION_SIMULACION_SEGUNDOS + " segundos");
        System.out.println("⏸  Presiona Ctrl+C para detener anticipadamente\n");
        System.out.println("=".repeat(80) + "\n");
        
        // Configurar shutdown hook para detención ordenada
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\n🛑 Señal de detención recibida...");
            detenerSistema(operadores, despachadores, monitor, 
                          execOperadores, execDespachadores, execMonitor);
        }));
        
        try {
            // Ejecutar por tiempo definido
            TimeUnit.SECONDS.sleep(DURACION_SIMULACION_SEGUNDOS);
            
            System.out.println("\n\n⏰ Tiempo de simulación completado");
            detenerSistema(operadores, despachadores, monitor, 
                          execOperadores, execDespachadores, execMonitor);
            
        } catch (InterruptedException e) {
            System.out.println("\n⚠ Simulación interrumpida");
            detenerSistema(operadores, despachadores, monitor, 
                          execOperadores, execDespachadores, execMonitor);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Detiene todos los componentes del sistema de forma ordenada.
     */
    private void detenerSistema(List<OperadorLlamadas> operadores,
                               List<Despachador> despachadores,
                               MonitorTiempoReal monitor,
                               ExecutorService execOperadores,
                               ExecutorService execDespachadores,
                               ExecutorService execMonitor) {
        
        System.out.println("\n🔄 Iniciando apagado ordenado del sistema...\n");
        
        // Paso 1: Detener operadores (no más llamadas nuevas)
        System.out.println("1️⃣ Deteniendo operadores...");
        for (OperadorLlamadas op : operadores) {
            op.detener();
        }
        execOperadores.shutdown();
        
        try {
            if (!execOperadores.awaitTermination(5, TimeUnit.SECONDS)) {
                execOperadores.shutdownNow();
            }
        } catch (InterruptedException e) {
            execOperadores.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Paso 2: Dar tiempo a despachadores para procesar cola restante
        System.out.println("2️⃣ Procesando emergencias restantes...");
        try {
            TimeUnit.SECONDS.sleep(10); // Tiempo para procesar cola
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Paso 3: Detener despachadores
        System.out.println("3️⃣ Deteniendo despachadores...");
        for (Despachador desp : despachadores) {
            desp.detener();
        }
        execDespachadores.shutdown();
        
        try {
            if (!execDespachadores.awaitTermination(5, TimeUnit.SECONDS)) {
                execDespachadores.shutdownNow();
            }
        } catch (InterruptedException e) {
            execDespachadores.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Paso 4: Detener monitor
        System.out.println("4️⃣ Deteniendo monitor...");
        monitor.detener();
        execMonitor.shutdown();
        
        try {
            if (!execMonitor.awaitTermination(2, TimeUnit.SECONDS)) {
                execMonitor.shutdownNow();
            }
        } catch (InterruptedException e) {
            execMonitor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Mostrar resumen final
        System.out.println("\n✅ Sistema detenido correctamente\n");
        monitor.mostrarResumenFinal();
        
        System.out.println("\n📊 RESUMEN DE ACTIVIDAD POR COMPONENTE:");
        System.out.println("\nOperadores:");
        for (OperadorLlamadas op : operadores) {
            System.out.println("  • " + op.getId() + ": " + op.getLlamadasAtendidas() + " llamadas procesadas");
        }
        
        System.out.println("\nDespachadores:");
        for (Despachador desp : despachadores) {
            System.out.println("  • " + desp.getId() + ": " + desp.getEmergenciasDespachas() + " emergencias despachadas");
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Gracias por usar el Sistema de Gestión de Emergencias Médicas");
        System.out.println("IUDigital de Antioquia - " + java.time.LocalDate.now().getYear());
        System.out.println("=".repeat(80) + "\n");
    }
}
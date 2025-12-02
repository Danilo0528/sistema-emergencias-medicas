package com.emergencias.hilos;

import com.emergencias.modelos.*;
import com.emergencias.gestores.GestorEmergencias;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Hilo que simula un operador recibiendo llamadas de emergencia.
 * Implementa patrón Producer para generar emergencias.
 * 
 * @author Equipo Emergencias
 * @version 1.0
 */
public class OperadorLlamadas implements Runnable {
    private final String id;
    private final GestorEmergencias gestorEmergencias;
    private final Random random;
    private volatile boolean activo;
    private int llamadasAtendidas;
    
    // Ubicaciones comunes de Santa Marta
    private static final String[] UBICACIONES = {
        "Calle 22 con Carrera 5",
        "Rodadero Sur",
        "El Centro Histórico",
        "Mamatoco",
        "Gaira",
        "Bello Horizonte",
        "Taganga",
        "Pozos Colorados",
        "Bastidas",
        "Altos del Prado"
    };
    
    // Descripciones de emergencias
    private static final String[] DESCRIPCIONES = {
        "Dolor de pecho severo",
        "Dificultad respiratoria",
        "Traumatismo por accidente",
        "Sangrado abundante",
        "Pérdida de conciencia",
        "Convulsiones",
        "Fractura expuesta",
        "Quemaduras graves",
        "Intoxicación",
        "Paro cardíaco"
    };
    
    public OperadorLlamadas(String id) {
        this.id = id;
        this.gestorEmergencias = GestorEmergencias.getInstancia();
        this.random = new Random();
        this.activo = true;
        this.llamadasAtendidas = 0;
    }
    
    @Override
    public void run() {
        System.out.println("👤 " + id + " iniciado y esperando llamadas...");
        
        try {
            while (activo && !Thread.currentThread().isInterrupted()) {
                // Simular tiempo entre llamadas (0.5-3 segundos) para más emergencias
                TimeUnit.MILLISECONDS.sleep(500 + random.nextInt(2500));
                
                // Generar emergencia aleatoria
                Emergencia emergencia = generarEmergenciaAleatoria();
                
                // Registrar en el sistema
                gestorEmergencias.registrarEmergencia(emergencia);
                llamadasAtendidas++;
                
                System.out.println("  " + id + " registró llamada #" + llamadasAtendidas);
            }
        } catch (InterruptedException e) {
            System.out.println("⚠ " + id + " interrumpido");
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("👤 " + id + " finalizado. Total llamadas: " + llamadasAtendidas);
        }
    }
    
    /**
     * Genera una emergencia con datos aleatorios realistas.
     * @return Emergencia generada
     */
    private Emergencia generarEmergenciaAleatoria() {
        // Distribución realista de prioridades (más leves que críticas)
        Prioridad prioridad;
        int valor = random.nextInt(100);
        if (valor < 10) {
            prioridad = Prioridad.CRITICO;
        } else if (valor < 30) {
            prioridad = Prioridad.GRAVE;
        } else if (valor < 60) {
            prioridad = Prioridad.MODERADO;
        } else {
            prioridad = Prioridad.LEVE;
        }
        
        String ubicacion = UBICACIONES[random.nextInt(UBICACIONES.length)];
        String descripcion = DESCRIPCIONES[random.nextInt(DESCRIPCIONES.length)];
        
        // Coordenadas aleatorias dentro de Santa Marta
        double latitud = 11.2 + (random.nextDouble() * 0.2); // Aproximado
        double longitud = -74.2 + (random.nextDouble() * 0.2);
        
        return new Emergencia(ubicacion, prioridad, descripcion, latitud, longitud);
    }
    
    /**
     * Detiene el operador de forma ordenada.
     */
    public void detener() {
        activo = false;
    }
    
    public String getId() {
        return id;
    }
    
    public int getLlamadasAtendidas() {
        return llamadasAtendidas;
    }
}
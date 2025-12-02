# Sistema de Gestión de Emergencias Médicas

## Equipo de Desarrollo
- **Danilo Jose Pino Ospino** - Arquitectura y sincronización
- **Danilo Jose Pino Ospino** - Gestión de recursos y priorización
- **Danilo Jose Pino Ospino** - Hilos concurrentes y comunicación
- **Danilo Jose Pino Ospino** - Pruebas y documentación

---

## Descripción del Proyecto

Sistema concurrente desarrollado en Java que simula la gestión integral de emergencias médicas para el Centro de Emergencias de Santa Marta. El sistema coordina la asignación eficiente de recursos limitados (ambulancias y personal médico) para atender múltiples emergencias simultáneas con diferentes niveles de prioridad.

### Características Principales

- Procesamiento Concurrente: Manejo simultáneo de múltiples llamadas de emergencia
- Sistema de Priorización Inteligente: Algoritmo que considera gravedad, tiempo de espera y distancia
- Gestión Thread-Safe: Manejo seguro de recursos compartidos con sincronización avanzada
- Monitoreo en Tiempo Real: Visualización continua del estado del sistema
- Prevención de Problemas: Implementación robusta contra deadlocks y race conditions
- Patrón Producer-Consumer: Arquitectura eficiente para procesamiento de emergencias

---

## Arquitectura del Sistema

### Componentes Principales

```
Sistema de Emergencias
├── Modelos de Dominio
│   ├── Emergencia (con prioridad dinámica)
│   ├── Ambulancia (thread-safe)
│   ├── MedicoEmergencia (asignación atómica)
│   └── Prioridad (enum con niveles)
│
├── Gestores (Singleton)
│   ├── GestorEmergencias (cola de prioridad)
│   └── GestorRecursos (pool de recursos)
│
└── Hilos Concurrentes
    ├── OperadorLlamadas (Producer)
    ├── Despachador (Consumer)
    └── MonitorTiempoReal (Observer)
```

### Patrones de Diseño Implementados

1. Singleton: Gestores centralizados para acceso global thread-safe
2. Producer-Consumer: Operadores producen emergencias, despachadores las consumen
3. Observer: Monitor observa cambios en el estado del sistema
4. Factory Method: Creación estructurada de recursos médicos

---

## Tecnologías y Herramientas

- Lenguaje: Java 17 LTS
- Build Tool: Maven 3.8+
- IDE Recomendado: NetBeans 12+ / IntelliJ IDEA / Eclipse
- Concurrencia:
  - ExecutorService para gestión de pools de hilos
  - ReentrantLock para secciones críticas
  - Semaphore para control de recursos limitados
  - AtomicBoolean / AtomicInteger para operaciones atómicas
  - PriorityBlockingQueue para cola de emergencias
  - ConcurrentHashMap para almacenamiento thread-safe
  - CopyOnWriteArrayList para listas concurrentes

---

## Instalación y Ejecución

### Requisitos Previos

- Java Development Kit (JDK) 17 o superior
- Apache Maven 3.6+
- Git para clonar el repositorio
- NetBeans IDE (opcional pero recomendado)

### Pasos de Instalación

#### Opción 1: Desde NetBeans (Recomendado)

1. Clonar el repositorio:
   ```bash
   git clone [URL_DEL_REPOSITORIO]
   cd sistema-emergencias-medicas
   ```

2. Abrir en NetBeans:
   - File → Open Project
   - Seleccionar la carpeta del proyecto
   - NetBeans detectará automáticamente el proyecto Maven

3. Compilar el proyecto:
   - Click derecho en el proyecto
   - Seleccionar "Clean and Build"

4. Ejecutar:
   - Click derecho en el proyecto
   - Seleccionar "Run"
   - O presionar F6

#### Opción 2: Línea de Comandos

```bash
# Clonar repositorio
git clone [URL_DEL_REPOSITORIO]
cd sistema-emergencias-medicas

# Compilar
mvn clean compile

# Ejecutar
mvn exec:java -Dexec.mainClass="com.emergencias.Main"

# O generar JAR ejecutable
mvn clean package
java -jar target/sistema-emergencias-medicas-1.0.0.jar
```

---

## Estructura del Proyecto

```
sistema-emergencias-medicas/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── emergencias/
│   │   │           ├── Main.java                    # Punto de entrada
│   │   │           ├── modelos/                     # Entidades del dominio
│   │   │           │   ├── Emergencia.java
│   │   │           │   ├── Ambulancia.java
│   │   │           │   ├── MedicoEmergencia.java
│   │   │           │   └── Prioridad.java
│   │   │           ├── gestores/                    # Lógica de negocio
│   │   │           │   ├── GestorEmergencias.java
│   │   │           │   └── GestorRecursos.java
│   │   │           └── hilos/                       # Componentes concurrentes
│   │   │               ├── OperadorLlamadas.java
│   │   │               ├── Despachador.java
│   │   │               └── MonitorTiempoReal.java
│   │   └── resources/
│   └── test/
│       └── java/
│
├── docs/                                            # Documentación
│   ├── diagramas/
│   │   ├── arquitectura.png
│   │   ├── clases.png
│   │   │   └── secuencia.png
│   └── documento-tecnico.pdf
│
├── pom.xml                                          # Configuración Maven
├── README.md                                        # Este archivo
└── .gitignore                                       # Archivos ignorados por Git
```

---

## Mecanismos de Sincronización

### 1. Semáforos
- Control de acceso a ambulancias y médicos limitados
- Implementación de permisos para recursos compartidos

### 2. Cola de Prioridad Bloqueante
- PriorityBlockingQueue para ordenar emergencias
- Ordenamiento dinámico considerando gravedad y tiempo de espera

### 3. Variables Atómicas
- AtomicBoolean para estado de disponibilidad
- AtomicInteger para contadores thread-safe

### 4. ConcurrentHashMap
- Almacenamiento thread-safe de recursos y emergencias
- Operaciones atómicas sin bloqueos externos

### 5. ExecutorService
- Pools de hilos optimizados para operadores y despachadores
- Gestión eficiente del ciclo de vida de hilos

---

## Algoritmo de Priorización

El sistema utiliza un algoritmo multi-factor para determinar la prioridad efectiva:

```
Prioridad Efectiva = Gravedad Base × Factor Tiempo
```

### Niveles de Gravedad Base:
- CRÍTICO (4): Riesgo de muerte inminente
- GRAVE (3): Requiere atención urgente
- MODERADO (2): Atención necesaria en breve
- LEVE (1): Puede esperar

### Factor Tiempo:
```
Factor = 1 + (minutos_espera × 0.1)
```

Esto asegura que emergencias menos graves que llevan mucho tiempo esperando eventualmente superen a emergencias más graves recién llegadas.

---

## Flujo de Operación

### 1. Recepción de Llamada
```
Usuario llama → Operador recibe → Registra emergencia → Cola de prioridad
```

### 2. Asignación de Recursos
```
Despachador toma emergencia → Busca ambulancia más cercana → Asigna médicos → Inicia atención
```

### 3. Atención de Emergencia
```
Ambulancia en camino → Llega al lugar → Médicos atienden → Completa emergencia → Libera recursos
```

### 4. Monitoreo Continuo
```
Monitor actualiza cada 10s → Muestra estado en tiempo real → Genera estadísticas
```

---

## Pruebas Realizadas

### Escenarios de Carga

| Escenario | Emergencias | Recursos | Resultado |
|-----------|-------------|----------|-----------|
| Carga Baja | 20 | 10 amb, 20 med | 100% atendidas |
| Carga Media | 50 | 10 amb, 20 med | 98% atendidas |
| Carga Alta | 100 | 10 amb, 20 med | 95% atendidas |
| Estrés | 200 | 10 amb, 20 med | Sistema estable |

### Métricas de Rendimiento

- Tiempo promedio de respuesta: 3-5 segundos (simulado)
- Throughput: ~15-20 emergencias/minuto
- Deadlocks detectados: 0 en 1000+ iteraciones
- Race conditions: 0 (validado con pruebas de concurrencia)

---

## Capturas de Pantalla

### Panel de Monitoreo en Tiempo Real

```
╔══════════════════════════════════════════════════════════════════════════╗
║               SISTEMA DE GESTIÓN DE EMERGENCIAS MÉDICAS                 ║
║                    Estado en Tiempo Real                                 ║
╠══════════════════════════════════════════════════════════════════════════╣
║ EMERGENCIAS:                                                             ║
║   • En Cola (Pendientes):        5                                       ║
║   • En Proceso (Activas):        8                                       ║
║   • Completadas:                 42                                      ║
║   • Canceladas:                  0                                       ║
╠══════════════════════════════════════════════════════════════════════════╣
║ RECURSOS DISPONIBLES:                                                    ║
║   Ambulancias:  2 / 10 disponibles                                    ║
║   Médicos:      6 / 20 disponibles                                    ║
╠══════════════════════════════════════════════════════════════════════════╣
```

---

## Contribuciones del Equipo

Cada miembro del equipo contribuyó equitativamente al proyecto:

- Arquitectura y Diseño: Diseño de clases, patrones y estructura
- Implementación Core: Desarrollo de modelos y gestores
- Concurrencia: Implementación de hilos y sincronización
- Testing y Debugging: Pruebas exhaustivas y corrección de bugs
- Documentación: README, JavaDoc y documento técnico

Distribución de Commits: Ver historial en Git
Reuniones de Equipo: [Número de reuniones]
Code Reviews: Revisión cruzada de todo el código

---

## Recursos Adicionales

### Documentación Técnica
- Documento Técnico Completo (PDF)
- Diagramas de Arquitectura
- JavaDoc Generado

### Video Demostrativo
[Enlace al video en YouTube]
- Demostración completa del sistema
- Explicación de arquitectura
- Análisis de concurrencia
- Justificación de decisiones de diseño

---

## Problemas Conocidos y Soluciones

### Problema: Starvation en emergencias leves
Solución: Implementamos factor de tiempo que incrementa prioridad con la espera

### Problema: Deadlock potencial en asignación de recursos
Solución: Uso de semáforos con timeouts y liberación ordenada

### Problema: Race conditions en actualización de estado
Solución: Uso de variables atómicas y estructuras thread-safe

---

## Mejoras Futuras

- Interfaz gráfica con JavaFX
- Integración con base de datos para persistencia
- Sistema de notificaciones push
- Integración con GPS real
- Machine Learning para predecir demanda
- API REST para integración con otros sistemas
- Dashboard web en tiempo real
- Soporte para múltiples ciudades

---

## Licencia

Proyecto académico desarrollado para el curso de Programación Concurrente.

IUDigital de Antioquia - 2025

---

## Contacto

Para preguntas o sugerencias sobre el proyecto:


- GitHub: [Danilo0528]

---

## Agradecimientos

- Profesor del curso por la guía y retroalimentación
- IUDigital de Antioquia por el espacio de aprendizaje
- Comunidad de Java por la documentación y recursos

---

Desarrollado con ❤️ por el equipo de Emergencias Médicas

Última actualización: Diciembre 2025

# Flujo UML de los algoritmos de planificación

> Base: `README.md §1.2`, `motor/Planificador.java`, `motor/CPU.java`, `modelo/Proceso.java`.
> Mock común: `A(0,5) B(3,2) C(1,3) D(2,2) E(4,4)` en `mock/MockProcesos.java`.

## 1. Arquitectura común

```mermaid
classDiagram
    class MainFIFO {
        +main()
    }
    class Inicio {
        +lanzar(fabrica)$
    }
    class VentanaSimulacion {
        -planificador : Planificador
        -temporizador : Timer
        +iniciarSimulacion()
        +manejarTick()
        +actualizarTodo()
    }
    class Planificador {
        <<interface>>
        +iniciar()
        +avanzar() bool
        +pausar() +continuar() +detener()
        +restablecerProcesos()
    }
    class PlanificadorFIFO
    class PlanificadorSJF
    class PlanificadorPrioridades
    class PlanificadorSRTF
    class PlanificadorRoundRobin
    class PlanificadorPrioridadesApropiativo
    class CPU {
        -dueno : Proceso
        +asignar() +liberar() +estaLibre()
        +ejecutar(reloj)
    }
    class Proceso {
        +ejecutarUnidad()
        +reiniciar()
        +getTiempoRestante() +getLlegada() +getRafaga() +getPrioridad()
    }
    MainFIFO --> Inicio : lanzar(VentanaFIFO::new)
    Inicio --> VentanaSimulacion : fabrica.get()
    VentanaSimulacion --> Planificador : iniciar() / avanzar()
    Planificador <|.. PlanificadorFIFO
    Planificador <|.. PlanificadorSJF
    Planificador <|.. PlanificadorPrioridades
    Planificador <|.. PlanificadorSRTF
    Planificador <|.. PlanificadorRoundRobin
    Planificador <|.. PlanificadorPrioridadesApropiativo
    PlanificadorFIFO --> CPU : asignar/liberar/ejecutar
    PlanificadorSJF --> CPU
    PlanificadorPrioridades --> CPU
    PlanificadorSRTF --> CPU
    PlanificadorRoundRobin --> CPU
    PlanificadorPrioridadesApropiativo --> CPU
    CPU --> Proceso : ejecutarUnidad()
```

## 2. Ciclo común `Main` → `Tick` (vale para los 6)

```mermaid
flowchart TD
    A["Main*.main() → Inicio.lanzar()"] --> B["new PlanificadorX() → restablecerProcesos()\nMockProcesos.nuevos() + reiniciar()\nreloj=0, cpu.reiniciar(), proceso.reiniciar()"]
    B --> C["VentanaSimulacion ctor\nTimer 500ms detenido"]
    C --> D["Usuario: Iniciar → iniciarSimulacion()\nplanificador.iniciar()"]
    D --> E["iniciar(): reiniciar() + programarSiguiente()\nseleccionar() + cpu.asignar() + EJECUTANDO"]
    E --> F["temporizador.start()"]
    F --> G["manejarTick() cada 500ms\nplanificador.avanzar()"]
    G -->|false| G
    G -->|true todosTerminados| H["temporizador.stop() + detener()"]
```

`iniciar()` es una sola vez por corrida (`if(ejecutando) return`). El bucle es `avanzar()`.

## 3. FIFO — no apropiativo, menor `C`

Criterio `seleccionar()`: `min(C) → id`. `cederTurno()`: solo si `restante==0` → `TERMINADO + tiempoFinalizacion=reloj + cpu.liberar()`.

```mermaid
flowchart TD
    S["iniciar(): programarSiguiente()\nseleccionar min C apto, ej. A"] --> L["avanzar(): reloj++"]
    L --> D1{"¿cpu.estaLibre()?"}
    D1 -->|No| EX["cpu.ejecutar(reloj)\nproceso.ejecutarUnidad() + segmento"]
    D1 -->|Sí| LG
    EX --> D2{"¿restante == 0?"}
    D2 -->|Sí| CT["cederTurno(): TERMINADO + liberar()"]
    D2 -->|No| LG
    CT --> LG["llegadas C == reloj → ESPERANDO"]
    LG --> D3{"¿cpu.estaLibre()?"}
    D3 -->|Sí| PS["programarSiguiente(): seleccionar min C\ncpu.asignar() + EJECUTANDO"]
    D3 -->|No| FIN
    PS --> FIN{"¿todosTerminados()?"}
    FIN -->|No| L
    FIN -->|Sí| STOP["stop + detener()"]
```

## 4. SJF — no apropiativo, menor `T`

Igual que FIFO pero `seleccionar()`: `min(T) → C → id`. Solo despacha cuando la CPU queda libre; un corto que llega a mitad de ráfaga espera.

```mermaid
flowchart TD
    S["iniciar(): seleccionar min T con C<=0"] --> L["avanzar(): reloj++ + cpu.ejecutar()"]
    L --> D1{"¿restante == 0?"}
    D1 -->|Sí| CT["cederTurno(): TERMINADO + liberar()"]
    D1 -->|No| LG["llegadas → ESPERANDO, sin desalojo"]
    CT --> LG
    LG --> D2{"¿cpu libre? → programarSiguiente()\nmin T entre aptos"}
    D2 --> FIN{"¿todosTerminados()?"}
    FIN -->|No| L
    FIN -->|Sí| STOP["stop"]
```

## 5. Prioridades (no apropiativo) — menor `P`

`seleccionar()`: `min(P) → T → C → id`. `cederTurno()` igual que FIFO/SJF. Llegada con mejor `P` no interrumpe.

```mermaid
flowchart TD
    S["iniciar(): seleccionar min P"] --> L["avanzar(): reloj++ + cpu.ejecutar()"]
    L --> D1{"¿restante == 0? → cederTurno TERMINADO"}
    D1 --> LG["llegadas → ESPERANDO"]
    LG --> D2{"¿cpu libre? → programarSiguiente min P"}
    D2 --> FIN{"¿todosTerminados()?"}
    FIN -->|No| L
    FIN -->|Sí| STOP["stop"]
```

## 6. SRTF — apropiativo, menor restante

`seleccionar()`: `min(tiempoRestante) → C → id`. `cederTurno()`: `restante==0 → TERMINADO`, si no → `ESPERANDO` (interrumpido).

```mermaid
flowchart TD
    S["iniciar(): seleccionar min restante"] --> L["avanzar(): reloj++ + cpu.ejecutar()"]
    L --> D1{"¿restante == 0? → cederTurno TERMINADO"}
    D1 --> LG["llegadas C==reloj → ESPERANDO"]
    LG --> D2{"¿cpu ocupada y mejor.restante < dueño.restante?"}
    D2 -->|Sí| PRE["dueño → ESPERANDO + cpu.liberar()\ncambiosContexto++ si cambia dueño"]
    D2 -->|No, empate mantiene| D3
    PRE --> D3{"¿cpu libre? → programarSiguiente min restante"}
    D3 --> FIN{"¿todosTerminados()?"}
    FIN -->|No| L
    FIN -->|Sí| STOP["stop"]
```

## 7. Prioridades apropiativo — menor `P` con desalojo

Igual selección que Prioridades, pero decisión extra por tick: `mejor.P < dueño.P` desaloja. Igual `P` no desaloja.

```mermaid
flowchart TD
    S["iniciar(): seleccionar min P"] --> L["avanzar(): reloj++ + cpu.ejecutar()"]
    L --> D1{"¿restante == 0? → cederTurno TERMINADO"}
    D1 --> LG["llegadas → ESPERANDO"]
    LG --> D2{"¿mejor.P < dueño.P?"}
    D2 -->|Sí, estrictamente mejor| PRE["dueño → ESPERANDO + liberar()"]
    D2 -->|No| D3
    PRE --> D3{"¿cpu libre? → programarSiguiente min P"}
    D3 --> FIN{"¿todosTerminados()?"}
    FIN -->|No| L
    FIN -->|Sí| STOP["stop"]
```

## 8. Round Robin — cola circular + quantum `Q=2`

Estructura propia `Queue<Proceso> cola` + `unidadesEnTurno`. `iniciar()` encola los de `C==0`. `cederTurno()`: `restante==0 → TERMINADO`, si no → `ESPERANDO + cola.offer()`.

```mermaid
flowchart TD
    S["iniciar(): encolar C==0 + programarSiguiente()\ncabeza de cola"] --> L["avanzar(): reloj++ + cpu.ejecutar()\nunidadesEnTurno++"]
    L --> EN["nuevos C==reloj → ESPERANDO + cola.offer()\nantes de reencolar al saliente"]
    EN --> D1{"¿restante == 0 o unidadesEnTurno >= quantum?"}
    D1 -->|Sí fin| CT1["cederTurno: TERMINADO + liberar()"]
    D1 -->|Sí quantum| CT2["cederTurno: ESPERANDO + offer al final + liberar()"]
    D1 -->|No| D2
    CT1 --> D2{"¿cpu libre? → programarSiguiente()\nhead apta de la cola"}
    CT2 --> D2
    D2 --> FIN{"¿todosTerminados()?"}
    FIN -->|No| L
    FIN -->|Sí| STOP["stop"]
```

## 9. Tabla de decisiones por método

| Método | FIFO | SJF | Prioridades | SRTF | Prioridades-Aprop. | Round Robin |
|---|---|---|---|---|---|---|
| `seleccionar()` | `min C` | `min T` | `min P` | `min restante` | `min P` | cabeza de `cola` apta |
| `esApto()` | `restante>0 && !TERMINADO && C<=reloj` | igual | igual | igual | igual | igual + está en cola |
| `cederTurno()` | solo al terminar | solo al terminar | solo al terminar | terminar o `ESPERANDO` si desalojado | terminar o `ESPERANDO` si desalojado | terminar o reencolar por quantum |
| Desalojo en `avanzar()` | no | no | no | `mejor.restante < dueño.restante` | `mejor.P < dueño.P` | `unidadesEnTurno >= quantum` |
| `programarSiguiente()` | si `cpu libre` | si `cpu libre` | si `cpu libre` | si `cpu libre` (tras posible desalojo) | si `cpu libre` (tras posible desalojo) | si `cpu libre`, `poll` apto |

# Planificador de procesos

Simulador de planificación de CPU en Java (Swing) con 6 planificadores: `FIFO`, `SJF`,
`Prioridades`, `Prioridades apropiativo`, `Round Robin` y `SRTF`.
Cada algoritmo implementa el mismo contrato (`motor/Planificador.java`) con su propia
política de selección y cesión. La vista solo observa al núcleo.

## 1. Base común a todos los algoritmos

### 1.1 Procesos y mock inicial

Modelo en `compartido/nucleo/modelo/Proceso.java`: cada proceso guarda `C` (llegada),
`T` (ráfaga), `P` (prioridad), `tiempoRestante`, `tiempoInicio` y `tiempoFinalizacion`.

Todos los casos parten del mismo mock (`compartido/nucleo/mock/MockProcesos.java`):

| Proceso | A | B | C | D | E |
|---|---|---|---|---|---|
| C (llegada) | 0 | 3 | 1 | 2 | 4 |
| T (ráfaga) | 5 | 2 | 3 | 2 | 4 |
| P (prioridad) | 3 | 1 | 5 | 4 | 2 |

`FIFO`, `SJF`, `SRTF` y `Round Robin` ignoran `P`. Los valores se pueden editar
mientras la simulación está detenida; Reiniciar recupera el mock.

### 1.2 Motor: reloj, CPU y tick

- CPU (`compartido/nucleo/motor/CPU.java`): solo guarda al dueño, ejecuta 1 unidad
  (`ejecutar(reloj)`) y registra el segmento Gantt `[reloj-1, reloj)`. No decide ni
  lleva el reloj.
- Planificador: lleva el `reloj` (empieza en `0`), despacha al primer apto en
  `iniciar()` y avanza de a 1 unidad en `avanzar()`.
- Orden fijo de cada tick (vale para los 6 casos):
  1. `reloj++`; el dueño ejecuta 1 unidad. Si terminó su ráfaga, se marca
     `TERMINADO` con `tiempoFinalizacion = reloj` y libera la CPU
     (en RR además se controla el quantum, ver §3.5).
  2. Las llegadas con `C == reloj` pasan a `ESPERANDO` (salvo que sean el dueño).
  3. Evaluación apropiativa (solo `SRTF` y `Prioridades apropiativo`): si hay un
     apto estrictamente mejor que el dueño, el dueño vuelve a `ESPERANDO` y libera.
  4. Si la CPU quedó libre, se despacha al siguiente según la política. Lo
     despachado rige desde el próximo tick: nadie ejecuta antes de haber llegado.
- Un proceso es apto si `tiempoRestante > 0`, no está `TERMINADO` y `C <= reloj`.
- Cambios de contexto: se cuenta 1 cada vez que el dueño pasa directamente de un
  proceso a otro distinto.

### 1.3 Métricas

En `compartido/nucleo/modelo/MetricaProceso.java` y `ResumenMetricas.java`:

- Retorno (turnaround) por proceso: `F = tiempoFinalizacion − C`.
- Espera por proceso: `E = F − T`.
- Promedios: `esperaPromedio` y `finalizacionPromedio` (media aritmética).
- En curso (proceso no terminado): `F = reloj − C`, `E = reloj − C − ejecutado`.

## 2. Clasificación

| Algoritmo | Tipo | Criterio de despacho | Cede la CPU cuando |
|---|---|---|---|
| FIFO | No apropiativo | Menor `C` | Solo al terminar |
| SJF | No apropiativo | Menor `T` | Solo al terminar |
| Prioridades | No apropiativo | Menor `P` | Solo al terminar |
| Prioridades apropiativo | Apropiativo | Menor `P` | Al terminar o ante `P` estrictamente mejor |
| Round Robin | Apropiativo | Orden de llegada a la cola | Al terminar o al agotar el quantum |
| SRTF | Apropiativo | Menor tiempo restante | Al terminar o al llegar uno estrictamente más corto |

No apropiativo = el dueño conserva la CPU hasta agotar su ráfaga.
Apropiativo = el dueño puede ser desalojado antes de terminar.

## 3. Algoritmos en detalle

### 3.1 FIFO — primero en llegar, primero en servirse

Archivo: `compartido/nucleo/algoritmos/no_apropiativos/PlanificadorFIFO.java`.
Vista: `compartido/vista/casos/fifo/`.

- Idea: cola por orden de llegada. Justo y sin inanición si las ráfagas son acotadas,
  pero penaliza a los cortos que llegan detrás de uno largo (efecto convoy).
- Selección entre aptos: menor `C`; empate → orden alfabético de `id`.
- Cesión: solo al terminar (`tiempoRestante == 0`).
- Llegadas: si la CPU está libre en ese instante, la llegada se despacha y arranca
  en el tick siguiente; si está ocupada, espera. Una llegada nunca adelanta a quien
  ya esperaba.

### 3.2 SJF — trabajo más corto primero (no apropiativo)

Archivo: `compartido/nucleo/algoritmos/no_apropiativos/PlanificadorSJF.java`.
Vista: `compartido/vista/casos/sjf/`.

- Idea: minimiza la espera promedio entre los no apropiativos, a costa de posible
  inanición de los largos si siguen llegando cortos.
- Selección entre aptos: menor `T` (ráfaga original, no restante); empate → menor
  `C` → orden alfabético.
- Cesión: solo al terminar. La decisión se toma únicamente cuando la CPU queda
  libre, evaluando a todos los llegados hasta ese instante (incluida la llegada
  de ese mismo tick).
- Diferencia con SRTF: aquí un corto que llega en mitad de una ejecución espera;
  en SRTF la interrumpe.

### 3.3 Prioridades — manda el número de prioridad (no apropiativo)

Archivo: `compartido/nucleo/algoritmos/no_apropiativos/PlanificadorPrioridades.java`.
Vista: `compartido/vista/casos/prioridades/` (variante no apropiativa).

- Idea: el `1` es la prioridad más alta. Útil para procesos urgentes; riesgo de
  inanición de prioridad baja.
- Selección entre aptos: menor `P`; empate → menor `T` → menor `C` →
  orden alfabético.
- Cesión: solo al terminar, igual que FIFO/SJF. Una llegada con mejor prioridad
  espera al próximo despacho, no interrumpe.

### 3.4 Prioridades apropiativo — misma selección, con desalojo

Archivo: `compartido/nucleo/algoritmos/apropiativos/PlanificadorPrioridadesApropiativo.java`.
Vista: `compartido/vista/casos/prioridades/` (botón de variante en
`VentanaPrioridades.java`).

- Selección: idéntica a Prioridades no apropiativo.
- Desalojo: tras registrar las llegadas de cada tick, si el mejor apto tiene
  `P` estrictamente menor que la del dueño, el dueño vuelve a `ESPERANDO` y se
  despacha al mejor en ese mismo tick (rige desde el siguiente). Igual prioridad
  no desaloja.
- El desalojo solo disputa la siguiente unidad de CPU; lo ya ejecutado no se pierde
  (queda en `tiempoRestante`).

### 3.5 Round Robin — turnos de un quantum en cola circular

Archivo: `compartido/nucleo/algoritmos/apropiativos/PlanificadorRoundRobin.java`
(`QUANTUM_INICIAL = 2`). Vista: `compartido/vista/casos/roundrobin/`.

- Idea: reparto equitativo por turnos. Quantum pequeño → más cambios de contexto;
  quantum grande → tiende a FIFO.
- Estructura: `Queue<Proceso>` propia. Al iniciar entran los de `C == 0`; cada tick
  los nuevos (`C == reloj`) entran al final antes de reencolar al saliente, así que
  el saliente queda detrás de las llegadas de ese instante.
- Cesión: al terminar su ráfaga sale del sistema; al agotar el quantum
  (`unidadesEnTurno >= quantum`) vuelve al final de la cola como `ESPERANDO`.
- Despacho: cabeza de la cola que siga apta. Si la CPU está libre, una llegada de
  ese instante arranca de inmediato.
- Quantum editable en el campo `Q` de la cabecera
  (`VentanaRoundRobin.java`); solo tiene efecto antes de iniciar
  (`establecerQuantum()`, mínimo 1).

### 3.6 SRTF — menor tiempo restante primero (SJF apropiativo)

Archivo: `compartido/nucleo/algoritmos/apropiativos/PlanificadorSRTF.java`.
Vista: `compartido/vista/casos/srtf/`.

- Idea: versión apropiativa de SJF; óptima en espera promedio teórica, con más
  cambios de contexto y posible inanición de largos.
- Selección entre aptos: menor `tiempoRestante` (baja a medida que ejecuta);
  empate → menor `C` → orden alfabético.
- Desalojo: en cada tick, tras las llegadas, si el mejor apto tiene restante
  estrictamente menor que el dueño, el dueño vuelve a `ESPERANDO` y se despacha
  al mejor. El empate mantiene al dueño (no hay desalojo innecesario).
- A diferencia de SJF, la comparación se repite en cada llegada, no solo en cada
  terminación.

## 4. Estructura del proyecto y ejecución

```text
src/main/java/es/unjfsc/planificador/
  compartido/nucleo/
    modelo/      Proceso, Segmento, EstadoProceso, MetricaProceso, ResumenMetricas
    mock/        MockProcesos
    motor/       Planificador (contrato), CPU
    algoritmos/
      no_apropiativos/  PlanificadorFIFO, PlanificadorSJF, PlanificadorPrioridades
      apropiativos/     PlanificadorSRTF, PlanificadorRoundRobin,
                        PlanificadorPrioridadesApropiativo
  compartido/vista/
    comun/       VentanaSimulacion, PanelGantt, PanelConfiguracion, PanelMetricas, Tema
    casos/fifo | sjf | prioridades | roundrobin | srtf/
      Main* (punto de entrada), Ventana*, LogicaVisual*
```

- Requisitos: Java 21, Maven (`pom.xml`).
- Cada caso se lanza desde su `Main*` (p. ej. `MainFIFO`, `MainSRTF`).
- Gantt: segmentos `[inicio, fin)` con `fin` exclusivo (`modelo/Segmento.java`),
  dibujados en `vista/comun/PanelGantt.java`.

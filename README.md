# Planificador de procesos — cómo funciona cada caso

Simulador de planificación de un solo CPU con cinco algoritmos. Esta guía explica la
lógica de funcionamiento de cada caso, sin código: por cada punto se indica en qué
archivo ocurre esa lógica.

Mapa rápido: la lógica vive en `compartido.nucleo` (modelo + motor + un
subpaquete por algoritmo en `algoritmos/`) y todo lo visual vive en
`compartido.vista` (paneles comunes en `comun/` y una ventana por caso en
`casos/<algoritmo>/`). La vista solo observa al núcleo, nunca al revés.

## Motor común (vale para los cinco casos)

El corazón está en `compartido/nucleo/motor/PlanificadorBase.java`, con el contrato
en `compartido/nucleo/motor/Planificador.java`:

- El reloj empieza en 0. Al iniciar, entran los procesos con llegada 0 y se despacha
  al primero.
- Cada avance suma una unidad al reloj y ocurre siempre en este orden: el dueño del
  CPU ejecuta una unidad y queda registrada como segmento del Gantt; después entran
  las llegadas de ese instante; el proceso interrumpido (si lo hubo) vuelve al final
  de la cola; por último, si el CPU quedó libre, se despacha al siguiente.
- Regla de llegadas que cumplen todos: si el CPU está libre en el instante en que
  entra un proceso, ese proceso puede empezar de inmediato; si otro está usando el
  CPU, el recién llegado espera.
- Por defecto (no apropiativo) un proceso cede el CPU solo cuando agota su ráfaga.
- Los segmentos `[inicio, fin)` del diagrama de Gantt se registran en
  `compartido/nucleo/modelo/Segmento.java`, donde el instante `fin` es exclusivo.
- Cada proceso guarda llegada (C), ráfaga (T), lo que le resta, su espera (E) y sus
  instantes de inicio y fin (F), en `compartido/nucleo/modelo/Proceso.java`. La espera
  se cuenta desde el instante siguiente a la llegada, de modo que se cumple
  E = F − C − T. Los resúmenes y promedios se calculan en
  `compartido/nucleo/modelo/ResumenMetricas.java` (detalle por proceso en
  `compartido/nucleo/modelo/MetricaProceso.java`, estados en
  `compartido/nucleo/modelo/EstadoProceso.java`).

## Caso FIFO (el primero que llega es el primero que se atiende)

Lógica en `compartido/nucleo/algoritmos/fifo/PlanificadorFIFO.java`:

- Al quedar libre el CPU se elige al proceso que lleva más tiempo esperando (menor
  instante de llegada; a igual llegada, orden alfabético).
- Cada proceso conserva el CPU hasta terminar su ráfaga completa, sin interrupciones.
- Un proceso que llega mientras otro se ejecuta espera su turno; como el criterio es
  la llegada, nunca puede adelantar a quien ya esperaba. Si llega justo cuando el
  CPU queda libre, arranca de inmediato.
- Demostración visual: `compartido/vista/casos/fifo/` (ventana, presentación y punto
  de entrada).

## Caso SJF (el trabajo más corto primero, sin interrumpir)

Lógica en `compartido/nucleo/algoritmos/sjf/PlanificadorSJF.java`:

- Al quedar libre el CPU se elige al proceso apto con la ráfaga más corta; los
  empates se rompen por llegada y luego por orden alfabético.
- Igual que FIFO, cada proceso ejecuta su ráfaga completa sin interrupciones.
- Que una llegada recién entrada gane el despacho por ser más corta no es un salto
  de cola: es la definición del algoritmo (en cada terminación se elige al más corto
  entre todos los llegados, incluida la llegada de ese mismo instante).
- Demostración visual: `compartido/vista/casos/sjf/`.

## Caso Prioridades (manda el número de prioridad, con dos modos)

Lógica en `compartido/nucleo/algoritmos/prioridades/PlanificadorPrioridades.java`,
con el dato extra de prioridad en
`compartido/nucleo/algoritmos/prioridades/ProcesoPrioridad.java`:

- Al despachar se elige al proceso con el número de prioridad más bajo (el 1 es la
  más alta); los empates se rompen por ráfaga más corta y luego por llegada.
- Modo no apropiativo (por defecto): el dueño conserva el CPU hasta terminar su
  ráfaga, igual que FIFO/SJF.
- Modo apropiativo: el dueño cede en cuanto aparece un proceso de prioridad
  estrictamente mejor; una prioridad igual no lo desaloja. La disputa es siempre por
  la siguiente unidad de CPU, nunca se le quita al dueño lo ya ejecutado.
- En la ventana del caso hay dos botones para elegir el modo antes de simular
  (lógica de esos botones en
  `compartido/vista/casos/prioridades/VentanaPrioridades.java`) y la columna extra
  de prioridad se declara en
  `compartido/vista/casos/prioridades/LogicaVisualPrioridades.java`.

## Caso Round Robin (turnos de un quantum en cola circular)

Lógica en `compartido/nucleo/algoritmos/roundrobin/PlanificadorRoundRobin.java`,
con el número de turno en cola en
`compartido/nucleo/algoritmos/roundrobin/ProcesoRoundRobin.java`:

- Cada proceso ocupa el CPU durante un quantum (2 unidades por defecto) y al
  agotarlo vuelve al final de la cola; al terminar su ráfaga sale del sistema.
- El despacho de cada instante se hace solo con los procesos ya conocidos: un
  proceso que llega mientras otro ejecuta su turno debe esperar, incluso si el
  quantum expira justo en ese instante, porque el saliente vuelve a ocupar el CPU
  antes de que la llegada exista para el planificador. Solo si no hay nadie (CPU
  vacía) la llegada de ese instante arranca de inmediato.
- Ejemplo: con A(0,3) y B(2,6), A ejecuta sus 3 unidades seguidas y B, aunque entra
  en 2, espera y arranca en 3.
- El quantum se edita en el campo Q de la cabecera (lógica en
  `compartido/vista/casos/roundrobin/VentanaRoundRobin.java`) y la insignia muestra
  el valor vigente (`compartido/vista/casos/roundrobin/LogicaVisualRoundRobin.java`).

## Caso SRTF (el menor tiempo restante, interrumpiendo)

Lógica en `compartido/nucleo/algoritmos/srtf/PlanificadorSRTF.java`:

- Siempre se prefiere al proceso con menos tiempo restante; los empates se rompen
  por llegada y luego por orden alfabético.
- Cuando entra un proceso con menos restante que el dueño del CPU, lo suspende y
  toma el CPU en la siguiente unidad; si empatan, el dueño se mantiene y la llegada
  espera.
- A diferencia de SJF, aquí el restante baja conforme se ejecuta, por eso la
  comparación se repite en cada llegada en lugar de solo en cada terminación.
- Demostración visual: `compartido/vista/casos/srtf/`.

## Parte visual común (apoya la explicación en vivo)

- `compartido/vista/comun/VentanaSimulacion.java`: ventana base con botones de
  transporte, control de velocidad, temporizador y refresco.
- `compartido/vista/comun/PanelGantt.java`: dibuja el Gantt a partir de los
  segmentos del núcleo.
- `compartido/vista/comun/PanelConfiguracion.java`: parámetros C y T de cada
  proceso y alta/baja de procesos.
- `compartido/vista/comun/PanelMetricas.java`: tabla de E, F y promedios.
- `compartido/vista/comun/DialogoNuevoAuto.java`: diálogo de nuevo proceso (pausa la
  simulación al abrirse).
- `compartido/vista/comun/Tema.java` y demás paneles: paleta, carriles y
  componentes de la interfaz.
- Cada caso se abre desde su punto de entrada (`MainFIFO`, `MainSJF`,
  `MainPrioridades`, `MainRoundRobin`, `MainSRTF` en `compartido/vista/casos/`) con
  su ventana y su presentación (`Ventana*` y `LogicaVisual*`).

## Tabla resumen para exponer

| Algoritmo | Criterio de despacho | Cuándo cede el CPU | Archivos de lógica |
|---|---|---|---|
| FIFO | Menor llegada | Solo al terminar | `nucleo/algoritmos/fifo/` |
| SJF | Menor ráfaga | Solo al terminar | `nucleo/algoritmos/sjf/` |
| Prioridades | Menor nº de prioridad | Al terminar; en modo apropiativo, ante prioridad estrictamente mejor | `nucleo/algoritmos/prioridades/` (2 archivos) |
| Round Robin | Menor turno en la cola | Al agotar el quantum o al terminar | `nucleo/algoritmos/roundrobin/` (2 archivos) |
| SRTF | Menor tiempo restante | Al terminar o al llegar uno estrictamente más corto | `nucleo/algoritmos/srtf/` |

# Planificador de procesos — cómo funciona cada caso

Simulador de planificación de un solo CPU con cinco algoritmos. Esta guía explica la
lógica de funcionamiento de cada caso, sin código: por cada punto se indica en qué
archivo ocurre esa lógica.

Mapa rápido: la lógica vive en `compartido.nucleo` (modelo + mock + contrato + CPU en
`motor/` + planificadores en `algoritmos/no_apropiativos/` y
`algoritmos/apropiativos/`) y todo lo visual vive en `compartido.vista` (paneles
comunes en `comun/` y una ventana por caso en `casos/<algoritmo>/`). La vista solo
observa al núcleo, nunca al revés.

## Los procesos: un único mock

Todos los casos parten de la misma carrera, la de
`compartido/nucleo/mock/MockProcesos.java`, con cinco procesos fijos que no se dan de alta
ni de baja:

| Proceso | A | B | C | D | E |
|---|---|---|---|---|---|
| C (llegada) | 0 | 3 | 1 | 2 | 4 |
| T (ráfaga) | 5 | 2 | 3 | 2 | 4 |
| P (prioridad) | 3 | 1 | 5 | 4 | 2 |

El mock lleva los tres datos, pero cada algoritmo usa solo los que necesita: FIFO, SJF,
SRTF y Round Robin ignoran P; los demás ignoran el turno en cola. Los valores se pueden
cambiar mientras la simulación está detenida, en el panel de parámetros; el botón
Reiniciar recupera el mock original.

## Motor común: CPU + planificador (vale para los seis casos)

El contrato está en `compartido/nucleo/motor/Planificador.java`, la CPU compartida en
`compartido/nucleo/motor/CPU.java` y cada algoritmo implementa por sí mismo el mismo
esqueleto de simulación, de principio a fin, en su propio archivo
(`compartido/nucleo/algoritmos/no_apropiativos/` o `apropiativos/` según el caso).
No hay clase base
común: cada caso se lee entero, con su decisión y su política.

- La CPU (`motor/CPU.java`) es el recurso compartido: guarda al dueño actual,
  ejecuta una unidad de su trabajo y registra el segmento del Gantt. No decide,
  no gestiona colas y no lleva el reloj. Es soporte: no se explica en clase.
- El planificador decide quién obtiene la CPU (`seleccionar()`, admisión de
  llegadas, interrupciones, cola de Round Robin) y lleva el reloj que alimenta el
  temporizador.

- El reloj empieza en 0. Al iniciar, entran los procesos con llegada 0 y se despacha
  al primero.
- Cada avance suma una unidad al reloj y ocurre siempre en este orden: el dueño
  ejecuta una unidad `[reloj-1, reloj)` que queda registrada como segmento del Gantt
  (y cede si terminó; en Round Robin, si agotó el quantum); después entran las
  llegadas de ese instante (en Round Robin, al final de la cola, por delante del
  saliente); por último, si el CPU quedó libre, se despacha al siguiente según la
  política. Lo despachado rige desde el próximo tick: nadie ejecuta antes de haber
  llegado.
- Regla de llegadas que cumplen todos: si el CPU está libre en el instante en que
  entra un proceso, ese proceso queda despachado y empieza en el tick siguiente;
  si otro está usando el CPU, el recién llegado espera (o lo interrumpe, si la
  política apropiativa lo manda).
- Por defecto (no apropiativo) un proceso cede el CPU solo cuando agota su ráfaga.
- Los segmentos `[inicio, fin)` del diagrama de Gantt se registran en
  `compartido/nucleo/modelo/Segmento.java`, donde el instante `fin` es exclusivo.
- Cada proceso guarda llegada (C), ráfaga (T), lo que le resta, y sus instantes de
  inicio y fin (F) en `compartido/nucleo/modelo/Proceso.java`; ahí mismo vive la
  prioridad (P) del caso de prioridades. Round Robin usa una `Queue` propia, no un
  campo en el proceso. La espera se deriva al final como E = F − C − T. Los resúmenes
  y promedios (espera, retorno, cambios de contexto) se calculan en
  `compartido/nucleo/modelo/ResumenMetricas.java` (detalle por proceso en
  `compartido/nucleo/modelo/MetricaProceso.java`, estados en
  `compartido/nucleo/modelo/EstadoProceso.java`).

## Caso FIFO (el primero que llega es el primero que se atiende)

Lógica en `compartido/nucleo/algoritmos/no_apropiativos/PlanificadorFIFO.java`:

- Al quedar libre el CPU se elige al proceso que lleva más tiempo esperando (menor
  instante de llegada; a igual llegada, orden alfabético).
- Cada proceso conserva el CPU hasta terminar su ráfaga completa, sin interrupciones.
- Un proceso que llega mientras otro se ejecuta espera su turno; como el criterio es
  la llegada, nunca puede adelantar a quien ya esperaba. Si llega justo cuando el
  CPU queda libre, arranca de inmediato.
- Demostración visual: `compartido/vista/casos/fifo/` (ventana, presentación y punto
  de entrada).

## Caso SJF (el trabajo más corto primero, sin interrumpir)

Lógica en `compartido/nucleo/algoritmos/no_apropiativos/PlanificadorSJF.java`:

- Al quedar libre el CPU se elige al proceso apto con la ráfaga más corta; los
  empates se rompen por llegada y luego por orden alfabético.
- Igual que FIFO, cada proceso ejecuta su ráfaga completa sin interrupciones.
- Que una llegada recién entrada gane el despacho por ser más corta no es un salto
  de cola: es la definición del algoritmo (en cada terminación se elige al más corto
  entre todos los llegados, incluida la llegada de ese mismo instante).
- Demostración visual: `compartido/vista/casos/sjf/`.

## Casos no apropiativos y apropiativos

No apropiativos (`FIFO`, `SJF`, `Prioridades`): el dueño conserva el CPU hasta
terminar su ráfaga. Apropiativos (`SRTF`, `Round Robin`,
`Prioridades apropiativo`): el dueño puede ceder antes de terminar.

## Caso Prioridades (manda el número de prioridad, no apropiativo)

Lógica en `compartido/nucleo/algoritmos/no_apropiativos/PlanificadorPrioridades.java`,
con el dato extra de prioridad en
`compartido/nucleo/modelo/Proceso.java`:

- Al despachar se elige al proceso con el número de prioridad más bajo (el 1 es la
  más alta); los empates se rompen por ráfaga más corta y luego por llegada.
- El dueño conserva el CPU hasta terminar su ráfaga, igual que FIFO/SJF.
- La columna extra de prioridad se declara en
  `compartido/vista/casos/prioridades/LogicaVisualPrioridades.java`.

## Caso Prioridades apropiativo (misma selección, con interrupción)

Lógica en
`compartido/nucleo/algoritmos/apropiativos/PlanificadorPrioridadesApropiativo.java`:

- Mismo criterio de despacho que Prioridades.
- El dueño cede en cuanto aparece un proceso de prioridad estrictamente mejor;
  una prioridad igual no lo desaloja. La disputa es siempre por la siguiente unidad
  de CPU, nunca se le quita al dueño lo ya ejecutado.
- En la ventana del caso hay dos botones para elegir la variante antes de simular
  (lógica de esos botones en
  `compartido/vista/casos/prioridades/VentanaPrioridades.java`): cada botón abre su
  propio planificador, sin modo compartido.

## Caso Round Robin (turnos de un quantum en cola circular)

Lógica en `compartido/nucleo/algoritmos/apropiativos/PlanificadorRoundRobin.java`,
con una `Queue` propia como cola circular:

- Cada proceso ocupa el CPU durante un quantum (2 unidades por defecto) y al
  agotarlo vuelve al final de la cola; al terminar su ráfaga sale del sistema.
- Los nuevos de cada instante entran a la cola antes de que el saliente vuelva a
  ella, así que el saliente queda detrás de las llegadas de ese instante. Si el CPU
  está libre, la llegada de ese instante arranca de inmediato.
- Con el mock y Q = 2 se ve: A ejecuta [0-2), C y D esperan en cola; luego rotan
  C, D, A, B, E hasta agotar sus ráfagas.
- El quantum se edita en el campo Q de la cabecera (lógica en
  `compartido/vista/casos/roundrobin/VentanaRoundRobin.java`) y la insignia muestra
  el valor vigente (`compartido/vista/casos/roundrobin/LogicaVisualRoundRobin.java`).

## Caso SRTF (el menor tiempo restante, interrumpiendo)

Lógica en `compartido/nucleo/algoritmos/apropiativos/PlanificadorSRTF.java`:

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
  proceso, más la columna extra que declare cada caso.
- `compartido/vista/comun/PanelMetricas.java`: tabla de E, F y promedios.
- `compartido/vista/comun/Tema.java` y demás paneles: paleta, carriles y
  componentes de la interfaz.
- Cada caso se abre desde su punto de entrada (`MainFIFO`, `MainSJF`,
  `MainPrioridades`, `MainRoundRobin`, `MainSRTF` en `compartido/vista/casos/`) con
  su ventana y su presentación (`Ventana*` y `LogicaVisual*`).

## Tabla resumen para exponer

| Algoritmo | Tipo | Criterio de despacho | Cuándo cede el CPU | Archivos de lógica |
|---|---|---|---|---|
| FIFO | No apropiativo | Menor llegada | Solo al terminar | `nucleo/algoritmos/no_apropiativos/PlanificadorFIFO.java` |
| SJF | No apropiativo | Menor ráfaga | Solo al terminar | `nucleo/algoritmos/no_apropiativos/PlanificadorSJF.java` |
| Prioridades | No apropiativo | Menor nº de prioridad | Solo al terminar | `nucleo/algoritmos/no_apropiativos/PlanificadorPrioridades.java` |
| Prioridades apropiativo | Apropiativo | Menor nº de prioridad | Al terminar o ante prioridad estrictamente mejor | `nucleo/algoritmos/apropiativos/PlanificadorPrioridadesApropiativo.java` |
| Round Robin | Apropiativo | Orden de la cola | Al agotar el quantum o al terminar | `nucleo/algoritmos/apropiativos/PlanificadorRoundRobin.java` |
| SRTF | Apropiativo | Menor tiempo restante | Al terminar o al llegar uno estrictamente más corto | `nucleo/algoritmos/apropiativos/PlanificadorSRTF.java` |

# SCOMP Sprint 3 — Flight Simulation System

## Auto-avaliação (0-100%)

- **1241178 Mário Machado**: 100%
- **1241323 José Cardoso**: 100%
- **1241182 Afonso Machado**: 100%
- **1240731 Tomás Pinho**: 100%
- **1240754 Eduardo Santos**: 100%

---

## Decisões Técnicas

- A arquitetura do Sprint 2 (pipes de controlo + pipes de dados) foi substituída por **shared memory POSIX** (shm_open e mmap) + semáforos POSIX nomeados (`sem_open`).
- Toda a estrutura SimState agora é em shared memory e é partilhada entre o processo pai, as suas threads e os processos filhos
- Cada voo continua a correr num processo filho (`fork`), mas agora escreve a sua posição diretamente em `state->positions[i]` e `state->history`, em vez de a enviar por um pipe de dados
- O processo pai cria **3 threads dedicadas**: safety, report e, opcionalmente, environment só se weather for indicado
- Foi adicionado um novo ficheiro `flights_weather_violations.json` especificamente para testar o efeito do vento a causar violações
- Mantemos a resolução de conflitos de partida (mesmo aeroporto, mesma hora → atraso +1 step)
- `signal(SIGPIPE, SIG_IGN)` mantido por segurança, embora já não exista comunicação por pipes entre pai e filhos

---

## Diagrama de componentes

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PROCESSO PAI                                   │
│                                                                             │
│  Thread principal               Thread safety            Thread report      │
│  ┌─────────────────┐            ┌─────────────────┐      ┌────────────────┐ │
│  │ parse + fork    │            │ thread_safety_fn│      │thread_report_fn│ │
│  │ main.c          │            │ threads.c       │      │ threads.c      │ │
│  │ loop por step:  │  sem_post  │ check_violations│ cond │ imprime novas  │ │
│  │  sem_step[i]    │──────────▶ │ sobre SimState │─────▶│ violações e    │ │
│  │  sem_done[i]    │  sem_post  │ envia SIGUSR1   │signal│ gera relatório │ │
│  │  sem_safety_*   │◀────────── │(sem_safety_done)│     │ final          │ │
│  └────────┬────────┘            └────────┬────────┘      └────────────────┘ │
│           │                              │                                  │
│  Thread environment (opcional)           │                                  │
│  ┌───────────────────────────────┐       │                                  │
│  │ thread_environment_fn         │       │                                  │
│  │ lê CSV de vento (weather_*.csv│       │                                  │
│  │ -> state->environment         │       │                                  │
│  └───────────────────────────────┘       │                                  │
│                                                                             │
│                    ┌────────────────────────────────┐                       │
│                    │   SHARED MEMORY  /aisafe_sim   │                       │
│                    │   (struct SimState)            │                       │
│                    │   positions[], history,        │                       │
│                    │   violations[], status[],      │                       │
│                    │   environment, pos_ready[],    │                       │
│                    │   sim_done, current_step       │                       │
│                    └────────────────┬───────────────┘                       │
└─────────────────────────────────────┼───────────────────────────────────────┘
                                      │ mmap (MAP_SHARED)
            ┌─────────────────────────┴───────────────────────────┐
            │              PROCESSOS FILHOS (1 por voo)           │
            │                                                     │
            │  ┌────────────────────────────────────────────────┐ │
            │  │             run_flight()                       │ │
            │  │          flight_process.c                      │ │
            │  │                                                │ │
            │  │  sem_wait(step_sems[i]) → next_position() →    │ │
            │  │  apply_environment_wind() →                    │ │
            │  │  escreve positions[i]/history em shm →         │ │
            │  │  sem_post(done_sems[i]) → repete               │ │
            │  └────────────────────────────────────────────────┘ │
            │                                                     │
            │  SIGUSR1 ← violação (da thread safety)              │
            │  SIGTERM ← terminação (threshold atingido)          │
            └─────────────────────────────────────────────────────┘
```

---

## Protocolo de sincronização por step (US108)

A sincronização é feita por dois pares de semáforos POSIX nomeados:

- sem_step[i] / sem_done[i] (um par por voo) — sincronização pai ↔ filho i
- sem_safety_check / sem_safety_done — sincronização pai ↔ thread safety

Sequência de cada step `N`:

1. O pai incrementa current_step e, para cada voo ativo cujo `start_step` já foi atingido, faz `sem_post(sem_step[i])` (`signalled[i] = 1`)
2. Cada filho `i` estava bloqueado em `sem_wait(sem_step[i])`. Ao ser libertado, calcula `next_position()`, aplica o vento (apply_environment_wind), e escreve a posição em `state->positions[i]` e em `state->history`, marca `pos_ready[i] = 1` e faz `sem_post(sem_done[i])`
3. O pai faz `sem_wait(sem_done[i])` para cada voo sinalizado neste step (**barreira**): o pai só avança depois de todos os filhos ativos terem escrito a sua posição
4. O pai faz `sem_post(sem_safety_check)`. A thread `safety` estava bloqueada em `sem_wait(sem_safety_check)`; ao acordar, percorre `state->positions[]` (só os voos com `pos_ready[i] == 1`), aplica `check_violations` (Haversine + threshold) e, se necessário, envia `SIGUSR1` aos filhos envolvidos, regista o evento e sinaliza a thread `report` via `pthread_cond_signal`
5. A thread `safety` termina a verificação com `sem_post(sem_safety_done)`. O pai estava em `sem_wait(sem_safety_done)` — só depois disto limpa `pos_ready[i] = 0` para todos os voos e avança para o step `N+1`

Este protocolo garante que nenhum filho avança para o step seguinte antes de o pai/thread safety terem processado todas as posições do step atual, mas agora com semáforos em vez de bytes em pipes, e com a verificação de segurança a correr numa thread dedicada em vez de no pai.

`sem_wait` é repetido em caso de `EINTR`, exceto na espera do filho em `sem_step_wait`, que pode ser interrompido por `SIGTERM` para permitir a terminação.

---

## Estrutura de ficheiros

```
sprint3/
├── Makefile
├── README.md
├── run_ui.sh                 - menu interativo para correr simulações
├── data/
│   ├── weather_aca121_realistic.csv
│   ├── weather_aca121_medium.csv
│   ├── weather_aca121_high.csv
│   └── weather_aca121_intense.csv
├── include/
│   ├── flight_plan.h         - estruturas do plano de voo
│   ├── simulation.h          - SimState, EnvironmentData, FlightHistory
│   ├── physics.h              - haversine, next_position, apply_environment_wind
│   ├── flight_process.h       - run_flight
│   ├── json_parser.h           - parse_flight_plans
│   ├── safety.h                - check_violations
│   ├── shm.h                   - shm_create / shm_open_existing / shm_destroy
│   ├── semaphore_sync.h        - sem_step/sem_done por voo
│   ├── threads.h               - ThreadContext, threads_launch, thread_environment_launch
│   └── report.h                - generate_report()
├── src/
│   ├── main.c                  - fork, shared memory, loop de simulação por step
│   ├── shm.c                   - criação/abertura/destruição da shared memory
│   ├── semaphore_sync.c        - criação/abertura/fecho dos semáforos POSIX
│   ├── threads.c               - thread_safety_fn, thread_report_fn, thread_environment_fn
│   ├── physics.c                - física climb/cruise/descend + vento
│   ├── flight_process.c         - loop do processo filho
│   ├── json_parser.c            - parser manual JSON
│   ├── safety.c                 - deteção de colisões (Haversine), headings
│   └── report.c                 - relatório final
└── tests/
    ├── flights_single.json
    ├── flights_no_violations.json
    ├── flights_departure_conflict.json
    ├── flights_with_violations.json
    ├── flights_stress_10.json
    ├── flights_termination.json
    └── flights_weather_violations.json
```

---

## Abordagem em cada US

### US105 — Inicialização do ambiente de simulação híbrido com shared memory

**Implementado com sucesso.**

- `shm_create()` cria `/aisafe_sim` com `shm_open(O_CREAT | O_EXCL)` + `ftruncate` + `mmap(MAP_SHARED)`, dimensionado para `sizeof(SimState)`, e é chamado pelo pai **antes** de qualquer `fork()`
- Os filhos herdam o mapeamento da shared memory por via do `fork()` (não é necessário `shm_open_existing()` neste fluxo, já que o `mmap` é feito antes do fork e é partilhado por herança)
- `sem_sync_init()` cria, para cada voo, dois semáforos POSIX nomeados (`/aisafe_step_<i>` e `/aisafe_done_<i>`), inicializados a 0
- Os processos filhos são criados antes de o pai lançar as threads `safety`/`report`, de forma a evitar criar um processo multi-thread imediatamente antes do `fork` (que poderia deixar o filho num estado inconsistente)
- Barreira inicial: cada filho faz um `sem_post(done_sems[i])` logo após instalar os seus handlers. O pai espera por todos antes de avançar, o que evita que um `SIGUSR1` precoce (da thread safety) que chegue a um filho que ainda tem a ação default para `SIGUSR1`

### US106 — Threads específicas no processo pai

**Implementado com sucesso.**

- O pai lança pelo menos duas threads dedicadas: `t_safety` (deteção de violações) e `t_report` (relatório/log de violações em tempo real)
- Se for indicado o weather, também é lançada t_environment, que carrega os dados de vento para `state->environment` antes do início da simulação
- `ThreadContext` agrupa o ponteiro para `SimState`, os arrays de semáforos `step_sems`/`done_sems`, o mutex, a condition variable `cond_violation`, a flag `new_violation` e os nomes dos ficheiros de relatório/weather
- Um `pthread_mutex_t` protege as escritas/leituras de `SimState` feitas pelas threads (violações, `violation_count`, `environment`, `sim_done`)

### US107 — Notificação da thread report via condition variables

**Implementado com sucesso.**

- Quando a thread `safety` deteta uma violação (Haversine < `SAFETY_THRESHOLD_H_M` e diferença de altitude < `SAFETY_THRESHOLD_V_M`), regista o evento em `state->violations[]`, incrementa `violation_count`, define `*ctx->new_violation = 1` e chama `pthread_cond_signal(ctx->cond_violation)`
- A thread `report` fica bloqueada em `pthread_cond_wait` enquanto `last_reported >= violation_count && !sim_done`; ao ser notificada, percorre todas as violações novas desde a última vez (não apenas a última, evitando saltos como 1, 3, 6, 10...) e imprime `[REPORT] Violation N at step S: flights A and B`
- Todo o acesso a `state` dentro da thread report é protegido pelo mesmo mutex usado pela thread safety

### US108 — Sincronização passo-a-passo com semáforos

**Implementado com sucesso.**

- Ver secção "Protocolo de sincronização por step" acima para o detalhe do protocolo de semáforos `sem_step[i]` / `sem_done[i]` / `sem_safety_check` / `sem_safety_done`
- Voos com partida atrasada (`start_step[i] > 0`) só são sinalizados quando `current_step > start_step[i]`
- Se `violation_count >= TERMINATION_THRESHOLD` (100), o pai envia `SIGTERM` a todos os filhos ainda ativos, marca-os como `FLIGHT_TERMINATED` e recolhe-os com `waitpid`. Filhos que já tinham terminado naturalmente nesse mesmo step não são à espera novamente em `sem_done` (para evitar deadlock)
- Filhos cujo step nunca chegou a ser sinalizado (porque o threshold foi atingido antes) são acordados com `sem_post(step_sems[i])` seguido de `SIGTERM`, para garantir que saem do `sem_wait` e terminam corretamente

### US109 — Geração do relatório final

**Implementado com sucesso.**

- A thread `report` chama `generate_report()` depois de `state->sim_done == 1`
- O ficheiro `simulation_report.txt` adiciona uma linha nova **Environment: ENABLED/DISABLED** que indica se a simulação correu com dados de vento

### US110 — Influência de fatores ambientais na simulação

**Implementado com sucesso.**

- Ativado com a flag `--weather <ficheiro.csv>`
- Formato do CSV: `aca,lat1,lon1,lat2,lon2,alt_inf_ft,alt_sup_ft,direction_deg,value_knot,day,start,end` — cada linha define uma célula retangular de espaço aéreo com uma direção (graus, relativa ao Norte) e intensidade de vento (nós)
- A thread `thread_environment_fn` lê o CSV no arranque, copia as células para `state->environment.cells[]` e marca `environment.ready = 1`; o pai aguarda este sinal antes de lançar as threads `safety`/`report`. Se a leitura do CSV falhar, a simulação termina com erro
- `apply_environment_wind()` (em `physics.c`) é chamada pelo processo filho a cada step, depois de `next_position()`: se a posição atual da aeronave estiver dentro de uma célula de vento (latitude/longitude/altitude), aplica um deslocamento (`lat`/`lon`) proporcional à velocidade do vento (convertida de nós para m/s) e a `TIME_STEP_S`
- Foram fornecidos 4 cenários de vento (`weather_aca121_realistic.csv`, `_medium.csv`, `_high.csv`, `_intense.csv`), com intensidades crescentes, para permitir comparar o efeito do vento na trajetória e na ocorrência de violações
- O ficheiro de teste `flights_weather_violations.json` foi criado especificamente para demonstrar como o vento pode aproximar duas trajetórias inicialmente divergentes, gerando violações que não ocorreriam sem vento

---

## O que não foi implementado / limitações conhecidas

- `shm_open_existing()` está implementado mas não é necessário no fluxo atual (o `mmap` é feito antes do `fork`, sendo herdado pelos filhos); fica disponível para um eventual cenário em que os filhos sejam processos independentes (`exec`)

---

## Função dos ficheiros de teste

| Ficheiro | Voos | Resultado esperado | Propósito |
|---|---|---|---|
| `flights_single.json` | 1 | PASSED, 0 violações | Caso base |
| `flights_no_violations.json` | 2 | PASSED, 0 violações | Rotas divergentes OPO→MAD e LIS→CDG |
| `flights_departure_conflict.json` | 2 | PASSED, 0 violações | Mesmo aeroporto mesma hora, rotas divergem |
| `flights_with_violations.json` | 3 | FAILED, várias violações | Rotas próximas, start_steps 0/1/2 |
| `flights_stress_10.json` | 10 | PASSED, 0 violações | 10 voos com rotas separadas (validação/desempenho) |
| `flights_termination.json` | 10 | FAILED, ≥100 violações, TERMINATED | Threshold de terminação atingido (US108) |
| `flights_weather_violations.json` | 2 | FAILED (com vento) | Demonstra o efeito do vento (US110) a aproximar trajetórias |

## Cenários de vento (US110)

| Ficheiro | Intensidade aproximada | Propósito |
|---|---|---|
| `weather_aca121_realistic.csv` | 12–25 nós | Cenário base, vento moderado realista |
| `weather_aca121_medium.csv` | ~25–50 nós | Vento médio, efeito mais visível na trajetória |
| `weather_aca121_high.csv` | ~50–75 nós | Vento forte |
| `weather_aca121_intense.csv` | 30–62.5 nós | Vento intenso, usado para forçar violações em `flights_weather_violations.json` |

Todos os ficheiros cobrem a área `aca=121`, dividida em 4 células retangulares entre 0 e 1000 ft, com direções variando entre 45° e 105°.
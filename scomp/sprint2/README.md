# SCOMP Sprint 2 — Flight Simulation System

## Auto-avaliação (0-100%)

- **1241178 Mário Machado**: 100%
- **1241323 José Cardoso**: 100%
- **1241182 Afonso Machado**: 100%
- **1240731 Tomás Pinho**: 100%
- **1240754 Eduardo Santos**: 100%

---

## Decisões Técnicas

 - Usamos o ficheiro `Flight_Plan_v0c.json` presente no Moodle como um plano de voo de exemplo
 - Para maior facilidade a testar as User Stories, cada plano de voo contém vários voos, sendo apenas necessário usar um ficheiro
 - Apenas foram considerados os parâmetros existentes no ficheiro JSON 
 - Se dois voos partem do mesmo aeroporto à mesma hora, o segundo deve ser atrasado

---

## Diagrama de componentes

```
┌────────────────────────────────────────────────────────────────┐
│                        PROCESSO PAI                            │
│                                                                │
│  ┌─────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │ parse+fork  │  │ check_violations │  │ generate_report  │   │
│  │  main.c     │  │   safety.c       │  │   report.c       │   │
│  └─────────────┘  └──────────────────┘  └──────────────────┘   │
│  ┌─────────────┐  ┌──────────────────┐                         │
│  │ step_signal │  │  FlightHistory   │                         │
│  │  sync.c     │  │  simulation.h    │                         │
│  └─────────────┘  └──────────────────┘                         │
└────────────┬──────────────────┬────────────────────────────────┘
             │                  │
     ┌───────▼──────┐   ┌───────▼──────┐       ┌───────────────┐
     │data pipe     │   │ctrl pipe     │       │               │
     │(posição)     │   │(byte1+byte2) │─────▶│   Relatório   │
     │filho → pai   │   │pai → filho   │       │  da Simulação │
     └───────┬──────┘   └───────┬──────┘       └───────────────┘
             │                  │
   ┌─────────▼──────────────────▼───────────────────────┐
   │            PROCESSOS FILHOS (1 por voo)            │
   │                                                    │
   │  ┌──────────────────────────────────────────────┐  │
   │  │              run_flight()                    │  │
   │  │           flight_process.c                   │  │
   │  │                                              │  │
   │  │  step_wait → calcula → write(pipe) →         │  │
   │  │  step_wait (barreira) → step_wait → ...      │  │
   │  └──────────────────────────────────────────────┘  │
   │                                                    │
   │  SIGUSR1 ← violação    SIGTERM ← terminar          │
   └────────────────────────────────────────────────────┘
```

---

## Protocolo de sincronização (US103)


![Sincronizacao](scomp/sprint2/sincronizacao.png)

O filho só avança quando recebe o byte 2 — o que garante que o pai verificou a existência de violações antes de qualquer filho avançar.



---

## Estrutura de ficheiros

```
sprint2/
├── makefile
├── README.md
├── include/
│   ├── flight_plan.h       - estruturas do plano de voo
│   ├── simulation.h        - AircraftPosition, SafetyViolationEvent, FlightHistory, SimState
│   ├── physics.h           - haversine, next_position
│   ├── flight_process.h    - run_flight()
│   ├── json_parser.h       - parse_flight_plans()
│   ├── safety.h            - check_violations()
│   ├── sync.h              - step_signal(), step_wait()
│   └── report.h            - generate_report()
├── src/
│   ├── main.c              - fork, loop de simulação, barreira
│   ├── physics.c           - física climb/cruise/descend
│   ├── flight_process.c    - loop do processo filho
│   ├── json_parser.c       - parser manual JSON
│   ├── safety.c            - deteção de colisões, headings
│   ├── sync.c              - ctrl pipes
│   └── report.c            - relatório com velocity vectors
└── tests/
    ├── flights_with_violations.json
    ├── flights_no_violations.json
    ├── flights_single.json
    ├── flights_departure_conflict.json
    ├── flights_stress_10.json
    └── flights_termination.json
```

---

## Abordagem em cada US

### US100 — Inicialização da simulação

**Implementado com sucesso.**

- Leitura e validação do JSON com mensagens de erro claras (ID inválido, legs em falta, perfil de climb/descend vazio, velocidade de cruise zero, número de segmentos diferente de 3)
- Resolver os conflitos de partida: `DepartureTime` é em minutos; o voo mais cedo tem `start_step=0`, os restantes têm `start_step = diferença em minutos`. Se dois voos partem do mesmo aeroporto à mesma hora, o segundo é atrasado +1 step
- Criam-se  2 pipes por voo (data + ctrl) antes de todos os `fork()`
- `signal(SIGPIPE, SIG_IGN)` para evitar crash quando um filho termina antes do pai escrever no ctrl pipe

---

### US101 — Captura e transmissão dos movimentos

**Implementado com sucesso.**

- A cada step, cada um dos filhos envia `AircraftPosition` ao pai via data pipe
- O `AircraftPosition` contém: `flight_id`, `lat`, `lon`, `alt_m`, `speed_knots`, `step`
- O pai lê sequencialmente todos os data pipes activos - e o `read()` bloqueia até receber
- Cada posição é guardada em `state.positions[i]` (posição actual) e em `state.history` (histórico com máximo de 500 posições por voo)
- Terminar de forma inesperada: `read()` retorna 0 (EOF) → pai marca `FLIGHT_COMPLETED` sem dar erro

---

### US102 — Deteção de violações de segurança em tempo real

**Implementado com sucesso.**

- O `check_violations()` é verificado após cada step, antes de avançar para o próximo step
- Verifica todos os pares de voos activos com posição válida (`flight_id != 0`)
- É considerado uma violação quando a distância horizontal < `SAFETY_THRESHOLD_H_M = 5000m` e distância vertical < `SAFETY_THRESHOLD_V_M = 300m`
- A distância horizontal é calculada com a fórmula de Haversine do enunciado
- Ao detectar uma violação envia `SIGUSR1` a ambos os filhos e regista em `state.violations[]` com step, posição média, IDs, velocidades e direções
- O handler `SIGUSR1` no filho, `sigfillset(&act.sa_mask)`, bloqueia todos os outros sinais durante o handling; usa `write()` (async-signal-safe); flag `volatile sig_atomic_t got_violation`
- Se `violation_count >= 100`, envia `SIGTERM` a todos os filhos activos e fecha os ctrl pipes

---

### US103 — Sincronização por time step

**Implementado com sucesso.**

- Protocolo de 2 bytes no ctrl pipe (pipe unidireccional pai → filho):
  - **Byte 1**: pai autoriza filho a calcular posição
  - **Byte 2**: pai confirma barreira após ler todos os data pipes e verificar violações
- Filho só avança para o step N+1 depois de receber o byte 2
- `step_wait()` repete o `read()` se interrompido por sinal (`errno == EINTR`) — o `SIGUSR1` de uma violação não quebra a sincronização
- `TIME_STEP_S` configurável com `--timestep <segundos>` (o default que metemos foi 60 segundos)
- Voos com partida atrasada: o pai só sinaliza e só lê quando `current_step > start_step[i]`

---

### US109 — Geração do relatório final

**Implementado com sucesso.**

- Ficheiro `simulation_report.txt` gerado pelo pai após todos os filhos terminarem
- Conteúdo:
  - Total de voos, steps e violações
  - Resultado global: `PASSED` (0 violações) ou `FAILED` (≥1 violação)
  - Status por voo: `COMPLETED` ou `TERMINATED`
  - Lista de violações (até 50) com step, posição (lat/lon/alt), IDs dos voos, velocidade e heading de cada avião no momento da violação
- Heading calculado a partir das últimas 2 posições no histórico com `atan2(dlon, dlat)` em graus (0=Norte, 90=Este)

## Função dos Ficheiros de teste

| Ficheiro | Voos | Resultado | Propósito |
|---|---|---|---|
| `flights_single.json` | 1 | PASSED, 0 violações | Caso base |
| `flights_no_violations.json` | 2 | PASSED, 0 violações | Rotas divergentes OPO→MAD e LIS→CDG |
| `flights_departure_conflict.json` | 2 | PASSED, 0 violações | Mesmo aeroporto mesma hora, rotas divergem |
| `flights_with_violations.json` | 3 | FAILED, 6 violações | Rotas próximas, start_steps 0/1/2 |
| `flights_stress_10.json` | 10 | PASSED, 0 violações | 10 voos rotas separadas |
| `flights_termination.json` | 10 | FAILED, 106 violações, TERMINATED | Threshold atingido em ~20 steps |

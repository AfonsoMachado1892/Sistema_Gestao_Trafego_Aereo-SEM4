# US112 — Monthly Report Generation

## 1. Requirements

**As a Flight Control Operator, I want to generate a monthly statistics report.**

> This will be one of a number (e.g. compliance, incident, etc) of reports the system will have to generate in the future and this work should be foundational to these to guarantee all follow a consistent branding and structure. We imagine each report type has a specific way of collecting data, specific sections and graphics.

### Acceptance Criteria
- A Flight Control Operator can generate a monthly statistics report for a chosen year and month.
- The report aggregates the flight plans created in that month and presents: the total number of flight plans, the count by status, and the count by route.
- The report follows a consistent branding and structure (header + body + footer), so that future report types can reuse it.
- The report is shown on screen and saved to a file.
- Access is restricted to the Flight Control Operator (and Admin), consistent with the other Flight Control Operator use cases.

### Decisions (to validate with PO / team)
- **Data source:** the report aggregates **flight plans** created in the chosen month. The `FlightPlan` aggregate is the only source with usable creation dates (`createdAt`), which makes monthly aggregation possible. Simulation results (US111) are not persisted per month and therefore cannot be aggregated monthly.
- **Statistics:** total flight plans, count by status (DRAFT, TESTED, VALIDATED, REJECTED, CANCELLED) and count by route.
- **Output:** displayed on screen and saved as a `.txt` file (same approach as US111).
- **Foundation:** an abstract `Report` base applies the **Template Method** pattern, so future reports (compliance, incident, ...) reuse the common branding and structure.

---

## 2. Analysis

### Actor
- Flight Control Operator (FCO)

### System Sequence Diagram
![SSD US112](../analysis/SSD_us112.svg)

### Domain Concepts Involved
- **FlightPlan** (Aggregate Root) — provides `createdAt`, `status` and `routeCode`, used to aggregate the monthly statistics.
- **FlightPlanRepository** — `findAll()` returns every flight plan; filtering by month is done in memory.

---

## 3. Design

### Sequence Diagram
![SD US112](SD_us112.svg)

### Class Diagram
![CD US112](CD_us112.svg)

### Design Decisions
- **Template Method (`Report`).** The abstract `Report` fixes the report skeleton in `render()` (branded header + body + footer); concrete reports implement only `reportTitle()` and `buildBody()`. This directly addresses the "foundational ... consistent branding and structure" requirement, so that future report types reuse the same structure.
- **Thin controller.** `GenerateMonthlyReportController` only enforces authorization and delegates to `MonthlyReportService`, mirroring `GenerateSimulationReportController` (US111).
- **Constructor injection.** The controller receives the `FlightPlanRepository` via its constructor; the UI (composition root, in the app module) resolves it through `PersistenceContext`. This keeps the `core` module independent of the infrastructure, consistent with US076/US077.
- **In-memory filtering.** As the `FlightPlanRepository` has no date-range query, the service fetches all plans via `findAll()` and filters by month. This avoids changing the shared repository interface (which would break other use cases' stubs and risk NFR06).
- **Testable statistics.** The counting logic is split into package-private methods (`filterByMonth`, `countByStatus`, `countByRoute`) so it can be unit-tested independently of file I/O and persistence.

### Classes Created / Modified

| Class | Layer | Action |
|---|---|---|
| `Report` | Application (report) | Created — abstract base (Template Method) |
| `MonthlyFlightPlanReport` | Application (report) | Created — concrete report |
| `MonthlyReportService` | Application (report) | Created |
| `GenerateMonthlyReportController` | Application (report) | Created |
| `MonthlyReportDTO` | Application (report) | Created |
| `ReportException` | Application (report) | Created |
| `GenerateMonthlyReportUI` | Presentation (report) | Created |
| `FlightControlOperatorMenuUI` | Presentation (simulation) | Modified — added "Generate Monthly Report" option |

---

## 4. Implementation

The UI asks the operator for a year and month, then calls the controller. The controller checks authorization and delegates to the service. The service fetches all flight plans, keeps the ones created in the chosen month, counts them by status and by route, builds a `MonthlyFlightPlanReport` (which renders the branded text via the `Report` template), saves the text to a `.txt` file, and returns a `MonthlyReportDTO` with the data, the rendered text and the file path. The UI prints the report and the file location.

---

## 5. Tests

### MonthlyReportService

| Test | Description |
|---|---|
| `ensureRejectsInvalidMonth` | Month 13 → `IllegalArgumentException` |
| `ensureRejectsMonthZero` | Month 0 → `IllegalArgumentException` |
| `ensureRejectsInvalidYear` | Invalid year → `IllegalArgumentException` |
| `ensureCountsByStatus` | Counts grouped by status |
| `ensureCountByStatusIsEmptyForNoPlans` | No plans → empty map |
| `ensureCountsByRoute` | Counts grouped by route |
| `ensureFilterKeepsPlansOfCurrentMonth` | Plans of the current month are kept |
| `ensureFilterExcludesOtherMonths` | Plans outside the month are excluded |
| `ensureGeneratesReportForCurrentMonth` | End-to-end for the current month |
| `ensureEmptyMonthYieldsZeroTotal` | Month with no plans → total 0 |

### GenerateMonthlyReportController

| Test | Description |
|---|---|
| `ensureAuthorizedUserCanGenerateReport` | Authorized → returns DTO |
| `ensureServiceIsDelegatedTo` | Controller delegates to the service |
| `ensureUnauthorizedUserIsBlocked` | Unauthorized → exception, service not reached |

> Note: `FlightPlan.createdAt()` is always set to `LocalDateTime.now()` and cannot be controlled, so date-based filtering is tested against the current month (and a different month for exclusion); the status/route counting is tested directly via the package-private helpers, independently of the creation date.
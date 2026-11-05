# US076 — List Pilot Roster

## 1. Requirements

**As an Air Transport Company Collaborator, I want to list my company's pilot roster.**

### Acceptance Criteria
- The ATCC can list all pilots associated with their own company.
- The roster includes both active and inactive pilots, clearly indicating each pilot's status.
- Each pilot entry shows: ID, name, company IATA code, status, and number of certifications.
- The roster is scoped to the ATCC's own company; pilots from other companies are not shown.
- If the company has no pilots, the system informs the user with an appropriate message.

### Decisions validated with the team
- The roster shows **all** pilots (active and inactive), with the status field distinguishing them.
- This use case is exclusive to the **ATCC** — the Admin does not have access (the Admin only reaches the Backoffice Operator menu).

---

## 2. Analysis

### Actor
- Air Transport Company Collaborator (ATCC)

### System Sequence Diagram
![SSD US076](SSD_US076.svg)

### Domain Concepts Involved
- **Pilot** (Aggregate Root) — identified by `PilotID`, belongs to a company via `AirTransportCompanyCode`, has a `PilotStatus` (ACTIVE / INACTIVE).
- **PilotRepository** — `findByCompany(AirTransportCompanyCode)` returns all pilots of the company.

---

## 3. Design

### Sequence Diagram
![SD US076](SD_US076.svg)

### Class Diagram
![CD US076/077](CD_US076_US077.svg)

### Design Decisions
- The controller resolves the collaborator's company via `CustomerCollaboratorRepository`, then delegates to `PilotService.listPilotsByCompany()`.
- DTO mapping (`toDTO`) is centralized in `PilotService`, avoiding duplication across controllers.
- The package-private constructor `ListPilotRosterController(AuthorizationService, ...)` enables unit testing without `AuthzRegistry`.
- All repositories are injected via the production constructor from `PersistenceContext.repositories()` — consistent with `ListCompanyFleetUI`.

### Classes Created / Modified

| Class | Layer | Action |
|---|---|---|
| `ListPilotRosterController` | Application | Created |
| `PilotService` | Application | Modified — added `listPilotsByCompany`, `listActivePilotsByCompany`, centralized `toDTO` |
| `ListPilotRosterUI` | Presentation | Created |
| `AirTransportCompanyMenuUI` | Presentation | Modified (added menu item 8) |

---

## 4. Implementation

The controller authenticates the user as ATCC, resolves the company, and delegates to the service. The service fetches all pilots of the company via `findByCompany` and maps them to `PilotDTO`. The UI prints each pilot in a formatted block, returning `false` (single display, no loop).

---

## 5. Tests

| Test | Description |
|---|---|
| `ensureRosterReturnsAllPilotsOfCompany` | Two pilots → both returned |
| `ensureRosterIncludesInactivePilots` | Active + inactive → both shown, distinct statuses |
| `ensureRosterReturnsEmptyWhenNoPilots` | Empty repo → empty result |
| `ensureRosterDoesNotReturnPilotsFromOtherCompany` | Pilot of FR → not shown to TP |
| `ensureRosterReturnsDTOWithCorrectFields` | DTO has correct companyIata, status, certificationCount |

(Additional service-level tests in `PilotServiceTest`: `listPilotsByCompany` and `listActivePilotsByCompany`.)

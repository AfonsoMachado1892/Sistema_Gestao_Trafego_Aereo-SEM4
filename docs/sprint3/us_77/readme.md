# US077 — Remove a Pilot

## 1. Requirements

**As an Air Transport Company Collaborator, I want to make a pilot inactive in my company's roster.**

> Cannot deactivate a pilot with flight plans assigned.

### Acceptance Criteria
- The ATCC can deactivate (remove) a pilot from their own company's roster.
- It is not possible to remove a pilot who has flight plans assigned (in DRAFT, TESTED, or VALIDATED status).
- Removing a pilot sets their status to inactive; the pilot is not permanently deleted.
- Only pilots scoped to the ATCC's own company may be removed.
- An already inactive pilot cannot be removed again.
- On success, the system confirms the removal with an appropriate message.

### Decisions validated with the team
- **Blocking flight plan states:** a pilot cannot be removed if they have any flight plan in `DRAFT`, `TESTED` or `VALIDATED`. Plans in `REJECTED` or `CANCELLED` do **not** block removal (the team chose the simpler approach: do not allow removal while a plan is assigned, rather than reassigning the plan to another certified pilot).
- **Login is not deactivated:** the use case only makes the pilot inactive in the roster. The associated `SystemUser` (login) is intentionally left untouched.
- This use case is exclusive to the **ATCC** — the Admin does not have access.

---

## 2. Analysis

### Actor
- Air Transport Company Collaborator (ATCC)

### System Sequence Diagram
![SSD US077](SSD_US077.svg)

### Domain Concepts Involved
- **Pilot** — `deactivate()` enforces the invariant that an already-inactive pilot cannot be deactivated again.
- **FlightPlan** — `FlightPlanStatus`: DRAFT, TESTED, VALIDATED, REJECTED, CANCELLED.
- **PilotRepository** — `findByPilotID(PilotID)`.
- **FlightPlanRepository** — `findByPilotID(PilotID)` to check assigned plans.

---

## 3. Design

### Sequence Diagram
![SD US077](SD_US077.svg)

### Class Diagram
![CD US076/077](../us_076/CD_US076_US077.svg)

### Design Decisions
- Business rules (ownership check + assigned plans check) live in `PilotService.removePilot()`, keeping the controller thin — mirrors `AircraftService.decommissionAircraft()`.
- `FlightPlanRepository` is passed as a parameter to `PilotService.removePilot()` so the service does not permanently depend on a repository it only needs for this rule.
- The package-private `removePilot(pilotID, companyIataCode)` in the controller lets tests bypass auth-based company resolution — same pattern as `DeactivateFlightRouteController`.
- All repositories (including `aircraftModels()`) are injected via the production constructor — no internal stubs.

### Classes Created / Modified

| Class | Layer | Action |
|---|---|---|
| `RemovePilotController` | Application | Created |
| `PilotService` | Application | Modified — added `removePilot`, `ensureNoAssignedFlightPlans` |
| `RemovePilotUI` | Presentation | Created |
| `AirTransportCompanyMenuUI` | Presentation | Modified (added menu item 9) |

---

## 4. Implementation

### Business Rule — Assigned Flight Plans

A flight plan blocks pilot removal if it is in an active workflow state (`DRAFT`, `TESTED`, `VALIDATED`). Plans in `REJECTED` or `CANCELLED` no longer count.

The pilot's status is set to `INACTIVE` via `pilot.deactivate()`; the `SystemUser` is not modified.

---

## 5. Tests

| Test | Description |
|---|---|
| `ensurePilotIsSuccessfullyRemoved` | Happy path — status INACTIVE |
| `ensureRemovedPilotIsPersistedAsInactive` | Repository reflects INACTIVE |
| `ensureRemoveSucceedsWithNoPlans` | No plans → allowed |
| `ensureRemoveSucceedsWithCancelledPlan` | CANCELLED → not blocking |
| `ensureRemoveSucceedsWithRejectedPlan` | REJECTED → not blocking |
| `ensureRemoveFailsForNonExistentPilot` | Unknown ID → `IllegalArgumentException` |
| `ensureRemoveFailsWhenPilotBelongsToAnotherCompany` | Wrong company → `IllegalArgumentException` |
| `ensureRemoveFailsWithDraftPlan` | DRAFT → `IllegalArgumentException` |
| `ensureRemoveFailsWithTestedPlan` | TESTED → `IllegalArgumentException` |
| `ensureRemoveFailsWithValidatedPlan` | VALIDATED → `IllegalArgumentException` |
| `ensureAlreadyInactivePilotCannotBeRemovedAgain` | Second removal → `IllegalArgumentException` |

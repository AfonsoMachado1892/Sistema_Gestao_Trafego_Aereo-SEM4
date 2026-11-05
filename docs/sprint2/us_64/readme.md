# US064 — Disable a Customer's Collaborator

## 1. Context

The goal is to allow a Backoffice Operator to disable a customer's collaborator so that they may no longer use the system.

---

## 2. Requirements

**US064** As a Backoffice Operator, I want to disable a customer's collaborator so that he may not use the system anymore.

**Acceptance Criteria:**

- US064.1 The Backoffice Operator must be able to select an active collaborator from the list.
- US064.2 The collaborator's current information must be displayed before confirmation.
- US064.3 The Backoffice Operator must explicitly confirm the disable action.
- US064.4 Upon confirmation, the collaborator's status must be set to DISABLED.
- US064.5 The associated SystemUser must also be deactivated so the collaborator can no longer log in.
- US064.6 Only authenticated users with the BACKOFFICE_OPERATOR or ADMIN role may access this functionality.
- US064.7 A success message must be shown upon successful disable.
- US064.8 Disabled collaborators must no longer appear in the active collaborators listing.

**Dependencies/References:**

- Depends on US030 (Authentication and Authorization).
- Depends on US061 (Add a Customer's Collaborator) — a collaborator must exist to be disabled.
- Depends on US062 (List Customer's Collaborators) — the listing of active collaborators is reused for selection.

---

## 3. Analysis

The domain model defines `CustomerCollaborator` as an Aggregate Root with a `CustomerStatus` field that can be either `ACTIVE` or `DISABLED`. The aggregate already exposes a `disable()` method and an `isActive()` predicate.

Key business rules identified:
- Disabling a collaborator sets their `CustomerStatus` to `DISABLED` via the `disable()` method on the aggregate.
- The associated `SystemUser` must also be deactivated via `UserManagementService.deactivateUser()` — this ensures the collaborator cannot log in anymore, satisfying the requirement "may not use the system anymore".
- Only active collaborators are shown for selection — disabled collaborators are already excluded by `findAllActive()`.
- The operation requires explicit confirmation from the Backoffice Operator before being executed.

### System Sequence Diagram (SSD)

![US064 SSD](analysis/US064_SSD.png)

---

## 4. Design

### 4.1. Realization

**Sequence Diagram (SD):**

![US064 SD](design/png/US064_SD.png)

**Class Diagram (CD):**

![US064 CD](design/png/US064_CD.png)

---

## 5. Implementation

The implementation follows the layered architecture promoted by the EAPLI Framework:

- **Domain:** `CustomerCollaborator` (Aggregate Root) — exposes `disable()` which sets `CustomerStatus` to `DISABLED`, and `isActive()` which returns `true` only when status is `ACTIVE`. Both methods already existed in the aggregate.
- **Application:** `DisableCustomerCollaboratorController` — enforces the `BACKOFFICE_OPERATOR` or `ADMIN` role, loads the collaborator by ID, calls `disable()` on the aggregate, saves it, and then calls `UserManagementService.deactivateUser()` on the associated `SystemUser`. This two-step approach (aggregate disable + system user deactivation) ensures both the business status and the authentication access are revoked.
- **Presentation:** `DisableCustomerCollaboratorUI` — lists active collaborators for selection, displays the selected collaborator's information, requests explicit confirmation, and delegates to the controller.

The `UserManagementService.deactivateUser()` method is obtained via `AuthzRegistry`, consistent with the approach used in `DisableEnableUserController`.

---

## 6. Integration/Demonstration

This US integrates with:
- **CustomerCollaborator aggregate** — `disable()` updates the status directly on the aggregate.
- **SystemUser aggregate (EAPLI)** — `UserManagementService.deactivateUser()` revokes login access.
- **Backoffice Menu** — `DisableCustomerCollaboratorUI` is accessible via `Backoffice Config > Customer Management > Disable Collaborator` in the backoffice console application.

---

## 7. Observations

- The `findAllActive()` method added to `CustomerCollaboratorRepository` in US063 is reused here — no additional repository changes are required.
- Disabling a collaborator does not delete any data — it is a soft disable that sets the status to `DISABLED` and revokes login access.
- The explicit confirmation step in the UI prevents accidental disabling of collaborators.
- Re-enabling a collaborator is out of scope for this user story.
# US063 — Edit a Customer's Collaborator

## 1. Context

The goal is to allow a Backoffice Operator to edit the contact information (email and phone number) of a customer's collaborator. All other information remains immutable after creation.

---

## 2. Requirements

**US063** As a Backoffice Operator, I want to edit the information (email and phone number) of a customer's collaborator. All other information cannot be changed.

**Acceptance Criteria:**

- US063.1 The Backoffice Operator must be able to select an active collaborator from the list.
- US063.2 Only the email address and phone number may be changed — all other fields (name, position, customer reference) are immutable.
- US063.3 The current contact information must be displayed before editing.
- US063.4 Leaving a field blank must preserve the current value.
- US063.5 Only authenticated users with the BACKOFFICE_OPERATOR or ADMIN role may access this functionality.
- US063.6 A success message must be shown upon successful update.
- US063.7 Invalid or incomplete input must produce clear and informative error messages.

**Dependencies/References:**

- Depends on US030 (Authentication and Authorization).
- Depends on US061 (Add a Customer's Collaborator) — a collaborator must exist to be edited.

---

## 3. Analysis

The domain model defines `CustomerCollaborator` as an Aggregate Root identified by `CustomerCollaboratorID`. The email is stored directly on the `CustomerCollaborator` aggregate, while the phone number is stored on the associated `AiSafeUser` aggregate. Both must be updated in the same operation.

Key business rules identified:
- Only email and phone number may be modified — all other fields are enforced as immutable by the aggregate.
- The `CustomerCollaborator` already exposes a `changeEmail()` method which enforces the email invariant via `Preconditions`.
- The phone number is stored on `AiSafeUser` (not on `CustomerCollaborator`) — updating it requires a separate save to the `AiSafeUserRepository`, consistent with the approach used in `AddCustomerCollaboratorController`.
- The `AiSafeUser` requires a new `changePhoneNumber()` method to be added to the aggregate.

### System Sequence Diagram (SSD)

![US063 SSD](analysis/US063_SSD.png)

---

## 4. Design

### 4.1. Realization

**Sequence Diagram (SD):**

![US063 SD](design/png/US063_SD.png)

**Class Diagram (CD):**

![US063 CD](design/png/US063_CD.png)


---

## 5. Implementation

The implementation follows the layered architecture promoted by the EAPLI Framework:

- **Domain:** `CustomerCollaborator` (Aggregate Root) — exposes `changeEmail()` to update the email while enforcing the invariant via `Preconditions`. `AiSafeUser` (Aggregate Root) — extended with `changePhoneNumber()` to update the phone number.
- **Application:** `EditCustomerCollaboratorController` — enforces the `BACKOFFICE_OPERATOR` or `ADMIN` role, loads the collaborator by ID, calls `changeEmail()` on the `CustomerCollaborator`, and calls `changePhoneNumber()` on the `AiSafeUser`. Both aggregates are saved separately to their respective repositories.
- **Presentation:** `EditCustomerCollaboratorUI` — lists active collaborators for selection, displays current contact info, collects new values (blank preserves current), and delegates to the controller.

The two-repository save pattern (one for `CustomerCollaborator`, one for `AiSafeUser`) is consistent with the approach already established in `AddCustomerCollaboratorController`, which also performs two separate saves when creating a collaborator.

---

## 6. Integration/Demonstration

This US integrates with:
- **CustomerCollaborator aggregate** — `changeEmail()` updates the email directly on the aggregate.
- **AiSafeUser aggregate** — `changePhoneNumber()` updates the phone number on the associated user.
- **Backoffice Menu** — `EditCustomerCollaboratorUI` is accessible via `Backoffice Config > Customer Management > Edit Collaborator` in the backoffice console application.

---

## 7. Observations

- The email is stored on `CustomerCollaborator` and the phone on `AiSafeUser` — this split follows the design established in US061 and must be respected here.
- The `changePhoneNumber()` method was added to `AiSafeUser` following the same pattern as the existing `changeEmail()` on `CustomerCollaborator`.
- The `findAllActive()` and `findById()` methods were added to `CustomerCollaboratorRepository` and implemented in `InMemoryCustomerCollaboratorRepository` using the existing `match()` and `matchOne()` patterns from the EAPLI `InMemoryDomainRepository`.
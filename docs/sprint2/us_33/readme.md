# US033 — List Backoffice Users

## 1. Context

The goal is to allow an Administrator to list all backoffice users registered in the system, including their current status (active or disabled).

---

## 2. Requirements

**US033** As Administrator, I want to be able to list the users of the backoffice, including their status.

**Acceptance Criteria:**

- US033.1 The Administrator must be able to view all backoffice users regardless of their status.
- US033.2 Each user entry must display at minimum: name, email and current status (active/disabled).
- US033.3 Only authenticated users with the ADMIN role may access this functionality.
- US033.4 If no users exist, an empty list is shown with no errors.
- US033.5 Both active and disabled users must appear in the listing.

**Dependencies/References:**

- Depends on US030 (Authentication and Authorization) — only authenticated Administrators may invoke this feature.
- Depends on US031 (Register Users) — users must exist in the system to be listed.

---

## 3. Analysis

The `SystemUser` aggregate (provided by the EAPLI Framework) already contains all information required by this user story: name, email and active status. No new domain concepts are introduced.

Key business rules identified:
- The listing includes **all** users regardless of status — both active and disabled.
- Authorization is enforced before any data access — only the `ADMIN` role may invoke this feature.
- The `ListUsersController` delegates to the EAPLI `UserManagementService.allUsers()` method, which returns all registered system users.

### System Sequence Diagram (SSD)

![US033 SSD](analysis/US033_SSD.png)

---

## 4. Design

### 4.1. Realization

**Sequence Diagram (SD):**

![US033 SD](design/png/US033_SD.png)

**Class Diagram (CD):**

![US033 CD](design/png/US033_CD.png)


---

## 5. Implementation

The implementation follows the layered architecture promoted by the EAPLI Framework:

- **Domain:** `SystemUser` (EAPLI Aggregate Root) — provides `name()`, `email()` and `isActive()` used for display.
- **Application:** `ListUsersController` — enforces the `ADMIN` role via `AuthorizationService` and delegates to `UserManagementService.allUsers()` to retrieve all users.
- **Presentation:** `ListUsersUI` — renders a formatted console table with columns for name, email and status. Displays the total count of users at the bottom.

The controller already existed in the codebase (`ListUsersController.java`) and required no modification — only the UI was created as a new class.

The `UserManagementService` is obtained via `AuthzRegistry`, consistent with the approach used in all other user management controllers in the project (e.g. `AddUserController`, `DisableEnableUserController`).

---

## 6. Integration/Demonstration

This US integrates with:
- **User Management aggregate** — reads `SystemUser` instances via `UserManagementService`.
- **Backoffice Menu** — `ListUsersUI` is accessible via `Backoffice Config > User Management > List Users` in the backoffice console application.

---

## 7. Observations

- The `ListUsersController` was already present in the codebase. This US only required the creation of `ListUsersUI`.
- The listing does not support filtering or pagination in this sprint — a simple console table is sufficient per the requirements.
- The `UserManagementService.allUsers()` method returns all users regardless of status, satisfying AC033.1 and AC033.5 without any additional repository queries.
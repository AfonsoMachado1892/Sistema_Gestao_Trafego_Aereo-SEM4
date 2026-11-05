# US032 — Disable / Enable Backoffice Users

## 1. Context

The goal of this user story is to allow an Administrator to disable or enable backoffice users of the AISafe system.

Disabling a user prevents authentication and access to system functionalities, while enabling restores access. This supports operational control over who may use the backoffice without deleting user records.

---

## 2. Requirements

**US032** As an Administrator, I want to be able to disable/enable users of the backoffice.

**Acceptance Criteria:**

- US032.1 Only an Administrator may disable or enable backoffice users.
- US032.2 The Administrator must be able to list all users and see their current status (active/disabled).
- US032.3 The Administrator must be able to disable an active user.
- US032.4 The Administrator must be able to enable a previously disabled user.
- US032.5 A success message must be shown after a successful operation.
- US032.6 Invalid operations must produce clear and informative error messages.

**Dependencies/References:**

- Depends on **US030 Authentication and Authorization**.
- Depends on **US031 Register Users**, since users must exist before they can be disabled or enabled.
- Complements **US033 List Users**.

---

## 3. Analysis

User lifecycle management (activation/deactivation) is handled by the EAPLI Framework `UserManagementService`, which persists `SystemUser` entities through `UserRepository`.

The AISafe application layer exposes this capability through `DisableEnableUserController`, enforcing that only users with the `ADMIN` role may perform the operation.

Key business rules identified:

- Only Administrators may disable or enable users.
- A disabled user cannot authenticate into the system.
- An enabled user regains the ability to authenticate according to assigned roles.
- The operation toggles the status of an existing user; it does not delete the user.

### System Sequence Diagram (SSD)

![US032 SSD](analysis/US032_SSD.png)

---

## 4. Design

### 4.1. Realization

**Sequence Diagram (SD):**

![US032 SD](design/US032_SD.png)

**Class Diagram (CD):**

![US032 CD](design/US032_CD.png)

### 4.2. Acceptance Tests

**Test 1:** Verifies that all users can be listed.

**Refers to Acceptance Criteria:** US032.2

```java
@Test
void ensureAllUsersAreListed() {
    final List<SystemUser> users = new ArrayList<>();
    controller.allUsers().forEach(users::add);
    assertEquals(1, users.size());
}
```

**Test 2:** Verifies that an active user can be disabled.

**Refers to Acceptance Criteria:** US032.3

```java
@Test
void ensureUserIsDisabledSuccessfully() {
    final SystemUser disabled = controller.disableUser(activeUser);
    assertFalse(disabled.isActive());
}
```

**Test 3:** Verifies that a disabled user can be enabled again.

**Refers to Acceptance Criteria:** US032.4

```java
@Test
void ensureDisabledUserCanBeEnabledAgain() {
    final SystemUser disabled = controller.disableUser(activeUser);
    final SystemUser enabled = controller.enableUser(disabled);
    assertTrue(enabled.isActive());
}
```

---

## 5. Implementation

The implementation follows the layered architecture promoted by the EAPLI Framework.

- **Application:**
  - `DisableEnableUserController` handles authorization and delegates to `UserManagementService`.

- **Presentation:**
  - `DisableEnableUserUI` lists users with status and toggles disable/enable on selection.
  - Available in the Backoffice Config menu under Administrator options.

- **Infrastructure (EAPLI):**
  - `UserManagementService` performs `deactivateUser` and `activateUser`.
  - `AuthorizationService` enforces the `ADMIN` role.

---

## 6. Integration/Demonstration

This US integrates with:

- **Backoffice Menu** — option "Disable / Enable Backoffice User" in `BackofficeConfigUI` (admin section).
- **US031 Register Users** — users must be registered before they can be managed.
- **Login** — disabled users cannot start a valid session.

---

## 7. Observations

- User activation state is managed by the EAPLI Framework; AISafe does not introduce a separate domain aggregate for this concern.
- The UI toggles status in a single screen: if the selected user is active, it is disabled; otherwise, it is enabled.
- Tests use repository stubs and a test-specific `AuthorizationService` subclass to avoid depending on an authenticated session.

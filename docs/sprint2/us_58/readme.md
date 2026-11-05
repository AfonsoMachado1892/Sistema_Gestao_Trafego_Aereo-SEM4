# US058 — Remove an Engine Model from an Aircraft Model

## 1. Context

The goal of this user story is to allow a Backoffice Operator to remove a certified engine model from an existing aircraft model.

This supports maintenance of the certified engine list when an engine variant is no longer offered for a given aircraft model, while preserving domain invariants.

---

## 2. Requirements

**US058** As a Backoffice Operator, I want to remove an engine model from an aircraft model's list of certified engines. This is not possible if there are actual aircraft using that model. An aircraft model must retain at least one certified engine model.

**Acceptance Criteria:**

- US058.1 The aircraft model must exist in the system.
- US058.2 The engine model must be certified for the selected aircraft model.
- US058.3 The aircraft model must retain at least one certified engine model after removal.
- US058.4 Removal must be rejected if any aircraft is registered using the aircraft model.
- US058.5 A success message must be shown upon successful removal.
- US058.6 Invalid or incomplete input must produce clear and informative error messages.

**Dependencies/References:**

- Depends on **US055 Create an Aircraft Model**.
- Depends on **US056 Create an Engine Model**.
- Related to **US057 Add an Engine Model to an Aircraft Model**.
- Related to **US070 Add an Aircraft to an Air Transport Company** (aircraft existence check).

---

## 3. Analysis

The `AircraftModel` aggregate maintains a set of certified `EngineModelID` values. Removal is performed through `removeCertifiedEngine()`, which enforces aggregate invariants.

Cross-aggregate rules are coordinated by `AircraftModelService`:

- The aircraft model must exist.
- No `Aircraft` may reference the aircraft model (`AircraftRepository.existsByAircraftModel`).
- The engine model must be certified and must not be the last certified engine.

### System Sequence Diagram (SSD)

![US058 SSD](analysis/US058_SSD.png)

---

## 4. Design

### 4.1. Realization

**Sequence Diagram (SD):**

![US058 SD](design/US058_SD.png)

**Class Diagram (CD):**

![US058 CD](design/US058_CD.png)

### 4.2. Acceptance Tests

**Test 1:** Verifies successful removal when invariants hold.

**Refers to Acceptance Criteria:** US058.2, US058.3, US058.5

```java
@Test
void ensureEngineModelIsRemovedSuccessfully() {
    final AircraftModel result = service.removeEngineModel(
            AircraftModelID.of("A320"), EngineModelID.of("CFM56-5B"));
    assertFalse(result.hasCertifiedEngine(EngineModelID.of("CFM56-5B")));
}
```

**Test 2:** Verifies removal is blocked when aircraft use the model.

**Refers to Acceptance Criteria:** US058.4

```java
@Test
void ensureCannotRemoveEngineModelWhenAircraftExistUsingModel() {
    assertThrows(IllegalStateException.class, () ->
            service.removeEngineModel(AIRCRAFT_MODEL_ID, ENGINE_MODEL_ID));
}
```

**Test 3:** Verifies the last certified engine cannot be removed.

**Refers to Acceptance Criteria:** US058.3

```java
@Test
void ensureCannotRemoveLastCertifiedEngine() {
    assertThrows(IllegalArgumentException.class, () ->
            model.removeCertifiedEngine(EngineModelID.of("CFM56-5B")));
}
```

---

## 5. Implementation

- **Domain:**
  - `AircraftModel.removeCertifiedEngine()` enforces aggregate invariants.
  - `AircraftModelService.removeEngineModel()` coordinates repository access and aircraft existence validation.

- **Repository:**
  - `AircraftRepository.existsByAircraftModel()` added to support US058.4.

- **Application:**
  - `RemoveEngineModelFromAircraftModelController` delegates to `AircraftModelService`.

- **Presentation:**
  - `RemoveEngineModelFromAircraftModelUI` guides selection, confirmation, and error display.
  - Available in `BackofficeConfigUI` under Backoffice Operator options.

---

## 6. Integration/Demonstration

This US integrates with:

- **US057** — symmetric operation (add certified engine).
- **US070** — aircraft registered for a model block engine removal.
- **Bootstrap** — initial aircraft models created with certified engines during bootstrap.

---

## 7. Observations

- Removal validates aircraft usage at the **aircraft model** level, not per engine variant, as required by the specification.
- The UI prevents selection when only one certified engine remains, providing early feedback before calling the controller.
- `AircraftModelService` accepts an optional `AircraftRepository`; when null, the aircraft existence rule is skipped (used only where appropriate, e.g. tests without aircraft data).

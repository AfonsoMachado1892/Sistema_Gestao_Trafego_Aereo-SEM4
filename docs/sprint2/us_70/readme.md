# US070 — Add an Aircraft to an Air Transport Company's Fleet

## 1. Context

The goal is to allow an Air Transport Company Collaborator to register a new aircraft in their company's fleet.

---

## 2. Requirements

**US070** As an Air Transport Company Collaborator, I want to add an aircraft to my company's fleet.

**Acceptance Criteria:**

- US070.1 The ATCC must be able to register a new aircraft for their own company.
- US070.2 Required information includes: aircraft registration number, aircraft model, cabin configuration (number of seats per class), number of flight crew elements, and country of registration.
- US070.3 The aircraft registration number must be unique across the system.
- US070.4 The total number of seats across all classes must not exceed the maximum capacity defined by the aircraft model.
- US070.5 The aircraft model must exist in the system and belong to valid certified configurations.
- US070.6 The country of registration may differ from the company's home country.
- US070.7 The aircraft must be created with an active operational status.
- US070.8 A success message must be shown upon successful registration.
- US070.9 Invalid or incomplete input must produce clear and informative error messages.

**Dependencies/References:**

- Depends on the existence of at least one `AircraftModel` registered in the system (US055).
- Depends on the existence of at least one `AirTransportCompany` registered in the system (US060).
- Depends on the existence of a `CustomerCollaborator` of type `AIR_TRANSPORT_COMPANY` registered in the system (US061).

---

## 3. Analysis

The domain model defines `Aircraft` as an Aggregate Root identified by `AircraftRegistration`. It references both the `AircraftModel` and `AirTransportCompany` aggregates via `@ManyToOne` associations, following ORM best practices while respecting DDD aggregate boundaries.

Key business rules identified:
- An `Aircraft` registration number must be unique across the entire system.
- The total number of seats in the cabin configuration cannot exceed the `maxPassengerCapacity` of the aircraft model.
- The company is automatically inferred from the authenticated collaborator — the system identifies the `AirTransportCompany` via the `CustomerCollaborator` associated with the logged-in `SystemUser`.
- The aircraft is always created with `OperationalStatus.ACTIVE`.
- The country of registration is independent of the company's home country.

### System Sequence Diagram (SSD)

![US070 SSD](analysis/US070_SSD.png)

---

## 4. Design

### 4.1. Realization

**Sequence Diagram (SD):**

![US070 SD](design/png/US070_SD.png)

**Class Diagram (CD):**

![US070 CD](design/png/US070_CD.png)

### 4.2. Acceptance Tests

**Test 1:** Verifies that it is not possible to register an aircraft with a duplicate registration number.

**Refers to Acceptance Criteria:** US070.3

```java
@Test
void ensureAircraftCannotBeRegisteredWithDuplicateRegistration() {
    service.addAircraft("CS-TTV", aircraftModel, airTransportCompany,
            CabinConfiguration.of(150, 20, 0), country, 2);
    assertThrows(IllegalArgumentException.class, () ->
            service.addAircraft("CS-TTV", aircraftModel, airTransportCompany,
                    CabinConfiguration.of(150, 20, 0), country, 2));
}
```

**Test 2:** Verifies that it is not possible to register an aircraft whose cabin configuration exceeds the model's maximum capacity.

**Refers to Acceptance Criteria:** US070.4

```java
@Test
void ensureAircraftCannotBeRegisteredWhenCabinExceedsModelCapacity() {
    assertThrows(IllegalArgumentException.class, () ->
            service.addAircraft("CS-TTV", aircraftModel, airTransportCompany,
                    CabinConfiguration.of(300, 100, 50), country, 2));
}
```

**Test 3:** Verifies that an aircraft is always created with ACTIVE operational status.

**Refers to Acceptance Criteria:** US070.7

```java
@Test
void ensureAircraftIsCreatedWithActiveStatus() {
    final Aircraft aircraft = service.addAircraft("CS-TTV", aircraftModel,
            airTransportCompany, CabinConfiguration.of(150, 20, 0), country, 2);
    assertEquals(OperationalStatus.ACTIVE, aircraft.status());
}
```

**Test 4:** Verifies that it is not possible to register an aircraft with zero or negative crew elements.

**Refers to Acceptance Criteria:** US070.2

```java
@Test
void ensureAircraftCannotBeRegisteredWithZeroCrewElements() {
    assertThrows(IllegalArgumentException.class, () ->
            new Aircraft(AircraftRegistration.of("CS-TTV"),
                    CabinConfiguration.of(150, 20, 0),
                    airTransportCompany, aircraftModel, country, 0));
}
```

**Test 5:** Verifies that it is not possible to register an aircraft with a cabin configuration with no seats.

**Refers to Acceptance Criteria:** US070.2

```java
@Test
void ensureAircraftCannotBeRegisteredWithEmptyCabinConfiguration() {
    assertThrows(IllegalArgumentException.class, () ->
            CabinConfiguration.of(0, 0, 0));
}
```

---

## 5. Implementation

The implementation follows the layered architecture promoted by the EAPLI Framework:

- **Domain:** `Aircraft` (Aggregate Root), `AircraftRegistration`, `CabinConfiguration`, `OperationalStatus` — enforce all business invariants via `Preconditions`.
- **Domain Service:** `AircraftService` coordinates cross-aggregate validation (registration uniqueness, cabin capacity) before creating the aircraft. This logic cannot live inside `Aircraft` itself as aggregates must remain persistence-ignorant.
- **Factory:** `AircraftFactory` is responsible for constructing `Aircraft` instances, following the DDD Factory pattern. Justified by the complexity of the creation process involving multiple aggregates.
- **Repository:** `AircraftRepository` interface in the domain; `InMemoryAircraftRepository` in the infrastructure layer.
- **Application:** `AddAircraftController` orchestrates authorization, resolves the `AirTransportCompany` from the authenticated collaborator, and delegates to the domain service.
- **Presentation:** `AddAircraftUI` handles user interaction via console, listing available aircraft models and collecting cabin configuration per class.

The company is automatically inferred from the authenticated `SystemUser` — the controller queries the `CustomerCollaboratorRepository` to find the collaborator and then resolves the `AirTransportCompany` via the `customerCode`. This ensures the ATCC can only add aircraft to their own company's fleet.

The `PersistenceContext` provides access to all repositories, enabling the controller to remain decoupled from persistence details (NFR08 — database by configuration).

---

## 6. Integration/Demonstration

This US integrates with:
- **AircraftModel aggregate** — the `AircraftService` validates the model's capacity before creating the aircraft.
- **AirTransportCompany aggregate** — the aircraft is automatically associated with the company of the authenticated collaborator.
- **CustomerCollaborator aggregate** — the controller resolves the company from the authenticated `SystemUser` via the `CustomerCollaboratorRepository`.
- **Air Transport Company Menu** — `AddAircraftUI` is accessible via `Air Transport Company Menu > Add Aircraft to Fleet` in the backoffice console application.

---

## 7. Observations

- The `AirTransportCompany` is inferred automatically from the authenticated collaborator, following the principle of least privilege — the ATCC can only register aircraft for their own company.
- The `CabinConfiguration` value object exposes the `exceedsCapacity()` method, following the Information Expert and Tell, Don't Ask principles — the cabin configuration itself knows whether it exceeds a given capacity.
- The `OperationalStatus` is always set to `ACTIVE` upon creation and is managed exclusively by the aggregate — no external actor can set it to `DECOMMISSIONED` directly (that is the responsibility of US071).
- Cross-aggregate references to `AircraftModel` and `AirTransportCompany` are handled via `@ManyToOne` JPA associations, consistent with the approach used in the `Airport` → `AirControlArea` relationship established in US052.
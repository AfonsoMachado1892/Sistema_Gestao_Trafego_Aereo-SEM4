# US073 — Create a Flight Route

## 1. Context

The goal is to allow an Air Transport Company Collaborator (ATCC) to register a new flight route for their company.

---

## 2. Requirements

**US073** As an Air Transport Company Collaborator, I want to create a flight route for my company.

**Acceptance Criteria:**

- US073.1 The route code must follow the format: 2 uppercase letters (matching the company's IATA code) followed by 1 to 4 digits (e.g. `TP123`).
- US073.2 The route code prefix must match the authenticated collaborator's company IATA code.
- US073.3 The route code must be unique across all flight routes.
- US073.4 The departure and arrival airports must exist in the system and must be different.
- US073.5 The flight type must be either REGULAR or CHARTER.
- US073.6 A REGULAR route requires at least one `ScheduleEntry` (day of week + departure time).
- US073.7 A CHARTER route requires an explicit departure and arrival date/time, with arrival after departure.
- US073.8 The system must resolve the company IATA code automatically from the authenticated collaborator — no manual input required.
- US073.9 This must also be achievable by a bootstrap process.
- US073.10 A success message with the created route data must be shown upon success.
- US073.11 Invalid or incomplete input must produce clear and informative error messages.

---

## 3. Analysis

The domain model defines `FlightRoute` as an Aggregate Root identified by `RouteCode`. The schedule is modelled as an abstract `Schedule` with two concrete subtypes: `RegularSchedule` (recurring, based on `ScheduleEntry` entries) and `CharterSchedule` (one-off, with explicit departure and arrival timestamps).

### System Sequence Diagram (SSD)

![US073 SSD](US073_SSD.png)

---

## 4. Design

### 4.1. Realization

**Sequence Diagram (SD):**

![US073 SD](US073_SD.png)

**Class Diagram (CD):**

![US073 CD](US073_CD.png)

### 4.2. Acceptance Tests

**Test 1:** Verifies that a regular flight route is created successfully with valid data.

**Refers to Acceptance Criteria:** US073.1, US073.4, US073.6

```java
@Test
void ensureRegularFlightRouteIsCreatedSuccessfully() {
    final FlightRouteDTO result = controller.createRegularFlightRoute(
            "TP101", "LIS", "OPO", "TP",
            RegularSchedule.of(List.of(
                    ScheduleEntry.of(DayOfWeek.MONDAY, LocalTime.of(9, 0)))));
    assertNotNull(result);
    assertEquals("TP101", result.routeCode);
    assertEquals("ACTIVE", result.routeStatus);
}
```

**Test 2:** Verifies that a route code with a prefix that does not match the company IATA is rejected.

**Refers to Acceptance Criteria:** US073.2

```java
@Test
void ensureRouteCodePrefixMismatchIsRejected() {
    assertThrows(IllegalArgumentException.class, () ->
            controller.createRegularFlightRoute(
                    "FR101", "LIS", "OPO", "TP", regularSchedule()));
}
```

**Test 3:** Verifies that a duplicate route code is rejected.

**Refers to Acceptance Criteria:** US073.3

```java
@Test
void ensureDuplicateRouteCodeIsRejected() {
    controller.createRegularFlightRoute("TP101", "LIS", "OPO", "TP", regularSchedule());
    assertThrows(IllegalStateException.class, () ->
            controller.createRegularFlightRoute("TP101", "LIS", "OPO", "TP", regularSchedule()));
}
```

**Test 4:** Verifies that the same airport for departure and arrival is rejected.

**Refers to Acceptance Criteria:** US073.4

```java
@Test
void ensureSameAirportForDepartureAndArrivalIsRejected() {
    assertThrows(IllegalArgumentException.class, () ->
            controller.createRegularFlightRoute("TP101", "LIS", "LIS", "TP", regularSchedule()));
}
```

**Test 5:** Verifies that a charter route with arrival before departure is rejected.

**Refers to Acceptance Criteria:** US073.7

```java
@Test
void ensureCharterRouteWithArrivalBeforeDepartureIsRejected() {
    assertThrows(IllegalArgumentException.class, () ->
            CharterSchedule.of(
                    LocalDateTime.of(2026, 6, 15, 11, 0),
                    LocalDateTime.of(2026, 6, 15, 9, 0)));
}
```

---

## 5. Implementation

The implementation follows the layered architecture promoted by the EAPLI Framework:

- **Domain:** `FlightRoute` (Aggregate Root), `RouteCode`, `RouteStatus`, `FlightType`, `Schedule`, `RegularSchedule`, `CharterSchedule`, `ScheduleEntry` — enforce all business invariants via `Preconditions`.
- **Domain Service:** `FlightRouteService` coordinates cross-aggregate validation (route code uniqueness, airport existence) before creating the route. Uniqueness cannot live inside `FlightRoute` as aggregates must remain persistence-ignorant.
- **Factory:** `FlightRouteFactory` builds `FlightRoute` instances, separating construction logic from the aggregate.
- **Repository:** `FlightRouteRepository` interface in the domain; `JpaFlightRouteRepository` and `InMemoryFlightRouteRepository` in the infrastructure layer.
- **Application:** `CreateFlightRouteController` orchestrates authorization, resolves the company from the authenticated collaborator, and delegates to the domain service.
- **Presentation:** `CreateFlightRouteUI` handles user interaction via console, listing airports, collecting schedule details, and displaying a summary before confirmation.
- **Bootstrap:** `FlightRouteBootstrapper` creates initial routes on system startup, satisfying US073.9.

The `RouteCode` value object enforces the format `^[A-Z]{2}\d{1,4}$` internally. The `FlightRoute` constructor enforces that the route code prefix matches the company IATA code, ensuring the invariant is respected even when the factory is bypassed.

`RegularSchedule` uses `@ElementCollection` with `@CollectionTable(name="FLIGHT_ROUTE_ENTRIES")` to persist its `ScheduleEntry` list. `CharterSchedule` is `@Embeddable` with two `LocalDateTime` fields. Only the schedule relevant to the `FlightType` is populated; the other is `null`.

---

## 6. Integration

This US integrates with:

- **Airport aggregate** — `FlightRouteService` validates airport existence via `AirportRepository.findByIataCode()`.
- **CustomerCollaborator aggregate** — `CreateFlightRouteController.currentCompanyIataCode()` resolves the company via `CustomerCollaboratorRepository.findBySystemUser()`.

---

## 7. Observations

- The `FlightRouteService` also exposes `listActiveRoutesByCompany()` and `listCompanyRoutes()` to support US074 and future list US.
- The `deactivationDate` field is nullable — a route with no deactivation date is considered permanently active.
- The `effectiveStatus()` method returns the computed status based on the deactivation date, while `routeStatus` holds the stored status. This separation allows the UI to display the effective status without requiring a database update on every check.
- A `FlightPlanRepository` dependency was added to `FlightRouteService` to enforce the invariant that a route with active flight plans cannot be deactivated (US074). This is injected via the controller constructor and passed to the service.
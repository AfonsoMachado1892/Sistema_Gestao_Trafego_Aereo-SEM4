# US080 — Create Flight Plan

## 1. Context

The goal of this user story is to allow an authenticated Pilot to register a new flight plan for an existing flight route.

A flight plan represents the operational plan for a concrete flight occurrence. It is created in `DRAFT` status and must later undergo validation and testing before it can be used to create or execute an actual flight.

The flight route defines the commercial and operational route, including its origin, destination, owning air transport company, route type, and allowed schedule.

The flight plan references the route and defines the concrete planned departure and arrival times, aircraft, authenticated pilot, fuel quantity, and operational description.

---

## 2. Requirements

**US080** — As a Pilot, I want to register a flight plan for a route.

The Pilot must provide:

* route;
* aircraft;
* planned departure date/time;
* planned arrival date/time;
* fuel quantity.

The assigned pilot is the authenticated Pilot.

The pilot must belong to the route’s company.

The aircraft must belong to the route’s company.

The selected aircraft must not be retired or decommissioned.

The flight plan status must be automatically set to `DRAFT` when created.

The flight plan must later undergo a multi-step validation process before it can be approved or used operationally.

---

## 3. Related Requirements

### 3.1. Flight Plans

A flight must have a valid flight plan to take place.

A flight takes place in the context of a route. Therefore, a flight can be understood as an instantiation of a route, including flight-specific information such as date/time, aircraft and load.

The flight plan describes how the flight takes place, including the fuel load.

Flight plans are described using a DSL. The DSL is imported, validated and processed by the system and serves as formal input for flight plan creation and validation.

### 3.2. Create Flight Plan from File

This requirement is related but different from US080.

Creating a flight plan from a file requires reading and validating a file according to the Core Flight DSL.

This includes:

* file format validation;
* lexical analysis;
* syntactic analysis;
* semantic analysis;
* meaningful error messages;
* ensuring that only valid flight plans may be imported and used.

US080 does not perform this validation.

### 3.3. Insert Weather Data in a Flight Plan

A later user story allows a Pilot to add weather data to one of their flight plans.

If the flight plan has already been tested, adding weather data makes the previous test result void.

Weather data should already exist in the system and should be selected by the Pilot, instead of being manually inserted line by line.

### 3.4. Test / Validate Flight Plan

A later user story validates and tests a flight plan.

The validation must include:

* validation of the flight plan described using the DSL;
* execution of the test component;
* implementation of the test component in C.

---

## 4. Client Specifications and Clarifications

During clarification with the client, the following additional rules and interpretations were identified.

### 4.1. Direction of the Route / FlightPlan Relationship

Although US080 states that the Pilot wants to add a flight plan to a route, this must not be interpreted as the `FlightRoute` aggregate owning a collection of flight plans.

The correct interpretation is:

* a flight plan knows/references its route;
* the route does not manage flight plans directly.

This keeps coupling low and cohesion high.

### 4.2. Flight, Route and FlightPlan

A flight is an instantiation of a route.

A flight includes flight-specific information such as:

* date/time;
* aircraft;
* load;
* operational data.

The flight plan describes how the flight takes place.

In the adopted design:

* `FlightRoute` defines the route and its allowed schedule;
* `FlightPlan` represents the operational plan for one concrete occurrence of that route;
* `Flight` represents the execution of a validated flight plan.

### 4.3. FlightPlan Lifecycle

A flight plan has its own lifecycle.

It is:

1. created as `DRAFT`;
2. validated through the DSL-related validation process;
3. tested;
4. eventually validated or rejected;
5. possibly affected by weather data insertion;
6. possibly cancelled;
7. kept in history after being used;
8. possibly reused as a basis for new plans.

This supports the decision to model `FlightPlan` as an Aggregate Root.

### 4.4. Multiple Flight Plans / Resubmission

The client clarified that if a flight plan is rejected during lexical analysis, DSL validation, or testing, the Pilot may need to submit a new one.

Therefore, the design must allow multiple flight plan submissions or new flight plans related to the same operational intention.

There is no explicit user story for resubmission, but the model should not prevent it.

### 4.5. US080 Does Not Validate the DSL

In US080, the system does not need to read or validate the flight plan description line by line.

The flight plan description may exist as raw text, but US080 does not perform lexical, syntactic, or semantic validation.

DSL validation belongs to later LPROG-related user stories.

### 4.6. US080 and File Import Are Different

US080 creates a draft flight plan manually through guided input.

Creating or importing a flight plan from a file is a different use case and requires validating the flight plan description according to the DSL.

Therefore, US080 must not be implemented as if it were the file import user story.

### 4.7. Aircraft Must Belong to the Route Company

The client clarified that it makes sense to enforce that the aircraft belongs to the company that owns the route.

Code sharing should be ignored.

Therefore:

* aircraft company must equal route company;
* aircraft from another company must be rejected.

### 4.8. Pilot Must Belong to the Route Company

US080 explicitly states that the pilot must belong to the route’s company.

Therefore:

* the authenticated Pilot must be active;
* the authenticated Pilot must belong to the same air transport company as the selected route.

### 4.9. Draft Plans Are Only Drafts

Draft flight plans must not be treated as valid operational plans.

Only validated flight plans may be considered ready for later operational use, such as creating or executing a flight.

### 4.10. Weather Data

For the weather data user story, the client clarified that it would be strange and burdensome to ask the Pilot to manually insert weather data.

Weather data should already exist in the system.

Therefore, the Pilot should select the weather information to use in the flight plan.

There may be multiple weather data providers, meaning there may be multiple weather data records for the same area and time.

### 4.11. Fuel Quantity

The client clarified that fuel quantity is used for simulation and should not be duplicated unnecessarily across different domain concepts.

Fuel quantity belongs to the flight plan / flight plan description.

For US080, fuel quantity must be provided and must be strictly positive.

### 4.12. Planned Departure and Arrival Times

The client clarified that departure time may exist in the DSL description and also as a frequently used property of the flight / flight plan.

Duplicating the first leg’s departure time as a property can be acceptable, provided that later validation detects inconsistencies.

Therefore, in this design, the flight plan stores concrete planned departure and arrival times.

Future validation, especially the flight plan validation user story, should detect inconsistencies between these stored times and the DSL description.

---

## 5. Analysis

The central domain concept for US080 is `FlightPlan`.

The flight plan is created for an existing `FlightRoute`.

The `FlightRoute` defines:

* route code;
* origin airport;
* destination airport;
* owning air transport company;
* route status;
* route deactivation date;
* route type;
* route schedule.

The `FlightPlan` defines:

* flight plan identity;
* referenced route code;
* aircraft registration;
* pilot identity;
* planned departure date/time;
* planned arrival date/time;
* fuel quantity;
* flight plan description;
* status.

The `FlightPlan` does not directly reference the `FlightRoute`, `Aircraft`, or `Pilot` aggregate objects.

Instead, it stores their identities:

* `RouteCode`;
* `AircraftRegistration`;
* `PilotID`.

This follows DDD principles for cross-aggregate references.

Cross-aggregate business rules are validated in the application/service layer.

---

## 6. Domain Decisions

### 6.1. FlightPlan as Aggregate Root

`FlightPlan` is modeled as an Aggregate Root because it has its own lifecycle:

* draft creation;
* validation;
* testing;
* weather data updates;
* cancellation;
* history;
* possible reuse as a basis for new plans.

### 6.2. FlightRoute Owns FlightType and Route Schedule

`FlightType` and route-level schedule belong to `FlightRoute`.

This avoids forcing the Pilot to repeatedly define whether each flight plan is regular or charter.

A regular route is regular because the route itself has a recurring schedule.

A charter route is charter because the route itself has a specific scheduled time.

Therefore:

* `FlightRoute` owns `FlightType`;
* `FlightRoute` owns either `RegularSchedule` or `CharterSchedule`;
* `FlightPlan` only stores concrete planned departure and arrival times.

### 6.3. FlightPlan Represents One Concrete Occurrence

A `FlightPlan` represents a concrete operational occurrence of a route.

Therefore, even when the route is regular, the flight plan stores a concrete:

* planned departure date/time;
* planned arrival date/time.

For a regular route, the planned departure must be compatible with the route’s regular schedule.

For a charter route, the planned departure must be compatible with the route’s charter schedule.

This validation is performed through:

```java
route.allowsDepartureAt(plannedDepartureDateTime)
```

### 6.4. Route Activity and Deactivation Date

A flight route may have a future deactivation date.

A route with a future deactivation date should remain active until that date.

Therefore, a flight plan can only be created if the route is active on the planned departure date.

A route should be considered unavailable for a flight plan if:

* it is already inactive;
* its deactivation date is on or before the planned departure date.

This means US080 should validate route availability using the planned departure date, not only the current system date.

### 6.5. Flight Is Not Created by US080

US080 creates a `FlightPlan` in `DRAFT`.

A `Flight` represents execution and should only be created from a validated flight plan.

Therefore:

* US080 creates `FlightPlan`;
* a later user story validates/tests `FlightPlan`;
* a later operation may create `Flight` from a validated `FlightPlan`.

### 6.6. FlightPlanID

`FlightPlanID` is generated automatically by the system.

This is a design decision, not an explicit requirement.

The Pilot does not provide the technical identity of the aggregate.

### 6.7. FlightPlanDescription in US080

In US080, the Pilot does not manually write or import the full DSL description.

The system generates a raw flight plan description automatically from the manually provided fields.

This keeps US080 focused on guided draft creation and avoids mixing it with DSL import/validation responsibilities.

DSL validation is intentionally left to the appropriate later user stories.

---

## 7. Acceptance Criteria

### US080.1 — Create Draft Flight Plan

Given an authenticated Pilot, when the Pilot provides valid flight plan data, the system creates a new `FlightPlan` with status `DRAFT`.

### US080.2 — Existing Route

The selected route must exist in the system.

If the route does not exist, the flight plan creation must be rejected.

### US080.3 — Route Active on Planned Departure Date

The selected route must be active on the planned departure date.

If the route is already inactive, or if its deactivation date is on or before the planned departure date, the flight plan creation must be rejected.

### US080.4 — Route Schedule Compatibility

The planned departure date/time must be allowed by the route schedule.

For regular routes, the departure must match one of the recurring schedule entries.

For charter routes, the departure must match the charter schedule.

If the planned departure is not allowed by the route, the flight plan creation must be rejected.

### US080.5 — Authenticated Pilot

The system must identify the authenticated user as a Pilot.

If the authenticated user is not a Pilot, the operation must be rejected.

### US080.6 — Active Pilot

The Pilot must be active.

If the Pilot is inactive, the operation must be rejected.

### US080.7 — Pilot Belongs to Route Company

The Pilot must belong to the same air transport company as the selected route.

If the Pilot does not belong to the route’s company, the operation must be rejected.

### US080.8 — Existing Aircraft

The selected aircraft must exist in the system.

If the aircraft does not exist, the flight plan creation must be rejected.

### US080.9 — Operational Aircraft

The selected aircraft must be operational.

If the aircraft is decommissioned or otherwise not operational, the operation must be rejected.

### US080.10 — Aircraft Belongs to Route Company

The selected aircraft must belong to the same air transport company as the selected route.

If the aircraft belongs to another company, the operation must be rejected.

### US080.11 — Fuel Quantity

Fuel quantity must be provided and must be strictly positive.

If fuel quantity is zero or negative, the operation must be rejected.

### US080.12 — Planned Times

Planned departure date/time must be provided.

Planned arrival date/time must be provided.

Planned arrival date/time must be after planned departure date/time.

### US080.13 — FlightPlan Description

The flight plan description is generated automatically from the manually provided fields.

US080 does not perform lexical, syntactic, or semantic validation of that description.

DSL validation is handled by other user stories.

### US080.14 — No Flight Creation

US080 must not create a `Flight`.

It only creates a `FlightPlan` in `DRAFT`.

### US080.15 — Clear Error Messages

Invalid input or failed business rules must produce clear and informative error messages.

---

## 8. Dependencies / References

This user story depends on existing data from:

* `FlightRoute`;
* `Aircraft`;
* `Pilot`;
* `AirTransportCompany`.

It also relates to:

* Create Flight Route;
* Deactivate Flight Route;
* Add Pilot;
* Remove Pilot;
* Create Flight Plan from File;
* Insert Weather Data in a Flight Plan;
* Test / Validate Flight Plan.

---

## 9. Design Overview

### 9.1. Aggregate Roots

The relevant Aggregate Roots are:

* `FlightPlan`;
* `FlightRoute`;
* `Aircraft`;
* `Pilot`.

### 9.2. Value Objects

Relevant value objects include:

* `FlightPlanID`;
* `RouteCode`;
* `AircraftRegistration`;
* `PilotID`;
* `FuelQuantity`;
* `FuelUnit`;
* `FlightPlanDescription`;
* `RegularSchedule`;
* `CharterSchedule`;
* `ScheduleEntry`.

### 9.3. Application / Service Layer Responsibilities

The application/service layer coordinates cross-aggregate validations:

* authenticated Pilot lookup;
* route existence;
* route activity on planned departure date;
* route schedule compatibility;
* pilot active status;
* pilot company compatibility;
* aircraft existence;
* aircraft operational status;
* aircraft company compatibility.

### 9.4. Domain Layer Responsibilities

The domain layer protects local invariants.

`FlightPlan` ensures:

* required fields are not null;
* status starts as `DRAFT`;
* planned arrival is after planned departure.

`FuelQuantity` ensures:

* amount is strictly positive;
* unit is valid.

`FlightRoute` ensures:

* route code is valid;
* route belongs to a company;
* departure and arrival airports are different;
* route type is compatible with the assigned schedule;
* `allowsDepartureAt(...)` encapsulates schedule validation;
* future deactivation date can be stored without immediately making the route unavailable before that date.

---

## 10. Implementation Plan

### 10.1. Domain

Implement or update:

* `FlightPlan`;
* `FlightPlanID`;
* `FlightPlanStatus`;
* `FuelQuantity`;
* `FuelUnit`;
* `FlightPlanDescription`.

Update supporting domain methods:

* `Aircraft.isOperational()`;
* `Aircraft.belongsTo(companyCode)`;
* `Pilot.belongsTo(companyCode)`;
* `FlightRoute.allowsDepartureAt(departureDateTime)`;
* `FlightRoute.isActiveOn(date)`;
* `FlightRoute.effectiveStatusOn(date)`.

### 10.2. Repository

Create:

* `FlightPlanRepository`.

Use existing repositories:

* `FlightRouteRepository`;
* `AircraftRepository`;
* `PilotRepository`.

### 10.3. Application

Create:

* `CreateFlightPlanController`;
* `FlightPlanService`.

The controller/service must:

1. authenticate and authorize the Pilot;
2. obtain the current authenticated Pilot;
3. list available routes for the Pilot’s company;
4. list operational aircraft for the Pilot’s company;
5. load the selected route;
6. validate that the route is active on the planned departure date;
7. validate that the route allows the planned departure date/time;
8. load the selected aircraft;
9. validate aircraft status and company;
10. validate Pilot status and company;
11. generate a raw flight plan description;
12. create the `FlightPlan` in `DRAFT`;
13. save it through `FlightPlanRepository`.

### 10.4. Presentation

Create:

* `CreateFlightPlanUI`.

The UI allows the Pilot to:

* list/select available routes from their company;
* see the route schedule formatted in a readable way;
* select an operational aircraft from their company;
* insert planned departure date and time separately;
* insert planned arrival date and time separately;
* insert fuel quantity and unit;
* review a summary;
* confirm creation.

The UI does not ask the Pilot to manually write or import a DSL description.

Instead, the description is generated automatically by the system.

The UI should provide:

* clear error messages;
* retry loops for invalid input;
* cancellation with `0` where appropriate;
* a confirmation step before creation.

---

## 11. Current UI Behaviour

The final UI flow is:

1. the Pilot opens `Pilot Menu`;
2. the Pilot selects `Create Flight Plan`;
3. the system lists available routes from the Pilot’s company;
4. the Pilot selects a route by number;
5. the system shows the selected route and formatted schedule;
6. the system lists operational aircraft from the Pilot’s company;
7. the Pilot selects an aircraft by number;
8. the Pilot enters planned departure date and time separately;
9. the Pilot enters planned arrival date and time separately;
10. the Pilot enters fuel quantity and fuel unit;
11. the system generates the flight plan description automatically;
12. the system shows a summary;
13. the Pilot confirms creation;
14. the system creates the flight plan in `DRAFT`;
15. the system shows a success message with the created flight plan information.

---

## 12. Observations

* `FlightPlan` stores references to other aggregates by identity, not by direct object reference.
* `FlightRoute` owns `FlightType` and route schedule.
* `FlightPlan` stores concrete planned departure and arrival times.
* `FlightPlanID` is system-generated.
* DSL validation is intentionally outside US080.
* US080 creates a draft plan only.
* A `Flight` should only be created from a validated `FlightPlan`.
* Weather data insertion belongs to a later user story and may invalidate previous tests.
* Future validation should check consistency between the stored planned times and the DSL description.
* A route with a future deactivation date remains active until that date.
* A flight plan must not be created for a planned departure date on or after the route deactivation date.

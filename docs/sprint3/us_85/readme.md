# US085 — Test / Validate Flight Plan

## 1. Context

The goal of this user story is to allow an authenticated Pilot to test and validate an existing flight plan.

A flight plan is created in `DRAFT` status and cannot be considered operationally valid until it undergoes a validation and testing process.

The validation process verifies whether the persisted flight plan is still consistent with the current domain state and whether the planned flight can be considered operationally acceptable according to a simplified flight test.

Unlike the flight control simulations implemented in later user stories, such as US100 and related requirements, US085 does not represent an area control simulation involving multiple flights. It is a flight plan validation and testing operation for a single flight plan.

The flight plan may have been created manually through US080 or imported from a file through the DSL-related use case. Regardless of how it was created, US085 works on the persisted `FlightPlan` aggregate and validates it using the current domain information available in the system.

---

## 2. Requirements

**US085** — As a Pilot, I want to test and validate a flight plan.

The Pilot must be able to select one of their draft flight plans and request its validation and test.

The system must:

* identify the authenticated Pilot;
* list the Pilot’s draft flight plans;
* allow the Pilot to select one draft flight plan;
* verify that the selected flight plan belongs to the authenticated Pilot;
* verify that the flight plan is in `DRAFT` status;
* validate route, aircraft, pilot, schedule and airport information;
* build a simulated representation of the flight plan;
* execute a simplified flight plan test;
* store the test result;
* update the flight plan status according to the result.

If the test succeeds, the flight plan becomes `VALIDATED`.

If the test fails, the flight plan becomes `REJECTED`.

---

## 3. Related Requirements

### 3.1. Create Flight Plan

US080 creates a flight plan manually through guided input.

In US080, the Pilot provides:

* route;
* aircraft;
* planned departure date/time;
* planned arrival date/time;
* fuel quantity.

The flight plan is created in `DRAFT` status.

US080 does not validate the flight plan description as DSL. The description may be generated automatically by the system and may contain plain text.

Therefore, US085 must not assume that every flight plan description is a valid DSL document.

### 3.2. Create Flight Plan from File

Creating a flight plan from a file is a different use case.

In that case, the file must conform to the Core Flight DSL and must be validated using:

* lexical analysis;
* syntactic analysis;
* semantic analysis.

After a valid file is imported, the system creates a persisted `FlightPlan`.

The imported representation belongs to the import boundary and should not become the working model of US085.

### 3.3. Flight DSL

The DSL is a formal input mechanism for describing flight plans.

However, once a flight plan has been created and persisted in the domain model, later operational validation should work from the persisted domain information, not by parsing the description back into an imported representation.

Therefore, US085 does not depend on `ImportedFlightPlan`.

### 3.4. Insert Weather Data in a Flight Plan

A later or related user story allows a Pilot to associate weather data with a flight plan.

If weather data is inserted or changed after a flight plan has already been tested, the previous test result becomes void and the flight plan must return to `DRAFT`.

US085 uses weather data if it is already associated with the flight plan.

If the flight plan has an associated `WeatherDataID`, the system loads the corresponding `WeatherData` and uses it to refine the simulation — adjusting fuel consumption based on wind speed, wind direction and the flight bearing.

The Pilot is not required to manually insert weather data during the test. Weather data association is handled by US082.

If no weather data is associated, the simulation uses standard no-weather assumptions.

### 3.5. Flight Control Simulation

The simulations from US100 and related requirements are different from US085.

Those simulations focus on flight control and may involve multiple flights in an air control area.

US085 focuses only on testing and validating a single flight plan.

---

## 4. Client Specifications and Clarifications

During clarification with the client, the following additional rules and interpretations were identified.

### 4.1. US085 Simulation Is Not the Same as US100+

The client clarified that the flight simulations implemented in US100 and related user stories do not use a physical flight model.

Those simulations are related to flight control and were implemented in C.

The simulation required in US085 is completely different.

Therefore, the US085 test component may be implemented either in C or in Java.

In this design, the US085 test component is implemented in Java.

### 4.2. US080 Does Not Validate the Flight Plan Description

The client clarified that, in US080, the system does not need to read the flight plan description line by line.

The description may be read as a string or from a text file, but it is not validated in US080.

Therefore, US085 must not assume that the flight plan description is a valid DSL document.

### 4.3. Fuel Quantity

The client clarified that fuel quantity is used for simulation and should not be duplicated unnecessarily across different domain concepts.

In the current domain model, `FlightPlan` stores a `FuelQuantity`.

US085 uses this persisted fuel quantity as the technical source for the simulation.

### 4.4. Departure Time Duplication

The client clarified that departure time may exist in the flight plan description, especially when the plan originates from the DSL.

However, the departure time of the first leg is also a property that is frequently used by the system.

Therefore, storing the planned departure date/time directly in `FlightPlan` is acceptable, even if this information also exists in a DSL description.

This duplication should be validated when applicable.

In the adopted design, US085 validates the persisted planned departure date/time against the route schedule and current domain rules.

### 4.5. ImportedFlightPlan Must Not Be Used as the US085 Working Model

`ImportedFlightPlan` belongs to the DSL import process.

It is an intermediate representation produced after parsing and validating a DSL file.

US085 should not parse the persisted flight plan description back into an `ImportedFlightPlan`.

Instead, US085 works with the persisted `FlightPlan` and related domain objects.

### 4.6. SimulatedFlightPlan

To avoid mixing import logic with operational validation, US085 introduces a dedicated simulation model.

The simulation model is built from domain data:

* `FlightPlan`;
* `FlightRoute`;
* `Aircraft`;
* `AircraftModel`;
* `Pilot`;
* departure airport;
* arrival airport.

This model is represented by `SimulatedFlightPlan`.

### 4.7. Segments

The current `FlightPlan` aggregate does not persist structured flight segments.

For US085, the system creates one direct simulated segment between the route’s departure airport and arrival airport.

This is acceptable because the simplified US085 simulation validates the route as a single planned path between two airports.

The simulation model supports a list of segments for future evolution, but the current implementation generates exactly one segment.

---

## 5. Analysis

The central aggregate of US085 is `FlightPlan`.

A `FlightPlan` stores:

* flight plan identity;
* route code;
* aircraft registration;
* pilot identity;
* planned departure date/time;
* planned arrival date/time;
* fuel quantity;
* description;
* status;
* creation date/time;
* optional weather data reference.

The `FlightPlan` does not directly own the route, aircraft, pilot or airports.

Instead, it stores their identities.

This follows DDD principles for cross-aggregate references.

US085 must coordinate several aggregates:

* `FlightPlan`;
* `FlightRoute`;
* `Aircraft`;
* `AircraftModel`;
* `Pilot`;
* `Airport`.

The application/service layer is responsible for loading these aggregates and validating cross-aggregate business rules.

The domain layer protects local invariants and status transitions.

The simulation layer is responsible for creating and testing a simplified operational representation of the flight plan.

---

## 6. Domain Decisions

### 6.1. FlightPlan Remains the Main Aggregate

US085 validates an existing `FlightPlan`.

It does not create a new flight plan.

It does not create a flight.

It does not create a route.

It does not create an aircraft.

It only changes the status of the selected flight plan according to the result of the test.

### 6.2. Only Draft Flight Plans Can Be Tested

Only flight plans in `DRAFT` status can be tested and validated.

This avoids revalidating already validated plans and prevents rejected or cancelled plans from being accidentally reused.

The allowed status transitions for US085 are:

* `DRAFT` → `TESTED` → `VALIDATED`, when the test succeeds;
* `DRAFT` → `REJECTED`, when the test fails.

### 6.3. Final Status After Success

Although the `FlightPlanStatus` enum contains both `TESTED` and `VALIDATED`, the final successful result of US085 is `VALIDATED`.

The intermediate `TESTED` status may be used internally to respect the domain transition rules.

From the user’s perspective, a successful US085 operation validates the flight plan.

### 6.4. Description Is Not the Technical Source of US085

The `FlightPlanDescription` is not the technical source for US085.

It may contain:

* automatically generated plain text from US080;
* original DSL text from file import;
* operational notes;
* human-readable information.

US085 must not require the Pilot to know or write DSL.

Therefore, the flight plan description is not parsed as part of the US085 validation.

### 6.5. SimulatedFlightPlan as a Dedicated Model

US085 introduces `SimulatedFlightPlan` as a dedicated model for the simplified flight plan test.

This avoids using `ImportedFlightPlan` outside the import boundary.

The simulation model is built from persisted domain data and is independent from the original creation mechanism of the flight plan.

### 6.6. One Direct Segment in the Current Version

Since the current domain model does not persist flight plan segments, the simulated flight plan uses one direct segment from the departure airport coordinates to the arrival airport coordinates.

The segment distance is calculated using the Haversine formula.

The total distance of the simulated flight plan is the sum of its segment distances.

In the current implementation, this means the total distance is the distance of the single direct segment.

### 6.7. AircraftModel Is Obtained Through Aircraft

The selected `Aircraft` already references its `AircraftModel`.

Therefore, US085 does not need to load the aircraft model through a separate repository.

The test uses aircraft model data such as:

* maximum range;
* maximum fuel capacity;
* service ceiling;
* cruise speed, if needed in the future.

### 6.8. Weather Data Refines the Simulation When Present

US085 does not require the Pilot to manually insert weather data.

If weather data is already associated with the flight plan via a `WeatherDataID`, the system loads it and uses it to refine the fuel consumption estimate.

The weather factor is calculated from the wind speed, wind direction and the bearing of the simulated segment, using a cosine model clamped between 0.85 and 1.25.

If no weather data is associated, the simulation uses standard no-weather assumptions and informs the Pilot in the validation report.

---

## 7. Acceptance Criteria

### US085.1 — List Draft Flight Plans

Given an authenticated Pilot, when the Pilot opens the test/validate flight plan option, the system lists only that Pilot’s flight plans in `DRAFT` status.

### US085.2 — Select Flight Plan

The Pilot must be able to select one of the listed draft flight plans.

If no draft flight plans exist, the system must inform the Pilot.

### US085.3 — Authenticated Pilot

The system must identify the authenticated user as a Pilot.

If the authenticated user is not a Pilot, the operation must be rejected.

### US085.4 — Active Pilot

The authenticated Pilot must be active.

If the Pilot is inactive, the operation must be rejected.

### US085.5 — Flight Plan Ownership

The selected flight plan must belong to the authenticated Pilot.

If the flight plan belongs to another Pilot, the operation must be rejected and the flight plan must not be changed.

### US085.6 — Draft Status

Only flight plans in `DRAFT` status can be tested.

If the selected flight plan is not in `DRAFT` status, the operation must be rejected.

### US085.7 — Existing Route

The route referenced by the flight plan must exist.

If the route does not exist, the test fails and the flight plan becomes `REJECTED`.

### US085.8 — Route Active on Planned Departure Date

The route must be active on the flight plan’s planned departure date.

If the route is inactive on that date, the test fails and the flight plan becomes `REJECTED`.

### US085.9 — Route Schedule Compatibility

The route must allow the flight plan’s planned departure date/time.

If the route schedule does not allow that departure date/time, the test fails and the flight plan becomes `REJECTED`.

### US085.10 — Existing Aircraft

The aircraft referenced by the flight plan must exist.

If the aircraft does not exist, the test fails and the flight plan becomes `REJECTED`.

### US085.11 — Operational Aircraft

The aircraft must be operational.

If the aircraft is decommissioned or otherwise not operational, the test fails and the flight plan becomes `REJECTED`.

### US085.12 — Aircraft Belongs to Route Company

The aircraft must belong to the same air transport company as the route.

If the aircraft belongs to another company, the test fails and the flight plan becomes `REJECTED`.

### US085.13 — Pilot Belongs to Route Company

The authenticated Pilot must belong to the same air transport company as the route.

If the Pilot does not belong to the route’s company, the test fails and the flight plan becomes `REJECTED`.

### US085.14 — Pilot Certification

The authenticated Pilot must be certified for the aircraft model on the planned departure date.

If the Pilot is not certified for the aircraft model, the test fails and the flight plan becomes `REJECTED`.

### US085.15 — Existing Departure Airport

The route’s departure airport must exist.

If the departure airport does not exist, the test fails and the flight plan becomes `REJECTED`.

### US085.16 — Existing Arrival Airport

The route’s arrival airport must exist.

If the arrival airport does not exist, the test fails and the flight plan becomes `REJECTED`.

### US085.17 — Build SimulatedFlightPlan

The system must build a `SimulatedFlightPlan` from the persisted flight plan and related domain data.

The simulated flight plan must contain one direct segment between the departure airport coordinates and the arrival airport coordinates.

### US085.18 — Distance Calculation

The system must calculate the distance of the simulated segment using the Haversine formula.

### US085.19 — Maximum Range Validation

The simulated total distance must not exceed the aircraft model’s maximum range.

If it exceeds the maximum range, the test fails and the flight plan becomes `REJECTED`.

### US085.20 — Fuel Capacity Validation

The loaded fuel quantity must not exceed the aircraft model’s maximum fuel capacity.

If loaded fuel exceeds the maximum fuel capacity, the test fails and the flight plan becomes `REJECTED`.

### US085.21 — Estimated Fuel Validation

The estimated fuel required for the simulated route must not exceed the loaded fuel quantity.

If the estimated fuel required exceeds the loaded fuel quantity, the test fails and the flight plan becomes `REJECTED`.

### US085.22 — Service Ceiling Validation

The simulated cruise altitude must not exceed the aircraft model’s service ceiling.

If the simulated altitude exceeds the service ceiling, the test fails and the flight plan becomes `REJECTED`.

### US085.23 — Store Test Result

The system must store the result of the test in the flight plan.

The result must contain enough information to be displayed to the Pilot.

### US085.24 — Successful Test

If all validations and test checks pass, the flight plan test result is stored and the flight plan becomes `VALIDATED`.

### US085.25 — Failed Test

If any validation or test check fails, the failure result is stored and the flight plan becomes `REJECTED`.

### US085.26 — Clear Report

After the test, the system must display a clear report showing:

* selected flight plan;
* route;
* aircraft;
* calculated distance;
* loaded fuel;
* estimated required fuel;
* estimated remaining fuel;
* validation messages;
* final status.

---

## 8. Dependencies / References

This user story depends on existing data from:

* `FlightPlan`;
* `FlightRoute`;
* `Aircraft`;
* `AircraftModel`;
* `Pilot`;
* `Airport`.

It also relates to:

* US080 — Create Flight Plan;
* US081 — Create Flight Plan from a File;
* US082 — Insert Weather Data in a Flight Plan;
* US083 — Flight DSL Specification and Validation;
* US100 and related flight control simulation user stories.

---

## 9. Design Overview

### 9.1. Aggregate Roots

The relevant Aggregate Roots are:

* `FlightPlan`;
* `FlightRoute`;
* `Aircraft`;
* `AircraftModel`;
* `Pilot`;
* `Airport`.

### 9.2. Value Objects

Relevant value objects include:

* `FlightPlanID`;
* `RouteCode`;
* `AircraftRegistration`;
* `PilotID`;
* `FuelQuantity`;
* `FuelUnit`;
* `FlightPlanStatus`;
* `AirportIataCode`;
* `Coordinates`.

### 9.3. Application / Service Layer Responsibilities

The application/service layer coordinates cross-aggregate validation:

* authenticated Pilot lookup;
* listing draft flight plans;
* selected flight plan loading;
* ownership validation;
* route loading;
* route activity validation;
* route schedule validation;
* aircraft loading;
* aircraft status validation;
* aircraft company validation;
* pilot company validation;
* pilot certification validation;
* airport loading;
* simulated flight plan creation;
* flight plan test execution;
* flight plan status update;
* persistence of the final result.

### 9.4. Domain Layer Responsibilities

The domain layer protects local invariants.

`FlightPlan` ensures:

* required fields are not null;
* planned arrival is after planned departure;
* created flight plans start as `DRAFT`;
* only draft flight plans can be marked as tested;
* only tested flight plans can be validated;
* cancelled flight plans cannot be rejected.

`FlightRoute` ensures:

* route identity and schedule rules;
* route activity on a given date;
* departure date/time compatibility through `allowsDepartureAt(...)`.

`Aircraft` exposes:

* operational status;
* owning air transport company;
* associated aircraft model.

`Pilot` exposes:

* active status;
* owning air transport company;
* certification validation for an aircraft model on a given date.

### 9.5. Simulation Layer Responsibilities

The simulation layer contains the simplified flight plan test.

It is responsible for:

* building the simulated flight plan;
* representing simulated segments;
* calculating distances;
* estimating fuel usage;
* validating maximum range;
* validating fuel capacity;
* validating estimated fuel requirements;
* validating service ceiling;
* producing a test result.

---

## 10. Implementation Plan

### 10.1. Domain / Simulation

Create:

* `SimulatedFlightPlan`;
* `SimulatedFlightSegment`;
* `SimulatedFlightPlanFactory`;
* `FlightPlanTester`;
* `FlightPlanTestResult`.

The simulation model should be immutable and should not be persisted as an aggregate.

The `SimulatedFlightPlanFactory` builds a simulated flight plan from:

* `FlightPlan`;
* `FlightRoute`;
* departure `Airport`;
* arrival `Airport`;
* `AircraftModel`.

The `FlightPlanTester` receives:

* `SimulatedFlightPlan` — which already contains the loaded fuel quantity;
* `AircraftModel`;
* `Optional<WeatherData>` — may be empty if no weather data is associated.

It returns a `FlightPlanTestResult`.

### 10.2. Repository

Use existing repositories:

* `FlightPlanRepository`;
* `FlightRouteRepository`;
* `AircraftRepository`;
* `AirportRepository`;
* `PilotRepository`.

No `ImportedFlightPlan` repository or DSL parser is used in US085.

### 10.3. Application

Create:

* `TestFlightPlanController`;
* `TestFlightPlanService`;
* `FlightPlanTestBoard`.

The controller/service must:

1. authenticate and authorize the Pilot;
2. obtain the current authenticated Pilot;
3. list the Pilot’s draft flight plans;
4. load the selected flight plan;
5. validate ownership;
6. validate draft status;
7. load the referenced route;
8. load the referenced aircraft;
9. obtain the aircraft model from the aircraft;
10. load the route’s departure and arrival airports;
11. validate route activity;
12. validate route schedule compatibility;
13. validate aircraft status and company;
14. validate Pilot status and company;
15. validate Pilot certification;
16. build a `SimulatedFlightPlan`;
17. execute `FlightPlanTester`;
18. record the test result;
19. update the flight plan status;
20. save the flight plan;
21. return a `FlightPlanTestBoard` for presentation.

### 10.4. Presentation

Create:

* `TestFlightPlanUI`.

The UI allows the Pilot to:

* open the flight plan test/validation option;
* view draft flight plans;
* select a flight plan by number;
* review selected flight plan details;
* confirm the test;
* see a visual progress indication;
* view the final validation report.

The UI must not:

* parse DSL;
* ask the Pilot to manually write DSL;
* ask the Pilot to manually insert weather data line by line;
* perform business validation itself.

The UI should provide:

* clear error messages;
* cancellation with `0` where appropriate;
* confirmation before executing the test;
* a readable final report.

---

## 11. Current UI Behaviour

The final UI flow is:

1. the Pilot opens `Pilot Menu`;
2. the Pilot selects `Test / Validate Flight Plan`;
3. the system shows the `AISAFE TEST CENTER` header;
4. the system lists the authenticated Pilot’s draft flight plans;
5. the Pilot selects a flight plan by number;
6. the system shows the selected flight plan details;
7. the Pilot confirms the test/validation operation;
8. the system shows a progress indication;
9. the system validates route, aircraft, pilot, schedule and airport information;
10. the system builds a simulated flight plan;
11. the system executes the simplified flight plan test;
12. the system stores the test result;
13. if the test succeeds, the flight plan becomes `VALIDATED`;
14. if the test fails, the flight plan becomes `REJECTED`;
15. the system displays a final report.

---

## 12. Observations

* US085 validates and tests a persisted `FlightPlan`.
* US085 does not create a new `FlightPlan`.
* US085 does not create a `Flight`.
* US085 does not create or update a `FlightRoute`.
* US085 does not parse `FlightPlanDescription` back into an `ImportedFlightPlan`.
* `ImportedFlightPlan` belongs to the DSL import boundary.
* `SimulatedFlightPlan` belongs to the US085 simulation/testing boundary.
* The current simulation uses one direct segment between the route’s departure and arrival airports.
* The direct segment uses airport coordinates and the Haversine formula.
* Segment support is kept in the simulation model to allow future evolution.
* The US085 simulation is different from US100+ flight control simulations.
* The US085 test component may be implemented in Java or C.
* In this implementation, Java is used.
* The flight plan description may be plain text, generated text or original DSL text.
* The Pilot is not required to know or write DSL to validate a manually created flight plan.
* Weather data may later refine the simulation, but it is not mandatory for the current version.
* A successful test results in `VALIDATED`.
* A failed test results in `REJECTED`.
* Only `DRAFT` flight plans can be tested.
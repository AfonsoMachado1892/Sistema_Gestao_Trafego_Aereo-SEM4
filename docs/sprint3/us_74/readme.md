# US074 — Deactivate a Flight Route

## 1. Context

The goal of this user story is to allow an Air Transport Company Collaborator (ATCC) to deactivate a flight route belonging to their company, specifying an effective deactivation date from which the route will no longer be available for new flight plans.

A flight route represents a commercial connection between two airports operated by an air transport company. Once deactivated, the route transitions to an `INACTIVE` status and no new flight plans may be created for it.

---

## 2. Requirements

**US074** As an Air Transport Company Collaborator, I want to deactivate a flight route from a given date onwards.

The ATCC must provide:

* the route to deactivate;
* the effective deactivation date.

The route cannot be deactivated if there are planned flights scheduled after the specified deactivation date.

No new flights may be created on a deactivated flight route.

---

## 3. Related Requirements

### 3.1. US073 — Create a Flight Route

US073 creates a flight route. A route must first be created and active before it can be deactivated.

### 3.2. US080 — Create a Flight Plan

US080 creates a flight plan for an existing route. A deactivated route must not accept new flight plans.

The deactivation validation in US074 must ensure no active flight plans exist after the deactivation date before proceeding.

### 3.3. US075 — Add a Pilot

Pilots are associated with air transport companies. Deactivating a route does not directly affect pilots, but may affect their available routes when creating flight plans.

---

## 4. Client Specifications and Clarifications

### 4.1. Deactivation Is Not Deletion

The route is not permanently deleted from the system.

It transitions from `ACTIVE` to `INACTIVE` status.

This is a soft deactivation, preserving historical data and audit trail.

### 4.2. Deactivation Date

The ATCC must specify a date from which the route becomes inactive.

The deactivation date:

* must not be in the past;
* is stored in the route for historical reference.

### 4.3. Scoped to Own Company

The ATCC may only deactivate routes belonging to their own company.

Attempting to deactivate a route from another company must be rejected.

### 4.4. Flight Plan Dependency

A route cannot be deactivated if there are active flight plans scheduled after the specified deactivation date.

This ensures operational integrity — no flight plan is left referencing an inactive route for a future date.

This validation is performed in the application layer, as it requires cross-aggregate coordination between `FlightRoute` and `FlightPlan`.

Note: at the time of implementation of US074, the `FlightPlan` aggregate was not yet implemented. This validation is marked as `TODO` and must be completed when US080 is integrated.

### 4.5. One-Way Lifecycle

A deactivated route cannot be reactivated.

The `INACTIVE` status is terminal.

---

## 5. Analysis

The central domain concept for US074 is `FlightRoute`.

The `FlightRoute` defines:

* route code (unique identifier and name);
* route status (`ACTIVE` or `INACTIVE`);
* departure airport;
* arrival airport;
* owning air transport company;
* deactivation date.

The deactivation operation:

1. verifies the route exists;
2. verifies the route belongs to the authenticated ATCC's company;
3. verifies the route is currently active;
4. verifies the deactivation date is not in the past;
5. verifies no active flight plans exist after the deactivation date *(TODO — US080)*;
6. sets the route status to `INACTIVE`;
7. stores the deactivation date.

Cross-aggregate business rules are validated in the application layer.

---

## 6. Domain Decisions

### 6.1. FlightRoute as Aggregate Root

`FlightRoute` is modeled as an Aggregate Root with its own lifecycle:

* created as `ACTIVE` (US073);
* deactivated to `INACTIVE` (US074);
* referenced by `FlightPlan` through `RouteCode`.

### 6.2. Soft Deactivation

The route is not removed from the repository.

Instead, its `routeStatus` is set to `INACTIVE` and a `deactivationDate` is stored.

This preserves historical data and allows existing flight plans and flights to retain their reference to the route.

### 6.3. RouteCode as Identity and Name

The route code (e.g. `TP123`) serves simultaneously as the technical identity of the aggregate and as its business name.

Format: 2 uppercase letters matching the company IATA code, followed by 1 to 4 digits.

This is validated at creation time and does not change.

### 6.4. Cross-Aggregate Validation in Application Layer

The check for active flight plans after the deactivation date requires coordination between `FlightRoute` and `FlightPlan`.

Following DDD principles, this cross-aggregate validation is performed in the `FlightRouteService`, not inside the `FlightRoute` aggregate itself.

The `FlightRoute.deactivate()` method only enforces local invariants:

* route must be active;
* deactivation date must not be in the past.

### 6.5. Company Scope Enforcement

The `FlightRouteService` verifies that the route's `airTransportCompanyIataCode` matches the company of the authenticated ATCC before proceeding with deactivation.

This prevents an ATCC from deactivating routes belonging to other companies.

---

## 7. Acceptance Criteria

### US074.1 — Deactivate Active Route

Given an authenticated ATCC, when the ATCC provides a valid route code and deactivation date, the system deactivates the route and sets its status to `INACTIVE`.

### US074.2 — Route Must Exist

The selected route must exist in the system.

If the route does not exist, the deactivation must be rejected.

### US074.3 — Route Must Be Active

The selected route must be currently active.

If the route is already inactive, the deactivation must be rejected.

### US074.4 — Route Must Belong to ATCC's Company

The selected route must belong to the same air transport company as the authenticated ATCC.

If the route belongs to another company, the deactivation must be rejected.

### US074.5 — Deactivation Date Must Not Be in the Past

The provided deactivation date must be today or in the future.

If the date is in the past, the deactivation must be rejected.

### US074.6 — No Active Flight Plans After Deactivation Date

The route cannot be deactivated if there are active flight plans scheduled after the specified deactivation date.

If such flight plans exist, the deactivation must be rejected with a clear error message.

*(This validation is marked as TODO pending US080 implementation.)*

### US074.7 — Deactivation Date Is Stored

The system must store the deactivation date in the route for historical reference.

### US074.8 — No New Flight Plans After Deactivation

After deactivation, no new flight plans may be created for the deactivated route.

### US074.9 — Clear Error Messages

Invalid input or failed business rules must produce clear and informative error messages.

### US074.10 — Success Confirmation

The system must confirm the deactivation with an appropriate success message including the route code.

---

## 8. Dependencies / References

This user story depends on:

* `FlightRoute` — the aggregate being deactivated;
* `CustomerCollaborator` — to identify the authenticated ATCC and their company.

It relates to:

* US073 — Create Flight Route;
* US080 — Create Flight Plan *(dependency for flight plan check — TODO)*;
* US075 — Add Pilot;
* US076 — List Pilot Roster.

---

## 9. Design Overview

### 9.1. Aggregate Roots

The relevant Aggregate Roots are:

* `FlightRoute`.

### 9.2. Value Objects

Relevant value objects include:

* `RouteCode`;
* `RouteStatus`;
* `LocalDate` (deactivation date).

### 9.3. Application Layer Responsibilities

The application layer coordinates:

* authentication and authorisation of the ATCC;
* resolution of the authenticated ATCC's company;
* route existence check;
* route active status check;
* company scope check;
* deactivation date validation;
* active flight plan check *(TODO — US080)*.

### 9.4. Domain Layer Responsibilities

The domain layer protects local invariants:

`FlightRoute` ensures:

* route must be active before deactivation;
* deactivation date must not be in the past;
* `INACTIVE` status is terminal — a deactivated route cannot be reactivated.

---

## 10. Implementation Plan

### Domain

Update:

* `FlightRoute` — add `deactivationDate` field and update `deactivate(LocalDate)` method.
* `RouteStatus` — already has `ACTIVE` and `INACTIVE`.

### Repository

Use existing:

* `FlightRouteRepository` — already has `findByRouteCode()` and `findActiveByCompany()`.

Future addition *(US080)*:

* `FlightPlanRepository.existsActiveByRouteCodeAfter(routeCode, date)`.

### Application

Create:

* `DeactivateFlightRouteController`.

Update:

* `FlightRouteService` — add `deactivateRoute()` and `listActiveRoutesByCompany()`.

The controller must:

1. authenticate and authorise the ATCC;
2. resolve the authenticated ATCC's company;
3. list active routes for the company;
4. receive the selected route code and deactivation date;
5. verify the route belongs to the ATCC's company;
6. verify no active flight plans exist after the deactivation date *(TODO)*;
7. deactivate the route;
8. save through `FlightRouteRepository`.

### Presentation

Create:

* `DeactivateFlightRouteUI`.

The UI should allow the ATCC to:

* view the list of active routes for their company;
* select a route to deactivate;
* provide a deactivation date;
* confirm the deactivation;
* receive a success or error message.

---

## 11. Observations

* `FlightRoute` stores references to airports and air transport company by identity (`IataCode`), not by direct object reference, following DDD principles.
* Deactivation is a one-way operation — an `INACTIVE` route cannot be reactivated.
* The deactivation date is stored for historical reference and future validation.
* The flight plan existence check is marked as `TODO` and must be completed when US080 is integrated.
* The `RouteCode` serves simultaneously as the technical identity and the business name of the route.
* The company scope check ensures an ATCC cannot affect routes outside their own company.
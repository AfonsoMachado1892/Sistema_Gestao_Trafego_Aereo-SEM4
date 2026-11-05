# US121 - Create Valid Flight Plan From File

## 1. Objective

The goal of this user story is to allow a Pilot to create a valid flight plan from a file.

The flight plan file must be written using the Flight DSL specified in US120.

Before being accepted by the system, the file must be validated through:

```txt
file format validation
lexical analysis
syntactic analysis
semantic analysis
domain integration validation
```

Only flight plans that are valid according to the DSL and coherent with the existing AISafe domain data may be imported and used by the system.

The DSL does not create flight routes. It describes flight plans.

Therefore, a flight plan imported from a file is only accepted if the route described in the DSL corresponds to an existing `FlightRoute` in the system.

---

## 2. User Story

As a Pilot, I want to create a valid flight plan from a file.

---

## 3. Acceptance Criteria

The official acceptance criteria for this user story are:

```txt
1. The flight plan file must conform to the Flight DSL as specified in US120.

2. The file must be validated through lexical, syntactic, and semantic analysis before being accepted.

3. Invalid files must produce meaningful and informative error messages.

4. Only valid flight plans may be imported and used by the system.

5. The system must support the possibility of multiple file formats, validating the format before processing.
```

---

## 4. Acceptance Criteria Compliance

| Acceptance Criteria                                                                                        | Status | Evidence                                                                                                                                                         |
| ---------------------------------------------------------------------------------------------------------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The flight plan file must conform to the Flight DSL as specified in US120.                                 | Done   | The import process uses the Flight DSL grammar and validation pipeline from US120.                                                                               |
| The file must be validated through lexical, syntactic, and semantic analysis before being accepted.        | Done   | The file is processed by `FlightPlanImportService`, which uses ANTLR parsing and semantic validation before returning an `ImportedFlightPlan`.                   |
| Invalid files must produce meaningful and informative error messages.                                      | Done   | Lexical, syntactic, semantic and domain integration errors are shown to the user.                                                                                |
| Only valid flight plans may be imported and used by the system.                                            | Done   | A valid `ImportedFlightPlan` is mapped into a real EAPLI `FlightPlan` and saved in `FlightPlanRepository`.                                                       |
| The system must support the possibility of multiple file formats, validating the format before processing. | Done   | The system validates the file format before processing. Currently, only `.flight` files are supported. Unsupported formats are rejected before ANTLR processing. |

---

## 5. Current Context

The project already contains the Flight DSL validation pipeline developed for US120.

The relevant LPROG artifacts include:

```txt
lprog/
 ├── grammar/
 │   └── FlightPlan.g4
 ├── src/main/java/
 │   ├── dsl/
 │   │   ├── FlightPlanDslApp.java
 │   │   ├── listener/
 │   │   │   └── FlightPlanStructureListener.java
 │   │   ├── model/
 │   │   │   ├── ImportedCoordinate.java
 │   │   │   ├── ImportedFlightPlan.java
 │   │   │   ├── ImportedFuel.java
 │   │   │   ├── ImportedLeg.java
 │   │   │   ├── ImportedRoute.java
 │   │   │   └── ImportedSegment.java
 │   │   ├── parser/
 │   │   │   ├── FlightPlanImportService.java
 │   │   │   ├── FlightPlanParserService.java
 │   │   │   ├── FlightPlanLexicalErrorListener.java
 │   │   │   └── FlightPlanSyntaxErrorListener.java
 │   │   ├── validation/
 │   │   │   ├── FlightPlanSemanticValidator.java
 │   │   │   └── SemanticError.java
 │   │   └── visitor/
 │   │       └── FlightPlanBuilderVisitor.java
 │   └── grammar/
 │       ├── FlightPlanLexer.java
 │       ├── FlightPlanParser.java
 │       ├── FlightPlanBaseVisitor.java
 │       ├── FlightPlanBaseListener.java
 │       ├── FlightPlanVisitor.java
 │       └── FlightPlanListener.java
```

The LPROG module validates a `.flight` file and produces an intermediate representation:

```txt
ImportedFlightPlan
ImportedRoute
ImportedLeg
ImportedSegment
ImportedCoordinate
ImportedFuel
```

This representation is not the main EAPLI domain aggregate.

It is an intermediate import model used before creating the real `FlightPlan` aggregate in the AISafe domain.

---

## 6. DSL Example

Example of a valid `.flight` file:

```txt
flight TP123 regular {

    route OPO -> LIS;

    aircraft CS-TAB;
    pilot P001;

    leg {

        departure OPO at 2026-06-10 10:30;
        arrival LIS at 2026-06-10 11:20;

        segment {
            (41.242, -8.678) -> (38.774, -9.134)
            altitude 10000 m;
            width 500 m;
            wind 90 deg 12 m/s;
        }

        fuel 5000 kg;

    }
}
```

This file describes:

```txt
Flight identifier: TP123
Flight type: regular
Route: OPO -> LIS
Aircraft: CS-TAB
Pilot: P001
Departure: OPO at 2026-06-10 10:30
Arrival: LIS at 2026-06-10 11:20
Fuel: 5000 kg
```

---

## 7. Important Interpretation

The DSL does not create flight plans by itself.

The DSL is a language used to describe flight plans.

The import use case is responsible for:

```txt
reading the file
validating the file format
validating the DSL content
building an ImportedFlightPlan
checking coherence with the EAPLI domain
creating a real FlightPlan aggregate
saving it in the FlightPlanRepository
```

The DSL also does not create flight routes.

A flight plan can only be imported if the route described in the DSL is coherent with an existing `FlightRoute` in the system.

---

## 8. File Format Validation

Before processing the file with ANTLR, the system validates the file format.

The currently supported file format is:

```txt
.flight
```

Unsupported formats are rejected before lexical, syntactic or semantic analysis is performed.

Example:

```txt
plan.txt
```

Expected result:

```txt
FILE_FORMAT_ERROR at line 0, column 0: Unsupported file format. Currently supported formats: .flight.
```

Although only `.flight` files are currently supported, the design allows the system to support additional import formats in the future.

---

## 9. Validation and Import Pipeline

The complete US121 validation and import pipeline is:

```txt
.flight file
    ↓
file format validation
    ↓
FlightPlanImportService
    ↓
FlightPlanLexer
    ↓
FlightPlanParser
    ↓
FlightPlanLexicalErrorListener
    ↓
FlightPlanSyntaxErrorListener
    ↓
FlightPlanStructureListener
    ↓
FlightPlanBuilderVisitor
    ↓
ImportedFlightPlan
    ↓
FlightPlanSemanticValidator
    ↓
domain integration validation
    ↓
FlightPlan EAPLI aggregate
    ↓
FlightPlanRepository
```

---

## 10. LPROG Validation

The LPROG validation phase ensures that the file is valid according to the Flight DSL.

It includes:

```txt
lexical validation
syntactic validation
semantic validation
internal representation creation
```

If lexical, syntactic or semantic errors are found, the import is rejected.

The system must not create an EAPLI `FlightPlan` if the DSL file is invalid.

---

## 11. Internal Representation

After successful lexical, syntactic and semantic validation, the system builds an internal representation:

```txt
ImportedFlightPlan
```

This object contains:

```txt
flight id
flight type
route origin and destination
aircraft registration
pilot id
legs
segments
fuel information
```

The `ImportedFlightPlan` is not persisted as the main system flight plan.

It is used as an intermediate representation before creating the real EAPLI `FlightPlan`.

---

## 12. Domain Integration Validation

After producing an `ImportedFlightPlan`, the system validates whether it can be used to create a real flight plan in the EAPLI domain.

This validation is required because the DSL only describes the flight plan. It does not guarantee that the referenced domain objects exist in the system.

The following integration rules are checked:

```txt
1. The imported flight id must identify an existing FlightRoute.

2. The imported route origin and destination must match the existing FlightRoute.

3. The imported flight type must match the existing FlightRoute type.

4. The FlightRoute must be active on the planned departure date.

5. The FlightRoute schedule must allow the planned departure date/time.

6. The imported aircraft registration must identify an existing Aircraft.

7. The Aircraft must be operational.

8. The Aircraft must belong to the same air transport company as the FlightRoute.

9. The authenticated user must correspond to an existing Pilot aggregate.

10. The authenticated Pilot must be active.

11. The authenticated Pilot must belong to the same air transport company as the FlightRoute.

12. The imported fuel quantity must be mapped to a valid FuelQuantity.

13. The imported departure and arrival times must be mapped to the FlightPlan planned departure and arrival times.
```

If any of these rules fail, the file may be valid as a DSL document, but it cannot be imported into the system.

In that case, the system rejects the import with a clear domain integration error.

---

## 13. Pilot Association Decision

The DSL contains a pilot identifier.

Example:

```txt
pilot P001;
```

However, the AISafe domain associates created flight plans with the authenticated Pilot executing the use case.

Therefore, during import:

```txt
The pilot identifier in the DSL is preserved in the original FlightPlanDescription.
The created FlightPlan is associated with the authenticated Pilot.
The system validates that the authenticated Pilot exists, is active, and belongs to the same air transport company as the resolved FlightRoute.
```

This avoids coupling the DSL pilot identifier to the internal `PilotID` used by the domain model.

It also reflects the user story actor:

```txt
As a Pilot, I want to create a valid flight plan from a file.
```

This means that a valid pilot from the same company as the route may import a flight plan file, and the resulting `FlightPlan` is associated with that authenticated pilot.

---

## 14. Route Resolution

The imported DSL contains a flight identifier.

Example:

```txt
flight TP123 regular
```

The EAPLI domain has `RouteCode`.

Example:

```txt
TP123
```

Therefore, the import process uses the imported flight identifier as the candidate route code.

Example:

```txt
ImportedFlightPlan.flightId = TP123
RouteCode = TP123
```

The system then searches for an existing route:

```txt
flightRouteRepository.findByRouteCode(RouteCode.of("TP123"))
```

If no route exists, the import is rejected.

Example error:

```txt
ROUTE_ERROR: No existing flight route was found for route code TP123.
```

The import process must not create a new route automatically.

Creating flight routes belongs to the flight route management use case, not to US121.

---

## 15. Route Coherence Validation

If a route exists with the imported route code, the system validates coherence between the DSL and the domain route.

Example DSL:

```txt
flight TP123 regular {
    route OPO -> LIS;
}
```

Expected existing route:

```txt
RouteCode: TP123
Departure airport: OPO
Arrival airport: LIS
Flight type: REGULAR
```

If the imported route does not match the system route, the import is rejected.

Example error:

```txt
ROUTE_ERROR: Imported route OPO -> LIS does not match system route TP123.
```

If the imported flight type does not match the system route type, the import is rejected.

Example error:

```txt
ROUTE_ERROR: Imported flight type REGULAR does not match system route type CHARTER.
```

---

## 16. FlightPlan Mapping

After successful validation, the imported representation is mapped to the EAPLI `FlightPlan` aggregate.

The implemented mapping is:

```txt
FlightPlan.routeCode
    = RouteCode resolved from ImportedFlightPlan.flightId

FlightPlan.aircraftRegistration
    = ImportedFlightPlan.aircraftRegistration

FlightPlan.pilotID
    = authenticated Pilot identity

FlightPlan.plannedDepartureDateTime
    = departure date/time of the first leg

FlightPlan.plannedArrivalDateTime
    = arrival date/time of the last leg

FlightPlan.fuelQuantity
    = fuel quantity of the imported leg

FlightPlan.description
    = original content of the .flight file

FlightPlan.status
    = DRAFT
```

The imported DSL content is stored as the `FlightPlanDescription`.

This allows later user stories, such as test and validation, to revalidate or inspect the original DSL description.

---

## 17. Multiple Legs Decision

The Core Flight DSL supports multiple legs.

However, the current EAPLI `FlightPlan` model stores a single global fuel quantity and represents one concrete flight plan occurrence for a route.

The project requirements also state that only direct flights are considered.

For this reason, the US121 integration currently restricts imported flight plans to exactly one leg.

If a file contains multiple legs, the DSL may be valid according to US120, but the system rejects it during domain integration.

Example error:

```txt
DOMAIN_INTEGRATION_ERROR: Only direct flight plans with exactly one leg can currently be imported into the system.
```

This keeps the DSL expressive while preserving compatibility with the current EAPLI domain model.

---

## 18. Error Reporting

US121 reports different categories of errors clearly.

### 18.1 File Format Errors

Example:

```txt
FILE_FORMAT_ERROR at line 0, column 0: Unsupported file format. Currently supported formats: .flight.
```

### 18.2 Lexical Errors

Example:

```txt
LEXICAL_ERROR at line 3, column 10: token recognition error at: 'OP@'
```

### 18.3 Syntax Errors

Example:

```txt
SYNTAX_ERROR at line 5, column 4: missing ';' at 'aircraft'
```

### 18.4 Semantic Errors

Example:

```txt
FUEL_ERROR at line 20, column 8: Leg 1 fuel quantity must be positive.
```

### 18.5 Domain Integration Errors

Example:

```txt
ROUTE_ERROR: No existing flight route was found for route code TP123.
```

Example:

```txt
AIRCRAFT_ERROR: Aircraft CS-TAB does not belong to the route's company.
```

Example:

```txt
PILOT_ERROR: Authenticated pilot must belong to the route's company.
```

---

## 19. Application Design

The implemented application structure is:

```txt
ImportFlightPlanFromFileUI
    ↓
CreateFlightPlanFromFileController
    ↓
CreateFlightPlanFromFileService
    ↓
FlightPlanImportService
    ↓
ImportedFlightPlan
    ↓
Domain integration validation
    ↓
FlightPlan.draft(...)
    ↓
FlightPlanRepository.save(...)
```

---

## 20. Implemented Classes

The US121 integration introduced:

```txt
CreateFlightPlanFromFileController
CreateFlightPlanFromFileService
```

The existing UI was adapted:

```txt
ImportFlightPlanFromFileUI
```

The EAPLI `core` module depends on the LPROG module:

```txt
core -> aisafe-lprog
```

This keeps the dependency direction correct.

The LPROG module does not depend on the EAPLI domain.

---

## 21. Controller Responsibilities

The `CreateFlightPlanFromFileController` is responsible for:

```txt
ensuring the authenticated user has the Pilot role
obtaining the authenticated Pilot aggregate
delegating the import process to the service
returning the created FlightPlan or validation errors
```

A user with the `PILOT` role but without a corresponding `Pilot` aggregate is rejected.

This ensures that only real domain pilots can import flight plans.

---

## 22. Service Responsibilities

The `CreateFlightPlanFromFileService` is responsible for:

```txt
validating the file format
calling the LPROG import service
receiving the ImportedFlightPlan
validating the imported data against EAPLI repositories
creating the FlightPlan aggregate
saving the FlightPlan
returning the created FlightPlan
```

The service coordinates cross-aggregate validation and therefore belongs to the domain/application service layer.

---

## 23. UI Behaviour

The final UI flow is:

```txt
1. Pilot opens the Pilot Menu.
2. Pilot selects "Import Flight Plan from File".
3. System asks for the .flight file path or lists available example files.
4. System validates the file format.
5. System validates the file using the US120 DSL validation pipeline.
6. System validates the imported data against the EAPLI domain.
7. System creates a FlightPlan in DRAFT status.
8. System shows a success message with the created FlightPlan information.
9. The imported flight plan appears in "List My Flight Plans".
```

If the file is invalid, the system shows the detected errors and does not create a `FlightPlan`.

---

## 24. Tested Scenarios

### 24.1 Invalid Date

File:

```txt
invalid-date-flight.flight
```

Observed result:

```txt
Flight plan import failed.
Errors:
 - DATE_TIME_ERROR at line 10, column 3: Leg 1 departure date/time is invalid.
```

### 24.2 Syntax Error

File:

```txt
invalid-flight.flight
```

Observed result:

```txt
Flight plan import failed.
Errors:
 - SYNTAX_ERROR at line 5, column 4: missing ';' at 'aircraft'
```

### 24.3 Lexical Error

File:

```txt
lexical-invalid-flight.flight
```

Observed result:

```txt
Flight plan import failed.
Errors:
 - LEXICAL_ERROR at line 3, column 10: token recognition error at: 'OP@'
 - SYNTAX_ERROR at line 3, column 14: extraneous input '->' expecting AIRPORT_CODE
 - SYNTAX_ERROR at line 3, column 20: mismatched input ';' expecting '->'
```

### 24.4 Semantic Error

File:

```txt
semantic-invalid-flight.flight
```

Observed result:

```txt
Flight plan import failed.
Errors:
 - FUEL_ERROR at line 20, column 8: Leg 1 fuel quantity must be positive.
```

### 24.5 Valid DSL But Missing Route

File:

```txt
valid-flight.flight
```

Condition:

```txt
No FlightRoute exists with route code TP123.
```

Observed result:

```txt
Flight plan import failed.
Reason: ROUTE_ERROR: No existing flight route was found for route code TP123.
```

### 24.6 Successful Import

File:

```txt
valid-flight.flight
```

Required domain data:

```txt
Existing FlightRoute: TP123, OPO -> LIS, REGULAR
Schedule allows: Wednesday at 10:30
Existing Aircraft: CS-TAB
Aircraft belongs to company TP
Authenticated Pilot belongs to company TP
```

Observed result:

```txt
Imported Flight Plan Created
----------------------------
Route          : TP123
Aircraft       : CS-TAB
Departure      : 2026-06-10T10:30
Arrival        : 2026-06-10T11:20
Fuel           : 5000.0 KG
Status         : DRAFT

Flight plan imported and created successfully.
The flight plan is now available in the system in DRAFT status.
```

The created flight plan then appears in:

```txt
List My Flight Plans
```

---

## 25. Design Decisions

### 25.1 The DSL Does Not Create Routes

The import process does not create `FlightRoute` objects.

A flight plan must reference an already existing route.

This avoids mixing the responsibility of US121 with the flight route creation use case.

### 25.2 ImportedFlightPlan Is an Intermediate Model

`ImportedFlightPlan` is not the final domain aggregate.

It is an intermediate representation produced by the DSL parser and validator.

The final persisted object is the EAPLI `FlightPlan`.

### 25.3 FlightPlan Is Created in DRAFT

A flight plan imported from a file is created in `DRAFT` status.

The file was validated according to the DSL and the current domain data, but the flight plan must still undergo later testing and validation before being operationally used.

### 25.4 Original DSL Content Is Stored

The original `.flight` file content is stored as the `FlightPlanDescription`.

This preserves the formal flight plan description for future validation and testing.

### 25.5 Only Supported Formats Are Processed

The system validates the file format before processing.

Currently, only `.flight` files are supported.

Unsupported files are rejected before ANTLR processing.

### 25.6 Pilot Is Resolved From Authentication

The `FlightPlan` is associated with the authenticated Pilot.

The pilot identifier inside the DSL is kept as part of the original imported description.

The system validates that the authenticated Pilot is active and belongs to the route's company.

---

## 26. Dependencies

US121 depends on:

```txt
US120 - Flight Description DSL Specification and Validation
US080 - Create Flight Plan
US073 - Create Flight Route
US070 - Add Aircraft to Air Transport Company
US075 - Add Pilot
```

The use case also depends on existing repositories for:

```txt
FlightPlan
FlightRoute
Aircraft
Pilot
```

---

## 27. Conclusion

US121 imports a valid flight plan from a file into the AISafe system.

The file must first be validated as a supported `.flight` file.

Then it must pass the complete US120 validation pipeline, including lexical, syntactic and semantic analysis.

After that, the imported data is validated against the EAPLI domain.

Only if all validations succeed is a real EAPLI `FlightPlan` created and saved in the system.

The imported flight plan is created in `DRAFT` status and appears in the normal list of flight plans created by the authenticated Pilot.

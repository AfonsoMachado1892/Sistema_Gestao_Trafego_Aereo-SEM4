# US050 — Register Air Control Area

## 1. Context

The goal of this user story is to allow a Backoffice Operator to register a new Air Control Area in the system.

An Air Control Area represents a controlled rectangular geographical region used later to associate airports with a valid operational area. Each area is defined by an area code, a name, rectangular geographic boundaries, and a configured vertical range.

---

## 2. Requirements

**US050** As a Backoffice Operator, I want to register an air control area.

**Acceptance Criteria:**

- US050.1 The Backoffice Operator must be able to register a new air control area by providing a unique area code and geographic boundaries.
- US050.2 The area code must be unique across the system; duplicate codes must be rejected.
- US050.3 Geographic boundaries must be valid.
- US050.4 A bootstrap process must also be capable of registering air control areas.
- US050.5 A success message must be shown upon successful registration.
- US050.6 Invalid or incomplete input must produce clear and informative error messages.

**Dependencies/References:**

- This US is a prerequisite for US052, since airports must be associated with an existing Air Control Area.
- Uses the shared `Coordinates` value object to represent geographic points.
- Uses the Backoffice application, since the operation is performed by a Backoffice Operator.

---

## 3. Client Specifications and Clarifications

During clarification with the client, the following additional domain rules were identified:

- Air Control Areas are always rectangular.
- Each Air Control Area is defined by two opposite corners:
    - southwest corner;
    - northeast corner.
- Air Control Areas extend vertically from ground level to a configured upper altitude.
- For the current scope, all Air Control Areas can be assumed to extend from `0 m` to `14000 m`.
- The value `14000 m` must not be hardcoded throughout the application. It must be centralized as a configured/default value.
- Air Control Areas must not overlap.
- The name of an Air Control Area should also be unique, as it identifies the area in business terms.

---

## 4. Analysis

The domain model defines `AirControlArea` as an Aggregate Root identified by `AirControlAreaCode`.

The geographic limits of an Air Control Area are represented by the `GeographicBoundary` value object. Following the client clarification, the boundary is represented as a rectangular area defined by:

- southwest corner;
- northeast corner;
- lower altitude;
- upper altitude.

The lower altitude is always `0 m`, while the upper altitude is obtained from `AirControlAreaDefaults.DEFAULT_UPPER_ALTITUDE_METERS`.

Key business rules identified:

- An Air Control Area must have a valid code.
- An Air Control Area must have a valid and unique name.
- The area code must be unique in the system.
- The geographic boundary must be valid:
    - southwest latitude must be lower than northeast latitude;
    - southwest longitude must be lower than northeast longitude;
    - coordinates must respect valid latitude/longitude ranges.
- Air Control Areas must not overlap.
- The vertical range is automatically configured as `0 m` to the configured upper altitude.
- Bootstrap must also be able to register Air Control Areas.

### System Sequence Diagram (SSD)

![US050 SSD](docs/sprint2/us_50/analysis/png/US050_SSD.png)

---

## 5. Design

### 5.1. Realization

**Sequence Diagram (SD):**

![US050 SD](docs/sprint2/us_50/design/png/US050_SD.png)

**Class Diagram (CD):**

![US050 CD](docs/sprint2/us_50/design/png/US050_CD.png)

---

## 6. Implementation

The implementation follows the layered architecture promoted by the EAPLI Framework.

- **Domain:**
    - `AirControlArea` is the Aggregate Root.
    - `AirControlAreaCode` identifies the aggregate.
    - `GeographicBoundary` represents the rectangular geographic and vertical limits.
    - `AirControlAreaDefaults` centralizes the configured altitude defaults.
    - `AirControlAreaFactory` centralizes aggregate creation.

- **Domain Service:**
    - `AirControlAreaService` coordinates business rules that require repository access:
        - unique area code;
        - unique area name;
        - no overlap with existing Air Control Areas.

- **Repository:**
    - `AirControlAreaRepository` defines persistence operations.
    - `InMemoryAirControlAreaRepository` provides an in-memory implementation.

- **Application:**
    - `RegisterAirControlAreaController` handles authorization and delegates business logic to the service.

- **Presentation:**
    - `RegisterAirControlAreaUI` allows the Backoffice Operator to register a new Air Control Area.
    - `ListAirControlAreasUI` allows registered areas to be listed for validation/demonstration purposes.
    - The UI displays the configured altitude range but does not ask the operator to provide it, since the range is defined by system configuration.

- **Bootstrap:**
    - `AirControlAreaBootstrapper` registers initial Air Control Areas during bootstrap.
    - Bootstrap uses the service directly instead of the controller, since bootstrap is an internal system process and should not depend on an authenticated user session.

The `PersistenceContext` provides access to repositories, allowing the application layer to remain decoupled from the persistence implementation.

---

## 7. Integration/Demonstration

This US integrates with:

- **US052 — Register Airport**
    - Airports must be associated with an existing Air Control Area.
    - The airport coordinates are later validated against the selected Air Control Area boundary.

- **Backoffice Menu**
    - The operation is available through the Backoffice configuration menu.
    - The operator can register and list Air Control Areas.

- **Bootstrap Process**
    - Air Control Areas are created during bootstrap to provide base data for airport registration.
    - In the current in-memory setup, bootstrap data must be loaded when the application starts, since in-memory data does not persist between application executions.

---

## 8. Observations

- The original design already represented Air Control Areas as rectangular bounding boxes using southwest and northeast coordinates. This was later confirmed by the client clarification, so no major redesign was required.
- The `GeographicBoundary` value object was extended to include vertical limits. Since all Air Control Areas currently use the same vertical range, the UI does not ask the operator for altitude data.
- The altitude values are centralized in `AirControlAreaDefaults` to avoid spreading magic numbers such as `14000` throughout the codebase.
- The system assumes a configured lower altitude of `0 m` and a configured upper altitude of `14000 m`.
- The no-overlap rule is enforced in the service, because checking overlap requires access to existing Air Control Areas.
- Bootstrap uses `AirControlAreaService` directly instead of `RegisterAirControlAreaController` to avoid authorization problems, since there is no authenticated user during bootstrap execution.
- A list UI was added even though it was not explicitly required, because it helps demonstrate and validate registered Air Control Areas.
- Some bootstrap Air Control Areas are simplified rectangular representations created for system testing/demo purposes and do not represent official real-world FIR boundaries.
- The current implementation is compatible with in-memory persistence and can later be used with a persistent repository implementation.
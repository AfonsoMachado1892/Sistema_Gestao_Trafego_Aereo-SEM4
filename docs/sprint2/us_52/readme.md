# US052 — Register Airport

## 1. Context

The goal of this user story is to allow a Backoffice Operator to register an airport in the system and associate it with exactly one existing Air Control Area.

An airport is identified by its IATA code and also stores its ICAO code, name, locality, geographic coordinates, elevation, and the Air Control Area to which it belongs.

---

## 2. Requirements

**US052** As a Backoffice Operator, I want to register an airport in a given air control area.

**Acceptance Criteria:**

- US052.1 The Backoffice Operator must be able to register an airport by providing: name, town, country, IATA code, ICAO code, coordinates, and elevation.
- US052.2 The airport must be associated with exactly one existing air control area.
- US052.3 The IATA code must be exactly 3 letters and unique worldwide within the system.
- US052.4 The ICAO code must be exactly 4 letters and unique worldwide within the system.
- US052.5 Coordinates must be valid:
    - latitude between `-90` and `90`;
    - longitude between `-180` and `180`.
- US052.6 Elevation must be expressed in meters above sea level and must be a valid numeric value.
- US052.7 A bootstrap process must also be capable of registering airports.
- US052.8 A success message must be shown upon successful registration.
- US052.9 Invalid or incomplete input must produce clear and informative error messages.

**Dependencies/References:**

- Depends on US050, since an airport must be associated with an existing Air Control Area.
- Uses the shared `Coordinates` value object.
- Uses `Country` and `IsoCode` from the shared kernel.
- Airport data provided in the XML dataset was used as reference data for bootstrap entries.

---

## 3. Client Specifications and Clarifications

During clarification with the client, the following additional rules were identified:

- An airport must belong to exactly one Air Control Area.
- The selected Air Control Area must already exist in the system.
- The airport coordinates must be inside the selected Air Control Area.
- If the airport coordinates are outside the selected area, the registration must be rejected.
- Country data can be treated as base data.
- The XML dataset provides useful airport reference data, but it does not include all domain information required by this US.

The provided XML dataset includes:

- IATA code;
- airport name;
- town;
- country name;
- latitude;
- longitude;
- elevation.

However, it does not include:

- ICAO code;
- country ISO code;
- Air Control Area code.

Therefore, the bootstrap process enriches the XML reference data manually with the missing information.

---

## 4. Analysis

The domain model defines `Airport` as an Aggregate Root identified by `AirportIataCode`.

The airport stores:

- IATA code;
- ICAO code;
- airport name;
- locality;
- coordinates;
- elevation;
- Air Control Area code.

The `Locality` value object groups the town/city and country. The country is represented through the shared `Country` value object, which contains an `IsoCode`.

Key business rules identified:

- IATA code must contain exactly 3 letters.
- ICAO code must contain exactly 4 letters.
- IATA code must be unique within the system.
- ICAO code must be unique within the system.
- The selected Air Control Area must exist.
- The airport coordinates must be inside the selected Air Control Area.
- Coordinates must be valid latitude/longitude values.
- Elevation must be numeric and represented in meters above sea level.
- Bootstrap must also be able to register airports.

The validation that an airport is inside an Air Control Area is performed using `GeographicBoundary.contains(coordinates)`.

### System Sequence Diagram (SSD)

![US052 SSD](analysis/US052_SSD.png)

---

## 5. Design

### 5.1. Realization

**Sequence Diagram (SD):**

![US052 SD](design/png/US052_SD.png)

**Class Diagram (CD):**

![US052 CD](design/png/US052_CD.png)

---

## 6. Implementation

The implementation follows the layered architecture promoted by the EAPLI Framework.

- **Domain:**
    - `Airport` is the Aggregate Root.
    - `AirportIataCode` identifies the aggregate.
    - `AirportIcaoCode` represents the ICAO code.
    - `Elevation` represents the airport elevation in meters.
    - `Locality` groups city/town and country.
    - `AirportFactory` centralizes aggregate creation.

- **Shared Kernel:**
    - `Coordinates` validates latitude and longitude.
    - `Country` represents the country associated with the airport locality.
    - `IsoCode` represents the country ISO code.

- **Domain Service:**
    - `AirportService` coordinates business rules that require repository access:
        - unique IATA code;
        - unique ICAO code;
        - existence of the selected Air Control Area;
        - validation that the airport coordinates are inside the selected Air Control Area.

- **Repository:**
    - `AirportRepository` defines persistence operations.
    - `InMemoryAirportRepository` provides an in-memory implementation.

- **Application:**
    - `RegisterAirportController` handles authorization and delegates business logic to the service.

- **Presentation:**
    - `RegisterAirportUI` allows the Backoffice Operator to register a new airport.
    - `ListAirportsUI` allows registered airports to be listed for validation/demonstration purposes.
    - The UI includes a cancellation option, allowing the user to press `0` to go back to the menu.

- **Bootstrap:**
    - `AirportBootstrapper` registers airport data during bootstrap.
    - The bootstrapper uses airport data from the provided XML as reference and enriches it with ICAO codes, ISO country codes, and Air Control Area associations.
    - `AirportBootstrapper` must run after `AirControlAreaBootstrapper`, because every airport must reference an existing Air Control Area.

The `PersistenceContext` provides access to repositories, allowing the application layer to remain decoupled from the persistence implementation.

---

## 7. Integration/Demonstration

This US integrates with:

- **US050 — Register Air Control Area**
    - The airport must reference an existing Air Control Area.
    - The selected Air Control Area is used to validate whether the airport coordinates are within its boundaries.

- **Backoffice Menu**
    - The operation is available through the Backoffice configuration menu.
    - The operator can register and list airports.

- **Bootstrap Process**
    - Airports are registered during bootstrap after the Air Control Areas are created.
    - This order is mandatory because the airport registration process validates the existence of the selected Air Control Area.

- **Provided XML Dataset**
    - The XML dataset was used as reference data for the airport bootstrap.
    - Since the XML does not include all required domain fields, the bootstrapper explicitly provides the missing information.

---

## 8. Observations

- The XML dataset was not imported directly. It was used as reference data for the bootstrap process.
- Direct XML import was not implemented because the XML does not include ICAO codes, ISO country codes, or Air Control Area associations, all of which are required by the domain model and acceptance criteria.
- The bootstrapper enriches the XML reference data manually with the missing values.
- The `Airport` aggregate stores the `AirControlAreaCode` instead of a direct object reference to `AirControlArea`, following DDD principles for cross-aggregate references.
- The airport-to-area validation is performed in `AirportService`, because it requires access to the `AirControlAreaRepository`.
- Elevation was modeled as a value object and supports decimal and negative values, since real airports may have decimal elevation values and some airports can be below sea level.
- The airport UI was improved with a cancellation option, allowing users to press `0` to return to the menu.
- A list UI was added even though it was not explicitly required, because it helps demonstrate and validate registered airports.
- Some Air Control Areas used for bootstrap are simplified rectangular representations created to support the airport dataset and system demonstration. They are not intended to represent official real-world FIR boundaries.
- Bootstrap order is important: Air Control Areas must be loaded before Airports.
- In the current in-memory setup, bootstrap data must be loaded when the application starts, since in-memory data does not persist between application executions.
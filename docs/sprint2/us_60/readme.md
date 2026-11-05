# US060 — Register an Air Transport Company

## 1. Context

The goal of this user story is to allow a Backoffice Operator to register an air transport company in the AISafe system.

Air transport companies are customers that later register aircraft, routes, pilots, and collaborators. Each company is identified by unique IATA and ICAO codes.

---

## 2. Requirements

**US060** As a Backoffice Operator, I want to register an air transport company. A company has a company name, and IATA (2 letters) and ICAO code (2-3 letters) that must be unique.

This must also be achievable by a bootstrap process.

**Acceptance Criteria:**

- US060.1 The Backoffice Operator must be able to register a company with IATA code, ICAO code, and name.
- US060.2 The IATA code must be exactly 2 letters.
- US060.3 The ICAO code must be 2 or 3 letters.
- US060.4 The IATA code must be unique in the system.
- US060.5 The ICAO code must be unique in the system.
- US060.6 Codes are normalized to upper case upon registration.
- US060.7 This must also be achievable by a bootstrap process.
- US060.8 A success message must be shown upon successful registration.
- US060.9 Invalid or incomplete input must produce clear and informative error messages.

**Dependencies/References:**

- Prerequisite for **US061 Add a Customer's Collaborator** (when customer is an air transport company).
- Prerequisite for **US070 Add an Aircraft to an Air Transport Company**.
- Uses **US030 Authentication and Authorization**.

---

## 3. Analysis

The domain model defines `AirTransportCompany` as an Aggregate Root identified by `AirTransportCompanyCode` (IATA code).

Key business rules identified:

- IATA code is a 2-letter value object (`AirTransportCompanyCode`).
- ICAO code is stored as a string with length between 2 and 3 characters.
- Company name is mandatory and trimmed.
- IATA and ICAO codes must be unique across all companies.
- Codes provided by the operator are normalized to upper case.

### System Sequence Diagram (SSD)

![US060 SSD](analysis/US060_SSD.png)

---

## 4. Design

### 4.1. Realization

**Sequence Diagram (SD):**

![US060 SD](design/US060_SD.png)

**Class Diagram (CD):**

![US060 CD](design/US060_CD.png)

### 4.2. Acceptance Tests

**Test 1:** Verifies successful registration.

**Refers to Acceptance Criteria:** US060.1, US060.8

```java
@Test
void ensureAirTransportCompanyIsRegisteredSuccessfully() {
    final AirTransportCompany company = controller.registerAirTransportCompany(
            "TP", "TAP", "TAP Air Portugal");
    assertEquals(AirTransportCompanyCode.of("TP"), company.identity());
}
```

**Test 2:** Verifies duplicate IATA code is rejected.

**Refers to Acceptance Criteria:** US060.4

```java
@Test
void ensureDuplicateIataCodeThrows() {
    assertThrows(IllegalStateException.class, () ->
            controller.registerAirTransportCompany("TP", "RYR", "Ryanair"));
}
```

**Test 3:** Verifies duplicate ICAO code is rejected.

**Refers to Acceptance Criteria:** US060.5

```java
@Test
void ensureDuplicateIcaoCodeThrows() {
    assertThrows(IllegalStateException.class, () ->
            controller.registerAirTransportCompany("FR", "TAP", "Ryanair"));
}
```

---

## 5. Implementation

- **Domain:**
  - `AirTransportCompany` is the Aggregate Root.
  - `AirTransportCompanyCode` validates the IATA code format.

- **Repository:**
  - `AirTransportCompanyRepository` with `findByIataCode` and `findByIcaoCode`.
  - `InMemoryAirTransportCompanyRepository` and JPA implementation.

- **Application:**
  - `RegisterAirTransportCompanyController` enforces authorization and uniqueness before save.

- **Presentation:**
  - `RegisterAirTransportCompanyUI` collects data, shows summary, and confirms registration.
  - Available in `BackofficeConfigUI`.

- **Bootstrap:**
  - `AirTransportCompanyBootstrapper` registers initial companies (TP, FR, LH) idempotently.

---

## 6. Integration/Demonstration

This US integrates with:

- **US061** — collaborators reference air transport companies.
- **US070** — aircraft are associated with a registered company.
- **Bootstrap** — demo companies are loaded at system startup.

---

## 7. Observations

- IATA code is the aggregate identity; ICAO code uniqueness is enforced separately.
- Bootstrap skips registration when codes already exist, supporting repeated bootstrap execution.
- The controller normalizes codes to upper case before validation and persistence.

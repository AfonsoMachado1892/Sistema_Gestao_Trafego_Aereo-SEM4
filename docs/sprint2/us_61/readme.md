# US061 — Add a Customer's Collaborator

## 1. Context

The goal is to allow a Backoffice Operator to register a collaborator for an existing AISafe customer.

In this context, a **customer** may be either:

- an `AirTransportCompany`;
- an `AirControlArea`.

The collaborator must also be registered as a system user, since collaborators need to authenticate and access the system according to their role.

---

## 2. Requirements

**US061** As a Backoffice Operator, I want to register a customer's collaborator.

A customer may be an air transport company or an air control area. The collaborator will also be a user of the system. Thus, each collaborator will be a distinct system user. There is no need to verify that the collaborator’s email is in the customer's domain. This could be extremely difficult or even impossible, and many users have emails outside the company’s main domain.

This must also be achievable by a bootstrap process.

**Acceptance Criteria:**

- **US061.1** The Backoffice Operator must be able to register a collaborator for an existing customer.
- **US061.2** The customer may be either an `AirTransportCompany` or an `AirControlArea`.
- **US061.3** The collaborator must be registered as a system user simultaneously.
- **US061.4** Required information includes name, email, phone number, and position/role within the customer.
- **US061.5** The email must be valid, but does not need to belong to the customer's domain.
- **US061.6** Each collaborator must correspond to a distinct system user.
- **US061.7** Duplicate collaborator emails must be rejected.
- **US061.8** The selected customer must already exist in the system.
- **US061.9** This must also be achievable by a bootstrap process.
- **US061.10** A success message must be shown upon successful registration.
- **US061.11** Invalid or incomplete input must produce clear and informative error messages.

**Dependencies/References:**

- Depends on **US030 Authentication and Authorization**, since the collaborator is also a system user.
- Depends on **US060 Register an Air Transport Company**, when the selected customer is an air transport company.
- Depends on **US050 Register an Air Control Area**, when the selected customer is an air control area.
- Supports **US062 List Customer's Collaborators**.
- Supports **US063 Edit Customer's Collaborator**.
- Supports **US064 Disable Customer's Collaborator**.
- Supports **US070 Add Aircraft to Air Transport Company**, since the authenticated collaborator’s company can be discovered through the `CustomerCollaborator`.

---

## 3. Analysis

The main domain concept introduced for this US is `CustomerCollaborator`.

A `CustomerCollaborator` represents a person associated with a customer of AISafe. However, the collaborator is also a system user. To avoid mixing authentication concerns with the business domain, the collaborator does not store a full `SystemUser` object. Instead, it stores only the `Username` that identifies the corresponding system user.

Therefore, the implementation creates:

```text
SystemUser
+
AiSafeUser
+
CustomerCollaborator
```

The responsibilities are separated as follows:

```text
SystemUser
→ authentication, authorization, username, name, email and roles

AiSafeUser
→ AISafe-specific user information, currently phone number

CustomerCollaborator
→ business association between a system user and a customer
```

The `CustomerCollaborator` aggregate stores:

```text
customerCollaboratorID
customerReference
email
username
position
status
```

The `CustomerReference` is a Value Object composed of:

```text
customerType
customerCode
```

This allows the collaborator to reference either an air transport company or an air control area without introducing an artificial `Customer` aggregate.

Examples:

```text
AIR_TRANSPORT_COMPANY + TP
AIR_CONTROL_AREA + KZLA
```

The role assigned to the created `SystemUser` depends on the selected customer type:

```text
AIR_TRANSPORT_COMPANY → AIR_TRANSPORT_COMPANY_COLLABORATOR
AIR_CONTROL_AREA → FLIGHT_CONTROL_OPERATOR
```

This follows the clarification that a Flight Control Operator is associated with an Air Control Area.

### Design decisions

- A new aggregate, `CustomerCollaborator`, was introduced.
- No generic `Customer` aggregate was created, because the actual customer aggregates already exist: `AirTransportCompany` and `AirControlArea`.
- `CustomerReference` was modeled as a Value Object because the pair `customerType + customerCode` has no lifecycle of its own and is persisted as part of `CustomerCollaborator`.
- `CustomerCollaborator` stores `Username` rather than `SystemUser`, following DDD principles of referencing external aggregates/contexts by identity.
- The collaborator’s `PhoneNumber` is stored in `AiSafeUser`.
- The collaborator’s email is stored as an `Email` Value Object and is also used when creating the `SystemUser`.
- The system does not validate whether the email belongs to the customer’s domain, as explicitly required.
- Positions are selected from predefined UI lists according to the customer type, while still being stored as a `String` in the domain.

### System Sequence Diagram (SSD)

![US061 SSD](analysis/US061_SSD.png)

---

## 4. Design

### 4.1. Realization

**Sequence Diagram (SD):**

![US061 SD](design/png/US061_SD.png)

**Class Diagram (CD):**

![US061 CD](design/png/US061_CD.png)

---

## 5. Implementation

The implementation follows the project’s layered architecture:

- **Presentation:** `AddCustomerCollaboratorUI`
- **Application/Service:** `AddCustomerCollaboratorController`, `CustomerCollaboratorService`
- **Domain:** `CustomerCollaborator`, `CustomerCollaboratorID`, `CustomerReference`, `CustomerType`, `CustomerStatus`, `CustomerCollaboratorFactory`
- **Repository:** `CustomerCollaboratorRepository`, `InMemoryCustomerCollaboratorRepository`
- **User Management Integration:** `SystemUser`, `AiSafeUser`, `AiSafeRoles`, `PhoneNumber`, `Email`
- **Persistence:** `RepositoryFactory`, `PersistenceContext`

The `AddCustomerCollaboratorUI` allows the Backoffice Operator to:

1. choose the customer type;
2. select an existing customer from a list;
3. provide username, password, name, email and phone number;
4. select a predefined position according to the customer type;
5. confirm the registration.

The `AddCustomerCollaboratorController` is responsible for:

- checking that the authenticated user is a `BACKOFFICE_OPERATOR` or `ADMIN`;
- assigning the correct role according to the customer type;
- creating the `SystemUser`;
- creating the `AiSafeUser` with the phone number;
- delegating the creation of the `CustomerCollaborator` to the service.

The `CustomerCollaboratorService` is responsible for:

- validating that the referenced customer exists;
- validating that the username is not already associated with another customer collaborator;
- validating that the collaborator email is not already used;
- creating the aggregate through `CustomerCollaboratorFactory`;
- saving it through `CustomerCollaboratorRepository`.

The `CustomerCollaboratorFactory` centralizes aggregate creation and generates a new unique `CustomerCollaboratorID`.

The `CustomerCollaborator` aggregate starts with status:

```text
ACTIVE
```

It may later be disabled by US064.

The `PersistenceContext` provides access to repositories, keeping the implementation compatible with the project’s database-by-configuration requirement.

---

## 6. Integration/Demonstration

This US integrates with:

- **Authentication and Authorization** — a `SystemUser` is created using the EAPLI framework’s `UserManagementService`.
- **AiSafe User Management** — an `AiSafeUser` is created to store the collaborator’s phone number.
- **AirTransportCompany aggregate** — when the selected customer type is `AIR_TRANSPORT_COMPANY`, the system validates the customer using `AirTransportCompanyRepository`.
- **AirControlArea aggregate** — when the selected customer type is `AIR_CONTROL_AREA`, the system validates the customer using `AirControlAreaRepository`.
- **Backoffice Menu** — the UI is available through `Backoffice Config > Add Customer Collaborator`.
- **US062** — collaborators can be listed by customer through the stored `CustomerReference`.
- **US064** — collaborators can later be disabled using their `CustomerStatus`.
- **US070** — when an Air Transport Company Collaborator is authenticated, the system can discover the company through the collaborator’s `CustomerReference`.

Example successful execution:

```text
Customer type:
1 - Air Transport Company
2 - Air Control Area
Option: 2

Available Air Control Areas:
1 - KZLA - Los Angeles FIR
...

Select air control area: 1

Username: jose.cardoso
Password: Password1
First name: José
Last name: Cardoso
Email: jose.cardoso@example.com
Phone country code: 351
Phone number: 910291392

Air Control Area collaborator position:
1 - Flight Control Operator
2 - Flight Plan Validation Operator
3 - Air Traffic Simulation Operator
4 - Area Control Supervisor

Option: 1

Customer collaborator registered successfully.
```

---

## 7. Observations

- The term “customer” represents an AISafe customer, not an airline passenger.
- A customer may be either an air transport company or an air control area.
- A `CustomerCollaborator` is always associated with a distinct `SystemUser`, but it is not itself a `SystemUser`; it stores only the `Username`.
- The selected customer is represented by `CustomerReference`, a Value Object composed of `CustomerType` and `customerCode`.
- The system does not verify whether the collaborator’s email belongs to the customer’s domain, as required.
- The collaborator’s role is inferred from the selected customer type:
    - `AIR_TRANSPORT_COMPANY` gives role `AIR_TRANSPORT_COMPANY_COLLABORATOR`;
    - `AIR_CONTROL_AREA` gives role `FLIGHT_CONTROL_OPERATOR`.
- The UI restricts available positions to predefined options to avoid meaningless free-text values.
- The option currently named “Disable / Enable Backoffice User” may show customer collaborators because they are also `SystemUser`s. A clearer menu name would be “Disable / Enable System User”.
- A bootstrap process is required by the acceptance criteria. If not yet implemented, `CustomerCollaboratorBootstrapper` must be added and executed after the relevant customers are bootstrapped.
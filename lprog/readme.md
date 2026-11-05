# US083 - Flight Description DSL Specification and Validation

## 1. Objective

The goal of this user story is to specify and implement a Domain Specific Language, DSL, for describing flight plans.

The DSL allows flight plans to be written in a structured text format and validated automatically.

The validation process includes:

```txt
lexical validation
syntactic validation
semantic validation
internal representation creation
```

The DSL is implemented using ANTLR. The implementation language is Java.

The system reads a `.flight` file, validates it, reports errors when they exist, and builds an internal representation when the file is valid.

---

## 2. User Story

As a Project Manager, I want the team to specify and implement the Flight Description DSL, so that flight plans can be formally defined and validated.

---

## 3. Acceptance Criteria

The official acceptance criteria for this user story are:

```txt
1. The informal lexical and syntactic specification of the DSL is documented.
2. A formal grammar is defined using ANTLR.
3. The system performs lexical and syntactic validation.
4. An internal representation, AST or domain objects, is produced.
5. Invalid inputs generate clear and informative error messages.
```

---

## 4. Acceptance Criteria Compliance

| Acceptance Criteria | Status | Evidence |
|---|---|---|
| The informal lexical and syntactic specification of the DSL is documented. | Done | Sections 7 and 8 document the lexical and syntactic specification. |
| A formal grammar is defined using ANTLR. | Done | The grammar is implemented in `lprog/grammar/FlightPlan.g4`. |
| The system performs lexical and syntactic validation. | Done | `FlightPlanParserService` uses the generated `FlightPlanLexer` and `FlightPlanParser`. Syntax errors are collected by `FlightPlanSyntaxErrorListener`. |
| An internal representation, AST or domain objects, is produced. | Done | `FlightPlanBuilderVisitor` creates an `ImportedFlightPlan` object with route, legs, segments, coordinates and fuel. |
| Invalid inputs generate clear and informative error messages. | Done | Syntax and semantic errors include an error type, line, column and message. |

---

## 5. Current Artifacts

The LPROG artifacts are located in:

```txt
lprog/
 ├── pom.xml
 ├── example/
 │   ├── valid-flight.flight
 │   ├── invalid-flight.flight
 │   ├── semantic-invalid-flight.flight
 │   └── invalid-date-flight.flight
 ├── generated/
 ├── grammar/
 │   └── FlightPlan.g4
 ├── src/
 │   └── main/
 │       └── java/
 │           ├── dsl/
 │           │   ├── FlightPlanDslApp.java
 │           │   ├── listener/
 │           │   │   └── FlightPlanStructureListener.java
 │           │   ├── model/
 │           │   │   ├── ImportedCoordinate.java
 │           │   │   ├── ImportedFlightPlan.java
 │           │   │   ├── ImportedFuel.java
 │           │   │   ├── ImportedLeg.java
 │           │   │   ├── ImportedRoute.java
 │           │   │   └── ImportedSegment.java
 │           │   ├── parser/
 │           │   │   ├── FlightPlanImportService.java
 │           │   │   ├── FlightPlanParserService.java
 │           │   │   └── FlightPlanSyntaxErrorListener.java
 │           │   ├── validation/
 │           │   │   ├── FlightPlanSemanticValidator.java
 │           │   │   └── SemanticError.java
 │           │   └── visitor/
 │           │       └── FlightPlanBuilderVisitor.java
 │           └── grammar/
 │               ├── FlightPlanLexer.java
 │               ├── FlightPlanParser.java
 │               ├── FlightPlanBaseVisitor.java
 │               ├── FlightPlanBaseListener.java
 │               ├── FlightPlanVisitor.java
 │               └── FlightPlanListener.java
 └── trees/
     ├── valid_tree.svg
     └── invalid_tree.svg
```

---

## 6. DSL Example

Example of a valid flight plan:

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

This example describes:

```txt
one flight
one route
one aircraft
one pilot
one leg
one segment
one fuel declaration
```

---

## 7. Informal Lexical Specification

The lexical specification defines the basic elements recognized by the language.

The DSL contains:

```txt
keywords
identifiers
airport codes
numeric literals
date literals
time literals
units
separators
symbols
```

---

### 7.1 Keywords

The following keywords are supported:

```txt
flight
route
aircraft
pilot
leg
departure
arrival
segment
fuel
at
altitude
width
wind
regular
charter
```

Keywords are case-insensitive.

Examples:

```txt
flight
Flight
FLIGHT
```

All these examples are accepted as the same keyword.

This is implemented in ANTLR using rules such as:

```antlr
FLIGHT: [fF][lL][iI][gG][hH][tT];
ROUTE: [rR][oO][uU][tT][eE];
```

---

### 7.2 Identifiers

The DSL contains identifier-like values.

```txt
FLIGHT_ID
AIRCRAFT_REGISTRATION
PILOT_ID
```

Examples:

```txt
TP123
CS-TAB
P001
```

Identifiers are case-sensitive.

For example, `TP123` and `tp123` are not treated as the same value.

---

### 7.3 Airport Codes

Airport codes are represented by the `AIRPORT_CODE` token.

Examples:

```txt
OPO
LIS
LPPT
```

The grammar accepts airport codes with three or four uppercase letters.

This supports both IATA-like and ICAO-like formats.

The grammar validates the format of the airport code. It does not verify whether the airport exists in the application database. That check belongs to the integration with the domain layer.

---

### 7.4 Numeric Literals

The DSL supports integer and floating-point numeric values.

Examples:

```txt
5000
10000
41.242
-8.678
```

Negative numeric values are accepted lexically and syntactically.

This is intentional because the grammar only validates the structure of the text. Domain-specific restrictions, such as fuel quantity being positive, are checked during semantic validation.

Example:

```txt
fuel -5000 kg;
```

This input is syntactically valid but semantically invalid.

---

### 7.5 Date and Time Literals

Dates and times follow fixed textual formats.

```txt
DATE  format: YYYY-MM-DD
TIME  format: HH:MM
```

Examples:

```txt
2026-06-10
10:30
```

The grammar validates the format. Semantic validation checks whether the date and time values are valid calendar/time values.

Example of invalid date:

```txt
2026-99-99
```

Example of invalid time:

```txt
99:99
```

---

### 7.6 Units

The following units are supported:

```txt
kg      fuel quantity in kilograms
l       fuel quantity in liters
m       distance, altitude or width in meters
deg     wind direction in degrees
m/s     wind speed in meters per second
```

---

### 7.7 Separators and Symbols

The DSL uses the following symbols:

```txt
{       LBRACE
}       RBRACE
(       LPAREN
)       RPAREN
,       COMMA
;       SEMI
->      ARROW
```

Examples:

```txt
route OPO -> LIS;
```

The `->` symbol represents movement from one airport to another.

The `;` symbol is used to terminate simple statements.

Whitespace, tabs and line breaks are ignored by the grammar.

---

## 8. Informal Syntactic Specification

The DSL follows a hierarchical block-based structure.

At the top level, a file contains exactly one flight plan.

```txt
flightPlan
 └── flight
```

A `flight` contains:

```txt
flight
 ├── flight identifier
 ├── flight type
 ├── route
 ├── aircraft
 ├── pilot
 └── one or more legs
```

A `leg` contains:

```txt
leg
 ├── departure
 ├── arrival
 ├── one or more segments
 └── fuel
```

A `segment` contains:

```txt
segment
 ├── start coordinate
 ├── end coordinate
 ├── altitude
 ├── width
 └── wind
```

---

### 8.1 Flight Structure

A flight has the following general format:

```txt
flight <flight_id> <flight_type> {
    route ...
    aircraft ...
    pilot ...
    leg ...
}
```

Example:

```txt
flight TP123 regular {
    ...
}
```

The supported flight types are:

```txt
regular
charter
```

---

### 8.2 Route Structure

A route defines the origin and destination airports.

```txt
route OPO -> LIS;
```

This follows the structure:

```txt
ROUTE AIRPORT_CODE ARROW AIRPORT_CODE SEMI
```

---

### 8.3 Aircraft Structure

The aircraft statement identifies the aircraft registration.

```txt
aircraft CS-TAB;
```

---

### 8.4 Pilot Structure

The pilot statement identifies the pilot.

```txt
pilot P001;
```

---

### 8.5 Leg Structure

A leg is represented as a block.

```txt
leg {
    departure OPO at 2026-06-10 10:30;
    arrival LIS at 2026-06-10 11:20;

    segment {
        ...
    }

    fuel 5000 kg;
}
```

Each leg must contain:

```txt
one departure
one arrival
one or more segments
one fuel declaration
```

---

### 8.6 Segment Structure

A segment is represented as a block.

```txt
segment {
    (41.242, -8.678) -> (38.774, -9.134)
    altitude 10000 m;
    width 500 m;
    wind 90 deg 12 m/s;
}
```

A segment contains:

```txt
start coordinate
end coordinate
altitude
width
wind
```

---

### 8.7 Statement Termination

Simple statements end with `;`.

Examples:

```txt
route OPO -> LIS;
aircraft CS-TAB;
pilot P001;
departure OPO at 2026-06-10 10:30;
arrival LIS at 2026-06-10 11:20;
fuel 5000 kg;
```

Blocks are delimited by `{` and `}`.

Examples:

```txt
flight TP123 regular {
    ...
}
```

```txt
leg {
    ...
}
```

```txt
segment {
    ...
}
```

---

## 9. Formal ANTLR Grammar

The formal grammar is implemented in:

```txt
lprog/grammar/FlightPlan.g4
```

The grammar contains both parser rules and lexer rules.

Parser rules define the structure of the language.

Lexer rules define the tokens of the language.

---

### 9.1 Main Parser Rules

```antlr
flightPlan
    : flight EOF
    ;

flight
    : FLIGHT flightID flightType LBRACE
        route
        aircraft
        pilot
        leg+
      RBRACE
    ;

route
    : ROUTE origin=airportCode ARROW destination=airportCode SEMI
    ;

aircraft
    : AIRCRAFT aircraftRegistration SEMI
    ;

pilot
    : PILOT pilotID SEMI
    ;

leg
    : LEG LBRACE
        departure
        arrival
        segment+
        fuel
      RBRACE
    ;

departure
    : DEPARTURE airportCode AT date time SEMI
    ;

arrival
    : ARRIVAL airportCode AT date time SEMI
    ;

segment
    : SEGMENT LBRACE
        start=coordinate ARROW end=coordinate
        altitude
        width
        wind
      RBRACE
    ;

fuel
    : FUEL quantity=number unit=fuelUnit SEMI
    ;
```

---

### 9.2 Main Lexer Rules

```antlr
FLIGHT: [fF][lL][iI][gG][hH][tT];
ROUTE: [rR][oO][uU][tT][eE];
AIRCRAFT: [aA][iI][rR][cC][rR][aA][fF][tT];
PILOT: [pP][iI][lL][oO][tT];
LEG: [lL][eE][gG];
DEPARTURE: [dD][eE][pP][aA][rR][tT][uU][rR][eE];
ARRIVAL: [aA][rR][rR][iI][vV][aA][lL];
SEGMENT: [sS][eE][gG][mM][eE][nN][tT];
FUEL: [fF][uU][eE][lL];

AT: [aA][tT];
ALTITUDE: [aA][lL][tT][iI][tT][uU][dD][eE];
WIDTH: [wW][iI][dD][tT][hH];
WIND: [wW][iI][nN][dD];

REGULAR: [rR][eE][gG][uU][lL][aA][rR];
CHARTER: [cC][hH][aA][rR][tT][eE][rR];

KG: [kK][gG];
MS: [mM] '/' [sS];
DEG: [dD][eE][gG];
M: [mM];
L: [lL];

LBRACE: '{';
RBRACE: '}';
LPAREN: '(';
RPAREN: ')';
COMMA: ',';
SEMI: ';';
ARROW: '->';

DATE: [0-9][0-9][0-9][0-9] '-' [0-9][0-9] '-' [0-9][0-9];
TIME: [0-9][0-9] ':' [0-9][0-9];

FLIGHT_ID: [A-Z][A-Z] [0-9][0-9][0-9] [0-9]? [A-Z]?;
AIRPORT_CODE: [A-Z][A-Z][A-Z] [A-Z]?;
AIRCRAFT_REGISTRATION: [A-Z][A-Z] '-' [A-Z0-9]+;
PILOT_ID: [A-Z] [0-9][0-9][0-9]+;

FLOAT: '-'? [0-9]+ '.' [0-9]+;
INT: '-'? [0-9]+;

WS: [ \t\r\n]+ -> skip;
```

---

## 10. Design Decisions

### 10.1 Block-Based Structure

The DSL uses a block-based structure because flight plans are naturally hierarchical.

A flight contains legs, and each leg contains departure, arrival, segments and fuel.

This structure improves readability and makes the DSL close to the problem domain.

---

### 10.2 Fixed Declaration Order

The grammar uses a fixed declaration order.

Inside a flight, the expected order is:

```txt
route
aircraft
pilot
leg+
```

Inside a leg, the expected order is:

```txt
departure
arrival
segment+
fuel
```

This decision makes the grammar simpler, deterministic and easier to validate.

It also makes error reporting more predictable.

---

### 10.3 One or More Legs

The grammar uses `leg+`.

This means that a flight must have at least one leg, but it may have more.

This supports both direct flights and flights with intermediate stops.

---

### 10.4 One or More Segments

The grammar uses `segment+`.

This means that each leg must have at least one segment.

This supports legs composed of one or more path segments.

---

### 10.5 Fuel Belongs to a Leg

Fuel is modeled inside the `leg` block.

Fuel is not modeled inside the `segment` block.

This reflects the interpretation that fuel quantity is associated with a flight leg rather than with each individual segment.

---

### 10.6 Segment as a Block

The `segment` was modeled as a block because it contains several related values:

```txt
start coordinate
end coordinate
altitude
width
wind
```

Using a block makes the segment easier to read and easier to extend.

---

### 10.7 Negative Numbers Are Accepted by the Grammar

The grammar accepts negative integers and floats.

This is intentional.

The objective is to separate syntactic validation from semantic validation.

For example:

```txt
fuel -5000 kg;
```

is syntactically valid, but semantically invalid.

This allows the semantic validator to produce a domain-specific error message.

---

### 10.8 Labels in Parser Rules

Labels are used to improve readability and simplify Java processing.

Example:

```antlr
route
    : ROUTE origin=airportCode ARROW destination=airportCode SEMI
    ;
```

This makes it easier to access the origin and destination airports when building the internal representation.

---

### 10.9 Visitor and Listener

The grammar was generated with visitor and listener support.

The `FlightPlanBuilderVisitor` traverses the parse tree and builds the internal representation.

The `FlightPlanStructureListener` collects structural information from the parse tree, such as:

```txt
number of flights
number of legs
number of segments
```

---

## 11. Validation Pipeline

The complete validation pipeline is:

```txt
.flight file
    ↓
FlightPlanLexer
    ↓
FlightPlanParser
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
ImportResult
```

---

## 12. Lexical, Syntactic and Semantic Analysis

### 12.1 Lexical Analysis

Lexical analysis divides the input text into tokens.

Example:

```txt
fuel 5000 kg;
```

is divided into:

```txt
FUEL INT KG SEMI
```

Another example:

```txt
route OPO -> LIS;
```

is divided into:

```txt
ROUTE AIRPORT_CODE ARROW AIRPORT_CODE SEMI
```

---

### 12.2 Syntactic Analysis

Syntactic analysis verifies whether the tokens appear in the correct order according to the grammar.

Valid example:

```txt
route OPO -> LIS;
```

Invalid example:

```txt
route -> OPO LIS;
```

The tokens exist, but their order does not match the grammar.

---

### 12.3 Semantic Analysis

Semantic analysis verifies whether the content makes sense in the problem domain.

Example:

```txt
fuel -5000 kg;
```

This input is lexically and syntactically valid, but semantically invalid because fuel quantity cannot be negative.

Semantic validation is implemented in Java through:

```txt
FlightPlanSemanticValidator
```

---

## 13. Internal Representation

The system builds an internal representation from a valid flight plan file.

The internal representation is composed of:

```txt
ImportedFlightPlan
ImportedRoute
ImportedLeg
ImportedSegment
ImportedCoordinate
ImportedFuel
```

This representation is built by:

```txt
FlightPlanBuilderVisitor
```

The visitor traverses the ANTLR parse tree and extracts the relevant values from the DSL file.

This intermediate representation separates the DSL structure from the main DDD domain model.

This design allows the DSL to contain concepts such as:

```txt
legs
segments
coordinates
altitude
width
wind
```

without forcing immediate changes to the main application domain model.

After successful validation, the imported representation can later be mapped to the application domain objects during US081.

---

## 14. Semantic Validation

Semantic validation is implemented in Java through:

```txt
FlightPlanSemanticValidator
```

The validator collects all semantic errors in a list instead of stopping at the first error.

Implemented semantic rules include:

```txt
flight identifier cannot be empty
flight type must be valid
each flight must contain at least one leg
each leg must contain exactly one departure and one arrival
each leg must contain at least one segment
fuel quantity must be positive
departure date/time must be valid
arrival date/time must be valid
departure time must be before arrival time
arrival airport of leg n must match departure airport of leg n+1
arrival time of leg n must be before departure time of leg n+1
route origin must match the first leg departure airport
route destination must match the last leg arrival airport
the same airport cannot be visited twice
segment start and end coordinates must be different
segment altitude must be positive
segment width must be positive
wind speed cannot be negative
```

The grammar currently allows exactly one flight per file.

Therefore, flight identifier uniqueness inside the file is structurally guaranteed by the grammar.

System-wide uniqueness, and existence checks such as whether an airport, aircraft or pilot exists in the application, require repository/domain integration and are left for US081.

Examples of integration-level checks:

```txt
flight identifier uniqueness in the system
existence of airports in the system
existence of aircraft registration
existence of pilot identifier
```

---

## 15. Error Reporting

Invalid inputs generate clear and informative error messages.

Syntax errors are reported with:

```txt
error type
line
column
message
```

Example:

```txt
SYNTAX_ERROR at line 5, column 4: missing ';' at 'aircraft'
```

Semantic errors are also reported with:

```txt
error type
line
column
message
```

Example:

```txt
FUEL_ERROR at line 19, column 8: Leg 1 fuel quantity must be positive.
```

Date/time semantic error example:

```txt
DATE_TIME_ERROR at line 10, column 3: Leg 1 departure date/time is invalid.
```

---

## 16. Valid Input Result

The file:

```txt
lprog/example/valid-flight.flight
```

is successfully parsed by ANTLR and produces a parse tree.

The parse tree evidence is stored in:

```txt
lprog/trees/valid_tree.svg
```

The complete validation pipeline produces the following result:

```txt
Flight plan is valid.
Flight ID: TP123
Flight type: regular
Route: OPO -> LIS
Aircraft: CS-TAB
Pilot: P001
Flights found: 1
Legs found: 1
Segments found: 1
```

---

## 17. Syntactically Invalid Input Result

The file:

```txt
lprog/example/invalid-flight.flight
```

contains a missing semicolon after the route declaration.

Invalid input:

```txt
route OPO -> LIS
```

Expected input:

```txt
route OPO -> LIS;
```

The system reports:

```txt
Flight plan is invalid.
SYNTAX_ERROR at line 5, column 4: missing ';' at 'aircraft'
```

This shows that the grammar detects syntactic errors and reports them with error type, line and column.

The invalid parse tree evidence is stored in:

```txt
lprog/trees/invalid_tree.svg
```

---

## 18. Semantically Invalid Input Result

The file:

```txt
lprog/example/semantic-invalid-flight.flight
```

is syntactically valid but contains a negative fuel quantity.

Invalid semantic input:

```txt
fuel -5000 kg;
```

The system reports:

```txt
Flight plan is invalid.
FUEL_ERROR at line 19, column 8: Leg 1 fuel quantity must be positive.
```

This shows that semantic validation is executed after successful lexical and syntactic validation.

---

## 19. Invalid Date Input Result

The file:

```txt
lprog/example/invalid-date-flight.flight
```

is syntactically valid but contains an invalid departure date.

Invalid semantic input:

```txt
departure OPO at 2026-99-99 10:30;
```

The system reports:

```txt
Flight plan is invalid.
DATE_TIME_ERROR at line 10, column 3: Leg 1 departure date/time is invalid.
```

This shows that invalid dates are handled as semantic errors without crashing the visitor.

---

## 20. Manual Demo Application

The complete validation pipeline was manually tested using:

```txt
FlightPlanDslApp
```

This class is a small demo utility used to test the DSL validation pipeline.

It is not part of the main domain model.

---

## 21. Maven Validation

The LPROG module compiles successfully with Maven.

This confirms that:

```txt
ANTLR-generated Java classes compile
the parser service compiles
the syntax error listener compiles
the builder visitor compiles
the structure listener compiles
the semantic validator compiles
the internal representation classes compile
```

---

## 22. Conclusion

The Flight Description DSL was specified and implemented using ANTLR.

The informal lexical and syntactic specifications are documented.

A formal ANTLR grammar was defined.

The system performs lexical and syntactic validation.

The system builds an internal representation from valid input files.

Invalid inputs generate clear and informative error messages.

The implementation also includes semantic validation, a custom syntax error listener, a visitor for building the internal representation, and a listener for collecting structural information.

At this stage, US083 is implemented and provides the foundation for US081, where valid imported flight plans can be mapped to the main application domain objects.
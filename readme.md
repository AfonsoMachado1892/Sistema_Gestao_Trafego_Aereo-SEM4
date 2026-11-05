# Project AIControl

## 1. Description of the Project

The project is commissioned by AISafe, a startup targeting the air traffic control market. Its goal is to develop a software system to manage part of the back office of an air traffic control platform, including:

* Basic configuration – registration and management of aircraft models, engine models, manufacturers, air transport companies, airports, and air control areas.
* Flight management – creation, validation, and management of flight routes and flight plans, processed using ANTLR (lexical, syntactic, and semantic analysis).
* Weather service – registration, import, and consultation of weather data per air control area, with support for an AI-enhanced weather service.
* Flight simulation – simulation of individual flights (validating altitude, fuel, and path) and simulation of air traffic control over an area, including collision detection and safety violation reporting. The simulation component is implemented in C, making use of processes, pipes, signals, threads, mutexes, semaphores, and shared memory.
* Remote access – TCP-based client applications for Weather Persons, Pilots, and Air Transport Company Collaborators; UDP-based flight logging server with an HTTP interface for real-time flight visualization.

## 2. Planning and Technical Documentation

[Planning and Technical Documentation](docs/readme.md)

## 3. Prerequisites

* Java JDK 21
* Apache Maven 3.9.14

Verify with:
```bash
java -version
mvn -version
```

## 4. How to Build

The project uses Maven as the build automation tool. All Maven commands must be run from inside the `eapli.base` directory.

**Full build** — compiles, runs tests, generates Surefire and Checkstyle reports:
```bash
cd eapli.base
bash build-all.sh clean
```

**Quick build** — compiles and runs tests only:
```bash
cd eapli.base
bash quickbuild.sh clean
```

> **Note:** A successful build must show `BUILD SUCCESS`.

## 5. How to Execute Tests

Tests run automatically as part of the build. To run tests only:
```bash
cd eapli.base
mvn test
```

Test reports are generated in:
- `eapli.base/target/site/surefire-report.html` — test results (passed/failed)
- `eapli.base/**/target/site/jacoco/` — code coverage per module

## 6. How to Run

After building, follow these steps:

**Step 1 — Load initial data (run once):**
```bash
cd eapli.base
bash run-bootstrap.sh
```

**Step 2 — Start the Backoffice application:**
```bash
cd eapli.base
bash run-backoffice.sh
```

Other available applications:
```bash
bash run-user.sh       # User console
bash run-other.sh      # Other console
```

> **Note:** The bootstrap only needs to be run once to populate the in-memory database with initial data (administrator and test users).

## 7. How to Deploy on Another Machine

To deploy on a Linux machine:

1. Ensure the prerequisites are installed (Java JRE 21, Maven 3.9.14)

2. Build the project:
```bash
cd eapli.base
bash build-all.sh clean
```
3. Run the bootstrap and start the application as described in section 6.

## 8. How to Generate PlantUML Diagrams

To generate PlantUML diagrams for documentation (Linux/macOS only):

```bash
./generate-plantuml-diagrams.sh
```

## 9. Continuous Integration

The repository uses GitHub Actions for continuous integration. The workflow runs automatically on every push to `main` and every night at 2:00 AM.

Build reports are published as artifacts on each run:
- `jacoco-report` — code coverage
- `surefire-report` — test results
- `checkstyle-report` — code style analysis
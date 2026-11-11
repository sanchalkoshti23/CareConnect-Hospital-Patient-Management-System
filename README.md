# CareConnect — Hospital Patient Management System

CareConnect is a Java Swing desktop application for managing hospital patient
records. It was developed for **SWYNEX Java Internship — Task 3** using
PostgreSQL, JDBC, Maven, and FlatLaf.

> **Development status:** The application code and automated validation tests
> compile successfully. Full manual GUI checks and live PostgreSQL CRUD testing
> have not been performed yet.

## Project objectives

- Provide a professional desktop interface for everyday patient-record tasks.
- Keep presentation, validation/business rules, and database access in separate
  application layers.
- Demonstrate parameterized JDBC access to PostgreSQL without embedding
  credentials in source code.
- Build an approachable Java project that can be explained and extended.

## Features

- Dashboard statistics read from PostgreSQL: total patients, male patients,
  female patients, and registrations today.
- A database-derived gender distribution overview and clear loading, empty, and
  unavailable-data states.
- A recent registrations table populated from the database.
- New patient registration with required-field validation.
- Patient records table with sorting and name/ID search.
- Patient editing, confirmed deletion, and refresh.
- Dashboard, registration, patient records, search, About/Help, and Exit
  navigation.
- Friendly database error messages; the app does not show fake statistics when
  PostgreSQL is unavailable.
- No seeded or demo patient records.

## Technology stack

| Area | Technology |
|---|---|
| Language/runtime | Java 25 |
| Desktop UI | Java Swing, FlatLaf 3.6.2 |
| Database | PostgreSQL, PostgreSQL JDBC driver 42.7.7 |
| Build | Apache Maven |
| Automated tests | JUnit Jupiter 5.13.4 |

## Architecture

The UI calls `PatientService`; the service validates and normalizes patient
input before calling `PatientDao`. The DAO owns SQL and uses JDBC
`PreparedStatement` and try-with-resources. `DBConnection` and `DatabaseConfig`
centralize connection creation and read credentials from environment variables.
Database requests are run in Swing workers so JDBC calls do not block the UI
event thread.

## Project structure

```text
careconnect/
├── database/
│   └── schema.sql
├── src/
│   ├── main/java/com/swynex/careconnect/
│   │   ├── Main.java
│   │   ├── dao/
│   │   │   └── PatientDao.java
│   │   ├── gui/
│   │   │   ├── CareConnectFrame.java
│   │   │   └── components/
│   │   │       ├── CareConnectIcon.java
│   │   │       ├── PatientFormPanel.java
│   │   │       ├── PatientTableModel.java
│   │   │       └── SectionPanel.java
│   │   ├── model/
│   │   │   ├── DashboardStats.java
│   │   │   └── Patient.java
│   │   ├── service/
│   │   │   └── PatientService.java
│   │   └── util/
│   │       ├── DBConnection.java
│   │       └── DatabaseConfig.java
│   └── test/java/com/swynex/careconnect/service/
│       └── PatientServiceTest.java
├── .gitignore
├── .env.example
├── pom.xml
├── README.md
└── SETUP.md
```

Maven creates `target/` during a build; it is generated output and is ignored by
Git. A local `.vscode/` settings folder may also exist, and is ignored.

## Prerequisites

- JDK 25 or newer. The Maven compiler target is Java 25.
- Maven 3.9 or newer.
- PostgreSQL 14 or newer.
- pgAdmin or PostgreSQL `psql` command-line client for applying the schema.

For detailed Windows and PowerShell instructions, see [SETUP.md](./SETUP.md).

## Quick start

1. Create the database `careconnect_db` in pgAdmin if it does not already exist.
   The schema script creates the table, not the database. In pgAdmin, select
   `careconnect_db`, open Query Tool, open `database/schema.sql`, and execute it.
   Alternatively, connect `psql` directly to the existing database:

   ```powershell
   psql -v ON_ERROR_STOP=1 -U postgres -d careconnect_db -f .\database\schema.sql
   ```

2. Set credentials in the same PowerShell window used to run CareConnect:

   ```powershell
   $env:CARECONNECT_DB_URL = "jdbc:postgresql://localhost:5432/careconnect_db"
   $env:CARECONNECT_DB_USERNAME = "your_local_postgres_username"
   $env:CARECONNECT_DB_PASSWORD = "your_local_postgres_password"
   ```

   [.env.example](./.env.example) contains safe placeholders as a reference.
   The application does not automatically load `.env` files.

3. Build, run automated tests, then launch:

   ```powershell
   mvn clean verify
   mvn exec:java
   ```

Use your own local credentials. Never commit passwords or real patient data.
`CARECONNECT_DB_URL` is optional and defaults to the URL shown above. The
username and password are required when the application connects.

## Build and tests

`mvn clean verify` compiles the Java 25 source, runs automated tests, and
packages the application as `target/careconnect-1.0.0.jar`. The current tests
cover service-layer input validation; they do not exercise a live database or
the Swing interface.

## Application modules

- **`Main`** — configures FlatLaf and UI defaults, wires the service and DAO, and
  launches the maximized desktop window.
- **`gui/CareConnectFrame`** — the dashboard, application header/sidebar,
  database-driven overview, registration and records pages, About page, dialogs,
  vector iconography, and navigation.
- **`gui/components/PatientFormPanel`** — reusable patient entry/edit form.
- **`gui/components/PatientTableModel`** — table columns and patient-row values.
- **`gui/components/CareConnectIcon`** — crisp, scalable vector icons drawn
  without adding an external icon dependency.
- **`model/Patient`** — patient fields, including the generated ID and
  registration timestamp.
- **`model/DashboardStats`** — statistics and recent-patient results.
- **`service/PatientService`** — required-field validation, age/phone/email/
  gender/blood-group checks, and service operations.
- **`dao/PatientDao`** — parameterized patient CRUD, search, and dashboard
  queries.
- **`util/DBConnection` and `DatabaseConfig`** — JDBC connection setup using
  environment-based credentials, including a check that the database is
  `careconnect_db`.
- **`database/schema.sql`** — PostgreSQL patient table and index definition.

## Database schema

The schema requires a connection to `careconnect_db` and creates
`public.patients` if the table does not already exist. It does not create the
database, and it checks the current database before making changes. The JDBC
configuration also verifies the connected database name. The table has:

- `patient_id`: generated-always identity primary key.
- `full_name`: required, non-blank text, up to 150 characters.
- `age`: required integer from 1 through 120.
- `gender`: required `Female`, `Male`, or `Other`.
- `phone`: required ten-digit phone number.
- `email`: required email-shaped value.
- `address`: optional text.
- `blood_group`: required ABO/Rh group.
- `created_at`: timestamp with time zone, generated by PostgreSQL.

The script uses standard PostgreSQL SQL and can be run in pgAdmin Query Tool
after selecting `careconnect_db`. It also creates an index for recent
registrations. `CREATE TABLE IF NOT EXISTS` does not update an existing table;
the script intentionally does not alter, drop, or delete an existing table or
patient data. If a `patients` table already exists, inspect its columns,
constraints, and identity configuration before relying on it. No sample
records are inserted.

## Screenshots

Screenshots are intentionally left as placeholders until the application is
run and reviewed during the manual testing phase:

- Dashboard: *add `docs/screenshots/dashboard.png` after capturing the screen.*
- Patient records: *add `docs/screenshots/patient-records.png` after capturing
  the screen.*
- Registration form: *add `docs/screenshots/registration.png` after capturing
  the screen.*

## Future enhancements

- Add role-based access and authentication.
- Add pagination and export/print features for larger patient lists.
- Add automated PostgreSQL integration tests using a dedicated test database.
- Add audit history and configurable patient categories.
- Add packaging and installer support for desktop distribution.

## Author and internship

- **Author:** To be confirmed by the project owner before publication.
- **Program:** SWYNEX Java Internship.
- **Task:** Task 3 — Hospital Patient Management System.

## Privacy and production use

This project is an internship demonstration, not a certified clinical system.
Do not use it to store real patient information without appropriate access
controls, security review, backups, audit logging, and applicable privacy and
health-data compliance measures.

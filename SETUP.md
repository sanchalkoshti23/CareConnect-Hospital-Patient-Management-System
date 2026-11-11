# CareConnect setup guide (Windows)

This guide covers local PostgreSQL setup, credentials, building, and launching
CareConnect. Commands below are for PowerShell from the project root.

## 1. Check Java and Maven

CareConnect targets Java 25. Check the installed tools:

```powershell
java -version
mvn -version
```

Maven must run using JDK 25 or newer. If more than one JDK is installed, make
sure `JAVA_HOME` points to the intended JDK before starting Maven.

## 2. Install and start PostgreSQL

Install PostgreSQL for Windows from the official PostgreSQL installer. Include
the command-line tools so that `psql.exe` is installed. Start the PostgreSQL
service and remember the local account and password you created during setup.

Check whether `psql` is available:

```powershell
psql --version
```

If PowerShell cannot find `psql`, call it by its full path. The versioned folder
depends on the PostgreSQL version installed:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" --version
```

## 3. Create the database and patients table

If you have not created the database yet, create a database named
`careconnect_db` in pgAdmin (or use an authorized administrator's
`CREATE DATABASE careconnect_db;`). Then select/connect to `careconnect_db`
before applying the table schema.

### Using pgAdmin Query Tool

1. In the Browser tree, select the `careconnect_db` database.
2. Open **Tools → Query Tool**.
3. Open `database/schema.sql` in the query editor (or paste its contents).
4. Execute the script.

The script checks that the current database is `careconnect_db`, creates
`public.patients` if it is absent, and creates the recent-registration index if
needed. It contains standard PostgreSQL SQL and no psql-only meta-commands.

### Optional: using psql

```powershell
psql -v ON_ERROR_STOP=1 -U postgres -d careconnect_db -f .\database\schema.sql
```

With an explicit `psql.exe` path:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -v ON_ERROR_STOP=1 -U postgres -d careconnect_db -f .\database\schema.sql
```

Enter the database account password when prompted. The database must already
exist; this schema script does not create databases.

### Important for an existing patients table

`CREATE TABLE IF NOT EXISTS` prevents a duplicate-table error, but PostgreSQL
does not use it to update an existing table's columns or constraints. This
script does not drop a table, change existing rows, or perform a migration. If
`public.patients` already exists, inspect its column types, nullability,
constraints, and identity configuration before using the application. Resolve
any schema mismatch with a reviewed migration and backup; do not assume
rerunning this script upgrades it.

### Optional: use a dedicated application account

For a local demonstration you can use an existing PostgreSQL account. To use a
separate account with only the permissions needed by the application, connect
as the database administrator:

```powershell
psql -U postgres -d careconnect_db
```

At the `psql` prompt, run the following once. `\password` prompts securely
rather than putting the password into SQL history:

```text
CREATE ROLE careconnect_app LOGIN;
\password careconnect_app
GRANT CONNECT ON DATABASE careconnect_db TO careconnect_app;
GRANT USAGE ON SCHEMA public TO careconnect_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.patients TO careconnect_app;
GRANT USAGE, SELECT ON SEQUENCE public.patients_patient_id_seq TO careconnect_app;
\q
```

If that role already exists, skip `CREATE ROLE` and use `ALTER ROLE
careconnect_app LOGIN;` before setting its password and permissions.

## 4. Set database connection variables

CareConnect uses environment variables; it does not load `.env` files. This
avoids adding a configuration parser and keeps credentials out of Java source
and GitHub. [.env.example](./.env.example) lists safe placeholders for the
expected settings but is not loaded by the application. Set these values in
the PowerShell terminal that will run the app:

```powershell
$env:CARECONNECT_DB_URL = "jdbc:postgresql://localhost:5432/careconnect_db"
$env:CARECONNECT_DB_USERNAME = "careconnect_app"
$env:CARECONNECT_DB_PASSWORD = "your_local_database_password"
```

Replace the example password with the one you chose. If you did not create the
dedicated role, set the username to your local PostgreSQL account.
`CARECONNECT_DB_URL` is optional if the database is on the default local host
and port. The username and password are required. The application rejects a
connection if its catalog is not `careconnect_db`.

These variables are scoped to this PowerShell window. If you open a different
terminal, set them again before running the application. Do not put real
credentials in a committed file or screenshot.

## 5. Build and run

Build and run the automated validation tests:

```powershell
mvn clean verify
```

Launch the desktop application from the same terminal that has the database
variables:

```powershell
mvn exec:java
```

The CareConnect window opens maximized. On startup it queries PostgreSQL for
dashboard data. If a connection or query fails, the UI shows an error and does
not substitute dummy patient records.

## Troubleshooting

- **`psql` is not recognized:** use the full path to `psql.exe` under your
  PostgreSQL installation's `bin` folder.
- **Connection refused:** verify the PostgreSQL Windows service is running and
  the URL host/port match your server.
- **Database or table missing:** rerun the `psql` schema command and check for
  any SQL error. `ON_ERROR_STOP=1` stops the script at the first error.
- **Credentials not configured:** set `CARECONNECT_DB_USERNAME` and
  `CARECONNECT_DB_PASSWORD` in the same PowerShell window used to launch Maven.
- **Login or permissions rejected:** verify the local role/password and grant
  the required table and identity-sequence permissions.
- **Build uses an unexpected Java version:** inspect `mvn -version` and set
  `JAVA_HOME` to a JDK 25+ installation before running Maven.

## Testing status

`mvn clean verify` runs compilation and automated service-validation tests.
Manual GUI walkthroughs and live PostgreSQL integration/CRUD tests are separate
steps and should be performed before presenting the application as runtime
verified.

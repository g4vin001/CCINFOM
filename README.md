# Archer's Ground DB App Starter

This repository contains a starter implementation for the CCINFOM Database Application project based on the Archer's Ground cafe proposal.

## Project Structure

- `db/archers_ground_schema.sql` - database creation script
- `db/archers_ground_seed.sql` - sample data insert script
- `src/com/archersground/dbapp/` - Java source files

## Scope Covered

- Core records:
  - menu items
  - customers
  - gates
  - employees
- Transactions:
  - place online order and process payment
  - update order preparation status
  - campus-gate delivery fulfillment
  - cancel or refund order
- Reports:
  - monthly sales summary
  - campus-gate delivery report
  - top-selling menu items
  - order volume by time of day

## Run Order

1. Execute `db/archers_ground_schema.sql`.
2. Execute `db/archers_ground_seed.sql`.
3. Set your database credentials using environment variables, or update the fallback values in `src/com/archersground/dbapp/config/DatabaseConfig.java`.
4. Compile the Java files.
5. Run `com.archersground.dbapp.Main`.

## Notes

- The app uses JDBC and standard Java libraries only.
- A MySQL JDBC driver must be available at runtime.
- The current implementation is a console starter app intended to satisfy the required 3-tier structure and make group work easier to split.
- The app reads DB credentials from these environment variables when available:
  - `DB_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`

## Environment Variable Setup

PowerShell example for the current terminal session:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/archers_ground_db"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
```

If these are not set, the fallback values in `DatabaseConfig.java` are used.

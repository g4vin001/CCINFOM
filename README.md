# Archer's Ground DB App

This repository contains a Java database application for the CCINFOM Database Application project based on the Archer's Ground cafe proposal.

The project uses:
- Java
- JDBC
- MySQL
- Swing for the GUI

## Project Structure

- `db/archers_ground_schema.sql` - creates the database and tables
- `db/archers_ground_seed.sql` - inserts sample data
- `lib/mysql-connector-j-9.6.0/mysql-connector-j-9.6.0.jar` - MySQL JDBC driver
- `src/com/archersground/dbapp/` - Java source code
- `out/` - compiled `.class` files

## Features

- View available menu items
- Place orders and process payments
- Update order preparation status
- Update campus-gate delivery status
- Cancel or refund orders
- Generate reports:
  - monthly sales summary
  - campus-gate delivery report
  - top-selling menu items
  - order volume by time of day

## Requirements

Before running the project, make sure you have:
- Java JDK installed
- MySQL Server installed and running
- MySQL Workbench or another SQL client

## Database Setup

Open MySQL Workbench and run the files in this order:

1. `db/archers_ground_schema.sql`
2. `db/archers_ground_seed.sql`

This will:
- create the `archers_ground_db` database
- create all required tables
- insert sample records

## Database Credentials

The application reads these environment variables if they are set:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DEFAULT_PROCESSING_EMPLOYEE_ID`

PowerShell example:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/archers_ground_db"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
$env:DEFAULT_PROCESSING_EMPLOYEE_ID="1"
```

If you do not set them, the app uses the fallback values in `src/com/archersground/dbapp/config/DatabaseConfig.java`.

Current fallback values:

```java
DEFAULT_URL = "jdbc:mysql://localhost:3306/archers_ground_db"
DEFAULT_USERNAME = "root"
DEFAULT_PASSWORD = ""
DEFAULT_PROCESSING_EMPLOYEE_ID = 1
```

Set `DB_PASSWORD` explicitly if your MySQL account uses a password.
Set `DEFAULT_PROCESSING_EMPLOYEE_ID` if customer orders should be recorded under a different active employee.

## How To Compile

From the project root:

```powershell
javac -cp "lib\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar" -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
```

## How To Run

After compiling, run:

```powershell
java -cp "out;lib\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar" com.archersground.dbapp.Main
```

This launches the Swing GUI.

## Logo In Header

To show a logo beside the `Archer's Ground` title in the Swing app header, place your image here:

- `assets/archers-ground-logo.png`

The app will load it automatically if the file exists.

## Recommended Run Order

1. Start MySQL Server.
2. Run `db/archers_ground_schema.sql`.
3. Run `db/archers_ground_seed.sql`.
4. Check `DatabaseConfig.java` or set environment variables.
5. Compile the Java files.
6. Run `com.archersground.dbapp.Main`.

## If You Need To Reset The Database

If you want to remove the existing database completely, run:

```sql
DROP DATABASE IF EXISTS archers_ground_db;
```

Then rerun:
- `db/archers_ground_schema.sql`
- `db/archers_ground_seed.sql`

If you only want to remove all rows but keep the tables:

```sql
USE archers_ground_db;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE refunds;
TRUNCATE TABLE order_status_log;
TRUNCATE TABLE payments;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE menu_items;
TRUNCATE TABLE employees;
TRUNCATE TABLE gates;
TRUNCATE TABLE customers;

SET FOREIGN_KEY_CHECKS = 1;
```

## Notes

- The GUI is split into `Customer Portal` and `Staff Portal`.
- Customer order placement no longer asks for an employee ID; it uses the configured default processing employee.
- The GUI is built on top of the same JDBC and service layer used by the original console workflow.
- The SQL files do not update MySQL automatically. You must execute them in MySQL Workbench or another SQL client.
- If you edit the seed file after data has already been inserted, rerun the database setup so MySQL reflects the new seed data.

-- ============================================================================
-- DATA.SQL — DATABASE SEED DATA (Runs on Every Application Startup)
-- ============================================================================
--
-- WHAT IS THIS FILE?
-- Spring Boot automatically executes this file on startup when:
--   spring.sql.init.mode = always (set in application.properties)
-- It runs AFTER Hibernate creates/updates the schema (ddl-auto = update).
--
-- WHY DO WE NEED SEED DATA?
-- Since we use H2 in-memory database, ALL data is lost when the app restarts.
-- This file re-inserts sample patients so we always have test data available.
-- This is essential for development and manual testing.
--
-- IDEMPOTENT INSERTS (INSERT ... WHERE NOT EXISTS):
-- Each INSERT uses "SELECT ... WHERE NOT EXISTS" pattern to prevent
-- duplicate key errors if the data already exists. This makes the script
-- safe to run multiple times (idempotent = same result regardless of
-- how many times you execute it).
--
-- WELL-KNOWN UUIDs:
-- We use pre-defined UUIDs instead of auto-generated ones so that:
-- 1. We can reference specific patients in tests by their known IDs
-- 2. The IDs are deterministic — same data on every restart
-- 3. API test files (.http files) can use these known IDs
-- ============================================================================


-- Ensure the 'patient' table exists
-- CREATE TABLE IF NOT EXISTS: Only creates if the table doesn't already exist.
-- Hibernate's ddl-auto=update creates it from the @Entity, but this is a safety net.
-- This is "defense in depth" — if Hibernate hasn't run yet, this ensures the table exists.
CREATE TABLE IF NOT EXISTS patient
(
    id              UUID PRIMARY KEY,            -- Primary key: UUID type for global uniqueness
    name            VARCHAR(255)        NOT NULL, -- Patient name, max 255 chars, cannot be null
    email           VARCHAR(255) UNIQUE NOT NULL, -- Email must be unique across all patients
    address         VARCHAR(255)        NOT NULL, -- Physical address
    date_of_birth   DATE                NOT NULL, -- Date only (no time component)
    registered_date DATE                NOT NULL  -- When the patient was registered
    );

-- ============================================================================
-- SAMPLE PATIENT DATA — 15 Pre-seeded Records
-- ============================================================================
-- Each INSERT follows this pattern:
-- INSERT INTO patient (...) SELECT values WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = 'uuid')
--
-- "SELECT 1 FROM patient WHERE id = ?" is an EXISTENCE CHECK:
-- - If a row with this ID exists → the subquery returns a row → NOT EXISTS is FALSE → INSERT skipped
-- - If no row with this ID exists → the subquery returns nothing → NOT EXISTS is TRUE → INSERT runs
--
-- WHY "SELECT 1" NOT "SELECT *"?
-- "SELECT 1" is a convention — we only care IF a row exists, not WHAT's in it.
-- "SELECT 1" is marginally faster because the DB doesn't need to read any columns.

-- Insert well-known UUIDs for specific patients
INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '123e4567-e89b-12d3-a456-426614174000',
       'John Doe',
       'john.doe@example.com',
       '123 Main St, Springfield',
       '1985-06-15',
       '2024-01-10'
    WHERE NOT EXISTS (SELECT 1
                  FROM patient
                  WHERE id = '123e4567-e89b-12d3-a456-426614174000');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '123e4567-e89b-12d3-a456-426614174001',
       'Jane Smith',
       'jane.smith@example.com',
       '456 Elm St, Shelbyville',
       '1990-09-23',
       '2023-12-01'
    WHERE NOT EXISTS (SELECT 1
                  FROM patient
                  WHERE id = '123e4567-e89b-12d3-a456-426614174001');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '123e4567-e89b-12d3-a456-426614174002',
       'Alice Johnson',
       'alice.johnson@example.com',
       '789 Oak St, Capital City',
       '1978-03-12',
       '2022-06-20'
    WHERE NOT EXISTS (SELECT 1
                  FROM patient
                  WHERE id = '123e4567-e89b-12d3-a456-426614174002');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '123e4567-e89b-12d3-a456-426614174003',
       'Bob Brown',
       'bob.brown@example.com',
       '321 Pine St, Springfield',
       '1982-11-30',
       '2023-05-14'
    WHERE NOT EXISTS (SELECT 1
                  FROM patient
                  WHERE id = '123e4567-e89b-12d3-a456-426614174003');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '123e4567-e89b-12d3-a456-426614174004',
       'Emily Davis',
       'emily.davis@example.com',
       '654 Maple St, Shelbyville',
       '1995-02-05',
       '2024-03-01'
    WHERE NOT EXISTS (SELECT 1
                  FROM patient
                  WHERE id = '123e4567-e89b-12d3-a456-426614174004');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174005',
       'Michael Green',
       'michael.green@example.com',
       '987 Cedar St, Springfield',
       '1988-07-25',
       '2024-02-15'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174005');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174006',
       'Sarah Taylor',
       'sarah.taylor@example.com',
       '123 Birch St, Shelbyville',
       '1992-04-18',
       '2023-08-25'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174006');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174007',
       'David Wilson',
       'david.wilson@example.com',
       '456 Ash St, Capital City',
       '1975-01-11',
       '2022-10-10'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174007');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174008',
       'Laura White',
       'laura.white@example.com',
       '789 Palm St, Springfield',
       '1989-09-02',
       '2024-04-20'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174008');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174009',
       'James Harris',
       'james.harris@example.com',
       '321 Cherry St, Shelbyville',
       '1993-11-15',
       '2023-06-30'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174009');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174010',
       'Emma Moore',
       'emma.moore@example.com',
       '654 Spruce St, Capital City',
       '1980-08-09',
       '2023-01-22'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174010');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174011',
       'Ethan Martinez',
       'ethan.martinez@example.com',
       '987 Redwood St, Springfield',
       '1984-05-03',
       '2024-05-12'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174011');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174012',
       'Sophia Clark',
       'sophia.clark@example.com',
       '123 Hickory St, Shelbyville',
       '1991-12-25',
       '2022-11-11'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174012');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174013',
       'Daniel Lewis',
       'daniel.lewis@example.com',
       '456 Cypress St, Capital City',
       '1976-06-08',
       '2023-09-19'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174013');

INSERT INTO patient (id, name, email, address, date_of_birth, registered_date)
SELECT '223e4567-e89b-12d3-a456-426614174014',
       'Isabella Walker',
       'isabella.walker@example.com',
       '789 Willow St, Springfield',
       '1987-10-17',
       '2024-03-29'
    WHERE NOT EXISTS (SELECT 1 FROM patient WHERE id = '223e4567-e89b-12d3-a456-426614174014');
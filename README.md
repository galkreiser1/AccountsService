# Account Service

A small Spring Boot REST application for managing employee payroll data, user roles, and security events.

This is a practice project built to work through core Spring concepts in a realistic but intentionally simplified setting. It is not meant to model a production payroll system, but rather a compact backend service for learning and demonstrating Spring Boot, Spring Security, JPA, REST APIs, and testing.

## What It Does

- Registers users and supports password changes
- Uses HTTP Basic authentication with role-based authorization
- Stores employee payroll records and exposes employee/accountant endpoints
- Supports admin actions such as role changes, user deletion, and account lock/unlock
- Logs security-related events such as failed logins, access denials, role changes, and user management actions
- Serves the application over HTTPS with a local self-signed certificate

## Tech Stack

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- H2 database
- MockMvc for API tests

## Project Structure

The code is grouped by domain rather than kept in one package:

- `auth` for signup and password-related logic
- `security` for Spring Security configuration and authentication flow
- `admin` for administrative endpoints and role/access management
- `payroll` for payment and employee payroll features
- `audit` for security event logging
- `user` for the user model and repository
- `common` for shared responses and exception handling

## Running Locally

The app runs on port `28852` and is configured for HTTPS.

Because it uses a self-signed certificate, tools like Postman may reject the connection unless SSL verification is disabled for local testing.

```bash
./gradlew test
./gradlew bootRun
```

Then access the API at:

```text
https://localhost:28852
```

## HTTPS Setup

The development keystore is not committed to the repository.

To run the app with HTTPS locally, generate a self-signed PKCS12 keystore and place it at:

```text
src/main/resources/keystore/service.p12
```

Example command:

```bash
keytool -genkeypair -alias accountant_service -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore service.p12 -validity 3650
```

Use:

- keystore password: `service`
- certificate alias: `accountant_service`
- common name (CN): `accountant_service`

## Notes

This project was mainly an exercise in backend structure, security basics, and clean API design. The goal was not to build a complete real-world payroll product, but to practice the kinds of Spring features that show up often in everyday backend work.

# API Reference

This document lists the main REST endpoints exposed by the service.

## Authentication

- `POST /api/auth/signup`
  Register a new user.

- `POST /api/auth/changepass`
  Change the authenticated user's password.

## Employee

- `GET /api/empl/payment`
  Get the authenticated employee's payroll data.
  Supports an optional `period` query parameter.

## Accountant

- `POST /api/acct/payments`
  Upload payroll records in bulk.

- `PUT /api/acct/payments`
  Update a specific payroll record.

## Administration

- `GET /api/admin/user`
  List users.

- `DELETE /api/admin/user/{email}`
  Delete a user by email.

- `PUT /api/admin/user/role`
  Grant or remove a role for a user.

- `PUT /api/admin/user/access`
  Lock or unlock a user account.

## Security Events

- `GET /api/security/events`
  Retrieve recorded security events.

## Access Model

- Anonymous users can register through `POST /api/auth/signup`.
- Authenticated users can change their password.
- Employees can view their own payroll data.
- Accountants can manage payroll records.
- Administrators can manage users, roles, and account access.
- Auditors can read security event data.

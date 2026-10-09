# SlotBook --- Full-Stack Appointment Booking Platform

SlotBook is a full-stack appointment booking application built with
Spring Boot and React. It demonstrates REST API development,
authentication and authorization, database schema migrations, and
safeguards against duplicate active bookings when concurrent requests
target the same appointment slot.

**Live demo:** https://slotbook-mu.vercel.app/\
**Backend:** https://slotbook-backend-udag.onrender.com

> **Project status:** The frontend and backend have been deployed.
> Review the Known Limitations section for remaining UI and deployment
> work.

## Features

-   Browse upcoming appointment slots grouped by date.
-   Register and log in with local authentication.
-   Sign in with Google OAuth 2.0.
-   Authenticate protected API requests using JWT bearer tokens.
-   Enforce `USER` and `ADMIN` roles.
-   Allow administrators to create and delete available slots.
-   Place a temporary five-minute hold on a slot and confirm it before
    expiry.
-   Generate upcoming 30-minute slots automatically.
-   Use optimistic locking, database constraints, and idempotency keys
    to help prevent duplicate active bookings.
-   Manage database schema changes with Flyway migrations.
-   Handle booking conflicts through REST API responses.

## Technology Stack

  Area              Technologies
  ----------------- -----------------------------------------------------
  Language          Java
  Backend           Spring Boot, Spring Web, Spring Data JPA, Hibernate
  Security          Spring Security, JWT, BCrypt, Google OAuth 2.0
  Database          PostgreSQL
  Migrations        Flyway
  Frontend          React, JavaScript, Vite, HTML, CSS
  Deployment        Docker, Render, Vercel, Neon PostgreSQL
  Build and tests   Maven Wrapper

## Architecture

``` text
User / Admin
    |
    v
React + Vite frontend (Vercel)
    |
    | REST API requests + JWT
    v
Spring Boot backend (Render)
    |
    +--> Spring Security / JWT / role authorization
    +--> Slot and booking services
    |
    v
PostgreSQL database (Neon)
    +--> Flyway schema migrations
    +--> Database constraints for data integrity
```

Google sign-in uses Google OAuth 2.0. After successful authentication,
the backend finds or creates a corresponding SlotBook user and issues a
SlotBook JWT for API access.

## Booking and Concurrency Design

Preventing duplicate active reservations is a central design goal.

### Optimistic locking

The `Slot` entity uses a JPA `@Version` field. Hibernate can detect
conflicting updates instead of silently accepting stale writes.

### Database-level uniqueness

A PostgreSQL partial unique index restricts active bookings to one per
slot:

``` sql
CREATE UNIQUE INDEX ux_bookings_active_slot
ON bookings(slot_id)
WHERE status IN ('HELD', 'CONFIRMED');
```

This database constraint provides an additional safeguard if
simultaneous requests pass application-level checks.

### Idempotency

The booking API accepts an `Idempotency-Key`. The application stores the
key with the user and associated slot/booking so a retry of the same
logical request can return the existing booking rather than create
another one. Reusing a key for a different slot is rejected.

### Booking lifecycle

``` text
AVAILABLE slot
     |
     | Book
     v
HELD booking (five-minute hold)
     |
     +---- Confirm before expiry ----> CONFIRMED
     |
     +---- Hold expires ------------> EXPIRED / slot released
```

The application also defines `CANCELLED` as a booking status. Hold
expiry and simultaneous confirmation/expiry are important edge cases to
test thoroughly.

## Automatic Slot Generation

The slot generation service is configured to create appointments across
a rolling seven-day window:

-   Opening time: 9:00 AM
-   Closing time: 5:00 PM
-   Slot duration: 30 minutes
-   Slots per day: 16
-   Slots across seven days: 112

An injected Java `Clock` is used for time-dependent logic. A database
unique constraint on `(date, start_time, end_time)` prevents duplicate
slot definitions.

## Authentication and Authorization

### Local authentication

-   Users can register and log in with email and password.
-   Passwords are hashed using BCrypt.
-   Successful login returns a JWT.
-   The JWT authentication filter validates bearer tokens on protected
    requests.

### Google OAuth 2.0

-   Google verifies the user's identity.
-   The backend finds or creates the corresponding SlotBook user.
-   The backend issues a SlotBook JWT.
-   The frontend processes the OAuth success route and uses the JWT for
    API requests.

### Roles

  Operation                Access
  ------------------------ --------------------
  View slots               Public
  Register / log in        Public
  Book or confirm a slot   Authenticated user
  Create slots             Admin
  Delete slots             Admin

Authorization is enforced by Spring Security on the backend; frontend
button visibility is only a user-interface convenience.

## Database and Migrations

Flyway manages schema evolution. The project has migrations covering the
initial schema, optimistic-locking version field, active-booking
uniqueness, idempotency keys, users, hold expiry, foreign keys, and
unique slot times.

Hibernate is configured to validate the schema rather than automatically
modify it (`spring.jpa.hibernate.ddl-auto=validate`).

Core tables include:

-   `users` --- user identity, role, and authentication provider.
-   `slots` --- appointment date, times, status, and optimistic-locking
    version.
-   `bookings` --- slot, user, status, creation time, and hold expiry.
-   `idempotency_keys` --- user-scoped request keys and associated
    booking references.

## Getting Started Locally

### Prerequisites

-   Java 21 or a compatible Java version for the project
-   Docker Desktop
-   Node.js and npm
-   Git

### 1. Clone the repository

``` bash
git clone <your-repository-url>
cd slotBook
```

Replace `<your-repository-url>` with your Git repository URL. If the
cloned folder has another name, enter that folder instead.

### 2. Start PostgreSQL

Use the project's existing Docker Compose file if available. A minimal
local PostgreSQL service can look like this:

``` yaml
services:
  postgres:
    image: postgres:17
    container_name: slotBook-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: slotbook
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Set a local-only `POSTGRES_PASSWORD` in an untracked `.env` file. Do not
commit that file. If your existing Compose configuration already works,
keep using it rather than replacing it unnecessarily.

Start the database:

``` bash
docker compose up -d
```

### 3. Configure backend environment variables

Configure the backend for your local PostgreSQL instance:

``` text
DATABASE_URL=jdbc:postgresql://localhost:5432/slotbook
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=<your-local-database-password>
JWT_SECRET=<a-long-random-secret>
GOOGLE_CLIENT_ID=<your-google-client-id>
GOOGLE_CLIENT_SECRET=<your-google-client-secret>
FRONTEND_URL=http://localhost:5173
```

Use your own Google OAuth credentials and a strong, private JWT secret.
Never commit real secrets to Git.

The application configuration should reference environment variables,
for example:

``` properties
server.port=${PORT:8080}
app.frontend-url=${FRONTEND_URL:http://localhost:5173}

spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

app.jwt.secret=${JWT_SECRET}

spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

Ensure the environment variable names match the checked-in application
configuration.

### 4. Run the backend

On Windows:

``` powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

``` bash
./mvnw spring-boot:run
```

Flyway should apply pending migrations at startup. Ensure PostgreSQL is
running and environment variables are correct.

### 5. Run the frontend

Open a second terminal:

``` bash
cd frontend
npm install
```

Create `frontend/.env` for local development:

``` env
VITE_API_URL=http://localhost:8080
```

Start the Vite development server:

``` bash
npm run dev
```

Vite normally serves the app at `http://localhost:5173`.

For production, set `VITE_API_URL` in the frontend hosting provider to
the deployed backend URL, then rebuild and redeploy.

## API Overview

These routes reflect the application's current route structure. Confirm
exact request and response schemas in the controllers before using them
in external integrations.

  --------------------------------------------------------------------------------
  Method                  Route                            Purpose / access
  ----------------------- -------------------------------- -----------------------
  `POST`                  `/api/auth/register`             Register a local user

  `POST`                  `/api/auth/login`                Log in and obtain a JWT

  `GET`                   `/api/slots`                     List future slots;
                                                           public

  `POST`                  `/api/slots`                     Create a slot; admin

  `POST`                  `/api/slots/{slotId}/book`       Place a hold;
                                                           authenticated

  `POST`                  `/api/slots/{slotId}/confirm`    Confirm a held booking;
                                                           authenticated

  `DELETE`                `/api/slots/{slotId}`            Delete an available
                                                           slot; admin

  `GET`                   `/oauth2/authorization/google`   Start Google OAuth
                                                           login
  --------------------------------------------------------------------------------

Booking requests should include an `Idempotency-Key` header.
Authenticated API requests should include:

``` http
Authorization: Bearer <your-jwt>
```

Conflicting booking operations return HTTP `409 Conflict`.
Unauthenticated requests to protected endpoints should be rejected by
Spring Security.

## Deployment

The current deployment uses:

-   **Frontend:** Vercel
-   **Backend:** Render
-   **Database:** Neon PostgreSQL

Configure production environment variables in hosting dashboards rather
than committing credentials.

Typical production settings include:

``` text
DATABASE_URL=<production-jdbc-url>
DATABASE_USERNAME=<database-user>
DATABASE_PASSWORD=<database-password>
JWT_SECRET=<strong-random-secret>
GOOGLE_CLIENT_ID=<google-client-id>
GOOGLE_CLIENT_SECRET=<google-client-secret>
FRONTEND_URL=https://slotbook-mu.vercel.app
```

Set the frontend's `VITE_API_URL` to:

``` text
https://slotbook-backend-udag.onrender.com
```

Register the correct OAuth redirect URI in Google Cloud Console. For the
deployed backend, the callback is:

``` text
https://slotbook-backend-udag.onrender.com/login/oauth2/code/google
```

The frontend must support client-side navigation to `/oauth-success`.
For Vercel SPA deployments, add and deploy an appropriate rewrite to
serve `/index.html` for application routes if required.

### Low-memory deployment note

A Render deployment previously initialized the database, Flyway,
Hibernate, and Spring Security but was then terminated after exceeding a
512 MB memory limit. If this persists, review JVM memory settings and
actual container memory usage. A heap cap can help, but the heap must
leave room for metaspace, thread stacks, direct buffers, and other
native memory.

## Testing

Run backend tests with:

``` powershell
.\mvnw.cmd test
```

Or on macOS/Linux:

``` bash
./mvnw test
```

For concurrency testing, use a fresh available slot and multiple
simultaneous requests. The key invariant is that no more than one active
booking (`HELD` or `CONFIRMED`) should exist for the same slot. Verify
the final database state as well as HTTP responses.

Also test:

-   Repeating the same idempotency key for the same slot.
-   Reusing a key for a different slot.
-   Confirming before and after hold expiry.
-   Two users attempting to book the same slot.
-   Admin-only create/delete routes.
-   Invalid and expired JWTs.
-   Google OAuth redirect handling in production.

## Known Limitations / Follow-up Work

-   Ensure booking status and confirmation controls are displayed only
    on the card belonging to the booking's slot.
-   Verify the Vercel `/oauth-success` rewrite in the deployed frontend.
-   Re-test Render startup memory after applying JVM tuning.
-   Expand concurrency and hold-expiry race tests before making stronger
    reliability claims.
-   The current OAuth flow passes the JWT in the redirect URL. A
    one-time code exchange or secure cookie-based handoff would be safer
    for a more production-hardened design.
-   Never expose or commit database passwords, JWT secrets, or Google
    OAuth client secrets. Rotate credentials that have been shared in
    logs or source files.

## What I Learned

-   Why check-then-act booking logic can fail under concurrent requests.
-   How JPA optimistic locking and database constraints work together.
-   How idempotency makes retried API requests safer.
-   How to implement JWT authentication and role-based authorization
    with Spring Security.
-   How to integrate Google OAuth with an application's own
    authentication model.
-   How to manage schema evolution using Flyway.
-   How to deploy a React and Spring Boot application with a
    cloud-hosted PostgreSQL database.

## License

Add a license file if you intend to distribute or reuse this project
publicly.

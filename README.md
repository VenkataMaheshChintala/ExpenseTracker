# Smart Expense Analyzer

A full-stack personal finance application for tracking daily expenses, managing categories, and visualising spending trends — built with a Spring Boot REST API and a Vanilla JS/HTML/CSS frontend.

## Features

- **JWT Authentication** — Secure register/login flow using Spring Security and JWT tokens.
- **Expense CRUD** — Add, edit, and delete expenses with category and payment method support.
- **Dashboard Overview** — Summary cards for total monthly/weekly spend and highest spending category.
- **Spending Charts** — Doughnut chart by category and a 7-day line chart (powered by Chart.js).
- **Expense Filters** — Filter the full expense list by category, date range, and amount range.
- **Smart Insights** — Rule-based insights that flag high-spending months and top categories.

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 17 | Core language |
| Spring Boot 4.0 | Application framework |
| Spring Security + JWT (JJWT 0.11) | Stateless auth |
| Spring Data JPA / Hibernate | ORM and database access |
| PostgreSQL | Relational database |
| Lombok | Boilerplate reduction |
| Maven | Build tool |

### Frontend
| Technology | Purpose |
|---|---|
| Vanilla HTML / CSS / JavaScript | UI structure and logic |
| Chart.js | Spending visualisations (doughnut + line) |
| Flatpickr | Date picker for expense form |
| Fetch API | REST communication with backend |

## Project Structure

```
ExpenseTracker/
├── backend/                          # Spring Boot application
│   └── src/main/java/com/finance/expense/
│       ├── config/                   # CORS & Security configuration
│       ├── controller/               # REST controllers (Auth, Expense, Dashboard)
│       ├── dto/                      # Data Transfer Objects
│       ├── entity/                   # JPA entities (User, Expense, Category)
│       ├── repository/               # Spring Data JPA repositories
│       ├── security/                 # JWT filter, utils, UserDetailsService
│       └── service/                  # Business logic (ExpenseService)
└── frontend/                         # Vanilla JS frontend (static files)
    ├── css/style.css
    ├── js/
    │   ├── api.js                    # Shared fetch wrapper (auto-attaches JWT)
    │   ├── auth.js                   # Login, register, logout, route guard
    │   └── dashboard.js              # Dashboard logic, charts, CRUD
    ├── index.html                    # Landing / redirect page
    ├── login.html
    ├── register.html
    └── dashboard.html
```

## Prerequisites

- JDK 17+
- PostgreSQL instance (local or cloud)
- Any static file server (e.g. VS Code Live Server, Python `http.server`) for the frontend

## Setup Instructions

### 1. Database

Create a PostgreSQL database:
```sql
CREATE DATABASE expense_tracker;
```

### 2. Backend Environment Variables

Set the following environment variables before running (or export them in your shell):

```bash
export DB_URL=jdbc:postgresql://localhost:5432/expense_tracker
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
```

> The server reads these via `${DB_URL}`, `${DB_USERNAME}`, and `${DB_PASSWORD}` in `application.properties`. The port defaults to `8080` and can be overridden with `PORT`.

### 3. Run the Backend

```bash
cd backend
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### 4. Run the Frontend

Serve the `frontend/` directory with any static file server. Examples:

```bash
# Python (from the project root)
python3 -m http.server 5500 --directory frontend

# Or use VS Code Live Server extension
```

Open `http://localhost:5500` in your browser.

## API Endpoints

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login, returns JWT | Public |
| GET | `/api/expenses` | List all user expenses | Bearer JWT |
| POST | `/api/expenses` | Create expense | Bearer JWT |
| PUT | `/api/expenses/{id}` | Update expense | Bearer JWT |
| DELETE | `/api/expenses/{id}` | Delete expense | Bearer JWT |
| GET | `/api/dashboard/summary` | Dashboard summary data | Bearer JWT |

## Architecture

- **Backend layers**: `Controller → Service → Repository` following a clean layered architecture.
- **Authentication**: Stateless JWT — the `JwtAuthenticationFilter` validates the token on every protected request and populates the `SecurityContext`.
- **Frontend auth**: `auth.js` stores the JWT in `localStorage` and the shared `fetchAPI()` wrapper in `api.js` automatically attaches it as a `Bearer` header on every request.
- **Database schema**: Auto-managed by Hibernate (`ddl-auto=update`). Tables: `users`, `expenses`, `categories`.

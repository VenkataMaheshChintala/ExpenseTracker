# Smart Expense Analyzer

A full-stack personal finance application for tracking daily expenses, managing custom categories, and visualising spending trends — built with a Spring Boot REST API and a Vanilla JS/HTML/CSS frontend.

## Features

- **JWT Authentication** — Secure register/login flow using Spring Security and JWT tokens.
- **Expense CRUD** — Add, edit, and delete expenses with category and payment method support.
- **Custom Category Groups** — Create named categories (e.g. "Goa Trip 2024") before or after adding expenses. View total spend, expense count, and a full expense list per category. Rename or delete categories at any time.
- **Dashboard Overview** — Summary cards for total monthly/weekly spend and highest spending category.
- **Spending Charts** — Doughnut chart by category and a 7-day line chart (powered by Chart.js).
- **Expense Filters** — Filter the full expense list by category, date range, and amount range.
- **Smart Insights** — Rule-based insights that flag high-spending months and top categories.

## Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 26 (target 21) | Core language |
| Spring Boot | 4.0.6 | Application framework |
| Spring Security + JWT (JJWT) | 0.11.5 | Stateless authentication |
| Spring Data JPA / Hibernate | 7.2.x | ORM and database access |
| PostgreSQL | 18 | Relational database |
| Lombok | 1.18.46 | Boilerplate reduction |
| Maven | — | Build tool |

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
├── backend/
│   └── src/main/java/com/finance/expense/
│       ├── config/
│       │   ├── CorsConfig.java          # CORS — allows all origins (dev)
│       │   └── SecurityConfig.java      # Spring Security — CSRF disabled, open routes
│       ├── controller/
│       │   ├── AuthController.java      # POST /api/auth/login|register
│       │   ├── CategoryController.java  # CRUD /api/categories
│       │   ├── DashboardController.java # GET  /api/dashboard/summary
│       │   └── ExpenseController.java   # CRUD /api/expenses
│       ├── dto/
│       │   ├── AuthRequest.java
│       │   ├── AuthResponse.java
│       │   ├── CategoryDto.java         # Category with expenseCount + totalSpend
│       │   ├── DashboardSummaryDto.java
│       │   ├── ExpenseDto.java
│       │   └── RegisterRequest.java
│       ├── entity/
│       │   ├── Category.java
│       │   ├── Expense.java
│       │   └── User.java
│       ├── repository/
│       │   ├── CategoryRepository.java
│       │   ├── ExpenseRepository.java
│       │   └── UserRepository.java
│       ├── security/
│       │   ├── CustomUserDetailsService.java
│       │   ├── JwtAuthenticationFilter.java
│       │   └── JwtUtils.java
│       └── service/
│           ├── CategoryService.java     # Category business logic
│           └── ExpenseService.java      # Expense business logic
└── frontend/
    ├── css/style.css
    ├── js/
    │   ├── api.js          # Shared fetch wrapper (auto-attaches JWT)
    │   ├── auth.js         # Login, register, logout, route guard
    │   └── dashboard.js    # Dashboard, expenses, categories logic + charts
    ├── index.html          # Landing / redirect page
    ├── login.html
    ├── register.html
    └── dashboard.html      # Main app (Dashboard | Expenses | Categories tabs)
```

## Prerequisites

- JDK 21+ (tested on Java 26)
- PostgreSQL instance (local or cloud)
- Any static file server for the frontend (e.g. VS Code Live Server, Python `http.server`)

> **Note on Lombok:** This project uses Lombok 1.18.46 which is required for Java 24+ support. If you're on Java 17–23, any version ≥ 1.18.30 works.

## Setup Instructions

### 1. Database

Create a PostgreSQL database:
```sql
CREATE DATABASE expense_tracker;
```

### 2. Backend Configuration

Edit `backend/src/main/resources/application.properties` and update the credentials if your PostgreSQL setup differs from the defaults:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker
spring.datasource.username=postgres
spring.datasource.password=postgres
```

For production deployments, these values are automatically overridden by `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables.

### 3. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

> If you get `Port 8080 was already in use`, a previous run is still alive. Kill it with:
> ```bash
> lsof -ti:8080 | xargs kill -9
> ```

### 4. Run the Frontend

Serve the `frontend/` directory with any static file server:

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
| GET | `/api/dashboard/summary` | Dashboard summary + insights | Bearer JWT |
| GET | `/api/categories` | List all user categories (with stats) | Bearer JWT |
| POST | `/api/categories` | Create a new category | Bearer JWT |
| PUT | `/api/categories/{id}` | Rename a category | Bearer JWT |
| DELETE | `/api/categories/{id}` | Delete a category | Bearer JWT |
| GET | `/api/categories/{id}/expenses` | All expenses in a specific category | Bearer JWT |

## Architecture

- **Backend layers**: `Controller → Service → Repository` — clean layered architecture.
- **Authentication**: Stateless JWT — `JwtAuthenticationFilter` validates the token on every protected request and populates the `SecurityContext`.
- **Frontend auth**: `auth.js` stores the JWT in `localStorage`; the shared `fetchAPI()` wrapper in `api.js` automatically attaches it as a `Bearer` header on every request.
- **Categories**: Each category belongs to a user. Deleting a category un-links its expenses (sets `category_id = null`) but does **not** delete the expenses themselves.
- **Database schema**: Auto-managed by Hibernate (`ddl-auto=update`). Tables: `users`, `expenses`, `categories`.

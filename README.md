# Smart Expense Analyzer & Budget Advisor

A modern full-stack web application designed to help users track daily expenses, manage budgets, and receive smart financial insights.

## Features

- **JWT Authentication**: Secure login and registration using Spring Security and JWT.
- **Modern Dashboard**: Visually stunning SaaS-like finance dashboard with dark mode and glassmorphism UI.
- **Analytics**: Dynamic Recharts visualizations (Pie Charts, Line Charts) for spending patterns.
- **Expense Tracking**: Add, categorize, and view recent transactions.
- **Smart Insights**: AI-like insights that inform you of high spending habits.

## Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.x**
- **Spring Security & JWT**
- **Hibernate / Spring Data JPA**
- **PostgreSQL**
- **Maven**
- **Lombok**

### Frontend
- **React 18** (Vite)
- **React Router**
- **Axios**
- **Recharts** for visualizations
- **Lucide React** for icons
- **Vanilla CSS** with modern styling practices

## Prerequisites

- JDK 17
- Node.js & npm
- PostgreSQL database

## Setup Instructions

### 1. Database Configuration
Create a PostgreSQL database named `expense_tracker`.
```sql
CREATE DATABASE expense_tracker;
```
Ensure your postgres instance is running on `localhost:5432` with username `postgres` and password `postgres`.
Update `backend/src/main/resources/application.properties` if your credentials differ.

### 2. Run the Backend
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```
The backend API will run on `http://localhost:8080`.

### 3. Run the Frontend
```bash
cd frontend
npm install
npm run dev
```
The React frontend will be accessible at `http://localhost:5173`.

## Architecture Details

- **Backend Layers**: Controller -> Service -> Repository (Clean architecture pattern).
- **Authentication**: Stateless session management via JWT tokens. Filter validates token per-request.
- **Frontend Architecture**: Context API is used to manage authentication globally. Custom Axios interceptor attaches the Bearer token automatically to every API request.
- **Styling**: Extensive use of CSS Variables in `index.css` for easy theming. Animations (`fadeIn`, hover transforms) are implemented purely in CSS.

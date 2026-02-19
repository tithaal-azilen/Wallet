# Wallet Application

A comprehensive Spring Boot application for managing virtual wallets, user transactions, and fee deductions. This project demonstrates a robust implementation of a digital wallet system with features like user management, fund transfers, transaction history, and event-driven email notifications.

## Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)

## Features

### User Management
- **Registration**: Users can sign up with username, email, password, and contact details.
- **Authentication**: JWT-based authentication for secure access.
- **Profile Management**: Users can view, update, and delete their profiles.
- **Wallet Creation**: Users can generate multiple wallets.

### Wallet Operations
- **Top-Up**: Credit funds to a wallet using a simulated credit card.
- **Transfer**: Transfer funds securely between different wallets.
- **Validation**: Strict validation for ownership and balance checks.

### Transaction History (Ledger)
- **User Ledger**: View all transactions associated with a user.
- **Wallet Ledger**: View transactions specific to a single wallet.

### Automated Services
- **Monthly Fee Deduction**: Automated background process to deduct monthly maintenance fees.
- **Email Notifications**: Event-driven emails for wallet creation and fee deductions.

## Technology Stack
- **Languages**: Java 17
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Security**: Spring Security, JWT (JSON Web Tokens)
- **Documentation**: Swagger UI / OpenAPI
- **Utilities**: Lombok, Java Mail Sender

## Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven 3.6+
- PostgreSQL Database

## Getting Started

1.  **Clone the Repository**
    ```bash
    git clone <repository-url>
    cd Wallet
    ```

2.  **Configure Database**
    Update `src/main/resources/application.properties` with your PostgreSQL credentials:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    ```

3.  **Configure Email (Optional for Local Dev)**
    To enable email features, configure your SMTP settings in `application.properties`:
    ```properties
    spring.mail.username=your-email@gmail.com
    spring.mail.password=your-app-password
    ```

4.  **Build and Run**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

## Configuration
Key configuration properties in `application.properties`:
- `server.port`: Application port (default: 8081)
- `wallet.monthly.fee.amount`: Amount deducted monthly (default: 5.0)

## API Documentation
Once the application is running, you can access the interactive Swagger UI documentation at:
`http://localhost:8081/swagger-ui/index.html`

### Key Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **Auth** | `/api/auth/register` | Register a new user |
| | `/api/auth/login` | Login and get JWT token |
| **User** | `/api/users/{id}` | Get/Update/Delete user |
| | `/api/users/{id}/addwallet` | Create a new wallet |
| **Wallet** | `/api/user/{userId}/wallet/{walletId}/payment` | Top-up wallet |
| | `/api/user/{userId}/wallet/transfer` | Transfer funds |
| **Ledger** | `/api/ledger/{userid}` | Get user transaction history |

## Testing
The project includes a comprehensive suite of unit and integration tests.
See [TESTS.md](TESTS.md) for a detailed list of all test scenarios.

To run the tests:
```bash
mvn test
```

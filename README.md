# Indian Contract AI

An AI-powered contract analysis system designed for Indian legal documents, providing contract summarization, clause extraction, risk identification, compliance analysis, and key obligation insights.

## Table of Contents

* [Problem Statement](#problem-statement)
* [Key Features](#key-features)
* [Tech Stack](#tech-stack)
* [System Architecture](#system-architecture)
* [Database Schema](#database-schema)
* [API Endpoints](#api-endpoints)
* [Setup & Installation](#setup--installation)
* [Testing Status](#testing-status)
* [Screenshots](#screenshots)
* [Author](#author)

---

## Problem Statement

* Manual contract review can be time-consuming and expensive, especially for small businesses and individuals.
* Generic contract analysis tools may not adequately address India-specific legal and regulatory requirements.
* Contracts may contain complex clauses, obligations, risks, and compliance requirements that are difficult to identify manually.
* There is a need for an accessible AI-assisted system that can analyze contracts while considering Indian legal and regulatory contexts.

---

## Key Features

* **User Authentication** — Secure registration and login using JWT.
* **Contract Upload** — Upload PDF and DOCX contracts for analysis.
* **Document Processing** — Extract contract text using Apache Tika.
* **Clause Extraction** — Identify and analyze important contract clauses.
* **AI Contract Analysis** — Analyze contracts using the Gemini API.
* **Risk Identification** — Identify potentially risky or unfavorable clauses.
* **Compliance Analysis** — Check contracts against selected Indian regulatory requirements.
* **Obligation Extraction** — Identify important responsibilities, actions, and deadlines.
* **Contract Chat** — Interact with uploaded contracts through an AI-powered chat interface.
* **Contract History** — View previously uploaded and analyzed contracts.
* **Dashboard** — Centralized interface for contract management and analysis.

---

## Tech Stack

| Layer            | Technology                      |
| ---------------- | ------------------------------- |
| Backend          | Java 17, Spring Boot 3.x        |
| Security         | Spring Security, JWT            |
| Frontend         | HTML5, CSS3, Vanilla JavaScript |
| Database         | MySQL 8                         |
| AI / NLP         | Google Gemini API               |
| Document Parsing | Apache Tika                     |
| Rule Engine      | Custom Java Rule Engine         |
| Data Access      | JPA / JDBC                      |
| Visualization    | Chart.js                        |

---

## System Architecture

```text
┌─────────────────────────────────────┐
│          Frontend                   │
│      HTML / CSS / JavaScript        │
│  Login · Dashboard · Analysis       │
│  Upload · Compliance · Chat         │
└──────────────────┬──────────────────┘
                   │
                   │ REST API
                   ▼
┌─────────────────────────────────────┐
│          Spring Boot Backend        │
│                                     │
│ Authentication · File Upload        │
│ Contract Processing · AI Analysis   │
│ Clause Extraction · Risk Analysis  │
│ Compliance · Obligations · Chat     │
└──────────────┬───────────┬──────────┘
               │           │
               ▼           ▼
        ┌────────────┐  ┌──────────────┐
        │   MySQL    │  │  Gemini API  │
        │  Database  │  │ AI Analysis  │
        └────────────┘  └──────────────┘
               │
               ▼
        Custom Rule Engine
       GST · DPDP · ICA
```

---

## Database Schema

The application uses MySQL to store user, contract, clause, compliance, obligation, and chat-related data.

### Core Tables

* `users` — User authentication and account information
* `contracts` — Uploaded contract details
* `clauses` — Extracted contract clauses
* `compliance_flags` — Identified compliance issues
* `obligations` — Extracted contractual obligations
* `chat_history` — Contract-related chat conversations

The database schema is available in:

```text
database/schema.sql
```

---

## API Endpoints

| Method | Endpoint                          | Description                      |
| ------ | --------------------------------- | -------------------------------- |
| POST   | `/api/auth/register`              | Register a new user              |
| POST   | `/api/auth/login`                 | Authenticate user and return JWT |
| POST   | `/api/contracts/upload`           | Upload a PDF/DOCX contract       |
| GET    | `/api/contracts/{id}`             | Get contract details             |
| GET    | `/api/contracts/{id}/clauses`     | Get extracted clauses            |
| GET    | `/api/contracts/{id}/risks`       | Get contract risk analysis       |
| GET    | `/api/contracts/{id}/compliance`  | Get compliance findings          |
| GET    | `/api/contracts/{id}/obligations` | Get extracted obligations        |
| POST   | `/api/contracts/{id}/chat`        | Chat with the uploaded contract  |
| GET    | `/api/contracts/history`          | Get user's contract history      |

---

## Setup & Installation

### Prerequisites

* Java 17 or later
* Maven
* MySQL 8
* Git
* Google Gemini API key

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/indian-contract-ai.git
cd indian-contract-ai
```

### 2. Configure MySQL

Create the required database and tables using:

```bash
mysql -u root -p < database/schema.sql
```

Alternatively, execute `database/schema.sql` using MySQL Workbench.

### 3. Configure Environment Variables

Configure the required application values:

```text
DB_URL
DB_USER
DB_PASSWORD
GEMINI_API_KEY
JWT_SECRET
```

**Never commit real API keys, database passwords, or other sensitive credentials to GitHub.**

### 4. Run the Backend

Using Maven:

```bash
mvn spring-boot:run
```

Or using the Maven wrapper on Windows:

```bash
mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

### 5. Run the Frontend

Open the frontend application in a browser or serve it using a local development server.

---

## Testing Status

The core application has been implemented and is currently in the **testing and validation phase**.

### Completed

* [x] Spring Boot backend
* [x] MySQL database
* [x] JWT authentication
* [x] Contract upload
* [x] PDF/DOCX text extraction
* [x] Clause extraction
* [x] Gemini API integration
* [x] AI-powered contract analysis
* [x] Risk identification
* [x] Indian regulatory compliance checks
* [x] Obligation extraction
* [x] Contract chat
* [x] Contract history
* [x] Frontend implementation

### In Progress

* [ ] End-to-end testing
* [ ] Bug fixing and validation
* [ ] Chart.js risk visualization
* [ ] Final UI improvements
* [ ] Performance and reliability testing

---

## Screenshots

Screenshots of the application will be added here, including:

* Login
* Dashboard
* Contract Upload
* Contract Analysis
* Risk Analysis
* Compliance Findings
* Obligation Tracker
* Contract Chat

---

## Security

* JWT-based authentication is used for securing user access.
* API keys and sensitive configuration are managed through environment variables.
* Database credentials should not be committed to the repository.
* `.gitignore` is used to prevent sensitive and generated files from being pushed to GitHub.

---

## Disclaimer

This project is developed for educational and research purposes. The AI-generated analysis is intended to assist with contract understanding and does not replace professional legal advice.

---

## Author

**Bhavana V**

Information Science Engineering Student

---

*An engineering project exploring the application of Artificial Intelligence, Natural Language Processing, and rule-based analysis to Indian contract documents.*


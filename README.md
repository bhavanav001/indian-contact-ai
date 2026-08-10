# Indian Contract AI

An AI-powered contract analysis system designed for Indian legal documents. The application helps users analyze contracts, extract important clauses and obligations, identify potential risks, and provide India-specific compliance insights.

> **Project Status:** Core application implemented — currently in the testing and validation phase.

## Features

* User registration and login with JWT authentication
* Secure contract upload for PDF/DOCX documents
* Contract text extraction using Apache Tika
* Automatic clause extraction and analysis
* AI-powered contract analysis using OpenAI API
* Contract summarization
* Legal risk identification
* India-specific regulatory compliance analysis
* Rule-based checks for:

  * Indian Contract Act, 1872
  * GST-related provisions
  * Digital Personal Data Protection (DPDP) Act
* Explainable compliance findings with relevant legal provisions
* Obligation extraction from contracts
* Contract history
* AI-powered contract chat
* Frontend dashboard for contract analysis

## Tech Stack

| Component           | Technology                      |
| ------------------- | ------------------------------- |
| Backend             | Java 17, Spring Boot 3.x        |
| Security            | Spring Security, JWT            |
| Frontend            | HTML5, CSS3, Vanilla JavaScript |
| Database            | MySQL 8                         |
| AI / NLP            | OpenAI API                      |
| Document Processing | Apache Tika                     |
| Rule Engine         | Custom Java Rule Engine         |
| Data Access         | JPA / JDBC                      |
| Visualization       | Chart.js                        |

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
        │   MySQL    │  │  OpenAI API  │
        │  Database  │  │ AI Analysis  │
        └────────────┘  └──────────────┘
               │
               ▼
        Custom Rule Engine
       GST · DPDP · ICA
```

## Database

The application uses MySQL to store application and contract-analysis data.

### Core Tables

* `users` — user accounts and authentication data
* `contracts` — uploaded contract information
* `clauses` — extracted contract clauses
* `compliance_flags` — identified compliance issues
* `obligations` — extracted contractual obligations
* `chat_history` — contract-related conversations

The database schema is available at:

```text
database/schema.sql
```

## API Endpoints

| Method | Endpoint                          | Description                      |
| ------ | --------------------------------- | -------------------------------- |
| POST   | `/api/auth/register`              | Register a new user              |
| POST   | `/api/auth/login`                 | Authenticate user and return JWT |
| POST   | `/api/contracts/upload`           | Upload a PDF/DOCX contract       |
| GET    | `/api/contracts/{id}`             | Get contract details             |
| GET    | `/api/contracts/{id}/clauses`     | Get extracted clauses            |
| GET    | `/api/contracts/{id}/risks`       | Get risk analysis                |
| GET    | `/api/contracts/{id}/compliance`  | Get compliance findings          |
| GET    | `/api/contracts/{id}/obligations` | Get extracted obligations        |
| POST   | `/api/contracts/{id}/chat`        | Chat with the contract           |
| GET    | `/api/contracts/history`          | Get user's contract history      |

## Project Structure

```text
Indian-Contract-AI/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── frontend/
│   ├── html/
│   ├── css/
│   ├── js/
│   └── ...
│
├── database/
│   └── schema.sql
│
├── .gitignore
├── README.md
└── ...
```

## Setup & Installation

### Prerequisites

Make sure you have installed:

* Java 17 or later
* Maven
* MySQL 8
* Git
* An OpenAI API key

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

Or execute `database/schema.sql` through MySQL Workbench.

### 3. Configure Environment Variables

Configure the following values:

```text
DB_URL
DB_USER
DB_PASSWORD
OPENAI_API_KEY
JWT_SECRET
```

**Do not commit real API keys, passwords, or other secrets to GitHub.**

### 4. Run the Backend

Using Maven:

```bash
mvn spring-boot:run
```

Or, if the Maven wrapper is included:

**Windows:**

```bash
mvnw.cmd spring-boot:run
```

**Linux/macOS:**

```bash
./mvnw spring-boot:run
```

### 5. Run the Frontend

Open the frontend application in a browser or serve the frontend using a local development server.

## Testing Status

The core application has been implemented and is currently undergoing testing and validation.

### Completed

* [x] Backend implementation
* [x] Database implementation
* [x] JWT authentication
* [x] Contract upload
* [x] PDF/DOCX text extraction
* [x] Clause extraction
* [x] AI contract analysis
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

## Screenshots

Screenshots of the following modules can be added here:

* Login
* Dashboard
* Contract Upload
* Contract Analysis
* Risk Analysis
* Compliance Findings
* Obligation Tracker
* Contract Chat

## Security

The application uses JWT-based authentication for user access.

Sensitive configuration such as API keys, database passwords, and JWT secrets should be stored using environment variables and must not be committed to the repository.

## Disclaimer

This project is developed for educational and research purposes. The AI-generated analysis is intended to assist with contract understanding and does not constitute professional legal advice.

## Author

**Bhavana V**

Information Science Engineering Student,SCE

---

*An engineering project exploring the application of Artificial Intelligence, Natural Language Processing, and rule-based analysis to Indian contract documents.*

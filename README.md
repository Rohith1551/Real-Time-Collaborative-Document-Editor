# Real-Time Collaborative Document Editor

A full-stack collaborative document editing application built with **Spring Boot, React, PostgreSQL, Apache Kafka, and WebSockets**. The project focuses on enabling multiple users to work on shared documents, handling concurrent edits, and maintaining document consistency.

## Features

- **Real-Time Communication:** WebSocket communication using STOMP for exchanging document updates.
- **Concurrent Editing:** Operational Transformation (OT) logic for handling simultaneous insert and delete operations.
- **Conflict Handling:** Optimistic locking using JPA's `@Version` mechanism to detect conflicting database updates.
- **Document Management:** Backend functionality for working with documents and document permissions.
- **Access Control:** Role-based document permissions, including OWNER, EDITOR, and VIEWER.
- **Version History:** Support for maintaining document versions.
- **Event-Driven Architecture:** Apache Kafka integration for document update events.
- **Persistent Storage:** PostgreSQL database with Spring Data JPA and Hibernate.
- **Backend Testing:** Unit tests for document service logic and application context initialization.
- **React Frontend:** React 19 and Vite-based frontend with STOMP client integration.

## Technology Stack

| Component | Technologies |
|---|---|
| Frontend | React 19, JavaScript, Vite |
| Backend | Java 17, Spring Boot |
| Database | PostgreSQL |
| ORM | Spring Data JPA, Hibernate |
| Real-Time Communication | WebSockets, STOMP |
| Messaging | Apache Kafka |
| Build Tool | Maven |
| Testing | JUnit, Spring Boot Test |
| Version Control | Git, GitHub |

## Architecture

The application consists of a React frontend, a Spring Boot backend, a PostgreSQL database, and Apache Kafka for event messaging.

```text
                React Frontend
                 (Vite + React)
                       |
             REST API / WebSocket
                       |
                Spring Boot
              /      |       \
             /       |        \
      PostgreSQL   STOMP      Kafka
       Database    Broker     Events
```

- **React frontend:** Provides the user interface and communicates with the backend.
- **Spring Boot backend:** Handles document operations, persistence, permissions, and collaborative editing logic.
- **PostgreSQL:** Stores persistent application data.
- **WebSockets/STOMP:** Enables real-time communication between connected clients.
- **Apache Kafka:** Supports asynchronous document event messaging.

## Project Structure

```text
Real-Time-Collaborative-Document-Editor/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       └── Real_Time_Collaborative_Document_Editor/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
├── .mvn/
├── docker-compose.yml
├── pom.xml
├── .gitignore
└── README.md
```

*The exact contents of individual directories may change as development continues.*

## Prerequisites

Install the following before running the project:

- Java Development Kit (JDK) 17
- Apache Maven, or the included Maven Wrapper
- Node.js and npm
- PostgreSQL
- Docker Desktop (for running Kafka through Docker Compose)
- Git

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/Real-Time-Collaborative-Document-Editor.git
cd Real-Time-Collaborative-Document-Editor
```

Replace `YOUR_USERNAME` with the GitHub account that owns the repository.

### 2. Configure PostgreSQL

Create a PostgreSQL database named:

```sql
CREATE DATABASE "Document-Editor";
```

Configure your database connection using environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/Document-Editor
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
```

Set `DB_USERNAME` and `DB_PASSWORD` in your local environment. Do not commit real credentials to GitHub.

For local development, Hibernate can update the database schema automatically. For production deployments, use a database migration tool and an appropriate schema management strategy.

### 3. Start Apache Kafka

If Kafka is configured in the project's Docker Compose file, start it from the project root:

```bash
docker compose up -d kafka
```

Check the container status:

```bash
docker compose ps
```

The current local Kafka configuration uses port `9092`.

### 4. Start the Spring Boot backend

Open a terminal in the project root and run:

```bash
mvn spring-boot:run
```

Alternatively, on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend is configured to run on:

```text
http://localhost:8080
```

Keep the terminal open while using the application.

### 5. Start the React frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Vite prints the local development URL, typically:

```text
http://localhost:5173
```

Open that URL in your browser.

**Note:** The frontend's REST API and WebSocket URLs must match the backend configuration. Verify CORS, security rules, and endpoint paths when running the application locally.

## Real-Time Editing and Conflict Handling

The project explores two important problems in collaborative editing.

### Operational Transformation

Operational Transformation (OT) adjusts editing operations when concurrent changes affect their positions. For example, an insertion by one user may shift the intended position of another user's operation.

The project includes logic for transforming insert and delete operations to help maintain consistency when edits overlap.

### Optimistic Locking

JPA's `@Version` mechanism detects when multiple transactions attempt to update the same document version. This helps identify conflicting updates rather than silently overwriting newer data.

### Real-Time Messaging

WebSockets with STOMP provide a communication channel for connected clients. Kafka is integrated for document update events and asynchronous messaging.

These mechanisms serve different purposes: OT handles editing-operation coordination, optimistic locking detects persistence conflicts, and messaging enables communication between application components.

## Testing

Run the backend test suite from the project root:

```bash
mvn test
```

Or, on Windows:

```powershell
.\mvnw.cmd test
```

The test suite includes document service tests and application context initialization tests.

To build the frontend for production:

```bash
cd frontend
npm run build
```

The generated frontend build is placed in `frontend/dist/`.

## Configuration and Security

- Keep `.env` and files containing local credentials out of version control.
- Never commit database passwords, API keys, or access tokens.
- Configure production credentials through environment variables or a secrets manager.
- Configure CORS and WebSocket access restrictions for the intended deployment environment.
- Review Spring Security configuration before exposing the application publicly.
- Use HTTPS and secure WebSocket connections (`wss://`) in production.
- Review database schema migration, logging, and error handling before production deployment.

## Future Improvements

Potential areas for further development include:

- Completing and refining the document editor interface.
- Improving document discovery and management in the frontend.
- Verifying concurrent editing across multiple browser sessions.
- Improving presence indicators and collaboration status.
- Adding integration tests for REST APIs and WebSocket messaging.
- Adding robust authentication and authorization flows where required.
- Containerizing the frontend, backend, and database for consistent deployment.
- Adding CI/CD automation and production deployment configuration.

## Contributing

Contributions and suggestions are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Implement and test your changes.
4. Commit your changes with a descriptive message.
5. Open a pull request.

## License

No license has been specified yet. Unless a license is added to this repository, all rights remain with the copyright holder, and reuse is subject to applicable copyright law.

---

**Built with Java, Spring Boot, React, PostgreSQL, Apache Kafka, and WebSockets.**
# 🚀 Pipes - CI/CD Pipeline Simulator

[![Java](https://img.shields.io/badge/Java-24-orange.svg?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-green.svg?logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36.svg?logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](#running-the-application)

> A robust Spring Boot REST API for simulating CI/CD pipeline execution with asynchronous job processing, real-time monitoring, and comprehensive execution history tracking.

**Course:** CS 305 Advanced Java | **Institution:** UNYT | **Semester:** Spring 2026

## 📋 Quick Links

<details open>
<summary><b>Click to expand Table of Contents</b></summary>

- [Project Overview](#-project-overview)
- [Features](#-features)
- [Quick Start](#-quick-start)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Running the Application](#-running-the-application)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Project Structure](#-project-structure)
- [Design Patterns & Architecture](#-design-patterns--architecture)
- [Development](#-development)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [Support](#-support)
- [License](#-license)

</details>

---

## ⚡ Quick Start

Get Pipes running in 5 minutes:

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/pipes.git
cd pipes

# 2. Setup PostgreSQL database
createdb pipes_db
createuser -P pipes_user  # password: pipes_pass

# 3. Build and run
mvn clean install
mvn spring-boot:run

# 4. Application is running at http://localhost:8080
```

**First API call - Register a user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}'
```

Full setup instructions → [Installation & Setup](#-installation--setup)

---

## 📖 Project Overview

Pipes is a REST API that simulates CI/CD pipeline execution. It allows users to:

- **Create and manage pipelines** with multiple stages and jobs
- **Execute pipelines** with asynchronous job processing
- **Monitor pipeline execution** in real-time with status tracking
- **Store execution history** and results
- **Authenticate securely** using JWT tokens
- **Access control** based on user roles and permissions

### Use Cases

- Create complex multi-stage pipelines
- Define jobs with shell commands or other execution strategies
- Run pipelines and monitor their execution
- View detailed logs and results of each job
- Manage multiple pipeline templates and execution runs

---

## ✨ Features

| Feature | Description | Status |
|---------|-------------|--------|
| 🔐 **User Authentication** | JWT-based login & registration | ✅ Complete |
| 📦 **Pipeline Management** | Create, update, delete pipelines with stages & jobs | ✅ Complete |
| ⚙️ **Pipeline Execution** | Asynchronous job execution with thread pools | ✅ Complete |
| 📊 **Real-time Monitoring** | Live status tracking (PENDING → RUNNING → SUCCESS/FAILED) | ✅ Complete |
| 📈 **Execution History** | Track all runs with detailed results & logs | ✅ Complete |
| 🎯 **Extensible Architecture** | Strategy, Builder, Observer patterns for flexibility | ✅ Complete |
| 🔍 **Database Auditing** | Audit logs for user actions & optimized queries | ✅ Complete |
| 🌐 **REST API** | Full RESTful API with proper HTTP status codes | ✅ Complete |
| 🧪 **Testing** | Unit & integration tests included | ✅ Complete |
| 📝 **Documentation** | Comprehensive API docs & architecture guides | ✅ Complete |

---

## 🛠 Technology Stack

```
┌─────────────────────────────────────────┐
│           TECHNOLOGY STACK              │
├─────────────────────────────────────────┤
│ Backend Framework    │ Spring Boot 3.3.5 │
│ Language            │ Java 24           │
│ Build Tool          │ Maven 3.x         │
│ Database            │ PostgreSQL 12+    │
│ Authentication      │ JWT (JJWT 0.12.5) │
│ ORM Framework       │ Hibernate/JPA     │
│ API Architecture    │ REST API          │
│ Testing Frameworks  │ JUnit 5 + Mockito │
└─────────────────────────────────────────┘
```

### Key Dependencies

- **spring-boot-starter-web** - REST API creation
- **spring-boot-starter-data-jpa** - Database access
- **spring-boot-starter-security** - Authentication & authorization
- **spring-boot-starter-validation** - Input validation
- **postgresql** - JDBC driver
- **jjwt** - JWT token handling
- **spring-boot-starter-test** - Testing support

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

1. **Java Development Kit (JDK) 24**
   ```bash
   java -version
   # Should output Java 24.x
   ```

2. **Apache Maven 3.x**
   ```bash
   mvn -version
   # Should output Maven 3.x
   ```

3. **PostgreSQL 12 or later**
   ```bash
   psql --version
   # Should output PostgreSQL 12+
   ```

4. **Git** (for cloning the repository)
   ```bash
   git --version
   ```

---

## 🚀 Installation & Setup

### Step 1: Clone the Repository

```bash
# Using HTTPS
git clone https://github.com/yourusername/pipes.git
cd pipes

# Or using SSH
git clone git@github.com:yourusername/pipes.git
cd pipes
```

> **Tip:** If you're forking this project, replace `yourusername` with your GitHub username.

### Step 2: Create PostgreSQL Database

Connect to PostgreSQL and create the database and user:

```sql
-- Connect to PostgreSQL as admin
psql -U postgres

-- Create database
CREATE DATABASE pipes_db;

-- Create user
CREATE USER pipes_user WITH PASSWORD 'pipes_pass';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE pipes_db TO pipes_user;

-- Connect to the database
\c pipes_db

-- Grant schema privileges
GRANT ALL PRIVILEGES ON SCHEMA public TO pipes_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO pipes_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO pipes_user;

-- Exit
\q
```

**Or use the quick setup:**

```bash
createdb -U postgres pipes_db
createuser -U postgres -P pipes_user  # Enter password: pipes_pass
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE pipes_db TO pipes_user;"
```

### Step 3: Build the Project

```bash
# Clean and build using Maven
mvn clean install

# Or using the Maven wrapper (included in the project)
./mvnw clean install

# On Windows
mvnw.cmd clean install
```

### Step 4: Database Schema Initialization

Hibernate will automatically create the schema on first run (`ddl-auto=update`). After the application starts once, run the initialization script:

```bash
psql -U pipes_user -d pipes_db -f src/main/resources/init.sql
```

This creates:
- Audit log table for tracking user actions
- Performance-optimized indexes on main tables

---

## ▶️ Running the Application

### Option 1: Using Maven

```bash
# Run the Spring Boot application
mvn spring-boot:run

# Or using the Maven wrapper
./mvnw spring-boot:run
```

### Option 2: Direct Java Execution

```bash
# After building with Maven
java -jar target/pipes-1.0.0.jar
```

### Option 3: Using IDE (IntelliJ IDEA)

1. Open the project in IntelliJ IDEA
2. Right-click on `PipesApplication.java`
3. Select "Run 'PipesApplication.main()'"

### ✅ Verify the Application is Running

Once started, you should see:

```
Started PipesApplication in X.XXX seconds
```

The application will be accessible at:
- **Base URL**: `http://localhost:8080`
- **Health Check**: `http://localhost:8080/actuator/health`

---

## ⚙️ Configuration

All configuration is managed in `src/main/resources/application.properties`:

### Database Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pipes_db
spring.datasource.username=pipes_user
spring.datasource.password=pipes_pass
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate auto schema DDL
spring.jpa.hibernate.ddl-auto=update  # Options: create-drop, create, update, validate, none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Server Configuration

```properties
server.port=8080  # REST API port
```

### JWT (JSON Web Token) Configuration

```properties
pipes.jwt.secret=pipes-super-secret-key-change-me-in-production-at-least-256-bits-long
pipes.jwt.expiration-ms=86400000  # 24 hours in milliseconds
```

⚠️ **Security Warning**: Change the `pipes.jwt.secret` to a strong, unique value in production!

### Thread Pool Configuration

```properties
pipes.executor.core-pool-size=4        # Core threads
pipes.executor.max-pool-size=16        # Maximum threads
pipes.executor.queue-capacity=100      # Queue capacity for pending tasks
```

### CORS Configuration

```properties
pipes.cors.allowed-origins=http://localhost:3000,http://127.0.0.1:5500,http://localhost:5500
```

### Logging Configuration

```properties
logging.level.com.pipes=DEBUG                        # Application debug logs
logging.level.org.springframework.security=WARN      # Spring Security warnings
```

### Environment-Specific Configuration

Create additional files for different environments:

- `application-dev.properties` - Development
- `application-prod.properties` - Production
- `application-test.properties` - Testing

Then activate with:

```bash
java -jar target/pipes-1.0.0.jar --spring.profiles.active=prod
```

---

## 📚 API Documentation

### Authentication Endpoints

#### Register User

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure_password_123"
}
```

**Response (201 Created):**
```json
{
  "message": "User registered successfully",
  "username": "john_doe"
}
```

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure_password_123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "expiresIn": 86400000
}
```

### Pipeline Endpoints

#### Create Pipeline

```http
POST /api/pipelines
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Build & Deploy Pipeline",
  "description": "Multi-stage CI/CD pipeline",
  "stages": [
    {
      "order": 1,
      "name": "Build",
      "jobs": [
        {
          "name": "Compile",
          "command": "mvn clean compile"
        }
      ]
    }
  ]
}
```

#### Get All Pipelines

```http
GET /api/pipelines
Authorization: Bearer {token}
```

#### Get Pipeline by ID

```http
GET /api/pipelines/{id}
Authorization: Bearer {token}
```

#### Update Pipeline

```http
PUT /api/pipelines/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Pipeline Name"
}
```

#### Delete Pipeline

```http
DELETE /api/pipelines/{id}
Authorization: Bearer {token}
```

### Pipeline Run Endpoints

#### Execute Pipeline

```http
POST /api/runs/pipeline/{pipelineId}
Authorization: Bearer {token}
```

**Response (201 Created):**
```json
{
  "runId": 123,
  "pipelineId": 1,
  "status": "PENDING",
  "createdAt": "2026-06-07T10:30:00Z"
}
```

#### Get Run by ID

```http
GET /api/runs/{runId}
Authorization: Bearer {token}
```

#### Get Pipeline Runs

```http
GET /api/pipelines/{pipelineId}/runs
Authorization: Bearer {token}
```

#### Get Run Results

```http
GET /api/runs/{runId}/results
Authorization: Bearer {token}
```

---

## 🗄️ Database Schema

### Entity Relationships

```
User (1) ──────────── (M) Pipeline
                           |
                           ├── (1:M) Stage
                           │         └── (1:M) Job
                           │
                           ├── (1:M) PipelineRun
                           │         ├── (1:M) StageResult
                           │         │         └── (1:M) JobResult
```

### Main Tables

| Table | Purpose | Key Fields |
|-------|---------|-----------|
| `users` | User accounts | id, username, password, created_at |
| `pipelines` | Pipeline templates | id, name, description, owner_id |
| `stages` | Pipeline stages | id, pipeline_id, order, name |
| `jobs` | Individual jobs | id, stage_id, name, command |
| `pipeline_runs` | Execution instances | id, pipeline_id, status, started_at, ended_at |
| `stage_results` | Stage execution results | id, pipeline_run_id, stage_id, status |
| `job_results` | Job execution results | id, stage_result_id, job_id, status, output, exit_code |
| `audit_log` | User action audit trail | id, username, action, detail, occurred_at |

### Running Status Enum

```java
enum RunStatus {
    PENDING,   // Waiting to execute
    RUNNING,   // Currently executing
    SUCCESS,   // Completed successfully
    FAILED,    // Execution failed
    CANCELLED  // Manually cancelled
}
```

---

## 📂 Project Structure

```
pipes/
├── README.md                           # This file
├── PACKAGE_STRUCTURE.md                # Detailed package documentation
├── pom.xml                             # Maven configuration
├── mvnw / mvnw.cmd                     # Maven wrapper scripts
├── src/
│   ├── main/
│   │   ├── java/com/pipes/
│   │   │   ├── PipesApplication.java   # Entry point
│   │   │   ├── config/                 # Configuration beans
│   │   │   ├── controller/             # REST endpoints
│   │   │   ├── dto/                    # Data transfer objects
│   │   │   ├── entity/                 # JPA entities
│   │   │   ├── exception/              # Custom exceptions & handlers
│   │   │   ├── pattern/                # Design patterns (Strategy, Builder, Observer)
│   │   │   ├── repository/             # Data access layer
│   │   │   ├── security/               # JWT & authentication
│   │   │   ├── service/                # Business logic
│   │   │   └── util/                   # Utility classes
│   │   └── resources/
│   │       ├── application.properties  # Configuration properties
│   │       ├── init.sql                # Database initialization script
│   │       ├── static/                 # Static resources (CSS, JS, images)
│   │       └── templates/              # HTML templates (if using Thymeleaf)
│   └── test/
│       └── java/com/pipes/             # Unit & integration tests
└── target/                             # Compiled artifacts (auto-generated)
```

For detailed package information, see [PACKAGE_STRUCTURE.md](PACKAGE_STRUCTURE.md)

---

## 🎨 Design Patterns & Architecture

### Architectural Pattern: Layered Architecture

The application follows a **3-tier layered architecture**:

```
Presentation Layer (Controllers) → Business Logic (Services) → Data Access (Repositories)
```

### Design Patterns Used

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Strategy** | `pattern/JobExecutionStrategy.java` | Pluggable job execution strategies |
| **Builder** | `pattern/PipelineRunBuilder.java` | Fluent construction of complex objects |
| **Observer** | `pattern/RunEventListener.java` | Event-driven pipeline monitoring |
| **Repository** | `repository/` | Abstraction of data access |
| **Dependency Injection** | `config/`, Spring IoC | Loose coupling & testability |
| **Singleton** | Spring Beans | Shared service instances |

### Execution Flow

```
1. User Login → JwtUtils generates token
2. API Request → JwtAuthFilter validates token
3. Request → Controller → Service
4. Service → Repository → Database
5. Pending job → PipelineExecutorService (async)
6. Job executes → JobExecutionStrategy
7. Results → RunPersistenceService → Database
8. Events → RunEventListener (Observer pattern)
9. Response → Controller → Client
```

---

## 💻 Development

### Code Organization

- **Controllers**: Handle HTTP requests/responses
- **Services**: Implement business logic and orchestration
- **Repositories**: Manage database operations
- **Entities**: Represent database tables
- **DTOs**: Transfer data between layers
- **Exceptions**: Custom error handling

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PipelineServiceTest

# Run with coverage
mvn test jacoco:report
```

### Building the Application

```bash
# Build without running tests
mvn clean package -DskipTests

# Full build with tests
mvn clean package

# Generate jar
mvn clean build-helper:parse-version assembly:single
```

### IDE Setup (IntelliJ IDEA)

1. Open project in IntelliJ
2. Maven dependencies auto-download
3. Configure JDK 24 in Project Structure
4. Mark `src/main/java` as Sources Root
5. Mark `src/test/java` as Test Sources Root

---

## 🔧 Troubleshooting

### Issue: PostgreSQL Connection Error

**Error**: `org.postgresql.util.PSQLException: Connection to localhost:5432 refused`

**Solution**:
1. Ensure PostgreSQL service is running
2. Check database credentials in `application.properties`
3. Verify PostgreSQL port (default: 5432)

```bash
# Check if PostgreSQL is running
psql -U postgres -c "SELECT version();"
```

### Issue: JWT Token Expired

**Error**: `Invalid JWT token`

**Solution**:
1. Generate a new token by logging in again
2. Check `pipes.jwt.expiration-ms` setting (default: 24 hours)

### Issue: Build Fails with Java Version

**Error**: `maven.compiler.source is set to non-existing JDK`

**Solution**:
```bash
# Verify Java 24 is installed and set as default
java -version

# Set JAVA_HOME to Java 24
export JAVA_HOME=/path/to/java-24  # Linux/Mac
set JAVA_HOME=C:\Java\jdk-24        # Windows
```

### Issue: Port 8080 Already in Use

**Error**: `Address already in use: bind`

**Solution**:
```bash
# Change port in application.properties
server.port=8081

# Or kill the process using port 8080
# Linux/Mac: lsof -ti:8080 | xargs kill -9
# Windows: netstat -ano | findstr :8080
```

### Issue: Database Migrations Failed

**Error**: `Database schema not updated`

**Solution**:
```bash
# Check if init.sql was executed
psql -U pipes_user -d pipes_db -f src/main/resources/init.sql

# View database tables
psql -U pipes_user -d pipes_db -c "\dt"
```

---

## 📝 Logging

The application uses Spring Boot's default logging with SLF4J:

```properties
# Application logs
logging.level.com.pipes=DEBUG

# Spring Security logs
logging.level.org.springframework.security=WARN

# JPA SQL logs
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

View logs:
```bash
# In console output during running
# Or check: target/logs/ (if configured)
```

---

## 🔐 Security Notes

1. **Change JWT Secret**: Before deploying to production, update `pipes.jwt.secret` in `application.properties`
2. **Database Credentials**: Store credentials in environment variables, not in code
3. **CORS Configuration**: Update `pipes.cors.allowed-origins` for your frontend URL
4. **HTTPS**: Use HTTPS in production
5. **Rate Limiting**: Consider implementing rate limiting for API endpoints
6. **Input Validation**: All DTOs have validation annotations

---

## 📄 License

This project is developed as a coursework assignment for **CS 305 Advanced Java at UNYT, Spring 2026**.

**Course Information**:
- Course: CS 305 - Advanced Java
- Institution: UNYT
- Semester: Spring 2026
- Assignment: CI/CD Pipeline Simulator

---

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### Development Workflow

1. **Fork** the repository on GitHub
2. **Clone** your fork locally
   ```bash
   git clone https://github.com/YOUR_USERNAME/pipes.git
   cd pipes
   ```
3. **Create** a feature branch
   ```bash
   git checkout -b feature/amazing-feature
   ```
4. **Make** your changes and commit
   ```bash
   git add .
   git commit -m "Add amazing feature"
   ```
5. **Push** to your fork
   ```bash
   git push origin feature/amazing-feature
   ```
6. **Open** a Pull Request with a clear description

### Pull Request Guidelines

- Provide a clear description of changes
- Reference related issues (use `Fixes #123`)
- Ensure all tests pass
- Update documentation if needed
- Follow existing code style and conventions

### Areas for Contribution

- 🐛 **Bug Fixes** - Found a bug? Report or fix it!
- ✨ **Features** - Implement new job execution strategies
- 📚 **Documentation** - Improve guides and API docs
- 🧪 **Tests** - Add more comprehensive test coverage
- 🎨 **UI** - Create a web dashboard
- ⚡ **Performance** - Optimize queries and caching

---

## 📞 Support & Contact

### Getting Help

- **📖 Documentation** → Check [README.md](README.md) and [PACKAGE_STRUCTURE.md](PACKAGE_STRUCTURE.md)
- **🐛 Bug Reports** → [Open an Issue](https://github.com/yourusername/pipes/issues)
- **💬 Discussions** → [GitHub Discussions](https://github.com/yourusername/pipes/discussions)
- **❓ Q&A** → [Stack Overflow Tag: `pipes-cicd`](https://stackoverflow.com/questions/tagged/pipes-cicd)

### Quick Reference

1. Check the [Troubleshooting](#-troubleshooting) section
2. Review [PACKAGE_STRUCTURE.md](PACKAGE_STRUCTURE.md) for architecture details
3. Check application logs for detailed error messages
4. Review [Spring Boot documentation](https://spring.io/projects/spring-boot)

---

## 📋 Roadmap

- [ ] Web UI Dashboard for pipeline management
- [ ] Docker containerization & Kubernetes support
- [ ] Additional job execution strategies (Docker, Kubernetes, FTP)
- [ ] Advanced scheduling and triggers
- [ ] Webhook support for external systems
- [ ] Email notifications on run completion
- [ ] Performance analytics & dashboards
- [ ] Multi-tenancy support
- [ ] SAML/OAuth2 authentication
- [ ] API rate limiting & throttling

---

## 📊 Project Stats

- **Lines of Code:** ~2000+
- **Test Coverage:** Growing
- **Design Patterns:** 5+
- **REST Endpoints:** 15+
- **Supported Versions:** Java 24+, Spring Boot 3.3.5+

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 Pipes Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 🎓 Academic Information

**Course:** CS 305 - Advanced Java  
**Institution:** UNYT  
**Semester:** Spring 2026  
**Type:** Coursework Assignment  

This project demonstrates:
- ✅ Spring Boot microservices architecture
- ✅ RESTful API design principles
- ✅ Three-tier layered architecture
- ✅ Design patterns (Strategy, Builder, Observer)
- ✅ Asynchronous programming with Java
- ✅ JWT-based security
- ✅ Database design & JPA/Hibernate
- ✅ Test-driven development approach

---

## 🌟 Show Your Support

If this project helped you, please consider:

- ⭐ **Starring** the repository
- 🔗 **Sharing** with your network
- 💬 **Contributing** improvements
- 📢 **Providing** feedback

---

## 📚 Additional Resources

- [Spring Boot Official Docs](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc7519)
- [REST API Best Practices](https://restfulapi.net/)
- [Java Design Patterns](https://github.com/iluwatar/java-design-patterns)

---

<div align="center">

**Made with ❤️ for CS 305 Advanced Java**

[⬆ back to top](#-pipes---cicd-pipeline-simulator)

</div>


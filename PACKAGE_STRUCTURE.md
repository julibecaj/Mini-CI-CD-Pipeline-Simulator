# Pipes Project - Package Structure

**Project:** CI/CD Pipeline Simulator  
**Framework:** Spring Boot 3.3.5  
**Language:** Java 24  
**Group ID:** com.pipes  
**Artifact ID:** pipes

---

## Project Overview

Pipes is a Spring Boot application that simulates a CI/CD pipeline execution system. It manages the creation, execution, and monitoring of automated pipelines with stages and jobs.

---

## Package Structure

```
com.pipes/
├── PipesApplication.java          # Main Spring Boot application entry point
├── config/                         # Configuration classes
├── controller/                     # REST API controllers
├── dto/                            # Data Transfer Objects
├── entity/                         # JPA Entity models
├── exception/                      # Exception handling
├── pattern/                        # Design patterns (Strategy, Builder, Observer)
├── repository/                     # Data access layer (Spring Data JPA)
├── security/                       # Security and authentication
├── service/                        # Business logic layer
└── util/                           # Utility classes
```

---

## Detailed Package Descriptions

### 📦 `com.pipes` (Root)

**File:** `PipesApplication.java`

- Main Spring Boot application class with `@SpringBootApplication` annotation
- Entry point for the application (`public static void main(String[] args)`)

---

### 🔧 `com.pipes.config`

**Purpose:** Application configuration and setup

**Contents:**
- **ExecutorConfig.java** - Configures thread pools/executors for asynchronous pipeline execution
- **SecurityConfig.java** - Spring Security configuration (JWT, authentication, authorization)

**Key Responsibilities:**
- Define Spring beans for managing concurrent task execution
- Configure security policies, filters, and authentication mechanisms
- Set up JWT token handling

---

### 🎯 `com.pipes.controller`

**Purpose:** REST API endpoints and HTTP request handling

**Contents:**
- **AuthController.java** - Authentication endpoints
  - User login
  - User registration
  - Token management
  
- **PipelineController.java** - Pipeline management endpoints
  - Create pipelines
  - View pipeline details
  - Update pipeline configurations
  - List pipelines
  
- **RunController.java** - Pipeline execution endpoints
  - Trigger pipeline runs
  - Monitor execution status
  - View run history and results

**Key Responsibilities:**
- Expose RESTful API endpoints
- Handle HTTP requests and responses
- Delegate business logic to services
- Return appropriate HTTP status codes

---

### 📊 `com.pipes.dto`

**Purpose:** Data Transfer Objects for API communication

**Contents:**
- **AuthDto.java** - Authentication-related DTOs
  - `LoginRequest` - Credentials for login
  - `LoginResponse` - JWT token and user info after login
  - `RegisterRequest` - User registration data
  
- **PipelineDto.java** - Pipeline-related DTOs
  - `CreateRequest` - Pipeline creation payload
  - `PipelineResponse` - Complete pipeline data
  - `PipelineSummary` - Lightweight pipeline info
  - `StageRequest/Response` - Stage configuration
  - `JobRequest/Response` - Job configuration
  
- **RunDto.java** - Pipeline execution DTOs
  - `RunResponse` - Execution run details
  - `RunSummary` - Summary of run
  - `StageResultResponse` - Stage execution results
  - `JobResultResponse` - Job execution results

**Key Responsibilities:**
- Decouple API layer from entity models
- Provide typed objects for JSON serialization/deserialization
- Ensure API contracts remain stable

---

### 🗄️ `com.pipes.entity`

**Purpose:** JPA Entity models for database persistence

**Contents:**
- **User.java** - User account entity
  - Username, password, roles
  
- **Pipeline.java** - Pipeline template entity
  - Pipeline name, description
  - Collection of stages
  - Owner/creator reference
  
- **Stage.java** - Pipeline stage entity
  - Stage name, order
  - Collection of jobs
  
- **Job.java** - Individual job entity
  - Job name, command, parameters
  - Execution strategy
  
- **PipelineRun.java** - Pipeline execution instance
  - `RunStatus` enum (PENDING, RUNNING, SUCCESS, FAILED, CANCELLED)
  - Reference to pipeline template
  - Start/end timestamps
  - Collection of stage results
  
- **StageResult.java** - Stage execution result
  - Status, start/end times
  - Collection of job results
  
- **JobResult.java** - Individual job execution result
  - Status, output, exit code
  - Start/end timestamps

**Key Responsibilities:**
- Map to database tables via JPA
- Store domain objects persistently
- Define relationships between entities

---

### ⚠️ `com.pipes.exception`

**Purpose:** Custom exception handling and error responses

**Contents:**
- **GlobalExceptionHandler.java** - Spring `@ControllerAdvice` for global exception handling
  - `ErrorResponse` - Standardized error response format
  
- **PipelineExecutionException.java** - Custom exception for pipeline execution failures
  
- **PipesAccessDeniedException.java** - Custom exception for authorization failures
  
- **ResourceNotFoundException.java** - Custom exception for missing resources

**Key Responsibilities:**
- Handle exceptions across the application
- Return consistent error responses
- Map exceptions to appropriate HTTP status codes

---

### 🎨 `com.pipes.pattern`

**Purpose:** Design pattern implementations for pipeline execution

**Contents:**
- **JobExecutionStrategy.java** - Strategy interface/abstract class
  - Defines contract for job execution strategies
  - Marker: Strategy Pattern
  
- **LocalShellStrategy.java** - Concrete strategy for executing shell commands locally
  - Implements shell command execution
  
- **PipelineRunBuilder.java** - Builder for constructing pipeline run objects
  - Fluent API for creating runs
  - Marker: Builder Pattern
  
- **RunEventListener.java** - Listener interface for pipeline events
  - Marker: Observer Pattern
  - Notifies listeners of status changes

**Key Responsibilities:**
- Provide pluggable execution strategies
- Support multiple job execution types (extensible design)
- Enable event-driven architecture for pipeline monitoring

---

### 🔄 `com.pipes.repository`

**Purpose:** Data access layer using Spring Data JPA

**Contents:**
- **PipelineRepository.java** - CRUD operations for Pipeline entities
  - Extends `JpaRepository<Pipeline, Long>`
  - Custom query methods for pipeline lookups
  
- **PipelineRunRepository.java** - CRUD operations for PipelineRun entities
  - Extends `JpaRepository<PipelineRun, Long>`
  - Query methods for run history and status
  
- **UserRepository.java** - CRUD operations for User entities
  - Extends `JpaRepository<User, Long>`
  - Find user by username (for authentication)

**Key Responsibilities:**
- Abstract database access logic
- Provide type-safe queries
- Enable test mocking and repository pattern

---

### 🔐 `com.pipes.security`

**Purpose:** Authentication, authorization, and JWT handling

**Contents:**
- **JwtUtils.java** - JWT token generation and validation
  - Token creation with claims
  - Token parsing and verification
  - Claims extraction
  
- **JwtAuthFilter.java** - Spring Security filter for JWT validation
  - Intercepts HTTP requests
  - Extracts and validates JWT tokens
  - Sets security context
  
- **PipesUserDetailsService.java** - Spring Security `UserDetailsService` implementation
  - Loads user information by username
  - Used by authentication manager

**Key Responsibilities:**
- Secure API endpoints with JWT tokens
- Authenticate users
- Authorize access based on roles
- Maintain security context

---

### 🛠️ `com.pipes.service`

**Purpose:** Business logic layer

**Contents:**
- **AuthService.java** - User authentication and registration
  - Handle login requests
  - Manage user registration
  - Password management
  
- **PipelineService.java** - Pipeline template management
  - Create, read, update, delete pipelines
  - Validate pipeline configurations
  - Manage stages and jobs
  
- **PipelineExecutorService.java** - Pipeline execution orchestration
  - Execute pipelines asynchronously
  - Manage concurrent execution
  - Handle timeouts and failures
  
- **RunService.java** - Pipeline run management
  - Retrieve run history
  - Monitor execution status
  - Query run results
  
- **RunPersistenceService.java** - Persist run results
  - Save execution results
  - Update run status
  - Store job outputs
  
- **JdbcDashboardService.java** - Dashboard data aggregation
  - Provide statistics
  - Generate reports
  - Performance metrics

**Key Responsibilities:**
- Implement business rules
- Orchestrate complex workflows
- Coordinate between repositories and controllers
- Handle transactions

---

### 📋 `com.pipes.util`

**Purpose:** Utility classes and helper functions

**Contents:**
- **ApiResponse.java** - Generic API response wrapper
  - Standardized success/error responses
  - Status codes and messages
  
- **FilteredList.java** - Paginated/filtered list wrapper
  - Pagination metadata
  - Filtering support
  - Result set information

**Key Responsibilities:**
- Provide reusable helper classes
- Standardize response formats
- Facilitate common operations

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│                   REST Clients                      │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│         Controller Layer                            │
│  (AuthController, PipelineController, RunController)│
└────────────┬──────────────────────┬────────────────┘
             │                      │
        ┌────▼──────┐          ┌────▼──────────┐
        │ DTO Layer │          │Exception      │
        │(Request)  │          │Handler        │
        └────┬──────┘          └────┬──────────┘
             │                      │
┌────────────▼──────────────────────▼────────────────┐
│           Service Layer                            │
│  (Business Logic & Orchestration)                  │
└──────────┬─────────────────────┬───────────────────┘
           │                     │
      ┌────▼──────┐         ┌────▼──────┐
      │Repository │         │Pattern    │
      │Layer      │         │(Strategy, │
      │(Data      │         │Builder,   │
      │Access)    │         │Observer)  │
      └────┬──────┘         └────┬──────┘
           │                     │
      ┌────▼─────────────────────▼──────┐
      │   Entity Layer (JPA Models)     │
      │ (User, Pipeline, Run, Results)  │
      └────┬─────────────────────────────┘
           │
      ┌────▼──────────────────────────┐
      │    PostgreSQL Database        │
      └───────────────────────────────┘

Security Layer (JWT):
┌──────────────────────────────────────┐
│ JwtAuthFilter → JwtUtils             │
│ PipesUserDetailsService              │
└──────────────────────────────────────┘
```

---

## Key Design Patterns

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Strategy Bundle** | `pattern/` | Different job execution strategies |
| **Builder** | `pattern/PipelineRunBuilder.java` | Construct complex run objects |
| **Observer** | `pattern/RunEventListener.java` | Event-driven pipeline monitoring |
| **Repository** | `repository/` | Abstract data access |
| **DAO** | `entity/` + `repository/` | Data access object pattern |
| **Service Locator** | `service/` | Centralized business logic |
| **Dependency Injection** | `config/` | Spring IoC container |

---

## Data Flow

### User Registration & Login
```
User → AuthController → AuthService → UserRepository → Database
                                        ↓
                                   JwtUtils (Token generation)
                                        ↓
                                   LoginResponse (DTO)
```

### Pipeline Creation
```
User → PipelineController → PipelineService → PipelineRepository → Database
```

### Pipeline Execution
```
User → RunController → PipelineExecutorService → JobExecutionStrategy
                            ↓
                       RunPersistenceService → Database
                            ↓
                       RunEventListener (Observer pattern)
```

---

## Dependencies

- **Spring Boot**: Web, Data JPA, Security, Validation
- **Database**: PostgreSQL
- **Authentication**: JWT (JJWT library)
- **Test**: JUnit, Spring Security Test

---

## Summary

The Pipes project follows a **layered architecture** with clear separation of concerns:

1. **Presentation Layer**: Controllers, DTOs
2. **Business Logic Layer**: Services
3. **Persistence Layer**: Repositories, Entities, Database
4. **Cross-Cutting Concerns**: Security, Exception Handling, Configuration
5. **Extensibility**: Design patterns for job execution strategies

This structure allows for maintainability, testability, and scalability of the pipeline execution system.


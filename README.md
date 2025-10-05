# Trade Store Application

A microservices-based trade processing system built with Spring Boot, implementing Event Sourcing and CQRS patterns for scalable and reliable trade data management.

## 🏗️ Architecture Overview

The Trade Store Application follows a microservices architecture with the following key components:

- **Event Sourcing**: All trade events are stored in DynamoDB for complete audit trail
- **CQRS**: Separate read and write models with PostgreSQL projections
- **Asynchronous Processing**: Kafka-based message queuing for scalable processing
- **Unified Validation**: Common validation module with AOP-based business rules

## 📦 Modules

### 1. Trade Ingestion Service (Port 8081)
**Purpose**: Entry point for trade data ingestion
- **REST API Controller**: Handles HTTP requests for trade submission
- **Kafka Producer**: Publishes trade events to message queue
- **Query Service**: Provides endpoints for querying trade data and exceptions
- **File Ingestion**: Supports batch processing of trade files

**Key Features**:
- RESTful API endpoints for trade submission
- Request ID generation for tracking
- Integration with Kafka for event publishing
- Exception querying capabilities

### 2. Trade Capture Service (Port 8082)
**Purpose**: Processes trade events and persists to event store
- **Kafka Consumer**: Consumes trade events from message queue
- **Trade Processor**: Converts and processes trade data
- **DynamoDB Client**: Handles event store persistence

**Key Features**:
- Event-driven processing with Kafka listeners
- DynamoDB integration for event sourcing
- Trade model conversion and validation
- Error handling and logging

### 3. Trade Validation & Storage Service (Port 8083)
**Purpose**: Validates trades and manages projections
- **Validation Engine**: Orchestrates trade validation process
- **Projection Service**: Manages read model updates
- **Expiry Scheduler**: Automated expiry processing
- **PostgreSQL Persistence**: Handles projection and exception storage

**Key Features**:
- Business rule validation (version control, maturity dates, expiry)
- CQRS projection management
- Scheduled expiry processing (every 5 minutes)
- Exception handling and storage

### 4. Trade Common Module
**Purpose**: Shared components and validation logic
- **Trade Model**: Common data models and DTOs
- **Validation Annotations**: Custom validation annotations
- **Common Validation Service**: Unified validation logic
- **Validation Aspect**: AOP-based validation processing

**Key Features**:
- Shared data models across services
- Centralized validation rules
- Aspect-oriented programming for validation
- Reusable business logic

## 🗄️ Data Stores

### DynamoDB (Event Store)
- **Purpose**: Immutable event storage for complete audit trail
- **Schema**: `reqtradeid` (HASH Key), `tradeId`, `version`, `counterPartyId`, `bookId`, `maturityDate`, `createdDate`, `expired`

### PostgreSQL (Projection Store)
- **Purpose**: Read-optimized projections for queries and reporting
- **Schema**: `trade_id` (Primary Key), `version`, `counter_party_id`, `book_id`, `maturity_date`, `created_date`, `expired`, `request_id`

### PostgreSQL (Exception Store)
- **Purpose**: Storage for validation failures and exceptions
- **Schema**: `id` (Primary Key), `trade_id`, `version`, `exception_reason`, `request_id`, `created_date`

## 🔄 Message Queue

### Kafka Cluster
- **Topic**: `trade_ingestion`
- **Purpose**: Asynchronous event processing and guaranteed delivery
- **Integration**: Confluent Cloud for managed Kafka service

## 📊 Architecture Diagrams

The `docs/` folder contains detailed PlantUML diagrams:

### Component Architecture Diagram
**File**: `docs/trade-store-component-diagram.puml`
- Shows all microservices and their components
- Illustrates data flow between services
- Displays external system integrations
- Includes port information for each service

### Sequence Diagram
**File**: `docs/trade-store-sequence-diagram.puml`
- Complete trade processing flow
- Query and exception handling flows
- Automated expiry processing
- Error scenarios and handling

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Gradle 7+
- Docker (for local development)
- Access to Kafka cluster
- AWS DynamoDB access
- PostgreSQL database

### Configuration
1. **Secrets Management**: Configure the following secrets in your environment:
   - `DB_PASSWORD`: PostgreSQL database password
   - `KAFKA_PASSWORD`: Kafka authentication password
   - `AWS_SECRET_KEY`: AWS credentials for DynamoDB access

2. **Application Properties**: Update `application.properties` files in each module with your specific configuration.

### Building the Application
```bash
# Build all modules
./gradlew build

# Run specific service
./gradlew :trade-ingestion:bootRun
./gradlew :trade-capture:bootRun
./gradlew :trade-validation-storage:bootRun
```

### CI/CD Pipeline
The application includes a comprehensive GitHub Actions workflow (`.github/workflows/gradle.yml`) that:
- Builds and tests all modules
- Replaces secrets in configuration files
- Runs security vulnerability scanning with OSV-Scanner
- Publishes build artifacts and security reports
- Generates comprehensive build information

## 🔒 Security Features

- **Secret Management**: GitHub Secrets integration for sensitive configuration
- **Vulnerability Scanning**: Automated OSV-Scanner integration for dependency security
- **Build Artifacts**: Secure artifact publishing with build metadata
- **Configuration Security**: Placeholder-based secret replacement

## 📈 Monitoring and Observability

- **Request Tracking**: Unique request IDs for end-to-end tracing
- **Exception Logging**: Comprehensive error logging and storage
- **Audit Trail**: Complete event sourcing for compliance
- **Health Checks**: Service health monitoring capabilities

## 🛠️ Development

### Project Structure
```
TradeStoreApp/
├── trade-ingestion/          # Trade Ingestion Service
├── trade-capture/            # Trade Capture Service
├── trade-validation-storage/ # Trade Validation & Storage Service
├── trade-common/             # Common Module
├── architecture-tests/       # Integration Tests
├── docs/                     # Architecture Diagrams
└── .github/workflows/        # CI/CD Pipeline
```

### Key Technologies
- **Spring Boot 3.x**: Application framework
- **Spring Kafka**: Message queuing
- **Spring Data JPA**: Database integration
- **AWS SDK**: DynamoDB integration
- **PostgreSQL**: Relational database
- **Gradle**: Build automation
- **PlantUML**: Architecture documentation

## 📝 API Endpoints

### Trade Ingestion Service (8081)
- `POST /api/trades` - Submit new trade
- `GET /api/trades/exceptions?requestId={id}` - Query exceptions

### Trade Capture Service (8082)
- Internal Kafka consumer endpoints

### Trade Validation Service (8083)
- `POST /api/trades/validate` - Validate trade
- `GET /api/trades/projections` - Query projections

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and security scans
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

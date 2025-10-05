# Trade Store System Documentation

This directory contains PlantUML diagrams that document the architecture and design of the Trade Store microservices system.

## Diagrams Overview

### 1. Component Diagram (`trade-store-component-diagram.puml`)
**Purpose:** Shows the high-level architecture and component relationships

**Key Elements:**
- **Microservices:** Trade Ingestion, Trade Capture, Trade Validation & Storage
- **External Systems:** Kafka Cluster, DynamoDB, PostgreSQL
- **Data Flow:** REST APIs → Kafka → Processing → Storage
- **Dependencies:** Common module shared across services

**View:** Use this diagram to understand the overall system architecture and how components interact.

### 2. Sequence Diagram (`trade-store-sequence-diagram.puml`)
**Purpose:** Illustrates the detailed flow of trade processing from ingestion to storage

**Key Flows:**
- **Trade Ingestion:** Client → REST API → Kafka → Capture Service
- **Trade Processing:** Validation → Storage → Projection Updates
- **Error Handling:** Various failure scenarios and recovery
- **Scheduled Tasks:** Automated expiry processing

**View:** Use this diagram to understand the step-by-step processing of trades and error scenarios.

### 3. Architecture Overview (`trade-store-architecture-overview.puml`)
**Purpose:** Provides a comprehensive view of the entire system including infrastructure layers

**Key Layers:**
- **External Layer:** Web clients, mobile apps, external systems
- **API Gateway Layer:** Load balancing and API management
- **Microservices Layer:** Core business services
- **Common Services Layer:** Shared components and utilities
- **Message Layer:** Kafka messaging infrastructure
- **Data Layer:** Event store and projection store
- **Infrastructure Layer:** AWS services, monitoring, security

**View:** Use this diagram to understand the complete system architecture including infrastructure concerns.

### 4. Data Flow Diagram (`trade-store-data-flow-diagram.puml`)
**Purpose:** Shows how data flows through the system and the data models used

**Key Data Flows:**
- **Ingestion Flow:** Data sources → Processing → Event store
- **Validation Flow:** Event processing → Validation → Projection/Exception storage
- **Query Flow:** Data retrieval from various stores
- **Scheduled Flow:** Automated processing tasks

**Data Models:**
- Trade model structure
- Database schemas (DynamoDB, PostgreSQL)
- Validation rules and business logic

**View:** Use this diagram to understand data flow patterns and database schemas.

### 5. Deployment Diagram (`trade-store-deployment-diagram.puml`)
**Purpose:** Illustrates the physical deployment architecture and infrastructure setup

**Key Elements:**
- **Cloud Infrastructure:** AWS services and availability zones
- **Service Deployment:** Multiple instances for high availability
- **Load Balancing:** Elastic Load Balancer distribution
- **Database Setup:** Primary-replica PostgreSQL configuration
- **Managed Services:** DynamoDB, Kafka, CloudWatch monitoring
- **Auto Scaling:** Automatic scaling based on load

**Infrastructure Details:**
- Port configurations for each service
- Security groups and VPC setup
- Monitoring and logging configuration
- Backup and disaster recovery

**View:** Use this diagram to understand the production deployment architecture and infrastructure requirements.

## How to View the Diagrams

### Option 1: Online PlantUML Viewer
1. Copy the content of any `.puml` file
2. Go to [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
3. Paste the content and view the rendered diagram

### Option 2: VS Code Extension
1. Install the "PlantUML" extension in VS Code
2. Open any `.puml` file
3. Use `Ctrl+Shift+P` → "PlantUML: Preview Current Diagram"

### Option 3: Local PlantUML Installation
1. Install PlantUML locally
2. Generate images: `plantuml -tpng *.puml`
3. View the generated PNG files

## System Architecture Summary

The Trade Store system is built using a **microservices architecture** with the following key characteristics:

### **Core Services:**
- **Trade Ingestion Service (Port 8081):** REST API for trade submission and querying
- **Trade Capture Service (Port 8082):** Kafka consumer for trade processing and DynamoDB storage
- **Trade Validation & Storage Service (Port 8083):** Business rule validation and PostgreSQL projections

### **Key Patterns:**
- **Event Sourcing:** Immutable trade events stored in DynamoDB
- **CQRS:** Separate read models (projections) in PostgreSQL
- **Microservices:** Independent, scalable services
- **Message-Driven:** Asynchronous processing via Kafka
- **AOP Validation:** Cross-cutting validation concerns

### **Technology Stack:**
- **Backend:** Spring Boot 3.3.4, Java 21
- **Messaging:** Apache Kafka (Confluent Cloud)
- **Databases:** AWS DynamoDB, PostgreSQL
- **Build:** Gradle Multi-module
- **Testing:** JUnit 5, Mockito

### **Business Rules:**
1. **Version Control:** Reject trades with lower version numbers
2. **Maturity Date:** Reject trades with past maturity dates
3. **Expiry Processing:** Automatically mark expired trades
4. **Exception Handling:** Store rejected trades with reasons

## Maintenance

These diagrams should be updated whenever:
- New services are added
- Data flow patterns change
- Technology stack is updated
- Business rules are modified

Keep the diagrams synchronized with the actual implementation to ensure they remain accurate and useful for development and documentation purposes.

# 🚀 Sentinel API Monitoring Dashboard

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.x-blue.svg)](https://reactjs.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.x-38bdf8.svg)](https://tailwindcss.com/)
[![Ollama](https://img.shields.io/badge/AI-Ollama%20%2F%20Qwen2.5-orange.svg)](https://ollama.ai/)

> **Monitor. Analyze. Optimize.**

**Sentinel** is a full-stack API Monitoring Dashboard that enables developers and organizations 
to track API availability, response times, HTTP status codes, and overall endpoint health through an intuitive,
interactive interface enhanced with AI-driven incident analysis.

---

## 📖 Overview

Modern applications rely heavily on interconnected microservices and APIs. A single failing endpoint can cause cascading failures across your infrastructure.

**Sentinel** provides a centralized monitoring platform to:

* Monitor APIs continuously with automated scheduled background checks.
* Visualize latency trends, success rates, and endpoint availability.
* Detect slow and failed endpoints based on configurable monitoring conditions.
* Generate automated alerts for endpoint failures and high latency.
* Analyze incidents using localized AI powered by Ollama and Qwen 2.5.
* Organize monitored APIs through projects, services, and endpoints.

---

## ✨ Features

### 🔐 Authentication & Security

* Secure JWT-based registration and login
* Protected application routes and API endpoints
* User session state management

![Authentication](./screenshots/authentication.png)

### 📊 Real-Time Dashboard

* **Health Overview**: Current endpoint health across monitored APIs
* **Performance Analytics**: Response-time and health information from recorded checks
* **Slow API Detection**: Automatically identifies endpoints with response times above 1000 ms
* **Incident Summary**: Active alert and health-status overview
* **Dark Mode UI**: Modern dark-themed monitoring interface

![Dashboard](./screenshots/dashboard.png)

### 📁 Project Management

* Organize APIs into distinct business projects
* Full CRUD management for projects
* Associate monitored services with projects

![Project Management](./screenshots/projects.png)

### 🔧 Service Management

* Create and manage logical API services within projects
* Group endpoints by service or application component
* Track service-level endpoint health

![Service Management](./screenshots/services.png)

### 🌐 Endpoint Management

* Register HTTP/HTTPS API endpoints (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`)
* Configure endpoint timeout limits
* Enable or disable individual endpoints for monitoring
* Associate endpoints with monitored services

![Endpoint Management](./screenshots/endpoints.png)

### 📊 Metrics Management

* Track response time, HTTP status codes, timestamps, and success state
* Store historical metrics for every monitoring execution
* Retrieve endpoint-specific metric history
* Identify slow API responses above the 1000 ms threshold
* Calculate average response time across successful checks
* Calculate current healthy and failed endpoint counts based on the latest health state
* Retrieve the latest monitoring records
* Calculate endpoint-specific historical response-time baselines
* Detect statistical latency anomalies using configurable sensitivity
* Explain anomalies with average, standard deviation, threshold, and deviation data

### Service Dependency Mapping

* Define directional dependencies between services in the same project
* Prevent self-dependencies, duplicates, cross-project relationships, and circular graphs
* View project dependency graphs with service health status
* Analyze direct, indirect, confirmed, and potentially affected services
* Inspect dependency relationships from the `/dependencies` frontend page

![Metrics Management](./screenshots/metrics.png)

### 🚨 Alert Management

* Automated incident detection for failed and slow endpoints
* Severity categorization (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
* Active and resolved alert tracking
* Alert history with resolution status
* Evidence-based `ANOMALY_DETECTED` alerts with duplicate suppression and automatic resolution

![Alerts Management](./screenshots/alerts.png)

### 🤖 AI Incident Analysis

* **Incident Context Building**: Builds analysis context from alert and monitoring data
* **Root Cause Analysis**: Generates possible causes and recommended actions
* **Confidence Scoring**: Provides a confidence value for diagnostic insights
* **Local Model Execution**: Uses Ollama with `qwen2.5:7b` for local AI inference

---

## 🛠 Tech Stack

### Frontend

* **Framework**: React 18 with Vite
* **Styling**: Tailwind CSS
* **HTTP Client**: Axios
* **Routing**: React Router DOM
* **Charts**: Recharts
* **Icons**: Lucide React

### Backend

* **Language**: Java 21
* **Framework**: Spring Boot 3.x
* **Security**: Spring Security, JWT (JSON Web Tokens)
* **Persistence**: Spring Data JPA, Hibernate
* **REST Integration**: Spring `RestClient`
* **Documentation**: OpenAPI / Swagger (SpringDoc UI)
* **Scheduling**: Spring Scheduled Tasks

### Database

* **Primary**: PostgreSQL
* **Development / In-Memory**: H2 Database support

### AI Engine

* **Local LLM Runner**: Ollama
* **Model**: `qwen2.5:7b`

---

## 📁 Project Structure

```text
Sentinel_API/
├── src/                                  # Spring Boot Backend Codebase
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── controller/               # REST API Controllers
│   │   │   ├── dto/                      # Data Transfer Objects
│   │   │   ├── entity/                   # JPA Database Entities
│   │   │   ├── repository/               # Spring Data JPA Repositories
│   │   │   ├── scheduler/                # Scheduled Endpoint Monitoring Engine
│   │   │   ├── security/                 # JWT Authentication Filters & Utilities
│   │   │   ├── service/                  # Service Layer Interfaces
│   │   │   └── serviceImplDb/            # Database Service Implementations & AI Engine
│   │   └── resources/
│   │       └── application.yaml          # Application Configuration
├── sentinel-api-frontend/                # React Frontend Codebase
│   ├── src/
│   │   ├── components/                   # UI Components & Modules
│   │   ├── pages/                        # Page Views
│   │   ├── services/                     # Axios API Integration Services
│   │   └── App.jsx                       # Main Routing & Application Setup
│   └── package.json
└── screenshots/                          # Dashboard Visual Screenshots
```

---

## 📋 Prerequisites

Before running Sentinel API, ensure you have installed:

* **Java Development Kit (JDK 21 or later)**: `java -version`
* **Node.js (v18+) & npm**: `node -v` and `npm -v`
* **PostgreSQL Database** (or use the H2 database configuration)
* **Ollama** (Required for AI Incident Analysis features)

---

## ⚙️ Installation & Setup Guide

### 1. Clone Repository

```bash
git clone https://github.com/chowgulesonakshi-lab/Sentinel_API.git
cd Sentinel_API
```

---

### 2. AI Engine Setup (Ollama)

To enable AI Incident Analysis:

1. Install [Ollama](https://ollama.ai/).

2. Pull the Qwen 2.5 7B model:

   ```bash
   ollama pull qwen2.5:7b
   ```

3. Start the Ollama server:

   ```bash
   ollama serve
   ```

The default Ollama server runs on port `11434`.

---

### 3. Backend Setup

Configure database parameters in `src/main/resources/application.yaml` or pass environment variables:

```yaml
server:
  port: ${PORT:8080}

spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/sentinel_api}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

Run the Spring Boot application using Gradle:

#### Windows

```powershell
.\gradlew.bat bootRun
```

#### Linux / macOS

```bash
./gradlew bootRun
```

The backend server starts at:

```text
http://localhost:8080
```

---

### 4. Frontend Setup

Navigate to the frontend directory:

```bash
cd sentinel-api-frontend
```

Install NPM packages:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

The frontend application starts at:

```text
http://localhost:5173
```

---

## 🔑 Default Configuration & Environment Variables

| Variable      | Default Value                                   | Description                |
| :------------ | :---------------------------------------------- | :------------------------- |
| `PORT`        | `8080`                                          | Backend HTTP Server Port   |
| `DB_URL`      | `jdbc:postgresql://localhost:5432/sentinel_api` | Database JDBC URL          |
| `DB_USERNAME` | `postgres`                                      | Database Username          |
| `DB_PASSWORD` | `postgres`                                      | Database Username Password |
| `OLLAMA_URL`  | `http://localhost:11434`                        | Ollama AI API Endpoint     |

---

## 📡 REST API Reference

The backend provides RESTful endpoints. Swagger documentation is available interactively at `http://localhost:8080/swagger-ui.html` when the application is running.

### 🔐 Authentication (`/`)

* `POST /signup` - Register a new user account
* `POST /login` - Authenticate user and receive JWT bearer token

### 📊 Dashboard (`/dashboard`)

* `GET /dashboard` - Retrieve aggregated dashboard metrics summary
* `GET /dashboard/recent-metrics` - Get recent health-check records
* `GET /dashboard/health` - Retrieve current health information per endpoint

### 📁 Projects (`/projects`)

* `GET /projects` - List all monitoring projects
* `GET /projects/{id}` - Fetch project details by ID
* `GET /projects/user/{ownerId}` - List projects by owner user ID
* `POST /projects` - Create a new project
* `PUT /projects/{id}` - Update project metadata
* `DELETE /projects/{id}` - Remove a project

### 🔧 Services (`/services`)

* `GET /services` - List all monitored API services
* `GET /services/project/{projectId}` - List services associated with a project
* `POST /services` - Register a new API service
* `PUT /services/id/{id}` - Update service configuration
* `DELETE /services/id/{id}` - Delete an API service

### 🌐 Endpoints (`/endpoints`)

* `GET /endpoints` - Fetch all configured target endpoints
* `GET /endpoints/{id}` - Get endpoint details by ID
* `GET /endpoints/service/{serviceId}` - Fetch endpoints under a specific service
* `POST /endpoints` - Add a new API endpoint for monitoring
* `PUT /endpoints/{id}` - Update endpoint monitoring settings
* `DELETE /endpoints/{id}` - Remove an endpoint from monitoring

### 📊 Metrics (`/metrics`)

* `GET /metrics` - List all historical metric execution records
* `GET /metrics/{id}` - Get a metric record by ID
* `GET /metrics/endpoint/{endpointId}` - Get metric history for an endpoint
* `GET /metrics/endpoint/{endpointId}/latest` - Get the latest metric for an endpoint
* `GET /metrics/slow` - List metric records exceeding the specified response-time threshold
* `GET /metrics/status/{statusCode}` - Get metrics matching an HTTP status code
* `GET /metrics/latest` - Get the 10 most recent metric records
* `GET /metrics/average-response-time` - Get average response time across successful checks
* `GET /metrics/up-count` - Count endpoints whose latest health state is UP
* `GET /metrics/down-count` - Count endpoints whose latest health state is DOWN
* `GET /metrics/failed-count` - Count historical failed metric checks
* `GET /metrics/slow-count` - Count endpoints whose latest response time exceeds 1000 ms
* `GET /metrics/last-health-check` - Get the timestamp of the latest recorded health check

Metrics responses include anomaly evaluation data when enough valid endpoint history exists, including the historical average, standard deviation, detection threshold, deviation, sensitivity, observation count, and explanation.

### Alert Rules (`/alert-rules`)

* `GET /alert-rules` - List configured alert rules
* `GET /alert-rules/{id}` - Retrieve an alert rule
* `GET /alert-rules/endpoint/{endpointId}` - List rules for an endpoint
* `GET /alert-rules/service/{serviceId}` - List rules for a service
* `POST /alert-rules` - Create an alert rule
* `PUT /alert-rules/{id}` - Update an alert rule
* `DELETE /alert-rules/{id}` - Delete an alert rule
* `PATCH /alert-rules/{id}/toggle` - Enable or disable a rule

### 🚨 Alerts (`/alerts`)

* `GET /alerts` - List all system alerts
* `GET /alerts/active` - Fetch active unresolved incident alerts
* `GET /alerts/resolved` - Fetch historical resolved alerts
* `GET /alerts/recent` - Get recent alert log feed
* `GET /alerts/critical-count` - Count critical alerts

Anomaly alerts include the observed latency, historical average, standard deviation, calculated threshold, deviation from baseline, sensitivity multiplier, and detection explanation.

### Service Dependencies (`/service-dependencies`)

* `POST /service-dependencies` - Create a service dependency
* `PUT /service-dependencies/{id}` - Update a dependency
* `DELETE /service-dependencies/{id}` - Remove a dependency
* `GET /service-dependencies/project/{projectId}` - List project dependencies
* `GET /service-dependencies/service/{serviceId}` - List dependencies for a service
* `GET /service-dependencies/project/{projectId}/graph` - Retrieve the complete project graph with service health
* `GET /service-dependencies/project/{projectId}/impact/{serviceId}` - Analyze direct and indirect dependency impact

### 🤖 AI Incident Analysis (`/ai`)

* `POST /ai/analyze/{alertId}` - Analyze alert context using Ollama and retrieve diagnostic insights

---

## 📌 API Monitoring Workflow

```text
       +-------------------+
       |    User Login     |
       +---------+---------+
                 |
                 v
       +-------------------+
       | JWT Auth Verified |
       +---------+---------+
                 |
                 v
   +---------------------------+
   | Create Project & Service  |
   +-------------+-------------+
                 |
                 v
   +---------------------------+
   | Configure Target Endpoints|
   +-------------+-------------+
                 |
                 v
   +-----------------------------+
   | Background Scheduler Engine |
   |   (Periodic HTTP Checks)    |
   +-------------+---------------+
                 |
        +--------+--------+
        |                 |
        v                 v
+---------------+ +---------------+
| Metrics Stored| | Alerts Checked|
+-------+-------+ +-------+-------+
        |                 |
        +--------+--------+
                 |
                 v
   +---------------------------+
   |  Latest Endpoint Health   |
   |  & Performance Analysis   |
   +-------------+-------------+
                 |
                 v
   +---------------------------+
   | Real-Time Dashboard View  |
   +-------------+-------------+
                 |
                 v
   +---------------------------+
   |  AI Incident Analysis     |
   |   (Ollama + Qwen 2.5)     |
   +---------------------------+
```

---

## 📊 Monitoring & Analysis Logic

Sentinel continuously records monitoring results for configured endpoints.

For each health check, the system records:

* Response time
* HTTP status code
* Success/failure state
* Endpoint health status
* Timestamp

The dashboard and metrics analysis use the **latest health state of each endpoint** when calculating current endpoint-level health counts.

Slow APIs are identified when the latest successful response for an endpoint has a response time **greater than 1000 ms**.

Historical metric records remain available for response-time and health analysis.

### Statistical anomaly detection

Anomaly detection is evaluated independently for each endpoint. Successful historical measurements are filtered to remove missing, invalid, non-positive, and failed-check values. The configured observation window is used to calculate an endpoint-specific average and standard deviation.

The default detection rule is:

```text
anomaly when current response time > historical average
          + (sensitivity multiplier × standard deviation)
```

Detection is skipped until the configured minimum number of valid observations is available. Anomaly detection can be enabled or disabled per endpoint, and its sensitivity, history window, and minimum sample count can be configured from the endpoint settings modal.

### Service dependency impact

Dependencies are stored as `dependent service → dependency service`. When a service is selected in the dependency graph, Sentinel traverses downstream relationships to identify direct and indirect dependents. Monitoring evidence distinguishes confirmed unhealthy services from services that are only potentially affected; a dependency relationship alone does not mark a service as failed.

When AI incident analysis is requested, the incident context includes dependency relationships and potential downstream impact as evidence separate from root-cause hypotheses.

---

## Verification

Backend tests:

```powershell
.\gradlew.bat test
```

Frontend production build:

```powershell
cd sentinel-api-frontend
npm run build
```

The backend test suite covers anomaly baselines, invalid and insufficient history, configurable sensitivity, duplicate anomaly alerts, anomaly resolution, dependency validation, circular dependency prevention, dependency graphs, and direct/indirect impact analysis.

### Capability evidence

The following distinction keeps the project claims aligned with the available evidence:

| Capability | Implementation evidence | Verification status |
| :--- | :--- | :--- |
| Configurable API monitoring | Endpoint-level intervals, timeouts, expected status codes, request configuration, and enable/disable controls | Covered by existing monitoring and endpoint tests |
| Dynamic alert management | Alert rules, evaluators, active/resolved lifecycle, duplicate suppression, and resolution | Covered by alert-rule and monitoring tests |
| Evidence-based AI incident analysis | Ollama incident context includes telemetry, endpoint history, alert data, and dependency context | AI context tests pass; live Ollama inference is environment-dependent |
| Statistical anomaly detection | Endpoint-specific historical average, standard deviation, sensitivity, minimum samples, and anomaly evidence | Anomaly unit and alert-lifecycle tests pass |
| Service dependency mapping and impact analysis | Dependency CRUD, graph, health status, cycle validation, and direct/indirect impact APIs | Dependency service tests pass; live PostgreSQL/API/UI verification remains environment-dependent |

The automated checks confirm the covered code paths; they do not by themselves prove production readiness or every runtime integration. In the current development environment, `npm run build` passes and targeted linting for the dependency UI passes. Repository-wide frontend lint still reports pre-existing issues in other modified files. A live PostgreSQL startup, authenticated browser walkthrough, and live Ollama request must be performed separately before describing those integrations as production-verified.

The application uses the existing `spring.jpa.hibernate.ddl-auto: update` configuration to add anomaly and service-dependency columns/tables when the backend starts. Review this behavior before using automatic schema updates in production.

---

## 🚀 Upcoming Features

* 🔔 **Smart Webhook Notifications**: Integration with Slack, Discord, Microsoft Teams, and Email alerts.
* 📱 **Mobile App & PWA**: Responsive PWA interface for mobile and tablet monitoring.
* 📈 **Exportable PDF/CSV Reports**: Scheduled weekly/monthly SLA and performance export reports.
* 🔒 **Enhanced Security**: Two-Factor Authentication (2FA), OAuth2, and Refresh Tokens.

---

## 💡 Why Sentinel?

* ✔ **Privacy-Focused AI**: AI incident analysis runs locally using Ollama.
* ✔ **Automated Monitoring**: Scheduled background endpoint health checks.
* ✔ **Comprehensive Visibility**: Projects, services, endpoints, metrics, and alerts in one platform.
* ✔ **Performance Insights**: Response-time tracking and slow API detection.
* ✔ **Modern Developer-Centric UI**: Interactive React and Tailwind dashboard.
* ✔ **Modular Architecture**: Spring Boot backend organized into controllers, services, repositories, entities, and scheduled monitoring components.

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.

2. Create your feature branch:

   ```bash
   git checkout -b feature/AmazingFeature
   ```

3. Commit your changes:

   ```bash
   git commit -m 'Add some AmazingFeature'
   ```

4. Push to the branch:

   ```bash
   git push origin feature/AmazingFeature
   ```

5. Open a Pull Request.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👩‍💻 Author

**Sonakshi Chowgule**
*Computer Science Engineering Student*
Passionate about Full-Stack Engineering, Distributed Systems, and AI.

---

## ⭐ Support

If you found Sentinel API useful, please consider giving this repository a ⭐ on GitHub!

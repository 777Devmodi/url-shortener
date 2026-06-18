# URL Shortener Platform

A production-grade URL shortener similar to Bitly, built with **Spring Boot 3**, **PostgreSQL** / **MySQL**, **Redis**, **JWT authentication**, **Redis Streams** for analytics, **Gemini AI** for auto-tagging & phishing detection, and fully containerized with **Docker**.

## Features

- **User Authentication** – Signup, login, JWT access & refresh tokens with rotation.
- **URL Shortening** – Base62-encoded short codes, custom alias support, TTL expiration.
- **Redirect Service** – Fast 302 redirects with Redis caching (Cache‑Aside, 30 min TTL).
- **Analytics Pipeline** – Asynchronous click event processing via Redis Streams.
- **Rate Limiting** – Token bucket algorithm using Redis and Lua scripting.
- **AI Integration** – Gemini API for URL auto‑tagging and phishing detection.
- **Observability** – Structured JSON logging, correlation IDs, Actuator health & metrics.
- **Security** – Spring Security, stateless JWT, rate limiting, CSRF disabled.
- **Containerization** – Multi‑stage Docker build, Docker Compose for full local stack.

## Tech Stack

| Area           | Technology                                                                 |
|----------------|----------------------------------------------------------------------------|
| Backend        | Java 21, Spring Boot 3, Spring Data JPA, Spring Security, Spring Actuator |
| Database       | MySQL (also supports PostgreSQL)                                           |
| Cache / Stream | Redis                                                                      |
| AI / ML        | Google Gemini API                                                          |
| Monitoring     | Micrometer, Prometheus endpoint, JSON logs via Logstash Logback Encoder   |
| CI/CD          | GitHub Actions                                                             |
| Container      | Docker, Docker Compose                                                     |

## Getting Started

### Prerequisites

- Java 21
- Maven
- MySQL (or PostgreSQL) running locally
- Redis running locally
- Gemini API key (optional, for AI features)

### Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/url-shortener.git
   cd url-shortener/backend
    ```

Configure database and Redis in src/main/resources/application.properties:

properties
spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=yourpassword

spring.data.redis.host=localhost
spring.data.redis.port=6379

# Optional: AI features
gemini.api.key=YOUR_KEY
Create the database:

sql
CREATE DATABASE url_shortener;
Run the application:

bash
mvn spring-boot:run
The API will be available at http://localhost:8080.

Docker (Full Stack)
Run the entire stack (app + MySQL + Redis) with a single command:

bash
docker compose up --build
The app will be available at http://localhost:8080.
To stop and remove all containers:

bash
docker compose down -v
API Endpoints
Authentication (public)
POST /api/auth/signup – Register new user

POST /api/auth/login – Get access & refresh tokens

POST /api/auth/refresh – Refresh token rotation

POST /api/auth/logout – Revoke refresh token

URL Shortener
POST /api/urls – Create short URL (authenticated or anonymous)

GET /api/{shortCode} – Redirect to original URL (public)

Monitoring (requires authentication)
GET /actuator/health – Health check (DB, Redis)

GET /actuator/metrics – Micrometer metrics

GET /actuator/prometheus – Prometheus scrape endpoint

CI/CD with GitHub Actions
Every push to main and every pull request triggers an automated pipeline defined in .github/workflows/ci.yml.

Pipeline Steps
Checkout the repository.

Set up JDK 21 (Temurin) and cache Maven dependencies.

Start services – MySQL 8.0 and Redis 7 containers.

Run tests – mvn verify with integration tests against the live DB and Redis.

Build Docker image – ensures the image builds successfully.

Optional: Push to Docker Hub
To enable automatic image pushing, uncomment the corresponding steps in the workflow and set these secrets in your GitHub repository (Settings → Secrets and variables → Actions):

DOCKERHUB_USERNAME – your Docker Hub username

DOCKERHUB_TOKEN – a Docker Hub access token (not your password)

Workflow Status
Once the CI workflow runs, add the badge:

markdown

Project Structure
text
url-shortener/
├── backend/                         # Spring Boot application
│   ├── src/main/java/com/urlshortener/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── security/
│   │   ├── config/
│   │   ├── analytics/
│   │   ├── cache/
│   │   ├── rate_limit/
│   │   ├── ai/
│   │   └── exception/
│   └── src/main/resources/
│       ├── application.properties
│       └── logback-spring.xml
├── docker/
│   └── Dockerfile
├── docker-compose.yml
├── .github/workflows/ci.yml
└── README.md
System Design & Interview Prep
This project is built to answer common system design interview questions. Key points:

Database: Indexes on short_code, UUID primary keys, denormalized click count.

Caching: Cache‑Aside pattern with Redis, TTL 30 min, LRU eviction discussion.

Rate Limiting: Token Bucket with Redis Lua scripts, comparison with Sliding Window.

Analytics: Redis Streams for async click processing, consumer groups for reliability.

Security: JWT with separate access/refresh secrets, refresh token rotation, revocation.

Scalability: Stateless app servers, Redis for shared state, DB can be sharded on short_code.

Tradeoffs: Eventual consistency for click counts, 302 (not 301) for redirects to enable counting.

Thank you for reviewing this project. Feedback and contributions are welcome!


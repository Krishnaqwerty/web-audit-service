# Web Audit Service

> **Enterprise-Grade URL Auditing REST API built with Java 21 LTS and Spring Boot 3.4+**

The **Web Audit Service** is a high-throughput, production-grade URL auditing REST API designed according to **Clean / Hexagonal Architecture** principles. It features defense-in-depth Server-Side Request Forgery (SSRF) security, Java 21 Virtual Threads (Project Loom) for non-blocking I/O, Caffeine L1 caching, Bucket4j token-bucket rate limiting, Resilience4j circuit breakers, Micrometer Prometheus metrics, and multi-stage Docker deployment.

---

## Live Deployment & Container Operations

The application is fully containerized and pre-configured for automated **Docker-based deployment on Render** via `render.yaml` Blueprint.

- **Live Application URL**: `https://web-audit-service.onrender.com/`
- **Interactive Swagger UI**: `https://web-audit-service.onrender.com/swagger-ui/index.html`
- **Health Check Endpoint**: `https://web-audit-service.onrender.com/actuator/health`
- **Prometheus Metrics**: `https://web-audit-service.onrender.com/actuator/prometheus`

### Docker Container Commands

#### 1. Build Local Docker Image
```bash
docker build -t web-audit-service .
```

#### 2. Run Local Docker Container
```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name web-audit-service \
  web-audit-service
```

#### 3. Run with Docker Compose
```bash
docker-compose up --build -d
```

### Render Blueprint Deployment Instructions
1. Push this repository to GitHub.
2. Log in to [Render Dashboard](https://dashboard.render.com/) and click **New +** -> **Blueprint**.
3. Connect your repository. Render automatically reads `render.yaml`, detects `runtime: docker`, and builds the image using `./Dockerfile`.
4. Subsequent pushes to `main` trigger zero-downtime automated redeployments.

---

## Key Features

- **Clean / Hexagonal Architecture**: Enforces strict boundaries between domain models, application use cases, and infrastructure adapters.
- **Java 21 Virtual Threads**: Ultra-high concurrency model for I/O-bound web audits (`spring.threads.virtual.enabled=true`).
- **Defense-in-Depth SSRF Security**: Restricts URL schemes to `http`/`https` and performs pre-flight IP resolution checks against loopback (`127.0.0.0/8`), private IPv4 CIDRs (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), link-local (`169.254.0.0/16`), and multicast targets.
- **Caffeine In-Memory Caching**: Caches audit results by normalized URL (`UrlUtils.normalizeUrl`). Returns `cached=true` on cache hits and supports `forceRefresh=true` cache bypass.
- **Token Bucket Rate Limiting**: Per-client IP rate limiting powered by Bucket4j, returning `429 Too Many Requests` with a `Retry-After: 60` HTTP header.
- **Outbound Concurrency Protection**: Java `Semaphore` bulkhead protecting system sockets from downstream saturation.
- **Resilience4j Circuit Breaker & Retry**: Automatic exponential backoff retries and circuit breaker isolation for unstable external targets.
- **Observability & Prometheus Metrics**: Custom Micrometer counters, gauges, and timers for latency, cache ratios, timeouts, status codes, and active requests at `/actuator/prometheus`.
- **Interactive Swagger UI**: Auto-generated OpenAPI v3 documentation accessible at `/swagger-ui/index.html`.
- **Standardized RFC 7807 Errors**: Machine-readable error payloads without stack trace leakage.

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Java 21 LTS** | Core Programming Language & Virtual Threads (Project Loom) |
| **Spring Boot 3.4.2** | Framework Ecosystem & Dependency Injection |
| **Spring WebFlux (`WebClient`)** | Non-blocking HTTP Client with Netty Connection Pooling |
| **Jsoup (1.18.3)** | Resilient HTML Parsing & Metadata Extraction |
| **Caffeine Cache** | L1 High-Performance In-Memory Cache |
| **Bucket4j (8.10.1)** | Client IP Token-Bucket Rate Limiter |
| **Resilience4j (2.2.0)** | Circuit Breaker, Exponential Backoff Retry, & Bulkhead |
| **Springdoc OpenAPI (2.8.4)** | Swagger UI & OpenAPI v3 Specification |
| **Micrometer & Prometheus** | Service Observability & Operational Metrics |
| **JUnit 5, Mockito & MockWebServer** | Automated Unit, Integration, and Network Test Suite |
| **Docker & Docker Compose** | Multi-Stage Containerization & Orchestration |

---

## High-Level Architecture

```
+---------------------------------------------------------------------------------------------------+
| Web Audit Service (Spring Boot 3.4+ / Java 21)                                                    |
|                                                                                                   |
|  +---------------------------------------------------------------------------------------------+  |
|  | INBOUND ADAPTERS                                                                            |  |
|  |  - RootController (`GET /`)                                                                 |  |
|  |  - AuditController (`POST /api/v1/audits`, `GET /api/v1/audits/{id}`)                        |  |
|  |  - GlobalExceptionHandler (RFC 7807 Error Response)                                          |  |
|  |  - CorrelationIdFilter (MDC `X-Request-ID` Tracing)                                          |  |
|  +---------------------------------------------------------------------------------------------+  |
|                                             |                                                     |
|                                             v                                                     |
|  +---------------------------------------------------------------------------------------------+  |
|  | APPLICATION USE CASES & SERVICES                                                            |  |
|  |  - AuditService (Workflow Orchestration)                                                    |  |
|  |  - UrlValidationService (SSRF & Scheme Enforcement)                                         |  |
|  |  - RateLimitationService (Bucket4j IP Enforcement)                                         |  |
|  |  - ConcurrencyControlService (Semaphore Bulkhead)                                           |  |
|  |  - MetricsService (Micrometer Observability)                                                |  |
|  +---------------------------------------------------------------------------------------------+  |
|                                             |                                                     |
|                                             v                                                     |
|  +---------------------------------------------------------------------------------------------+  |
|  | OUTBOUND ADAPTERS & INFRASTRUCTURE                                                          |  |
|  |  - WebClient / ResilientWebClient (Reactor Netty Connection Pool)                           |  |
|  |  - CaffeineCacheManager (Normalized URL Keying)                                             |  |
|  |  - PageParserService (Jsoup HTML Title Extractor)                                           |  |
|  +---------------------------------------------------------------------------------------------+  |
+---------------------------------------------------------------------------------------------------+
```

---

## API Contract

### Root Service Info
`GET /`

#### Response: `200 OK`
```json
{
  "service": "Web Audit Service",
  "version": "0.1.0-SNAPSHOT",
  "status": "UP",
  "profile": "prod",
  "documentation": "/swagger-ui/index.html",
  "health": "/actuator/health"
}
```

### Submit URL Audit
`POST /api/v1/audits`

#### Request Payload
```json
{
  "url": "https://example.com",
  "forceRefresh": false
}
```

#### Response: `201 Created`
```json
{
  "id": "aud_9f8a7b6c5d4e3f2a",
  "url": "https://example.com",
  "status": "COMPLETED",
  "httpStatusCode": 200,
  "pageTitle": "Example Domain",
  "responseTimeMs": 142,
  "finalRedirectedUrl": "https://example.com/",
  "contentType": "text/html; charset=UTF-8",
  "contentLengthBytes": 1256,
  "redirectCount": 0,
  "cached": false,
  "createdAt": "2026-07-25T23:00:00Z",
  "completedAt": "2026-07-25T23:00:00.142Z"
}
```

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

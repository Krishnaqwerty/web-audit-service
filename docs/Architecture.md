# Web Audit Service — Architecture Specification

## Executive Summary
The **Web Audit Service** is an enterprise-grade, high-throughput RESTful web auditing platform built on **Java 21** and **Spring Boot 3.4+**. It allows clients to submit URLs for security, metadata, performance, and compliance auditing while safeguarding internal network infrastructure against Server-Side Request Forgery (SSRF) and malicious target behavior.

---

## Architectural Guiding Principles

1. **Clean / Hexagonal Architecture (Ports and Adapters)**: Strict boundary enforcement between core domain logic and external infrastructure (HTTP clients, caches, web controllers).
2. **SOLID Design**: Single responsibility services, open for extension via strategy interfaces, dependency inversion through Spring container context.
3. **Non-Blocking & Virtual Threads**: Leveraging Java 21 Virtual Threads (Project Loom) to achieve high concurrency for I/O-bound HTTP fetch operations.
4. **Defense-in-Depth Security**: Strict URL validation, DNS resolution checks, private IP range blocking (SSRF prevention), timeout enforcement, and response body size limits.
5. **High Observability**: End-to-end request tracing via MDC correlation IDs (`X-Correlation-ID`), Micrometer Prometheus metrics, and Spring Boot Actuator health indicators.

---

## High-Level Component Diagram

```
                                  +---------------------------------------+
                                  |            HTTP Client                |
                                  |     (REST Consumer / Frontend)        |
                                  +---------------------------------------+
                                                      |
                                           REST API (HTTP/JSON)
                                                      |
                                                      v
+---------------------------------------------------------------------------------------------------+
| Web Audit Service (Spring Boot 3.4+ / Java 21)                                                   |
|                                                                                                   |
|  +---------------------------------------------------------------------------------------------+  |
|  | INBOUND ADAPTERS                                                                            |  |
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

## Data & Workflow Execution Sequences

### 1. Primary Request Flow
```
Client                CorrelationFilter     AuditController     RateLimiter     Validator     CaffeineCache     WebClient
  |                           |                    |                 |              |               |               |
  |--- POST /api/v1/audits -->|                    |                 |              |               |               |
  |                           |-- Set MDC Req ID ->|                 |              |               |               |
  |                           |                    |-- Check Limit ->|              |               |               |
  |                           |                    |                 |-- Pass ----->|               |               |
  |                           |                    |                    Validate -->|               |               |
  |                           |                    |                                |-- Pass ------>|               |
  |                           |                    |                                   Cache Miss ->|               |
  |                           |                    |                                                |-- Fetch URI ->|
  |                           |                    |                                                |<-- 200 HTML --|
  |                           |                    |                                   Store Cache <|               |
  |                           |<-- 201 Created ----|<-----------------------------------------------|               |
  |<-- Response + X-Req-ID ---|
```

### 2. Cache Hit Flow
```
Client                AuditController     CaffeineCache
  |                          |                  |
  |--- POST /api/v1/audits ->|                  |
  |                          |-- Check Cache -->|
  |                          |<-- Hit (200) ----|
  |<-- 201 Created (cached=true) ---------------|
```

### 3. Failure & Resiliency Flow
```
Client                AuditController     RateLimiter / Semaphore     Target Web Server
  |                          |                      |                        |
  |--- POST /api/v1/audits ->|                      |                        |
  |                          |-- Check Capacity --->|                        |
  |                          |<-- Limit Exceeded ---|                        |
  |<-- 429 / 503 Error ------| (Retry-After: 60)                              |
  |                          |                                               |
  |--- POST /api/v1/audits ->|---------------------------------------------->| (Timeout 5000ms)
  |<-- 504 Gateway Timeout --|<----------------------------------------------|
```

---

## Deployment Architecture

```
                                +---------------------------+
                                |  Load Balancer / Ingress  |
                                +---------------------------+
                                              |
                                     HTTP Traffic (Port 8080)
                                              |
                                              v
+---------------------------------------------------------------------------------------------------+
| Container Instance (Docker / Temurin JRE 21 Runtime)                                              |
|                                                                                                   |
|  +-------------------------------------+      +------------------------------------------------+  |
|  | Web Audit Service (Port 8080)       |      | Actuator Prometheus Metrics Endpoint           |  |
|  | - Virtual Threads (Project Loom)    |      | - GET /actuator/prometheus                     |  |
|  | - Caffeine L1 Memory Cache          |      | - GET /actuator/health/readiness               |  |
|  +-------------------------------------+      +------------------------------------------------+  |
+---------------------------------------------------------------------------------------------------+
```

---

## Technology Decisions & Tradeoffs

| Component | Technology | Rationale | Alternatives Considered | Tradeoff |
|-----------|------------|-----------|-------------------------|----------|
| **Architecture** | Clean / Hexagonal | Decouples business logic from HTTP clients & Spring Web | Layered Architecture | Higher upfront DTO/Mapper boilerplate |
| **Concurrency** | Java 21 Virtual Threads | Ultra-high I/O throughput without reactive complexity | Spring WebFlux / Reactor | Must avoid pinning synchronized blocks |
| **Security Guard** | Pre-flight IP Resolution | Blocks private CIDRs, loopbacks & metadata endpoints | Regex URL matching | Extra DNS lookup overhead per unique domain |
| **Caching** | Caffeine L1 Cache | High-speed JVM memory cache with TTL eviction | Redis / Remote Cache | Cache cleared on service restart |
| **Rate Limiter** | Bucket4j Token Bucket | Fine-grained per-IP request throttling | API Gateway rate limiter | Tracked IP buckets consume small memory |
| **Resilience** | Resilience4j Circuit Breaker | Automatic retry with exponential backoff & isolation | Standard timeouts only | Requires fallback strategy definition |

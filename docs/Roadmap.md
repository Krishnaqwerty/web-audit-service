# Web Audit Service — Implementation Roadmap

This document outlines the structured, multi-phase implementation roadmap for building the production-grade **Web Audit Service**.

---

## Phase Overview & Status

| Phase | Description | Status | Target Milestone |
|-------|-------------|--------|------------------|
| **Phase 1** | Project Bootstrap & Hexagonal Infrastructure Setup | 🟢 Completed | Foundation & Build Pipeline |
| **Phase 2** | Security Engine, Core URL Auditing & OpenAPI | 🟢 Completed | Core Business Logic & Caching |
| **Phase 3** | Resilience4j, Micrometer Metrics, Docker & CI | 🟢 Completed | Production Hardening & Ops |
| **Phase 4** | Deep Metadata Extraction & Security Header Audit | 🟡 Next | Metadata & Scoring Engine |
| **Phase 5** | Distributed Redis L2 Caching & Event Bus | 🔴 Planned | Distributed Scalability |
| **Phase 6** | E2E Load & Chaos Verification Suite | 🔴 Planned | Quality Assurance |
| **Phase 7** | Multi-Region Deployment & Helm Packaging | 🔴 Planned | Enterprise Release |

---

## Phase Details

### Phase 1: Project Bootstrap & Hexagonal Infrastructure Setup (COMPLETED)
- Initialized Java 21 & Spring Boot 3.4.2 Maven project dependencies (`pom.xml`).
- Configured clean package structure and dual profiles (`application-dev.yml`, `application-prod.yml`).
- Implemented `CorrelationIdFilter` for MDC request correlation ID tracing.
- Created `@ValidUrl` validator and `IpUtils` SSRF guard blocking loopback and private IPv4 CIDRs.

### Phase 2: Security Engine, Core URL Auditing & OpenAPI (COMPLETED)
- Implemented `POST /api/v1/audits` fetching target websites and extracting HTTP status, page title (Jsoup), response duration (ms), Content-Type, Content-Length, timestamp, and `cached` status.
- Caffeine caching by normalized URL (`UrlUtils.normalizeUrl`), setting `cached=true` on cache hits.
- Concurrency control via Java `Semaphore` (`audit.concurrency.max-concurrent-audits=50`).
- Bucket4j IP rate limiting with `Retry-After: 60` HTTP header.
- Springdoc OpenAPI Swagger documentation (`/swagger-ui.html` and `/v3/api-docs`).

### Phase 3: Resilience4j, Micrometer Metrics, Docker & CI (COMPLETED)
- Integrated `resilience4j-spring-boot3` (circuit breaker & exponential backoff retries).
- Created `MetricsService` recording request latency, cache hits/misses, timeouts, status code distribution, rate limits, and active requests. Exposed `/actuator/prometheus`.
- Created multi-container `docker-compose.yml` with container healthchecks and resource limits.
- Formulated GitHub Actions CI workflow (`.github/workflows/ci.yml`).
- Authored production [README.md](file:///Users/krishna/Desktop/audit/web-audit-service/README.md) and deployment guides (`docs/Deployment.md`).
- Executed Senior Staff Engineer Code Review & Tech Debt Analysis.

### Phase 4: Deep Metadata Extraction & Security Header Audit (NEXT)
- Implement OpenGraph (`og:title`, `og:image`, `og:description`), Twitter Card metadata, and canonical URL extraction.
- Implement Security Header analysis (`HSTS`, `CSP`, `X-Frame-Options`, `X-Content-Type-Options`).
- Calculate audit scores (SEO Score, Security Score, Overall Score).

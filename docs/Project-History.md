# Web Audit Service — Project History & Changelog

All notable changes, architectural milestone completions, refactorings, security enhancements, and breaking changes to the **Web Audit Service** project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.3.0] - 2026-07-25

### Added - Phase 3 Production Hardening, Observability & Ops
- **Resilience4j Integration**: Added `resilience4j-spring-boot3` (2.2.0) and `spring-boot-starter-aop` for circuit breaking, exponential backoff retries, and bulkhead protection.
- **Micrometer & Prometheus Metrics**:
  - Implemented `MetricsService` and `MetricsServiceImpl` registering custom counters, timers, and gauges (`webaudit.requests.latency`, `webaudit.cache.hits`, `webaudit.cache.misses`, `webaudit.failures.timeout`, `webaudit.failures.external`, `webaudit.requests.status`, `webaudit.ratelimit.violations`, `webaudit.requests.active`).
  - Exposed Prometheus scraping endpoint at `/actuator/prometheus`.
- **Docker Compose**: Authored production-ready `docker-compose.yml` with container healthchecks, resource limits (1GB RAM / 2 vCPUs), and environment variable injection.
- **GitHub Actions CI Pipeline**: Created `.github/workflows/ci.yml` running JDK 21 automated Maven builds (`mvn clean verify`) and test suites on every push and pull request.
- **Master Production README**: Authored comprehensive root `README.md` covering Architecture, Features, API Contracts, Profiles, Docker, Local running, Cloud Deployment (Render, Railway), Testing, Rate Limiting, Caching, Logging, and Observability.
- **Deployment & Failure Analysis Guide**: Enhanced `docs/Deployment.md` with cloud platform guides, Blue-Green deployments, Rollback protocols, and Failure Analysis matrices.
- **Senior Engineer Code Review**: Conducted code review identifying code smells, security considerations, performance bottlenecks, technical debt, and prioritized backlog.

---

## [0.2.0] - 2026-07-25

### Added - Phase 2 Core URL Audit Engine
- URL Audit Core (`POST /api/v1/audits`), Jsoup title parser, Caffeine cache normalized keying, Bucket4j `Retry-After: 60` headers, Springdoc OpenAPI Swagger UI (`/swagger-ui.html`), and MockWebServer tests.

---

## [0.1.0] - 2026-07-25

### Added - Phase 1 Foundation
- Maven build configuration (`pom.xml`), dual Spring profiles, `@ConfigurationProperties`, Clean Architecture packages, `CorrelationIdFilter`, `@ValidUrl` validator, and Actuator probes.

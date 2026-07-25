# Changelog

All notable changes to the **Web Audit Service** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [v1.0.0] - 2026-07-26

### Added

- Production-grade URL audit API (`POST /api/v1/audits` and `GET /`)
- Defensive URL & SSRF validation (blocking loopback, link-local, and private IPv4 CIDRs)
- Non-blocking HTTP auditing via Spring WebFlux `WebClient` with Netty connection pooling
- Configurable Caffeine L1 in-memory caching keyed by normalized URL
- Token-bucket client IP rate limiting via Bucket4j with `429 Too Many Requests` status and `Retry-After` headers
- Concurrency controls via Java `Semaphore` bulkhead protecting system sockets
- Resilience4j circuit breakers and exponential backoff retries
- Structured RFC 7807 problem details error handling without stack trace leakage
- End-to-end MDC request tracing (`X-Request-ID` / `X-Correlation-ID`)
- OpenAPI v3 Swagger UI documentation accessible at `/swagger-ui/index.html`
- Spring Boot Actuator liveness and readiness health probes
- Micrometer Prometheus operational metrics exposed at `/actuator/prometheus`
- Multi-stage `Dockerfile` and `docker-compose.yml` container orchestration
- Render Native Java 21 automated deployment Blueprint (`render.yaml`)
- GitHub Actions CI workflow running automated JDK 21 Maven builds (`mvn clean verify`)
- Comprehensive JUnit 5, Mockito, and OkHttp `MockWebServer` test suite (27 tests passing)
- Dual environment profiles (`application-dev.yml`, `application-prod.yml`)
- Complete engineering documentation suite (Architecture, API, Deployment, Failure Analysis, Observability, Rollback Strategy, Decision Log, AI Usage)

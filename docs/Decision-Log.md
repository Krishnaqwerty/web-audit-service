# Web Audit Service — Decision Log (Architecture Decision Records)

This document records the key architectural decisions, rationale, alternatives considered, and tradeoffs made throughout the lifecycle of the **Web Audit Service**.

---

## ADR-001: Adoption of Clean / Hexagonal Architecture (Ports and Adapters)
* **Date**: 2026-07-25 | **Status**: Accepted

## ADR-002: Java 21 & Spring Boot 3.x with Virtual Threads (Project Loom)
* **Date**: 2026-07-25 | **Status**: Accepted

## ADR-003: SSRF Prevention via DNS Pinning & IP Range Guarding
* **Date**: 2026-07-25 | **Status**: Accepted

## ADR-004: In-Memory Multi-Level Caching with Caffeine
* **Date**: 2026-07-25 | **Status**: Accepted

## ADR-005: Token Bucket Rate Limiting & Circuit Breaking
* **Date**: 2026-07-25 | **Status**: Accepted

## ADR-006: Standardized RFC 7807 Problem Details Error Representation
* **Date**: 2026-07-25 | **Status**: Accepted

---

## ADR-007: Outbound Concurrency Control via Configurable Java Semaphore

* **Date**: 2026-07-25
* **Status**: Accepted
* **Context**: Unbounded concurrent outbound WebClient HTTP connections can overwhelm local OS sockets or saturate network bandwidth during traffic spikes.
* **Decision**: Implement a thread-safe `ConcurrencyControlService` backed by a Java `Semaphore` initialized with `audit.concurrency.max-concurrent-audits=50`.
* **Reason**: Limits total active outbound audit requests in flight, guaranteeing system resource bounds. Rejects excessive requests with `ConcurrencyLimitExceededException` (HTTP 503).
* **Alternatives Considered**:
  - *Unbounded WebClient connections*: Risks socket exhaustion or thread starvation.
  - *Blocking Servlet queue*: Degrades API latency under heavy load.
* **Tradeoffs**:
  - *Positive*: Strict resource bounds, fast fail under heavy load.
  - *Negative*: Rejects requests if outbound capacity is exhausted.

---

## ADR-008: OpenAPI / Swagger Integration with Springdoc

* **Date**: 2026-07-25
* **Status**: Accepted
* **Context**: API consumers and frontend integration teams require interactive API documentation and schema specifications.
* **Decision**: Adopt `springdoc-openapi-starter-webmvc-ui` (2.8.4) to auto-generate OpenAPI v3 specs and render Swagger UI at `/swagger-ui.html`.
* **Reason**: Seamless Spring Boot 3 integration without manual Swagger YAML maintenance.
* **Alternatives Considered**:
  - *Manual OpenAPI YAML spec file*: Difficult to keep synchronized with Java DTOs.
* **Tradeoffs**:
  - *Positive*: Auto-generated interactive UI, standard OpenAPI contract.
  - *Negative*: Adds ~3MB to final JAR artifact size.

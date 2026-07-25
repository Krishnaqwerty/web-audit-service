# Web Audit Service — Observability & Monitoring Specification

## 1. Metrics Infrastructure (Micrometer & Prometheus)

Prometheus metrics are exposed at `GET /actuator/prometheus`. Key operational metrics include:

- `webaudit_requests_latency_seconds`: Timer recording execution duration of URL audits.
- `webaudit_cache_hits_total`: Counter tracking Caffeine L1 cache hits.
- `webaudit_cache_misses_total`: Counter tracking Caffeine L1 cache misses.
- `webaudit_failures_timeout_total`: Counter tracking outbound HTTP request timeouts.
- `webaudit_failures_external_total`: Counter tracking target network connection errors.
- `webaudit_requests_status_total{status="200"}`: Distribution counter of HTTP response codes.
- `webaudit_ratelimit_violations_total`: Counter tracking Bucket4j rate limit rejections.
- `webaudit_requests_active`: AtomicInteger gauge tracking currently active in-flight audit requests.

---

## 2. Request Correlation & Distributed Tracing

- **Filter**: `CorrelationIdFilter` captures client-provided `X-Request-ID` or `X-Correlation-ID` headers or generates a random UUID.
- **MDC Propagation**: Injects `requestId` into SLF4J MDC context.
- **Structured Log Format**: All logs include `[req_id=...]` for cross-system correlation.
- **OpenTelemetry Readiness**: Standardized MDC layout ensures seamless future integration with OpenTelemetry agents and Jaeger / Zipkin tracing collectors.

---

## 3. Health Monitoring & Probes

Spring Boot Actuator health endpoint (`GET /actuator/health`) exposes:
- **Liveness Probe**: `GET /actuator/health/liveness`
- **Readiness Probe**: `GET /actuator/health/readiness`
- **Custom Details**: `CustomHealthIndicator` includes virtual threads state, SSRF security status, and application build metadata.

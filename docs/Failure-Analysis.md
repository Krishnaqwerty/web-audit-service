# Web Audit Service — Failure Analysis & Mitigation Matrix

## Failure Scenarios & Operational Mitigations

| Failure Scenario | Root Cause | Impact | Automated Mitigation | Manual Resolution / Action |
|------------------|------------|--------|----------------------|----------------------------|
| **Target Website Unavailable** | Downstream server failure (500/502/503) | Outbound fetch fails | Resilience4j Circuit Breaker opens after 50% failure rate; returns structured RFC 7807 error payload. | None. Auto-recovers when target website resumes operation. |
| **Outbound Fetch Timeout** | Slow target web server | Worker thread blocked | `WebClient` request times out after `5000ms`, throwing `TargetTimeoutException`. | Adjust `HTTP_READ_TIMEOUT_MS` if auditing slow sites. |
| **Traffic Burst / DoS Attack** | High volume of inbound requests | Potential socket exhaustion | Bucket4j token bucket throttles requests, returning `429 Too Many Requests` with `Retry-After: 60`. | Scale service instances or increase `RATE_LIMIT_RPM`. |
| **Outbound Connection Exhaustion** | Concurrent audit requests exceed threshold | High latency or connection drops | `ConcurrencyControlService` semaphore bulkhead limits active outbound audits to 50. | Adjust `audit.concurrency.max-concurrent-audits`. |
| **High Memory / Cache Pressure** | Large volume of distinct URLs audited | Memory usage increases | Caffeine automatically evicts least recently used (LRU) cache items upon reaching capacity. | Increase `CACHE_MAX_SIZE` or allocate higher JVM RAM. |
| **Cloud Deployment Failure** | Invalid configuration or broken build | Deployment aborts | Render / Container health checks fail on `/actuator/health`; deployment is rolled back automatically. | Inspect build logs via CI/CD pipeline or Render console. |

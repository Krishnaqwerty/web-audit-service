# Web Audit Service — Rollback Protocol & Strategy

## 1. Automated Rollback Trigger Criteria

A deployment rollback is automatically triggered if any of the following occur within 15 minutes of deployment:
- Health check endpoint (`/actuator/health/readiness`) fails to return HTTP 200 within 30 seconds of boot.
- HTTP 5xx error rate spikes above 2% of total traffic.
- P99 latency exceeds SLA threshold (> 3000ms).

---

## 2. Rollback Execution Steps

### Cloud Platform (Render)
1. Navigate to Render Dashboard -> Web Audit Service -> **Events**.
2. Locate the last known successful release commit tag (e.g. `v1.0.0`).
3. Click **Rollback to this revision**.
4. Render immediately switches traffic back to the prior application build.

### Docker Environment
```bash
# Rollback to specific tagged release image
docker-compose down
docker pull company/web-audit-service:v1.0.0
docker-compose up -d
```

---

## 3. Post-Rollback Diagnostics Protocol

1. Preserve failure logs using `docker logs web-audit-service > rollback_failure.log`.
2. Inspect Prometheus metrics at `/actuator/prometheus` to locate error spike origin.
3. Reproduce issue in `dev` environment profile.
4. File a P0 bug report and create a fix branch before re-triggering release pipeline.

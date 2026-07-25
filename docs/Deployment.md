# Web Audit Service — Deployment, Operations & Rollback Guide

## Overview
This document provides production deployment procedures, failure analysis matrices, rollback protocols, and cloud platform deployment guides (Render, Railway, Docker) for the **Web Audit Service**.

---

## 1. Cloud Deployment Guides

### A. Deployment on Render
1. Create a new **Web Service** on [Render](https://render.com).
2. Connect your GitHub repository.
3. Select **Docker** environment.
4. Set Environment Variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `PORT=8080`
   - `HTTP_CONNECT_TIMEOUT_MS=3000`
   - `HTTP_READ_TIMEOUT_MS=5000`
   - `RATE_LIMIT_RPM=60`
5. Set Health Check Path: `/actuator/health/readiness`.

### B. Deployment on Railway
1. Create a new project on [Railway](https://railway.app).
2. Select **Deploy from GitHub Repo**.
3. Railway automatically detects `Dockerfile`.
4. Configure Variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `PORT=8080`
5. Expose HTTP port `8080`.

### C. Container Deployment via Docker Compose
```bash
# Production Docker Compose release
docker-compose up --build -d

# Verify container health
docker-compose ps
```

---

## 2. Failure Analysis & Mitigation Matrix

| Failure Scenario | Root Cause | Impact | Automated Mitigation Strategy | Manual Action Required |
|------------------|------------|--------|-------------------------------+------------------------|
| **External Website Unavailable** | Target server down (500/502/503) | Audit fetch fails | Resilience4j Circuit Breaker opens after 50% failures; returns structured error. | None. Auto-recovers when target restores. |
| **Cache Unavailable / Exceeded** | Memory pressure | Higher latency | Caffeine automatically evicts least recently used (LRU) items. | Increase `CACHE_MAX_SIZE` or allocate more RAM. |
| **Traffic Spike / DoS** | Burst of client requests | Potential socket exhaustion | Bucket4j returns `429 Too Many Requests` with `Retry-After: 60`. Semaphore blocks concurrency. | Scale horizontally or reduce `RATE_LIMIT_RPM`. |
| **Slow Upstream Server** | Target server delays response | Worker thread blocked | Outbound fetch times out after `5000ms` (`TargetTimeoutException`). | Adjust `HTTP_READ_TIMEOUT_MS` if required. |
| **Deployment Failure** | Broken container build or bad config | New instance fails healthcheck | Container platform aborts deployment; traffic remains on old container. | Inspect container logs via `docker logs`. |

---

## 3. Blue-Green Deployment & Rollback Protocol

### Blue-Green Strategy
1. Deploy new version (Green) alongside running version (Blue).
2. Execute automated health checks against Green `/actuator/health/readiness`.
3. Switch Load Balancer / Ingress traffic from Blue to Green.
4. Monitor Prometheus metrics (`webaudit.requests.status`, `webaudit.failures.external`) for 15 minutes.
5. Decommission Blue instance upon successful verification.

### Automated Rollback Procedure
If Green health check fails or error rate spikes > 5%:
1. Immediately revert Load Balancer traffic back to Blue container instance.
2. Freeze deployment pipeline.
3. Fetch application logs: `docker logs web-audit-service`.
4. Identify root cause, apply fix in feature branch, run `./mvnw test`, and re-trigger deployment.

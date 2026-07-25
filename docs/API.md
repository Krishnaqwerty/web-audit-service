# Web Audit Service — REST API Specification

## Base URL
`/api/v1`

---

## Authentication & Headers
- `Content-Type`: `application/json`
- `Accept`: `application/json`
- `X-Correlation-ID` *(Optional)*: Unique client request identifier (UUID). If omitted, the service generates one automatically and returns it in the response headers.

---

## Endpoints Summary

| Method | Endpoint | Description | Rate Limited |
|--------|----------|-------------|--------------|
| `POST` | `/api/v1/audits` | Submit a URL for auditing | Yes |
| `GET`  | `/api/v1/audits/{id}` | Retrieve an existing audit report by ID | No |
| `GET`  | `/api/v1/audits` | List recent audit reports (Paginated) | Yes |
| `GET`  | `/actuator/health` | Service health status probe | No |
| `GET`  | `/actuator/metrics` | Prometheus/Micrometer metrics | Restricted |

---

## 1. Submit Audit Request

### `POST /api/v1/audits`

Submits a target URL to be fetched, validated against SSRF rules, parsed for metadata/security headers, and evaluated.

#### Request Headers
```http
Content-Type: application/json
X-Correlation-ID: e6b1f23a-4c8d-4a11-8e9f-98123456789a
```

#### Request Body
```json
{
  "url": "https://example.com",
  "forceRefresh": false,
  "options": {
    "includePerformanceMetrics": true,
    "includeSecurityHeaders": true,
    "maxRedirects": 3
  }
}
```

#### Field Constraints
- `url` *(string, required)*: Valid absolute HTTP/HTTPS URL. Max length 2048 characters. Private/internal IPs are rejected with HTTP 400 (SSRF Violation).
- `forceRefresh` *(boolean, optional, default: false)*: Bypasses L1 in-memory cache if true.
- `options` *(object, optional)*: Audit configuration overrides.

#### Response: HTTP 201 Created (Audit Completed)
```json
{
  "id": "aud_9f8a7b6c5d4e3f2a",
  "url": "https://example.com",
  "status": "COMPLETED",
  "overallScore": 92,
  "scores": {
    "seoScore": 95,
    "securityScore": 88,
    "performanceScore": 93
  },
  "metadata": {
    "title": "Example Domain",
    "description": "Example Domain is used for illustrative examples in documents.",
    "canonicalUrl": "https://example.com",
    "language": "en",
    "charset": "UTF-8",
    "openGraph": {
      "ogTitle": "Example Domain",
      "ogType": "website",
      "ogImage": null
    },
    "headingSummary": {
      "h1Count": 1,
      "h2Count": 0,
      "h3Count": 0
    }
  },
  "security": {
    "isHttps": true,
    "hstsEnabled": true,
    "contentSecurityPolicyPresent": false,
    "xFrameOptions": "DENY",
    "xContentTypeOptions": "nosniff",
    "referrerPolicy": "strict-origin-when-cross-origin"
  },
  "performance": {
    "responseTimeMs": 142,
    "contentSizeBytes": 1256,
    "httpStatusCode": 200,
    "redirectCount": 0
  },
  "cached": false,
  "createdAt": "2026-07-25T22:45:00Z",
  "completedAt": "2026-07-25T22:45:00.142Z"
}
```

---

## 2. Retrieve Audit Report

### `GET /api/v1/audits/{id}`

Retrieves an audit report by its unique ID (`aud_...`).

#### Response: HTTP 200 OK
Returns the full `AuditReport` JSON object (matching the response structure above).

#### Response: HTTP 404 Not Found
```json
{
  "type": "https://webaudit.com/errors/not-found",
  "title": "Audit Report Not Found",
  "status": 404,
  "detail": "Audit report with ID 'aud_invalid_id' was not found.",
  "instance": "/api/v1/audits/aud_invalid_id",
  "timestamp": "2026-07-25T22:45:10Z",
  "correlationId": "e6b1f23a-4c8d-4a11-8e9f-98123456789a"
}
```

---

## 3. Standard RFC 7807 Error Responses

All non-2xx API responses return standard RFC 7807 `ProblemDetail` payloads:

### Invalid URL Request (HTTP 400 Bad Request)
```json
{
  "type": "https://webaudit.com/errors/invalid-input",
  "title": "Invalid Audit Request",
  "status": 400,
  "detail": "URL 'ftp://example.com' is invalid. Only http and https schemes are permitted.",
  "instance": "/api/v1/audits",
  "timestamp": "2026-07-25T22:45:15Z",
  "correlationId": "e6b1f23a-4c8d-4a11-8e9f-98123456789a",
  "invalidParams": [
    {
      "name": "url",
      "reason": "Must use HTTP or HTTPS protocol"
    }
  ]
}
```

### SSRF Policy Violation (HTTP 400 Bad Request)
```json
{
  "type": "https://webaudit.com/errors/ssrf-violation",
  "title": "Security Policy Violation",
  "status": 400,
  "detail": "Target host resolves to a restricted private or loopback IP address (127.0.0.1).",
  "instance": "/api/v1/audits",
  "timestamp": "2026-07-25T22:45:20Z",
  "correlationId": "e6b1f23a-4c8d-4a11-8e9f-98123456789a"
}
```

### Rate Limit Exceeded (HTTP 429 Too Many Requests)
```json
{
  "type": "https://webaudit.com/errors/rate-limit-exceeded",
  "title": "Rate Limit Exceeded",
  "status": 429,
  "detail": "You have exceeded the maximum request limit of 60 requests per minute.",
  "instance": "/api/v1/audits",
  "timestamp": "2026-07-25T22:45:25Z",
  "correlationId": "e6b1f23a-4c8d-4a11-8e9f-98123456789a"
}
```

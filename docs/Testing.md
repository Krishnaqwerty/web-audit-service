# Web Audit Service — Testing Strategy & Guidelines

## Overview
Quality assurance in the **Web Audit Service** follows the **Test Pyramid** model: unit testing pure domain logic and value objects, integration testing Spring components with WireMock external target stubs, and end-to-end testing API endpoints.

---

## Code Coverage Goals
- **Overall Line Coverage**: >= 85%
- **Branch Coverage**: >= 80%
- **Domain & Application Security Modules**: 100% path coverage (SSRF rules, URI validation, rate limiting).

---

## Test Categories & Frameworks

| Test Type | Scope | Key Libraries | Execution Speed |
|-----------|-------|---------------|-----------------|
| **Unit Tests** | Domain entities, value objects, use case orchestrators | JUnit 5, AssertJ, Mockito | Extremely Fast (< 10ms/test) |
| **Security Tests** | SSRF rules, IP range blocking, input validation | JUnit 5, Custom InetAddress Mocks | Fast (< 50ms/test) |
| **Integration Tests** | REST Controllers, Spring Context, Caffeine Cache | `@SpringBootTest`, `WebTestClient` / `MockMvc` | Medium (< 2s/suite) |
| **External Stub Tests** | HTTP client outbound audit engine | WireMock 3.x | Medium (< 3s/suite) |

---

## 1. Unit Testing Guidelines

- **No Spring Context in Unit Tests**: Domain rules (`TargetUrl`, `UrlValidationDomainService`, metadata scorers) must be tested purely with standard JUnit 5 and AssertJ.
- **Parametrized Tests**: Use `@ParameterizedTest` and `@ValueSource` / `@CsvSource` to cover boundary cases for URL formats, IPv4/IPv6 addresses, and malformed HTML payloads.

### SSRF Protection Test Suite Matrix
Tests must assert rejection for:
- IPv4 Loopback: `http://127.0.0.1`, `http://127.0.0.2`
- Decimal IP Encodings: `http://2130706433` (evaluates to `127.0.0.1`)
- Hexadecimal IP Encodings: `http://0x7f000001`
- IPv6 Loopback & Unspecified: `http://[::1]`, `http://[::]`
- Private Class A/B/C Networks: `http://10.0.0.1`, `http://172.16.0.1`, `http://192.168.1.1`
- AWS Metadata Endpoint: `http://169.254.169.254/latest/meta-data/`
- Non-HTTP Protocols: `file:///etc/passwd`, `gopher://localhost:70`, `ftp://server/file`

---

## 2. External HTTP Service Stubbing with WireMock

Avoid making real network calls during integration testing. Use **WireMock** to simulate external web server behavior:

- **Success Scenario**: Stub WireMock to return HTTP 200 OK with full HTML containing `<title>`, `<meta description>`, OpenGraph tags, and security headers.
- **Slow Target Timeout**: Stub WireMock with a delayed response (`withFixedDelay(10000)`) to verify service connect/read timeouts work as configured.
- **Redirect Loops**: Stub WireMock to return HTTP 301 pointing to another URL up to 5 times to verify max redirect bounds.
- **Large Payload Bomb**: Stub WireMock to stream > 2 MB of HTML data to verify response body truncation and memory safety safeguards.
- **Downstream Server Error**: Stub WireMock to return HTTP 500 / 503 errors and verify graceful error report generation.

---

## 3. Running Automated Tests

```bash
# Run all unit and integration tests
./gradlew test

# Run security test suite specifically
./gradlew test --tests "*Ssrf*"

# Generate JaCoCo Code Coverage Report
./gradlew jacocoTestReport
```

View HTML coverage report at: `build/reports/jacoco/test/html/index.html`.

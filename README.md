# Sentinel Gateway

**Spring Cloud Gateway · Java 21 · Spring Boot · Reactive Security & Reliability**

Sentinel Gateway is a backend engineering project exploring the responsibilities of a centralized API gateway in a service-oriented architecture: request tracing, token validation, role-based authorization, traffic control, service discovery, and fault handling.

Built with **Spring Cloud Gateway (WebFlux)**, the project brings cross-cutting concerns into a dedicated gateway layer rather than duplicating them across downstream services.

> **Status:** Engineering project / development prototype. The repository contains working implementations and configuration for the capabilities described below; full integration testing, security hardening, and production deployment have **not** been established.

[**Portfolio**](https://www.syedharis.pro) · [**Sentinel Case Study**](https://www.syedharis.pro/work/sentinel-gateway) · [**Contact**](https://www.syedharis.pro/contact)

---

## 1. Architecture at a Glance

```text
                      Client / API Consumer
                               |
                               v
                      Sentinel Gateway
                     (Reactive WebFlux)
                               |
                     Correlation ID + Logs
                               |
                        JWT Validation
                               |
                    Rate Limit + Role Checks
                               |
                         Route Matching
                               |
             +-----------------+------------------+
             |                 |                  |
        Direct HTTP       Eureka / LB        Circuit Breaker
           Routes          Service Routes      + Fallback
             |                 |                  |
             +-----------------+------------------+
                               |
                       Downstream Services

                 Redis: per-user rate-limit counters
                 Actuator: operational endpoints
```

The diagram is a conceptual overview of the configured components; it is not a claim that every downstream service is deployed or connected.

## 2. Implemented Components

| Component | What the source implements |
| :--- | :--- |
| **Request correlation** | Generates a UUID per request and propagates `X-Correlation-ID` to the downstream request and response. |
| **Request logging** | Captures request timestamp, HTTP method, path, status, correlation ID, client-address information, and elapsed processing time. |
| **JWT validation** | Extracts a Bearer token, verifies a signed JWT using JJWT, handles expired/invalid tokens, and forwards identity claims in request headers. |
| **Role-based authorization** | Evaluates configured path patterns against `USER`, `MANAGER`, `ADMIN`, and `SUPER_ADMIN` roles; unmatched paths are denied by the configured default policy. |
| **Redis-backed rate limiting** | Uses reactive Redis counters with a configured expiration window. The current keying strategy is **per username**, not per route. |
| **Error response handling** | Maps authentication, authorization, and rate-limit exceptions to structured JSON responses with HTTP 401, 403, and 429 statuses. |
| **Service routing** | Configures static HTTP upstreams and Eureka/load-balanced service URIs. |
| **Resilience configuration** | Configures a Resilience4j circuit breaker for the product route, with a local service-unavailable fallback endpoint. |

The authorization and rate-limit filters exempt the explicitly configured public authentication paths. Other requests are subject to the gateway filters; behavior depends on the supplied token, route, and running dependencies.

## 3. Reactive Filter Pipeline

The gateway implements ordered global filters:

| Order | Filter | Responsibility |
| ---: | --- | --- |
| `-3` | `CorrelationIdFilter` | Attach and propagate a per-request correlation ID |
| `-2` | `RequestLoggingFilter` | Record request/response metadata and elapsed time |
| `-1` | `JwtAuthenticationFilter` | Validate Bearer JWT and attach user/role information |
| `0` | `RateLimitFilter` | Apply Redis-backed quota checks to authenticated requests |
| `1` | `RbacFilter` | Enforce configured role/path authorization rules |

The filters are implemented with Spring Cloud Gateway's reactive `GlobalFilter` and `GatewayFilterChain` abstractions.

## 4. Routes and Service Integration

Example mappings currently present in `application.yml`:

| Incoming path | Configured destination | Notes |
| --- | --- | --- |
| `/admin/**` | `http://localhost:8082` | Direct HTTP route |
| `/auth/**` | `lb://PRODUCT-SERVICE` | Eureka/load-balanced destination as currently configured |
| `/api/products/**` | `lb://PRODUCT-SERVICE` | Circuit breaker with `/fallback/products` fallback |
| `/api/orders/**` | `http://localhost:8083` | Direct HTTP route |

These are **development-oriented mappings**. They require the corresponding upstream services; the repository does not include the implementations of those services.

RBAC path rules are configured separately from routing rules. A rule for `/manager/**` does not by itself create a downstream route.

## 5. Technology Stack

| Area | Stack |
| :--- | :--- |
| Language | Java 21 |
| Application framework | Spring Boot 3.5.0 |
| Gateway | Spring Cloud Gateway Server (WebFlux) |
| Spring Cloud | 2025.0.0 release train |
| Authentication | JJWT 0.12.5 |
| Authorization | Configurable role/path rules |
| Traffic control | Reactive Spring Data Redis |
| Discovery and routing | Netflix Eureka Client, Spring Cloud LoadBalancer |
| Resilience | Resilience4j via Spring Cloud Circuit Breaker |
| Observability | SLF4J logging, Spring Boot Actuator |
| Build | Maven and Maven Wrapper |

## 6. Local Development

### Prerequisites

- JDK **21**
- Network access for Maven dependencies
- A Redis instance compatible with the reactive Redis client
- Downstream services for the routes you intend to exercise
- Eureka server for `lb://` service routes, if used

The configuration also references an **optional** Spring Cloud Config Server at `http://localhost:8888`.

### Configure secrets

`src/main/resources/application.yml` reads JWT and Redis credentials from environment variables:

```yaml
jwt:
  secret: ${JWT_SECRET}

spring:
  data:
    redis:
      password: ${REDIS_PASSWORD}
```

Set **JWT_SECRET** to an appropriately strong signing secret and **REDIS_PASSWORD** to the password for your own Redis instance. Do **not** commit either value. Update the Redis host and service addresses in local configuration for your environment.

In **PowerShell**, you can set environment variables for the current shell session (replace placeholders locally; do not commit real values):

```powershell
$env:JWT_SECRET = "<your-strong-JWT-signing-secret>"
$env:REDIS_PASSWORD = "<your-Redis-password>"
```

> Use secure secret storage for any environment outside a local development session. The repository does not include usable credentials or downstream service deployments.

### Build and run

On Windows:

```powershell
./mvnw.cmd clean package
./mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

The configured gateway port is **8080**. Successful startup and route availability depend on your external-service configuration.

### Tests

```powershell
./mvnw.cmd test
```

The repository currently includes a **Spring context-loading test**. It does not yet contain a comprehensive suite proving JWT/RBAC decisions, Redis quota behavior, downstream routing, or resilience under failure. Test execution results are not claimed here.

## 7. Project Structure

```text
src/
  main/
    java/com/example/sentinel_gateway/
      filter/       Correlation, logging, JWT, rate limit, RBAC
      service/      Token parsing, authorization, Redis quota checks
      properties/   Typed configuration bindings
      exception/    Domain errors and centralized error responses
      fallback/     Product-service fallback endpoint
      security/     Role definitions
    resources/
      application.yml
      application.properties
  test/
    java/com/example/sentinel_gateway/
      SentinelGatewayApplicationTests.java
```

## 8. Security and Production Readiness

This codebase is a **technical demonstration, not a production security guarantee**. Before any internet-facing deployment, important follow-up work includes:

- Restricting Actuator exposure and health-detail visibility. The current development configuration exposes all management endpoints and detailed health information.
- Reviewing trusted proxy/header handling and identity-header propagation at the gateway boundary.
- Adding isolated and integration-level tests for authentication, authorization, rate limiting, circuit breaking, and fallback behavior.
- Reviewing exception messages and logging for inadvertent disclosure of sensitive operational data.
- Validating all service routes, discovery integration, and secret provisioning for the deployment environment.

These are documented limitations, not claims of completed hardening.

---

## Author

**Syed Haris Mujtaba**  
Software Engineer · Backend & Systems · Automation

[**Website**](https://www.syedharis.pro) · [**LinkedIn**](https://www.linkedin.com/in/syedharismujtaba/) · [**Project Inquiries**](https://www.syedharis.pro/contact) · [**Email**](mailto:contact@syedharis.pro)

# PulseBoard — High-Level Design

> **Status:** Working draft. This document is deliberately iterative and will be refined as implementation decisions are made.

## 1. Purpose and scope

PulseBoard is a Spring Boot service for authenticated incident tracking. It is a learning project that uses PostgreSQL, MongoDB, Elasticsearch, Docker, Kubernetes/AKS, Azure Key Vault, Prometheus, and Grafana. The system is a single deployable service; it is not decomposed into multiple microservices.

## 2. Functional requirements

### 2.1 Identity and sessions

- A user can register.
- A user can log in.
- A user can log out.

**Design note:** JWTs are self-contained, so a client-side logout only discards the token; it does not invalidate an already-issued token. Server-enforced logout is deferred unless a token-revocation mechanism is added later.

### 2.2 Incident management

- An authenticated user can create, view, edit, and delete an incident.
- An authenticated user can resolve an incident and reopen a resolved incident.

### 2.3 Incident timeline

- The system records comments, status changes, and assignment changes against an incident.

### 2.4 Discovery

- Users can search incidents by incident name, incident ID, or incident-description content.
- Users can filter incidents by status and assignee.

### 2.5 Deferred design decisions

- The assignment-change requirement means incidents need an assignee concept. Whether this is a single optional user, a team, or both will be defined with the core entities.
- Authorization boundaries (for example, who may edit or delete an incident) will be defined with the API/interface requirements.

## 3. Non-functional requirements

| Area | V1 requirement | Design implication |
|---|---|---|
| Expected load | 10–50 users; up to 50–100 incidents or timeline comments per day | One Spring Boot service and managed data stores are sufficient; no early scaling design is required. |
| Performance | Normal authenticated incident operations should normally complete within 500 ms; search within 1 second, excluding client/network delay. | Establish basic API latency metrics. These are initial targets because the original requirement, “fast enough,” was not testable. |
| Availability | No uptime/service-level objective for v1; development and planned-maintenance downtime are acceptable. | Start with one pod. High availability and multi-region recovery are out of scope. |
| Consistency | Incident state is strongly consistent in PostgreSQL. Search may lag behind an incident change for a short period. | Elasticsearch is a rebuildable projection, not an authority for incident state. |
| Security baseline | Use password hashing and JWT-based authentication. Additional security requirements are minimal for v1. | HTTPS is required for deployed environments; secrets remain outside source control; JWT expiry and authorization policy are defined later. |
| Secondary-write reliability | For the initial build, log failed MongoDB timeline or Elasticsearch projection writes for manual repair. Add automatic retry after the core system is working. | Avoid distributed transactions. PostgreSQL changes must not be rolled back because a secondary store is unavailable. |
| Operations | Health checks, metrics, logs, and alerts are required. | Expose Actuator health and Prometheus metrics; collect structured logs; create a small Grafana dashboard and basic failure alerts. |

**Deferred improvement:** automatic retry needs a durable retry record or outbox-style mechanism in PostgreSQL. In-memory retries are not reliable after a pod restart, so add this only when automatic retry becomes an active requirement.

## 4. Core entities and data ownership

### 4.1 User

| Field | Notes |
|---|---|
| `id` | Stable user identifier. |
| `name` | Display name. |
| `email` | Unique login identifier. |
| `passwordHash` | A one-way password hash; never store a raw password. |
| `role` | Retained for future authorization policy. V1 can use a single `USER` value until differentiated permissions are needed. |
| `createdAt`, `updatedAt` | Audit timestamps. |

### 4.2 Incident

| Field | Notes |
|---|---|
| `id` | Stable incident identifier, used by the API and search. |
| `title`, `description` | Primary incident content. |
| `status` | Initial values: `OPEN`, `INVESTIGATING`, `RESOLVED`. |
| `severity` | Initial values: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`. |
| `creatorId` | Reference to the registered user who created the incident. |
| `assigneeId` | Optional reference to one registered user. An incident has at most one current assignee. |
| `tags` | Zero or more labels for categorisation. Model as a relational incident-to-tag association, not an unstructured comma-separated string. |
| `createdAt`, `updatedAt`, `resolvedAt` | Separate timestamps; a single vague `timestamp` is insufficient for audit and lifecycle queries. |

### 4.3 Timeline entry

Timeline is an append-only record, stored separately from the incident's authoritative state.

| Field | Notes |
|---|---|
| `id` | Stable event identifier. |
| `incidentId` | Reference to the incident (the original term “ticket” is normalized to this identifier). |
| `actorId` | The registered user who made the comment or change. |
| `type` | `COMMENT`, `STATUS_CHANGED`, or `ASSIGNMENT_CHANGED`. |
| `message` | Comment body; optional for non-comment events. |
| `previousValue`, `newValue` | Used for status and assignment changes, making the audit record meaningful. |
| `createdAt` | Event timestamp. |

### 4.4 Relationships and ownership

- A user can create many incidents and can be the current assignee of many incidents.
- An incident has zero or more tags and zero or more timeline entries.
- PostgreSQL owns `User`, `Incident`, and the incident-tag relationship.
- MongoDB owns `TimelineEntry` documents, which refer to PostgreSQL IDs but have no cross-database foreign key.
- Elasticsearch owns a denormalized, rebuildable incident-search projection.

**Deletion decision to resolve later:** hard-deleting an incident destroys its timeline/audit context. The recommended implementation is a soft delete (`deletedAt`/`deletedBy`) and exclusion from normal list/search results. This preserves history without changing the user-facing “delete” behaviour.

## 5. API and system interfaces

### 5.1 REST API

- All incident, timeline, and search endpoints require a valid JWT.
- Registration and login endpoints are public.
- API payloads use JSON over HTTPS.
- List and search responses support pagination and default to newest-first ordering.

| Resource | Intended operations |
|---|---|
| `/auth` | Register, log in, client-side log out. |
| `/incidents` | Create an incident; list incidents with status/assignee filters, pagination, and sorting. |
| `/incidents/{incidentId}` | Get, update, soft delete, resolve, and reopen an incident. |
| `/incidents/{incidentId}/timeline` | Read timeline entries; add comments. Status and assignment changes create timeline events automatically. |
| `/search/incidents` | Search title, incident ID, and description; filter by status and assignee; paginate results. |
| `/actuator/health`, `/actuator/prometheus` | Kubernetes health probing and Prometheus metric scraping. |

### 5.2 Authorization and lifecycle rules

- Any authenticated user may add a comment, edit incident details, change an assignee, and soft-delete an incident.
- Any authenticated user may change an incident status, except that only the incident creator may resolve or reopen it.
- Delete is a soft delete: set deletion metadata and exclude the incident from normal reads, lists, and search results. Do not physically erase its state or timeline.
- Status and assignment changes are represented both in the PostgreSQL incident state and as automatically generated MongoDB timeline events.

**Risk accepted for v1:** allowing any authenticated user to edit or delete any incident is intentionally permissive. A production system would normally use creator, assignee, team, or role-based authorization for these actions.

### 5.3 External system interfaces

| System | Direction | Interface purpose |
|---|---|---|
| PostgreSQL | Application ↔ database | Authoritative users, incidents, and tags; accessed with JPA/Hibernate. |
| MongoDB | Application ↔ database | Append-only timeline-entry documents. |
| Elasticsearch | Application ↔ search service | Incident search projection reads and writes. |
| Azure Key Vault | Application → secret store | Reads database credentials, search credentials, and JWT signing secret using workload identity. |
| Prometheus / Grafana | Monitoring system ← application | Scrapes application metrics and presents operational dashboards/alerts. |
| AKS | Client → Kubernetes → application | Runs and exposes the containerised Spring Boot application. |

## 6. High-level architecture

### 6.1 Core application and data architecture

![PulseBoard core high-level architecture](docs/assets/pulseboard-core-hld.png)

The diagram shows the initial, technology-neutral runtime architecture. Azure/Kubernetes deployment components will be added later, after the core application is working locally.

- PostgreSQL is the authoritative source of truth for users, incidents, and tags.
- MongoDB stores and returns incident timeline entries.
- Elasticsearch stores a rebuildable incident-search projection. It is updated when incident data changes and serves search queries; it is not used to determine authoritative incident state.

## 7. Key technical decisions and risks

_To be defined._

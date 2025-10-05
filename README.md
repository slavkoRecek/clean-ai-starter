# Clean AI Starter Backend

A production-ready Kotlin + Quarkus starter for building AI-enabled backends with clean architecture, modular boundaries, and batteries-included tooling. Use it to bootstrap projects that need AI-assisted workflows, secure JWT/OIDC authentication, PostgreSQL persistence, and real-time messaging out of the box.

- Modular clean architecture keeps AI workflows, storage, and messaging concerns isolated behind clear contracts.
- Koog-powered enrichment pipeline showcases how to run structured LLM calls in Kotlin services.
- Durable WebSocket messaging layer (Quarkus WebSockets Next) delivers entity-change notifications with acknowledgements and automatic backfill.

## Kotlin + Quarkus at the Core
- Quarkus 3 on Java 21 for fast startup, developer hot reload, and native compilation options.
- Kotlin 2.2 with kotlinx.serialization, coroutine-friendly Vert.x integration, and compiler defaults for modern language features.
- Gradle Kotlin DSL build (`build.gradle.kts`) wired to the Quarkus platform BOM, Koog agents, and AWS adapters.

## Koog-Powered AI Workflows
- Ships with the Koog Agents SDK (`ai.koog:koog-agents`) and Gemini executors for Google AI models.
- `LogEntryEnrichmentService` demonstrates prompt DSL usage, structured output parsing, and resilient retry configuration.
- `LLMProvider` centralizes Koog client wiring so you can swap providers or models with minimal surface area changes.

## Clean Architecture Overview
- Features live under `modules/<feature>/{domain,infrastructure}` to keep business logic isolated from adapters.
- Domain layer hosts immutable entities, services, and repository contracts; infrastructure supplies REST resources, persistence adapters, external clients, and config mappings.
- Shared concerns (exception mapping, pagination, serializers) are collected under `webinfra/` and `libs/`.

## Quick Start
1. Clone the repository and install Java 21, Docker, and Docker Compose.
2. Start the PostgreSQL service: `docker compose up postgres -d`.
3. Launch the Quarkus dev server: `./gradlew quarkusDev -Dquarkus.profile=local`.
4. Open `http://localhost:8080/q/swagger-ui/` for live API docs and `http://localhost:8080/q/dev/` for Quarkus Dev UI.

## Local Environment Setup
- Copy `.env.example` (if present) or create `.env` with at least `POSTGRES_PASSWORD` and optional AWS/Tigris credentials.
- Local profile settings live in `src/main/resources/application-local.properties`; override JDBC URL, OIDC issuer, and storage provider there.
- Use the bundled `compose.yml` to provision Postgres on `localhost:5445`.

## Application Configuration
- Central Quarkus config: `src/main/resources/application.properties` (baseline), `application-local.properties` (dev), `application-main.properties` (Fly.io).
- AI credentials: set `GEMINI_API_KEY` for Koog Gemini access via the `app.ai.gemini.api-key` mapping.
- Storage options: choose `app.storage.provider=tigris` or `s3` with matching bucket and endpoint properties.
- Authentication: configure OIDC issuer, client ID, and JWKS endpoint through `quarkus.oidc.*` and MicroProfile JWT properties.

## Domain Module Tour
- **Logbook** (`modules/logbook`): end-to-end AI log entry ingestion, enrichment pipeline, persistence, and REST resources.
- **Files** (`modules/files`): upload/download flows, S3/Tigris adapters, and secure link generation.
- **Folders** (`modules/folders`): hierarchical organization with ownership checks.
- **Messaging** (`modules/messaging`): entity change notifications, WebSocket delivery, and acknowledgment handling.
- **Auth & Profile** (`modules/auth`, `modules/profile`): OIDC-backed user info provider and self-profile endpoint.
- **Email** (`modules/email`): SES integration via domain-level `EmailSender` abstraction.

## AI Processing Pipeline
1. User uploads audio or structured content, persisted via the File module.
2. `LogEntryProcessingService` orchestrates asynchronous status transitions (upload → transcribe → enrich).
3. `LogEntryTranscriptionService` (stubbed for customization) and `LogEntryEnrichmentService` call Koog executors.
4. Structured summaries, titles, and categories are generated through Koog’s structured output helpers and stored back in Postgres.
5. Messaging module emits entity-changed events for subscribed clients.

## Authentication & Security
- JWT/OIDC support using Quarkus Security with `OidcUserInfoProvider` to read subject, profile, and picture claims.
- REST endpoints require `@Authenticated` where user context is needed; DTOs avoid leaking domain entities.
- WebSocket endpoint enforces authentication via Quarkus WebSockets Next subprotocol negotiation.
- Local testing flow uses Clerk as the OIDC provider (see `auth_demo_react/`) so you can generate JWTs that mirror production-style tokens during development and Bruno runs.

## Data & Persistence
- PostgreSQL via Hibernate ORM with Panache in infrastructure repositories.
- Flyway migrations located in `src/main/resources/db/migration/` (baseline `V1__create_core_tables.sql`).
- Domain objects persist using UUID primary keys, timestamp auditing, and soft-delete fields where appropriate.

## Messaging & Realtime Updates
- `EntityChangedMessageService` records change events for any module, persisting them until clients confirm receipt.
- `MessageWebSocketEndpoint` (Quarkus WebSockets Next) authenticates users, replays queued messages, and streams new ones live.
- Connection registry manages multiple devices per user, gracefully handling reconnects and distributing messages across sessions.
- Clients acknowledge deliveries with JSON payloads; persisted status prevents duplicates and supports offline catch-up flows.

## Testing Strategy
- JUnit 5 + `@QuarkusTest` integration suites covering REST resources, file flows, and logbook enrichment.
- Mockito Kotlin utilities for domain-level unit tests (`src/test/kotlin/com/cleanai/fixture`).
- Bruno API collections (`bruno/clean-ai-starter/`) for manual verification with real OIDC tokens.

## Tooling & Developer Experience
- Quarkus Dev UI, swagger-ui, and live reload for rapid iteration.
- Cursor/Codex-friendly repository context (see `AGENTS.md`, `CLAUDE.md`).
- Bruno manual testing guide in `BRUNO_TESTING.md` and Clerk-based token generator under `auth_demo_react/`.
- Qodana static analysis config (`qodana.yaml`) ready for JetBrains inspections.

## Deployment Options
- Fly.io ready: `fly.toml` points to `src/main/docker/Dockerfile.jvm` with migration-on-start and health checks.
- Container builds for JVM, uber-jar, and native images under `src/main/docker/`.
- Configure environment variables for production OIDC, storage, and database targets via Fly secrets or your platform of choice.

## Project Structure Reference
```
src/main/kotlin/com/cleanai/
├── modules/
│   ├── logbook/            # AI logbook feature (domain + infra)
│   ├── files/              # File upload and storage adapters
│   ├── folders/            # Folder hierarchy management
│   ├── messaging/          # Entity change events + websockets
│   ├── auth/ | profile/    # OIDC integration & profile API
│   └── email/              # SES email sender
├── webinfra/               # Cross-cutting web concerns (errors, pagination)
└── libs/                   # Shared domain utilities (exceptions, pagination, LLM helpers)

src/main/resources/
├── application*.properties # Config per profile
├── db/migration/           # Flyway migrations
└── META-INF/
```

## License & Credits
Released under the Apache 2.0 License. Built with AI and love as a foundation for AI-first Kotlin + Quarkus services.

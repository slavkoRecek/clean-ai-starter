# Repository Guidelines

## Project Structure & Module Organization
Logbook Backend uses a modular Quarkus layout under `src/main/kotlin/com/cleanai`. Feature work belongs in `modules/<feature>/{domain,infrastructure}`—domain owns business rules, infrastructure holds REST, persistence, and external adapters. Shared utilities stay in `webinfra` or `libs`. Config, Flyway migrations, and serialization tweaks live in `src/main/resources`. Tests mirror production packages in `src/test/kotlin` with fixtures in `src/test/resources`.

## Build, Test, and Development Commands
- `docker compose up postgres -d` starts the Postgres instance used by dev and tests.
- `./gradlew quarkusDev -Dquarkus.profile=local` runs Quarkus with hot reload on `http://localhost:8080`.
- `./gradlew build` compiles, executes tests, and assembles artifacts under `build/`.
- `./gradlew test --tests "ProfileServiceTest"` scopes execution to a class while iterating.

## Coding Style & Naming Conventions
Target Kotlin 2.2 with four-space indentation, trailing commas for multiline calls, and immutable domain types. Use constructor injection with `@ApplicationScoped` beans; expose REST resources from infrastructure packages annotated with `@Path`, `@Produces`, `@Consumes`, and guard user endpoints using `@Authenticated`. Name classes in PascalCase (`ProfileService`, `PgProfileRepository`), DTOs with `Request`/`Response`, and migrations as `V{version}__{description}.sql`. Keep DTOs separate from JPA entities and rely on kotlinx.serialization

## Testing Guidelines
Place unit and integration suites in `src/test/kotlin`. Unit tests use JUnit 5 plus `mockito-kotlin`; integration flows rely on `@QuarkusTest`, RestAssured for HTTP assertions, and WireMock to isolate externals. Add ArchUnit rules when touching boundaries, cover success, failure, and security cases, and run `./gradlew test` before handing a branch to review.

## Commit & Pull Request Guidelines
Write concise, imperative commits (`fix deploy warning`) and add explanatory bodies when introducing migrations or cross-cutting changes.. PRs should restate scope, link the spec, list manual verification (migrations, Bruno flows), highlight new env vars, and attach UI or schema screenshots. Confirm CI is green prior to requesting review.

## Collaboration Workflow
We operate specification-first: draft or update the design doc in `ai_implementation/` before coding, confirm alignment with the architect, then outline the implementation plan. For multi-step tasks, create a plan with at least two items and update it as you progress. Ask clarifying questions early, present trade-offs when they exist, and challenge risky assumptions constructively.

## Security & Configuration Tips
Store secrets in `.env` or shell profiles. Update `fly.toml`, `compose.yml`, and supporting notes whenever infrastructure changes. Prefer helpers in `scripts/` for key generation, enforce `@Authenticated` or role checks on new endpoints, and scope AWS resources per environment to avoid collisions.

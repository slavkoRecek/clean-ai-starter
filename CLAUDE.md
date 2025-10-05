# Logbook Backend - Development Guide

## 1. Project Overview

Kotlin/Quarkus backend with modular clean architecture, JWT authentication, PostgreSQL database, and AWS integration.

## 2. Architecture & Standards

### 2.1 Module Organization

- Organize features into modules under `src/main/kotlin/com/chirp/modules/`
- Each module follows clean architecture: `domain/` and `infrastructure/`
- Domain layer: business logic, entities, repository interfaces
- Infrastructure layer: REST endpoints, JPA entities, repository implementations

#### Module Structure Pattern

```
modules/{feature}/
├── domain/
│   ├── {Feature}.kt           # Domain entity
│   ├── {Feature}Service.kt    # Business logic
│   └── {Feature}Repository.kt # Repository interface
└── infrastructure/
    ├── {Feature}Resource.kt   # REST endpoints
    ├── {Feature}Entity.kt     # JPA entity
    └── Pg{Feature}Repository.kt # Repository implementation
```

### 2.2 Coding Standards

#### Dependency Injection
- Use `@ApplicationScoped` for services and repositories
- Use constructor injection, not field injection
- Inject `UserInfoProvider` to get current authenticated user ID

#### REST Resources
- Place in `infrastructure/` layer
- Use `@Path("/api/{resource}")` for endpoints
- Use `@Authenticated` for protected endpoints
- Always use `@Produces(MediaType.APPLICATION_JSON)` and `@Consumes(MediaType.APPLICATION_JSON)`
- Separate DTOs from domain entities
- Create companion object `fromDomain()` methods in DTOs
- Inject `UserInfoProvider` to get `userId` in authenticated endpoints
- Always verify user ownership for user-specific resources
- Use JWT tokens with Bearer authentication

#### Database
- Use JPA entities in infrastructure layer, domain entities in domain layer
- Repository interfaces in domain layer, implementations in infrastructure
- Use Panache for repository implementations
- Create Flyway migrations manually with format: `V{version}__{description}.sql`
- Always test migrations locally first

#### Error Handling
- Use domain exceptions from `com.cleanai.libs.exception.DomainException` hierarchy
- All domain exceptions extend `DomainException` sealed class
- HTTP status mapping is handled automatically by `@Provider` ExceptionMapper classes
- Available domain exceptions:
  - `ObjectNotFoundException` → 404 NOT_FOUND
  - `UnauthorizedAccessException` → 403 FORBIDDEN
  - `UnauthenticatedException` → 401 UNAUTHORIZED
  - `DomainValidationException` → 400 BAD_REQUEST
  - `ResourceAlreadyExistsException` → 409 CONFLICT
  - `InvalidStateException` → 409 CONFLICT
  - `InfrastructureException` → 503 SERVICE_UNAVAILABLE
- All exceptions return consistent `ErrorResponse(message: String)` format
- For validation: `ConstraintViolationException` → 400 BAD_REQUEST
- Standard exceptions: `IllegalAccessException` → 403, `IllegalArgumentException` → 400

#### Validation & Serialization
- Use `@Valid` for request validation
- Use `@field:NotBlank`, `@field:NotNull` for validation annotations
- **Hybrid Serialization Approach**:
  - **REST Endpoints**: Use `@Serializable` for DTOs with kotlinx.serialization (primary)
  - **Tests**: Jackson is configured for REST Assured compatibility
  - **Jackson Configuration**: Automatically ignores getter methods, only serializes fields
  - **No Manual Annotations**: Avoid `@JsonIgnore` - use global Jackson configuration instead

#### Instant/Timestamp Handling
- **For DTOs/API Contracts**: Use `String` fields with conversion methods (preferred approach)
  ```kotlin
  @Serializable
  data class ExampleDto(
      val createdAt: String,  // ISO string format
      val updatedAt: String   // ISO string format
  ) {
      // Safe: Jackson ignores getter methods, only serializes fields
      fun getCreatedAtInstant(): Instant = Instant.parse(createdAt)
      fun getUpdatedAtInstant(): Instant = Instant.parse(updatedAt)
  }
  ```
- **For internal domain models**: Use `Instant` directly without serialization concerns
- **Global Jackson Configuration**: Automatically handles Java 8 time types when they appear as fields

#### Testing Strategy
- Write automated tests as primary verification method
- Unit tests for services and domain logic
- Integration tests for repositories and resources using `@QuarkusTest`
- Mock external dependencies in unit tests
- Use Bruno only for manual API verification during development

#### AWS Integration
- S3 for file storage using `FileStorageRepository` interface
- SES for email sending using `EmailSender` interface
- Configure AWS credentials via environment variables
- Use presigned URLs for file downloads

### 2.3 File Naming Conventions

- Domain entities: `{Feature}.kt`
- Services: `{Feature}Service.kt`
- Repository interfaces: `{Feature}Repository.kt`
- Repository implementations: `Pg{Feature}Repository.kt`
- REST resources: `{Feature}Resource.kt`
- JPA entities: `{Feature}Entity.kt`
- DTOs: `{Feature}Dto.kt`, `Create{Feature}Dto.kt`, `Update{Feature}Dto.kt`

### 2.4 Environment Configuration

- Database config in `application.properties`
- Local overrides in `application-local.properties`
- Sensitive values via environment variables
- AWS credentials via `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
- S3 bucket via `app.storage.s3.bucket`

### 2.5 Common Patterns to Avoid

- Don't put business logic in REST resources
- Don't use JPA entities as DTOs
- Don't skip authentication on user-specific endpoints
- Don't modify existing database migrations
- Don't write integration tests without `@QuarkusTest`
- Don't use field injection over constructor injection
- Don't throw generic exceptions - use specific domain exceptions
- Don't handle exceptions in resources - let ExceptionMappers handle them
- Don't add manual Jackson annotations (`@JsonIgnore`, `@JsonProperty`) - use global configuration
- Don't create getter methods that return non-serializable types (like `Instant`) in DTOs

## 3. Development Workflow

### 3.1 Implementation Process

1. Create domain layer first (entity, service, repository interface)
2. Create infrastructure layer (JPA entity, repository implementation, REST resource)
3. Write automated tests for all layers
4. Create database migration if needed
5. Use Bruno for manual API verification

### 3.2 Code Templates

#### REST Resource Template

```kotlin
@Path("/api/{resources}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class {Feature}Resource(
    private val {feature}Service: {Feature}Service,
    private val userInfoProvider: UserInfoProvider
) {
    @PUT
    @Authenticated
    fun upsert{Feature}(@Valid request: Upsert{Feature}Request): Response {
        val userId = userInfoProvider.getUserId()
        val {feature} = request.toDomain(userId)
        val saved{Feature} = {feature}Service.upsert{Feature}({feature})
        return Response.ok({Feature}Response.fromDomain(saved{Feature})).build()
    }
    
    @GET
    @Path("/me")
    @Authenticated
    fun get{Feature}(): Response {
        val userId = userInfoProvider.getUserId()
        // Implementation
    }
}
```

#### Service Template

```kotlin
@ApplicationScoped
class {Feature}Service(
    private val {feature}Repository: {Feature}Repository
) {
    fun upsert{Feature}({feature}: {Feature}): {Feature} {
        val existing = {feature}Repository.findById({feature}.id)
        
        // Verify ownership if exists
        existing?.let { 
            if (it.userId != {feature}.userId) {
                throw UnauthorizedAccessException("User ${feature}.userId cannot modify {feature} ${feature}.id")
            }
        }
        
        return {feature}Repository.persist({feature})
    }
    
    fun getByUserId(userId: Long): {Feature}? {
        return {feature}Repository.findByUserId(userId)
    }
}
```

#### Repository Interface Template

```kotlin
interface {Feature}Repository {
    fun findById(id: UUID): {Feature}?
    fun findByUserId(userId: Long): {Feature}?
    fun persist({feature}: {Feature}): {Feature}
}
```

#### Repository Implementation Template

```kotlin
@ApplicationScoped
class Pg{Feature}Repository : {Feature}Repository {

    override fun findById(id: UUID): {Feature}? {
        return {Feature}Entity.findById(id)?.toDomain()
    }

    override fun findByUserId(userId: String): {Feature}? {
        return {Feature}Entity.find("userId = ?1", userId)
            .firstResult()?.toDomain()
    }

    @Transactional
    override fun persist({feature}: {Feature}): {Feature} {
        val existingEntity = {Feature}Entity.findById({feature}.id)
        
        val entity = if (existingEntity != null) {
            // Update existing entity
            existingEntity.apply {
                // Update fields from domain object
                field1 = {feature}.field1
                field2 = {feature}.field2
                updatedAt = {feature}.updatedAt
            }
        } else {
            // Create new entity
            {Feature}Entity.from({feature})
        }
        
        entity.persistAndFlush()
        return entity.toDomain()
    }
}
```

#### Exception Usage Example

```kotlin
// In service layer
fun getByUserId(userId: Long): Feature {
    return repository.findByUserId(userId)
        ?: throw ObjectNotFoundException("Feature not found for user $userId")
}

// For access control
if (feature.userId != userId) {
    throw UnauthorizedAccessException("User $userId cannot access feature ${feature.id}")
}

// For validation
if (request.name.isBlank()) {
    throw DomainValidationException("Feature name cannot be blank")
}
```

## 4. AI Assistant Guidelines

### 4.1 Role Definition

- **Human**: Solution Architect - defines requirements, makes high-level decisions, provides technical direction
- **AI**: Senior Backend Developer - independently executes tasks, provides technical expertise, challenges decisions constructively

### 4.2 Collaboration Process

#### Communication Principles
- **Ask clarifying questions** before starting implementation to fully understand requirements
- **Challenge decisions constructively** when you identify potential issues or better alternatives
- **Present options** when multiple valid approaches exist, with pros/cons analysis
- **Confirm understanding** by restating requirements in technical terms
- **Provide expert feedback** on feasibility, performance, security, and maintainability concerns

#### Decision Making Process
1. **Understand the requirement**: Ask questions to clarify scope, constraints, and business goals
2. **Analyze options**: Identify multiple approaches and their trade-offs
3. **Present recommendations**: Share your analysis with clear reasoning
4. **Get architect approval**: Wait for decision before implementing
5. **Execute independently**: Implement the approved solution with full autonomy

#### Examples of Constructive Challenges
- "I understand you want feature X, but given our current architecture, approach Y might be more maintainable because..."
- "This requirement could be implemented in 3 ways: [options]. I recommend option A because..."
- "Before implementing this, I want to confirm: are you aware this will require a database migration that affects..."
- "I notice this conflicts with our existing pattern Z. Should we maintain consistency or is there a reason to deviate?"

#### When to Present Options
- Multiple valid architectural approaches exist
- Performance vs. complexity trade-offs need consideration
- Security implications require decision
- Breaking changes vs. backward compatibility choices
- Different levels of implementation complexity available

#### Clarifying Questions to Ask
- What is the business goal behind this requirement?
- Are there any performance or scalability constraints?
- Should this follow existing patterns or is innovation needed?
- What is the priority: speed of delivery vs. long-term maintainability?
- Are there any security or compliance considerations?
- How should this integrate with existing features?


#### When to Use Gradle Directly
- Build and compilation: `./gradlew build`, `./gradlew clean`
- Dependency management: `./gradlew dependencies`
- Run all tests: `./gradlew test`
- Run specific test no local profile: `./gradlew test --tests *{TestClassName}*`
- **IMPORTANT**: Always start Quarkus dev mode with local profile: `./gradlew quarkusDev -Dquarkus.profile=local`


### 4.5 Implementation Standards

- Follow all architecture rules and coding standards above
- Write e2e tests for all new functionality

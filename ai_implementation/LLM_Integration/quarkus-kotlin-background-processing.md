# Quarkus Background Processing with ManagedExecutor - Implementation Guide

## Overview

This document explains how to implement correct asynchronous background processing in Quarkus using `ManagedExecutor` and blocking operations. It serves as a reference for the [Chirp Processing Pipeline](./chirp-processing-sdd.md) and other background processing features.

## ManagedExecutor vs Coroutines

### Why ManagedExecutor?

While Quarkus supports Kotlin coroutines for REST endpoints, **long-running background processing** with database operations is better handled with `ManagedExecutor` and traditional blocking operations.

### Key Benefits of ManagedExecutor

- **CDI Context Propagation**: Maintains request context across async operations
- **Transaction Control**: Works with `@Transactional` for long-running transactions
- **Container Managed**: Proper lifecycle management by Quarkus
- **Simpler Debugging**: Traditional stack traces, easier to troubleshoot
- **Jakarta EE Standard**: Follows enterprise Java patterns

## Implementation Pattern

### Background Processing Service

```kotlin
@ApplicationScoped
class BackgroundProcessingService(
    private val repository: SomeRepository
) {
    private val log = LoggerFactory.getLogger(BackgroundProcessingService::class.java)

    @Transactional
    @ActivateRequestContext
    fun processAsync(id: UUID) {
        try {
            executeProcessing(id)
        } catch (error: Exception) {
            log.error("Processing failed for: $id", error)
            handleProcessingError(id, error)
        }
    }

    private fun executeProcessing(id: UUID) {
        // Step 1: Validate input
        val entity = repository.findById(id) 
            ?: error("Entity not found: $id")

        // Step 2: Execute processing steps with blocking operations
        updateStatus(id, Status.PROCESSING)
        
        // Simulate long-running work (e.g., external API calls)
        Thread.sleep(5000) // Audio processing simulation
        updateStatus(id, Status.TRANSCRIBED)
        
        Thread.sleep(10000) // LLM processing simulation  
        updateStatus(id, Status.COMPLETED)
        
        log.info("Completed processing for: $id")
    }

    private fun updateStatus(id: UUID, status: Status) {
        val entity = repository.findById(id) ?: return
        repository.persist(entity.copy(status = status))
    }

    private fun handleProcessingError(id: UUID, error: Throwable) {
        val entity = repository.findById(id) ?: return
        repository.persist(
            entity.copy(
                status = Status.FAILED,
                errorMessage = error.message
            )
        )
    }
}
```

### Service Layer Triggering Background Processing

```kotlin
@ApplicationScoped
class SomeService(
    private val repository: SomeRepository,
    private val backgroundProcessingService: BackgroundProcessingService,
    private val managedExecutor: ManagedExecutor
) {
    
    fun createEntity(data: CreateRequest): Entity {
        val entity = repository.persist(data.toEntity())
        
        // Trigger background processing when entity reaches UPLOADED status
        if (entity.status == Status.UPLOADED) {
            log.info("Triggering background processing for entity ${entity.id}")
            
            // Fire-and-forget: use ManagedExecutor for proper context propagation
            managedExecutor.submit {
                backgroundProcessingService.processAsync(entity.id)
            }
        }
        
        return entity
    }
}
```

## Key Design Principles

### 1. Fire-and-Forget Pattern
- REST endpoints trigger processing but return immediately
- Users poll for status updates instead of waiting for completion
- Non-blocking request handling

### 2. Error Handling
- Use `runCatching` for top-level error handling
- Store error states in the database for user visibility
- Log detailed error information for debugging

### 3. Status Tracking
- Update entity status throughout processing pipeline
- Provide clear status transitions (PENDING → PROCESSING → COMPLETED/FAILED)
- Enable user polling for progress updates

### 4. Required Annotations for Background Processing
- **Always use**: `@Transactional` to maintain transaction across entire processing
- **Always use**: `@ActivateRequestContext` to ensure CDI context is active
- **Don't use**: Manual transaction management or context switching

## Integration with Existing Systems

### Database Updates
```kotlin
// ✅ Correct: Direct repository calls in blocking functions
private fun updateEntity(id: UUID, data: Data) {
    repository.findById(id)?.let { entity ->
        repository.persist(entity.copy(data = data))
    }
}
```

### External API Calls
```kotlin
// ✅ Correct: Blocking HTTP calls (using RestClient or OkHttp)
@ApplicationScoped
class ProcessingService(
    private val httpClient: OkHttpClient
) {
    fun processWithExternalCall(data: Data): Result {
        val request = Request.Builder()
            .url("https://api.example.com/process")
            .post(data.toRequestBody())
            .build()
            
        val response = httpClient.newCall(request).execute() // Blocking call
        return response.body?.string()?.toResult() ?: error("No response")
    }
}
```

### Long-Running Operations
```kotlin
// ✅ Correct: Use Thread.sleep for delays, blocking operations
private fun processWithDelay(id: UUID) {
    updateStatus(id, Status.PROCESSING)
    
    // Simulate long-running work
    Thread.sleep(5000) // Audio processing
    updateStatus(id, Status.TRANSCRIBED)
    
    Thread.sleep(10000) // LLM processing
    updateStatus(id, Status.COMPLETED)
}
```

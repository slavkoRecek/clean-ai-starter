package com.cleanai.libs.exception

/**
 * Base exception class for all domain-specific exceptions
 */
sealed class DomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Used when an entity is not found
 */
class ObjectNotFoundException(
    message: String = "Resource not found",
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Used when user doesn't have access to a resource
 */
class UnauthorizedAccessException(
    message: String = "Unauthorized access to resource",
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Used when user does not provide valid credentials
 */
class UnauthenticatedException(
    message: String = "Unauthenticated access",
    cause: Throwable? = null
) : DomainException(message, cause)


/**
 * Used for validation failures in domain logic
 */
class DomainValidationException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Used when a resource already exists (e.g., duplicate email)
 */
class ResourceAlreadyExistsException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Used when an operation is not allowed in current state
 */
class InvalidStateException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)

class InfrastructureException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)

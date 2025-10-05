package com.cleanai.webinfra

import com.cleanai.libs.exception.DomainException
import com.cleanai.libs.exception.DomainValidationException
import com.cleanai.libs.exception.InfrastructureException
import com.cleanai.libs.exception.InvalidStateException
import com.cleanai.libs.exception.ObjectNotFoundException
import com.cleanai.libs.exception.ResourceAlreadyExistsException
import com.cleanai.libs.exception.UnauthenticatedException
import com.cleanai.libs.exception.UnauthorizedAccessException
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class DomainExceptionMapper : ExceptionMapper<DomainException> {
    override fun toResponse(exception: DomainException): Response {
        val (status, message) = when (exception) {
            is ObjectNotFoundException -> Response.Status.NOT_FOUND to
                    (exception.message ?: "Resource not found")

            is UnauthorizedAccessException -> Response.Status.FORBIDDEN to
                    (exception.message ?: "Access denied")
            is UnauthenticatedException -> Response.Status.UNAUTHORIZED to
                    (exception.message ?: "Unauthenticated access")

            is DomainValidationException -> Response.Status.BAD_REQUEST to
                    (exception.message ?: "Validation failed")

            is ResourceAlreadyExistsException -> Response.Status.CONFLICT to
                    (exception.message ?: "Resource already exists")

            is InvalidStateException -> Response.Status.CONFLICT to
                    (exception.message ?: "Invalid state for operation")

            is InfrastructureException -> Response.Status.SERVICE_UNAVAILABLE to
                    (exception.message ?: "Infrastructure issue occurred")
        }

        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErrorResponse(message))
            .build()
    }
}

@Provider
class ValidationExceptionMapper : ExceptionMapper<ConstraintViolationException> {
    override fun toResponse(exception: ConstraintViolationException): Response {
        val message = exception.constraintViolations.firstOrNull()?.message
            ?: "Validation failed"

        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErrorResponse(message))
            .build()
    }
}

@Provider
class IllegalAccessExceptionMapper : ExceptionMapper<IllegalAccessException> {
    override fun toResponse(exception: IllegalAccessException): Response {
        return Response.status(Response.Status.FORBIDDEN)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErrorResponse(exception.message ?: "Access denied"))
            .build()
    }
}

@Provider
class IllegalArgumentExceptionMapper : ExceptionMapper<IllegalArgumentException> {
    override fun toResponse(exception: IllegalArgumentException): Response {
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErrorResponse(exception.message ?: "Invalid argument"))
            .build()
    }
}



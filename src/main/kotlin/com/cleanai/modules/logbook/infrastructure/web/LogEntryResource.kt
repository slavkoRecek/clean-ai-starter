package com.cleanai.modules.logbook.infrastructure.web

import com.cleanai.modules.auth.infrastructure.OidcUserInfoProvider
import com.cleanai.modules.logbook.domain.LogEntryService
import com.cleanai.webinfra.PageResponse
import io.quarkus.security.Authenticated
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.UUID

@Path("/api/log-entries")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class LogEntryResource(
    private val logEntryService: LogEntryService,
    private val oidcUserInfoProvider: OidcUserInfoProvider
) {

    @PUT
    @Authenticated
    fun upsertLogEntry(@Valid request: UpsertLogEntryRequest): Response {
        val authorId = oidcUserInfoProvider.getOidcUserId()
        val logEntry = request.toDomain(authorId)
        val savedLogEntry = logEntryService.upsertLogEntry(logEntry, authorId)
        return Response.ok(LogEntryResponse.fromDomain(savedLogEntry)).build()
    }

    @GET
    @Path("/{logEntryId}")
    @Authenticated
    fun getLogEntry(@PathParam("logEntryId") logEntryId: String): Response {
        val authorId = oidcUserInfoProvider.getOidcUserId()
        val logEntryUuid = UUID.fromString(logEntryId)

        val logEntry = logEntryService.getLogEntryById(logEntryUuid, authorId)
        return Response.ok(LogEntryResponse.fromDomain(logEntry)).build()
    }

    @GET
    @Path("/me")
    @Authenticated
    fun getMyLogEntries(
        @QueryParam("limit") @DefaultValue("20") limit: Int,
        @QueryParam("offset") @DefaultValue("0") offset: Int,
        @QueryParam("search") search: String?,
        @QueryParam("orderBy") @DefaultValue("UPDATED_AT") orderBy: LogEntryOrderByDto,
        @QueryParam("orderDirection") @DefaultValue("DESC") orderDirection: SortDirectionDto
    ): PageResponse<LogEntryPreview> {
        val authorId = oidcUserInfoProvider.getOidcUserId()

        val result = logEntryService.getLogEntriesForAuthor(
            authorId = authorId,
            limit = limit,
            offset = offset,
            search = search,
            orderBy = orderBy.toDomain(),
            orderDirection = orderDirection.toDomain()
        )

        return PageResponse.fromDomain(result) { LogEntryPreview.fromDomain(it) }
    }
}

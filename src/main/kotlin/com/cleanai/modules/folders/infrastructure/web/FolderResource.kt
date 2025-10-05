package com.cleanai.modules.folders.infrastructure.web

import com.cleanai.modules.auth.infrastructure.OidcUserInfoProvider
import com.cleanai.modules.folders.domain.FolderService
import com.cleanai.webinfra.PageResponse
import io.quarkus.security.Authenticated
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.UUID

@Path("/api/folders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class FolderResource(
    private val folderService: FolderService,
    private val oidcUserInfoProvider: OidcUserInfoProvider,
) {

    @PUT
    @Authenticated
    fun upsertFolder(@Valid request: UpsertFolderRequest): FolderResponse {
        val userId = oidcUserInfoProvider.getOidcUserId()
        val folder = request.toDomain(userId)
        val savedFolder = folderService.upsertFolder(folder, userId)
        return FolderResponse.fromDomain(savedFolder)
    }

    @GET
    @Path("/{folderId}")
    @Authenticated
    fun getFolder(@PathParam("folderId") folderId: String): Response {
        val userId = oidcUserInfoProvider.getOidcUserId()
        val folderUuid = UUID.fromString(folderId)
        val folder = folderService.getFolderById(folderUuid, userId)
        return Response.ok(FolderResponse.fromDomain(folder)).build()
    }

    @GET
    @Path("/me")
    @Authenticated
    fun getUserFolders(
        @QueryParam("parentId") parentId: String?,
        @QueryParam("limit") @DefaultValue("20") limit: Int,
        @QueryParam("offset") @DefaultValue("0") offset: Int,
        @QueryParam("search") search: String?,
        @QueryParam("includeArchived") @DefaultValue("false") includeArchived: Boolean,
        @QueryParam("includeDeleted") @DefaultValue("false") includeDeleted: Boolean,
        @QueryParam("orderBy") @DefaultValue("UPDATED_AT") orderBy: FolderOrderByDto,
        @QueryParam("orderDirection") @DefaultValue("DESC") orderDirection: SortDirectionDto,
    ): PageResponse<FolderPreview> {
        val userId = oidcUserInfoProvider.getOidcUserId()
        val parentUuid = parentId?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) }

        val pageResult = folderService.getUserFolders(
            userId = userId,
            parentId = parentUuid,
            limit = limit,
            offset = offset,
            search = search,
            includeArchived = includeArchived,
            includeDeleted = includeDeleted,
            orderBy = orderBy.toDomain(),
            orderDirection = orderDirection.toDomain(),
        )

        return PageResponse.fromDomain(pageResult) { FolderPreview.fromDomain(it) }
    }
}

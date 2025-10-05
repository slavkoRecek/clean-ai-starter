package com.cleanai.modules.files.infrastructure.web

import com.cleanai.modules.auth.infrastructure.OidcUserInfoProvider
import com.cleanai.modules.files.domain.FileService
import com.cleanai.modules.files.domain.FileUploadRequest
import io.quarkus.security.Authenticated
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.jboss.resteasy.reactive.RestForm
import org.jboss.resteasy.reactive.multipart.FileUpload
import java.util.*

@Path("/api/files")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Files")
@Authenticated
class FileResource(
    private val fileService: FileService,
    private val oidcUserInfoProvider: OidcUserInfoProvider
) {

    @POST
    fun uploadFile(
        @RestForm("file") input: FileUpload
    ): FileDto? {
        val userId = oidcUserInfoProvider.getOidcUserId()
        val request = FileUploadRequest(
            name = input.fileName(),
            mimeType = input.contentType(),
            sizeByte = input.size().toDouble(),
            path = input.uploadedFile()
        )
        val file = fileService.uploadFile(userId, request)
        return FileDto.fromDomain(file)
    }

    @GET
    @Path("/{fileId}")
    fun getFile(
        @PathParam("fileId") fileId: String
    ): FileDto {
        val userId = oidcUserInfoProvider.getOidcUserId()
        val response = fileService.getFile(userId, UUID.fromString(fileId))
        return FileDto.fromDomain(response)
    }

}


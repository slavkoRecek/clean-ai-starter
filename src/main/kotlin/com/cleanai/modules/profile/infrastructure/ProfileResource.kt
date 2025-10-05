package com.cleanai.modules.profile.infrastructure

import com.cleanai.modules.auth.infrastructure.OidcUserInfoProvider
import io.quarkus.security.Authenticated
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.serialization.Serializable

@Path("/api/profiles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ProfileResource(
    private val oidcUserInfoProvider: OidcUserInfoProvider
) {

    @GET
    @Path("/me")
    @Authenticated
    fun getUserProfile(): Response {
        val profileDto = oidcUserInfoProvider.getProfileDto()
        return Response.ok(profileDto).build()
    }
}

@Serializable
data class ProfileDto(
    val oidcUserId: String,
    val name: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val imageUrl: String?
)

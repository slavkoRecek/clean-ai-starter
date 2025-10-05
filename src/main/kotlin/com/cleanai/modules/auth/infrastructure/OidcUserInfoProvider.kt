package com.cleanai.modules.auth.infrastructure

import com.cleanai.modules.profile.infrastructure.ProfileDto
import org.eclipse.microprofile.jwt.JsonWebToken
import jakarta.enterprise.context.RequestScoped

@RequestScoped
class OidcUserInfoProvider(
    private val jwt: JsonWebToken
) {
    
    fun getOidcUserId(): String {
        return jwt.subject
    }
    
    fun getProfileDto(): ProfileDto {
        val oidcUserId = jwt.subject
        val email = jwt.getClaim<String>("email") ?: "unknown@example.com"
        val givenName = jwt.getClaim<String>("given_name")
        val familyName = jwt.getClaim<String>("family_name")
        val imageUrl = jwt.getClaim<String>("picture")

        val name = when {
            givenName != null && familyName != null -> "$givenName $familyName"
            givenName != null -> givenName
            familyName != null -> familyName
            else -> email.substringBefore("@")
        }

        return ProfileDto(
            oidcUserId = oidcUserId,
            name = name,
            email = email,
            firstName = givenName,
            lastName = familyName,
            imageUrl = imageUrl
        )
    }
}

package com.cleanai.webinfra

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.quarkus.jackson.ObjectMapperCustomizer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import kotlinx.serialization.json.Json

@ApplicationScoped
class JsonSerializationConfig : ObjectMapperCustomizer {

    @Produces
    fun jsonSerializer(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        // You can add other configuration options here if needed
        // coerceInputValues = true
        // isLenient = true
        // etc.
    }
    
    override fun customize(mapper: ObjectMapper) {
        // Disable getter/setter method detection - only use fields
        // This prevents Jackson from trying to serialize computed properties like getCreatedAtInstant()
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        
        // Enable JSR310 time module for Instant handling
        mapper.registerModule(JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}

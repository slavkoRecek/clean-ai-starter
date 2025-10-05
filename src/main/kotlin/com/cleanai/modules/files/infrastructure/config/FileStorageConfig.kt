package com.cleanai.modules.files.infrastructure.config

import com.cleanai.modules.files.domain.FileStorageRepository
import com.cleanai.modules.files.infrastructure.persistence.S3FileStorageRepository
import com.cleanai.modules.files.infrastructure.persistence.TigrisFileStorageRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.services.s3.S3Client

@ApplicationScoped
class FileStorageConfig {

    @Produces
    @ApplicationScoped
    fun fileStorageRepository(
        @ConfigProperty(name = "app.storage.provider") storageProvider: String,
        @ConfigProperty(name = "app.storage.s3.bucket", defaultValue = "default-bucket") s3BucketName: String,
        @ConfigProperty(name = "app.storage.tigris.endpoint", defaultValue = "https://fly.storage.tigris.dev") tigrisEndpoint: String,
        @ConfigProperty(name = "app.storage.tigris.bucket", defaultValue = "default-bucket") tigrisBucketName: String,
        @ConfigProperty(name = "AWS_ACCESS_KEY_ID", defaultValue = "") accessKeyId: String,
        @ConfigProperty(name = "AWS_SECRET_ACCESS_KEY", defaultValue = "") secretAccessKey: String,
        @ConfigProperty(name = "AWS_REGION", defaultValue = "auto") region: String,
        s3Client: S3Client
    ): FileStorageRepository {
        return when (storageProvider.lowercase()) {
            "s3" -> S3FileStorageRepository(s3Client, s3BucketName)
            "tigris" -> TigrisFileStorageRepository(
                tigrisEndpoint = tigrisEndpoint,
                bucketName = tigrisBucketName,
                accessKeyId = accessKeyId,
                secretAccessKey = secretAccessKey,
                region = region
            )
            else -> throw IllegalArgumentException("Unknown storage provider: $storageProvider. Supported providers: s3, tigris")
        }
    }
}

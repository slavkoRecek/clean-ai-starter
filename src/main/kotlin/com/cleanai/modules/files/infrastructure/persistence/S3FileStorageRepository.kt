package com.cleanai.modules.files.infrastructure.persistence

import com.cleanai.libs.exception.InfrastructureException
import com.cleanai.modules.files.domain.File
import com.cleanai.modules.files.domain.FileStorageRepository
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.Duration

class S3FileStorageRepository(
    private val s3Client: S3Client,
    private val bucketName: String
) : FileStorageRepository {

    override fun storeFile(file: File) {
        try {
            val request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(file.storagePath)
                .metadata(
                    mapOf(
                        "originalName" to file.name,
                        "mimeType" to file.mimeType,
                        "sizeByte" to file.sizeByte.toString()
                    )
                )
                .contentType(file.mimeType)
                .build()


            s3Client.putObject(request, file.tempPath!!)
        } catch (e: Exception) {
            throw InfrastructureException("Failed to store file in S3: ${e.message}", e)
        }
    }

    override fun getDownloadUrl(storagePath: String): String {
        try {
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(storagePath)
                .build()


            val s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build()

            val presignerBuilder = S3Presigner.builder()
                .serviceConfiguration(s3Configuration)

            val presigner = presignerBuilder.build()

            val presignedRequest = presigner.presignGetObject {
                it.getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(10))
            }

            return presignedRequest.url().toString()
        } catch (e: Exception) {
            throw InfrastructureException("Failed to generate download URL: ${e.message}", e)
        }
    }

    override fun getFileContent(storagePath: String): ByteArray {
        try {
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(storagePath)
                .build()

            val response = s3Client.getObject(getObjectRequest)
            return response.readAllBytes()
        } catch (e: Exception) {
            throw InfrastructureException("Failed to get file content from S3: ${e.message}", e)
        }
    }

    override fun moveFile(currentPath: String, targetPath: String) {
        try {
            // Copy object to new location
            val copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(currentPath)
                .destinationBucket(bucketName)
                .destinationKey(targetPath)
                .build()

            s3Client.copyObject(copyRequest)

            // Delete original object
            val deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(currentPath)
                .build()

            s3Client.deleteObject(deleteRequest)
        } catch (e: Exception) {
            throw InfrastructureException("Failed to move file from $currentPath to $targetPath: ${e.message}", e)
        }
    }
}

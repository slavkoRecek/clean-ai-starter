package com.cleanai.modules.files.domain

import com.cleanai.libs.exception.ObjectNotFoundException
import com.cleanai.libs.exception.UnauthorizedAccessException
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.LoggerFactory
import java.util.UUID


@ApplicationScoped
class FileService(
    private val dbRepository: FileDbRepository,
    private val fileStorageRepository: FileStorageRepository,
) {
    private val log = LoggerFactory.getLogger(FileService::class.java)
    fun uploadFile(userId: String, uploadRequest: FileUploadRequest): FileResponse {
        val file = File.create(userId, uploadRequest)
        dbRepository.persist(file)
        fileStorageRepository.storeFile(file)

        return FileResponse(
            file = file,
            downloadUrl = fileStorageRepository.getDownloadUrl(file.storagePath)
        )
    }

    fun getFile(userId: String, id: UUID): FileResponse {
        val file = dbRepository.findById(id)
            ?: throw ObjectNotFoundException("File with id $id not found")
        if (file.userId != userId) {
            throw UnauthorizedAccessException("User $userId is not allowed to access file $id")
        }

        return FileResponse(
            file = file,
            downloadUrl = fileStorageRepository.getDownloadUrl(file.storagePath)
        )
    }

    fun getFileContent(userId: String, id: UUID): ByteArray {
        val file = dbRepository.findById(id)
            ?: throw ObjectNotFoundException("File with id $id not found")
        if (file.userId != userId) {
            throw UnauthorizedAccessException("User $userId is not allowed to access file $id")
        }

        return fileStorageRepository.getFileContent(file.storagePath)
    }

    fun getFileMetadata(userId: String, id: UUID): File {
        val file = dbRepository.findById(id)
            ?: throw ObjectNotFoundException("File with id $id not found")

        if (file.userId != userId) {
            throw UnauthorizedAccessException("User $userId is not allowed to access file $id")
        }

        return file
    }
}

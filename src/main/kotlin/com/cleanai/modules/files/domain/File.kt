package com.cleanai.modules.files.domain

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.*

data class File(
    val id: UUID,
    val userId: String,
    val name: String,
    val storagePath: String,
    val mimeType: String,
    val sizeByte: Double,
    val createdAt: Instant = Instant.now(),
    val tempPath: Path? = null
) {
    companion object {
        fun create(userId: String, request: FileUploadRequest): File {
            val id = UUID.randomUUID()
            val path = request.path
            return File(
                id = id,
                userId = userId,
                name = request.name,
                storagePath = this.dateBasedStoragePath(id, request.name),
                mimeType = this.mimeType(path),
                sizeByte = this.sizeByte(path),
                tempPath = path
            )
        }

        private fun sizeByte(paths: Path): Double {
            return Files.size(paths).toDouble()
        }

        private fun mimeType(path: Path): String {
            Files.probeContentType(path)?.let { return it }
            return "application/octet-stream"
        }

        private fun dateBasedStoragePath(uUID: UUID, fileName: String): String {
            return "uploaded/${uUID}/${fileName}"
        }
    }
}

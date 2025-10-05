package com.cleanai.modules.files.infrastructure.web

import com.cleanai.modules.files.domain.FileResponse
import kotlinx.serialization.Serializable

@Serializable
data class FileDto(
    val id: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Double,
    val downloadUrl: String,
) {
    companion object {
        fun fromDomain(fileResponse: FileResponse): FileDto {
            return FileDto(
                id = fileResponse.file.id.toString(),
                fileName = fileResponse.file.name,
                mimeType = fileResponse.file.mimeType,
                sizeBytes = fileResponse.file.sizeByte,
                downloadUrl = fileResponse.downloadUrl
            )
        }
    }
}

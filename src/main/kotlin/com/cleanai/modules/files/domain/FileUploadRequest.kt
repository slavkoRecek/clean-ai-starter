package com.cleanai.modules.files.domain

import java.nio.file.Path

data class FileUploadRequest(
    val name: String,
    val path: Path,
    val mimeType: String,
    val sizeByte: Double
)


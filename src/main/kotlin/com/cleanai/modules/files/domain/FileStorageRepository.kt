package com.cleanai.modules.files.domain

interface  FileStorageRepository {
    fun storeFile(file: File)
    fun getDownloadUrl(storagePath: String): String
    fun getFileContent(storagePath: String): ByteArray
    fun moveFile(currentPath: String, targetPath: String)
}

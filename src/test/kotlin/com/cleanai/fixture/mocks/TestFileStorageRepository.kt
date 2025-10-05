package com.cleanai.fixture.mocks

import com.cleanai.modules.files.domain.File
import com.cleanai.modules.files.domain.FileStorageRepository
import io.quarkus.test.Mock
import jakarta.enterprise.context.ApplicationScoped
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Mock
@ApplicationScoped
class TestFileStorageRepository : FileStorageRepository {
    private val storagePath = Files.createTempDirectory("test-storage")
    private val files = mutableMapOf<String, Path>()

    override fun storeFile(file: File) {
        val targetPath = storagePath.resolve(file.id.toString())
        Files.copy(file.tempPath!!, targetPath, StandardCopyOption.REPLACE_EXISTING)
        files[file.storagePath] = targetPath
    }


    override fun getDownloadUrl(storagePath: String): String {
        return "file://${files[storagePath]?.toAbsolutePath() ?: throw IllegalArgumentException("File not found")}"
    }

    override fun getFileContent(storagePath: String): ByteArray {
        val filePath = files[storagePath] ?: throw IllegalArgumentException("File not found: $storagePath")
        return Files.readAllBytes(filePath)
    }

    override fun moveFile(currentPath: String, targetPath: String) {
        val currentFile = files[currentPath] ?: throw IllegalArgumentException("Source file not found: $currentPath")
        
        // Create target directory if it doesn't exist
        val targetFilePath = storagePath.resolve(targetPath.replace("/", "_"))
        Files.copy(currentFile, targetFilePath, StandardCopyOption.REPLACE_EXISTING)
        
        // Update file mapping
        files[targetPath] = targetFilePath
        files.remove(currentPath)
        
        // Delete original file
        Files.deleteIfExists(currentFile)
    }

    // Optional: Cleanup method that can be called after tests
    fun cleanup() {
        files.values.forEach { Files.deleteIfExists(it) }
        Files.deleteIfExists(storagePath)
    }
}

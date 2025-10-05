package com.cleanai.modules.files.infrastructure.persistence

import com.cleanai.modules.files.domain.File
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "files")
class FileEntity : PanacheEntityBase {
    companion object : PanacheCompanionBase<FileEntity, UUID> {
        fun from(domain: File) = FileEntity().apply {
            id = domain.id
            userId = domain.userId
            name = domain.name
            storagePath = domain.storagePath
            mimeType = domain.mimeType
            sizeByte = domain.sizeByte
            createdAt = domain.createdAt
        }
    }

    @Id
    @Column(columnDefinition = "uuid")
    var id: UUID? = null

    @Column(name = "user_id", nullable = false)
    var userId: String? = null

    @Column(name = "name", nullable = false)
    var name: String? = null

    @Column(name = "storage_path", nullable = false)
    var storagePath: String? = null

    @Column(name = "mime_type", nullable = false)
    var mimeType: String? = null

    @Column(name = "size_byte", columnDefinition = "numeric(10,2)")
    var sizeByte: Double? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant? = null

    fun toDomain(): File {
        return File(
            id = id!!,
            userId = userId!!,
            name = name!!,
            storagePath = storagePath!!,
            mimeType = mimeType!!,
            sizeByte = sizeByte!!,
            createdAt = createdAt!!
        )
    }
}

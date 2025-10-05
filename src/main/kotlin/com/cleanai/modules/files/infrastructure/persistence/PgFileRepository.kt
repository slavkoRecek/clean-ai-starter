package com.cleanai.modules.files.infrastructure.persistence

import com.cleanai.modules.files.domain.File
import com.cleanai.modules.files.domain.FileDbRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

@ApplicationScoped
class PgFileRepository: FileDbRepository {

    @Transactional
    override fun persist(file: File): File =
        FileEntity.from(file).run {
            persistAndFlush()
            toDomain()
        }

    override fun findById(id: UUID): File? {
        return FileEntity.findById(id)?.toDomain()
    }
}

package com.cleanai.modules.files.domain

import java.util.*

interface FileDbRepository {
    fun persist(file: File): File
    fun findById(id: UUID): File?
}

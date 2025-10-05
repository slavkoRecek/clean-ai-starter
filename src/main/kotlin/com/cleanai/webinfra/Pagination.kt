package com.cleanai.webinfra

import com.cleanai.libs.pagination.Page
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.QueryParam
import kotlinx.serialization.Serializable

class PaginationParams {
    @QueryParam("page")
    @DefaultValue("0")
    var page: Int = 0
        private set

    @QueryParam("size")
    @DefaultValue("20")
    var size: Int = 20
        private set

    init {
        require(page >= 0) { "Page must be greater than or equal to 0" }
        require(size > 0) { "Size must be greater than 0" }
        require(size <= 100) { "Size must not exceed 100" }
    }
}

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean
) {
    companion object {
        fun <T, R> fromDomain(
            page: Page<T>,
            transform: (T) -> R
        ): PageResponse<R> {
            val totalPages = if (page.totalElements % page.size == 0L) {
                (page.totalElements / page.size).toInt()
            } else {
                (page.totalElements / page.size + 1).toInt()
            }

            return PageResponse(
                content = page.content.map(transform),
                page = page.page,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = totalPages,
                isFirst = page.page == 0,
                isLast = page.page >= totalPages - 1
            )
        }
    }
}

package com.app.pose.ui.feature.library

import com.app.pose.domain.model.Category
import com.app.pose.domain.model.Exercise

data class LibraryUiState(
    val query: String = "",
    val selectedCategory: Category = Category.ALL,
    val exercises: List<Exercise> = emptyList(),
    val totalCount: Int = 0
)

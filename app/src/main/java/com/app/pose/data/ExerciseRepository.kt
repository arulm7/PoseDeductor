package com.app.pose.data

import com.app.pose.data.mock.MockExercises
import com.app.pose.domain.model.Category
import com.app.pose.domain.model.Exercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExerciseRepository {
    private val _exercises = MutableStateFlow(MockExercises.exerciseList)
    val exercises: Flow<List<Exercise>> = _exercises.asStateFlow()

    fun getAllExercises(): List<Exercise> = _exercises.value

    fun getExerciseById(id: String): Exercise? {
        return _exercises.value.find { it.id == id } ?: _exercises.value.firstOrNull()
    }

    fun getExercisesByCategory(category: Category): List<Exercise> {
        return if (category == Category.ALL) {
            _exercises.value
        } else {
            _exercises.value.filter { it.categories.contains(category) }
        }
    }

    fun searchExercises(query: String, category: Category = Category.ALL): List<Exercise> {
        val q = query.trim().lowercase()
        return _exercises.value.filter { ex ->
            val matchesCategory = category == Category.ALL || ex.categories.contains(category)
            val matchesQuery = q.isEmpty() ||
                    ex.name.lowercase().contains(q) ||
                    ex.muscles.any { it.lowercase().contains(q) }
            matchesCategory && matchesQuery
        }
    }
}

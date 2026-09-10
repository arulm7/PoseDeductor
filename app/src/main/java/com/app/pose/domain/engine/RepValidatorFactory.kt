package com.app.pose.domain.engine

object RepValidatorFactory {
    fun createValidator(exerciseId: String): ExerciseRepValidator {
        return when (exerciseId.lowercase()) {
            "squat", "squats" -> SquatRepValidator()
            else -> SquatRepValidator() // Default to SquatRepValidator for now
        }
    }
}

package com.app.pose.domain.model

enum class Difficulty(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

enum class Category(val label: String) {
    ALL("All"),
    STRENGTH("Strength"),
    UPPER_BODY("Upper Body"),
    LOWER_BODY("Lower Body"),
    CORE("Core"),
    FULL_BODY("Full Body")
}

data class Mistake(
    val title: String,
    val detail: String
)

data class Exercise(
    val id: String,
    val name: String,
    val categories: List<Category>,
    val difficulty: Difficulty,
    val muscles: List<String>,
    val durationMin: Int,
    val targetReps: Int,
    val summary: String,
    val instructions: List<String>,
    val checkpoints: List<String>,
    val mistakes: List<Mistake>,
    val cues: List<String>,
    val iconEmoji: String = "🏋️"
)

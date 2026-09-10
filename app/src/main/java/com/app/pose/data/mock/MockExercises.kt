package com.app.pose.data.mock

import com.app.pose.domain.model.Category
import com.app.pose.domain.model.Difficulty
import com.app.pose.domain.model.Exercise
import com.app.pose.domain.model.Mistake

object MockExercises {
    val exerciseList: List<Exercise> = listOf(
        Exercise(
            id = "squat",
            name = "Squat",
            categories = listOf(Category.STRENGTH, Category.LOWER_BODY, Category.FULL_BODY),
            difficulty = Difficulty.BEGINNER,
            muscles = listOf("Quads", "Glutes", "Core"),
            durationMin = 4,
            targetReps = 12,
            summary = "The foundation of lower-body strength. Coach AI watches your depth, knee tracking and torso angle on every rep.",
            instructions = listOf(
                "Stand with feet shoulder-width apart, toes turned slightly out.",
                "Push your hips back and bend your knees as if sitting into a chair.",
                "Descend until your thighs are roughly parallel to the floor.",
                "Drive through your mid-foot to stand back up and squeeze your glutes."
            ),
            checkpoints = listOf(
                "Keep your chest upright",
                "Keep knees aligned with your feet",
                "Lower until your thighs are approximately parallel",
                "Keep your heels planted on the floor"
            ),
            mistakes = listOf(
                Mistake("Knees caving inward", "Push your knees out in line with your second toe."),
                Mistake("Rounding the lower back", "Brace your core and keep a neutral spine throughout."),
                Mistake("Cutting the depth short", "Aim for thighs parallel before driving back up.")
            ),
            cues = listOf(
                "Keep your knees aligned",
                "Keep your back straighter",
                "Go slightly lower",
                "Slow down",
                "Maintain your balance"
            ),
            iconEmoji = "🏋️"
        ),
        Exercise(
            id = "pushup",
            name = "Push-up",
            categories = listOf(Category.STRENGTH, Category.UPPER_BODY),
            difficulty = Difficulty.INTERMEDIATE,
            muscles = listOf("Chest", "Triceps", "Core"),
            durationMin = 3,
            targetReps = 10,
            summary = "A full upper-body press. Coach AI checks elbow flare, hip line and lockout depth.",
            instructions = listOf(
                "Set your hands slightly wider than shoulder-width.",
                "Brace your core so hips, shoulders and heels form one line.",
                "Lower your chest to just above the floor.",
                "Press back up until your elbows are straight."
            ),
            checkpoints = listOf(
                "Keep your body in one straight line",
                "Keep your elbows close to your ribs",
                "Lower your chest to fist height"
            ),
            mistakes = listOf(
                Mistake("Sagging hips", "Squeeze glutes and brace the core."),
                Mistake("Flaring elbows", "Keep elbows at about 45°."),
                Mistake("Half reps", "Lower with control to full depth.")
            ),
            cues = listOf(
                "Keep your elbows closer",
                "Keep your back straighter",
                "Go slightly lower",
                "Slow down"
            ),
            iconEmoji = "💪"
        ),
        Exercise(
            id = "lunge",
            name = "Lunge",
            categories = listOf(Category.STRENGTH, Category.LOWER_BODY),
            difficulty = Difficulty.BEGINNER,
            muscles = listOf("Quads", "Glutes", "Balance"),
            durationMin = 5,
            targetReps = 16,
            summary = "Single-leg strength and stability. Coach AI tracks your balance and front-knee angle.",
            instructions = listOf(
                "Step forward about two foot-lengths.",
                "Lower your back knee toward the floor.",
                "Keep your torso tall and your weight in the front heel.",
                "Push back to standing and alternate sides."
            ),
            checkpoints = listOf(
                "Keep your torso upright",
                "Front knee stacked over the ankle",
                "Lower until both knees reach 90°"
            ),
            mistakes = listOf(
                Mistake("Leaning forward", "Keep your chest proud and tall."),
                Mistake("Narrow stance", "Step wider to stay balanced."),
                Mistake("Knee past toes", "Shift weight into the front heel.")
            ),
            cues = listOf(
                "Maintain your balance",
                "Keep your torso upright",
                "Go slightly lower",
                "Slow down"
            ),
            iconEmoji = "🦵"
        ),
        Exercise(
            id = "curl",
            name = "Bicep Curl",
            categories = listOf(Category.STRENGTH, Category.UPPER_BODY),
            difficulty = Difficulty.BEGINNER,
            muscles = listOf("Biceps", "Forearms"),
            durationMin = 3,
            targetReps = 12,
            summary = "Isolated arm work. Coach AI flags swinging and incomplete range of motion.",
            instructions = listOf(
                "Stand tall with a dumbbell in each hand, palms forward.",
                "Curl the weights toward your shoulders.",
                "Pause at the top, then lower under control."
            ),
            checkpoints = listOf(
                "Keep your elbows pinned to your sides",
                "Avoid swinging your torso",
                "Lower all the way down each rep"
            ),
            mistakes = listOf(
                Mistake("Using momentum", "Slow the lowering phase to 2 seconds."),
                Mistake("Elbows drifting", "Keep upper arms still and vertical."),
                Mistake("Partial range", "Fully extend at the bottom.")
            ),
            cues = listOf(
                "Keep your elbows closer",
                "Slow down",
                "Keep your back straighter"
            ),
            iconEmoji = "🦾"
        ),
        Exercise(
            id = "press",
            name = "Shoulder Press",
            categories = listOf(Category.STRENGTH, Category.UPPER_BODY),
            difficulty = Difficulty.INTERMEDIATE,
            muscles = listOf("Delts", "Triceps", "Core"),
            durationMin = 4,
            targetReps = 10,
            summary = "Overhead pressing power. Coach AI monitors your rib flare and lockout symmetry.",
            instructions = listOf(
                "Hold dumbbells at shoulder height, palms forward.",
                "Brace your core and press straight overhead.",
                "Lower with control back to shoulder height."
            ),
            checkpoints = listOf(
                "Keep your ribs down and core braced",
                "Press in a straight vertical line",
                "Finish with both arms fully locked out"
            ),
            mistakes = listOf(
                Mistake("Arching the back", "Squeeze glutes to stay stacked."),
                Mistake("Uneven lockout", "Match the height of both arms."),
                Mistake("Pressing forward", "Keep the bar path vertical.")
            ),
            cues = listOf(
                "Keep your back straighter",
                "Maintain your balance",
                "Slow down"
            ),
            iconEmoji = "🏋️‍♀️"
        ),
        Exercise(
            id = "plank",
            name = "Plank",
            categories = listOf(Category.CORE, Category.FULL_BODY),
            difficulty = Difficulty.BEGINNER,
            muscles = listOf("Core", "Shoulders"),
            durationMin = 2,
            targetReps = 3,
            summary = "An isometric hold. Coach AI measures your hip line and holds the timer only while form is clean.",
            instructions = listOf(
                "Place forearms on the floor under your shoulders.",
                "Extend your legs and lift your hips into a straight line.",
                "Hold, breathing steadily."
            ),
            checkpoints = listOf(
                "Hips level with shoulders",
                "Neck long and neutral",
                "Squeeze glutes and quads"
            ),
            mistakes = listOf(
                Mistake("Hips too high", "Lower until your body is one line."),
                Mistake("Sagging low back", "Brace as if bracing for a punch."),
                Mistake("Holding your breath", "Breathe steadily through the hold.")
            ),
            cues = listOf(
                "Keep your back straighter",
                "Maintain your balance"
            ),
            iconEmoji = "🧘"
        ),
        Exercise(
            id = "calf-raise",
            name = "Calf Raise",
            categories = listOf(Category.STRENGTH, Category.LOWER_BODY),
            difficulty = Difficulty.BEGINNER,
            muscles = listOf("Calves", "Ankles"),
            durationMin = 2,
            targetReps = 20,
            summary = "Ankle strength and control. Coach AI counts only reps with a full pause at the top.",
            instructions = listOf(
                "Stand tall with feet hip-width apart.",
                "Rise onto the balls of your feet.",
                "Pause at the top, then lower slowly."
            ),
            checkpoints = listOf(
                "Rise as high as you can",
                "Pause one second at the top",
                "Lower slowly to a full stretch"
            ),
            mistakes = listOf(
                Mistake("Bouncing", "Control both directions of the rep."),
                Mistake("Short range", "Get all the way onto your toes."),
                Mistake("Rolling ankles out", "Keep pressure on the big toe.")
            ),
            cues = listOf(
                "Slow down",
                "Maintain your balance",
                "Go slightly higher"
            ),
            iconEmoji = "🦶"
        ),
        Exercise(
            id = "glute-bridge",
            name = "Glute Bridge",
            categories = listOf(Category.CORE, Category.LOWER_BODY),
            difficulty = Difficulty.BEGINNER,
            muscles = listOf("Glutes", "Hamstrings"),
            durationMin = 3,
            targetReps = 15,
            summary = "Hip extension without load. Coach AI checks lockout height and pelvis alignment.",
            instructions = listOf(
                "Lie on your back, knees bent, feet flat.",
                "Drive through your heels to lift your hips.",
                "Squeeze at the top, then lower with control."
            ),
            checkpoints = listOf(
                "Squeeze glutes at the top",
                "Keep ribs down, avoid arching",
                "Knees stay in line with your feet"
            ),
            mistakes = listOf(
                Mistake("Arching the back", "Lift with the glutes, not the spine."),
                Mistake("Pushing through toes", "Drive through the heels."),
                Mistake("Rushing reps", "Pause for one second at the top.")
            ),
            cues = listOf(
                "Keep your back straighter",
                "Slow down",
                "Maintain your balance"
            ),
            iconEmoji = "🔥"
        )
    )
}

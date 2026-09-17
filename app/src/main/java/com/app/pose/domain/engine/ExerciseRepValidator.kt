package com.app.pose.domain.engine

import com.app.pose.domain.classifier.PoseClassificationResult
import com.app.pose.domain.model.PosePoint

interface ExerciseRepValidator {
    fun processFrame(
        landmarks: List<PosePoint>,
        tflitePrediction: PoseClassificationResult?,
        timestampMs: Long
    ): RepValidationResult

    fun reset()
}

package com.app.pose.domain.analysis

import com.app.pose.domain.model.PosePoint

interface ExerciseAnalyzer<T> {
    fun analyze(landmarks: List<PosePoint>, timestampMs: Long): T
    fun reset()
}

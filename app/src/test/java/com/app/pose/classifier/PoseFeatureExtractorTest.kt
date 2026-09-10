package com.app.pose.classifier

import com.app.pose.domain.classifier.PoseFeatureExtractor
import com.app.pose.domain.classifier.PoseFeatureScaler
import com.app.pose.domain.model.PosePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class PoseFeatureExtractorTest {

    @Test
    fun testFeatureScalerConstants() {
        assertEquals(170, PoseFeatureScaler.MEAN.size)
        assertEquals(170, PoseFeatureScaler.SCALE.size)

        for (i in 0 until 170) {
            assertFalse("Scaler mean should not be NaN at $i", PoseFeatureScaler.MEAN[i].isNaN())
            assertFalse("Scaler scale should not be NaN or zero at $i", PoseFeatureScaler.SCALE[i].isNaN() || PoseFeatureScaler.SCALE[i] == 0f)
        }
    }

    @Test
    fun testExtract170Features_producesExactVectorSize() {
        val mockLandmarks = List(33) { i ->
            PosePoint(
                x = 0.5f + (i * 0.01f),
                y = 0.4f + (i * 0.015f),
                z = 0.05f - (i * 0.002f),
                visibility = 0.95f
            )
        }

        val features = PoseFeatureExtractor.extract170Features(mockLandmarks)

        assertNotNull(features)
        assertEquals(170, features.size)

        for (i in features.indices) {
            assertFalse("Feature at index $i is NaN", features[i].isNaN())
            assertFalse("Feature at index $i is Infinite", features[i].isInfinite())
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExtractFeatures_throwsWhenInsufficientLandmarks() {
        val incompleteLandmarks = List(20) { PosePoint(0.5f, 0.5f) }
        PoseFeatureExtractor.extract170Features(incompleteLandmarks)
    }
}

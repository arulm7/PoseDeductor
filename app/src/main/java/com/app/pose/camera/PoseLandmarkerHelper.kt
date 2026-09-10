package com.app.pose.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.app.pose.domain.model.PosePoint
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkerHelper(
    private val context: Context,
    private val onPoseDetected: (List<PosePoint>, Long) -> Unit,
    private val onError: (String) -> Unit = {}
) {
    private var poseLandmarker: PoseLandmarker? = null

    init {
        setupPoseLandmarker()
    }

    private fun setupPoseLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_lite.task")
                .setDelegate(Delegate.CPU)
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result: PoseLandmarkerResult, _: MPImage ->
                    val landmarksList = result.landmarks()
                    if (landmarksList.isNotEmpty()) {
                        val firstPose = landmarksList[0]
                        val posePoints = firstPose.map { landmark ->
                            PosePoint(
                                x = landmark.x(),
                                y = landmark.y(),
                                z = landmark.z(),
                                visibility = landmark.visibility().orElse(1f)
                            )
                        }
                        onPoseDetected(posePoints, result.timestampMs())
                    } else {
                        onPoseDetected(emptyList(), result.timestampMs())
                    }
                }
                .setErrorListener { error: RuntimeException ->
                    onError(error.message ?: "Pose landmarker error")
                }
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            onError(e.message ?: "Failed to initialize PoseLandmarker")
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun detectLiveStream(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val landmarker = poseLandmarker
        if (landmarker == null) {
            imageProxy.close()
            return
        }

        try {
            val bitmap = imageProxy.toBitmap()
            val rotation = imageProxy.imageInfo.rotationDegrees

            val matrix = Matrix().apply {
                postRotate(rotation.toFloat())
                if (isFrontCamera) {
                    postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            val frameTime = SystemClock.uptimeMillis()

            landmarker.detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            onError(e.message ?: "Error processing camera frame")
        } finally {
            imageProxy.close()
        }
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}

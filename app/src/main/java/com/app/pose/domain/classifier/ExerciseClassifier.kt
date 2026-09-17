package com.app.pose.domain.classifier

import android.content.Context
import android.util.Log
import com.app.pose.domain.model.PosePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

enum class ExercisePoseClass(val id: Int, val label: String) {
    JUMPING_JACKS_DOWN(0, "jumping_jacks_down"),
    JUMPING_JACKS_UP(1, "jumping_jacks_up"),
    PULLUPS_DOWN(2, "pullups_down"),
    PULLUPS_UP(3, "pullups_up"),
    PUSHUPS_DOWN(4, "pushups_down"),
    PUSHUPS_UP(5, "pushups_up"),
    SITUP_DOWN(6, "situp_down"),
    SITUP_UP(7, "situp_up"),
    SQUATS_DOWN(8, "squats_down"),
    SQUATS_UP(9, "squats_up");

    companion object {
        fun fromId(id: Int): ExercisePoseClass = entries.find { it.id == id } ?: SQUATS_UP
    }
}

data class PoseClassificationResult(
    val topClass: ExercisePoseClass,
    val topConfidence: Float,
    val secondClass: ExercisePoseClass,
    val secondConfidence: Float,
    val probabilities: FloatArray
)

class ExerciseClassifier(private val context: Context) : AutoCloseable {

    companion object {
        private const val TAG = "ExerciseClassifier"
        private const val MODEL_FILENAME = "exercise_classifier.tflite"
        const val NUM_FEATURES = 170
        const val NUM_CLASSES = 10
    }

    private var interpreter: Interpreter? = null

    init {
        try {
            val modelBuffer = loadModelFile(context, MODEL_FILENAME)
            val options = Interpreter.Options().apply {
                setNumThreads(2)
            }
            interpreter = Interpreter(modelBuffer, options)

            val inputTensor = interpreter?.getInputTensor(0)
            val outputTensor = interpreter?.getOutputTensor(0)

            Log.i(
                TAG,
                "ExerciseClassifier initialized successfully. " +
                        "Input shape: ${inputTensor?.shape()?.contentToString()}, dataType: ${inputTensor?.dataType()}. " +
                        "Output shape: ${outputTensor?.shape()?.contentToString()}, dataType: ${outputTensor?.dataType()}."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load TFLite model from assets/$MODEL_FILENAME", e)
        }
    }

    suspend fun classify(landmarks: List<PosePoint>): PoseClassificationResult? = withContext(Dispatchers.Default) {
        if (landmarks.size < 33 || interpreter == null) {
            return@withContext null
        }

        try {
            // Extract exact 170 standardized features
            val features170 = PoseFeatureExtractor.extract170Features(landmarks)

            val input = Array(1) { features170 }
            val output = Array(1) { FloatArray(NUM_CLASSES) }

            interpreter?.run(input, output)

            val probs = output[0]

            // Find top and second-best classes
            var topIdx = 0
            var topConf = -1f
            var secondIdx = 0
            var secondConf = -1f

            for (i in 0 until NUM_CLASSES) {
                val conf = probs[i]
                if (conf > topConf) {
                    secondIdx = topIdx
                    secondConf = topConf
                    topIdx = i
                    topConf = conf
                } else if (conf > secondConf) {
                    secondIdx = i
                    secondConf = conf
                }
            }

            val topClass = ExercisePoseClass.fromId(topIdx)
            val secondClass = ExercisePoseClass.fromId(secondIdx)

            Log.d(
                TAG,
                "Prediction -> 1st: ${topClass.label} (${"%.1f".format(topConf * 100)}%), " +
                        "2nd: ${secondClass.label} (${"%.1f".format(secondConf * 100)}%)"
            )

            PoseClassificationResult(
                topClass = topClass,
                topConfidence = topConf,
                secondClass = secondClass,
                secondConfidence = secondConf,
                probabilities = probs
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during pose classification inference", e)
            null
        }
    }

    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun close() {
        try {
            interpreter?.close()
            interpreter = null
            Log.i(TAG, "ExerciseClassifier interpreter closed and resources released.")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ExerciseClassifier interpreter", e)
        }
    }
}

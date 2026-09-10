package com.app.pose.classifier

import android.app.Application
import com.app.pose.ui.feature.camera.CameraWorkoutViewModel
import org.junit.Assert.assertNotNull
import org.junit.Test

class CameraWorkoutViewModelTest {

    @Test
    fun testCameraWorkoutViewModel_hasApplicationConstructorForViewModelProvider() {
        // Verify that Java reflection can find the 1-parameter (Application) constructor
        // required by ViewModelProvider.AndroidViewModelFactory
        val constructor = CameraWorkoutViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull("Constructor CameraWorkoutViewModel(Application) must exist", constructor)
    }
}

package com.app.pose.domain.analysis

import com.app.pose.domain.model.PosePoint
import kotlin.math.abs
import kotlin.math.atan2

object JointAngleCalculator {

    /**
     * Calculates the angle in degrees (0..180) at joint [b] formed by rays BA and BC.
     * @param a First point (e.g., Hip)
     * @param b Vertex / Joint point (e.g., Knee)
     * @param c Third point (e.g., Ankle)
     */
    fun calculateAngle(a: PosePoint, b: PosePoint, c: PosePoint): Float {
        val radians = atan2(c.y - b.y, c.x - b.x) - atan2(a.y - b.y, a.x - b.x)
        var angle = abs(radians * 180.0 / Math.PI).toFloat()

        if (angle > 180f) {
            angle = 360f - angle
        }

        return angle
    }

    /**
     * Calculates the angle in degrees (0..90) of a segment (e.g., Shoulder -> Hip) relative to the vertical axis.
     * 0 deg means perfectly vertical, 90 deg means horizontal.
     */
    fun calculateVerticalAngle(top: PosePoint, bottom: PosePoint): Float {
        val dx = abs(top.x - bottom.x)
        val dy = abs(top.y - bottom.y)
        val radians = atan2(dx, dy)
        return (radians * 180.0 / Math.PI).toFloat()
    }
}

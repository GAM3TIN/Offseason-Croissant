package frc.robot.vision

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
// import edu.wpi.first.wpilibj.command.Subsystem
import frc.robot.subsystems.drive.DriveSubsystem
// import frc.robot.Network
// import frc.robot.subsystems.DriveTrain
// import org.ghrobotics.frc2019.?Constants
// import org.ghrobotics.frc2019.subsystems.drive.DriveSubsystem
import org.ghrobotics.lib.debug.LiveDashboard
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.units.*
// import org.ghrobotics.lib.mathematics.units.Rotation2d
import org.ghrobotics.lib.wrappers.networktables.get
import org.team5940.pantry.lib.Updatable

object TargetTracker : Updatable {

    private val targets = mutableSetOf<TrackedTarget>()

    val kGson = Gson()
    private val visionTargetEntry = LiveDashboard.liveDashboardTable["visionTargets"]

    private var visionTargets: List<Pose2d> = listOf()
        set(value) {
            visionTargetEntry.setStringArray(
                    value.map {
                        jsonObject(
                                "x" to it.translation.x.meters,
                                "y" to it.translation.y.meters,
                                "angle" to it.rotation.degrees
                        ).toString()
                    }.toTypedArray()
            )
            field = value
        }

    override fun update() {
        synchronized(targets) {
            val currentTime = Timer.getFPGATimestamp()

            val currentRobotPose = DriveSubsystem.robotPosition

            // Update and remove old targets
            targets.removeIf {
                it.update(currentTime, currentRobotPose)
                !it.isAlive
            }
            // Publish to dashboard
            visionTargets = targets.asSequence()
                    .filter { it.isReal }
                    .map { it.averagedPose2d }
                    .toList()
        }
    }

    fun addSamples(creationTime: Double, samples: Iterable<Pose2d>) {
        if (creationTime >= Timer.getFPGATimestamp()) return // Cannot predict the future

        synchronized(targets) {
            for (samplePose in samples) {
                val closestTarget = targets.minBy {
                    it.averagedPose2d.translation.getDistance(samplePose.translation)
                }
                val sample = TrackedTargetSample(creationTime, samplePose)
                if (closestTarget == null ||
                        closestTarget.averagedPose2d.translation.getDistance(samplePose.translation) > kTargetTrackingDistanceErrorTolerance.value
                ) {
                    // Create new target if no targets are within tolerance
                    targets += TrackedTarget(sample)
                } else {
                    // Add sample to target within tolerance
                    closestTarget.addSample(sample)
                }
            }
        }
    }

    /**
     * Find the target that's closest to the robot per it's averagedPose2dRelativeToBot
     */
    fun getBestTarget(isFrontTarget: Boolean) = synchronized(targets) {
        targets.asSequence()
                .filter {
                    if (!it.isReal) return@filter false
                    val x = it.averagedPose2dRelativeToBot.translation.x
                    if (isFrontTarget) x.meters >= 0.meters else x.meters <= 0.meters
                }.minBy { it.averagedPose2dRelativeToBot.translation.norm }
    }

    fun getBestTargetUsingReference(referencePose: Pose2d, isFrontTarget: Boolean) = synchronized(targets) {
        targets.asSequence()
                .associateWith { it.averagedPose2d.relativeTo(referencePose) }
                .filter {
                    val x = it.value.translation.x
                    it.key.isReal && if (isFrontTarget) x > 0.0 else x < 0.0
                }
                .minBy { it.value.translation.norm }?.key
    }

    fun getAbsoluteTarget(translation2d: Translation2d) = synchronized(targets) {
        targets.asSequence()
                .filter {
                    it.isReal &&
                            translation2d.getDistance(it.averagedPose2d.translation) <= kTargetTrackingDistanceErrorTolerance.value
                }
                .minBy { it.averagedPose2d.translation.getDistance(translation2d) }
    }

    class TrackedTarget(
        initialTargetSample: TrackedTargetSample
    ) {

        private val samples = mutableSetOf<TrackedTargetSample>()

        override fun toString(): String {
            return "Pose $averagedPose2d isAlive? $isAlive isReal? $isReal"
        }

        /**
         * The averaged pose2d for x time
         */
        var averagedPose2d = initialTargetSample.targetPose
            private set

        var averagedPose2dRelativeToBot = Pose2d()
            private set

        /**
         * Targets will be "alive" when it has at least one data point for x time
         */
        var isAlive = true
            private set

        /**
         * Target will become a "real" target once it has received data points for x time
         */
        var isReal = false
            private set

        var stability = 0.0
            private set

        init {
            addSample(initialTargetSample)
        }

        fun addSample(newSamples: TrackedTargetSample) = synchronized(samples) {
            samples.add(newSamples)
        }

        fun update(currentTime: Double, currentRobotPose: Pose2d) = synchronized(samples) {
            // Remove expired samples
            samples.removeIf { currentTime - it.creationTime >= kTargetTrackingMaxLifetime.value }
            // Update State
            isAlive = samples.isNotEmpty()
            if (samples.size >= 2) isReal = true
            stability = (samples.size / (kVisionCameraFPS * kTargetTrackingMaxLifetime.value))
                    .coerceAtMost(1.0)
            // Update Averaged Pose
            var accumulatedX = 0.meters
            var accumulatedY = 0.meters
            var accumulatedAngle = 0.0
            for (sample in samples) {
                accumulatedX += sample.targetPose.translation.x.meters
                accumulatedY += sample.targetPose.translation.y.meters
                accumulatedAngle += sample.targetPose.rotation.radians
            }
            averagedPose2d = Pose2d(
                    (accumulatedX / samples.size),
                    (accumulatedY / samples.size),
                    Rotation2d(accumulatedAngle / samples.size)
            )
            averagedPose2dRelativeToBot = averagedPose2d.relativeTo(currentRobotPose)
        }
    }

    data class TrackedTargetSample(
        val creationTime: Double,
        val targetPose: Pose2d
    )

    // VISION
    const val kVisionCameraFPS = 30.0
    val kVisionCameraPing = 0.75.second
    val kVisionCameraTimeout = 2.second
    val kTargetTrackingDistanceErrorTolerance = 16.inches
    val kTargetTrackingMinLifetime = 0.1.second
    val kTargetTrackingMaxLifetime = 0.5.second
}
package frc.robot.subsystems.drive

import com.team254.lib.physics.DifferentialDrive
import frc.robot.subsystems.superstructure.Elevator
import org.ghrobotics.lib.mathematics.units.inch
import org.ghrobotics.lib.mathematics.units.inches
import org.ghrobotics.lib.mathematics.units.kFeetToMeter

class ClosedLoopChezyDriveCommand : ManualDriveCommand() {

    companion object {
        val kMaxLinearAcceleration = 10.0 * kFeetToMeter
        var lastLinearVelocity = 0.0
    }

    override fun initialize() {
        super.initialize()
        DriveSubsystem.leftMotor.master.talonSRX.configClosedloopRamp(0.12)
        DriveSubsystem.rightMotor.master.talonSRX.configClosedloopRamp(0.12)
    }

    override fun execute() {
        val curvature = rotationSource()
        var linear = -speedSource()
        val isQuickTurn = quickTurnSource()

        // limit linear speed based on elevator height, linear function with height above stowed
        val elevator = Elevator.currentState.position
        if (elevator > 32.inches) {
            // y = mx + b, see https://www.desmos.com/calculator/quelminicu
            linear *= (-0.0208108 * elevator.inch + 1.66696)
        }

        // limit linear acceleration
//        if (lastLinearVelocity + kMaxLinearAcceleration * 0.020 < linear) linear = lastLinearVelocity + kMaxLinearAcceleration * 0.020
//        if (lastLinearVelocity - kMaxLinearAcceleration * 0.020 > linear) linear = lastLinearVelocity - kMaxLinearAcceleration * 0.020

        val multiplier = if (DriveSubsystem.lowGear) 8.0 * kFeetToMeter else 12.0 * kFeetToMeter

        var wheelSpeeds = curvatureDrive(linear, curvature, isQuickTurn, maxWheelVelocity = 0.18)
        wheelSpeeds = DifferentialDrive.WheelState(wheelSpeeds.left * multiplier, wheelSpeeds.right * multiplier)

        DriveSubsystem.setWheelVelocities(wheelSpeeds)

        lastLinearVelocity = linear
    }

    override fun end(interrupted: Boolean) {
        super.end(interrupted)
        DriveSubsystem.leftMotor.master.talonSRX.configClosedloopRamp(0.0)
        DriveSubsystem.rightMotor.master.talonSRX.configClosedloopRamp(0.0)
    }
}

operator fun DifferentialDrive.WheelState.times(scaler: Double) = DifferentialDrive.WheelState(left * scaler, right * scaler)
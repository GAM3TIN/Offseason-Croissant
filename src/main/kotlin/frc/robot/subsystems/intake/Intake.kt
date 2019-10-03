package frc.robot.subsystems.intake

import kotlin.properties.Delegates
import frc.robot.Ports.kPCMID
import frc.robot.Ports.IntakePorts.CARGO_PORT
import frc.robot.Ports.IntakePorts.HATCH_PORT
import frc.robot.Ports.IntakePorts.PISTON_PORTS
import org.ghrobotics.lib.commands.FalconSubsystem
import org.ghrobotics.lib.mathematics.units.amp
import org.ghrobotics.lib.mathematics.units.milli
import org.ghrobotics.lib.mathematics.units.nativeunit.DefaultNativeUnitModel
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.motors.ctre.FalconSRX
import org.ghrobotics.lib.wrappers.FalconDoubleSolenoid
import org.ghrobotics.lib.wrappers.FalconSolenoid

object Intake : FalconSubsystem() {

    private val hatchMotor = FalconSRX(HATCH_PORT, DefaultNativeUnitModel)
    private val cargoMotor = FalconSRX(CARGO_PORT, DefaultNativeUnitModel)
    private val solenoid = FalconDoubleSolenoid(PISTON_PORTS[0], PISTON_PORTS[1], kPCMID)

    // Open and close the intake
    var wantsOpen: Boolean by Delegates.observable(false) { _, _, wantsClosed ->
        if (wantsClosed) {
            solenoid.state = FalconSolenoid.State.Forward
        } else {
            solenoid.state = FalconSolenoid.State.Reverse
        }
    }

    init {
        hatchMotor.configCurrentLimit(true, FalconSRX.CurrentLimitConfig(40.amp, 400.milli.second, 22.amp))
        cargoMotor.configCurrentLimit(true, FalconSRX.CurrentLimitConfig(35.amp, 400.milli.second, 20.amp))
        cargoMotor.talonSRX.configPeakOutputForward(0.8)
        cargoMotor.talonSRX.configPeakOutputReverse(-0.8)
    }

    var hatchMotorOutput
        get() = hatchMotor.voltageOutput / 12.0
        set(value) = hatchMotor.setVoltage(value)

    var cargoMotorOutput
        get() = cargoMotor.voltageOutput / 12.0
        set(value) = cargoMotor.setVoltage(value)
}
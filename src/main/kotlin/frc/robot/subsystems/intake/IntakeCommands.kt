@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package frc.robot.subsystems.intake

import edu.wpi.first.wpilibj.frc2.command.InstantCommand
import frc.robot.Controls
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.mathematics.units.derived.volt
import kotlin.math.abs
import kotlin.math.absoluteValue

//val intakeState = true
val closeIntake = InstantCommand(Runnable { Intake.wantsOpen = false })
val openIntake = InstantCommand(Runnable { Intake.wantsOpen = true })

class IntakeHatchCommand(val releasing: Boolean) : FalconCommand(Intake) {

    override fun initialize() {
        println("intaking hatch command")
        Intake.hatchMotorOutput = 12.volt * (if (releasing) -1 else 1)
        Intake.cargoMotorOutput = 0.volt
        Intake.wantsOpen = false
    }

    override fun end(interrupted: Boolean) {
        Intake.wantsOpen = false
        Intake.hatchMotorOutput = 0.volt
        Intake.cargoMotorOutput = 0.volt
    }
}

class IntakeCargoCommand(val releasing: Boolean) : FalconCommand(Intake) {

//    var wasOpen: Boolean = false

    override fun initialize() {
        println("${if (releasing) "releasing" else "intaking"} cargo command!")
//        wasOpen = Intake.wantsOpen
        Intake.wantsOpen = !releasing

        Intake.hatchMotorOutput = 12.volt * (if (releasing) 1 else -1)
        Intake.cargoMotorOutput = 12.volt * (if (!releasing) 1 else -1)

        super.initialize()
    }

    override fun end(interrupted: Boolean) {
        Intake.wantsOpen = false
        Intake.cargoMotorOutput = 3.volt
        Intake.hatchMotorOutput = 3.volt
        GlobalScope.launch {
            delay(500)
            Intake.cargoMotorOutput = 0.volt
            Intake.hatchMotorOutput = 0.volt
        }
        super.end(interrupted)
    }
}

class IntakeTeleopCommand : FalconCommand(Intake) {

    override fun execute() {
        val speed = -intakeSource()

        if (abs(speed) > 0.2) {
            if(Controls.intakeState) {
                println("Intake state works")
                Intake.hatchMotorOutput = (-12).volt * speed  //not changed
                Intake.wantsOpen = false
            }
            else {
                Intake.wantsOpen = true
                Intake.cargoMotorOutput = 12.volt * speed  // changed from "12"
            }
        }
        else {
            if(Controls.intakeState) {
                Intake.hatchMotorOutput = 12.volt * speed    //not changed
                Intake.wantsOpen = false
            }else {
                Intake.wantsOpen = true
                Intake.cargoMotorOutput = (-12).volt       // changed from "0"
            }
        }
    }

    override fun isFinished() = intakeSource().absoluteValue < 0.5

    override fun end(interrupted: Boolean) {
        Intake.hatchMotorOutput = 0.volt
        Intake.cargoMotorOutput = 0.volt
    }

    companion object {
        val intakeSource by lazy { Controls.driverFalconXbox.getRawAxis(5) }
    }
}

class IntakeCloseCommand : FalconCommand(Intake) {
    init {
        Intake.wantsOpen = false
    }
}

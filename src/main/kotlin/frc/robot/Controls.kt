package frc.robot

import edu.wpi.first.wpilibj.GamepadBase
import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.XboxController
import edu.wpi.first.wpilibj.frc2.command.* // ktlint-disable no-wildcard-imports
import frc.robot.auto.routines.BottomRocketRoutine2
import frc.robot.auto.routines.TestRoutine
import frc.robot.subsystems.climb.ClimbSubsystem
import frc.robot.subsystems.drive.CharacterizationCommand
import frc.robot.subsystems.drive.DriveSubsystem
import frc.robot.subsystems.drive.VisionDriveCommand
import frc.robot.subsystems.intake.IntakeCargoCommand
import frc.robot.subsystems.intake.IntakeHatchCommand
import frc.robot.subsystems.superstructure.* // ktlint-disable no-wildcard-imports
import net.bytebuddy.implementation.bind.annotation.Super
import org.ghrobotics.lib.commands.sequential
import org.ghrobotics.lib.mathematics.units.derived.degree
import org.ghrobotics.lib.mathematics.units.inch
import org.ghrobotics.lib.wrappers.hid.* // ktlint-disable no-wildcard-imports
import org.team5940.pantry.lib.Updatable

object Controls : Updatable {

    var isClimbing = false
    var intakeState = true

    private val zero = ZeroSuperStructureRoutine()

    val driverControllerLowLevel = XboxController(0)
    val driverFalconXbox = driverControllerLowLevel.mapControls {
      //  registerEmergencyMode()

//        button(kB).changeOn { isClimbing = true }
//        button(kX).changeOn { isClimbing = false }
//        button(kA).changeOn(ClimbSubsystem.fullS3ndClimbCommand)


        //Come back to, to look if it is useful
        //       button(kX).changeOn(BottomRocketRoutine2()())
//        button(kX).changeOn(CharacterizationCommand(DriveSubsystem))

        state({ driverControllerLowLevel.getRawButton(10) }) {
            intakeState = false
            //println("cargo activated")
            //test
            button(kA).changeOn(Superstructure.kCargoLow).changeOn {
                println("Cargo 1")
            } // .changeOff { Superstructure.kStowed.schedule() }
            button(kX).changeOn(Superstructure.kCargoMid).changeOn {
                println("Cargo 2")
            } // .changeOff { Superstructure.kStowed.schedule() }
            button(kY).changeOn(Superstructure.kCargoHigh).changeOn {
                println("Cargo 3")
            }// .changeOff { Superstructure.kStowed.schedule() }
            button(kB).changeOn(Superstructure.kCargoShip) // .changeOff { Superstructure.kStowed.schedule() }
            button(kStickLeft).changeOn(Superstructure.kCargoIntake)
        }

      state({ !driverControllerLowLevel.getRawButton(10) }) {
          intakeState = false
          //println("cargo activated")
          button(kA).changeOn(Superstructure.kHatchLow).changeOn {
              println("Cargo 1")
          } // .changeOff { Superstructure.kStowed.schedule() }
          button(kX).changeOn(Superstructure.kHatchMid).changeOn {
              println("Cargo 2")
          } // .changeOff { Superstructure.kStowed.schedule() }
          button(kY).changeOn(Superstructure.kHatchHigh).changeOn {
              println("Cargo 3")
          }// .changeOff { Superstructure.kStowed.schedule() }
          button(kB).changeOn(Superstructure.kStowed) // .changeOff { Superstructure.kStowed.schedule() }
          //Could Be wrong, check later
          button(kStickLeft).changeOn(Superstructure.kCargoIntake)
      }


     //    Hatch ( with option )
//        button(kB).changeOff{
//
////            println("hatch activated")
//            // hatch presets
//            button(kA).changeOn(Superstructure.kHatchLow).changeOn{
//                println("Hatch 1")
//            } // .changeOff { Superstructure.kStowed.schedule() }
//            button(kX).changeOn(Superstructure.kHatchMid).changeOn{
//                println("Hatch 2")
//
//            } // .changeOff { Superstructure.kStowed.schedule() }
//            button(kY).changeOn(Superstructure.kHatchHigh).changeOn{
//                println("Hatch 3")
//            } // .changeOff { Superstructure.kStowed.schedule() }
//            //button(kB).changeOn(Superstructure.kStowed)
//            intakeState = true
//        }
        // Stow (for now like this coz i dont wanna break anything
        pov(0).change(ClosedLoopElevatorMove{Elevator.currentState.position + 1.inch})
        pov(180).change(ClosedLoopElevatorMove{Elevator.currentState.position - 1.inch})


        //KILL Switch
        button(kStart).changeOn {
            Robot.activateEmergency()
        }


        //Gas, Backwords




        // Vision align
//            triggerAxisButton(GenericHID.Hand.kRight).change(
//                    ConditionalCommand(VisionDriveCommand(true), VisionDriveCommand(false),
//                            BooleanSupplier { !Superstructure.currentState.isPassedThrough }))
//              shifting is good
        // Shifting
        if (Constants.kIsRocketLeague) {
            button(kBumperRight).change(VisionDriveCommand(true))
//                button(kBumperRight).change(ClosedLoopVisionDriveCommand(true))
            // button(9).changeOn { DriveSubsystem.lowGear = true }.changeOff { DriveSubsystem.lowGear = false }
        } else {
            triggerAxisButton(GenericHID.Hand.kRight).change(VisionDriveCommand(true))
//                triggerAxisButton(GenericHID.Hand.kRight).change(ClosedLoopVisionDriveCommand(true))
            button(kBumperLeft).changeOn { DriveSubsystem.lowGear = true }.changeOff { DriveSubsystem.lowGear = false }
        }
        // Hab 3 climb
        state({ isClimbing }) {
            pov(270).changeOn(ClimbSubsystem.hab3ClimbCommand)
        }
        pov(90).changeOn(ClimbSubsystem.hab3prepMove).changeOn{ isClimbing = true }
    }

//    val auxXbox = XboxController(1)
//    val auxFalconXbox = auxXbox.mapControls {
//        button(kY).changeOn(ClimbSubsystem.fullS3ndClimbCommand)
//    }

    val operatorJoy = Joystick(5)
    val operatorFalconHID = operatorJoy.mapControls {

        //        button(4).changeOn(ClimbSubsystem.fullS3ndClimbCommand)


        // climbing

        // cargo presets
//            button(12).changeOn(Superstructure.kCargoIntake.andThen { Intake.wantsOpen = true }) // .changeOff { Superstructure.kStowed.schedule() }



        //elevator
        //  button(11).changeOn(ClosedLoopElevatorMove { Elevator.currentState.position - 1.inch })

        // that one passthrough preset that doesnt snap back to normal
//            button(4).changeOn(Superstructure.kBackHatchFromLoadingStation)

        // hatches
        lessThanAxisButton(1).change(IntakeHatchCommand(releasing = false))
        greaterThanAxisButton(1).change(IntakeHatchCommand(releasing = true))

        // cargo -- intake is a bit tricky, it'll go to the intake preset automatically
        // the lessThanAxisButton represents "intaking", and the greaterThanAxisButton represents "outtaking"
        val cargoCommand = sequential { +PrintCommand("running cargoCommand"); +Superstructure.kCargoIntake; +IntakeCargoCommand(releasing = false) }
        lessThanAxisButton(0).changeOff { (sequential { +ClosedLoopWristMove(40.degree) ; +Superstructure.kStowed; }).schedule() }.change(cargoCommand)
        greaterThanAxisButton(0).changeOff { }.change(IntakeCargoCommand(true))
        state({ isClimbing }) {
            button(12).changeOn(ClimbSubsystem.fullS3ndClimbCommand)
        }

        //   button(4).changeOn(ClimbSubsystem.prepMove).changeOn { isClimbing = true }

    }

    override fun update() {
        driverFalconXbox.update()
        operatorFalconHID.update()
//        auxFalconXbox.update()
    }
}

private fun Command.andThen(block: () -> Unit) = sequential { +this@andThen ; +InstantCommand(Runnable(block)) }

//private fun FalconXboxBuilder.registerEmergencyMode() {}




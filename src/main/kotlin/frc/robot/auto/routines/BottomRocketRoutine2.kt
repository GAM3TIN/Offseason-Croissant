// package frc.robot.auto.routines
//
// import frc.robot.auto.Autonomous
// import frc.robot.auto.paths.TrajectoryFactory
// import frc.robot.auto.paths.TrajectoryWaypoints
// import frc.robot.subsystems.drive.DriveSubsystem
// import frc.robot.subsystems.intake.IntakeCloseCommand
// import frc.robot.subsystems.intake.IntakeHatchCommand
// import frc.robot.subsystems.superstructure.Superstructure
// import org.ghrobotics.lib.commands.parallel
// import org.ghrobotics.lib.commands.sequential
// import org.ghrobotics.lib.mathematics.twodim.trajectory.types.duration
// import org.ghrobotics.lib.mathematics.units.* // ktlint-disable no-wildcard-imports
// import org.ghrobotics.lib.utils.withEquals
//
// class BottomRocketRoutine2 : AutoRoutine() {
//
//    private val path1 = TrajectoryFactory.sideStartReversedToRocketFPrepare
//    private val path2 = TrajectoryFactory.rocketFPrepareToRocketF
//
//    // Second path goes to the loading station to pick up a hatch panel
//    private val path3 = TrajectoryFactory.rocketFToLoadingStation
//
//    // Third path goes to the near side of the rocket
//    private val path4 = TrajectoryFactory.loadingStationToRocketN
//
//    override val duration: SIUnit<Second>
//        get() = path3.duration + path4.duration + path1.duration + path2.duration
//
//    override val routine
//        get() = sequential {
//
//            +parallel {
//                +DriveSubsystem.followTrajectory(
//                    path1,
//                    Autonomous.startingPosition.withEquals(Autonomous.StartingPositions.LEFT)
//                )
//                +sequential {
//                    +DriveSubsystem.notWithinRegion(TrajectoryWaypoints.kHabitatL1Platform)
//                    +Superstructure.kHatchLow
//                }.withTimeout(4.second)
//            }
//
//            +super.followVisionAssistedTrajectory(
//                path2,
//                Autonomous.startingPosition.withEquals(Autonomous.StartingPositions.LEFT),
//                4.feet,
//                true
//            )
//
//            // Reorient position on field based on Vision alignment.
//            +relocalize(
//                TrajectoryWaypoints.kRocketF,
//                true,
//                Autonomous.startingPosition.withEquals(Autonomous.StartingPositions.LEFT)
//            )
//
//            val path2 = super.followVisionAssistedTrajectory(
//                path3,
//                Autonomous.startingPosition.withEquals(Autonomous.StartingPositions.LEFT),
//                4.feet, false
//            )
//
//            // Part 2: Place hatch and go to loading station.
//            +parallel {
//                // Follow the trajectory with vision correction to the loading station.
//                +path2
//                // Take the superstructure to pickup position and arm hatch intake 3 seconds before arrival.
//                +sequential {
//                    // Place hatch panel.
//                    +IntakeHatchCommand(true).withTimeout(0.5.second)
//                    +IntakeCloseCommand()
//                    +Superstructure.kBackHatchFromLoadingStation.withTimeout(3.second)
//                    +IntakeHatchCommand(false).withExit { path2.isFinished }
//                }
//            }
//
//            // Reorient position on field based on Vision alignment.
//            +relocalize(
//                TrajectoryWaypoints.kLoadingStation,
//                false,
//                Autonomous.startingPosition.withEquals(Autonomous.StartingPositions.LEFT)
//            )
//
//            // Part 3: Pickup hatch and go to the near side of the rocket.
//            +parallel {
//                // Make sure the intake is holding the hatch panel.
//                +IntakeHatchCommand(false).withTimeout(0.5.second)
//                // Follow the trajectory with vision correction to the near side of the rocket.
//                +super.followVisionAssistedTrajectory(
//                    path4,
//                    Autonomous.startingPosition.withEquals(Autonomous.StartingPositions.LEFT),
//                    6.feet, true
//                )
//                // Take the superstructure to scoring height.
//                +Superstructure.kHatchLow.withTimeout(4.second)
//            }
//
//            // Part 4: Score the hatch and go to the loading station for the end of the sandstorm period.
//            +parallel {
//                // Score hatch.
//                // Follow the trajectory to the loading station.
//                +DriveSubsystem.followTrajectory(
//                    TrajectoryFactory.rocketNToLoadingStation,
//                    Autonomous.startingPosition.withEquals(Autonomous.StartingPositions.LEFT)
//                )
//                // Take the superstructure to a position to pick up the next hatch.
//                +sequential {
//                    +IntakeHatchCommand(releasing = true).withTimeout(0.5.second)
//                    +IntakeCloseCommand()
//                    +Superstructure.kBackHatchFromLoadingStation
//                }
//            }
//        }
// }
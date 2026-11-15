
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class everything {

    @TeleOp(name = "Arcade Drive", group = "TeleOp")
    public static class ArcadeDriveTeleOp extends LinearOpMode {

        Gamepad currentGamepad = new Gamepad();
        Gamepad previousGamepad = new Gamepad();

        // L3 DIRECTION TOGGLE
        boolean reverseMode = false;

        // FLYWHEEL TOGGLE
        boolean flywheelToggledOn = false;

        @Override
        public void runOpMode() {

            // =================================
            // HARDWARE MAPPING
            // =================================

            DcMotor tealMotor = hardwareMap.get(DcMotor.class, "teal");
            DcMotor pinkMotor = hardwareMap.get(DcMotor.class, "pink");

            DcMotorEx flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");

            DcMotor intakeMotor = hardwareMap.get(DcMotor.class, "intake");

            CRServo gateServo = hardwareMap.get(CRServo.class, "gateServo");


            // =================================
            // MOTOR DIRECTIONS
            // =================================

            tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);
            pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            flywheel.setDirection(DcMotorSimple.Direction.REVERSE);


            // =================================
            // MOTOR BRAKING
            // =================================

            tealMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            pinkMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);


            // INITIAL FLYWHEEL SPEED
            double flywheelPower = 300;


            telemetry.addData("Status", "Initialized. Ready!");
            telemetry.update();

            waitForStart();


            // =================================
            // MAIN TELEOP LOOP
            // =================================

            while (opModeIsActive()) {

                // IMPORTANT:
                // Read current controller FIRST.
                // Previous controller is saved at the end.

                currentGamepad.copy(gamepad1);


                // =================================
                // L3 BUTTON - 180 DEGREE TOGGLE
                // =================================

                if (currentGamepad.left_stick_button &&
                        !previousGamepad.left_stick_button) {

                    reverseMode = !reverseMode;
                }


                // =================================
                // 1. ARCADE DRIVE INPUT
                // =================================

                double drive = -currentGamepad.left_stick_y;
                double turn = currentGamepad.right_stick_x;


                // =================================
                // L3 REVERSE MODE
                // FLIP DRIVE AND TURN
                // =================================

                if (reverseMode) {
                    drive = -drive;
                    turn = -turn;
                }


                // =================================
                // 2. MIXED MOTOR POWER
                // =================================

                double tealInput = drive + turn;
                double pinkInput = drive - turn;


                // NORMALIZE DRIVE INPUTS

                double max = Math.max(
                        Math.abs(tealInput),
                        Math.abs(pinkInput)
                );

                if (max > 1.0) {
                    tealInput /= max;
                    pinkInput /= max;
                }


                // =================================
                // 3. CUBIC DRIVE SENSITIVITY
                // =================================

                double tealPower = Math.pow(tealInput, 3);
                double pinkPower = Math.pow(pinkInput, 3);


                // =================================
                // 4. FLYWHEEL SPEED ADJUSTMENT
                // =================================

                if (currentGamepad.dpad_up &&
                        !previousGamepad.dpad_up) {

                    flywheelPower += 10;
                }

                if (currentGamepad.dpad_down &&
                        !previousGamepad.dpad_down) {

                    flywheelPower -= 10;
                }


                // =================================
                // 5. APPLY DRIVE MOTOR POWER
                // =================================

                tealMotor.setPower(tealPower);
                pinkMotor.setPower(pinkPower);


                // =================================
                // 6. INTAKE CONTROL
                // =================================

                if (currentGamepad.left_trigger > 0.25) {

                    intakeMotor.setPower(1);

                } else if (currentGamepad.left_bumper) {

                    intakeMotor.setPower(-1);

                } else {

                    intakeMotor.setPower(0.0);
                }


                // =================================
                // 7. FLYWHEEL TOGGLE
                // RIGHT TRIGGER
                // =================================

                if (currentGamepad.right_trigger > 0.25 &&
                        previousGamepad.right_trigger <= 0.25) {

                    flywheelToggledOn = !flywheelToggledOn;
                }


                // APPLY FLYWHEEL SPEED

                if (flywheelToggledOn) {

                    flywheel.setVelocity(flywheelPower);

                } else {

                    flywheel.setVelocity(0);
                }


                // =================================
                // 8. GATE SERVO CONTROL
                // =================================

                if (currentGamepad.a) {

                    gateServo.setPower(0.8);

                } else if (currentGamepad.b) {

                    gateServo.setPower(-0.8);

                } else {

                    gateServo.setPower(0.0);
                }


                // =================================
                // 9. TELEMETRY
                // =================================

                telemetry.addData("Drive Input", "%.2f", drive);
                telemetry.addData("Turn Input", "%.2f", turn);

                telemetry.addData("Teal Power", "%.2f", tealPower);
                telemetry.addData("Pink Power", "%.2f", pinkPower);

                telemetry.addData("Flywheel Speed Setting", "%.0f", flywheelPower);
                telemetry.addData("Current Flywheel Speed", "%.0f", flywheel.getVelocity());

                telemetry.addData("Flywheel Status",
                        flywheelToggledOn ? "ON" : "OFF");

                telemetry.addData("Intake Power", intakeMotor.getPower());

                telemetry.addData("Servo Power", gateServo.getPower());

                telemetry.addData("L3 Direction",
                        reverseMode ? "REVERSED 180" : "NORMAL");

                telemetry.update();


                // SAVE CURRENT CONTROLLER FOR NEXT LOOP
                previousGamepad.copy(currentGamepad);
            }


            // =================================
            // STOP MOTORS WHEN TELEOP ENDS
            // =================================

            tealMotor.setPower(0);
            pinkMotor.setPower(0);
            intakeMotor.setPower(0);
            flywheel.setVelocity(0);
            gateServo.setPower(0);

        }
    }
}

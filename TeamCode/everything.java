package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior;
import com.qualcomm.robotcore.hardware.DcMotor.RunMode;
import com.qualcomm.robotcore.util.ElapsedTime;

public class everything {

    @TeleOp(name = "Arcade Drive", group = "TeleOp")
    public static class ArcadeDriveTeleOp extends LinearOpMode {

        private DcMotor teal;
        private DcMotor pink;
        private DcMotor intake;
        private DcMotorEx flywheel;
        private CRServo gateServo;

        private Gamepad currentGamepad = new Gamepad();
        private Gamepad previousGamepad = new Gamepad();

        private boolean reverseMode = false;
        private boolean flywheelToggledOn = false;
        private boolean flywheelIdleToggledOn = false;

        // Flywheel speeds (ticks per second, used with setVelocity)
        // With a working encoder these are ticks/sec. If no encoder is detected, they act as percent power (0-100).
        private double flywheelPower = 165;
        private double flywheelIdlePower = 5;
        // flywheelPower & flywheelIdlePower increment
        private double flywheelPowerInc = 1;
        private double flywheelIdlePowerInc = 1;

        // Encoder health check
        private boolean flywheelEncoderFailed = false;
        private ElapsedTime encoderCheckTimer = new ElapsedTime();
        private boolean encoderChecking = false;
        private int encoderCheckStartPos = 0;

        // Joystick deadband
        private static final double DEADBAND = 0.05;

        @Override
        public void runOpMode() {

            teal = hardwareMap.get(DcMotor.class, "teal");
            pink = hardwareMap.get(DcMotor.class, "pink");
            intake = hardwareMap.get(DcMotor.class, "intake");
            flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
            gateServo = hardwareMap.get(CRServo.class, "gateServo");

            teal.setDirection(DcMotorSimple.Direction.FORWARD);
            pink.setDirection(DcMotorSimple.Direction.REVERSE);
            flywheel.setDirection(DcMotorSimple.Direction.REVERSE);

            // Drive motors always BRAKE so the robot stops as soon as power hits 0
            teal.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
            pink.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);

            flywheel.setZeroPowerBehavior(ZeroPowerBehavior.FLOAT);

            // Velocity readings need the encoder running
            flywheel.setMode(RunMode.STOP_AND_RESET_ENCODER);
            flywheel.setMode(RunMode.RUN_USING_ENCODER);

            telemetry.addLine("Ready!");
            telemetry.update();

            waitForStart();

            while (opModeIsActive()) {

                currentGamepad.copy(gamepad1);

                // -------------------------
                // ARCADE DRIVE
                // -------------------------

                double drive = -currentGamepad.left_stick_y;
                double turn = currentGamepad.right_stick_x;

                // Deadband: hard zero near center so motors get exactly 0 power
                if (Math.abs(drive) < DEADBAND) drive = 0;
                if (Math.abs(turn) < DEADBAND) turn = 0;

                // Cubic sensitivity
                drive = Math.pow(drive, 3);
                turn = Math.pow(turn, 3);

                // L3 toggles reverse mode
                if (currentGamepad.left_stick_button &&
                        !previousGamepad.left_stick_button) {
                    reverseMode = !reverseMode;
                }

                if (reverseMode) {
                    drive = -drive;
                    turn = -turn;
                }

                double leftPower = drive + turn;
                double rightPower = drive - turn;

                // Keep motor power within range
                double max = Math.max(1.0,
                        Math.max(Math.abs(leftPower), Math.abs(rightPower)));

                leftPower /= max;
                rightPower /= max;

                teal.setPower(leftPower);
                pink.setPower(rightPower);

                // -------------------------
                // INTAKE
                // -------------------------

                if (currentGamepad.left_trigger > 0.25) {
                    intake.setPower(1.0);
                } else if (currentGamepad.left_bumper) {
                    intake.setPower(-0.7);
                } else {
                    intake.setPower(0);
                }

                // -------------------------
                // FLYWHEEL TOGGLE
                // -------------------------

                // Press right trigger once to turn flywheel ON/OFF
                if (currentGamepad.right_trigger > 0.25 &&
                        previousGamepad.right_trigger <= 0.25) {
                    flywheelToggledOn = !flywheelToggledOn;
                }

                // Press Y once to turn flywheel IDLE on/off
                if (currentGamepad.y && !previousGamepad.y) {
                    flywheelIdleToggledOn = !flywheelIdleToggledOn;
                }

                // -------------------------
                // FLYWHEEL SPEED ADJUSTMENT
                // -------------------------

                // D-pad up increases speed by flywheelPowerInc
                if (currentGamepad.dpad_up &&
                        !previousGamepad.dpad_up) {
                    flywheelPower += flywheelPowerInc;
                }

                // D-pad down decreases speed by flywheelPowerInc
                if (currentGamepad.dpad_down &&
                        !previousGamepad.dpad_down) {
                    flywheelPower = Math.max(0, flywheelPower - flywheelPowerInc);
                }

                // D-pad right/left adjusts the IDLE speed by flywheelIdlePowerInc
                if (currentGamepad.dpad_right && !previousGamepad.dpad_right) {
                    flywheelIdlePower += flywheelIdlePowerInc;
                }
                // D-pad right/left adjusts the IDLE speed by flywheelIdlePowerInc
                if (currentGamepad.dpad_left && !previousGamepad.dpad_left) {
                    flywheelIdlePower = Math.max(0, flywheelIdlePower - flywheelIdlePowerInc);
                }

                // -------------------------
                // FLYWHEEL POWER APPLICATION
                // -------------------------

                double flywheelTarget = 0;
                if (flywheelToggledOn) {
                    flywheelTarget = flywheelPower;      // Full shooting speed
                } else if (flywheelIdleToggledOn) {
                    flywheelTarget = flywheelIdlePower;  // Idle speed (toggled with Y)
                }

                // Detect a missing/unplugged encoder: commanded to spin but position never changes
                if (!flywheelEncoderFailed) {
                    if (flywheelTarget > 0) {
                        if (!encoderChecking) {
                            encoderChecking = true;
                            encoderCheckTimer.reset();
                            encoderCheckStartPos = flywheel.getCurrentPosition();
                        } else if (encoderCheckTimer.seconds() > 1.0 &&
                                flywheel.getCurrentPosition() == encoderCheckStartPos) {
                            flywheelEncoderFailed = true;
                            flywheel.setMode(RunMode.RUN_WITHOUT_ENCODER);
                        }
                    } else {
                        encoderChecking = false;
                    }
                }

                if (flywheelEncoderFailed) {
                    // No encoder: drive by percent power instead
                    flywheel.setPower(Math.min(1.0, flywheelTarget / 100.0));
                } else {
                    flywheel.setVelocity(flywheelTarget);
                }

                // -------------------------
                // GATE SERVO
                // -------------------------

                if (currentGamepad.a) {
                    gateServo.setPower(0.8);
                } else if (currentGamepad.b) {
                    gateServo.setPower(-0.8);
                } else {
                    gateServo.setPower(0);
                }

                // -------------------------
                // TELEMETRY
                // -------------------------

                telemetry.addData("Reverse Mode", reverseMode ? "ON" : "OFF");
                telemetry.addData("Flywheel", flywheelToggledOn ? "ON" : (flywheelIdleToggledOn ? "IDLE" : "OFF"));
                if (flywheelEncoderFailed) {
                    telemetry.addData("Flywheel Encoder", "NOT DETECTED - using % power");
                    telemetry.addData("Flywheel Commanded Power %", "%.0f", Math.min(100, flywheelTarget));
                } else {
                    telemetry.addData("Flywheel Target (ticks/s)", flywheelTarget);
                    telemetry.addData("Flywheel Actual (ticks/s)", "%.1f", Math.abs(flywheel.getVelocity()));
                }
                telemetry.addData("Shoot Speed / Idle Speed", "%.0f / %.0f", flywheelPower, flywheelIdlePower);
                // Encoder positions: shows which port the flywheel encoder is really plugged into
                telemetry.addData("Enc Pos flywheel/teal/pink/intake", "%d / %d / %d / %d",
                        flywheel.getCurrentPosition(), teal.getCurrentPosition(),
                        pink.getCurrentPosition(), intake.getCurrentPosition());
                telemetry.addData("Intake Power", intake.getPower());
                telemetry.update();

                // Save current gamepad state for next loop
                previousGamepad.copy(currentGamepad);
            }

            // -------------------------
            // STOP ALL MOTORS
            // -------------------------

            teal.setPower(0);
            pink.setPower(0);
            intake.setPower(0);
            flywheel.setVelocity(0);
            gateServo.setPower(0);
        }
    }
}

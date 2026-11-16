package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior;

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

        // --- MOTOR & GEAR CONFIGURATION FOR RPM ---
        private static final double COUNTS_PER_MOTOR_REV = 28.0; 
        private static final double GEAR_RATIO = 1.0;            

        // Target shooting speed in RPM 
        private double flywheelTargetRPM = 1000.0; 

        // Joystick deadband
        private static final double DEADBAND = 0.05;

        // Helper method to convert RPM to Encoder Ticks Per Second for setVelocity()
        private double rpmToTicksPerSecond(double rpm) {
            return (rpm * COUNTS_PER_MOTOR_REV * GEAR_RATIO) / 60.0;
        }

        // Helper method to convert Encoder Ticks Per Second back to RPM for telemetry
        private double ticksPerSecondToRpm(double ticksPerSec) {
            return (ticksPerSec * 60.0) / (COUNTS_PER_MOTOR_REV * GEAR_RATIO);
        }

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
            flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            teal.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
            pink.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
            flywheel.setZeroPowerBehavior(ZeroPowerBehavior.FLOAT);

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

                if (Math.abs(drive) < DEADBAND) drive = 0;
                if (Math.abs(turn) < DEADBAND) turn = 0;

                drive = Math.pow(drive, 3);
                turn = Math.pow(turn, 3);

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
                // FLYWHEEL SPEED ADJUSTMENT (D-Pad)
                // -------------------------

                if (currentGamepad.dpad_up && !previousGamepad.dpad_up) {
                    flywheelTargetRPM += 200; 
                }

                if (currentGamepad.dpad_down && !previousGamepad.dpad_down) {
                    flywheelTargetRPM = Math.max(0, flywheelTargetRPM - 200);
                }

                // -------------------------
                // FLYWHEEL POWER APPLICATION & TELEMETRY TRACKING
                // -------------------------

                String flywheelState;
                double activeTargetRPM = 0.0;

                if (currentGamepad.y) {
                    activeTargetRPM = 0.0;
                    flywheel.setVelocity(0); 
                    flywheelState = "OFF (Y Pressed)";
                } else if (currentGamepad.right_trigger > 0.25) {
                    activeTargetRPM = flywheelTargetRPM;
                    flywheel.setVelocity(rpmToTicksPerSecond(activeTargetRPM)); 
                    flywheelState = "SHOOTING";
                } else {
                    activeTargetRPM = flywheelTargetRPM * 0.30;
                    flywheel.setVelocity(rpmToTicksPerSecond(activeTargetRPM)); 
                    flywheelState = "IDLE (30%)";
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
                telemetry.addData("Flywheel State", flywheelState);
                telemetry.addData("Shooting Setpoint", "%.0f RPM", flywheelTargetRPM);
                telemetry.addData("Active Target RPM", "%.0f RPM", activeTargetRPM);
                telemetry.addData("Actual RPM", "%.1f RPM", ticksPerSecondToRpm(flywheel.getVelocity()));
                telemetry.addData("Intake Power", intake.getPower());
                telemetry.update();

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

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior;
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

        // Flywheel speeds
        private double flywheelPower = 55;
        private double flywheelIdlePower = 300; // Adjust this value for your desired idle speed

        // Braking timer
        private ElapsedTime brakeTimer = new ElapsedTime();
        private boolean previousDriveMoving = false;
        private boolean brakingAfterRelease = false;

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
                // DRIVE BRAKING
                // -------------------------

                boolean driveMoving =
                        Math.abs(currentGamepad.left_stick_y) > 0.05 ||
                                Math.abs(currentGamepad.right_stick_x) > 0.05;

                if (driveMoving) {
                    teal.setZeroPowerBehavior(ZeroPowerBehavior.FLOAT);
                    pink.setZeroPowerBehavior(ZeroPowerBehavior.FLOAT);
                    brakingAfterRelease = false;

                } else if (previousDriveMoving) {
                    // Joystick was just released: start 0.1-second brake
                    teal.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
                    pink.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);

                    brakeTimer.reset();
                    brakingAfterRelease = true;

                } else if (brakingAfterRelease &&
                        brakeTimer.seconds() >= 0.1) {
                    // After 0.1 seconds, switch to coast
                    teal.setZeroPowerBehavior(ZeroPowerBehavior.FLOAT);
                    pink.setZeroPowerBehavior(ZeroPowerBehavior.FLOAT);

                    brakingAfterRelease = false;
                }

                previousDriveMoving = driveMoving;

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

                // -------------------------
                // FLYWHEEL SPEED ADJUSTMENT
                // -------------------------

                // D-pad up increases speed by 5
                if (currentGamepad.dpad_up &&
                        !previousGamepad.dpad_up) {
                    flywheelPower += 5;
                }

                // D-pad down decreases speed by 5 (Fixed typo here)
                if (currentGamepad.dpad_down &&
                        !previousGamepad.dpad_down) {
                    flywheelPower = Math.max(0, flywheelPower - 5);
                }

                // -------------------------
                // FLYWHEEL POWER APPLICATION
                // -------------------------

                if (flywheelToggledOn) {
                    flywheel.setVelocity(flywheelPower); // Full shooting speed
                } else if (currentGamepad.y) {
                    flywheel.setVelocity(flywheelIdlePower); // Low idle speed when holding Y
                } else {
                    flywheel.setVelocity(0); // Completely off
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
                telemetry.addData("Flywheel", flywheelToggledOn ? "ON" : (currentGamepad.y ? "IDLE" : "OFF"));
                telemetry.addData("Flywheel Speed", flywheelPower);
                telemetry.addData("Intake Power", intake.getPower());
                telemetry.addData("Braking", brakingAfterRelease ? "YES" : "NO");
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

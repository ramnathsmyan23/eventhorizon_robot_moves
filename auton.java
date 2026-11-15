package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name = "Basic Autonomous", group = "Autonomous")
public class auton extends LinearOpMode {

    // =================================================
    // PATH TIMES (seconds) - tune these during testing
    // =================================================
    private static final double FORWARD_SECONDS = 1.0;     // the 57 inch leg
    private static final double TURN_LEFT_SECONDS = 0.5;   // 90 degrees left
    private static final double BACK_1_SECONDS = 0.2;      // ~12 inches (57 in = 1 s, so 12 in ~ 0.21 s)
    private static final double TURN_RIGHT_SECONDS = 0.5;  // 90 degrees right
    private static final double BACK_2_SECONDS = 0.8;      // ~45 inches (45 / 57 = 0.79 s)

    // =================================================
    // DRIVE SETTINGS
    // =================================================
    private static final double DRIVE_POWER = 0.5;
    private static final double TURN_POWER = 0.4;
    private static final double SETTLE_TIME = 0.25;  // pause after each move so the robot fully stops

    // =================================================
    // SHOOTING
    // =================================================
    private static final double FLYWHEEL_TARGET_VELOCITY = 55;  // ticks/sec (same as TeleOp)
    private static final double FLYWHEEL_FALLBACK_POWER = 0.55; // used if flywheel encoder isn't detected
    private static final double FLYWHEEL_TOLERANCE = 5;
    private static final double FLYWHEEL_SPINUP_TIMEOUT = 3.0;
    private static final double INTAKE_FEED_POWER = 0.7;
    private static final double GATE_OPEN_POWER = 0.8;
    private static final double SHOOT_TIME = 30.0;  // seconds to shoot the 4 balls

    // =================================================
    // HARDWARE
    // =================================================
    private DcMotor teal;   // left
    private DcMotor pink;   // right
    private DcMotor intake;
    private DcMotorEx flywheel;
    private CRServo gateServo;

    private boolean flywheelEncoderFailed = false;
    private final ElapsedTime timer = new ElapsedTime();

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

        teal.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pink.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        teal.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        pink.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        flywheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        stopAllMotors();

        telemetry.addData("Status", "Autonomous Ready!");
        telemetry.update();

        waitForStart();
        if (!opModeIsActive()) return;

        // 1. Forward
        status("Forward " + FORWARD_SECONDS + " s");
        drive(DRIVE_POWER, DRIVE_POWER, FORWARD_SECONDS);

        // 2. Turn left 90
        status("Turn left");
        drive(-TURN_POWER, TURN_POWER, TURN_LEFT_SECONDS);

        // 3. Shoot 4 balls (30 seconds)
        status("Spinning up flywheel");
        spinUpFlywheel();
        status("Shooting 4 balls");
        shoot();

        // 4. Backward ~12 in
        status("Backward " + BACK_1_SECONDS + " s");
        drive(-DRIVE_POWER, -DRIVE_POWER, BACK_1_SECONDS);

        // 5. Turn right 90
        status("Turn right");
        drive(TURN_POWER, -TURN_POWER, TURN_RIGHT_SECONDS);

        // 6. Backward ~45 in
        status("Backward " + BACK_2_SECONDS + " s");
        drive(-DRIVE_POWER, -DRIVE_POWER, BACK_2_SECONDS);

        stopAllMotors();
        status("Autonomous Complete!");
    }

    // =================================================
    // DRIVE: run both drive motors for a set time, then brake and settle
    // Turn left  = left motor backward, right motor forward
    // Turn right = left motor forward, right motor backward
    // =================================================
    private void drive(double leftPower, double rightPower, double seconds) {
        if (!opModeIsActive()) return;

        teal.setPower(leftPower);
        pink.setPower(rightPower);
        timer.reset();
        while (opModeIsActive() && timer.seconds() < seconds) {
            idle();
        }
        stopDrive();

        timer.reset();
        while (opModeIsActive() && timer.seconds() < SETTLE_TIME) {
            idle();
        }
    }

    // =================================================
    // FLYWHEEL + SHOOTING
    // =================================================
    private void spinUpFlywheel() {
        if (!opModeIsActive()) return;
        flywheel.setVelocity(FLYWHEEL_TARGET_VELOCITY);
        int startPos = flywheel.getCurrentPosition();
        timer.reset();

        while (opModeIsActive() && timer.seconds() < FLYWHEEL_SPINUP_TIMEOUT) {
            double actual = Math.abs(flywheel.getVelocity());
            telemetry.addData("Flywheel Target (ticks/s)", FLYWHEEL_TARGET_VELOCITY);
            telemetry.addData("Flywheel Actual (ticks/s)", "%.1f", actual);
            telemetry.update();

            if (Math.abs(actual - FLYWHEEL_TARGET_VELOCITY) <= FLYWHEEL_TOLERANCE) {
                return; // at speed
            }

            // No encoder movement after 1 second: fall back to plain power
            if (timer.seconds() > 1.0 && flywheel.getCurrentPosition() == startPos) {
                flywheelEncoderFailed = true;
                flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                flywheel.setPower(FLYWHEEL_FALLBACK_POWER);
                return;
            }
        }
    }

    private void shoot() {
        if (!opModeIsActive()) return;
        gateServo.setPower(GATE_OPEN_POWER);
        intake.setPower(INTAKE_FEED_POWER);
        timer.reset();

        while (opModeIsActive() && timer.seconds() < SHOOT_TIME) {
            if (flywheelEncoderFailed) {
                telemetry.addData("Flywheel", "No encoder - %.0f%% power", FLYWHEEL_FALLBACK_POWER * 100);
            } else {
                telemetry.addData("Flywheel Target (ticks/s)", FLYWHEEL_TARGET_VELOCITY);
                telemetry.addData("Flywheel Actual (ticks/s)", "%.1f", Math.abs(flywheel.getVelocity()));
            }
            telemetry.addData("Shoot time left", "%.1f s", SHOOT_TIME - timer.seconds());
            telemetry.update();
        }

        gateServo.setPower(0);
        intake.setPower(0);
        if (flywheelEncoderFailed) {
            flywheel.setPower(0);
        } else {
            flywheel.setVelocity(0);
        }
    }

    // =================================================
    // HELPERS
    // =================================================
    private void status(String text) {
        telemetry.addData("Path", text);
        telemetry.update();
    }

    private void stopDrive() {
        teal.setPower(0.0);
        pink.setPower(0.0);
    }

    private void stopAllMotors() {
        if (teal != null) teal.setPower(0.0);
        if (pink != null) pink.setPower(0.0);
        if (intake != null) intake.setPower(0.0);
        if (flywheel != null) {
            if (flywheelEncoderFailed) flywheel.setPower(0); else flywheel.setVelocity(0);
        }
        if (gateServo != null) gateServo.setPower(0);
    }
}

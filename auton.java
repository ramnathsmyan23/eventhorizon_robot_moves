package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name = "Basic Autonomous", group = "Autonomous")
public class BasicAutonomous extends LinearOpMode {

    // =================================
    // TUNING CONSTANTS (adjust in testing)
    // =================================

    // Flywheel (ticks per second, same units as TeleOp)
    private static final double FLYWHEEL_TARGET_VELOCITY = 55;
    private static final double FLYWHEEL_TOLERANCE = 5;        // "at speed" window
    private static final double FLYWHEEL_SPINUP_TIMEOUT = 3.0; // seconds, shoot anyway after this

    // Shooting
    private static final double INTAKE_FEED_POWER = 0.7;
    private static final double GATE_OPEN_POWER = 0.8;   // same as TeleOp "A" button
    private static final double SHOOT_TIME = 5.0;        // seconds to feed all 4 balls

    // Driving
    private static final double DRIVE_POWER = 0.5;
    private static final double TURN_POWER = 0.4;
    private static final double FORWARD_TIME_1 = 2.0;
    private static final double TURN_TIME = 1.0;
    private static final double FORWARD_TIME_2 = 7.0;
    private static final double BRAKE_SETTLE_TIME = 0.15; // pause after each move so the robot fully stops

    // =================================
    // HARDWARE
    // =================================

    private DcMotor tealMotor;
    private DcMotor pinkMotor;
    private DcMotor intakeMotor;
    private DcMotorEx flywheel;
    private CRServo gateServo;

    private final ElapsedTime timer = new ElapsedTime();

    @Override
    public void runOpMode() {

        // 1. HARDWARE MAPPING
        tealMotor = hardwareMap.get(DcMotor.class, "teal");
        pinkMotor = hardwareMap.get(DcMotor.class, "pink");
        intakeMotor = hardwareMap.get(DcMotor.class, "intake");
        flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        gateServo = hardwareMap.get(CRServo.class, "gateServo");

        // 2. DIRECTIONS (matched to the TeleOp)
        tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        flywheel.setDirection(DcMotorSimple.Direction.REVERSE);

        // 3. ZERO POWER BEHAVIOR
        tealMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pinkMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        // 4. FLYWHEEL ENCODER (needed for setVelocity / getVelocity)
        flywheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        stopAllMotors();

        telemetry.addData("Status", "Autonomous Ready!");
        telemetry.addData("Balls", "4 Preloaded");
        telemetry.update();

        waitForStart();

        if (!opModeIsActive()) return;

        // =================================
        // STEP 1: SHOOT FOUR BALLS
        // =================================

        telemetry.addData("Path", "Spinning Up Flywheel");
        telemetry.update();

        spinUpFlywheel();
        if (!opModeIsActive()) return;

        telemetry.addData("Path", "Shooting 4 Balls");
        telemetry.update();

        shoot();
        if (!opModeIsActive()) return;

        // =================================
        // STEP 2: DRIVE FORWARD
        // =================================

        telemetry.addData("Path", "Moving Forward");
        telemetry.update();
        move(DRIVE_POWER, DRIVE_POWER, FORWARD_TIME_1);
        if (!opModeIsActive()) return;

        // =================================
        // STEP 3: TURN TOWARD PARK
        // =================================

        telemetry.addData("Path", "Turning Toward Park");
        telemetry.update();
        move(TURN_POWER, -TURN_POWER, TURN_TIME);
        if (!opModeIsActive()) return;

        // =================================
        // STEP 4: DRIVE INTO PARK ZONE
        // =================================

        telemetry.addData("Path", "Driving Into Park Zone");
        telemetry.update();
        move(DRIVE_POWER, DRIVE_POWER, FORWARD_TIME_2);

        // =================================
        // STEP 5: DONE
        // =================================

        stopAllMotors();
        telemetry.addData("Status", "Autonomous Complete!");
        telemetry.addData("Location", "Park Zone");
        telemetry.update();
        sleep(500);
    }

    // =================================
    // FLYWHEEL SPIN-UP
    // Waits until the flywheel reaches target speed (or times out).
    // =================================

    private void spinUpFlywheel() {
        flywheel.setVelocity(FLYWHEEL_TARGET_VELOCITY);
        timer.reset();

        while (opModeIsActive() && timer.seconds() < FLYWHEEL_SPINUP_TIMEOUT) {
            double actual = flywheel.getVelocity();
            telemetry.addData("Flywheel Target (ticks/s)", FLYWHEEL_TARGET_VELOCITY);
            telemetry.addData("Flywheel Actual (ticks/s)", "%.1f", actual);
            telemetry.update();

            if (Math.abs(actual - FLYWHEEL_TARGET_VELOCITY) <= FLYWHEEL_TOLERANCE) {
                break;
            }
        }
    }

    // =================================
    // SHOOT: gate open + intake feeding, flywheel held at speed
    // =================================

    private void shoot() {
        gateServo.setPower(GATE_OPEN_POWER);
        intakeMotor.setPower(INTAKE_FEED_POWER);
        timer.reset();

        while (opModeIsActive() && timer.seconds() < SHOOT_TIME) {
            telemetry.addData("Path", "Shooting");
            telemetry.addData("Flywheel Target (ticks/s)", FLYWHEEL_TARGET_VELOCITY);
            telemetry.addData("Flywheel Actual (ticks/s)", "%.1f", flywheel.getVelocity());
            telemetry.update();
        }

        gateServo.setPower(0);
        intakeMotor.setPower(0);
        flywheel.setVelocity(0);
    }

    // =================================
    // MOVE: run both drive motors for a set time, then brake and settle
    // =================================

    private void move(double leftPower, double rightPower, double seconds) {
        tealMotor.setPower(leftPower);
        pinkMotor.setPower(rightPower);
        timer.reset();

        while (opModeIsActive() && timer.seconds() < seconds) {
            idle();
        }

        stopDrive();

        // Short pause so BRAKE fully stops the robot before the next move
        timer.reset();
        while (opModeIsActive() && timer.seconds() < BRAKE_SETTLE_TIME) {
            idle();
        }
    }

    // =================================
    // STOP HELPERS
    // =================================

    private void stopDrive() {
        tealMotor.setPower(0.0);
        pinkMotor.setPower(0.0);
    }

    private void stopAllMotors() {
        if (tealMotor != null) tealMotor.setPower(0.0);
        if (pinkMotor != null) pinkMotor.setPower(0.0);
        if (intakeMotor != null) intakeMotor.setPower(0.0);
        if (flywheel != null) flywheel.setVelocity(0);
        if (gateServo != null) gateServo.setPower(0);
    }
}


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

    private DcMotor teal;
    private DcMotor pink;
    private DcMotor intake;
    private DcMotorEx flywheel;
    private CRServo gateServo;

    // Drive
    private static final double DRIVE_POWER = 0.5;
    private static final double TURN_POWER = 0.4;

    // Times in milliseconds
    private static final int FORWARD_MS = 1100;
    private static final int TURN_LEFT_MS = 520;
    private static final int BACK_1_MS = 230;
    private static final int TURN_RIGHT_MS = 520;
    private static final int BACK_2_MS = 870;
    private static final int SETTLE_MS = 250;

    // Shooting
    private static final double FLYWHEEL_TARGET_VELOCITY = 200;
    private static final double FLYWHEEL_FALLBACK_POWER = 0.55;
    private static final double INTAKE_FEED_POWER = 0.7;
    private static final double GATE_OPEN_POWER = 0.8;
    private static final double SHOOT_TIME = 10.0;
    private static final int INTAKE_WAIT_MS = 2000;

    private boolean flywheelEncoderFailed = false;
    private int flywheelStartPos = 0;
    private final ElapsedTime timer = new ElapsedTime();

    @Override
    public void runOpMode() {

        // Initialize hardware
        teal = hardwareMap.get(DcMotor.class, "teal");
        pink = hardwareMap.get(DcMotor.class, "pink");
        intake = hardwareMap.get(DcMotor.class, "intake");
        flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        gateServo = hardwareMap.get(CRServo.class, "gateServo");

        teal.setDirection(DcMotorSimple.Direction.FORWARD);
        pink.setDirection(DcMotorSimple.Direction.REVERSE);
        flywheel.setDirection(DcMotorSimple.Direction.REVERSE);

        // Set braking behavior
        teal.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pink.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        flywheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addData("Status", "Autonomous Ready!");
        telemetry.update();

        waitForStart();

        if (!opModeIsActive()) return;

        // 0. START OUTTAKE - STAYS ON THROUGHOUT AUTONOMOUS
        startFlywheel();

        // 1. MOVE FORWARD
        telemetry.addData("Status", "Moving Forward");
        telemetry.update();

        teal.setPower(DRIVE_POWER);
        pink.setPower(DRIVE_POWER);
        sleep(FORWARD_MS);

        teal.setPower(0);
        pink.setPower(0);
        sleep(SETTLE_MS);

        if (!opModeIsActive()) return;

        // 2. TURN LEFT
        telemetry.addData("Status", "Turning Left");
        telemetry.update();

        teal.setPower(-TURN_POWER);
        pink.setPower(TURN_POWER);
        sleep(TURN_LEFT_MS);

        teal.setPower(0);
        pink.setPower(0);
        sleep(SETTLE_MS);

        if (!opModeIsActive()) return;

        // 3. ROBOT IS NOW IN SHOOTING POSITION
        // INTAKE STARTS ONLY HERE
        telemetry.addData("Status", "Ready to Shoot");
        telemetry.update();

        shoot();

        if (!opModeIsActive()) return;

        // 4. BACKWARD ~12 INCHES
        telemetry.addData("Status", "Moving Backward");
        telemetry.update();

        teal.setPower(-DRIVE_POWER);
        pink.setPower(-DRIVE_POWER);
        sleep(BACK_1_MS);

        teal.setPower(0);
        pink.setPower(0);
        sleep(SETTLE_MS);

        if (!opModeIsActive()) return;

        // 5. TURN RIGHT
        telemetry.addData("Status", "Turning Right");
        telemetry.update();

        teal.setPower(TURN_POWER);
        pink.setPower(-TURN_POWER);
        sleep(TURN_RIGHT_MS);

        teal.setPower(0);
        pink.setPower(0);
        sleep(SETTLE_MS);

        if (!opModeIsActive()) return;

        // 6. BACKWARD ~45 INCHES
        telemetry.addData("Status", "Moving Backward");
        telemetry.update();

        teal.setPower(-DRIVE_POWER);
        pink.setPower(-DRIVE_POWER);
        sleep(BACK_2_MS);

        teal.setPower(0);
        pink.setPower(0);

        // 7. OUTTAKE OFF ONLY AT THE END
        flywheel.setVelocity(0);
        intake.setPower(0);
        gateServo.setPower(0);

        telemetry.addData("Status", "Autonomous Complete!");
        telemetry.update();
    }

    // OUTTAKE ON
    private void startFlywheel() {
        flywheelStartPos = flywheel.getCurrentPosition();
        flywheel.setVelocity(FLYWHEEL_TARGET_VELOCITY);
    }

    // CHECK FLYWHEEL ENCODER
    private void checkFlywheelEncoder() {
        if (!flywheelEncoderFailed &&
                flywheel.getCurrentPosition() == flywheelStartPos) {

            flywheelEncoderFailed = true;

            flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            flywheel.setPower(FLYWHEEL_FALLBACK_POWER);
        }
    }

    // INTAKE ON ONLY AT SHOOTING POSITION
    // OUTTAKE STAYS ON THROUGHOUT
    private void shoot() {

        checkFlywheelEncoder();

        // Intake and gate start only after the left turn
        gateServo.setPower(GATE_OPEN_POWER);
        intake.setPower(INTAKE_FEED_POWER);

        // Wait 2 seconds after intake starts
        sleep(INTAKE_WAIT_MS);

        if (!opModeIsActive()) {
            intake.setPower(0);
            gateServo.setPower(0);
            return;
        }

        // Shoot for 10 seconds
        timer.reset();

        while (opModeIsActive() && timer.seconds() < SHOOT_TIME) {

            if (flywheelEncoderFailed) {
                telemetry.addData(
                        "Flywheel",
                        "Fallback - %.0f%% power",
                        FLYWHEEL_FALLBACK_POWER * 100
                );
            } else {
                telemetry.addData(
                        "Flywheel Target (ticks/s)",
                        FLYWHEEL_TARGET_VELOCITY
                );

                telemetry.addData(
                        "Flywheel Actual (ticks/s)",
                        "%.1f",
                        Math.abs(flywheel.getVelocity())
                );
            }

            telemetry.addData("Intake", "ON");
            telemetry.addData(
                    "Shoot time left",
                    "%.1f s",
                    SHOOT_TIME - timer.seconds()
            );

            telemetry.update();
            idle();
        }

        // Intake and gate OFF after shooting
        // Outtake remains ON
        intake.setPower(0);
        gateServo.setPower(0);
    }
}

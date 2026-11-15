
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous(name = "Basic Autonomous", group = "Autonomous")

public class BasicAutonomous extends LinearOpMode {

    // DRIVE MOTORS
    private DcMotor tealMotor;
    private DcMotor pinkMotor;

    // INTAKE AND OUTTAKE
    private DcMotor intakeMotor;
    private DcMotor flywheel;

    @Override
    public void runOpMode() {

        // =================================
        // 1. HARDWARE MAPPING
        // =================================

        tealMotor = hardwareMap.get(DcMotor.class, "teal");
        pinkMotor = hardwareMap.get(DcMotor.class, "pink");

        intakeMotor = hardwareMap.get(DcMotor.class, "intake");
        flywheel = hardwareMap.get(DcMotor.class, "flywheel");


        // =================================
        // 2. MOTOR DIRECTIONS
        // =================================

        tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        intakeMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        flywheel.setDirection(DcMotorSimple.Direction.FORWARD);


        // =================================
        // 3. ZERO POWER BEHAVIOR
        // =================================

        tealMotor.setZeroPowerBehavior(
            DcMotor.ZeroPowerBehavior.BRAKE);

        pinkMotor.setZeroPowerBehavior(
            DcMotor.ZeroPowerBehavior.BRAKE);

        intakeMotor.setZeroPowerBehavior(
            DcMotor.ZeroPowerBehavior.BRAKE);

        flywheel.setZeroPowerBehavior(
            DcMotor.ZeroPowerBehavior.FLOAT);


        // =================================
        // 4. INITIAL MOTOR POWERS
        // =================================

        stopAllMotors();

        telemetry.addData("Status", "Autonomous Ready!");
        telemetry.addData("Balls", "4 Preloaded");
        telemetry.update();

        waitForStart();


        if (opModeIsActive()) {

            // =================================
            // STEP 1: SHOOT FOUR BALLS
            // =================================

            telemetry.addData("Path", "Shooting 4 Balls");
            telemetry.update();

            // Start flywheel
            flywheel.setPower(0.63);

            // Feed balls into flywheel
            intakeMotor.setPower(0.7);

            // Starting estimate - adjust during testing
            sleep(5000);

            // Stop shooting mechanism
            intakeMotor.setPower(0.0);
            flywheel.setPower(0.0);


            // =================================
            // STEP 2: DRIVE FORWARD
            // =================================

            if (!opModeIsActive()) return;

            telemetry.addData("Path", "Moving Forward");
            telemetry.update();

            tealMotor.setPower(0.5);
            pinkMotor.setPower(0.5);

            sleep(2000);

            stopDrive();


            // =================================
            // STEP 3: TURN TOWARD PARK
            // =================================

            if (!opModeIsActive()) return;

            telemetry.addData("Path", "Turning Toward Park");
            telemetry.update();

            tealMotor.setPower(0.4);
            pinkMotor.setPower(-0.4);

            sleep(1000);

            stopDrive();


            // =================================
            // STEP 4: DRIVE INTO PARK ZONE
            // =================================

            if (!opModeIsActive()) return;

            telemetry.addData("Path", "Driving Into Park Zone");
            telemetry.update();

            tealMotor.setPower(0.5);
            pinkMotor.setPower(0.5);

            sleep(7000);

            stopDrive();


            // =================================
            // STEP 5: AUTONOMOUS COMPLETE
            // =================================

            stopAllMotors();

            telemetry.addData("Status", "Autonomous Complete!");
            telemetry.addData("Location", "Park Zone");
            telemetry.update();

            sleep(500);
        }

        stopAllMotors();
    }


    // =================================
    // STOP DRIVE MOTORS
    // =================================

    private void stopDrive() {
        tealMotor.setPower(0.0);
        pinkMotor.setPower(0.0);
    }


    // =================================
    // STOP ALL MOTORS
    // =================================

    private void stopAllMotors() {

        if (tealMotor != null) tealMotor.setPower(0.0);
        if (pinkMotor != null) pinkMotor.setPower(0.0);

        if (intakeMotor != null) intakeMotor.setPower(0.0);
        if (flywheel != null) flywheel.setPower(0.0);
    }
}


package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous(name = "Basic Autonomous", group = "Autonomous")

public class BasicAutonomous extends LinearOpMode {

    // Declare drive motors
    // Teal = Left side, Pink = Right side
    private DcMotor tealMotor = null;
    private DcMotor pinkMotor = null;

    // Declare intake and outtake motors
    private DcMotor intakeMotor = null;
    private DcMotor outtakeMotor = null;

    @Override
    public void runOpMode() {

        // --- HARDWARE MAPPING ---

        tealMotor = hardwareMap.get(DcMotor.class, "teal");
        pinkMotor = hardwareMap.get(DcMotor.class, "pink");

        intakeMotor = hardwareMap.get(DcMotor.class, "intake");
        outtakeMotor = hardwareMap.get(DcMotor.class, "outtake");


        // --- MOTOR DIRECTIONS ---

        tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);


        // --- MOTOR BRAKING ---

        tealMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pinkMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        outtakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        // --- INITIAL MOTOR POWERS ---

        tealMotor.setPower(0.0);
        pinkMotor.setPower(0.0);

        intakeMotor.setPower(0.0);
        outtakeMotor.setPower(0.0);


        telemetry.addData("Status", "Initialized Autonomous. Ready!");
        telemetry.update();


        // Wait for match to start
        waitForStart();


        if (opModeIsActive()) {

            // =================================
            // STEP 1: DRIVE FORWARD
            // =================================

            tealMotor.setPower(0.5);
            pinkMotor.setPower(0.5);

            telemetry.addData("Path", "Driving Forward");
            telemetry.update();

            sleep(2000);


            // =================================
            // STEP 2: TURN
            // =================================

            tealMotor.setPower(0.4);
            pinkMotor.setPower(-0.4);

            telemetry.addData("Path", "Turning");
            telemetry.update();

            sleep(1000);


            // =================================
            // STEP 3: DRIVE FORWARD
            // =================================

            tealMotor.setPower(0.5);
            pinkMotor.setPower(0.5);

            telemetry.addData("Path", "Driving Forward");
            telemetry.update();

            sleep(7000);


            // =================================
            // STEP 4: INTAKE
            // =================================

            // ADD YOUR INTAKE ACTION HERE

            // Example:
            // intakeMotor.setPower(0.7);
            // sleep(2000);
            // intakeMotor.setPower(0.0);


            // =================================
            // STEP 5: OUTTAKE
            // =================================

            // ADD YOUR OUTTAKE ACTION HERE

            // Example:
            // outtakeMotor.setPower(0.63);
            // sleep(2000);
            // outtakeMotor.setPower(0.0);


            // =================================
            // STEP 6: STOP ALL MOTORS
            // =================================

            tealMotor.setPower(0.0);
            pinkMotor.setPower(0.0);

            intakeMotor.setPower(0.0);
            outtakeMotor.setPower(0.0);

            telemetry.addData("Path", "Autonomous Complete");
            telemetry.update();

            sleep(500);
        }
    }
}

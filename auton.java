package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous(name = "Basic Autonomous", group = "Autonomous")
public class BasicAutonomous extends LinearOpMode {

    // Declare drive motors (Teal = Left side, Pink = Right side)
    private DcMotor tealMotor = null;
    private DcMotor pinkMotor = null;

    @Override
    public void runOpMode() {

        // --- HARDWARE MAPPING ---
        tealMotor = hardwareMap.get(DcMotor.class, "teal");
        pinkMotor = hardwareMap.get(DcMotor.class, "pink");

        // --- MOTOR DIRECTIONS ---
        tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        // --- MOTOR BRAKING ---
        tealMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pinkMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addData("Status", "Initialized Autonomous. Ready!");
        telemetry.update();

        // Wait for the match to start on the Driver Station
        waitForStart();

        // Ensure opmode is still active before running steps
        if (opModeIsActive()) {

            // --- STEP 1: Drive Forward ---
            // Set both motors to 50% power
            tealMotor.setPower(0.5);
            pinkMotor.setPower(0.5);
            
            telemetry.addData("Path", "Driving Forward");
            telemetry.update();
            
            // Keep driving for 2000 milliseconds (2 seconds)
            sleep(2000);

            // --- STEP 2: Turn ---
            // Set motors in opposite directions to spin in place
            tealMotor.setPower(0.4);
            pinkMotor.setPower(-0.4);
            
            telemetry.addData("Path", "Turning");
            telemetry.update();
            
            // Keep turning for 1000 milliseconds (1 second)
            sleep(1000);

            // --- STEP 3: Stop Motors ---
            tealMotor.setPower(0.0);
            pinkMotor.setPower(0.0);
            
            telemetry.addData("Path", "Complete");
            telemetry.update();
            
            // Brief pause at the end of the routine
            sleep(500);
        }
    }
}

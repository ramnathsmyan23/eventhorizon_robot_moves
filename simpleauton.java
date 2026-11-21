
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous(name = "Path 2 Autonomous", group = "Autonomous")

public class auton extends LinearOpMode {

    private DcMotor teal;  // Left drive
    private DcMotor pink;  // Right drive

    // Drive settings
    private static final double DRIVE_POWER = 0.5;

    // Movement time
    private static final int FORWARD_MS = 150;

    @Override
    public void runOpMode() {

        // Initialize motors
        teal = hardwareMap.get(DcMotor.class, "teal");
        pink = hardwareMap.get(DcMotor.class, "pink");

        // Set motor directions
        teal.setDirection(DcMotorSimple.Direction.FORWARD);
        pink.setDirection(DcMotorSimple.Direction.REVERSE);

        // Set braking behavior
        teal.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pink.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Ready telemetry
        telemetry.addData("Status", "Ready");
        telemetry.addData("Movement", "Forward 300 ms");
        telemetry.update();

        // Wait for driver to press START
        waitForStart();

        if (!opModeIsActive()) return;

        // MOVE FORWARD
        telemetry.addData("Status", "Moving Forward");
        telemetry.update();

        teal.setPower(DRIVE_POWER);
        pink.setPower(DRIVE_POWER);

        sleep(FORWARD_MS);

        // IMMEDIATELY STOP BOTH MOTORS
        teal.setPower(0);
        pink.setPower(0);

        // Confirm stopped
        telemetry.addData("Status", "Stopped - BRAKE");
        telemetry.update();
    }
}

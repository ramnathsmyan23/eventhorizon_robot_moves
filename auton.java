package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous(name = "Auton", group = "Autnonomous")
public class auton extends LinearOpMode { 

   private DcMotor tealmotor = null;
   private Dcmotor pinkmotor = null;

    @Override
    public void runOpMode() {

        // --- HARDWARE MAPPING ---
        tealMotor = hardwareMap.get(DcMotor.class, "teal"); // INTERCHANGEABLE: Device configuration name for left
        pinkMotor = hardwareMap.get(DcMotor.class, "pink"); // INTERCHANGEABLE: Device configuration name for right

        // --- MOTOR DIRECTIONS ---
        tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);  // INTERCHANGEABLE: Change to REVERSE if left runs backward
        pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);  // INTERCHANGEABLE: Change to FORWARD if right runs backward

        // --- MOTOR BRAKING ---
        tealMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); // INTERCHANGEABLE: Can be FLOAT
        pinkMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); // INTERCHANGEABLE: Can be FLOAT

        telemetry.addData("Status", "Initialized Arcade Drive. Ready!");
        telemetry.update();

        // Wait for the match to start on the Driver Station
        waitForStart();

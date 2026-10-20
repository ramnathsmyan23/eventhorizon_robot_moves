package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous(name = "Auton", group = "Autnonomous")
public class auton extends LinearOpMode { 

   private DcMotor tealMotor = null;
   private Dcmotor pinkMotor = null;

    @Override
    public void runOpMode() {

        // --- HARDWARE MAPPING ---
        tealMotor = hardwareMap.get(DcMotor.class, "teal"); // INTERCHANGEABLE: Device configuration name for left
        pinkMotor = hardwareMap.get(DcMotor.class, "pink"); // INTERCHANGEABLE: Device configuration name for right

        // --- MOTOR DIRECTIONS ---
        tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);  // INTERCHANGEABLE: Change to REVERSE if left runs backward
        pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);  // INTERCHANGEABLE: Change to FORWARD if right runs backward

       tealMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
       pinkMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // --- MOTOR BRAKING ---
        tealMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); // INTERCHANGEABLE: Can be FLOAT
        pinkMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); // INTERCHANGEABLE: Can be FLOAT

        telemetry.addData("Status", "Initialized Arcade Drive. Ready!");
        telemetry.update();

        // Wait for the match to start on the Driver Station
        waitForStart();

        tealMotor.setPower(0.4);
        pinkMotor.setPower(0.4);
        sleep(200);
        tealMotor.setPower(1);
        pinkMotor.setPower(0);
        sleep(50);
        tealMotor.setPower(0.4);
        pinkMotor.setPower(0.4);
        sleep(200);
        tealMotor.setPower(1);
        pinkMotor.setPower(0);
        sleep(50);
        tealMotor.setPower(0.4);
        pinkMotor.setPower(0.4);
        sleep(200);
       
     } 

}
   

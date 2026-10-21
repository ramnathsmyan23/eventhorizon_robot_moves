package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous(name = "Auton", group = "Autonomous")
public class auton extends LinearOpMode { 

    // Changed to DcMotorEx to allow ticks-per-second velocity controls
    private DcMotorEx tealMotor = null;
    private DcMotorEx pinkMotor = null;

    @Override
    public void runOpMode() {
        
        // Initialize motors from the hardware map
        tealMotor = hardwareMap.get(DcMotorEx.class, "tealMotor");
        pinkMotor = hardwareMap.get(DcMotorEx.class, "pinkMotor");

        // Set directions
        tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);  
        pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);  

        // Configure internal PID motor loops
        tealMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        pinkMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // --- MOTOR BRAKING ---
        tealMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); 
        pinkMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); 

        // Wait for the match to start on the Driver Station
        waitForStart();

        // 1. Move forward at ~1000 Ticks Per Second
        tealMotor.setVelocity(1000);
        pinkMotor.setVelocity(1000);
        sleep(200);

        // 2. Turn/Pivot (Teal full forward speed, Pink stopped)
        tealMotor.setVelocity(2400);
        pinkMotor.setVelocity(0);
        sleep(50);

        // 3. Move forward again
        tealMotor.setVelocity(1000);
        pinkMotor.setVelocity(1000);
        sleep(200);

        // 4. Turn/Pivot again
        tealMotor.setVelocity(2400);
        pinkMotor.setVelocity(0);
        sleep(50);

        // 5. Final forward push
        tealMotor.setVelocity(1000);
        pinkMotor.setVelocity(1000);
        sleep(200);
       
        // Stop the motors at the end of the routine
        tealMotor.setVelocity(0);
        pinkMotor.setVelocity(0);
    } 
}

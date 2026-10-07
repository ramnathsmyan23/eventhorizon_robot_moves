package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Basic Tank Drive (Teal & Pink)", group = "TeleOp") // INTERCHANGEABLE: OpMode name displayed on Driver Station
public class TankDriveTeleOp extends LinearOpMode {

    // Declare drive motors (Teal = Left side, Pink = Right side)
    private DcMotor tealMotor = null;
    private DcMotor pinkMotor = null;

    // Optional: Slow mode factor (1.0 = full power, 0.5 = half speed)
    private static final double SLOW_MODE_FACTOR = 0.5; // INTERCHANGEABLE: Adjust speed reduction level

    @Override
    public void runOpMode() {

        // --- HARDWARE MAPPING ---
        // Changed to "teal" (left) and "pink" (right). Must match the Driver Station config!
        tealMotor = hardwareMap.get(DcMotor.class, "teal"); // INTERCHANGEABLE: Device configuration name for left
        pinkMotor = hardwareMap.get(DcMotor.class, "pink"); // INTERCHANGEABLE: Device configuration name for right

        // --- MOTOR DIRECTIONS ---
        // Facing opposite directions on the chassis: one side runs FORWARD, other REVERSE
        tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);  // INTERCHANGEABLE: Change to REVERSE if left runs backward
        pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);  // INTERCHANGEABLE: Change to FORWARD if right runs backward

        // --- MOTOR BRAKING ---
        // BRAKE actively resists rolling when inputs are zero; FLOAT coasts freely
        tealMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); // INTERCHANGEABLE: Can be FLOAT
        pinkMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); // INTERCHANGEABLE: Can be FLOAT

        telemetry.addData("Status", "Initialized with Teal (Left) and Pink (Right). Ready!");
        telemetry.update();

        // Wait for the match to start on the Driver Station
        waitForStart();

        // --- MAIN TELEOP LOOP ---
        while (opModeIsActive()) {

            // 1. INPUT ASSIGNMENTS (Standard Tank Drive)
            // Left stick controls Teal; Right stick controls Pink.
            // FTC sticks read negative when pushed up, so we negate them.
            double tealInput = -gamepad1.left_stick_y;   // INTERCHANGEABLE: Driver controls mapping
            double pinkInput = -gamepad1.right_stick_y;  // INTERCHANGEABLE: Driver controls mapping

            // 2. DRIVER SENSITIVITY PREFERENCES (Cubic response curve)
            // Preserves +/- sign, softens low-speed control, but reaches 100% at full push
            double tealPower = Math.pow(tealInput, 3);   // INTERCHANGEABLE: Use raw input or Math.pow(..., 3)
            double pinkPower = Math.pow(pinkInput, 3);   // INTERCHANGEABLE: Use raw input or Math.pow(..., 3)

            // 3. SLOW MODE
            // Hold Right Bumper for precision driving
            if (gamepad1.right_bumper) { // INTERCHANGEABLE: Change trigger/bumper button preference
                tealPower *= SLOW_MODE_FACTOR;
                pinkPower *= SLOW_MODE_FACTOR;
            }

            // 4. POWER APPLICATION
            tealMotor.setPower(tealPower);
            pinkMotor.setPower(pinkPower);

            // 5. DIAGNOSTICS & TELEMETRY
            telemetry.addData("Teal (Left) Power", "%.2f", tealPower);
            telemetry.addData("Pink (Right) Power", "%.2f", pinkPower);
            telemetry.addData("Slow Mode Active", gamepad1.right_bumper);
            telemetry.update();
        }
    }
}

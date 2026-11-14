
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "Arcade Drive", group = "TeleOp")
public class ArcadeDriveTeleOp extends LinearOpMode {

    // Drive motors
    private DcMotor tealMotor = null;
    private DcMotor pinkMotor = null;

    // Flywheel motor
    private DcMotor flywheel = null;

    @Override
    public void runOpMode() {

        // --- HARDWARE MAPPING ---
        tealMotor = hardwareMap.get(DcMotor.class, "teal");
        pinkMotor = hardwareMap.get(DcMotor.class, "pink");
        flywheel = hardwareMap.get(DcMotor.class, "flywheel");

        // --- MOTOR DIRECTIONS ---
        tealMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        pinkMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        flywheel.setDirection(DcMotorSimple.Direction.FORWARD);

        // --- MOTOR BRAKING ---
        tealMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pinkMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        double flywheelPower = 0.0;

        telemetry.addData("Status", "Initialized. Ready!");
        telemetry.update();

        waitForStart();

        // --- MAIN TELEOP LOOP ---
        while (opModeIsActive()) {

            // 1. ARCADE DRIVE INPUT
            double drive = -gamepad1.left_stick_y;
            double turn = gamepad1.right_stick_x;

            // 2. MIXED MOTOR POWER
            double tealInput = drive + turn;
            double pinkInput = drive - turn;

            // Normalize drive inputs
            double max = Math.max(
                Math.abs(tealInput),
                Math.abs(pinkInput)
            );

            if (max > 1.0) {
                tealInput /= max;
                pinkInput /= max;
            }

            // 3. CUBIC DRIVE SENSITIVITY
            double tealPower = Math.pow(tealInput, 3);
            double pinkPower = Math.pow(pinkInput, 3);

            // 4. FLYWHEEL SPEED ADJUSTMENT
            if (gamepad1.dpad_up) {
                flywheelPower += 0.005;
            }

            if (gamepad1.dpad_down) {
                flywheelPower -= 0.005;
            }

            // Limit flywheel power
            flywheelPower = Range.clip(flywheelPower, 0.0, 1.0);

            // 5. APPLY MOTOR POWER
            tealMotor.setPower(tealPower);
            pinkMotor.setPower(pinkPower);
            flywheel.setPower(flywheelPower);

            // 6. TELEMETRY
            telemetry.addData("Drive Input", "%.2f", drive);
            telemetry.addData("Turn Input", "%.2f", turn);
            telemetry.addData("Teal Power", "%.2f", tealPower);
            telemetry.addData("Pink Power", "%.2f", pinkPower);
            telemetry.addData("Flywheel Power", "%.3f", flywheelPower);

            telemetry.update();
        }
    }
}

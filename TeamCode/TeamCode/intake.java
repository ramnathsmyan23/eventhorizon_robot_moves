
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

//This does not work because the file name and the class name are not the same , the code will compile.

@TeleOp(name = "Arcade Drive", group = "TeleOp")

public class ArcadeDriveTeleOp extends LinearOpMode {

    private DcMotor intakeMotor = null;

    @Override
    public void runOpMode() {

        // Initialize intake motor
        intakeMotor = hardwareMap.get(DcMotor.class, "intake");

        // Wait for Driver Station to start
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        // Main control loop
        while (opModeIsActive()) {

            // INTAKE FORWARD
            if (gamepad1.left_trigger > 0.25) {

                intakeMotor.setPower(0.7);

            }

            // INTAKE REVERSE
            else if (gamepad1.left_bumper) {

                intakeMotor.setPower(-0.7);

            }

            // STOP INTAKE
            else {

                intakeMotor.setPower(0.0);

            }

        }
    }
}

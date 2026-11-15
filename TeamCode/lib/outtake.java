
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

//This does not work because the file name and the class name are not the same , the code will compile.

@TeleOp(name = "Arcade Drive", group = "TeleOp")

public class ArcadeDriveTeleOp extends LinearOpMode {

    private DcMotor intakeMotor = null;
    private DcMotor outtakeMotor = null;

    @Override
    public void runOpMode() {

        intakeMotor = hardwareMap.get(DcMotor.class, "intake");
        outtakeMotor = hardwareMap.get(DcMotor.class, "outtake");

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // OUTTAKE
            if (gamepad1.right_bumper) {

                outtakeMotor.setPower(0.63);

            }
            // else if (gamepad1.left_bumper) {
            //     outtakeMotor.setPower(0.7);
            // }

            else {

                outtakeMotor.setPower(0.0);

            }

            idle();
        }
    }
}

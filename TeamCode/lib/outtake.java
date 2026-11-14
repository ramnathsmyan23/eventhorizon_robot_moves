package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name="BioBuzz Outtake", group="TeleOp")
  public class BioBuzzOuttake extends LinearOpMode {

  freethrow = hardwareMap.get(Servo.class, "A"); 

    private DcMotor outtakeMotor;
    private CRServo freethrow;

// OUTAKE
if (gamepad1.right_bumper > 0.25) {
  outtakeMotor.setPower(1.0);
}
else if (gamepad1.left_bumper > 0.25) {
  outtakeMotor.setPower(0.5);
}  else {
  outtakeMotor.setPower(0.0);
}

while (opModeIsActive()) {
            // OUTTAKE
            if (gamepad1.right_bumper) {
                outtakeMotor.setPower(1.0);
            } else if (gamepad1.left_bumper) {
                outtakeMotor.setPower(0.5);
            } else {
                outtakeMotor.setPower(0.0);
            }

            // FREETHROW
            if (gamepad1.a) {
                freethrow.setPower(1.0);
            } else {
                freethrow.setPower(0.0);
            }

]

if (gamepad1.a) {
    freethrow.setPower(1.0); 
}

package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;


freethrow = hardwareMap.get(Servo.class, "A"); 

// OUTAKE
if (gamepad1.right_bumper > 0.25) {
  outtakeMotor.setPower(1.0);
}
else if (gamepad1.left_bumper > 0.25) {
  outtakeMotor.setPower(0.5);
}  else {
  outtakeMotor.setPower(0.0);
}

]

if (gamepad1.a) {
    freethrow.setPower(1.0); 
}

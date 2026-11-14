package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Arcade Drive", group = "TeleOp") // INTERCHANGEABLE: OpMode name displayed on Driver Station
public class ArcadeDriveTeleOp extends LinearOpMode {
  private DcMotor intakeMotor = null;

  @Override
  public void runOpMode() {
    intakeMotor = hardwareMap.get(DcMotor.class, "intake");
    
    

    // OUTTAKE
        if (gamepad1.right_bumper > 0.25) {
          outtakeMotor.setPower(0.63);
        }
        //else if (gamepad1.left_bumper > 0.25) {
          //outtakeMotor.setPower(0.7);
        }
        else {
          outtakeMotor.setPower(0.0);
        }
    }
}

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
  
    // 4. INTAKE
        if (gamepad1.left_trigger > 0.1) {
          intakeMotor.setPower(0.7);
        }
        else if (gamepad1.right_trigger > 0.25) {
          intakeMotor.setPower(-1.0);
        }
        else {
          intakeMotor.setPower(0.0);
        }
    }
}

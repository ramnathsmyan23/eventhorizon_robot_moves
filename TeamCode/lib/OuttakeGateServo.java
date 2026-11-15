package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;

@TeleOp(name = "Outtake Gate Servo", group = "TeleOp")

public class OuttakeGateServo extends LinearOpMode {

    // Declare continuous rotation servo
    private CRServo gateServo;

    @Override
    public void runOpMode() {

        // Initialize servo
        gateServo = hardwareMap.get(CRServo.class, "gateServo");

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // GATE SERVO CONTROL
            if (gamepad1.a) {

                gateServo.setPower(0.8);

            } else if (gamepad1.b) {

                gateServo.setPower(-0.8);

            } else {

                gateServo.setPower(0.0);
            }

            telemetry.addData("Servo Power", gateServo.getPower());
            telemetry.update();

        }
    }
}

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotor.RunMode;
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Arcade Drive", group = "TeleOp")
public class everything extends LinearOpMode {

    // ------------------------------------------------------------------
    // FLYWHEEL CONSTANTS
    // ------------------------------------------------------------------
    // true  = read ticks-per-rev and max RPM from the motor type chosen in
    //         your Robot Configuration (recommended, if that type is right).
    // false = always use the MANUAL values below (use this after you
    //         measure ticks-per-rev with the X-button calibration).
    private static final boolean USE_CONFIGURED_MOTOR_VALUES = true;

    // Manual values (also the fallback if the configuration has no motor type).
    // Ticks per ONE revolution of the FLYWHEEL itself (include any gearbox
    // or belt ratio between the motor and the flywheel).
    private static final double MANUAL_TICKS_PER_REV = 28.0;
    private static final double MANUAL_MAX_RPM = 6000.0;

    // Full-speed RPM target the OpMode starts with.
    private static final double START_RPM = 55.0;

    // How much each D-pad step changes the full-speed RPM target.
    private static final double RPM_STEP = 5.0;

    // Hold-to-repeat: after holding D-pad up/down for HOLD_DELAY_MS,
    // the RPM keeps stepping every HOLD_REPEAT_MS until you let go.
    private static final double HOLD_DELAY_MS = 400.0;
    private static final double HOLD_REPEAT_MS = 50.0;

    // Idle speed as a fraction of the full-speed target.
    private static final double IDLE_FRACTION = 0.30;

    // "Actual RPM" is measured from encoder position change over this window.
    // Longer = steadier number, shorter = faster response.
    private static final double RPM_WINDOW_MS = 250.0;

    // Number of hand-turned revolutions used by the X-button calibration.
    private static final double CALIBRATION_REVS = 10.0;

    // Startup encoder self-test: the flywheel is spun open-loop for a
    // moment and the encoder must report movement, otherwise the encoder
    // is not connected / not working and RPM control cannot work.
    private static final double ENCODER_TEST_POWER = 0.5;
    private static final long ENCODER_TEST_MS = 500;
    private static final int ENCODER_TEST_MIN_TICKS = 5;

    // Outtake gate servo (right bumper). Hold = open, release = closed.
    // Servo positions are 0.0-1.0 across the servo's full range, so degrees
    // are converted using SERVO_RANGE_DEGREES (180 for most standard servos,
    // 270 for some goBILDA/Axon servos). Change it if 90 deg looks wrong.
    private static final double SERVO_RANGE_DEGREES = 180.0;
    private static final double GATE_CLOSED_DEGREES = 0.0;
    private static final double GATE_OPEN_DEGREES = 90.0;

    // Joystick deadband
    private static final double DEADBAND = 0.05;

    // ------------------------------------------------------------------
    // HARDWARE
    // ------------------------------------------------------------------
    private DcMotor teal;
    private DcMotor pink;
    private DcMotor intake;
    private DcMotorEx flywheel;
    private Servo gateServo;
    private String gateStatus = "OK";

    private Gamepad currentGamepad = new Gamepad();
    private Gamepad previousGamepad = new Gamepad();

    // ------------------------------------------------------------------
    // STATE
    // ------------------------------------------------------------------
    private boolean reverseMode = false;

    // Right trigger toggles full speed (shooting)
    private boolean flywheelToggledOn = false;

    // Y toggles idle. Flywheel STARTS in idle mode.
    private boolean flywheelIdleToggledOn = true;

    // Full-speed target in RPM. Starts at START_RPM. D-pad adjusts it.
    private double targetRpm = START_RPM;

    // Values actually used for RPM math (chosen at startup).
    private double ticksPerRev = MANUAL_TICKS_PER_REV;
    private double maxRpm = MANUAL_MAX_RPM;
    private String valuesSource = "manual";

    // Set by the startup self-test. true = real RPM (velocity) control,
    // false = encoder not working, fall back to open-loop power.
    private boolean encoderWorks = false;

    // X toggles calibration mode (motor off, turn the flywheel by hand).
    private boolean calibrating = false;

    // Measured RPM (from encoder position change)
    private int lastRpmPos = 0;
    private double lastRpmTimeMs = 0;
    private double measuredRpm = 0;

    // Timers for D-pad hold-to-repeat
    private final ElapsedTime clock = new ElapsedTime();
    private double upPressedAt = 0;
    private double upLastStep = 0;
    private double downPressedAt = 0;
    private double downLastStep = 0;

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------

    // setVelocity / getVelocity use ticks per second
    private double rpmToTicksPerSec(double rpm) {
        return rpm * ticksPerRev / 60.0;
    }

    private double ticksPerSecToRpm(double ticksPerSec) {
        return ticksPerSec * 60.0 / ticksPerRev;
    }

    // Spins the flywheel open-loop briefly and checks the encoder moved.
    private boolean testFlywheelEncoder() {
        flywheel.setMode(RunMode.STOP_AND_RESET_ENCODER);
        flywheel.setMode(RunMode.RUN_WITHOUT_ENCODER);

        int startPos = flywheel.getCurrentPosition();
        flywheel.setPower(ENCODER_TEST_POWER);
        sleep(ENCODER_TEST_MS);
        int endPos = flywheel.getCurrentPosition();
        flywheel.setPower(0);

        return Math.abs(endPos - startPos) >= ENCODER_TEST_MIN_TICKS;
    }

    // Puts the flywheel in the correct run mode for normal driving.
    private void applyFlywheelRunMode() {
        flywheel.setMode(RunMode.STOP_AND_RESET_ENCODER);
        if (encoderWorks) {
            flywheel.setMode(RunMode.RUN_USING_ENCODER);
        } else {
            flywheel.setMode(RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    private void resetRpmMeasurement() {
        lastRpmPos = flywheel.getCurrentPosition();
        lastRpmTimeMs = clock.milliseconds();
        measuredRpm = 0;
    }

    @Override
    public void runOpMode() {

        teal = hardwareMap.get(DcMotor.class, "teal");
        pink = hardwareMap.get(DcMotor.class, "pink");
        intake = hardwareMap.get(DcMotor.class, "intake");
        flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        // tryGet returns null (instead of crashing the whole OpMode) if "gateServo"
        // is not configured as a regular Servo. Driving and the flywheel still work.
        gateServo = hardwareMap.tryGet(Servo.class, "gateServo");
        if (gateServo == null) {
            if (hardwareMap.tryGet(CRServo.class, "gateServo") != null) {
                gateStatus = "gateServo is set as a CONTINUOUS ROTATION servo in Robot Configuration. Change its type to Servo, save, and activate it.";
            } else {
                gateStatus = "No device named gateServo in the ACTIVE Robot Configuration. Check the name, the hub, and that the config is activated.";
            }
        }

        teal.setDirection(DcMotorSimple.Direction.FORWARD);
        pink.setDirection(DcMotorSimple.Direction.REVERSE);
        flywheel.setDirection(DcMotorSimple.Direction.REVERSE);

        // Drive motors always BRAKE so the robot stops as soon as power hits 0
        teal.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
        pink.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);

        flywheel.setZeroPowerBehavior(ZeroPowerBehavior.FLOAT);

        // Pick the ticks-per-rev and max RPM used for all RPM math
        if (USE_CONFIGURED_MOTOR_VALUES) {
            double cfgTicks = flywheel.getMotorType().getTicksPerRev();
            double cfgMaxRpm = flywheel.getMotorType().getMaxRPM();
            if (cfgTicks > 0) {
                ticksPerRev = cfgTicks;
                valuesSource = "config";
            }
            if (cfgMaxRpm > 0) {
                maxRpm = cfgMaxRpm;
                valuesSource = "config";
            }
        }

        flywheel.setMode(RunMode.STOP_AND_RESET_ENCODER);
        flywheel.setMode(RunMode.RUN_WITHOUT_ENCODER);

        telemetry.addLine("Ready! (flywheel encoder test runs on START)");
        if (gateServo == null) {
            telemetry.addData("WARNING gateServo", gateStatus);
        }
        telemetry.addData("Ticks/rev | Max RPM", "%.1f | %.0f (%s)",
                ticksPerRev, maxRpm, valuesSource);
        telemetry.update();

        waitForStart();

        // -------------------------
        // FLYWHEEL ENCODER SELF-TEST (about half a second after START)
        // -------------------------
        telemetry.addLine("Testing flywheel encoder...");
        telemetry.update();

        encoderWorks = testFlywheelEncoder();
        applyFlywheelRunMode();
        resetRpmMeasurement();

        while (opModeIsActive()) {

            currentGamepad.copy(gamepad1);
            double nowMs = clock.milliseconds();

            // -------------------------
            // ARCADE DRIVE
            // -------------------------

            double drive = -currentGamepad.left_stick_y;
            double turn = currentGamepad.right_stick_x;

            // Deadband: hard zero near center so motors get exactly 0 power
            if (Math.abs(drive) < DEADBAND) drive = 0;
            if (Math.abs(turn) < DEADBAND) turn = 0;

            // Cubic sensitivity
            drive = Math.pow(drive, 3);
            turn = Math.pow(turn, 3);

            // L3 toggles reverse mode
            if (currentGamepad.left_stick_button &&
                    !previousGamepad.left_stick_button) {
                reverseMode = !reverseMode;
            }

            // 180-degree mode: forward/back is flipped, but steering is NOT
            // flipped. Pushing the stick right still spins the robot the same
            // way as normal mode, which makes the physical front swing left
            // when the back is acting as the front.
            if (reverseMode) {
                drive = -drive;
            }

            double leftPower = drive + turn;
            double rightPower = drive - turn;

            // Keep motor power within range
            double max = Math.max(1.0,
                    Math.max(Math.abs(leftPower), Math.abs(rightPower)));

            leftPower /= max;
            rightPower /= max;

            teal.setPower(leftPower);
            pink.setPower(rightPower);

            // -------------------------
            // INTAKE
            // -------------------------

            if (currentGamepad.left_trigger > 0.25) {
                intake.setPower(1.0);
            } else if (currentGamepad.left_bumper) {
                intake.setPower(-0.7);
            } else {
                intake.setPower(0);
            }

            // -------------------------
            // FLYWHEEL TOGGLES
            // -------------------------

            // Right trigger (single press) toggles full speed ON/OFF.
            // Second press returns to the baseline (idle 30% or off 0%).
            if (currentGamepad.right_trigger > 0.25 &&
                    previousGamepad.right_trigger <= 0.25) {
                flywheelToggledOn = !flywheelToggledOn;
            }

            // Y (single press) toggles idle mode ON/OFF
            if (currentGamepad.y && !previousGamepad.y) {
                flywheelIdleToggledOn = !flywheelIdleToggledOn;
            }

            // X (single press) toggles calibration mode
            if (currentGamepad.x && !previousGamepad.x) {
                calibrating = !calibrating;
                if (calibrating) {
                    // Motor off, encoder zeroed so you can count turns by hand
                    flywheel.setPower(0);
                    flywheel.setMode(RunMode.STOP_AND_RESET_ENCODER);
                    flywheel.setMode(RunMode.RUN_WITHOUT_ENCODER);
                } else {
                    applyFlywheelRunMode();
                }
                resetRpmMeasurement();
            }

            // -------------------------
            // FLYWHEEL RPM ADJUSTMENT (5 RPM per step, hold to repeat)
            // -------------------------

            // D-pad up: +5 RPM on press, keeps climbing while held (capped at max RPM)
            if (currentGamepad.dpad_up) {
                if (!previousGamepad.dpad_up) {
                    // First press: step once right away
                    upPressedAt = nowMs;
                    upLastStep = nowMs;
                    targetRpm = Math.min(maxRpm, targetRpm + RPM_STEP);
                } else if (nowMs - upPressedAt >= HOLD_DELAY_MS
                        && nowMs - upLastStep >= HOLD_REPEAT_MS) {
                    // Held long enough: repeat
                    upLastStep = nowMs;
                    targetRpm = Math.min(maxRpm, targetRpm + RPM_STEP);
                }
            }

            // D-pad down: -5 RPM on press, keeps dropping while held (not below 0)
            if (currentGamepad.dpad_down) {
                if (!previousGamepad.dpad_down) {
                    downPressedAt = nowMs;
                    downLastStep = nowMs;
                    targetRpm = Math.max(0, targetRpm - RPM_STEP);
                } else if (nowMs - downPressedAt >= HOLD_DELAY_MS
                        && nowMs - downLastStep >= HOLD_REPEAT_MS) {
                    downLastStep = nowMs;
                    targetRpm = Math.max(0, targetRpm - RPM_STEP);
                }
            }

            // -------------------------
            // FLYWHEEL OUTPUT  (this is what actually moves the real motor)
            // -------------------------

            double commandedRpm;
            String flywheelState;

            if (flywheelToggledOn) {
                commandedRpm = targetRpm;                    // full speed target
                flywheelState = "FULL";
            } else if (flywheelIdleToggledOn) {
                commandedRpm = targetRpm * IDLE_FRACTION;    // 30% idle
                flywheelState = "IDLE";
            } else {
                commandedRpm = 0;                            // off
                flywheelState = "OFF";
            }

            if (calibrating) {
                // Motor stays off so the flywheel can be turned by hand
                flywheel.setPower(0);
            } else if (encoderWorks) {
                // Real closed-loop RPM control
                flywheel.setVelocity(rpmToTicksPerSec(commandedRpm));
            } else {
                // Encoder not working: open-loop power estimate (RPM / max RPM)
                flywheel.setPower(Math.min(1.0, commandedRpm / maxRpm));
            }

            // -------------------------
            // MEASURED FLYWHEEL RPM (from encoder position change)
            // -------------------------

            if (!calibrating && nowMs - lastRpmTimeMs >= RPM_WINDOW_MS) {
                int pos = flywheel.getCurrentPosition();
                double minutes = (nowMs - lastRpmTimeMs) / 60000.0;
                measuredRpm = Math.abs(pos - lastRpmPos) / ticksPerRev / minutes;
                lastRpmPos = pos;
                lastRpmTimeMs = nowMs;
            }

            // -------------------------
            // OUTTAKE GATE SERVO (right bumper)
            // Hold = 90 degrees (open), release = 0 degrees (closed)
            // -------------------------

            boolean gateOpen = currentGamepad.right_bumper;
            double gateDegrees = gateOpen ? GATE_OPEN_DEGREES : GATE_CLOSED_DEGREES;
            double gatePosition = gateDegrees / SERVO_RANGE_DEGREES;
            if (gateServo != null) {
                gateServo.setPosition(gatePosition);
            }

            // -------------------------
            // TELEMETRY
            // -------------------------

            telemetry.addData("Reverse Mode", reverseMode ? "ON" : "OFF");

            if (calibrating) {
                double ticksCounted = Math.abs(flywheel.getCurrentPosition());
                telemetry.addLine("*** FLYWHEEL CALIBRATION - flywheel motor is OFF ***");
                telemetry.addData("Do this", "Turn the flywheel BY HAND exactly %.0f full revolutions", CALIBRATION_REVS);
                telemetry.addData("Ticks counted", "%.0f", ticksCounted);
                telemetry.addData("Measured ticks per rev", "%.1f", ticksCounted / CALIBRATION_REVS);
                telemetry.addLine("Press X to exit calibration");
            } else {
                telemetry.addData("Flywheel State", flywheelState);
                telemetry.addData("Flywheel Control",
                        encoderWorks ? "RPM (encoder OK)" : "OPEN-LOOP (NO ENCODER SIGNAL - check cable)");
                telemetry.addData("Ticks/rev | Max RPM", "%.1f | %.0f (%s)",
                        ticksPerRev, maxRpm, valuesSource);
                telemetry.addData("Full-Speed Target (RPM)", "%.0f", targetRpm);
                telemetry.addData("Flywheel Commanded (RPM)", "%.0f", commandedRpm);
                if (encoderWorks) {
                    telemetry.addData("Flywheel Actual (RPM)", "%.0f", measuredRpm);
                    telemetry.addData("Flywheel Hub Velocity (RPM)", "%.0f",
                            Math.abs(ticksPerSecToRpm(flywheel.getVelocity())));
                } else {
                    telemetry.addData("Flywheel Actual (RPM)", "n/a (no encoder)");
                    telemetry.addData("Flywheel Power Sent", "%.3f", flywheel.getPower());
                }
                telemetry.addData("Flywheel Encoder Position", flywheel.getCurrentPosition());
            }
            telemetry.addData("RB (gate button)", gateOpen ? "PRESSED" : "released");
            if (gateServo != null) {
                telemetry.addData("Outtake Gate", "%s (%.0f deg) - servo position sent: %.2f",
                        gateOpen ? "OPEN" : "CLOSED", gateDegrees, gatePosition);
            } else {
                telemetry.addData("Outtake Gate NOT WORKING", gateStatus);
            }
            telemetry.addData("Intake Power", intake.getPower());
            telemetry.update();

            // Save current gamepad state for next loop
            previousGamepad.copy(currentGamepad);
        }

        // -------------------------
        // STOP ALL MOTORS
        // -------------------------

        teal.setPower(0);
        pink.setPower(0);
        intake.setPower(0);
        flywheel.setPower(0);
    }
}

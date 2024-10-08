package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * This OpMode illustrates the concept of driving a path based on encoder counts.
 * The code is structured as a LinearOpMode
 *
 * The code REQUIRES that you DO have encoders on the wheels,
 *   otherwise you would use: RobotAutoDriveByTime;
 *
 *  This code ALSO requires that the drive Motors have been configured such that a positive
 *  power command moves them forward, and causes the encoders to count UP.
 *
 *   The desired path in this example is:
 *   - Drive forward for 48 inches
 *   - Spin right for 12 Inches
 *   - Drive Backward for 24 inches
 *   - Stop and close the claw.
 *
 *  The code is written using a method called: encoderDrive(speed, leftInches, rightInches, timeoutS)
 *  that performs the actual movement.
 *  This method assumes that each movement is relative to the last stopping place.
 *  There are other ways to perform encoder based moves, but this method is probably the simplest.
 *  This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@Autonomous(name="Robot: Auto Drive By Encoder", group="Robot")
@Disabled
public class RobotAutoDriveByEncoder_Linear extends LinearOpMode {

    /* Declare OpMode members. */
    private DcMotor         leftDriveFront   = null;
    private DcMotor         rightDriveFront  = null;
    private DcMotor         leftDriveBack   = null;
    private DcMotor         rightDriveBack   = null;


    private ElapsedTime     runtime = new ElapsedTime();

    // Calculate the COUNTS_PER_INCH for your specific drive train.
    // Go to your motor vendor website to determine your motor's COUNTS_PER_MOTOR_REV
    // For external drive gearing, set DRIVE_GEAR_REDUCTION as needed.
    // For example, use a value of 2.0 for a 12-tooth spur gear driving a 24-tooth spur gear.
    // This is gearing DOWN for less speed and more torque.
    // For gearing UP, use a gear ratio less than 1.0. Note this will affect the direction of wheel rotation.
    static final double     COUNTS_PER_MOTOR_REV    = 8192 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = (60 * 3.1415) / (70 * 3.1415);     // No External Gearing.
    static final double     WHEEL_DIAMETER_INCHES   = 2.75591 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                                                      (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double     DRIVE_SPEED             = 0.8;
    static final double     TURN_SPEED              = 0.5;
    static final int[][]    MAP = new int[144][144];
    int xLocationOnField;
    int yLocationOnField;

    //144 inches for x and y so COUNTS_PER_INCH * 144 = MAX_X
    @Override
    public void runOpMode() {

        // Initialize the drive system variables.
        leftDriveFront = hardwareMap.get(DcMotor.class, "motor1");
        rightDriveFront = hardwareMap.get(DcMotor.class, "motor2");
        leftDriveBack = hardwareMap.get(DcMotor.class, "motor3");
        rightDriveBack = hardwareMap.get(DcMotor.class, "motor3");


        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // When run, this OpMode should start both motors driving forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        leftDriveFront.setDirection(DcMotor.Direction.REVERSE);
        rightDriveFront.setDirection(DcMotor.Direction.FORWARD);
        leftDriveBack.setDirection(DcMotor.Direction.REVERSE);
        rightDriveBack.setDirection(DcMotor.Direction.FORWARD);

        leftDriveFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightDriveFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Send telemetry message to indicate successful Encoder reset
        telemetry.addData("Starting at",  "%7d :%7d",
                          leftDriveFront.getCurrentPosition(),
                          rightDriveFront.getCurrentPosition());
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();


        // Step through each leg of the path,
        // Note: Reverse movement is obtained by setting a negative distance (not speed)
        encoderDrive(DRIVE_SPEED,  48,  48, 5.0);  // S1: Forward 47 Inches with 5 Sec timeout
        encoderDrive(TURN_SPEED,   12, -12, 4.0);  // S2: Turn Right 12 Inches with 4 Sec timeout
        encoderDrive(DRIVE_SPEED, -24, -24, 4.0);  // S3: Reverse 24 Inches with 4 Sec timeout

        telemetry.addData("Path", "Complete");
        telemetry.update();
        sleep(1000);  // pause to display final telemetry message.
    }

    /*
     *  Method to perform a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the OpMode running.
     */
    public void encoderDrive(double speed, double leftInches, double rightInches, double timeoutS) {
        int newLeftTarget;
        int newRightTarget;

        // Ensure that the OpMode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newLeftTarget = leftDriveFront.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH);
            newRightTarget = rightDriveFront.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);
            leftDriveFront.setTargetPosition(newLeftTarget);
            rightDriveFront.setTargetPosition(newRightTarget);

            // Turn On RUN_TO_POSITION
            leftDriveFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightDriveFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            leftDriveFront.setPower(Math.abs(speed));
            rightDriveFront.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                   (runtime.seconds() < timeoutS) &&
                   (leftDriveFront.isBusy() && rightDriveFront.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Running to",  " %7d :%7d", newLeftTarget,  newRightTarget);
                telemetry.addData("Currently at",  " at %7d :%7d",
                                            leftDriveFront.getCurrentPosition(), rightDriveFront.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            leftDriveFront.setPower(0);
            rightDriveFront.setPower(0);

            // Turn off RUN_TO_POSITION
            leftDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(250);   // optional pause after each move.
        }
    }

    public void findRoute(int desiredXPositionOnMap, int desiredYPositionOnMap) {

        if(getXLocationOnField() < desiredXPositionOnMap && getYLocationOnField() < desiredYPositionOnMap)/*Top Left*/{
            
        } else if(getXLocationOnField() > desiredXPositionOnMap && getYLocationOnField() > desiredYPositionOnMap)/*Bottom Left*/{

        } else if(getXLocationOnField() < desiredXPositionOnMap && getYLocationOnField() > desiredYPositionOnMap)/*Bottom Right*/{

        } else if(getXLocationOnField() > desiredXPositionOnMap && getYLocationOnField() < desiredYPositionOnMap)/*Top Right*/{

        } else if(getXLocationOnField() > desiredXPositionOnMap && getYLocationOnField() == desiredYPositionOnMap)/*Left*/{

        } else if(getXLocationOnField() > desiredXPositionOnMap && getYLocationOnField() == desiredYPositionOnMap)/*Right*/{

        } else if(getXLocationOnField() == desiredXPositionOnMap && getYLocationOnField() < desiredYPositionOnMap)/*UP*/{

        } else if(getXLocationOnField() == desiredXPositionOnMap && getYLocationOnField() > desiredYPositionOnMap)/*Down*/{
            int moveY = desiredYPositionOnMap - getYLocationOnField();
        }
    }

    public void moveForward(int inches){
        leftDriveFront.setDirection(DcMotor.Direction.REVERSE);
        rightDriveFront.setDirection(DcMotor.Direction.FORWARD);
        leftDriveBack.setDirection(DcMotor.Direction.REVERSE);
        rightDriveBack.setDirection(DcMotor.Direction.FORWARD);

        leftDriveFront.setPower(1.0);
        rightDriveFront.setPower(1.0);
        leftDriveBack.setPower(1.0);
        rightDriveBack.setPower(1.0);
    }

    public void moveBackward(int inches){
        leftDriveFront.setDirection(DcMotor.Direction.FORWARD);
        rightDriveFront.setDirection(DcMotor.Direction.REVERSE);
        leftDriveBack.setDirection(DcMotor.Direction.FORWARD);
        rightDriveBack.setDirection(DcMotor.Direction.REVERSE);

        leftDriveFront.setPower(1.0);
        rightDriveFront.setPower(1.0);
        leftDriveBack.setPower(1.0);
        rightDriveBack.setPower(1.0);
    }

    public void moveRight(int inches){
        leftDriveFront.setDirection(DcMotor.Direction.FORWARD);
        rightDriveFront.setDirection(DcMotor.Direction.REVERSE);
        leftDriveBack.setDirection(DcMotor.Direction.REVERSE);
        rightDriveBack.setDirection(DcMotor.Direction.FORWARD);

        leftDriveFront.setPower(1.0);
        rightDriveFront.setPower(1.0);
        leftDriveBack.setPower(1.0);
        rightDriveBack.setPower(1.0);
    }

    public void moveLeft(int inches){
        leftDriveFront.setDirection(DcMotor.Direction.REVERSE);
        rightDriveFront.setDirection(DcMotor.Direction.FORWARD);
        leftDriveBack.setDirection(DcMotor.Direction.FORWARD);
        rightDriveBack.setDirection(DcMotor.Direction.REVERSE);

        leftDriveFront.setPower(1.0);
        rightDriveFront.setPower(1.0);
        leftDriveBack.setPower(1.0);
        rightDriveBack.setPower(1.0);
    }

    public void moveDiagonalTopLeft(int inches){
        rightDriveFront.setDirection(DcMotor.Direction.FORWARD);
        leftDriveBack.setDirection(DcMotor.Direction.FORWARD);

        rightDriveFront.setPower(1.0);
        leftDriveBack.setPower(1.0);
    }

    public void moveDiagonalTopRight(int inches){
        leftDriveFront.setDirection(DcMotor.Direction.FORWARD);
        rightDriveBack.setDirection(DcMotor.Direction.FORWARD);

        leftDriveFront.setPower(1.0);
        rightDriveBack.setPower(1.0);
    }

    public void moveDiagonalBotLeft(int inches){
        leftDriveFront.setDirection(DcMotor.Direction.REVERSE);
        rightDriveBack.setDirection(DcMotor.Direction.REVERSE);

        leftDriveFront.setPower(1.0);
        rightDriveBack.setPower(1.0);
    }

    public void moveDiagonalBotRight(int inches){
        rightDriveFront.setDirection(DcMotor.Direction.REVERSE);
        leftDriveBack.setDirection(DcMotor.Direction.REVERSE);

        rightDriveFront.setPower(1.0);
        leftDriveBack.setPower(1.0);
    }

    //Getters for location on the field
    public int getXLocationOnField(){
        return xLocationOnField;
    }
    public int getYLocationOnField(){
        return yLocationOnField;
    }

    //Setters for location on the Field
    public void setXLocationOnField(int setXLocation){
        xLocationOnField = setXLocation;
    }
    public void setYLocationOnField(int setYLocation){
        yLocationOnField = setYLocation;
    }
}

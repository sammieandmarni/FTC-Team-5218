package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDriveDeadReckon;
import team25core.PeriodicTimerTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;

/**
 * Created by Lizzie on 11/19/2016.
 */
@Autonomous(name = "(S) Corner (G) Particle Cap Ball", group = "AutoTest")
public class MochaParticleCapBallAutonomous extends Robot {

    private DcMotorController mc;
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private DcMotor shooterLeft;
    private DcMotor shooterRight;
    private DcMotor sbod;
    private int paddleCount;

    private RunToEncoderValueTask scoreCenterDeadReckonTask;
    private PeriodicTimerTask ptt;
    private FourWheelDirectDriveDeadReckon pushCapDeadReckon;
    private DeadReckonTask pushCapDeadReckonTask;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (paddleCount >= 10) {
            ptt.stop();
            stopShooter();
            addTask(pushCapDeadReckonTask);
        }
        if (e instanceof PeriodicTimerTask.PeriodicTimerEvent) {
            RobotLog.i("163: Period timer task expired, %d", paddleCount);
            paddleCount++;
            addTask(scoreCenterDeadReckonTask);
        }
    }

    @Override
    public void init()
    {
        paddleCount = 0;
        frontLeft = hardwareMap.dcMotor.get("motorFL");
        frontRight = hardwareMap.dcMotor.get("motorFR");
        backLeft = hardwareMap.dcMotor.get("motorBL");
        backRight = hardwareMap.dcMotor.get("motorBR");
        shooterLeft = hardwareMap.dcMotor.get("shooterLeft");
        shooterRight = hardwareMap.dcMotor.get("shooterRight");

        sbod = hardwareMap.dcMotor.get("brush");

        mc = hardwareMap.dcMotorController.get("mechanisms");

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        scoreCenterDeadReckonTask = new RunToEncoderValueTask(this, sbod, 1, .8);
        ptt = new PeriodicTimerTask(this, 300);

        pushCapDeadReckon = new FourWheelDirectDriveDeadReckon
                (this, MochaCalibration.TICKS_PER_INCH, MochaCalibration.TICKS_PER_DEGREE, frontRight, backRight, frontLeft, backLeft);
        pushCapDeadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 68, -.75);
        pushCapDeadReckonTask = new DeadReckonTask(this, pushCapDeadReckon);
    }

    protected void startShooter()

    {
        shooterLeft.setPower(.575);
        shooterRight.setPower(-.575);
    }

    protected void stopShooter()
    {
        shooterLeft.setPower(0);
        shooterRight.setPower(0);
    }

    @Override
    public void start()
    {
        startShooter();
        this.addTask(new SingleShotTimerTask(this, 3000) {
            @Override
            public void handleEvent(RobotEvent e) {
                robot.addTask(ptt);
                robot.addTask(scoreCenterDeadReckonTask);
            }
        });
    }
}

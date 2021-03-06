/* 
 * Title: LauncherController
 * Author: Peter Quinn
 * Desc: Provides methods to control the launching mechanism of the robot
 * Created: March 17, 2016
 * 
 * Edit Log:
 */

package soccer;

import lejos.hardware.ev3.EV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;

/**
 * 
 * @author Peter Quinn
 *
 */
public class LauncherController {

	// motors
	private RegulatedMotor launcherRight;
	private RegulatedMotor launcherLeft;
	private RegulatedMotor angleAdjustMotor;
	private EV3LargeRegulatedMotor conveyerRight;
	private EV3LargeRegulatedMotor conveyerLeft;

	// data for calculations
	private double launcherWheelRadius;
	private double conveyerWheelRadius;
	private double ballDiameter;

	// launcher speeds and accelerations
	private final int FIRING_SPEED = 600;
	private final int LAUNCHER_ACCELERATION = 2000;
	private final int INTAKE_SPEED = 150;
	private final int REJECT_SPEED = 90;
	private final int CONVEYER_SPEED = 150;
	private final int CONVEYER_OFFSET = 0; // conveyer rotates this extra
											// amount (degrees)
	private final int SPINUP_DELAY = 500; // time allowed for motors to speed up

	private final int FIRE_ANGLE = 15; // angle to aim the ball basket at
	private final int ANGLE_SPEED = 50;

	private final int NUDGE_SPEED = 250;

	/**
	 * 
	 * @param launherRight
	 * @param launcherLeft
	 * @param conveyerRight
	 * @param conveyerLeft
	 * @param launcherWheelRadius
	 * @param conveyerWheelRadius
	 * @param ballDiameter
	 */
	public LauncherController(Motors motors) {
		this.launcherRight = motors.getLauncherRight();
		this.launcherLeft = motors.getLauncherLeft();
		this.conveyerRight = motors.getConveyerRight();
		this.conveyerLeft = motors.getConveyerLeft();
		this.ballDiameter = PhysicalConstants.BALL_DIAMTER;
		this.conveyerWheelRadius=PhysicalConstants.CONVEYER_WHEEL_RADIUS;
		this.angleAdjustMotor = motors.getAngleAdjustMotor();
	}

	// stops launcher motors
	/**
	 * <p>
	 * This method stops the motors of the launcher
	 */
	public void stopLauncher() {
		launcherLeft.setSpeed(0);
		launcherRight.setSpeed(0);
		launcherLeft.forward();
		launcherRight.forward();
		launcherLeft.flt();
		launcherRight.flt();
	}

	// set launcher motors to firing speed
	// set launcher motors to firing speed
	/**
	 * <p>
	 * sets the firing speed of the launcher
	 */
	public void setToFiringSpeed() {
		launcherLeft.setSpeed(FIRING_SPEED);
		launcherRight.setSpeed(FIRING_SPEED);
		launcherLeft.setAcceleration(LAUNCHER_ACCELERATION);
		launcherRight.setAcceleration(LAUNCHER_ACCELERATION);
		launcherLeft.forward();
		launcherRight.forward();
		try {
			Thread.sleep(SPINUP_DELAY);
		} catch (InterruptedException e) {
		}

	}

	/**
	 * <p>
	 * Sets the motors to the speed required to retrieve a ball
	 */
	public void setToIntakeSpeed() {
		launcherLeft.setSpeed(INTAKE_SPEED);
		launcherRight.setSpeed(INTAKE_SPEED);
		launcherLeft.setAcceleration(LAUNCHER_ACCELERATION);
		launcherRight.setAcceleration(LAUNCHER_ACCELERATION);
		launcherLeft.backward();
		launcherRight.backward();
		try {
			Thread.sleep(SPINUP_DELAY);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * <p>
	 * Sets the motors to the speed required to release an incorrect ball
	 */
	public void setToRejectSpeed() {
		launcherLeft.setSpeed(REJECT_SPEED);
		launcherRight.setSpeed(REJECT_SPEED);
		launcherLeft.setAcceleration(LAUNCHER_ACCELERATION);
		launcherRight.setAcceleration(LAUNCHER_ACCELERATION);
		launcherLeft.backward();
		launcherRight.backward();
		try {
			Thread.sleep(SPINUP_DELAY);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * <p>
	 * Moves the conveyer backwards by the size of a single ball
	 */
	public void conveyerBackOneBall() {
		double theta = 360 * ballDiameter / (2 * Math.PI * conveyerWheelRadius) + CONVEYER_OFFSET;
		conveyerLeft.setSpeed(CONVEYER_SPEED);
		conveyerRight.setSpeed(CONVEYER_SPEED);
		conveyerLeft.rotate((int) -theta, true);
		conveyerRight.rotate((int) -theta, false);
	}
	
	/**
	 * <p>
	 * rotates the conveyer inward
	 */
	public void conveyerBack(){
		conveyerLeft.setSpeed(CONVEYER_SPEED);
		conveyerRight.setSpeed(CONVEYER_SPEED);
		conveyerLeft.backward();
		conveyerRight.backward();
	}
	
	/**
	 * <p>
	 * rotates the conveyer outward
	 */
	public void conveyerForward(){
		conveyerLeft.setSpeed(CONVEYER_SPEED);
		conveyerRight.setSpeed(CONVEYER_SPEED);
		conveyerLeft.forward();
		conveyerRight.forward();
	}
	
	public void conveyerStop(){
		conveyerLeft.stop(true);
		conveyerRight.stop(false);
	}

	/**
	 * <p>
	 * Moves the conveyer forwards by the size of a single ball
	 */
	public void conveyerForwardOneBall() {
		double theta = 360 * ballDiameter / (2 * Math.PI * conveyerWheelRadius) + CONVEYER_OFFSET;
		conveyerLeft.setSpeed(CONVEYER_SPEED);
		conveyerRight.setSpeed(CONVEYER_SPEED);
		conveyerLeft.rotate((int) theta, true);
		conveyerRight.rotate((int) theta, false);
	}

	/**
	 * <p>
	 * raises the firing angle of the ramp
	 */
	public void raiseAngle() {
		angleAdjustMotor.setSpeed(ANGLE_SPEED);
		angleAdjustMotor.rotate(FIRE_ANGLE);
		
	}

	/**
	 * <p>
	 * lowers the firing angle of the ramp
	 */
	public void lowerAngle() {
		angleAdjustMotor.setSpeed(ANGLE_SPEED);
		angleAdjustMotor.rotate(-FIRE_ANGLE);
		angleAdjustMotor.flt();
	}

}

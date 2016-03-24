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
	private final int FIRING_SPEED = 250;
	private final int LAUNCHER_ACCELERATION = 2000;
	private final int INTAKE_SPEED = 90;
	private final int REJECT_SPEED = 90;
	private final int CONVEYER_SPEED = 100;
	private final int CONVEYER_OFFSET = 10; // conveyer rotates this extra
											// amount (degrees)
	private final int SPINUP_DELAY = 500; // time allowed for motors to speed up
	private final int FIRE_ANGLE = 10; //angle to aim the ball basket at
	private final int ANGLE_SPEED = 50; 

	public LauncherController(RegulatedMotor launcherRight, RegulatedMotor launcherLeft, RegulatedMotor angleAdjustMotor,
			EV3LargeRegulatedMotor conveyerRight, EV3LargeRegulatedMotor conveyerLeft, double launcherWheelRadius,
			double conveyerWheelRadius, double ballDiameter) {
		super();
		this.launcherRight = launcherRight;
		this.launcherLeft = launcherLeft;
		this.conveyerRight = conveyerRight;
		this.conveyerLeft = conveyerLeft;
		this.launcherWheelRadius = launcherWheelRadius;
		this.conveyerWheelRadius = conveyerWheelRadius;
		this.ballDiameter = ballDiameter;
		this.angleAdjustMotor = angleAdjustMotor;
	}

	// stops launcher motors
	public void stopLauncher() {
		launcherLeft.setSpeed(0);
		launcherRight.setSpeed(0);
		launcherLeft.forward();
		launcherRight.forward();
		launcherLeft.flt();
		launcherRight.flt();
	}

	// set launcher motors to firing speed
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

	public void conveyerBackOneBall() {
		double theta = 360 * ballDiameter / (2 * Math.PI * conveyerWheelRadius) + CONVEYER_OFFSET;
		conveyerLeft.setSpeed(CONVEYER_SPEED);
		conveyerRight.setSpeed(CONVEYER_SPEED);
		conveyerLeft.rotate((int) -theta, true);
		conveyerRight.rotate((int) -theta, false);
	}

	public void conveyerForwardOneBall() {
		double theta = 360 * ballDiameter / (2 * Math.PI * conveyerWheelRadius) + CONVEYER_OFFSET;
		conveyerLeft.setSpeed(CONVEYER_SPEED);
		conveyerRight.setSpeed(CONVEYER_SPEED);
		conveyerLeft.rotate((int) theta, true);
		conveyerRight.rotate((int) theta, false);
	}
	
	public void raiseAngle(){
		angleAdjustMotor.setSpeed(ANGLE_SPEED);
		angleAdjustMotor.rotate(FIRE_ANGLE);
	}
	public void lowerAngle(){
		angleAdjustMotor.setSpeed(ANGLE_SPEED);
		angleAdjustMotor.rotate(-FIRE_ANGLE);
	}
	

}

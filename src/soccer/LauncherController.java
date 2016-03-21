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
	private EV3LargeRegulatedMotor conveyerRight;
	private EV3LargeRegulatedMotor conveyerLeft;

	// data for calculations
	private double launcherWheelRadius;
	private double conveyerWheelRadius;
	private double ballDiameter;

	// launcher speeds and accelerations
	private final int FIRING_SPEED = 180;
	private final int LAUNCHER_ACCELERATION = 2000;
	private final int INTAKE_SPEED = 50;
	private final int REJECT_SPEED = 50;
	private final int CONVEYER_SPEED = 70;
	private final int CONVEYER_OFFSET = 10; //conveyer rotates this extra amount

	public LauncherController(RegulatedMotor launherRight, RegulatedMotor launcherLeft,
			EV3LargeRegulatedMotor conveyerRight, EV3LargeRegulatedMotor conveyerLeft, double launcherWheelRadius,
			double conveyerWheelRadius, double ballDiameter) {
		super();
		this.launcherRight = launherRight;
		this.launcherLeft = launcherLeft;
		this.conveyerRight = conveyerRight;
		this.conveyerLeft = conveyerLeft;
		this.launcherWheelRadius = launcherWheelRadius;
		this.conveyerWheelRadius = conveyerWheelRadius;
		this.ballDiameter = ballDiameter;
	}
	
	//stops launcher motors
	public void stopLauncher(){
		launcherLeft.setSpeed(0);
		launcherRight.setSpeed(0);
		launcherLeft.forward();
		launcherRight.forward();
	}

	//set launcher motors to firing speed
	public void setToFiringSpeed() {
		launcherLeft.setSpeed(FIRING_SPEED);
		launcherRight.setSpeed(FIRING_SPEED);
		launcherLeft.setAcceleration(LAUNCHER_ACCELERATION);
		launcherRight.setAcceleration(LAUNCHER_ACCELERATION);
		launcherLeft.forward();
		launcherRight.forward();
	}
	
	public void setToIntakeSpeed(){
		launcherLeft.setSpeed(INTAKE_SPEED);
		launcherRight.setSpeed(INTAKE_SPEED);
		launcherLeft.setAcceleration(LAUNCHER_ACCELERATION);
		launcherRight.setAcceleration(LAUNCHER_ACCELERATION);
		launcherLeft.forward();
		launcherRight.forward();
	}
	
	public void setToRejectSpeed(){
		launcherLeft.setSpeed(REJECT_SPEED);
		launcherRight.setSpeed(REJECT_SPEED);
		launcherLeft.setAcceleration(LAUNCHER_ACCELERATION);
		launcherRight.setAcceleration(LAUNCHER_ACCELERATION);
		launcherLeft.backward();
		launcherRight.backward();
	}

	public void conveyerBackOneBall(){
		double theta = 360*ballDiameter/(2*Math.PI*conveyerWheelRadius)+CONVEYER_OFFSET;
		conveyerLeft.setSpeed(CONVEYER_SPEED);
		conveyerRight.setSpeed(CONVEYER_SPEED);
		conveyerLeft.rotate((int) -theta, true);
		conveyerRight.rotate((int) -theta, false);
	}
	
	public void conveyerForwardOneBall(){
		double theta = 360*ballDiameter/(2*Math.PI*conveyerWheelRadius)+CONVEYER_OFFSET;
		conveyerLeft.setSpeed(CONVEYER_SPEED);
		conveyerRight.setSpeed(CONVEYER_SPEED);
		conveyerLeft.rotate((int) theta, true);
		conveyerRight.rotate((int) theta, false);
	}
}

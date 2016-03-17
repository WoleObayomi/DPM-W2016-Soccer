package soccer;

import lejos.hardware.Brick;
/*Author: Peter Quinn
* Initial Creation Date: March 12, 2016
* 
* Description: Class to create an object to set up and allow easy access to the motors on 
* the two EV3 bricks by other classes
*  
* Edit Log:
*/

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

/**
 * 
 * @author Peter Quinn
 * @author Wole Obayomi
 *
 */
public class Motors {

	// brick variables
	Brick masterBrick;
	Brick slaveBrick;

	// motor variables
	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;
	EV3LargeRegulatedMotor launcherRight;
	EV3LargeRegulatedMotor launcherLeft;

	/**
	 * 
	 * @param masterBrick
	 * @param slaveBrick
	 */
	public Motors(Brick masterBrick, Brick slaveBrick) {
		this.masterBrick = masterBrick;
		this.slaveBrick = slaveBrick;

		// get ports from bricks and assign the motors the arbitrary ports for
		// now
		leftMotor = new ReversedEV3LargeMotor(masterBrick.getPort("A"));
		rightMotor = new ReversedEV3LargeMotor(masterBrick.getPort("D"));

		// launcherLeft = new EV3LargeRegulatedMotor();
		// launcherRight = new EV3LargeRegulatedMotor();
	}

	// getters for motors
	/**
	 * 
	 * @return leftMotor instance of the left motor object
	 */
	public EV3LargeRegulatedMotor getLeftMotor() {
		return leftMotor;

	}

	/**
	 * 
	 * @return rightMotor instance of the right motor object
	 */
	public EV3LargeRegulatedMotor getRightMotor() {
		return rightMotor;
	}

	/**
	 * 
	 * @return launcherRight instance of the right launcher motor
	 */
	public EV3LargeRegulatedMotor getLauncherRight() {
		return launcherRight;
	}

	/**
	 * 
	 * @return launcherLeft instance of the left launcher motor
	 */
	public EV3LargeRegulatedMotor getLauncherLeft() {
		return launcherLeft;
	}
}

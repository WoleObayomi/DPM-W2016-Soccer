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
	private Brick masterBrick;
	private Brick slaveBrick;

	// motor variables
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static EV3LargeRegulatedMotor launcherRight;
	private static EV3LargeRegulatedMotor launcherLeft;
	private static EV3LargeRegulatedMotor conveyerRight;
	private static EV3LargeRegulatedMotor conveyerLeft;

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

		//launcher motors
		launcherLeft = new EV3LargeRegulatedMotor(slaveBrick.getPort("A"));
		launcherRight = new EV3LargeRegulatedMotor(slaveBrick.getPort("D"));
		conveyerLeft = new EV3LargeRegulatedMotor(masterBrick.getPort("B"));
		conveyerRight = new EV3LargeRegulatedMotor(masterBrick.getPort("C"));
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

	/**
	 * @return the conveyerRight
	 */
	public static EV3LargeRegulatedMotor getConveyerRight() {
		return conveyerRight;
	}

	/**
	 * @return the conveyerLeft
	 */
	public static EV3LargeRegulatedMotor getConveyerLeft() {
		return conveyerLeft;
	}
	
	
	
	
}

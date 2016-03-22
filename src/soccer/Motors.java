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
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.remote.ev3.RemoteRequestRegulatedMotor;
import lejos.robotics.RegulatedMotor;

/**
 * 
 * @author Peter Quinn
 * @author Wole Obayomi
 *
 */
public class Motors {

	// brick variables
	private Brick masterBrick;
	private RemoteRequestEV3 slaveBrick;

	// motor variables
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static RegulatedMotor launcherRight;
	private static RegulatedMotor launcherLeft;
	private static RegulatedMotor angleAdjustMotor;
	private static EV3LargeRegulatedMotor conveyerRight;
	private static EV3LargeRegulatedMotor conveyerLeft;
	

	/**
	 * 
	 * @param masterBrick
	 * @param slaveBrick
	 */
	public Motors(Brick masterBrick, RemoteRequestEV3 slaveBrick) {
		this.masterBrick = masterBrick;
		this.slaveBrick = slaveBrick;

		// get ports from bricks and assign the motors the arbitrary ports for
		// now
		leftMotor = new ReversedEV3LargeMotor(masterBrick.getPort("A"));
		rightMotor = new ReversedEV3LargeMotor(masterBrick.getPort("D"));

		// launcher motors
		
		launcherLeft = slaveBrick.createRegulatedMotor("A", 'L');
		launcherRight = slaveBrick.createRegulatedMotor("D", 'L');
		
		angleAdjustMotor = slaveBrick.createRegulatedMotor("B", 'L');
		
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
	public RegulatedMotor getLauncherRight() {
		return launcherRight;
	}

	/**
	 * 
	 * @return launcherLeft instance of the left launcher motor
	 */
	public RegulatedMotor getLauncherLeft() {
		return launcherLeft;
	}

	/**
	 * @return the angleAdjustMotor
	 */
	public RegulatedMotor getAngleAdjustMotor() {
		return angleAdjustMotor;
	}

	/**
	 * @return the conveyerRight
	 */
	public EV3LargeRegulatedMotor getConveyerRight() {
		return conveyerRight;
	}

	/**
	 * @return the conveyerLeft
	 */
	public EV3LargeRegulatedMotor getConveyerLeft() {
		return conveyerLeft;
	}
	


}

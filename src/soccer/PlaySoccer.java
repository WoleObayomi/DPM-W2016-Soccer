/*
 * Author: Peter Quinn
 * Initial Creation Date: March 10, 2016
 * Description: Main class for soccer playing robot
 *  
 * Edit Log:
 * 
 *  March 12 - Peter: added the main with code for getting second brick and 
 *  setting up motors and sensors objects
 * 
 */

package soccer;

import lejos.hardware.Brick;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.remote.ev3.RemoteEV3;

/**
 * 
 * @author Peter Quinn
 *
 */
public class PlaySoccer {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// get the second brick
		Brick masterBrick = LocalEV3.get();
		Brick slaveBrick = null;
		TextLCD masterLCD = masterBrick.getTextLCD();
		try {
			slaveBrick = new RemoteEV3("10.0.1.2");
		} catch (Exception e) {
			// error message if it can't find the second brick
			masterLCD.clear();
			masterLCD.drawString("Error finding brick", 0, 0);
			masterLCD.drawString("Exiting...", 0, 0);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.exit(0);
		}

		// set up objects we need

		// exit thread
		Exit exit = new Exit();
		exit.start();

		// motors object
		Motors motors = new Motors(masterBrick, slaveBrick);

		// sensors object
		Sensors sensors = new Sensors(masterBrick, slaveBrick);

		// odometer thread
		Odometer odometer = new Odometer(motors, PhysicalConstants.WHEEL_RADIUS, PhysicalConstants.TRACK_WIDTH);
		odometer.start();

		// navigation controller
		Navigation nav = new Navigation(odometer, motors, sensors, PhysicalConstants.WHEEL_RADIUS,
				PhysicalConstants.WHEEL_RADIUS, PhysicalConstants.TRACK_WIDTH);

		// determine which planner to use from eventual wifi connection
		// and create the appropriate one
	}

}

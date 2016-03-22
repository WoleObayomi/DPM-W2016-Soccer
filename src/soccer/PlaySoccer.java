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
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.remote.ev3.RemoteEV3;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.robotics.SampleProvider;

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
		RemoteRequestEV3 slaveBrick = null;
		TextLCD masterLCD = masterBrick.getTextLCD();
		masterLCD.drawString("Connecting...", 0, 0);
		try {
			slaveBrick = new RemoteRequestEV3(BrickFinder.discover()[0].getIPAddress());
			masterLCD.drawString("Slave connected", 0, 0);
			Thread.sleep(1000);
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
		Exit exit = new Exit(slaveBrick);
		exit.start();

		// motors object
		Motors motors = new Motors(masterBrick, slaveBrick);

		// sensors object
		Sensors sensors = null;
		

		// odometer thread
		Odometer odometer = new Odometer(motors, PhysicalConstants.WHEEL_RADIUS, PhysicalConstants.TRACK_WIDTH);
		odometer.start();

		
		// odometry display for debugging
		OdometryDisplay odoDisp = new OdometryDisplay(odometer, masterLCD);
		odoDisp.start();

		// navigation controller
		Navigation nav = new Navigation(odometer, motors, sensors, PhysicalConstants.WHEEL_RADIUS,
				PhysicalConstants.WHEEL_RADIUS, PhysicalConstants.TRACK_WIDTH);

		

		LauncherController launcher = new LauncherController(motors.getLauncherRight(), motors.getLauncherLeft(), motors.getAngleAdjustMotor(),
				motors.getConveyerRight(), motors.getConveyerLeft(), PhysicalConstants.LAUNCHER_WHEEL_RADIUS,
				PhysicalConstants.CONVEYER_WHEEL_RADIUS, PhysicalConstants.BALL_DIAMTER);

		launcher.setToIntakeSpeed();
		nav.travel(15);
		launcher.conveyerBackOneBall();
		launcher.stopLauncher();
		nav.travel(-4);
		nav.turnTo(90);
		launcher.raiseAngle();
		launcher.setToFiringSpeed();
		launcher.conveyerForwardOneBall();
		launcher.conveyerForwardOneBall();
		launcher.lowerAngle();
		launcher.stopLauncher();
		
		
		System.exit(0);

		// determine which planner to use from eventual wifi connection
		// and create the appropriate one below

	}

}

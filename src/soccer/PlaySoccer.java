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

import java.util.concurrent.Callable;

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
	
	private void moveTowardTileIntersection(Sensors sensors, Navigation nav) {
		double distFromSideUS = sensors.getSideDist();
		double distFromFrontUS = sensors.getFrontDist();
		
		/* 
		 * assuming robot starts in orientation such that
		 * the front US sensor sees nothing and the side US
		 * sensor is close to the wall on the right
		 */
		if(distFromSideUS < 15) {
			nav.turnTo(90); //turn to face the left (assuming counterclockwise measurement for angles)
			nav.travel(4);	//keep moving to the left until desired distance from wall
			nav.turnTo(0);	//return to original orientation
		}
		
		/*
		 * if front facing ultrasonic sensor is too close to a wall
		 * then rotate the robot by 180 degrees so it no longer faces a wall
		 */
		if(distFromFrontUS < 15) {
			nav.turnTo(180); 
		}
	}
	
	public static void main(String[] args) {

		// get the second brick
		Brick masterBrick = LocalEV3.get();
		RemoteRequestEV3 slaveBrick = null;
		TextLCD masterLCD = masterBrick.getTextLCD();
		masterLCD.drawString("Connecting...", 0, 0);
		try {
			slaveBrick = new RemoteRequestEV3(BrickFinder.discover()[0].getIPAddress());
			masterLCD.drawString("Slave connected", 0, 0);

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
		final Sensors sensors = new Sensors(masterBrick, slaveBrick);

		// odometer thread
		Odometer odometer = new Odometer(motors, PhysicalConstants.LEFT_WHEEL_RADIUS,
				PhysicalConstants.RIGHT_WHEEL_RADIUS, PhysicalConstants.TRACK_WIDTH);
		odometer.start();

		// odometry display for debugging
		OdometryDisplay odoDisp = new OdometryDisplay(odometer, masterLCD);
		odoDisp.start();

		// navigation controller
		Navigation nav = new Navigation(odometer, motors, sensors, PhysicalConstants.LEFT_WHEEL_RADIUS,
				PhysicalConstants.RIGHT_WHEEL_RADIUS, PhysicalConstants.TRACK_WIDTH);

		// odometry correction

		OdometryCorrection odoCorrection = new OdometryCorrection(odometer, sensors);
		odoCorrection.start();

		// PUT TESTING CODE HERE

		// create USLocalization obj and use the method in it

		new USLocalization(sensors, odometer, motors.getLeftMotor(), motors.getRightMotor()).doLocalization();

		// do light localization
		new LightLocalizer(odometer, sensors, nav).doLocalization();

		nav.travelTo(0, 0, false);
		nav.turnToAbs(0);

		// determine which planner to use from eventual wifi connection
		// and create the appropriate one below

	}

}

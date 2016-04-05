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
 *  March 30 - Peter: added code to determine how to adjust odometer based on starting
 *  corner 
 * 
 * 	April 3 - Peter: added code to implement the wifi class we were provided
 */

package soccer;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.swing.plaf.basic.BasicTreeUI.TreeTraverseAction;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.Sounds;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.remote.ev3.RemoteEV3;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.robotics.SampleProvider;
import sun.launcher.resources.launcher_zh_CN;
import wifi.WifiConnection;

/**
 * 
 * @author Peter Quinn
 *
 */
public class PlaySoccer {

	// Data for/from wifi class
	private int teamNumber = 9;
	private String serverAddress = "";

	private int SC, role, w1, d1, d2, llX, llY, urX, urY, BC;

	/**
	 * @param sensors
	 * @param nav
	 * 
	 *            <p>
	 *            prevents the robot from colliding with the wall when initially
	 *            placed in the field and prior to localization
	 */

	public static void main(String[] args) {

		int llX = 6;
		int llY = 5;
		int urX = 7;
		int urY = 6;
		int SC = 2;

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

		// launcher
		LauncherController launcher = new LauncherController(motors);

		// create USLocalization obj and use the method in it

		// Sound.setVolume(85);
		// Sound.beepSequence();

		// new USLocalization(sensors, odometer, motors.getLeftMotor(),
		// motors.getRightMotor(), nav).doLocalization();

		// localize with light
		// new LightLocalizer(odometer, sensors, nav).doLocalization();
		// Sound.beepSequence();
		// Sound.setVolume(0);

		// nav.travelTo(0, 0, false);
		// nav.turnToAbs(0);

		
	
		// start odometry correction
		OdometryCorrection odoCorrection = new OdometryCorrection(odometer, sensors);
		odoCorrection.start();

		nav.travelTo((0 * PhysicalConstants.TILE_SPACING), (5 * PhysicalConstants.TILE_SPACING), true, true);
		nav.travelTo((0 * PhysicalConstants.TILE_SPACING), (0 * PhysicalConstants.TILE_SPACING), true, true);
		nav.turnTo(180);

		// determine which planner to use from eventual wifi connection
		// and create the appropriate one below

	}

	private void connectToWifi() {
		WifiConnection wifi = null;
		try {
			wifi = new WifiConnection(serverAddress, teamNumber);
		} catch (IOException e) {
		}
		SC = wifi.StartData.get("SC");
		role = wifi.StartData.get("Role");
		w1 = wifi.StartData.get("w1");
		d1 = wifi.StartData.get("d1");
		d2 = wifi.StartData.get("d2");
		llX = wifi.StartData.get("ll-x");
		llY = wifi.StartData.get("ll-y");
		urX = wifi.StartData.get("ur-x");
		urY = wifi.StartData.get("yr-y");
		BC = wifi.StartData.get("BC");

	}

	/**
	 * 
	 * @param SC
	 * @param odometer
	 * 
	 * <p>
	 * Moves robot to starting corner
	 */
	private void applyStartingCorner(int SC, Odometer odometer) {
		switch (SC) {
		case 1:
			// do nothing
			break;
		case 2:
			odometer.setX(odometer.getX() + 10 * PhysicalConstants.TILE_SPACING);
			odometer.setTheta(odometer.getTheta() - 90);
			break;
		case 3:
			odometer.setX(odometer.getX() + 10 * PhysicalConstants.TILE_SPACING);
			odometer.setY(odometer.getY() + 10 * PhysicalConstants.TILE_SPACING);
			odometer.setTheta(odometer.getTheta() + 180);
			break;
		case 4:
			odometer.setY(odometer.getY() + 10 * PhysicalConstants.TILE_SPACING);
			odometer.setTheta(odometer.getTheta() + 90);
			break;
		default:
			break;
		}

	}

}

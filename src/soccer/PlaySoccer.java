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
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import sun.launcher.resources.launcher_zh_CN;
import wifi.WifiConnection;

/**
 * 
 * @author Peter Quinn
 *
 */
public class PlaySoccer {
	private static DataLogger dl;
	// Data for/from wifi class
	private static int teamNumber = 9;
	private static String serverAddress = "192.168.10.122";

	private static int topCornerX = 10;// tiles
	private static int topCornerY = 10;// tiles
	private static int DTN, OTN, DSC, OSC, role, w1, d1, d2, llX, llY, urX, urY, BC;

	/**
	 * @param sensors
	 * @param nav
	 * 
	 *            <p>
	 *            prevents the robot from colliding with the wall when initially
	 *            placed in the field and prior to localization
	 */

	public static void main(String[] args) {

		// exit thread to abort at anytime

		Exit primeExit = new Exit();
		primeExit.start();

		// get the second brick
		Brick masterBrick = LocalEV3.get();
		RemoteRequestEV3 slaveBrick = null;
		TextLCD masterLCD = masterBrick.getTextLCD();
		masterLCD.drawString("Connecting...", 0, 0);
		try {
			slaveBrick = new RemoteRequestEV3("10.0.1.2");
			masterLCD.drawString("Slave connected", 0, 0);
			primeExit.addSlaveBrick(slaveBrick);

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

		// connect to wifi

		// connectToWifi();
		// printParameters();

		// motors object
		Motors motors = new Motors(masterBrick, slaveBrick);
		primeExit.addMotors(motors);

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

		// calibration spins
		// nav.turnTo(90);
		// nav.turnTo(90);
		// nav.turnTo(90);
		// nav.turnTo(90);
		// Button.waitForAnyPress();
		//
		// nav.turnTo(-90);
		// nav.turnTo(-90);
		// nav.turnTo(-90);
		// nav.turnTo(-90);
		//
		// Button.waitForAnyPress();

		// move the front motor out of the way, clear of the wall, and set up
		// for US localization
		motors.getUSMotor().rotate(90);

		// create USLocalization obj and use the method in it

		Sound.setVolume(85);
		Sound.beepSequence();

		/*
		 * 
		LocalEV3.get().getLED().setPattern(3);
		new USLocalization(sensors, odometer, motors.getLeftMotor(), motors.getRightMotor(), nav).doLocalization();
		LocalEV3.get().getLED().setPattern(2);
		// localize with light
		new LightLocalizer(odometer, sensors, nav).doLocalization();
		Sound.beepSequenceUp();
		Sound.setVolume(0);
		nav.travelTo(0, 0, false, false);
		nav.turnToAbs(0);
		Sound.beepSequence();
		Sound.beepSequence();
		*/
		
//		nav.travelTo(5*PhysicalConstants.TILE_SPACING, 0, true, false);
//		nav.travelTo(5*PhysicalConstants.TILE_SPACING+.75*PhysicalConstants.TILE_SPACING, .75*PhysicalConstants.TILE_SPACING, false, false);
//		nav.relocalize();
//		nav.travelTo(6*PhysicalConstants.TILE_SPACING, 1*PhysicalConstants.TILE_SPACING, true, false);
//		nav.turnToAbs(0);
		BallPickupController bp = new BallPickupController(BC, odometer, nav, launcher, sensors, motors, 0, 0, 0, 0);
		bp.navigateToPlatform();
		Button.waitForAnyPress();
	}

	private static void connectToWifi() {

		TextLCD LCD = LocalEV3.get().getTextLCD();
		LCD.clear();
		LCD.drawString("Connecting Wifi...", 0, 0);
		WifiConnection wifi = null;
		try {
			wifi = new WifiConnection(serverAddress, teamNumber);
		} catch (IOException e) {
		}
		DTN = wifi.StartData.get("DTN");
		DSC = wifi.StartData.get("DSC");
		OTN = wifi.StartData.get("OTN");
		OSC = wifi.StartData.get("OSC");
		w1 = wifi.StartData.get("w1");
		d1 = wifi.StartData.get("d1");
		d2 = wifi.StartData.get("d2");
		llX = wifi.StartData.get("ll-x");
		llY = wifi.StartData.get("ll-y");
		urX = wifi.StartData.get("ur-x");
		urY = wifi.StartData.get("ur-y");
		BC = wifi.StartData.get("BC");

		LCD.drawString("Wifi Connected", 0, 0);

	}

	private static void printParameters() {

		// prints parameters received from wifi
		TextLCD LCD = LocalEV3.get().getTextLCD();
		LCD.clear();
		LCD.drawString("DSC: " + DSC + "   OSC: " + OSC, 0, 0);
		LCD.drawString("w1: " + w1 + "   BC: " + BC, 0, 1);
		LCD.drawString("d1: " + d1 + "   d2: " + d2, 0, 2);
		LCD.drawString("ll-x: " + llX + "  ll-y: " + llY, 0, 3);
		LCD.drawString("ur-x: " + urX + "  ur-y: " + urY, 0, 4);
		LCD.drawString("Role: " + role, 0, 5);
	}

	/**
	 * 
	 * @param SC
	 * @param odometer
	 * 
	 *            <p>
	 *            Moves robot to starting corner
	 */
	private static void applyStartingCorner(int SC, Odometer odometer) {
		switch (SC) {
		case 1:
			// do nothing
			break;
		case 2:
			odometer.setX(odometer.getX() + topCornerX * PhysicalConstants.TILE_SPACING);
			odometer.setTheta(odometer.getTheta() - 90);
			break;
		case 3:
			odometer.setX(odometer.getX() + topCornerX * PhysicalConstants.TILE_SPACING);
			odometer.setY(odometer.getY() + topCornerY * PhysicalConstants.TILE_SPACING);
			odometer.setTheta(odometer.getTheta() + 180);
			break;
		case 4:
			odometer.setY(odometer.getY() + topCornerY * PhysicalConstants.TILE_SPACING);
			odometer.setTheta(odometer.getTheta() + 90);
			break;
		default:
			break;
		}

	}

}

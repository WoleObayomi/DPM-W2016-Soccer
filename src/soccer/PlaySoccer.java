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
 */

package soccer;

import java.util.concurrent.Callable;

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

/**
 * 
 * @author Peter Quinn
 *
 */
public class PlaySoccer {

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

		Sound.setVolume(85);
		Sound.beepSequence();

		new USLocalization(sensors, odometer, motors.getLeftMotor(), motors.getRightMotor(), nav).doLocalization();

		// localize with light
		new LightLocalizer(odometer, sensors, nav).doLocalization();
		Sound.beepSequence();
		Sound.setVolume(0);

		nav.travelTo(0, 0, false);
		nav.turnToAbs(0);

		// start odometry correction
		OdometryCorrection odoCorrection = new OdometryCorrection(odometer, sensors);
		odoCorrection.start();

		nav.travelTo(llX * PhysicalConstants.TILE_SPACING - 1, llY * PhysicalConstants.TILE_SPACING, true);

		nav.turnToAbs(80);
		launcher.setToIntakeSpeed();
		nav.travel(10);
		launcher.conveyerBackOneBall();
		launcher.stopLauncher();

		nav.face(0, SC * PhysicalConstants.TILE_SPACING);

		launcher.setToFiringSpeed();
		launcher.conveyerForwardOneBall();
		launcher.conveyerForwardOneBall();
		launcher.raiseAngle();
		launcher.lowerAngle();
		launcher.stopLauncher();
		// determine which planner to use from eventual wifi connection
		// and create the appropriate one below

	}

	// not used
	private void moveTowardTileIntersection(Sensors sensors, Navigation nav) {
		double distFromSideUS = sensors.getSideDist();
		double distFromFrontUS = sensors.getFrontDist();

		/*
		 * assuming robot starts in orientation such that the front US sensor
		 * sees nothing and the side US sensor is close to the wall on the right
		 */
		if (distFromSideUS < 15) {
			nav.turnTo(-90); // turn to face the left (assuming counterclockwise
								// measurement for angles)
			nav.travel(4); // keep moving to the left until desired distance
							// from wall
			nav.turnTo(0); // return to original orientation
		}

		/*
		 * if front facing ultrasonic sensor is too close to a wall then rotate
		 * the robot by 180 degrees so it no longer faces a wall
		 */
		if (distFromFrontUS < 15) {
			nav.turnTo(180);
		}
	}

	// not used

	private static void moveAwayFromCorner(Sensors sensors, Navigation nav, Motors motors) {
		int largeDist = 100;
		int travelDist = 13;
		int safeDist = 3;
		EV3LargeRegulatedMotor left = motors.getLeftMotor();
		EV3LargeRegulatedMotor right = motors.getRightMotor();
		left.setAcceleration(2000);
		right.setAcceleration(2000);
		left.setSpeed(200);
		right.setSpeed(200);
		// check if forward or right
		if (sensors.getFrontDist() > largeDist) {

			// forward
			if (sensors.getSideDist() < largeDist) {
				nav.turnTo(45);
				nav.travel(travelDist);
			}
			// right
			else {
				nav.turnTo(-45);
				nav.travel(travelDist);
			}
		} else {

			// close to a wall, move back
			while (sensors.getFrontDist() < safeDist) {

				left.backward();
				right.backward();

			}
			left.stop(true);
			left.stop(false);

			// wall on left, facing left
			if (sensors.getSideDist() < largeDist) {
				nav.turnTo(125);
				nav.travel(travelDist);
			}

			// no wall on left, facing down
			else {
				nav.turnTo(-90 - 30);
				nav.travel(travelDist);
			}

		}
	}

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

	// not going to use

	private void getClear(Sensors sensors, Navigation nav, Motors motors, Odometer odometer) {

		int minDist = 4;
		int largeDist = 100;
		int travelDist = 8;
		EV3LargeRegulatedMotor leftMotor = motors.getLeftMotor();
		EV3LargeRegulatedMotor rightMotor = motors.getRightMotor();
		leftMotor.setAcceleration(2000);
		rightMotor.setAcceleration(2000);
		leftMotor.setSpeed(200);
		rightMotor.setSpeed(200);
		/*
		 * Straight | | | | | |__________________Right
		 * 
		 */

		// see if we are facing straight or right
		if (sensors.getFrontDist() > largeDist) {
			// check if straight
			if (sensors.getSideDist() > largeDist) {
				// rotate right to ~0 degrees
				while (sensors.getFrontDist() < 240) {
					leftMotor.forward();
					rightMotor.backward();
				}
				leftMotor.stop(true);
				rightMotor.stop(false);
				odometer.setTheta(0);
				nav.travelTo(travelDist, travelDist, false);

			}
			// facing right
			else {

				while (sensors.getSideDist() > minDist) {
					leftMotor.forward();
					rightMotor.backward();
				}
				leftMotor.stop(true);
				rightMotor.stop(false);
				odometer.setTheta(90);

				nav.travelTo(travelDist, travelDist, false);

			}
		}
		// we are facing down or left
		else {

			// facing down
			if (sensors.getSideDist() < largeDist) {
				odometer.setTheta(180);
				nav.turnTo(-135);
				nav.travel(travelDist);

			}
			// facing left
			else {
				odometer.setTheta(270);
				nav.travel(-travelDist);
			}

		}
	}

}

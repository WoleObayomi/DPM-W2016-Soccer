/*
 * Title: PlannerDefense
 * Author: Peter Quinn
 * Date: March 10, 2016
 * Desc: The procedure that the robot follow when it is a defender, after it has localized
 * 
 * Edit hist:
 * 
 * April 8, 2016 - Peter: Added  procedure (was orginally empty skeleton class)
 * 
 */

package soccer;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * 
 * @author Peter Quinn
 *
 */
public class PlannerDefense {
	private Navigation nav;
	private int defenseLine;
	private double goalSize;
	private Odometer odometer;
	private Motors motors;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	// constants
	private final int yTileMax = 11;
	private final int xTileMax = 11;
	private final int SLEEP = 30;// ms
	private final int SPEED = 180;

	public PlannerDefense(Navigation nav, int defenseLine, double goalSize, Odometer odometer, Motors motors) {
		this.nav = nav;
		this.defenseLine = defenseLine;
		this.goalSize = goalSize;
		this.odometer = odometer;
		this.motors = motors;
		this.leftMotor = motors.getLeftMotor();
		this.rightMotor = motors.getRightMotor();
	}

	public void run() {

		// get beside the zone while avoiding obstacles. Stays out of neutral
		// area at start

		// start below zone, move up and past the line
		double defenseLineY = (yTileMax - defenseLine) * PhysicalConstants.TILE_SPACING;

		if (odometer.getY() < defenseLineY) {
			nav.movePastY(defenseLineY, true, false);
		} else {// start above zone, move down just above line
			nav.movePastY(defenseLineY + PhysicalConstants.TILE_SPACING, true, false);
		}

		// travel to the middle in front of the net
		double centerX = xTileMax / 2 * PhysicalConstants.TILE_SPACING;
		nav.travelTo(centerX, defenseLineY + PhysicalConstants.TILE_SPACING / 2, true, false);
		nav.turnToAbs(90);

		double goalWidth = goalSize * PhysicalConstants.TILE_SPACING;
		
		

		// move back and forth in front of goal
		boolean left = true;
		leftMotor.setSpeed(SPEED);
		rightMotor.setSpeed(SPEED);
		while (true) {

			// move forward

			if (left) {// going left
				leftMotor.forward();
				rightMotor.forward();

				if (odometer.getX() > centerX + goalWidth / 2) {// far left, so
																// switch to
																// right
					left = false;
				}
			} else {// going right
				leftMotor.backward();
				rightMotor.backward();
				if (odometer.getX() < centerX - goalWidth / 2) {// far right, so
																// switch to
																// left
					left = true;
				}
			}

			// sleep before going again
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}

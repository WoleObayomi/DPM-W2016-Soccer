/*
 * Title: Navigation
 * 
 * Author: Peter Quinn (260689207) Rony Azrak (260606812)
 *   
 *Date created: 8/2/2016
 * 
 * Description: Contains methods that allow the robot to travel to a point given an x and y coordinate.
 * If it sees and obstacle, it will follow it until it is back on track. Contains other methods for
 * turning the robot, making it face a point, and travelling in straight line
 * 
 * Edit Log:
 * 15/2/2016 - Peter: wall follow / obstacle avoidance procedures disabled for lab 4
 * March 13 - Peter: modified for use with final project
 * March 24 - Peter: added isStraight() to be able to know if the robot is travelling in a 
 * straight line
 * March 26 - Peter: added face(double x, double y) which will turn the robot to face the 
 * point x, y; updated class description
*/
package soccer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * 
 * @author Peter Quinn
 *
 */
public class Navigation {

	// to be passed in through/generated the constructor
	private EV3LargeRegulatedMotor rightMotor;
	private EV3LargeRegulatedMotor leftMotor;
	private WallFollowController wallFollowController;
	private Odometer odometer;
	private Sensors sensors;
	private double leftRadius;
	private double rightRadius;
	private double trackWidth;

	// navigation constants
	private final double distError = 1.5;
	private final double thetaTolerance = 2;
	private final int NAV_SLEEP = 50;
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 240;
	private final int ACCELERATION = 2000;
	private final int WALL_DETECTED_RANGE = 8; // cm
	private final int WALL_FOLLOW_EXIT_ANGLE = 10;
	// additional variables
	private boolean isNavigating = false;
	private boolean isTurning = false;

	// constructor, needs an odometer to read from, needs motors to access
	/**
	 * 
	 * @param odometer
	 * @param motors
	 * @param sensors
	 * @param leftWheelRadius
	 * @param rightWheelRadius
	 * @param trackWidth
	 */
	public Navigation(Odometer odometer, Motors motors, Sensors sensors, double leftWheelRadius,
			double rightWheelRadius, double trackWidth) {
		this.odometer = odometer;
		this.leftMotor = motors.getLeftMotor();
		this.rightMotor = motors.getRightMotor();
		this.leftRadius = leftWheelRadius;
		this.rightRadius = rightWheelRadius;
		this.trackWidth = trackWidth;
		this.sensors = sensors;
		this.wallFollowController = new WallFollowController(leftMotor, rightMotor);

		leftMotor.setAcceleration(ACCELERATION);
		rightMotor.setAcceleration(ACCELERATION);

	}

	// takes x and y of its destination and turns and drives to it, allows
	// wallfollowing to be on or off
	/**
	 * 
	 * @param x
	 *            point to travel to on the x axis
	 * @param y
	 *            point to travel to on the y axis
	 * @param wallFollowOn
	 *            boolean value to indicate whether wall following is to be used
	 */
	public void travelTo(double x, double y, boolean wallFollowOn) {

		isNavigating = true;

		// get within a circle of radius distError to point we want
		while (Math.sqrt(Math.pow(x - odometer.getX(), 2) + Math.pow(y - odometer.getY(), 2)) > distError) {

			// check for a wall in front if wallfollowing is on
			if (wallFollowOn) {
				if (sensors.getFrontDist() < WALL_DETECTED_RANGE) {

					followWall(x, y, odometer);
				}
			}

			// navigating to point
			double xNow = odometer.getX();
			double yNow = odometer.getY();

			// this function moves the angle to regular cartesian orientation
			double thetaNow = 360 - odometer.getTheta() + 90;
			double theta = Math.toDegrees(Math.atan2(y - yNow, x - xNow));

			// process the angles so the robot doesn't get confused
			if (theta < 0)
				theta += 360;

			if (thetaNow >= 360)
				thetaNow -= 360;

			// find min turn

			if (Math.abs(thetaNow - theta) > thetaTolerance && Math.abs(y - yNow) > .1) {
				// find and turn the min angle
				if (thetaNow - theta < -180) {
					turnTo(thetaNow - theta + 360);
				} else if (thetaNow - theta > 180) {
					turnTo(thetaNow - theta - 360);
				} else {
					turnTo(thetaNow - theta);
				}

			}

			// after turning we go forward

			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.forward();
			leftMotor.forward();

			// wait a bit before starting again
			try {
				Thread.sleep(NAV_SLEEP);
			} catch (Exception e) {
				// don't expect any interruptions
			}

		}

		// here we are within distError of our destination point, so we stop
		// moving

		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);

		leftMotor.stop(true);
		rightMotor.stop(false);
		isNavigating = false;
	}

	// turns a relative angle
	/**
	 * 
	 * @param theta
	 *            Angle to turn to
	 */
	public void turnTo(double theta) {

		isTurning = true;

		leftMotor.setAcceleration(1000);
		rightMotor.setAcceleration(1000);
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(convertAngle(leftRadius, trackWidth, theta), true);
		rightMotor.rotate(-convertAngle(rightRadius, trackWidth, theta), false);

		isTurning = false;
	}

	// turns to an absolute angle
	/**
	 * 
	 * @param theta
	 *            Absolute angle to turn to
	 */
	public void turnToAbs(double theta) {

		double deltaTheta = theta - odometer.getTheta();

		if (deltaTheta < -180) {
			turnTo(deltaTheta + 360);
		} else {
			turnTo(deltaTheta);
		}

	}

	// has the robot travel a distance it is passed, forward or backwards
	// depending on sign
	/**
	 * 
	 * @param distance
	 *            Distance robot should travel. Forward - positive and Backward
	 *            - negative
	 * 
	 */
	public void travel(double distance) {

		double x = odometer.getX();
		double y = odometer.getY();

		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.setSpeed(FORWARD_SPEED);

		if (distance == 0) {
			return;
		} else if (distance > 0) {

			leftMotor.forward();
			rightMotor.forward();

			while (Math.sqrt(Math.pow(x - odometer.getX(), 2) + Math.pow(y - odometer.getY(), 2)) < Math
					.abs(distance)) {
				try {
					Thread.sleep(NAV_SLEEP);
				} catch (InterruptedException e) {
				}
			}
			leftMotor.stop(true);
			rightMotor.stop(false);
			return;
		} else {
			leftMotor.backward();
			rightMotor.backward();
			while (Math.sqrt(Math.pow(x - odometer.getX(), 2) + Math.pow(y - odometer.getY(), 2)) < Math
					.abs(distance)) {
				try {
					Thread.sleep(NAV_SLEEP);
				} catch (InterruptedException e) {
				}
			}
			leftMotor.stop(true);
			rightMotor.stop(false);
			return;
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * 
	 *            Turns robot to face x, y
	 */

	public void face(double x, double y) {
		// makes the robot face a point x,y
		double xNow = odometer.getX();
		double yNow = odometer.getY();

		// this function moves the angle to regular cartesian orientation
		double thetaNow = 360 - odometer.getTheta() + 90;
		double theta = Math.toDegrees(Math.atan2(y - yNow, x - xNow));

		// process the angles so the robot doesn't get confused
		if (theta < 0)
			theta += 360;

		if (thetaNow >= 360)
			thetaNow -= 360;

		// find min turn

		if (Math.abs(thetaNow - theta) > thetaTolerance) {
			// find and turn the min angle
			if (thetaNow - theta < -180) {
				turnTo(thetaNow - theta + 360);
			} else if (thetaNow - theta > 180) {
				turnTo(thetaNow - theta - 360);
			} else {
				turnTo(thetaNow - theta);
			}

		}
	}

	/**
	 * 
	 * @return boolean Indicates if robot is currently navigating
	 */
	public boolean isNavigating() {
		// returns true if it is navigating or if it is turning
		return isNavigating || isTurning;

	}

	/**
	 * 
	 */

	public boolean isStraight() {
		return leftMotor.getSpeed() == FORWARD_SPEED && rightMotor.getSpeed() == FORWARD_SPEED;
	}

	// Private helper methods

	/**
	 * 
	 * @param x
	 * @param y
	 */
	private void followWall(double x, double y, Odometer odometer) {

		// turn to the left so our wall following sensor on the right is facing
		// the
		// wall we saw with the front sensor

		leftMotor.stop(true);
		rightMotor.stop(false);
		turnTo(90);
		float distToWall;
		while (true) {

			distToWall = sensors.getSideDist();
			wallFollowController.processData(distToWall);

			// check if we see another wall in front
			// if (sensors.getFrontDist() < WALL_DETECTED_RANGE) { // if we do,
			// // call
			// // this method
			// // again
			// // to
			// // follow the new wall, then return when we are done with that
			// // one
			// followWall(x, y);
			// return;
			// }

			// check to see if we have gotten around the wall
			double xNow = odometer.getX();
			double yNow = odometer.getY();

			// this function moves the angle to regular cartesian orientation
			double thetaNow = 360 - odometer.getTheta() + 90;
			double theta = Math.toDegrees(Math.atan2(y - yNow, x - xNow));

			// process the angles
			if (theta < 0) {
				theta += 360;
			}
			if (thetaNow > 360) {
				thetaNow -= 360;
			}

			// check our heading is ~90 degrees to the correct
			// direction, this will be true when we have gone around the wall
			if (Math.abs(theta - (thetaNow + 90)) < WALL_FOLLOW_EXIT_ANGLE) {
				// send the stop code to the motor controller
				wallFollowController.processData(-1);
				return;
			}
			// wait a bit before starting again
			try {
				Thread.sleep(NAV_SLEEP);
			} catch (Exception e) {
				// don't expect any interruptions
			}

		}
	}

	// from the square driver class from lab 2

	// takes the radius of the wheel and the distance you want to travel and
	// converts to number of degree the motor must rotate

	/**
	 * 
	 * @param radius
	 * @param distance
	 * @return int number of wheel rotations
	 */
	private int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// takes an angle to rotate the robot and calculates the into the number of
	// degrees that each motor must rotate (in opposite directions) to achieve
	// the rotation
	/**
	 * 
	 * @param radius
	 * @param width
	 * @param angle
	 * @return int number of degrees the wheels must rotate in opposite
	 *         directions
	 */
	private int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}

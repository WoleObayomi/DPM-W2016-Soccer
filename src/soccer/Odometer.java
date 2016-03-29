/*
 * Odometer.java
 * 
 * Author: Peter Quinn
 * 
 * Date:8/2/2016
 * 
 * Description: Uses the information provided by the motors on the robot to 
 * calculate the position of the robot
 * 
 * 
 * Edit Log:
 * 
 * March 13 - Peter: modified for use with final project. Made the constructor take in more
 * of the values needed for the class to function
 * 
 * March 17 - Peter: fixed typo made during last modifications 
 * 
 */

package soccer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * 
 * @author Peter Quinn
 *
 */
public class Odometer extends Thread {
	// robot position
	private double x, y, theta;

	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 30; //ms

	// lock object for mutual exclusion
	private Object lock;

	// motors we need to get the tacho count from
	private EV3LargeRegulatedMotor rightMotor;
	private EV3LargeRegulatedMotor leftMotor;

	// constants we need for calculations
	// could modify here to have 2 different wheel sizes
	private double LeftWheelRadius,RightWheelRadius, track;

	// constructor takes the motors object to access the left and right motors
	/**
	 * 
	 * @param motors
	 * @param wheelRadius
	 * @param track
	 */
	public Odometer(Motors motors, double LeftWheelRadius, double RightWheelRadius, double track) {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
		rightMotor = motors.getRightMotor();
		leftMotor = motors.getLeftMotor();
		this.LeftWheelRadius = LeftWheelRadius;
		this.RightWheelRadius  = RightWheelRadius;
		this.track = track;
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		// variables needed for calculations
		double leftThetaLast, rightThetaLast, leftThetaNow, rightThetaNow, deltaTheta, deltaC;
		leftThetaLast = 0;
		rightThetaLast = 0;
		rightMotor.resetTachoCount();
		leftMotor.resetTachoCount();

		while (true) {
			updateStart = System.currentTimeMillis();

			// thetas must be in rads
			rightThetaNow = rightMotor.getTachoCount() * Math.PI / 180;
			leftThetaNow = leftMotor.getTachoCount() * Math.PI / 180;

			// calculations from slides

			// delta theta in rads
			deltaTheta = ((rightThetaNow - rightThetaLast) * RightWheelRadius - (leftThetaNow - leftThetaLast) * LeftWheelRadius)
					/ track;

			deltaC = ((rightThetaNow - rightThetaLast) * RightWheelRadius + (leftThetaNow - leftThetaLast) * LeftWheelRadius) / 2;

			rightThetaLast = rightThetaNow;
			leftThetaLast = leftThetaNow;

			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!

				// sin and cos in java take arguments in rads
				x = x + deltaC * Math.sin(theta * Math.PI / 180 + deltaTheta / 2);
				y = y + deltaC * Math.cos(theta * Math.PI / 180 + deltaTheta / 2);
				theta = theta - deltaTheta * 180 / Math.PI; // convert to
															// degrees for
															// display
				if (theta > 360) {
					theta -= 360;
				}
				if (theta < 0) {
					theta += 360;
				}
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	/**
	 * 
	 * @param position
	 *            array of Doubles containing position variables
	 * @param update
	 *            boolean array containing values to indicate whether a position
	 *            variable should be updated
	 */
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	/**
	 * 
	 * @return x positional variable
	 */
	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	/**
	 * 
	 * @return y positional variable
	 */
	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	/**
	 * 
	 * @return theta positional variable
	 */
	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	/**
	 * 
	 * @param position
	 *            array of Doubles containing position variables
	 * @param update
	 *            boolean array containing values to indicate whether a position
	 *            variable should be updated
	 */
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	/**
	 * 
	 * @param x
	 *            set positional variable
	 */
	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	/**
	 * 
	 * @param y
	 *            set positional variable
	 */
	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	/**
	 * 
	 * @param theta
	 *            set positional variable
	 */
	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}
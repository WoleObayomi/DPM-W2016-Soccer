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

import com.sun.accessibility.internal.resources.accessibility;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;

/**
 * 
 * @author Peter Quinn
 *
 */
public class Navigation {

	// to be passed in through/generated the constructor
	private EV3LargeRegulatedMotor rightMotor;
	private EV3LargeRegulatedMotor leftMotor;
	private RegulatedMotor USMotor;
	private WallFollowController wallFollowController;
	private Odometer odometer;
	private Sensors sensors;
	private double leftRadius;
	private double rightRadius;
	private double trackWidth;

	// navigation constants
	private final double distError = 1.5;
	private final double thetaTolerance = 2;
	private final int NAV_SLEEP = 25;// ms
	private final int RELOCALAIZE_DELAY = 10000;// ms, max time between
												// relocalizations
	private final int RELOCALIZE_COUNTER_MAX = RELOCALAIZE_DELAY / NAV_SLEEP;
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 220;

	private final int ACCELERATION = 250;
	private final int DELAY = 75; // ms, delay to wait for other threads to

	private final int ROTATE_LEFT_OFFSET = 5;
	

	// wall following
	private final int WALL_DETECTED_RANGE = 17; // cm
	private final int WALL_FOLLOW_EXIT_ANGLE = 10;
	private final int WALL_TRAVEL_PAST_MARGIN_LEFT = 15;
	private final int WALL_TRAVEL_PAST_MARGIN_RIGHT = 20;
	private final int WALL_DECTECTION_DELAY = 3000;// ms
	private final int WALL_DECTECTION_COUNTER = WALL_DECTECTION_DELAY / NAV_SLEEP;
	private final int TOO_CLOSE = 5;
	private final int FOLLOW_DIST = 25;

	// additional variables
	private boolean isNavigating = false;
	private boolean isTurning = false;
	SweepMotor USMotorSweep = null;

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
		this.USMotor = motors.getUSMotor();
		this.leftRadius = leftWheelRadius;
		this.rightRadius = rightWheelRadius;
		this.trackWidth = trackWidth;
		this.sensors = sensors;
		this.wallFollowController = new WallFollowController(leftMotor, rightMotor);

		leftMotor.setAcceleration(ACCELERATION);
		rightMotor.setAcceleration(ACCELERATION);
		USMotor.setAcceleration(ACCELERATION);

	}

	// takes x and y of its destination and turns and drives to it, allows
	// wallfollowing to be on or off, relocalization can be turned on or off
	/**
	 * 
	 * @param x
	 *            point to travel to on the x axis
	 * @param y
	 *            point to travel to on the y axis
	 * @param wallFollowOn
	 *            boolean value to indicate whether wall following is to be used
	 * @param relocalizeOn
	 *            boolean value to indicate whether the robot should relocalize
	 *            periodically and after wall following to correct angle
	 */
	public void travelTo(double x, double y, boolean wallFollowOn, boolean relocalizeOn) {
		int relocalizerCounter = 0;
		isNavigating = true;
		USMotorSweep = null;
		if (wallFollowOn) {
			USMotorSweep = new SweepMotor(USMotor);
			USMotorSweep.start();
			// turn on motor sweeping
			USMotorSweep.on();
		}
		// get within a circle of radius distError to point we want
		while (Math.sqrt(Math.pow(x - odometer.getX(), 2) + Math.pow(y - odometer.getY(), 2)) > distError) {

			// check if we need to relocalize
			if (relocalizerCounter == RELOCALIZE_COUNTER_MAX && relocalizeOn) {
				leftMotor.stop(true);
				rightMotor.stop(false);
				relocalize();
				relocalizerCounter = 0;
			} else if (relocalizeOn) {
				relocalizerCounter++;
			}
			// check for a wall in front if wallfollowing is on
			if (wallFollowOn) {
				if (sensors.getFrontDist() < WALL_DETECTED_RANGE) {// see a wall

					// stop
					leftMotor.stop(true);
					rightMotor.stop(false);
					// stop sweeping, suspend thread to stop errors
					if (USMotorSweep != null) {
						USMotorSweep.off();
						try {
							Thread.sleep(DELAY);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						USMotorSweep.suspend();
					}

					// analyze the wall and our position and avoid it on the
					// side with the most room
					analyzeWall();
					try {
						Thread.sleep(DELAY);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (USMotorSweep != null) {// resume sweeping
						USMotorSweep.on();
						USMotorSweep.resume();
					}
					if (relocalizeOn) {
						relocalize();
						relocalizerCounter = 0;
					}
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

			if (Math.abs(thetaNow - theta) > thetaTolerance && Math.abs(y - yNow) > .5) {
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

		if (USMotorSweep != null) {
			USMotorSweep.off();
			USMotorSweep.kill();
		}

		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {

		}
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
		leftMotor.stop(true);
		rightMotor.stop(false);

		leftMotor.setAcceleration(ACCELERATION);
		rightMotor.setAcceleration(ACCELERATION);
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
	 * @param wallFollowOn
	 * @param relocalize
	 * <p>
	 * avoid a certain x coordinate while navigating
	 */
	public void movePastX(double x, boolean wallFollowOn, boolean relocalize) {

		int relocalizerCounter = 0;
		USMotorSweep = null;

		// to the right of the line
		if (odometer.getX() > x) {

			USMotorSweep = null;
			if (wallFollowOn) {
				USMotorSweep = new SweepMotor(USMotor);
				USMotorSweep.start();
				// turn on motor sweeping
				USMotorSweep.on();
			}

			// go straight left until past line
			while (odometer.getX() > x) {
				// check if we need to relocalize
				if (relocalize) {
					if (relocalizerCounter == RELOCALIZE_COUNTER_MAX) {
						relocalize();
						relocalizerCounter = 0;
					}
					relocalizerCounter++;
				}
				// check for a wall in front if wallfollowing is on
				if (wallFollowOn) {
					if (sensors.getFrontDist() < WALL_DETECTED_RANGE) {
						if (USMotorSweep != null) {
							USMotorSweep.off();
							try {
								Thread.sleep(DELAY);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							USMotorSweep.suspend();
						}
						analyzeWall();// analyze and avoid wall

						if (USMotorSweep != null) {// resume sweeping
							USMotorSweep.on();
							try {
								Thread.sleep(DELAY);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							USMotorSweep.resume();
						}

						if (relocalize) {
							relocalize();
							relocalizerCounter = 0;
						}
					}
				}

				if (Math.abs(odometer.getTheta() - 270) > thetaTolerance) {
					turnToAbs(270);
				}
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.forward();
				leftMotor.forward();
				try {
					Thread.sleep(NAV_SLEEP);
				} catch (InterruptedException e) {
				}

			}
			leftMotor.stop(true);
			rightMotor.stop(false);
			return;
		} else {
			// go right
			while (odometer.getX() < x) {
				// check if we need to relocalize
				if (relocalize) {
					if (relocalizerCounter == RELOCALIZE_COUNTER_MAX) {
						relocalize();
						relocalizerCounter = 0;
					}
					relocalizerCounter++;
				}

				// check for a wall in front if wallfollowing is on
				if (wallFollowOn) {
					if (sensors.getFrontDist() < WALL_DETECTED_RANGE) {
						if (USMotorSweep != null) {
							USMotorSweep.off();
							try {
								Thread.sleep(DELAY);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							USMotorSweep.suspend();
						}
						analyzeWall();// analyze and avoid wall

						if (USMotorSweep != null) {// resume sweeping
							USMotorSweep.on();
							try {
								Thread.sleep(DELAY);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							USMotorSweep.resume();
						}
						if (relocalize) {
							relocalize();
							relocalizerCounter = 0;
						}
					}
				}
				if (Math.abs(odometer.getTheta() - 90) > thetaTolerance) {
					turnToAbs(90);
				}
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.forward();
				leftMotor.forward();
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
	 * @param y
	 * @param wallFollowOn
	 * @param relocalizeOn
	 * <p>
	 * avoid a certain y coordinate
	 */
	public void movePastY(double y, boolean wallFollowOn, boolean relocalizeOn) {

		int relocalizerCounter = 0;
		USMotorSweep = null;
		if(wallFollowOn){
			USMotorSweep = new SweepMotor(USMotor);
			USMotorSweep.start();
			USMotorSweep.on();
		}

		if (odometer.getY() > y) { // above y linee
			// go straight down until past line
			while (odometer.getY() > y) {
				// check if we need to relocalize
				if (relocalizeOn) {
					if (relocalizerCounter == RELOCALIZE_COUNTER_MAX) {
						relocalize();
						relocalizerCounter = 0;
					}
					relocalizerCounter++;
				}
				// check for a wall in front if wallfollowing is on
				if (wallFollowOn) {
					if (sensors.getFrontDist() < WALL_DETECTED_RANGE) {
						if (USMotorSweep != null) {
							USMotorSweep.off();
							try {
								Thread.sleep(DELAY);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							USMotorSweep.suspend();
						}
						analyzeWall();// analyze and avoid wall

						if (USMotorSweep != null) {// resume sweeping
							USMotorSweep.on();
							try {
								Thread.sleep(DELAY);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							USMotorSweep.resume();
						}
						if (relocalizeOn) {
							relocalize();
							relocalizerCounter = 0;
						}
					}
				}
				if (Math.abs(odometer.getTheta() - 180) > thetaTolerance) {
					turnToAbs(180);
				}
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.forward();
				leftMotor.forward();
				try {
					Thread.sleep(NAV_SLEEP);
				} catch (InterruptedException e) {
				}

			}
			leftMotor.stop(true);
			rightMotor.stop(false);
			return;
		} else {
			// go up
			while (odometer.getY() < y) {
				// check if we need to relocalize
				if (relocalizeOn) {
					if (relocalizerCounter == RELOCALIZE_COUNTER_MAX) {
						relocalize();
						relocalizerCounter = 0;
					}
					relocalizerCounter++;
				}

				// check for a wall in front if wallfollowing is on
				if (wallFollowOn) {
					if (sensors.getFrontDist() < WALL_DETECTED_RANGE) {
						if (USMotorSweep != null) {
							USMotorSweep.off();
							try {
								Thread.sleep(DELAY);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							USMotorSweep.suspend();
						}
						analyzeWall();// analyze and avoid wall

						if (USMotorSweep != null) {// resume sweeping
							USMotorSweep.on();
							try {
								Thread.sleep(DELAY);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							USMotorSweep.resume();
						}
						if (relocalizeOn) {
							relocalize();
							relocalizerCounter = 0;
						}
					}
				}
				if (!(odometer.getTheta()<thetaTolerance || odometer.getTheta()>thetaTolerance)) {
					turnToAbs(0);
				}
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.forward();
				leftMotor.forward();
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
	 * <p>
	 * stop the robot
	 */
	public void stop() {
		leftMotor.stop(true);
		rightMotor.stop(false);
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
	 * <p>
	 * true if both motors are moving at the same speed
	 */

	public boolean isStraight() {
		return leftMotor.getSpeed() == FORWARD_SPEED && rightMotor.getSpeed() == FORWARD_SPEED;
	}

	// Private helper methods
	/**
	 * <p>
	 * decides whether to follow wall on the left or right
	 */
	private void analyzeWall() {

		// stop
		leftMotor.stop();
		rightMotor.stop();

		// look right

		USMotor.rotateTo(90);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		float leftDist = sensors.getSideDist();
		float rightDist = sensors.getFrontDist();

		if (leftDist > rightDist) {
			// more to go left, so we will put the wall on our right and go left
			followWallOnRight();
		} else {
			// more room on right, so we will put the wall on our left and go
			// right
			followWallOnLeft();
		}
		USMotor.rotateTo(0);

	}
	
	/**
	 * <p>
	 * will cause robot to follow wall on right
	 */
	private void followWallOnRight() {

		leftMotor.stop(true);
		rightMotor.stop(false);
		turnTo(-90);

		USMotor.rotateTo(90);

		float distToWall;
		boolean firstSide = true;

		while (true) {

			distToWall = sensors.getFrontDist();

			// past side of obstacle
			if (distToWall > FOLLOW_DIST) {

				if (firstSide) {
					// move forward past edge
					travel(WALL_TRAVEL_PAST_MARGIN_RIGHT);
					// turn back the way we were going
					turnTo(70);
					// on second side now
					firstSide = false;

					// travel until we see other side of wall
					int counter = 0;
					distToWall = sensors.getFrontDist();
					while (distToWall > 30 && counter < WALL_DECTECTION_COUNTER) {

						distToWall = sensors.getFrontDist();
						counter++;
						leftMotor.forward();
						rightMotor.forward();

						try {
							Thread.sleep(NAV_SLEEP);
						} catch (InterruptedException e) {
						}

					}
					// start the loop again for the other side
					continue;

				} else {
					// past second side, so we make sure we are clear of the
					// wall
					// then stop wall following

					travel(WALL_TRAVEL_PAST_MARGIN_RIGHT);
					USMotor.rotateTo(0);
					return;
				}
			} else { // not past side of obstacle, follow wall

				if (distToWall < TOO_CLOSE) {// too close to wall

					turnTo(5);
					travel(5);

				} else {
					leftMotor.forward();
					rightMotor.forward();
					try {
						Thread.sleep(NAV_SLEEP);
					} catch (InterruptedException e) {
					}
				}
			}

			try {
				Thread.sleep(NAV_SLEEP);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// follows wall on left
	/**
	 * <p>
	 * will cause robot to follow wall on the left
	 */
	private void followWallOnLeft() {
		leftMotor.stop(true);
		rightMotor.stop(false);
		turnTo(90);
		float distToWall;
		boolean firstSide = true;

		while (true) {

			distToWall = sensors.getSideDist();

			// past side of obstacle
			if (distToWall > FOLLOW_DIST) {

				if (firstSide) {
					// move forward past edge
					travel(WALL_TRAVEL_PAST_MARGIN_LEFT);
					// turn back the way we were going
					turnTo(-70);
					// on second side now
					firstSide = false;

					// travel until we see other side of wall
					int counter = 0;
					distToWall = sensors.getSideDist();
					while (distToWall > 30 && counter < WALL_DECTECTION_COUNTER) {

						distToWall = sensors.getSideDist();
						counter++;
						leftMotor.forward();
						rightMotor.forward();

						try {
							Thread.sleep(NAV_SLEEP);
						} catch (InterruptedException e) {
						}

					}
					// start the loop again for the other side
					continue;

				} else {
					// past second side, so we make sure we are clear of the
					// wall
					// then stop wall following

					travel(WALL_TRAVEL_PAST_MARGIN_LEFT);
					return;
				}
			} else { // not past side of obstacle, follow wall

				if (distToWall < TOO_CLOSE) {// too close to wall

					turnTo(5);
					travel(5);

				} else {
					leftMotor.forward();
					rightMotor.forward();
					try {
						Thread.sleep(NAV_SLEEP);
					} catch (InterruptedException e) {
					}
				}
			}

			try {
				Thread.sleep(NAV_SLEEP);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param odometer
	 * 
	 * <p>
	 * causes the robot to follow the wall using the P-Type method
	 */
	private void followWallPType(double x, double y, Odometer odometer) {

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

	public void relocalize() {

		Sound.setVolume(85);
		Sound.beepSequenceUp();
		// find nearest corner
		double GRID_SPACING = PhysicalConstants.TILE_SPACING;

		double HALF_TILE = PhysicalConstants.TILE_SPACING / 2;
		// get current x and y according to odometer (of center of bot)
		double x = odometer.getX();
		double y = odometer.getY();
		// subtract by gridline spacing distance. Stop when we find we are
		// close to a line or we have dropped too far to find a line
		int xCount = 0;
		while (x > HALF_TILE) {
			x -= GRID_SPACING;
			xCount++;
		}

		// subtract by gridline spacing distance. Stop when we find we are
		// close to a line or we have dropped too far to find a line
		int yCount = 0;
		while (y > HALF_TILE) {
			y -= GRID_SPACING;
			yCount++;
		}

		// set xCount and yCount to the true coordinates by multiplying by the
		// tile spacing
		xCount *= PhysicalConstants.TILE_SPACING;
		yCount *= PhysicalConstants.TILE_SPACING;

		// find angle to this corner
		double angleToCorner = angleDifference(xCount, yCount);

		// go to easiest corner
		if (angleToCorner < 90) { // facing towards the nearest corner, go to it
			travelTo(xCount, yCount, false, false);
		} else {
			double theta = odometer.getTheta();
			if (theta > 315 || theta < 135) {// moving up/right, make target
												// point up and right
				travelTo(xCount + PhysicalConstants.TILE_SPACING, yCount + PhysicalConstants.TILE_SPACING, false,
						false);
			} else {
				// facing down/left, so move target point down/left
				travelTo(xCount - PhysicalConstants.TILE_SPACING, yCount - PhysicalConstants.TILE_SPACING, false,
						false);
			}
		}
		// near corner, run light localizer
		new LightLocalizer(odometer, sensors, this).doLocalization();
		Sound.beepSequence();
		Sound.setVolume(0);
		return;

	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return double the angle between the current orientation and the
	 *         orientation needed to face x, y
	 */
	private double angleDifference(double x, double y) {
		// gets the difference between the current angle and the angle neeeded
		// to face a point
		double xNow = odometer.getX();
		double yNow = odometer.getY();

		// this function moves the angle to regular Cartesian orientation
		double thetaNow = 360 - odometer.getTheta() + 90;
		double theta = Math.toDegrees(Math.atan2(y - yNow, x - xNow));

		// process the angles
		if (theta < 0)
			theta += 360;

		if (thetaNow >= 360)
			thetaNow -= 360;

		return Math.abs(theta - thetaNow);
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

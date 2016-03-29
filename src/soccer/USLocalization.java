package soccer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * 
 * @author Peter Quinn
 *
 */
public class USLocalization {

	// constants
	private final int ROTATE_SPEED = 240;
	private final int DISTANCE_TO_WALL = 30;
	private final int NOISE_MARGIN = 2;
	private final int REFRESH_RATE = 50;
	private final int MIN_DIST = 6;
	private final int CORRECTION_DIST = 6;

	// to be passed in by constructor
	private Sensors sensors;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Odometer odo;
	private Navigation nav;

	/**
	 * 
	 * @param sensors
	 * @param odometer
	 * @param leftMotor
	 * @param rightMotor
	 */
	public USLocalization(Sensors sensors, Odometer odometer, EV3LargeRegulatedMotor leftMotor,
			EV3LargeRegulatedMotor rightMotor, Navigation nav) {
		this.sensors = sensors;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odo = odometer;
		this.nav = nav;
	}

	// uses falling edge technique
	/**
	 * 
	 */
	public void doLocalization() {

		double angleA = 0, angleB = 0;

		// rotate the robot until it sees no wall
		float distance = sensors.getFrontDist();

		while (distance < DISTANCE_TO_WALL + NOISE_MARGIN) {

			if (distance < MIN_DIST) {
				nav.travel(-(CORRECTION_DIST - distance));
				distance = sensors.getFrontDist();
			}

			// rotate clockwise
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			leftMotor.forward();
			rightMotor.backward();

			try {
				Thread.sleep(REFRESH_RATE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// get US data to check for no wall
			distance = sensors.getFrontDist();

		}
		// keep rotating until the robot sees a wall, then latch the first
		// angle
		while (distance > DISTANCE_TO_WALL) {

			// rotate clockwise
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			leftMotor.forward();
			rightMotor.backward();

			try {
				Thread.sleep(REFRESH_RATE);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			// get US data to check for a wall
			distance = sensors.getFrontDist();

			// check for noise margin, if we are in it we loop until we are
			// below it
			if (distance < DISTANCE_TO_WALL + NOISE_MARGIN) {
				// save angle we enter the noise margin at
				double marginEnterAngle = odo.getTheta();
				do {
					try {
						Thread.sleep(REFRESH_RATE);
					} catch (Exception e) {

					}
					distance = sensors.getFrontDist();

				} while (distance > DISTANCE_TO_WALL - NOISE_MARGIN);
				// save angle we exit the noise margin at
				double marginExitAngle = odo.getTheta();

				leftMotor.stop(true);
				rightMotor.stop(false);

				// take the average of the angle we entered and exited the
				// noise margin at to save angle we saw the wall
				angleA = (marginEnterAngle + marginExitAngle) / 2;

				// break out of the parent loop
				break;
			}

		}
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.backward();
		rightMotor.forward();

		// give some delay before looking for checking for the wall again
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		distance = sensors.getFrontDist();
		// switch direction and wait until it sees a wall
		while (distance > DISTANCE_TO_WALL) {

			// rotate the opposite direction, counter clockwise
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			leftMotor.backward();
			rightMotor.forward();

			try {
				Thread.sleep(REFRESH_RATE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// get US data to check for a wall
			distance = sensors.getFrontDist();

			// check for noise margin, if we are in it we loop until we are
			// below it
			if (distance < DISTANCE_TO_WALL + NOISE_MARGIN) {
				// save angle we enter the noise margin at
				double marginEnterAngle = odo.getTheta();
				do {
					try {
						Thread.sleep(REFRESH_RATE);
					} catch (Exception e) {

					}
					distance = sensors.getFrontDist();

				} while (distance > DISTANCE_TO_WALL - NOISE_MARGIN);
				// save angle we exit the noise margin at
				double marginExitAngle = odo.getTheta();

				leftMotor.stop(true);
				rightMotor.stop(false);

				// take the average of the angle we entered and exited the
				// noise margin at to save angle we saw the wall
				angleB = (marginEnterAngle + marginExitAngle) / 2;

				// break out of the parent loop
				break;
			}

		}

		// angleA is clockwise from angleB, so assume the average of the
		// angles to the right of angleB is 45 degrees past 'north'
		double deltaTheta;
		if (angleB > angleA) {
			deltaTheta = 225 - (angleA + angleB) / 2;
		} else {
			deltaTheta = 45 - (angleA + angleB) / 2;
		}

		leftMotor.stop(true);
		rightMotor.stop(false);
		// update the odometer position (example to follow:)
		odo.setPosition(new double[] { 0.0, 0.0, odo.getTheta() + deltaTheta }, new boolean[] { false, false, true });

	}
}

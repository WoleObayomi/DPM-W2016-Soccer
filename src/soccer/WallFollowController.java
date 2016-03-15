package soccer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * @author Peter Quinn
 *
 */
public class WallFollowController {

	// motors that are passed in through constructor
	private EV3LargeRegulatedMotor rightMotor;
	private EV3LargeRegulatedMotor leftMotor;

	// wallfollowing controller constants (P-Type controller)
	private final int bandCenter = 10;
	private final int bandWidth = 2;
	// correct based on the magnitude of the error
	// can be tuned by changing k (gain)
	private final int k = 10;
	private final int motorStraight = 100;


	// constructor takes left and right motors, and object to access sensor data
	/**
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 */
	public WallFollowController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}

	/**
	 * 
	 * @param distToWall measurement taken to correct robot's trajectory
	 */
	public void processData(float distToWall) {

		// if we pass a negative, stop movement
		if (distToWall == -1) {
			leftMotor.setSpeed(0);
			rightMotor.setSpeed(0);
			leftMotor.stop(true);
			rightMotor.stop(false);
			return;
		}

		// calculate the error
		int error = (int) (distToWall - bandCenter);

		int correction = k * Math.abs(error);

		if (correction > 18 * k) {
			correction = 18 * k;
		}

		if (Math.abs(error) < bandWidth) { // check if the car is within the
											// tolerable distance

			leftMotor.setSpeed(motorStraight); // Set robot moving forward
			rightMotor.setSpeed(motorStraight);
			leftMotor.forward();
			rightMotor.forward();

		} else if (error < 0) {
			// robot is too close to wall, move the right motor
			// faster and the left motor slower
			leftMotor.setSpeed(motorStraight);
			rightMotor.setSpeed(correction + motorStraight);
			leftMotor.forward();
			rightMotor.forward();

		} else {

			// robot is too far from the wall, move the left motor
			// faster and the right motor slower

			leftMotor.setSpeed(motorStraight + correction);
			rightMotor.setSpeed(motorStraight);
			leftMotor.forward();
			rightMotor.forward();
		}

	}

}

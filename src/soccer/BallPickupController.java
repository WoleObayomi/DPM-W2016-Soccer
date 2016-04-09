package soccer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * 
 * @author Wole Obayomi Jr
 *
 */

public class BallPickupController {

	private static int ballColorID;
	private static Odometer odometer;
	private static Navigation navigation;
	private static LauncherController launcher;
	private static Sensors sensors;
	private static Motors motors;
	private static final float MIN_DIST_TO_BALL = 8; // min distance to reach
														// before initiating
														// ball pickup (in cm)
	private static final float APPROACH_SPEED = 50;
	private static final float BALL_GAP = 3;
	private static final float APPROACH_DIST = 2; // each approach to the ball.
													// the bot should 'inch'
													// closer by the specified
													// value
	private static final double DIST_TO_NEXT_BALL = PhysicalConstants.BALL_DIAMTER + BALL_GAP;

	private final float BALL_HEIGHT = 3; // cm,
	private int llX;
	private int llY;
	private int urX;
	private int urY;

	/**
	 * 
	 * @param ballColorID
	 * @param odometer
	 * @param navigation
	 * @param launcher
	 * @param sensors
	 * @param motors
	 */
	public BallPickupController(int ballColorID, Odometer odometer, Navigation navigation, LauncherController launcher,

			Sensors sensors, Motors motors, int llX, int llY, int urX, int urY) {

		this.ballColorID = ballColorID;
		this.odometer = odometer;
		this.navigation = navigation;
		this.launcher = launcher;
		this.sensors = sensors;
		this.motors = motors;
		this.llX = llX;
		this.llY = llY;
		this.urX = urX;
		this.urY = urY;

	}

	/**
	 * <p>
	 * navigates to the intersection before the ball platform and begins to slowly approach platform
	 */
	public void navigateToPlatform() {
		//navigate to lower left corner of tile where the ball platform is placed
		navigation.travelTo(llX, llY, true, false);
		//orient robot so that ball platform is in its field of view
		navigation.turnTo(90);
	}
	
	
	/**
	 * 
	 * @return true when measured distance to ball is less than or equal to
	 *         optimal distance for initiating pickup
	 */
	
	private boolean closeEnoughToBall() {
		/*
		 * might need a range since we don't want to climb over the platform
		 * erroneously
		 */
		return sensors.getBallDist() <= MIN_DIST_TO_BALL;
	}

	/**
	 * <p>
	 * causes the robot to move a certain distance as it approaches the ball
	 * platform
	 */

	private void slowlyApproachPlatform() {

		/*
		 * Navigation's travel function sets speed to 100 but might need slower
		 * speed so robot doesn't react haphazardly and looks more composed
		 */
		EV3LargeRegulatedMotor leftMotor = motors.getLeftMotor();
		EV3LargeRegulatedMotor rightMotor = motors.getRightMotor();

		leftMotor.setAcceleration(2000);
		rightMotor.setAcceleration(2000);
		leftMotor.setSpeed(APPROACH_SPEED);
		rightMotor.setSpeed(APPROACH_SPEED);
		rightMotor.forward();
		leftMotor.forward();

	}

	private void stop() {
		EV3LargeRegulatedMotor leftMotor = motors.getLeftMotor();
		EV3LargeRegulatedMotor rightMotor = motors.getRightMotor();

		rightMotor.stop();
		leftMotor.stop();
	}

	/**
	 * <p>
	 * initiates ball pickup routine
	 */
	public void pickBall() {

		// navigate to the ball pickup place

		launcher.setToIntakeSpeed();
		slowlyApproachPlatform();
		// wait until we see the ball properly
		while (sensors.getBallDist() > BALL_HEIGHT) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {

			}
		}
		stop();

		// move forward 2 cm to grab ball
		navigation.travel(2);

		launcher.conveyerBackOneBall();
		launcher.stopLauncher();

		// move back 8 cm to clear platform
		navigation.travel(-10);
		//
		// if (sensors.getCenterColourValue() != ballColorID) {
		// rejectBall();
		// moveToNextBall();
		// } else {
		// // do nothing for now
		// }
	}

	/**
	 * <p>
	 * ball rejection routine
	 */

	private void rejectBall() {

		launcher.setToRejectSpeed();
		launcher.conveyerForwardOneBall();
		launcher.stopLauncher();

	}

	/**
	 * moves the robot to the next ball
	 */

	private void moveToNextBall() {

		navigation.travelTo(odometer.getX(), odometer.getY() + DIST_TO_NEXT_BALL, false, false);

	}
}

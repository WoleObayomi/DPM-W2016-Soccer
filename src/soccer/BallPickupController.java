package soccer;

public class BallPickupController {
	
	private static int ballColorID;
	private static Odometer odometer;
	private static Navigation navigation;
	private static LauncherController launcher;
	private static Sensors sensors;
	private static Motors motors;
	private static final float MIN_DIST_TO_BALL = 8; //min distance to reach before initiating ball pickup (in cm)
	private static final float APPROACH_SPEED = 50;
	private static final float BALL_GAP = 3;
	private static final float APPROACH_DIST = 2; //each approach to the ball. the bot should 'inch' closer by the specified value
	private static final double DIST_TO_NEXT_BALL = PhysicalConstants.BALL_DIAMTER + BALL_GAP;
	
	public BallPickupController(int ballColorID, Odometer odometer, Navigation navigation, LauncherController launcher, 
			Sensors sensors, Motors motors) {
		
		this.ballColorID = ballColorID;
		this.odometer = odometer;
		this.navigation = navigation;
		this.launcher = launcher;
		this.sensors = sensors;
		this.motors = motors;
		
	}
	
	private boolean closeEnoughToBall() {
		/*
		  might need a range
	      since we don't want to climb over
	      the platform erroneously
	    */
		return sensors.getBallDist() <= MIN_DIST_TO_BALL;
	}
	
	private void slowlyApproachPlatform() {
		
		/*
		 	Navigation's travel function sets speed
		 	to 100 but might need slower speed so robot 
			doesn't react haphazardly and looks more composed
		 */	
		navigation.travel(APPROACH_DIST);
		
	}
	
	public void pickBall() {
		
		while( !closeEnoughToBall() ) {
			slowlyApproachPlatform();
		}
		
		launcher.setToIntakeSpeed();
		launcher.conveyerBackOneBall();
		launcher.stopLauncher();
		
		if( sensors.getCenterColourValue() != ballColorID ) {
			rejectBall();
			moveToNextBall();
		}
		else {
			//do nothing for now
		}
	}
	
	private void rejectBall() {
		
		launcher.setToRejectSpeed();
		launcher.conveyerForwardOneBall();
		launcher.stopLauncher();
		
	}
	
	private void moveToNextBall() {
		
		navigation.travelTo(odometer.getX(), odometer.getY() + DIST_TO_NEXT_BALL, true);
		
	}
}

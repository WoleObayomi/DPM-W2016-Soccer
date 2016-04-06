package soccer;

//skeleton class that hold all static physical values of the robot and arena
/**
 * 
 * @author Peter Quinn
 *
 */
public class PhysicalConstants {

	// sensor distances
	public static final double DIST_TO_SIDE_LIGHTSENSOR = 13.5;// cm
	public static final double DIST_TO_FRONT_US = 13;// cm

	// wheel and track dimensions
	public static final double LEFT_WHEEL_RADIUS = 2.145; // cm
	public static final double RIGHT_WHEEL_RADIUS = 2.155; // cm
	public static final double TRACK_WIDTH = 20; // cm, overturning -->
													// decrease, underturning
													// --> increase

	// launcher data
	public static final double LAUNCHER_WHEEL_RADIUS = 2.3; // cm
	public static final double CONVEYER_WHEEL_RADIUS = 2.1; // cm

	// arena constants
	public static final double TILE_SPACING = 30.48; // cm
	public static final double BALL_DIAMTER = 5.1;// cm

}

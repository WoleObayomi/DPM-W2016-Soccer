/* Author: Peter Quinn (260689207) Rony Azrak (260606812)

 * Group: 47
 * 
 * Date: Feb 15,2016
 * 
 * Description: Uses the light sensor to correct the x and y values of the odometer.
 * Starts by moving the robot a sufficient distance away from both walls, then rotates the robot while
 * using ColourDataGetter to save the angles the lines are seen at. Then uses the angles to compute 
 * the correct x, y and theta of the robot and sets the values in the odometer to the correct values.
 * 
 * Edit Log:
 * 
 * March 13 - Peter: modified for final robot
 */

package soccer;

/**
 * 
 * @author Peter Quinn
 *
 */
public class LightLocalizer {
	private Odometer odo;
	private Sensors sensors;
	private Navigation navigation;

	// constants
	final double STARTING_DIST_FROM_WALL = 4;
	final int MOTOR_SPEED = 200;
	final double DIST_TO_LIGHT = PhysicalConstants.DIST_TO_SIDE_LIGHTSENSOR; // distance
																				// between
																				// color
																				// sensor
																				// and
	// center of
	// robot

	/**
	 * 
	 * @param odo
	 * @param sensors
	 * @param navigation
	 */
	public LightLocalizer(Odometer odo, Sensors sensors, Navigation navigation) {
		this.odo = odo;
		this.sensors = sensors;
		this.navigation = navigation;
	}

	/**
	 * 
	 */
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees

		// align the robot properly relative to the corner

		// be at least STARTING_DIST away from the left wall
		navigation.turnToAbs(-90);
		float distance = sensors.getFrontDist();
		if (distance < STARTING_DIST_FROM_WALL)
			navigation.travel(distance - STARTING_DIST_FROM_WALL);

		// be at least STARTING_DIST away from the back wall
		navigation.turnToAbs(180);
		distance = sensors.getFrontDist();
		if (distance < STARTING_DIST_FROM_WALL)
			navigation.travel(distance - STARTING_DIST_FROM_WALL);

		// set orientation of robot to -15 degrees to make sure it starts in a
		// good position to detect lines
		navigation.turnToAbs(-15);

		// array to hold the angles of the lines
		double[] angleData = new double[4];

		// make sure the array gets filled with angles
		while (angleData[0] == 0 || angleData[1] == 0 || angleData[2] == 0 || angleData[3] == 0) {
			// have a thread that watches for the gridlines and saves the angle
			// of
			// the robot when they are detected to an array
			ColourDataGetter colourDataGetter = new ColourDataGetter(angleData, odo, sensors);
			colourDataGetter.start();

			// turn 360 degrees to see all the lines
			navigation.turnTo(360);
			colourDataGetter.end();
		}
		// get angle data that was collected
		// account for the sensor being to the left of the robot by subtracting
		// 90 and then
		// adding 360 if it is less than 0
		double thetaYNeg, thetaYPos, thetaXNeg, thetaXPos;

		thetaXNeg = angleData[0] - 90;
		if (thetaXNeg < 0) {
			thetaXNeg += 360;
		}
		thetaYPos = angleData[1] - 90;
		if (thetaYPos < 0) {
			thetaYPos += 360;
		}

		thetaXPos = angleData[2] - 90;
		if (thetaXPos < 0) {
			thetaXPos += 360;
		}
		thetaYNeg = angleData[3] - 90;
		if (thetaYNeg < 0) {
			thetaYNeg += 360;
		}

		// calculations of angle

		double deltaThetaY = thetaYNeg - thetaYPos;
		if (deltaThetaY < 0) {
			deltaThetaY += 360;
		}

		double deltaThetaX = thetaXPos - thetaXNeg;
		if (deltaThetaX < 0) {
			deltaThetaX += 360;
		}

		// calculations of position
		double x = -DIST_TO_LIGHT * Math.cos(Math.toRadians(deltaThetaY / 2));
		double y = -DIST_TO_LIGHT * Math.cos(Math.toRadians(deltaThetaX / 2));

		double deltaTheta1 = 90 - (thetaYPos - 180) + deltaThetaY / 2;

		// we tried to use this extra calculation to improve the angle
		// adjustment, but it made things worse
		// may try using it in the future
		// double deltaTheta2 = 90 - (thetaXNeg - 180) + deltaThetaX / 2;
		// double aveDeltaTheta = (deltaTheta1 + deltaTheta2) / 2;

		// update position
		odo.setPosition(new double[] { x, y, odo.getTheta() + deltaTheta1 + 45 }, new boolean[] { true, true, true });

	}

}

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

public class LightLocalizer {
	private Odometer odo;
	private Sensors sensors;
	private Navigation navigation;

	// constants
	final int STARTING_DIST_FROM_WALL = 17;
	final int MOTOR_SPEED = 100;
	final double DIST_TO_LIGHT = PhysicalConstants.DIST_TO_SIDE_LIGHTSENSOR; // distance between color sensor and
										// center of
										// robot

	public LightLocalizer(Odometer odo, Sensors sensors, Navigation navigation) {
		this.odo = odo;
		this.sensors = sensors;
		this.navigation = navigation;
	}

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

		// set orientation of robot to 45 degrees to make sure it starts in a
		// good position to detect lines
		navigation.turnToAbs(45);

		// array to hold the angles of the lines
		double[] angleData = new double[4];

		// have a thread that watches for the gridlines and saves the angle of
		// the robot when they are detected to an array
		ColourDataGetter colourDataGetter = new ColourDataGetter(angleData, odo, sensors);
		colourDataGetter.start();

		// turn 360 degrees to see all the lines
		navigation.turnTo(360);

		// get angle data that was collected
		// account for the sensor being behind the robot by adding 180 and then
		// subtracting 360 if it over 360
		double thetaYNeg, thetaYPos, thetaXNeg, thetaXPos;

		thetaXNeg = angleData[0] + 180;
		if (thetaXNeg > 360) {
			thetaXNeg -= 360;
		}
		thetaYPos = angleData[1] + 180;
		if (thetaYPos > 360) {
			thetaYPos -= 360;
		}

		thetaXPos = angleData[2] + 180;
		if (thetaXPos > 360) {
			thetaXPos -= 360;
		}
		thetaYNeg = angleData[3] + 180;
		if (thetaYNeg > 360) {
			thetaYNeg -= 360;
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
		double deltaTheta2 = 90 - (thetaXNeg - 180) + deltaThetaX / 2;
		double aveDeltaTheta = (deltaTheta1 + deltaTheta2) / 2;

		// update position
		odo.setPosition(new double[] { x, y, odo.getTheta() + deltaTheta2 }, new boolean[] { true, true, true });

		// proceed to 0,0,0

		navigation.travelTo(0, 0, true);
		navigation.turnToAbs(0);

	}

}

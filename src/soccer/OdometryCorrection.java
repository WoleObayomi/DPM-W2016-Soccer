/* 
 * OdometryCorrection.java
 * 
 * Author: Peter Quinn (260689207)
 * 
 * Created: 8/2/2016
 * 
 * Desc:Corrects an odometer passed to it by using a light sensor to detect gridlines
 * and correction based on where the gridlines are known to be located
 * 
 * Edit History:
 * March 13 - Peter: modified for final robot 
 * 
 */
package soccer;

//NOTE: this class can use quite a bit of cleaning up if we have time. 
//	1. Implement code so it this works even if the DIST_TO_SENSOR is not 0
//	2. Find some way to use mods instead of loops to make the code more efficient
//	3. Find some way to put all the data filtering in the sensors class, and just have a 
//		boolean to check to know if we crossed a line, just for organization sake. It 
//		won't be the end of the world if we have to do the filtering in the run of this thread.
public class OdometryCorrection extends Thread {

	private static final int CORRECTION_PERIOD = 10;
	private Odometer odometer;
	private Sensors sensors;

	// constants we need

	// only snap if we are at least this close to a line
	private final int ERROR_THRESHOLD = 6;

	// the spacing of the grid lines
	private final double GRID_SPACING = 30.48;

	// distance the sensor is in front of the center of the axle
	// we moved this to zero after doing the code
	private final double DIST_TO_SENSOR = 0;

	// constructor
	public OdometryCorrection(Odometer odometer, Sensors sensors) {
		this.odometer = odometer;
		this.sensors = sensors;
	}

	// helper method
	private void snapToNearestGridLine() {

		// get current x and y according to odometer (of center of bot)
		double x = odometer.getX();
		double y = odometer.getY();

		// subtract off 15 from starting in the middle of a tile
		// add the distance from the odometer to the sensor to find the location
		// of the sensor
		x = x + DIST_TO_SENSOR;
		y = y + DIST_TO_SENSOR;

		// subtract by gridline spacing distance of 30. Stop when we find we are
		// close to a line or we have dropped too far to find a line
		int xCount = 0;
		while (Math.abs(x) > ERROR_THRESHOLD && x > -GRID_SPACING) {
			x -= GRID_SPACING;
			xCount++;
		}

		// subtract by gridline spacing distance of 30. Stop when we find we are
		// close to a line or we have dropped too far to find a line
		int yCount = 0;
		while (Math.abs(y) > ERROR_THRESHOLD && y > -GRID_SPACING) {
			y -= GRID_SPACING;
			yCount++;
		}

		// analyze what happened in the loops
		if (Math.abs(x) < ERROR_THRESHOLD && Math.abs(y) < ERROR_THRESHOLD) {
			// close to an intersection, try setting both x and y
			// commented out because it seemed to cause more harm than good
			/*
			 * odometer.setX(GRID_SPACING * xCount - DIST_TO_SENSOR);
			 * odometer.setY(GRID_SPACING * yCount - DIST_TO_SENSOR);
			 */
		} else if (Math.abs(x) < ERROR_THRESHOLD) {

			// close to x line, set x in the odometer to the
			// correct value of the grid line (minus the distance the sensor is
			// ahead)
			odometer.setX(GRID_SPACING * xCount - DIST_TO_SENSOR);

		} else if (Math.abs(y) < ERROR_THRESHOLD) {

			// close to y line, set y in the odometer to the
			// correct value of the grid line (minus the distance the sensor is
			// ahead)
			odometer.setY(GRID_SPACING * yCount - DIST_TO_SENSOR);

		} else {
			// fix nothing if we are not close to anything
			// we probably saw a random dark spot
		}
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		float lastColor = sensors.getCenterColourValue();
		float color;
		while (true) {
			correctionStart = System.currentTimeMillis();

			// get the value of the ambient light under the sensor
			color = sensors.getCenterColourValue();

			// tweak this to find black lines
			// if we see a difference of at least .12, its a black line
			if (lastColor - color >= .12) {

				// execute correction method
				snapToNearestGridLine();

				// sleep for a longer time because we don't want to read the
				// same black line several times
				correctionEnd = System.currentTimeMillis();
				if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
					try {
						Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
					} catch (InterruptedException e) {
						// there is nothing to be done here because it is not
						// expected that the odometry correction will be
						// interrupted by another thread
					}
				}

			}

			// update last colour
			lastColor = sensors.getCenterColourValue();

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}
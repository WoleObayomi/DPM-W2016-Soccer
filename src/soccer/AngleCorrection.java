/*
 * Title: Angle Correction
 * Author: Peter Quinn
 * Created: Marhc 24, 2016
 * 
 * Description: Uses LineListeners to detect when the two gridline sensing colour
 * sensors detect lines and saves the x and y from odometer when they are detected. This
 * data is then used to calculate and correct the angle of the robot.
 * 
 * Edit Log:
 * 
 */

package soccer;

/**
 * 
 * @author Peter Quinn
 *
 */
public class AngleCorrection extends Thread {

	private final int CORRECTION_PERIOD = 10; // ms
	private final double SENSOR_SEP = PhysicalConstants.DIST_TO_SIDE_LIGHTSENSOR;
	private final int WAIT_LIMIT = 100;

	private LineListener centerLine;
	private LineListener sideLine;
	private Odometer odometer;
	private Navigation nav;
	private Sensors sensors;

	private double x1, x2, y1, y2;

	/**
	 * 
	 * @param odometer
	 * @param nav
	 * @param sensors
	 */
	public AngleCorrection(Odometer odometer, Navigation nav, Sensors sensors) {
		this.sensors = sensors;
		this.nav=nav;
		this.odometer=odometer;
	}

	@Override
	public void run() {
		long correctionStart, correctionEnd;
		sideLine = new LineListener(sensors.getSideLSSampleProvider());
		centerLine = new LineListener(sensors.getCenterLSSampleProvider());
		sideLine.start();
		centerLine.start();

		while (true) {
			correctionStart = System.currentTimeMillis();

			// check if we are moving in a straight line
			if (nav.isStraight()) {

				// check for center detected
				if (centerLine.lineDetected()) {
					x1 = odometer.getX();
					y1 = odometer.getY();
					centerLine.reset();

					// wait for left side sensor
					int waitCounter = 0;
					while (waitCounter < WAIT_LIMIT) {

						if (sideLine.lineDetected()) {
							x2 = odometer.getX();
							y2 = odometer.getY();
							sideLine.reset();
							double angle = calculateAngle(true);
							odometer.setTheta(correctedAngle(angle));
							break;
						}
						waitCounter++;
						try {
							sleep(CORRECTION_PERIOD);
						} catch (InterruptedException e) {
						}
					}
				} else if (sideLine.lineDetected()) {
					x1 = odometer.getX();
					y1 = odometer.getY();
					sideLine.reset();

					// wait for center sensor
					int waitCounter = 0;
					while (waitCounter < WAIT_LIMIT) {

						if (centerLine.lineDetected()) {
							x2 = odometer.getX();
							y2 = odometer.getY();
							centerLine.reset();
							double angle = calculateAngle(false);
							odometer.setTheta(correctedAngle(angle));
							break;
						}
						waitCounter++;
						try {
							sleep(CORRECTION_PERIOD);
						} catch (InterruptedException e) {
						}
					}
				}

			}

			// this ensure the correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done
				}
			}
		}
	}

	// gets the Cartesian angle the robot cross the line at, in a relative frame
	/**
	 * 
	 * @param centerFirst
	 * @return angle
	 */
	private double calculateAngle(boolean centerFirst) {
		double posDelta = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

		double angle = Math.toDegrees(Math.atan(SENSOR_SEP / posDelta));

		if (centerFirst) {
			angle = 180 - angle;
		}
		return angle;
	}

	// gets the appropriate value to pass to the odometer
	/**
	 * 
	 * @param angle
	 * @return correctedAngle
	 */
	private double correctedAngle(double angle) {

		double currentAngle = odometer.getTheta();
		double correctAngle = 0;
		if (currentAngle <= 45 || currentAngle > 315) { // N
			correctAngle = 90 - angle;
		} else if (currentAngle > 45 && currentAngle <= 135) {// E
			correctAngle = 180 - angle;
		} else if (currentAngle > 135 && currentAngle <= 225) {// S
			correctAngle = 270 - angle;
		} else if (currentAngle > 225 && currentAngle <= 315) {// W
			correctAngle = 360 - angle;
		}

		return correctAngle;

	}

}

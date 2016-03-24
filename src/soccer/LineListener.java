/*
 * Title: LineListener
 * Date created: March 24, 2016
 * Author: Peter Quinn
 * 
 * Description: Takes a sample provider of a colour and sensor and 
 * uses the data from it to detect gridlines. Implements a derivative filter
 * to detect lines.
 * 
 * Edit History:
 * 
 */

package soccer;

import lejos.robotics.SampleProvider;

public class LineListener extends Thread {


	private final float THRESHOLD = (float) .12; //tweak this to set the sensitivity of the filer
	private final int SLEEP_TIME = 10; // ms

	private SampleProvider colourSensor;
	private float[] colourData;
	private boolean lineDetected;
	private float[] pointData;

	// pass a colour sensor sampleProvider to the constructor
	public LineListener(SampleProvider colourSensor) {

		this.colourSensor = colourSensor;
		colourData = new float[colourSensor.sampleSize()];
		lineDetected = false;
		pointData = new float[3];

	}

	@Override
	// invoke with .start()

	// continually check if there is a line. If there is, set the boolean to
	// true.

	public void run() {
		long correctionStart, correctionEnd;

		colourSensor.fetchSample(colourData, 0);

		while (true) {
			correctionStart = System.currentTimeMillis();

			if (detectLine()) {
				lineDetected = true;
			}
			// this ensure that the line is looked for only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < SLEEP_TIME) {
				try {
					Thread.sleep(SLEEP_TIME - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// don't expect interruption
				}
			}
		}
	}

	// reports if a line has been detected
	public boolean lineDetected() {
		return lineDetected;
	}

	// reset the line detected boolean, must always be done after a line is
	// detected
	public void reset() {
		lineDetected = false;
	}

	private void updatePoints() {
		pointData[0] = pointData[1];
		pointData[1] = pointData[2];
		colourSensor.fetchSample(colourData, 0);
		pointData[2] = colourData[0];
	}

	private boolean detectLine() {

		updatePoints();
		// take the slope (linear derivative)
		float d1 = pointData[1] - pointData[0];
		float d2 = pointData[2] - pointData[1];

		// compare magnitudes of slope, if d2 is significantly bigger than d1,
		// we are dropping rapidly, so hopefully seeing a line
		if (Math.abs(d2) - Math.abs(d1) > THRESHOLD) {
			return true;

		} else {
			return false;
		}
	}

}

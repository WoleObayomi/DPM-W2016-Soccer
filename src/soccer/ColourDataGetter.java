/* Author: Peter Quinn (260689207), Rony Azrak (260606812)
 * Group: 47
 * 
 * 
 * Date: Feb 15,2016
 * 
 * Description: A thread that save the angle of the odometer to an array passed to it 
 * when it sees a gridline. Holds 4 gridlines 
 * 
 * 
 * Edit Log:
 * March 26 - Peter: modifed to take advantage of the new LineListener class
 */

package soccer;

import lejos.hardware.Sound;

public class ColourDataGetter extends Thread {

	private double[] angleData;
	private final int CORRECTION_PERIOD = 50;
	private final int LONG_SLEEP = 200;

	private Odometer odometer;
	private Sensors sensors;

	public ColourDataGetter(double[] angleData, Odometer odometer, Sensors sensors) {
		this.angleData = angleData;
		this.odometer = odometer;
		this.sensors=sensors;
	}

	@Override
	public void run() {

		long correctionStart, correctionEnd;

		float lastColor = sensors.getSideColourValue();
		float currentColor;
		int lineCounter = 0;

		while (true) {
			correctionStart = System.currentTimeMillis();

			// create a line listener to detect lines
			LineListener line = new LineListener(sensors.getSideLSSampleProvider());
			line.start();

			//check if we find a line
			if (line.lineDetected()) {
				
				line.reset(); //reset the boolean
				
				Sound.setVolume(85);
				Sound.beep();

				angleData[lineCounter] = odometer.getTheta();
				lineCounter++;

				// return when we see all 4 lines
				if (lineCounter == 4) {
					Sound.setVolume(0);
					return;
				}

				// sleep for a longer time because we don't want to read the
				// same black line several times
				correctionEnd = System.currentTimeMillis();
				if (correctionEnd - correctionStart < LONG_SLEEP) {
					try {
						Thread.sleep(LONG_SLEEP - (correctionEnd - correctionStart));
					} catch (InterruptedException e) {
						// there is nothing to be done here
					}
				}
			}

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here
				}
			}

		}

	}

}

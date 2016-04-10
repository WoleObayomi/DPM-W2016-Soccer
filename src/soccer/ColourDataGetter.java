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

import java.util.ArrayList;

import lejos.hardware.Sound;

/**
 * 
 * @author Peter Quinn
 *
 */
public class ColourDataGetter extends Thread {

	private ArrayList<Double> angleData;
	private final int CORRECTION_PERIOD = 20;
	private final int LONG_SLEEP = 200;
	private boolean on = true;
	private Odometer odometer;
	private Sensors sensors;

	/**
	 * 
	 * @param angleData
	 * @param odometer
	 * @param sensors
	 */
	public ColourDataGetter(ArrayList<Double> angleData, Odometer odometer, Sensors sensors) {
		this.angleData = angleData;
		this.odometer = odometer;
		this.sensors = sensors;
	}

	
	@Override
	public void run() {

		long correctionStart, correctionEnd;

		
		// create a line listener to detect lines
		LineListener line = new LineListener(sensors.getSideLSSampleProvider());
		line.start();

		angleData.clear();
		while (on) {
			correctionStart = System.currentTimeMillis();

			// check if we find a line
			if (line.lineDetected()) {

				line.reset(); // reset the boolean

				angleData.add(odometer.getTheta());
				

	
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
		//end line thread when this thread is turned off
		line.end();

	}

	/**
	 * <p>
	 * ends the color obtaining process
	 */
	public void end() {
		on = false;
	}

}

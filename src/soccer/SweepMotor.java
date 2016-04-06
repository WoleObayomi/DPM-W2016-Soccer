/*
 * Title: SweepMotor
 * Author: Peter Quinn
 * 
 * Date: April 6, 2016
 * 
 * Description: This thread takes a motor in its constructor and sweeps it back and forth 
 * at a constant speed within a certain range of angles. Sweeping can be temporarily
 * turned off or on using the off and on methods
 * 
 * Edit History:
 * 
 */

package soccer;

import lejos.robotics.RegulatedMotor;

public class SweepMotor extends Thread {

	private RegulatedMotor usMotor;
	private boolean sweep = false;
	private boolean goingLeft = true;
	private Object lock;
	private boolean alive = true;

	// constants

	private final int CORRECTION_PERIOD = 25; // ms
	private final int RIGHT_MAX_ANGLE = 14;
	private final int LEFT_MAX_ANGLE = 30;
	private final int SPEED = 50;
	private final int ACCELERATION = 2000;

	public SweepMotor(RegulatedMotor usMotor) {
		this.usMotor = usMotor;
		usMotor.setSpeed(SPEED);
		usMotor.setAcceleration(ACCELERATION);
		lock = new Object();
	}

	@Override
	public void run() {
		long correctionEnd, correctionStart;
		while (Exit.alive() && alive) {

			correctionStart = System.currentTimeMillis();
			synchronized (lock) {
				if (sweep) {

					float tachoCount = usMotor.getTachoCount();
					// if we are outside range for some reason, reset
					if (tachoCount < -LEFT_MAX_ANGLE || tachoCount > RIGHT_MAX_ANGLE) {
						if (tachoCount < 0) {
							usMotor.forward();
							goingLeft = false;
						} else {
							usMotor.backward();
							goingLeft = true;
						}
					}

					// going left
					if (goingLeft) {
						// check if at end yet
						if (tachoCount >= -LEFT_MAX_ANGLE) {
							usMotor.backward();// if not at end, go left more
						} else { // all the way left, go right
							usMotor.forward();
							goingLeft = false;
						}
					} else { // going right
						if (tachoCount <= RIGHT_MAX_ANGLE) {
							usMotor.forward(); // check if at right limit
						} else {// all the way right, go left again
							usMotor.backward();
							goingLeft = true;
						}

					}
				}

			}

			// this ensure the sweep occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the thread will be
					// interrupted by another thread
				}
			}
		}
		
		usMotor.flt();
	}

	// turn on sweeping
	public void on() {
		synchronized (lock) {
			sweep = true;
		}

	}

	// turn off sweeping
	public void off() {
		synchronized (lock) {
			sweep = false;
		}
	}

	public void kill() {
		alive = false;
	}
}

/* Author: Peter Quinn (260689207) Rony Azrak (260606812)
 *  
 * Date: Feb 15,2016
 * 
 * Description: A thread that detects if the back button is pressed. If the back button is pressed, 
 * it ends the program.
 * 
 */

package soccer;

import lejos.hardware.Button;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.robotics.RegulatedMotor;

/**
 * 
 * @author Peter Quinn
 *
 */
public class Exit extends Thread {

	private RemoteRequestEV3 slaveBrick = null;
	private Motors motors = null;
	private RegulatedMotor launcherLeft, launcherRight, angleMotor, sweepMotor;
	private final int SLEEP_TIME = 200;
	private static boolean alive = true;

	public void run() {

		while (Button.waitForAnyPress() != Button.ID_ESCAPE) {

			try {
				sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//wait for threads looking at this to stop nicely
		alive=false;
		try {
			sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		// disconnect slave brick
		if (slaveBrick != null)
			slaveBrick.disConnect();

		System.exit(0);
	}

	// constructor
	/**
	 * 
	 * @param slaveBrick
	 */
	public Exit(RemoteRequestEV3 slaveBrick) {

		this.slaveBrick = slaveBrick;

	}

	/**
	 * 
	 * @param slaveBrick
	 * <p>
	 * initialize slave brick
	 */
	public void addSlaveBrick(RemoteRequestEV3 slaveBrick) {
		this.slaveBrick = slaveBrick;
	}

	/**
	 * 
	 * @param motors
	 * <p>
	 * initialize motors
	 */
	public void addMotors(Motors motors) {
		this.motors = motors;
		this.launcherLeft = motors.getLauncherLeft();
		this.launcherRight = motors.getLauncherRight();
		this.angleMotor = motors.getAngleAdjustMotor();
		this.sweepMotor = motors.getUSMotor();
	}

	// alternate constructor

	public Exit() {

	}
	
	//can notify threads to end certain tasks peacfully
	/**
	 * 
	 * @return boolean
	 * <p>
	 * returns whether the exit thread is alive or not
	 */
	public static boolean alive(){
		return alive;
	}

}

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

/**
 * 
 * @author Peter Quinn
 *
 */
public class Exit extends Thread {

	private RemoteRequestEV3 slaveBrick;

	public void run() {

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		slaveBrick.disConnect();
		System.exit(0);
	}

	public Exit(RemoteRequestEV3 slaveBrick) {

		this.slaveBrick = slaveBrick;

	}

}

/* Author: Peter Quinn (260689207) Rony Azrak (260606812)
 * Group: 47
 * 
 * Date: Feb 15,2016
 * 
 * Description: A thread that detects if the back button is pressed. If the back button is pressed, 
 * it ends the program.
 * 
 */


package soccer;

import lejos.hardware.Button;

public class Exit extends Thread{
	
	public void run() {
		
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}

	

}

/*Author: Peter Quinn
* Initial Creation Date: March 12, 2016
* 
* Description: Class to create an object to set up and allow easy access to the sensors 
* on the two EV3 bricks by other classes. Also performs data filtering
*  
* Edit Log:
*/
package soccer;

import lejos.hardware.Brick;

public class Sensors {

	// brick variables
	Brick masterBrick;
	Brick slaveBrick;

	// sensor variables

	public Sensors(Brick masterBrick, Brick slaveBrick) {
		this.masterBrick = masterBrick;
		this.slaveBrick = slaveBrick;

		// set up sensors below
		// for now we'll need: front US, side US, center light sensor

	}

	// add some filters

	// add getters for sensor data

	public boolean isFrontWall() {
		// TODO Auto-generated method stub
		return false;
	}

	public float getSideDist() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getFrontDist() {
		// TODO Auto-generated method stub
		return 0;
	}

	// it would be awesome if we could find some way to write a function that
	// would return a boolean when a line is detected
	
	public float getCenterColourValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getSideColourValue() {
		// TODO Auto-generated method stub
		return 0;
	}
}

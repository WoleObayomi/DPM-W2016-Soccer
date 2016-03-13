package soccer;

import lejos.hardware.Brick;
/*Author: Peter Quinn
* Initial Creation Date: March 12, 2016
* 
* Description: Class to create an object to set up and allow easy access to the motors on 
* the two EV3 bricks by other classes
*  
* Edit Log:
*/

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Motors {
	
	//brick variables
	Brick masterBrick;
	Brick slaveBrick;
	
	//motor variables
	EV3LargeRegulatedMotor leftmotor;
		//etc
	
	public Motors(Brick masterBrick, Brick slaveBrick){
		this.masterBrick=masterBrick;
		this.slaveBrick=slaveBrick;
		
		//get ports from bricks and assign the motors the ports
	}
	
	//getters for motors
	public EV3LargeRegulatedMotor getLeftMotor(){
		return null;
		
	}
	
	public EV3LargeRegulatedMotor getRightMotor(){
		return null;
	}

	public EV3LargeRegulatedMotor getLauncherRight(){
		return null;
	}
	
	public EV3LargeRegulatedMotor getLauncherLeft(){
		return null;
		
	}
}

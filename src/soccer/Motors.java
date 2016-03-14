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
import lejos.hardware.port.MotorPort;

public class Motors {
	
	//brick variables
	Brick masterBrick;
	Brick slaveBrick;
	
	//motor variables
	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;
	EV3LargeRegulatedMotor launcherRight;
	EV3LargeRegulatedMotor launcherLeft;
	
	public Motors(Brick masterBrick, Brick slaveBrick){
		this.masterBrick=masterBrick;
		this.slaveBrick=slaveBrick;
		
		//get ports from bricks and assign the motors the arbitrary ports for now
		leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
		rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		launcherLeft = new EV3LargeRegulatedMotor(MotorPort.C);
		launcherRight = new EV3LargeRegulatedMotor(MotorPort.D);
	}
	
	//getters for motors
	public EV3LargeRegulatedMotor getLeftMotor(){
		return leftMotor;
		
	}
	
	public EV3LargeRegulatedMotor getRightMotor(){
		return rightMotor;
	}

	public EV3LargeRegulatedMotor getLauncherRight(){
		return launcherRight;
	}
	
	public EV3LargeRegulatedMotor getLauncherLeft(){
		return launcherLeft;
		
	}
}

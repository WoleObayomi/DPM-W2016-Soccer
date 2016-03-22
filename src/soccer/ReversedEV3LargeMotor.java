package soccer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

//allows reversed motors to be used more intuitively 
/**
 * 
 * @author Peter Quinn
 *
 */
public class ReversedEV3LargeMotor extends EV3LargeRegulatedMotor{

	/**
	 * 
	 * @param port
	 */
	public ReversedEV3LargeMotor(Port port) {
		super(port);
	}

	/**
	 * <p> Normalizes the forward movement (in the code) of the motor when orientation is reversed
	 */
	public void forward(){
		super.backward();
	}
	
	/**
	 * <p> Normalizes the backward movement (in the code) of the motor when orientation is reversed
	 */
	public void backward(){
		super.forward();
	}
	
	/**
	 * <p> Normalizes the tachometer count of the motor when orientation is reversed
	 */
	public int getTachoCount(){
		return -super.getTachoCount();
	}
	
	/**
	 * <p> Normalizes the angle of rotation of the motor when orientation is reversed
	 */
	public void rotate(int angle){
		super.rotate(-angle);
	}
	/**
	 * <p> Normalizes the forward movement of the motor when orientation is reversed and immediate return is desired
	 */
	public void rotate(int angle, boolean immediateReturn){
		super.rotate(-angle, immediateReturn);
	}
	
}

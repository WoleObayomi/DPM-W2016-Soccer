package soccer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

//allows reversed motors to be used more intuitively 
public class ReversedEV3LargeMotor extends EV3LargeRegulatedMotor{

	public ReversedEV3LargeMotor(Port port) {
		super(port);
	}

	public void forward(){
		super.backward();
	}
	
	public void backward(){
		super.forward();
	}
	
	public int getTachoCount(){
		return -super.getTachoCount();
	}
}

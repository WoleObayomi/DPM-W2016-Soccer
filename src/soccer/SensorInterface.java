package soccer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public interface SensorInterface {
	
	public EV3LargeRegulatedMotor getLeftMotor();
	public EV3LargeRegulatedMotor getRightMotor();
	public EV3LargeRegulatedMotor getLeftLauncherMotor();
	public EV3LargeRegulatedMotor getRightLauncherMotor();
	
	
}

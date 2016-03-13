package soccer;

public class PlannerOffense {
	
	
	private Odometer odometer;
	private Navigation nav;
	private Sensors sensors;
	private Motors motors;

	public PlannerOffense(Odometer odometer, Navigation nav, Sensors sensors, Motors motors){
		this.odometer = odometer;
		this.nav = nav;
		this.sensors = sensors;
		this.motors = motors;
	}
	
	//have the robot execute its offense procedure
	public void run(){
		
		//localize first using 
		USLocalization USLocalizer = new USLocalization(sensors, odometer, motors.getLeftMotor(), motors.getRightMotor());
		USLocalizer.doLocalization();
		
	}

}

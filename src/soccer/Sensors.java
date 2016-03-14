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
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.*;

public class Sensors {

	// brick variables
	Brick masterBrick;
	Brick slaveBrick;

	// sensor variables
	
	//static so all instances of Sensors class share the
	//same variables for each connected sensor
	private static SensorModes frontUS;
	private static SampleProvider frontUSValue;
	private static float[]frontUSData;
	private static SensorModes sideUS;
	private static SampleProvider sideUSValue;
	private static float[]sideUSData;
	private static SensorModes centerLS;
	private static SampleProvider centerLSValue;
	private static float[]centerLSData;
	private static Port frontUSPort;
	private static Port sideUSPort;
	private static Port centerLSPort;
	private static String sensorMode = "median";
	public Sensors(Brick masterBrick, Brick slaveBrick) {
		this.masterBrick = masterBrick;
		this.slaveBrick = slaveBrick;
		
		// set up sensors below
		// for now we'll need: front US, side US, center light sensor
		
		//not sure what ports to assign
		frontUSPort = null;
		sideUSPort = null;
		centerLSPort = null;
		//setting up front ultrasonic sensor
		frontUS = new EV3UltrasonicSensor(frontUSPort);
		frontUSValue = frontUS.getMode("Distance");
		frontUSData = new float[frontUSValue.sampleSize()];
		
		//setting up side ultrasonic sensor
		sideUS = new EV3UltrasonicSensor(sideUSPort);
		sideUSValue = sideUS.getMode("Distance");
		sideUSData = new float[sideUSValue.sampleSize()];
		
		//setting up center light sensor
		centerLS = new EV3ColorSensor(centerLSPort);
		centerLSValue = centerLS.getMode("Red");
		centerLSData = new float[centerLSValue.sampleSize()];
	}

	// add some filters
	/**
	 * 
	 * @param sp
	 * @param data
	 * @return mean filtered data
	 */
	private float meanFilter(SampleProvider sp, float[]data) {
		new MeanFilter(sp, sp.sampleSize()).fetchSample(data, 0);
		return data[0];
	}
	
	/**
	 * 
	 * @param sp
	 * @param data
	 * @return median filtered data
	 */
	private float medianFilter(SampleProvider sp, float[]data) {
		new MedianFilter(sp, sp.sampleSize()).fetchSample(data, 0);
		return data[0];
	}
	
	// add getters for sensor data
	/**
	 * 
	 * @param filterType
	 * @return filteredData
	 */
	public float getFrontUSData(String filterType) {
		String filterTypeLC = filterType.toLowerCase();
		float filteredData = -666;
		//use either median or mean filtering
		switch(filterTypeLC) {
			case "median": 
				filteredData = medianFilter(frontUSValue, frontUSData);
			break;
			
			case "mean":
				filteredData = meanFilter(frontUSValue, frontUSData);
			break;
			
			default:
				System.out.println("Invalid option");
			break;
		
		}
		
		return filteredData;
	}
	
	/**
	 * 
	 * @param filterType
	 * @return filteredData
	 */
	public float getSideUSData(String filterType) {
		String filterTypeLC = filterType.toLowerCase();
		float filteredData = -666;
		//use either median or mean filtering
		switch(filterTypeLC) {
			case "median": 
				filteredData = medianFilter(sideUSValue, sideUSData);
			break;
			
			case "mean":
				filteredData = meanFilter(sideUSValue, sideUSData);
			break;
			
			default:
				System.out.println("Invalid option");
			break;
		
		}
		
		return filteredData;
	}
	
	public float getCenterLSData(String filterType) {
		String filterTypeLC = filterType.toLowerCase();
		float filteredData = -666;
		//use either median or mean filtering
		switch(filterTypeLC) {
			case "median": 
				filteredData = medianFilter(centerLSValue, centerLSData);
			break;
			
			case "mean":
				filteredData = meanFilter(centerLSValue, centerLSData);
			break;
			
			default:
				System.out.println("Invalid option");
			break;
		
		}
		
		return filteredData;
	}
	
	public boolean isFrontWall() {
		// TODO Auto-generated method stub
		return false;
	}

	public float getSideDist() {
		// TODO Auto-generated method stub
		return getSideUSData(sensorMode);
	}

	public float getFrontDist() {
		// TODO Auto-generated method stub
		return getFrontUSData(sensorMode);
	}

	// it would be awesome if we could find some way to write a function that
	// would return a boolean when a line is detected
	
	public float getCenterColourValue() {
		// TODO Auto-generated method stub
		return getCenterLSData(sensorMode);
	}

	public float getSideColourValue() {
		// TODO Auto-generated method stub
		return 0;
	}
}

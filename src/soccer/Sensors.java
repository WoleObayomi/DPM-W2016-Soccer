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
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.remote.ev3.RemoteRequestSampleProvider;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.*;

/**
 * 
 * @author Peter Quinn
 * @author Wole Obayomi Jr
 *
 */
public class Sensors {

	// brick variables
	Brick masterBrick;
	RemoteRequestEV3 slaveBrick;

	// PORT ASSIGNMENTS
	// master
	private static String frontUSPort = "S1";
	private static String sideUSPort = "S2";
	private static String centerLSPort = "S3";
	private static String sideLSPort = "S4";

	// slave
	private static String ballColourIDPort = "S1";
	private static String ballUSPort = "S2";

	private static final int FILTER_MAX = 3;
	private static final int FLITER_SLEEP = 10;//ms
	// static so all instances of Sensors class share the
	// same variables for each connected sensor

	private static SensorModes frontUS;
	private static SampleProvider frontUSValue;
	private static float[] frontUSData;
	private static int frontUSFilterCount = 0;

	private static SensorModes sideUS;
	private static SampleProvider sideUSValue;
	private static float[] sideUSData;
	private static int sideUSFilterCount = 0;

	private static SensorModes centerLS;
	private static SampleProvider centerLSValue;
	private static float[] centerLSData;

	private static SensorModes sideLS;
	private static SampleProvider sideLSValue;
	private static float[] sideLSData;

	private static SensorModes ballUS;
	private static SampleProvider ballUSValue;
	private static float[] ballUSData;
	private static int ballUSFilterCount = 0;

	private static SensorModes ballColourID;
	private static SampleProvider ballColourIDValue;
	private static float[] ballColourIDData;

	/**
	 * 
	 * @param masterBrick
	 * @param slaveBrick
	 */
	public Sensors(Brick masterBrick, RemoteRequestEV3 slaveBrick) {
		this.masterBrick = masterBrick;
		this.slaveBrick = slaveBrick;

		// set up sensors below

		// masterBrick Sensors

		// front ultrasonic sensor
		frontUS = new EV3UltrasonicSensor(masterBrick.getPort(frontUSPort));
		frontUSValue = frontUS.getMode("Distance");
		frontUSData = new float[frontUSValue.sampleSize()];

		// side ultrasonic sensor
		sideUS = new EV3UltrasonicSensor(masterBrick.getPort(sideUSPort));
		sideUSValue = sideUS.getMode("Distance");
		sideUSData = new float[sideUSValue.sampleSize()];

		// setting up center light sensor
		centerLS = new EV3ColorSensor(masterBrick.getPort(centerLSPort));
		
		centerLSValue = centerLS.getMode("Red");
		centerLSData = new float[centerLSValue.sampleSize()];

		// setting up side light sensor
		sideLS = new EV3ColorSensor(masterBrick.getPort(sideLSPort));
		sideLSValue = sideLS.getMode("Red");
		sideLSData = new float[sideLSValue.sampleSize()];

		// slaveBrick
		

		ballUSValue = slaveBrick.createSampleProvider(ballUSPort, "lejos.hardware.sensor.EV3UltrasonicSensor", "Distance");
		ballUSData = new float[ballUSValue.sampleSize()];

		ballColourIDValue = slaveBrick.createSampleProvider(ballColourIDPort, "lejos.hardware.sensor.EV3ColorSensor", "Red");
		ballColourIDData = new float [ballColourIDValue.sampleSize()];
	}

	// add getters for sensor data
	/**
	 * 
	 * 
	 * @return data or 254
	 */
	public float getFrontDist() {

		frontUSValue.fetchSample(frontUSData, 0);
		float data = frontUSData[0];
		// convert to cm
		data *= 100;

		if (data < 254) {
			// good value, set filter counter to 0 and return the data
			frontUSFilterCount = 0;
			return data;
		} else if (frontUSFilterCount < FILTER_MAX) {
			// could be noise, so increment the counter and try getting a new
			// value
			frontUSFilterCount++;
			
			//wait to be sure we have a new signal
			try {
				Thread.sleep(FLITER_SLEEP);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			return getFrontDist();

		} else {
			// got 254 multiple times, so its probably true
			return 254;
		}

	}

	/**
	 * 
	 * 
	 * @return data or 254
	 */
	public float getSideDist() {

		sideUSValue.fetchSample(sideUSData, 0);
		float data = sideUSData[0];
		// convert to cm
		data *= 100;

		if (data < 254) {
			// good value, set filter counter to 0 and return the data
			sideUSFilterCount = 0;
			return data;
		} else if (sideUSFilterCount < FILTER_MAX) {
			// could be noise, so increment the counter and try getting a new
			// value
			sideUSFilterCount++;
			//wait to be sure we have a new value
			try {
				Thread.sleep(FLITER_SLEEP);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			return getSideDist();

		} else {
			// got 254 multiple times, so its the true value
			return 254;
		}

	}

	/**
	 * 
	 * @return data value measured by center light sensor
	 */
	public float getCenterColourValue() {

		centerLSValue.fetchSample(centerLSData, 0);
		float data = centerLSData[0];

		return data;

	}

	/**
	 * 
	 * @return data value measured by side light sensor
	 */
	public float getSideColourValue() {

		sideLSValue.fetchSample(sideLSData, 0);
		float data = sideLSData[0];

		return data;

	}
	
	/**
	 * 
	 * @return measured distance to ball or 254 
	 */
	public float getBallDist(){
		ballUSValue.fetchSample(ballUSData, 0);
		float data = ballUSData[0];
		// convert to cm
		data *= 100;

		if (data < 254) {
			// good value, set filter counter to 0 and return the data
			ballUSFilterCount = 0;
			return data;
		} else if (sideUSFilterCount < FILTER_MAX) {
			// could be noise, so increment the counter and try getting a new
			// value
			ballUSFilterCount++;
			return getBallDist();

		} else {
			// got 254 multiple times, so its the true value
			return 254;
		}
		
	}
	
	/**
	 * 
	 * @return centered light sensor sample provider
	 */
	public SampleProvider getCenterLSSampleProvider(){
		return centerLSValue;
	}
	
	/**
	 * 
	 * @return side light sensor sample provider
	 */
	public SampleProvider getSideLSSampleProvider(){
		return sideLSValue;
	}
	//TODO complete this based on how we decide to detect ball colour
	/**
	 * 
	 * @return color measured by light sensor
	 */
	public float getBallColourID(){
		return 0;
	}
	
	/**
	 * 
	 * @return float
	 * <p>
	 * returns the color value of the object detected
	 */
	public float getBallColourValue(){
		ballColourIDValue.fetchSample(ballColourIDData, 0);
		return ballColourIDData[0];
	}
	
	
	
	
}

package soccer;

import java.util.concurrent.Callable;
/**
 * 
 * @author Wole Obayomi Jr
 *
 */
public class FrontUSTest {
	
	private static DataLogger dl;
	private static Sensors sensors;
	private static Navigation nav;
	public static Callable<Float>getData = new Callable<Float>() {

		@Override
		public Float call() throws Exception {
			// TODO Auto-generated method stub
			return getUSData();
		}
		
	};
	
	/**
	 * 
	 * @param dl
	 * @param sensors
	 * @param nav
	 */
	public FrontUSTest(DataLogger dl, Sensors sensors, Navigation nav) {
		this.dl = dl;
		this.sensors = sensors;
		this.nav = nav;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * <p>
	 * navigate robot to predefined coordinates
	 */
	public void travelToTile(int x, int y) {
		nav.travelTo(x, y, true, false);
	}
	
	/**
	 * 
	 * @return float
	 * <p>
	 * returns the distance measured using the front-facing ultrasonic sensor
	 */
	private static float getUSData() {
		return sensors.getFrontDist();
	}
}

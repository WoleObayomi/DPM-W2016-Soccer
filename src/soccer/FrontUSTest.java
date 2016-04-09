package soccer;

import java.util.concurrent.Callable;

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
	
	public FrontUSTest(DataLogger dl, Sensors sensors, Navigation nav) {
		this.dl = dl;
		this.sensors = sensors;
		this.nav = nav;
	}
	
	public void travelToTile(int x, int y) {
		nav.travelTo(x, y, true, false);
	}
	
	private static float getUSData() {
		return sensors.getFrontDist();
	}
}

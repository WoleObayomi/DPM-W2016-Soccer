package soccer;

/**
 * 
 * @author Peter Quinn
 *
 */
public class PlannerOffense {

	private Odometer odometer;
	private Navigation nav;
	private Sensors sensors;
	private Motors motors;
	private int BC;
	private int[] ballZone;
	private int attackLine;

	private int xTileMax = 11;
	private int yTileMax = 11;

	/**
	 * 
	 * @param odometer
	 * @param nav
	 * @param sensors
	 * @param motors
	 */
	public PlannerOffense(Odometer odometer, Navigation nav, Sensors sensors, Motors motors, int attackLine, int BC,
			int[] ballZone) {
		this.odometer = odometer;
		this.nav = nav;
		this.sensors = sensors;
		this.motors = motors;
		this.BC = BC;
		this.ballZone = ballZone; // {llX,llY,urX,urY}
		this.attackLine = attackLine;
	}

	// have the robot execute its offense procedure
	public void run() {

		// move around outside to get beside attack zone
		double attackLineY = attackLine * PhysicalConstants.TILE_SPACING;
		if (odometer.getY() > attackLineY) {// above attacker line, move down
											// and past
			nav.movePastY(attackLineY, true, true);

		} else {// start below zone, move up to just before the line
			nav.movePastY(attackLineY - PhysicalConstants.TILE_SPACING, true, true);
		}

		// move to middle of atttack zone
		double centerX = xTileMax / 2 * PhysicalConstants.TILE_SPACING;
		nav.travelTo(centerX, attackLineY - PhysicalConstants.TILE_SPACING / 2, true, true);

		//go get balls
		
		//shoot on net
	}

}

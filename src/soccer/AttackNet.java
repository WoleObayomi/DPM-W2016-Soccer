/*
 * Title: AttackNet
 * Author: Peter Quinn
 * 
 * Date: April 12, 2016
 * 
 * Desc: Moves the robot to the attack zone, aims for the middle of the net and shoots 
 * the balls
 * 
 * Edit Hist:
 */

package soccer;

import javax.swing.plaf.basic.BasicTreeUI.TreeTraverseAction;

/**
 * 
 * @author Peter Quinn
 *
 */
public class AttackNet {

	private LauncherController launcher;
	private Navigation nav;
	private double attackZoneX;
	private double attackZoneY;
	private Sensors sensors;
	private Motors motors;
	private Odometer odometer;

	// constants
	double netX = 5 * PhysicalConstants.TILE_SPACING;
	double netY = 10 * PhysicalConstants.TILE_SPACING;

	public void run() {

		// get back to attack zone from picking up balls
		nav.travelTo(attackZoneX, attackZoneY, true, false);

		while (true) {
			// face the net
			nav.face(netX, netY);

			// check if there there is an obstacle between us and the net we can
			// see
			motors.getUSMotor().rotateTo(0);
			if (sensors.getFrontDist() > 250) {
				break; // we have a clear path,
			} else {
				if (odometer.getX() < 7 * PhysicalConstants.TILE_SPACING) {
					nav.travelTo(odometer.getX() + PhysicalConstants.TILE_SPACING, odometer.getY(), false, false);
				} else {
					nav.travelTo(3 * PhysicalConstants.TILE_SPACING, odometer.getY(), false, false);
				}
			}

		}

		// aiming on a clear path
		// shoot the ball
		for (int i = 0; i < 4; i++) {
			launcher.setToFiringSpeed();
			launcher.conveyerForwardOneBall();
			launcher.raiseAngle();
			launcher.lowerAngle();
		}
		launcher.stopLauncher();

	}

	/**
	 * 
	 * @param launcher
	 * @param nav
	 * @param attackZoneX
	 * @param attackZoneY
	 * @param sensors
	 * @param motors
	 * @param odometer
	 */
	public AttackNet(LauncherController launcher, Navigation nav, double attackZoneX, double attackZoneY,
			Sensors sensors, Motors motors, Odometer odometer) {
		super();
		this.launcher = launcher;
		this.nav = nav;
		this.attackZoneX = attackZoneX;
		this.attackZoneY = attackZoneY;
		this.sensors = sensors;
		this.motors = motors;
		this.odometer = odometer;
	}

}

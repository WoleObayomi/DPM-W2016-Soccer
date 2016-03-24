package soccer;

public class AngleCorrection extends Thread {

	private final int CORRECTION_PERIOD = 10; // ms

	private LineListener centerLine;
	private LineListener sideLine;

	public AngleCorrection() {

	}

	@Override
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();

			// this ensure the correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done
				}
			}
		}
	}

}

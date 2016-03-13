/* Author: Peter Quinn (260689207) Rony Azrak (260606812)
 * Group: 47
 * 
 * Date: Feb 15,2016
 * 
 * Description: Thread that writes US sensor data to a csv file which can be taken and analyzed later. 
 * On as soon as the thread starts, can be turned on or off by using setLoggerState method
 * 
 */

package soccer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

import javax.swing.text.WrappedPlainView;

import lejos.robotics.SampleProvider;

public class DataLogger extends Thread {

	private final int SLEEP_TIME = 100;
	private boolean logging = true;
	Callable<Float> funcToLogDataFrom;
	String filename;

	public void setLoggerState(boolean state) {
		logging = state;
	}

	public DataLogger(String filename, Callable<Float> funcToCall) {

		// call this function by passing a String and
		// and a Callable<Float> which has the syntax:
		//
		// new Callable<Float>() {
		// public Float call() {
		// return methodToPass();
		// }};

		// call the method you passed by doing funcToCall.call();

		this.filename = filename;
		this.funcToLogDataFrom = funcToCall;
	}

	@Override
	public void run() {

		File file = new File(filename);

		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

			if (!file.exists()) {
				file.createNewFile();
			}

			boolean first = true;

			while (logging) {

				if (!first) {
					writer.write(',');
				}
				first = false;

				float distance = funcToLogDataFrom.call();
				writer.write(Float.toString((distance)));
				sleep(SLEEP_TIME);
			}
			writer.close();
			return;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

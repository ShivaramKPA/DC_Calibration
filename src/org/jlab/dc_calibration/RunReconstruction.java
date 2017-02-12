/*  +__^_________,_________,_____,________^-.-------------------,
 *  | |||||||||   `--------'     |          |                   O
 *  `+-------------USMC----------^----------|___________________|
 *    `\_,---------,---------,--------------'
 *      / X MK X /'|       /'
 *     / X MK X /  `\    /'
 *    / X MK X /`-------'
 *   / X MK X /
 *  / X MK X /
 * (________(                @author m.c.kunkel
 *  `------'
*/
package org.jlab.dc_calibration;

import java.io.IOException;
import java.util.Map;

public class RunReconstruction {
	public static void main(String[] args) throws IOException, InterruptedException {

		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			System.out.format("%s=%s%n", envName, env.get(envName));
		}

		// File inFile = new File("/Volumes/Mac_Storage/Work_Codes/CLAS12/WORKSHOP/gemcOut.evio");
		// Runtime r = Runtime.getRuntime();
		// Process p = r.exec("$COATJAVA/bin/gemc-evio " + inFile + " 11 -1.0 1.0");
		// p.waitFor();
		// BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		// String line = "";
		//
		// while ((line = b.readLine()) != null) {
		// System.out.println(line);
		// }
		//
		// b.close();
	}
}

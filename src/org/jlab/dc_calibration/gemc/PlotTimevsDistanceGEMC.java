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
package org.jlab.dc_calibration.gemc;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;

public class PlotTimevsDistanceGEMC {

	public static void main(String[] args) {

		EvioSource reader = new EvioSource();
		reader.open("/Users/Mike/output_calcTime.ev");
		while (reader.hasEvent()) {
			EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
			event.show(); // print out all banks in the event
			if (event.hasBank("GenPart::true")) {
				EvioDataBank dcBank = (EvioDataBank) event.getBank("GenPart::true");
				dcBank.show(); // printout the content of the bank
			}
		}
	}
}

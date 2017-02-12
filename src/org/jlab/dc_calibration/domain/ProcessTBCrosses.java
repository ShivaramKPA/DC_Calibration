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
package org.jlab.dc_calibration.domain;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

public class ProcessTBCrosses extends DCTBValid {

	private EvioDataBank bnkCrs;
	private EvioDataEvent event;
	private int nCrs;

	public ProcessTBCrosses(EvioDataEvent event) {
		this.event = event;
		init();
	}

	public int getNCrs() {
		return nCrs;
	}

	private void init() {
		if (this.isValid()) {
			this.bnkCrs = (EvioDataBank) event.getBank("TimeBasedTrkg::TBCrosses");
			setNtrks();
		} else
			this.nCrs = 0;
	}

	private void setNtrks() {
		this.nCrs = bnkCrs.rows();

	}

	@Override
	protected boolean isValid() {
		return event.hasBank("TimeBasedTrkg::TBCrosses") ? true : false;
	}

}

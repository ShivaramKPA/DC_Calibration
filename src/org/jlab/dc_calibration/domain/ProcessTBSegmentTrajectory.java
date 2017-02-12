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

public class ProcessTBSegmentTrajectory extends DCTBValid {
	private EvioDataEvent event;
	private EvioDataBank bnkSegs;
	private int nSegs;

	public ProcessTBSegmentTrajectory(EvioDataEvent event) {
		this.event = event;
		init();
	}

	public int getNsegs() {
		return nSegs;
	}

	private void init() {
		if (this.isValid()) {
			this.bnkSegs = (EvioDataBank) event.getBank("TimeBasedTrkg::TBSegmentsTrajectory");
			setNTrks();
		} else
			this.nSegs = 0;
	}

	private void setNTrks() {
		this.nSegs = bnkSegs.rows();

	}

	@Override
	protected boolean isValid() {
		return event.hasBank("TimeBasedTrkg::TBSegmentsTrajectory") ? true : false;

	}
}

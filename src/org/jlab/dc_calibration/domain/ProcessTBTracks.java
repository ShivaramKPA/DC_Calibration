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
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

public class ProcessTBTracks extends DCTBValid implements iProcessable {

	private DataBank bnkTrks;
	private DataEvent event;
	private int nTrks;

	public ProcessTBTracks(DataEvent event) {
		this.event = event;
		init();
	}

	public int getNTrks() {
		return nTrks;
	}

	private void init() {
		if (this.isValid()) {
			this.bnkTrks = (DataBank) event.getBank("TimeBasedTrkg::TBTracks");
			setNtrks();
		} else
			this.nTrks = 0;
	}

	private void setNtrks() {
		this.nTrks = bnkTrks.rows();

	}

	@Override
	protected boolean isValid() {
		return event.hasBank("TimeBasedTrkg::TBTracks") ? true : false;
	}

	public void processEvent(DataEvent event) {
		// TODO Auto-generated method stub

	}

}

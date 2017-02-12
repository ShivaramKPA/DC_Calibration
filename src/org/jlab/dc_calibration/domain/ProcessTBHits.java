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

import static org.jlab.dc_calibration.domain.Histograms.hArrWire;
import static org.jlab.dc_calibration.domain.Histograms.htrkDoca;

import java.util.HashMap;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

public class ProcessTBHits extends DCTBValid {

	private EvioDataBank bnkHits;
	private EvioDataEvent event;

	private int nRows;

	private HashMap<Integer, Integer> layerMapTBHits;
	private HashMap<Integer, Integer> wireMapTBHits;
	private HashMap<Integer, Double> timeMapTBHits;
	private HashMap<Integer, Double> trkDocaMapTBHits;

	public ProcessTBHits(EvioDataEvent event) {
		this.event = event;
		init();
	}

	private void init() {
		if (this.isValid()) {
			this.bnkHits = (EvioDataBank) event.getBank("TimeBasedTrkg::TBHits");
			initMaps();
			setNrows();

		} else
			this.nRows = 0;
	}

	private void initMaps() {
		this.layerMapTBHits = new HashMap<Integer, Integer>();
		this.wireMapTBHits = new HashMap<Integer, Integer>();
		this.timeMapTBHits = new HashMap<Integer, Double>();
		this.trkDocaMapTBHits = new HashMap<Integer, Double>();
	}

	private void setNrows() {
		this.nRows = bnkHits.rows();

	}

	public void clearMaps() {
		this.layerMapTBHits.clear();
		this.wireMapTBHits.clear();
		this.timeMapTBHits.clear();
		this.trkDocaMapTBHits.clear();
	}

	protected HashMap<Integer, Integer> getLayerMapTBHits() {
		return layerMapTBHits;
	}

	protected HashMap<Integer, Integer> getWireMapTBHits() {
		return wireMapTBHits;
	}

	protected HashMap<Integer, Double> getTimeMapTBHits() {
		return timeMapTBHits;
	}

	protected HashMap<Integer, Double> getTrkDocaMapTBHits() {
		return trkDocaMapTBHits;
	}

	protected int getNrows() {
		return nRows;
	}

	protected void processTBhits() {

		for (int j = 0; j < this.nRows; j++) {
			layerMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("layer", j));
			wireMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("wire", j));
			timeMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getDouble("time", j));
			trkDocaMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getDouble("trkDoca", j));
			int docaBin = (int) ((bnkHits.getDouble("trkDoca", j) + (0.8)) / 0.2);
			if (docaBin > -1 && docaBin < 8) {
				hArrWire.get(new Coordinate(bnkHits.getInt("superlayer", j) - 1, bnkHits.getInt("layer", j) - 1, docaBin))
				        .fill(bnkHits.getInt("wire", j));
			}
			htrkDoca.get(new Coordinate(bnkHits.getInt("sector", j) - 1, bnkHits.getInt("superlayer", j) - 1))
			        .fill(bnkHits.getDouble("trkDoca", j));
		}
	}

	@Override
	protected boolean isValid() {
		return event.hasBank("TimeBasedTrkg::TBHits") ? true : false;

	}

}

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

import static org.jlab.dc_calibration.domain.Constants.nTh;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.rad2deg;
import static org.jlab.dc_calibration.domain.Constants.thBins;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;
import static org.jlab.dc_calibration.domain.Constants.wpdist;
import static org.jlab.dc_calibration.domain.Histograms.h1ThSL;
import static org.jlab.dc_calibration.domain.Histograms.h1timeSlTh;
import static org.jlab.dc_calibration.domain.Histograms.h2timeVtrkDocaVZ;

import java.util.HashMap;
import java.util.Map;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

public class ProcessTBSegments extends DCTBValid {

	private EvioDataBank bnkSegs;
	private int nRows;
	private EvioDataEvent event;

	private Map<Integer, Integer> gSegmThBinMapTBSegments;
	private Map<Integer, Double> gSegmAvgWireTBSegments;
	private Map<Integer, Double> gFitChisqProbTBSegments;
	private HashMap<Integer, Double> timeMapTBHits;
	private HashMap<Integer, Double> trkDocaMapTBHits;

	ProcessTBHits tbHits;

	public ProcessTBSegments(EvioDataEvent event) {
		this.event = event;
		init();
	}

	private void init() {
		if (this.isValid()) {
			this.bnkSegs = (EvioDataBank) event.getBank("TimeBasedTrkg::TBSegments");
			initMaps();
			setNrows();
		} else
			this.nRows = 0;
	}

	private void initMaps() {
		this.gSegmThBinMapTBSegments = new HashMap<Integer, Integer>();
		this.gSegmAvgWireTBSegments = new HashMap<Integer, Double>();
		this.gFitChisqProbTBSegments = new HashMap<Integer, Double>();
	}

	private void setNrows() {
		this.nRows = bnkSegs.rows();
	}

	public void clearMaps() {
		this.gSegmThBinMapTBSegments.clear();
		this.gSegmAvgWireTBSegments.clear();
		this.gFitChisqProbTBSegments.clear();
	}

	public void setTBhits(ProcessTBHits tbHits) {
		this.tbHits = tbHits;
	}

	protected Map<Integer, Integer> getgSegmThBinMapTBSegments() {
		return gSegmThBinMapTBSegments;
	}

	protected Map<Integer, Double> getgSegmAvgWireTBSegments() {
		return gSegmAvgWireTBSegments;
	}

	protected Map<Integer, Double> getgFitChisqProbTBSegments() {
		return gFitChisqProbTBSegments;
	}

	protected int getNrows() {
		return nRows;
	}

	public void processEvent() {
		gSegmThBinMapTBSegments = new HashMap<Integer, Integer>();
		gSegmAvgWireTBSegments = new HashMap<Integer, Double>();
		gFitChisqProbTBSegments = new HashMap<Integer, Double>();
		bnkSegs = (EvioDataBank) event.getBank("TimeBasedTrkg::TBSegments");

		int nHitsInSeg = 0;
		for (int j = 0; j < this.nRows; j++) {
			int superlayer = bnkSegs.getInt("superlayer", j);
			int sector = bnkSegs.getInt("sector", j);
			gSegmAvgWireTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("avgWire", j));
			gFitChisqProbTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("fitChisqProb", j));

			double thDeg = rad2deg * Math.atan2(bnkSegs.getDouble("fitSlope", j), 1.0);
			h1ThSL.get(new Coordinate(bnkSegs.getInt("superlayer", j) - 1)).fill(thDeg);
			for (int h = 1; h <= 12; h++) {
				if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1)
					nHitsInSeg++;
			}
			int thBn = -1;
			int thBnVz = -1;
			for (int th = 0; th < nTh; th++) {
				if (thDeg > thBins[th] && thDeg <= thBins[th + 1])
					thBn = th;
			}
			for (int th = 0; th < nThBinsVz; th++) {
				if (thDeg > thEdgeVzL[th] && thDeg <= thEdgeVzH[th])
					thBnVz = th;
			}
			gSegmThBinMapTBSegments.put(bnkSegs.getInt("ID", j), thBn);
			double docaMax = 2.0 * wpdist[superlayer - 1];
			for (int h = 1; h <= 12; h++) {
				if (nHitsInSeg > 5)// Saving only those with more than 5 hits
				{
					Double gTime = this.timeMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
					Double gTrkDoca = this.trkDocaMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
					if (gTime == null || gTrkDoca == null)
						continue;
					if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBn > -1 && thBn < nTh)
						h1timeSlTh.get(new Coordinate(superlayer - 1, thBn)).fill(gTime);
					if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz > -1 && thBnVz < nThBinsVz) {// && thBnVz < nThBinsVz
						double docaNorm = gTrkDoca / docaMax;
						h2timeVtrkDocaVZ.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(Math.abs(docaNorm), gTime);
					}
				}
			}
		}

	}

	protected void processTBSegments(ProcessTBHits tbHits) {
		this.timeMapTBHits = tbHits.getTimeMapTBHits();
		this.trkDocaMapTBHits = tbHits.getTrkDocaMapTBHits();
		processEvent();
	}

	protected void processTBSegments() {
		processTBSegments(this.tbHits);
	}

	@Override
	protected boolean isValid() {
		return event.hasBank("TimeBasedTrkg::TBSegments") ? true : false;
	}
}

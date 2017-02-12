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

import static org.jlab.dc_calibration.domain.Constants.nHists;
import static org.jlab.dc_calibration.domain.Constants.nLayer;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nSectors;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.parName;
import static org.jlab.dc_calibration.domain.Constants.prevFitPars;
import static org.jlab.dc_calibration.domain.Histograms.h2timeVtrkDocaVZ;
//import static org.jlab.dc_calibration.domain.Histograms.*;
//import static org.jlab.dc_calibration.domain.Constants.*;
import static org.jlab.dc_calibration.domain.Histograms.hArrWire;
import static org.jlab.dc_calibration.domain.Histograms.htrkDoca;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnStrategy;
import org.freehep.math.minuit.MnUserParameters;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.evio.EvioDataEvent;

public class TestFile implements ActionListener, Runnable {

	private ArrayList<EmbeddedCanvas> wires;
	private ArrayList<EmbeddedCanvas> trkDocas;
	private Map<Coordinate, EmbeddedCanvas> trkDocasvsTime;
	private Map<Coordinate, EmbeddedCanvas> trkDocasvsTimeProfiles;

	private ArrayList<String> fileArray;

	private Map<Coordinate, GraphErrors> htime2DisDocaProfile;
	private Map<Coordinate, DCFitFunction> mapOfFitFunctions;
	private Map<Coordinate, MnUserParameters> mapOfFitParameters;
	private Map<Coordinate, double[]> mapOfUserFitParameters;
	private Map<Coordinate, DCFitDrawer> mapOfFitLines;

	private ProcessTBHits tbHits;
	private ProcessTBTracks tbTracks;
	private ProcessTBSegments tbSegments;
	private ProcessTBCrosses tbCrs;

	private boolean acceptorder = false;
	private boolean isLinearFit;

	private EvioDataChain reader;
	private OrderOfAction OAInstance;
	private DCTabbedPane dcTabbedPane;

	private InitializeHistograms initializeHistograms;

	public TestFile(ArrayList<String> files, boolean isLinearFit) {
		this.fileArray = files;
		this.isLinearFit = isLinearFit;

		this.reader = new EvioDataChain();
		addToReader();
		this.initializeHistograms = new InitializeHistograms();
		initializeHistograms.initTBhists();
		initializeHistograms.initTBSegments();
		initMaps();
		this.dcTabbedPane = new DCTabbedPane("PooperDooper");

	}

	public TestFile(OrderOfAction OAInstance, ArrayList<String> files, boolean isLinearFit) {
		this.OAInstance = OAInstance;
		this.fileArray = files;
		this.isLinearFit = isLinearFit;

		this.reader = new EvioDataChain();
		addToReader();
		this.initializeHistograms = new InitializeHistograms();
		initializeHistograms.initTBhists();
		initializeHistograms.initTBSegments();
		initMaps();
		this.dcTabbedPane = new DCTabbedPane("PooperDooper");
	}

	private void initMaps() {
		this.htime2DisDocaProfile = new HashMap<Coordinate, GraphErrors>();
		this.mapOfFitFunctions = new HashMap<Coordinate, DCFitFunction>();
		this.mapOfFitParameters = new HashMap<Coordinate, MnUserParameters>();
		this.mapOfUserFitParameters = new HashMap<Coordinate, double[]>();
		this.mapOfFitLines = new HashMap<Coordinate, DCFitDrawer>();

	}

	private void createCanvas() {
		wires = new ArrayList<EmbeddedCanvas>();
		trkDocas = new ArrayList<EmbeddedCanvas>();
		trkDocasvsTime = new HashMap<Coordinate, EmbeddedCanvas>();
		trkDocasvsTimeProfiles = new HashMap<Coordinate, EmbeddedCanvas>();
		for (int i = 0; i < nSectors; i++) {
			wires.add(new EmbeddedCanvas());
			wires.get(i).setSize(4 * 400, 6 * 400);
			wires.get(i).divide(6, 6);
			trkDocas.add(new EmbeddedCanvas());
			trkDocas.get(i).setSize(4 * 400, 6 * 400);
			trkDocas.get(i).divide(3, 2);
			for (int j = 0; j < nSL; j++) {
				trkDocasvsTime.put(new Coordinate(i, j), new EmbeddedCanvas());
				trkDocasvsTime.get(new Coordinate(i, j)).setSize(4 * 400, 6 * 400);
				trkDocasvsTime.get(new Coordinate(i, j)).divide(2, 6);
				trkDocasvsTimeProfiles.put(new Coordinate(i, j), new EmbeddedCanvas());
				trkDocasvsTimeProfiles.get(new Coordinate(i, j)).setSize(4 * 400, 6 * 400);
				trkDocasvsTimeProfiles.get(new Coordinate(i, j)).divide(2, 6);
			}
		}
	}

	private void addToReader() {

		for (String str : fileArray) {
			this.reader.addFile(str);
		}
		this.reader.open();

	}

	private void createFitLines() {
		for (int i = 0; i < nSectors; i++) {
			for (int j = 0; j < nSL; j++) {
				for (int k = 0; k < nThBinsVz; k++) {
					String title = "Sector " + i + " timeVtrkDocaS " + j + " Th" + k;
					mapOfFitLines.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k, isLinearFit));
					mapOfFitLines.get(new Coordinate(i, j, k)).setLineColor(2);
					mapOfFitLines.get(new Coordinate(i, j, k)).setLineWidth(3);
					mapOfFitLines.get(new Coordinate(i, j, k)).setLineStyle(4);
					mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j, k)));
				}
			}
		}

	}

	private boolean isValid(EvioDataEvent event) {
		DCTBValid HtsVal = new ProcessTBHits(event);
		DCTBValid TrksVal = new ProcessTBTracks(event);
		DCTBValid SegsVal = new ProcessTBSegments(event);
		DCTBValid CrsVal = new ProcessTBCrosses(event);
		this.tbHits = new ProcessTBHits(event);
		this.tbTracks = new ProcessTBTracks(event);
		this.tbSegments = new ProcessTBSegments(event);
		this.tbCrs = new ProcessTBCrosses(event);
		return (HtsVal.isValid() && TrksVal.isValid() && SegsVal.isValid() && CrsVal.isValid()) ? true : false;
	}

	protected void processData() {
		int icounter = 0;
		while (reader.hasEvent()) {// && icounter < 100
			icounter++;
			if (icounter % 200 == 0) {
				System.out.println("Processed " + icounter + " events.");
			}
			EvioDataEvent event = reader.getNextEvent();
			if (this.isValid(event)) {// tbTracks.getNTrks() > 0 &&
				// System.out.println(tbCrs.getNCrs() + " " + tbTracks.getNTrks() + " " + tbHits.getNrows() + " " + tbSegments.getNrows());
				tbHits.processTBhits();
				tbSegments.processTBSegments(tbHits);
				tbHits.clearMaps();
				tbSegments.clearMaps();
			}
		}
	}

	protected void drawHistograms() {
		createCanvas();
		int canvasPlace;
		for (int i = 0; i < nSL; i++) {
			canvasPlace = 0;
			for (int j = 0; j < nLayer; j++) {
				for (int k = 0; k < nHists; k++) {
					wires.get(i).cd(canvasPlace);
					wires.get(i).draw(hArrWire.get(new Coordinate(i, j, k)));
					canvasPlace++;
				}
			}
			wires.get(i).save("src/images/wires" + (i + 1) + ".png");
		}
		for (int i = 0; i < nSectors; i++) {
			canvasPlace = 0;
			for (int j = 0; j < nSL; j++) {
				trkDocas.get(i).cd(canvasPlace);
				trkDocas.get(i).draw(htrkDoca.get(new Coordinate(i, j)));
				canvasPlace++;
			}
			trkDocas.get(i).save("src/images/trkDocas" + (i + 1) + ".png");
		}

		for (int i = 0; i < nSectors; i++) {
			for (int j = 0; j < nSL; j++) {
				for (int k = 0; k < nThBinsVz; k++) {
					htime2DisDocaProfile.put(new Coordinate(i, j, k), h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX());
					htime2DisDocaProfile.get(new Coordinate(i, j, k)).setTitle("Sector " + i + " timeVtrkDocaS " + j + " Th" + k);
				}
			}
		}
		// Lets Run the Fitter
		runFitter();
		// Done running fitter
		// lets create lines we just fit
		createFitLines();
		for (int i = 0; i < nSectors; i++) {
			for (int j = 0; j < nSL; j++) {
				for (int k = 0; k < nThBinsVz; k++) {
					trkDocasvsTime.get(new Coordinate(i, j)).cd(k);
					trkDocasvsTime.get(new Coordinate(i, j)).draw(h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)));
					// trkDocasvsTime.get(new Coordinate(i, j)).draw(mapOfFitLines.get(new Coordinate(i, j, k)), "same");

					trkDocasvsTimeProfiles.get(new Coordinate(i, j)).cd(k);
					trkDocasvsTimeProfiles.get(new Coordinate(i, j)).draw(h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX());
					// trkDocasvsTimeProfiles.get(new Coordinate(i, j)).draw(mapOfFitLines.get(new Coordinate(i, j, k)), "same");
				}

				trkDocasvsTime.get(new Coordinate(i, j)).save("src/images/timeVtrkDocaSector" + (i + 1) + "SuperLayer" + (j + 1) + ".png");
				addToPane("timeVtrkDocaSector" + (i + 1) + "SuperLayer" + (j + 1), trkDocasvsTime.get(new Coordinate(i, j)));
				trkDocasvsTimeProfiles.get(new Coordinate(i, j))
				        .save("src/images/timeVtrkDocaProfilesSector" + (i + 1) + "SuperLayer" + (j + 1) + ".png");
				addToPane("timeVtrkDocaProfilesSector" + (i + 1) + "SuperLayer" + (j + 1),
				        trkDocasvsTimeProfiles.get(new Coordinate(i, j)));
			}
		}
		showFrame();
	}

	protected void addToPane(String tabName, EmbeddedCanvas can) {
		dcTabbedPane.addCanvasToPane(tabName, can);
	}

	public void runFitter() {
		final int nFreePars = 4;
		// initial guess of tMax for the 6 superlayers (cell sizes are different for each)
		// This is one of the free parameters (par[2], but fixed for now.)
		double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 525.0, 550.0 };
		// Now start minimization
		double parSteps[] = { 0.00001, 0.001, 0.01, 0.01, 0.0001 };
		double pLow[] = { prevFitPars[0] * 0.4, prevFitPars[1] * 0.0, prevFitPars[2] * 0.4, prevFitPars[3] * 0.4, prevFitPars[4] * 0.0 };
		double pHigh[] = { prevFitPars[0] * 1.6, prevFitPars[1] * 5.0, prevFitPars[2] * 1.6, prevFitPars[3] * 1.6, prevFitPars[4] * 1.6 };
		Map<Coordinate, MnUserParameters> mapTmpUserFitParameters = new HashMap<Coordinate, MnUserParameters>();
		for (int i = 0; i < nSectors; i++) {
			for (int j = 0; j < nSL; j++) {
				for (int k = 0; k < nThBinsVz; k++) {
					mapOfFitFunctions.put(new Coordinate(i, j, k),
					        new DCFitFunction(h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX(), j, k, isLinearFit));
					mapOfFitParameters.put(new Coordinate(i, j, k), new MnUserParameters());
					for (int p = 0; p < nFreePars; p++) {
						mapOfFitParameters.get(new Coordinate(i, j, k)).add(parName[p], prevFitPars[p], parSteps[p], pLow[p], pHigh[p]);
					}
					mapOfFitParameters.get(new Coordinate(i, j, k)).setValue(2, tMaxSL[j]);// tMax for SLth superlayer
					mapOfFitParameters.get(new Coordinate(i, j, k)).fix(2);
					MnMigrad migrad =
					        new MnMigrad(mapOfFitFunctions.get(new Coordinate(i, j, k)), mapOfFitParameters.get(new Coordinate(i, j, k)));
					FunctionMinimum min = migrad.minimize();

					if (!min.isValid()) {
						// try with higher strategy
						System.out.println("FM is invalid, try with strategy = 2.");
						MnMigrad migrad2 = new MnMigrad(mapOfFitFunctions.get(new Coordinate(i, j, k)), min.userState(), new MnStrategy(2));
						min = migrad2.minimize();
					}
					mapTmpUserFitParameters.put(new Coordinate(i, j, k), min.userParameters());
					double[] fPars = new double[nFreePars];
					double[] fErrs = new double[nFreePars];
					for (int p = 0; p < nFreePars; p++) {
						fPars[p] = mapTmpUserFitParameters.get(new Coordinate(i, j, k)).value(parName[p]);
						fErrs[p] = mapTmpUserFitParameters.get(new Coordinate(i, j, k)).error(parName[p]);
					}
					mapOfUserFitParameters.put(new Coordinate(i, j, k), fPars);
				} // end of nThBinsVz loop
			} // end of superlayer loop
		} // end of sector loop

	}

	public void showFrame() {
		dcTabbedPane.showFrame();
	}

	public void actionPerformed(ActionEvent e) {
		OAInstance.buttonstatus(e);
		acceptorder = OAInstance.isorderOk();
		JFrame frame = new JFrame("JOptionPane showMessageDialog example1");
		if (acceptorder) {
			JOptionPane.showMessageDialog(frame, "Click OK to start processing the time to distance fitting...");
			processData();
			drawHistograms();
			// DCTabbedPane test = new DCTabbedPane();
		} else
			System.out.println("I am red and it is not my turn now ;( ");
	}

	@Override
	public void run() {
		processData();
		drawHistograms();
	}

	public static void main(String[] args) {

		ArrayList<String> fileArray = new ArrayList<String>();

		fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_1.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_10.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_2.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_4.evio");

		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_1.evio");
		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_10.evio");
		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_2.evio");
		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_4.evio");

		TestFile rd = new TestFile(fileArray, true);
		rd.processData();
		rd.drawHistograms();

	}
}

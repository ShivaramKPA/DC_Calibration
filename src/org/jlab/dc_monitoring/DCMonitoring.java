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
 *  `------'				 @author Olga Cortes
*/
package org.jlab.dc_monitoring;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jlab.clas.physics.Vector3;
import org.jlab.dc_calibration.domain.Coordinate;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataChain;

public class DCMonitoring {

	private Dimension screensize = null;
	private JFrame frame = null;
	private JTabbedPane tabbedPane = null;

	private Map<Coordinate, H2F> occupanciesByCoordinate = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> trkdocasvstime = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> cross_yvsx = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> cross_uyvsux = new HashMap<Coordinate, H2F>();
	private H2F thetaVSmomenutm = thetaVSmomenutm = new H2F("theta vs momentum", "", 100, 0.0, 10.0, 150, 0, 2);
	private H2F phiVSmomenutm = new H2F("phi vs momentum", "", 100, 0.0, 10.0, 150, -Math.PI, Math.PI);
	private H2F thetaVSphi = new H2F("theta vs phi", "", 150, -Math.PI, Math.PI, 150, 0, 2);

	private EmbeddedCanvas can1 = null;
	private EmbeddedCanvas can2 = null;
	private EmbeddedCanvas can3 = null;
	private EmbeddedCanvas can4 = null;
	private EmbeddedCanvas can5 = null;
	private EmbeddedCanvas can6 = null;

	private EvioDataChain reader = null;

	public DCMonitoring(String fileName) {
		this.reader = new EvioDataChain();
		reader.addFile(fileName);
		init();
		processEvent();
		drawPlots();
		addCanvasToPane();
	}

	public DCMonitoring(EvioDataChain reader) {
		this.reader = reader;
		init();
		processEvent();
		drawPlots();
		addCanvasToPane();
	}

	private void setScreenSize() {
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
	}

	private void setJFrame() {
		frame = new JFrame("DC MONITORING");
		frame.setSize((int) (screensize.getHeight() * .75 * 1.618), (int) (screensize.getHeight() * .75));
	}

	private void setJTabbedPane() {
		tabbedPane = new JTabbedPane();
	}

	private void createHistograms() {
		for (int i = 0; i < 6; i++) {
			cross_yvsx.put(new Coordinate(i), new H2F("y vs x sector" + (i + 1), "", 180, -180, 180, 120, -140, 140));
			cross_yvsx.get(new Coordinate(i)).setTitleX("x");
			cross_yvsx.get(new Coordinate(i)).setTitleY("y");
			cross_uyvsux.put(new Coordinate(i), new H2F("Uy vs Ux sector" + (i + 1), "", 100, -1, 1, 100, -1, 1));
			cross_uyvsux.get(new Coordinate(i)).setTitleY("Ux");
			cross_uyvsux.get(new Coordinate(i)).setTitleY("Uy");
			for (int j = 0; j < 6; j++) {
				occupanciesByCoordinate.put(new Coordinate(i, j), new H2F("Occupancy" + i, "", 112, 1, 113, 6, 1, 7));
				occupanciesByCoordinate.get(new Coordinate(i, j)).setTitleX("Wire");
				occupanciesByCoordinate.get(new Coordinate(i, j)).setTitleY("Layer");
				if ((i * j % 6 == 0) || (i * j % 6 == 1)) {
					trkdocasvstime.put(new Coordinate(i, j), new H2F("TrackDoca vs time" + j, "", 100, 0, 200, 100, 0, 1));
				} else
					trkdocasvstime.put(new Coordinate(i, j), new H2F("TrackDoca vs time" + j, "", 100, 0, 600, 100, 0, 2));
				trkdocasvstime.get(new Coordinate(i, j)).setTitleY("time");
				trkdocasvstime.get(new Coordinate(i, j)).setTitleY("TrackDoca");
			}
		}
	}

	private void createCanvas() {
		can1 = new EmbeddedCanvas();
		can5 = new EmbeddedCanvas();
		can2 = new EmbeddedCanvas();
		can3 = new EmbeddedCanvas();
		can4 = new EmbeddedCanvas();
		can6 = new EmbeddedCanvas();
		can1.divide(6, 6);
		can3.divide(2, 3);
		can4.divide(2, 3);
		can5.divide(6, 6);
		can6.divide(2, 2);

	}

	private void init() {
		setScreenSize();
		setJFrame();
		setJTabbedPane();
		createHistograms();
		createCanvas();
		// reader = new EvioSource();
		reader.open();
	}

	private void processEvent() {
		int counter = 0;
		while (reader.hasEvent()) {
			DataEvent event = reader.getNextEvent();
			counter++;
			if (counter % 200 == 0)
				System.out.println("done " + counter + " events");
			if (event.hasBank("TimeBasedTrkg::TBHits"))
				processTBHits(event);
			if (event.hasBank("TimeBasedTrkg::TBCrosses"))
				processTBCrosses(event);
			if (event.hasBank("TimeBasedTrkg::TBTracks"))
				processTBTracks(event);

		}

	}

	private void processTBHits(DataEvent event) {
		DataBank bnkHits = event.getBank("TimeBasedTrkg::TBHits");
		for (int i = 0; i < bnkHits.rows(); i++) {
			occupanciesByCoordinate.get(new Coordinate(bnkHits.getInt("sector", i) - 1, bnkHits.getInt("superlayer", i) - 1))
			        .fill(bnkHits.getInt("wire", i), bnkHits.getInt("layer", i));
			trkdocasvstime.get(new Coordinate(bnkHits.getInt("sector", i) - 1, bnkHits.getInt("superlayer", i) - 1))
			        .fill(bnkHits.getDouble("time", i), bnkHits.getDouble("trkDoca", i));
		}

	}

	private void processTBCrosses(DataEvent event) {
		DataBank tbcrossesBank = event.getBank("TimeBasedTrkg::TBCrosses");
		for (int i = 0; i < tbcrossesBank.rows(); i++) {
			cross_uyvsux.get(new Coordinate(tbcrossesBank.getInt("sector", i) - 1)).fill(tbcrossesBank.getDouble("ux", i),
			        tbcrossesBank.getDouble("uy", i));
			cross_yvsx.get(new Coordinate(tbcrossesBank.getInt("sector", i) - 1)).fill(tbcrossesBank.getDouble("x", i),
			        tbcrossesBank.getDouble("y", i));
		}
	}

	private void processTBTracks(DataEvent event) {
		Vector3 momentum = new Vector3();
		DataBank tbtracksBank = event.getBank("TimeBasedTrkg::TBTracks");
		for (int i = 0; i < tbtracksBank.rows(); i++) {
			momentum.setXYZ(tbtracksBank.getDouble("t1_px", i), tbtracksBank.getDouble("t1_py", i), tbtracksBank.getDouble("t1_pz", i));
			thetaVSmomenutm.fill(tbtracksBank.getDouble("p", i), momentum.theta());
			phiVSmomenutm.fill(tbtracksBank.getDouble("p", i), momentum.phi());
			thetaVSphi.fill(momentum.phi(), momentum.theta());
		}
	}

	private void drawPlots() {
		int canvasPlace = 0;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				can1.cd(canvasPlace);
				can1.draw(occupanciesByCoordinate.get(new Coordinate(i, j)));
				can5.cd(canvasPlace);
				can5.draw(trkdocasvstime.get(new Coordinate(i, j)));
				canvasPlace++;
			}
		}
		for (int i = 0; i < 6; i++) {
			can3.cd(i);
			can3.draw(cross_uyvsux.get(new Coordinate(i)));
			can4.cd(i);
			can4.draw(cross_yvsx.get(new Coordinate(i)));
		}

		can6.cd(0);
		can6.draw(thetaVSmomenutm);
		can6.cd(1);
		can6.draw(phiVSmomenutm);
		can6.cd(2);
		can6.draw(thetaVSphi);
	}

	private void addCanvasToPane() {
		tabbedPane.add("Occupancies all", can1);
		tabbedPane.add("Occupancies track", can2);
		tabbedPane.add("Crosses angles", can3);
		tabbedPane.add("Crosses position", can4);
		tabbedPane.add("TrkDoca vs Time", can5);
		tabbedPane.add("Track Kin", can6);
		frame.add(tabbedPane);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// String fileName =
		// "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.evio";
		// DCMonitoring test = new DCMonitoring(fileName);

		// Uncomment to test using EvioDataChain
		EvioDataChain chain = new EvioDataChain();
		String fileName =
		        "/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/cookedFiles/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.evio";

		chain.addFile(fileName);
		// fileName = "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.2.evio";
		// chain.addFile(fileName);
		DCMonitoring test = new DCMonitoring(chain);

	}
}

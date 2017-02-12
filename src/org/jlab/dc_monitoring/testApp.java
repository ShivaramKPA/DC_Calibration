package org.jlab.dc_monitoring;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jlab.clas.physics.Vector3;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioSource;

public class testApp {
	@SuppressWarnings("null")
	public static void main(String[] args) {
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		JFrame frame = new JFrame("DC MONITORING");
		frame.setSize((int) (screensize.getHeight() * .75 * 1.618), (int) (screensize.getHeight() * .75));
		JTabbedPane tabbedPane = new JTabbedPane();

		H2F[] occupancies = new H2F[36];
		H2F[] trkdocasvstime = new H2F[36];
		H2F[] cross_yvsx = new H2F[6];
		H2F[] cross_uyvsux = new H2F[6];
		H2F thetaVSmomenutm = new H2F("theta vs momentum", "", 100, 0.0, 10.0, 150, 0, 2);
		H2F phiVSmomenutm = new H2F("phi vs momentum", "", 100, 0.0, 10.0, 150, -Math.PI, Math.PI);
		H2F thetaVSphi = new H2F("theta vs phi", "", 150, -Math.PI, Math.PI, 150, 0, 2);

		thetaVSmomenutm.setTitleX("p (GeV/c)");
		thetaVSmomenutm.setTitleY("#theta(rad)");
		thetaVSphi.setTitleX("#phi (rad)");
		thetaVSphi.setTitleY("#theta(rad)");
		phiVSmomenutm.setTitleX("p (GeV/c)");
		phiVSmomenutm.setTitleY("#phi(rad)");
		for (int j = 0; j < occupancies.length; j++) {
			occupancies[j] = new H2F("Occupancy" + j, "", 112, 1, 113, 6, 1, 7);
			occupancies[j].setTitleX("Wire");
			occupancies[j].setTitleY("Layer");
			if ((j % 6 == 0) || (j % 6 == 1)) {
				trkdocasvstime[j] = new H2F("TrackDoca vs time" + j, "", 100, 0, 200, 100, 0, 1);
			} else
				trkdocasvstime[j] = new H2F("TrackDoca vs time" + j, "", 100, 0, 600, 100, 0, 2);

		}
		for (int i = 0; i < cross_yvsx.length; i++) {
			cross_yvsx[i] = new H2F("y vs x sector" + (i + 1), "", 180, -180, 180, 120, -140, 140);
			cross_yvsx[i].setTitleX("x");
			cross_yvsx[i].setTitleY("y");
			cross_uyvsux[i] = new H2F("Uy vs Ux sector" + (i + 1), "", 100, -1, 1, 100, -1, 1);
			cross_uyvsux[i].setTitleX("Ux");
			cross_uyvsux[i].setTitleY("Uy");

		}

		EvioSource reader = new EvioSource();
		String fileName = "/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/reconstructedDataR128T0corT2DfromCCDBvarFit1.0.evio";
		reader.open(fileName);
		// reader.open("/Users/omcortes/Documents/EVIOfiles/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.10.evio");
		/* DC_Calib theDCcalib = new DC_Calib(); */
		int counter = 0;
		while (reader.hasEvent()) {
			DataEvent event = reader.getNextEvent();

			if (event.hasBank("TimeBasedTrkg::TBHits")) {
				counter++;
				if (counter % 10000 == 0) {
					System.out.println("done " + counter + " events");
				}
				DataBank tbhitsBank = event.getBank("TimeBasedTrkg::TBHits");
				int nTBHits = tbhitsBank.rows();
				for (int i = 0; i < nTBHits; i++) {
					if (tbhitsBank.getInt("sector", i) == 1 && tbhitsBank.getInt("superlayer", i) == 1) {
						occupancies[0].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[0].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 1 && tbhitsBank.getInt("superlayer", i) == 2) {
						occupancies[1].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[1].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 1 && tbhitsBank.getInt("superlayer", i) == 3) {
						occupancies[2].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[2].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 1 && tbhitsBank.getInt("superlayer", i) == 4) {
						occupancies[3].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[3].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 1 && tbhitsBank.getInt("superlayer", i) == 5) {
						occupancies[4].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[4].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 1 && tbhitsBank.getInt("superlayer", i) == 6) {
						occupancies[5].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[5].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 2 && tbhitsBank.getInt("superlayer", i) == 1) {
						occupancies[6].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[6].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 2 && tbhitsBank.getInt("superlayer", i) == 2) {
						occupancies[7].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[7].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 2 && tbhitsBank.getInt("superlayer", i) == 3) {
						occupancies[8].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[8].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 2 && tbhitsBank.getInt("superlayer", i) == 4) {
						occupancies[9].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[9].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 2 && tbhitsBank.getInt("superlayer", i) == 5) {
						occupancies[10].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[10].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 2 && tbhitsBank.getInt("superlayer", i) == 6) {
						occupancies[11].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[11].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 3 && tbhitsBank.getInt("superlayer", i) == 1) {
						occupancies[12].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[12].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 3 && tbhitsBank.getInt("superlayer", i) == 2) {
						occupancies[13].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[13].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 3 && tbhitsBank.getInt("superlayer", i) == 3) {
						occupancies[14].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[14].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 3 && tbhitsBank.getInt("superlayer", i) == 4) {
						occupancies[15].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[15].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 3 && tbhitsBank.getInt("superlayer", i) == 5) {
						occupancies[16].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[16].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 3 && tbhitsBank.getInt("superlayer", i) == 6) {
						occupancies[17].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[17].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 4 && tbhitsBank.getInt("superlayer", i) == 1) {
						occupancies[18].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[18].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 4 && tbhitsBank.getInt("superlayer", i) == 2) {
						occupancies[19].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[19].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 4 && tbhitsBank.getInt("superlayer", i) == 3) {
						occupancies[20].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[20].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 4 && tbhitsBank.getInt("superlayer", i) == 4) {
						occupancies[21].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[21].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 4 && tbhitsBank.getInt("superlayer", i) == 5) {
						occupancies[22].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[22].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 4 && tbhitsBank.getInt("superlayer", i) == 6) {
						occupancies[23].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[23].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 5 && tbhitsBank.getInt("superlayer", i) == 1) {
						occupancies[24].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[24].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 5 && tbhitsBank.getInt("superlayer", i) == 2) {
						occupancies[25].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[25].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 5 && tbhitsBank.getInt("superlayer", i) == 3) {
						occupancies[26].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[26].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 5 && tbhitsBank.getInt("superlayer", i) == 4) {
						occupancies[27].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[27].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 5 && tbhitsBank.getInt("superlayer", i) == 5) {
						occupancies[28].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[28].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 5 && tbhitsBank.getInt("superlayer", i) == 6) {
						occupancies[29].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[29].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 6 && tbhitsBank.getInt("superlayer", i) == 1) {
						occupancies[30].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[30].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 6 && tbhitsBank.getInt("superlayer", i) == 2) {
						occupancies[31].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[31].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 6 && tbhitsBank.getInt("superlayer", i) == 3) {
						occupancies[32].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[32].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 6 && tbhitsBank.getInt("superlayer", i) == 4) {
						occupancies[33].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[33].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else if (tbhitsBank.getInt("sector", i) == 6 && tbhitsBank.getInt("superlayer", i) == 5) {
						occupancies[34].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[34].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					} else {
						occupancies[35].fill(tbhitsBank.getInt("wire", i), tbhitsBank.getInt("layer", i));
						trkdocasvstime[35].fill(tbhitsBank.getDouble("time", i), tbhitsBank.getDouble("trkDoca", i));
					}
					// if (tbhitsBank.getInt("trkID", i) < 0) {
					// System.out.print("uyuyuy");
					// }

				}

			}
			if (event.hasBank("TimeBasedTrkg::TBCrosses")) {
				DataBank tbcrossesBank = event.getBank("TimeBasedTrkg::TBCrosses");
				int nTBCrosses = tbcrossesBank.rows();
				for (int j = 0; j < nTBCrosses; j++) {
					if (tbcrossesBank.getInt("sector", j) == 1) {
						cross_uyvsux[0].fill(tbcrossesBank.getDouble("ux", j), tbcrossesBank.getDouble("uy", j));
						cross_yvsx[0].fill(tbcrossesBank.getDouble("x", j), tbcrossesBank.getDouble("y", j));
					}

					if (tbcrossesBank.getInt("sector", j) == 2) {
						cross_uyvsux[1].fill(tbcrossesBank.getDouble("ux", j), tbcrossesBank.getDouble("uy", j));
						cross_yvsx[1].fill(tbcrossesBank.getDouble("x", j), tbcrossesBank.getDouble("y", j));
					}

					if (tbcrossesBank.getInt("sector", j) == 3) {
						cross_uyvsux[2].fill(tbcrossesBank.getDouble("ux", j), tbcrossesBank.getDouble("uy", j));
						cross_yvsx[2].fill(tbcrossesBank.getDouble("x", j), tbcrossesBank.getDouble("y", j));
					}

					if (tbcrossesBank.getInt("sector", j) == 4) {
						cross_uyvsux[3].fill(tbcrossesBank.getDouble("ux", j), tbcrossesBank.getDouble("uy", j));
						cross_yvsx[3].fill(tbcrossesBank.getDouble("x", j), tbcrossesBank.getDouble("y", j));
					}

					if (tbcrossesBank.getInt("sector", j) == 5) {
						cross_uyvsux[4].fill(tbcrossesBank.getDouble("ux", j), tbcrossesBank.getDouble("uy", j));
						cross_yvsx[4].fill(tbcrossesBank.getDouble("x", j), tbcrossesBank.getDouble("y", j));
					}

					if (tbcrossesBank.getInt("sector", j) == 6) {
						cross_uyvsux[5].fill(tbcrossesBank.getDouble("ux", j), tbcrossesBank.getDouble("uy", j));
						cross_yvsx[5].fill(tbcrossesBank.getDouble("x", j), tbcrossesBank.getDouble("y", j));
					}

				}
			}

			if (event.hasBank("TimeBasedTrkg::TBTracks")) {
				Vector3 momentum = new Vector3();
				DataBank tbtracksBank = event.getBank("TimeBasedTrkg::TBTracks");
				int nTBTracks = tbtracksBank.rows();
				for (int j = 0; j < nTBTracks; j++) {

					momentum.setXYZ(tbtracksBank.getDouble("t1_px", j), tbtracksBank.getDouble("t1_py", j),
					        tbtracksBank.getDouble("t1_pz", j));
					thetaVSmomenutm.fill(tbtracksBank.getDouble("p", j), momentum.theta());
					phiVSmomenutm.fill(tbtracksBank.getDouble("p", j), momentum.phi());
					thetaVSphi.fill(momentum.phi(), momentum.theta());
				}
			}

		}
		EmbeddedCanvas can1 = new EmbeddedCanvas();
		EmbeddedCanvas can5 = new EmbeddedCanvas();
		can1.divide(6, 6);
		can5.divide(6, 6);
		for (int k = 0; k < occupancies.length; k++) {
			can1.cd(k);
			can1.draw(occupancies[k]);
			can5.cd(k);
			can5.draw(trkdocasvstime[k]);
		}
		EmbeddedCanvas can2 = new EmbeddedCanvas();

		EmbeddedCanvas can3 = new EmbeddedCanvas();
		can3.divide(2, 3);
		for (int k = 0; k < cross_uyvsux.length; k++) {
			can3.cd(k);
			can3.draw(cross_uyvsux[k]);
		}

		EmbeddedCanvas can4 = new EmbeddedCanvas();
		can4.divide(2, 3);
		for (int k = 0; k < cross_yvsx.length; k++) {
			can4.cd(k);
			can4.draw(cross_yvsx[k]);
		}
		EmbeddedCanvas can6 = new EmbeddedCanvas();
		can6.divide(2, 2);
		can6.cd(0);
		can6.draw(thetaVSmomenutm);
		can6.cd(1);
		can6.draw(phiVSmomenutm);
		can6.cd(2);
		can6.draw(thetaVSphi);
		tabbedPane.add("Occupancies all", can1);
		tabbedPane.add("Occupancies track", can2);
		tabbedPane.add("Crosses angles", can3);
		tabbedPane.add("Crosses position", can4);
		tabbedPane.add("TrkDoca vs Time", can5);
		tabbedPane.add("Track Kin", can6);
		frame.add(tabbedPane);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		System.out.print("here");

	}
}

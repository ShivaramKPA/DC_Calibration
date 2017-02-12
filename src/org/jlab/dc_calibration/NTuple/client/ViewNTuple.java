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
package org.jlab.dc_calibration.NTuple.client;

import static org.jlab.dc_calibration.domain.Constants.parName;
import static org.jlab.dc_calibration.domain.Constants.prevFitPars;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnStrategy;
import org.freehep.math.minuit.MnUserParameters;
import org.jlab.dc_calibration.NTuple.NTuple;
import org.jlab.dc_calibration.domain.DCFitDrawer;
import org.jlab.dc_calibration.domain.DCFitFunction;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;

public class ViewNTuple {
	public static void main(String[] args) {
		// NTuple("pionTestData", "Sector:SuperLayer:ThetaBin:Time:Doca");

		NTuple elecTestData = new NTuple("pionTest", 5);
		elecTestData.open("src/files/elecTest.evio");
		elecTestData.scan();

		H2F hElecDocaTime =
		        elecTestData.histogram2D("Doca", "Time", "Sector ==1 & SuperLayer ==4 & ThetaBin ==1 ", 200, 0.0, 1.0, 150, 0.0, 200.0);
		GraphErrors elecTestgraph = hElecDocaTime.getProfileX();

		final int nFreePars = 4;
		DCFitFunction dcElecFcn = new DCFitFunction(elecTestgraph, 4, 1, false);

		MnUserParameters elecUpar = new MnUserParameters();
		double parSteps[] = { 0.00001, 0.001, 0.01, 0.01, 0.0001 };
		double pLow[] = { prevFitPars[0] * 0.4, prevFitPars[1] * 0.0, prevFitPars[2] * 0.4, prevFitPars[3] * 0.4, prevFitPars[4] * 0.0 };
		double pHigh[] = { prevFitPars[0] * 1.6, prevFitPars[1] * 5.0, prevFitPars[2] * 1.6, prevFitPars[3] * 1.6, prevFitPars[4] * 1.6 };
		for (int p = 0; p < nFreePars; p++) {
			elecUpar.add(parName[p], prevFitPars[p], parSteps[p], pLow[p], pHigh[p]);
		}
		double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 525.0, 550.0 };

		elecUpar.setValue(2, tMaxSL[3]);// 155.0); //tMax for SLth superlayer
		elecUpar.fix(2); // fixed for now.

		System.out.println("start migrad");
		MnMigrad migradElec = new MnMigrad(dcElecFcn, elecUpar);

		FunctionMinimum elecMin = migradElec.minimize();

		if (!elecMin.isValid()) {
			// try with higher strategy
			System.out.println("FM is invalid, try with strategy = 2.");
			MnMigrad migrad2 = new MnMigrad(dcElecFcn, elecMin.userState(), new MnStrategy(2));
			elecMin = migrad2.minimize();
		}

		MnUserParameters elecUserpar = elecMin.userParameters();

		for (int p = 0; p < nFreePars; p++)
			System.out.println(parName[p] + " = " + elecUserpar.value(parName[p]) + " +/- " + elecUserpar.error(parName[p]));
		double[] fPars = new double[nFreePars], fErrs = new double[nFreePars];

		for (int p = 0; p < nFreePars; p++) {
			fPars[p] = elecUserpar.value(parName[p]);
			fErrs[p] = elecUserpar.error(parName[p]);
		}
		double[] pars4FitLine = new double[nFreePars];

		for (int i = 0; i < nFreePars; i++) {
			pars4FitLine[i] = fPars[i];
		}
		DCFitDrawer fitElecLine = new DCFitDrawer("Electrons", 0.0, 0.35, 4, 1, false);

		double[] pars4FitLineTmp = new double[nFreePars];
		// for (int i = 0; i < nSectors; i++) {
		// for (int j = 0; j < nSL; j++) {
		//
		fitElecLine.setLineColor(2);
		fitElecLine.setLineWidth(3);
		fitElecLine.setLineStyle(4);
		fitElecLine.setParameters(pars4FitLine);

		EmbeddedCanvas can1 = new EmbeddedCanvas();
		can1.divide(1, 3);
		can1.cd(0);
		can1.draw(hElecDocaTime);
		can1.cd(1);
		can1.draw(hElecDocaTime);
		can1.draw(fitElecLine, "same");
		can1.cd(2);
		can1.draw(elecTestgraph);
		can1.draw(fitElecLine, "same");

		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		JFrame aFrame = new JFrame("Sectors");
		aFrame.setSize((int) (screensize.getHeight() * .75 * 1.618), (int) (screensize.getHeight() * .75));
		aFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("A plot", can1);
		aFrame.add(tabbedPane);
		aFrame.setVisible(true);
		// can1.divide(2, 3);
		// Ex_all.draw("Sector", "", "", can1, 0);

	}

}

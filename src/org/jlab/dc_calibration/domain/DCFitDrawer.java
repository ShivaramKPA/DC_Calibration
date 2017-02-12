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

import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;

//import static org.jlab.dc_calibration.domain.Constants.*;
import java.util.ArrayList;

import org.jlab.groot.math.Func1D;

public class DCFitDrawer extends Func1D {
	private int superlayer;
	private int thetaBin;
	private boolean isLinearFit;
	private double[] fPars;
	private DCTimeFunction timeFunc;

	public DCFitDrawer() {
		super("calibFnToDraw", 0.0, 1.0);
		this.initParameters();
	}

	public DCFitDrawer(String name, double xmin, double xmax, int superlayer, int thetaBin, boolean isLinearFit) {
		super(name, xmin, xmax);
		this.initParameters();
		this.superlayer = superlayer;
		this.thetaBin = thetaBin;
		this.isLinearFit = isLinearFit;
	}

	private void initParameters() {
		ArrayList<String> pars = new ArrayList<String>();
		pars.add("v0");
		pars.add("deltamn");
		pars.add("tmax");
		pars.add("distbeta");
		for (int loop = 0; loop < pars.size(); loop++) {
			this.addParameter(pars.get(loop));
		}
		double prevFitPars[] = { 62.92e-04, 1.35, 148.02, 0.055 };

		this.setParameters(prevFitPars);
	}

	private void setParmLength(int i) {
		this.fPars = new double[i + 1];
	}

	@Override
	public void setParameters(double[] params) {
		setParmLength(params.length);
		for (int i = 0; i < params.length; i++) {
			this.setParameter(i, params[i]);
			fPars[i] = params[i];
		}
	}

	@Override
	public double evaluate(double xNorm) {
		double thetaDeg = 0.5 * (thEdgeVzL[thetaBin] + thEdgeVzH[thetaBin]); // Center of theta bin
		timeFunc = new DCTimeFunction(superlayer, thetaDeg, xNorm, fPars);
		double calcTime = isLinearFit ? timeFunc.linearFit() : timeFunc.nonLinearFit();
		return calcTime;
	}

}
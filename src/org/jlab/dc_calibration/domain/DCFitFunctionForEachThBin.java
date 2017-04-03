/* 
 * @author m.c.kunkel & kpadhikari
 *  based of the KrishnaFcn.java
*/
package org.jlab.dc_calibration.domain;

import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;

import org.freehep.math.minuit.FCNBase;
import org.jlab.groot.data.GraphErrors;

public class DCFitFunctionForEachThBin implements FCNBase {

	private GraphErrors profileX;
	private int superlayer;
	private int thetaBin;
	private boolean isLinear;

	private DCTimeFunction timeFunc;

	public DCFitFunctionForEachThBin(GraphErrors profileX, int superlayer, int thetaBin, boolean isLinear) {
		this.profileX = profileX;
		this.superlayer = superlayer;
		this.thetaBin = thetaBin;
		this.isLinear = isLinear;
	}

	public double errorDef() {
		return 1;
	}

	@Override
	public double valueOf(double[] par) {
		double thetaDeg = 0.5 * (thEdgeVzL[thetaBin] + thEdgeVzH[thetaBin]);
		double delta = 0;
		double chisq = 0;

		for (int i = 0; i < profileX.getDataSize(0); i++) {

			double docaNorm = profileX.getDataX(i);
			double measTime = profileX.getDataY(i);
			double measTimeErr = profileX.getDataEY(i);
			timeFunc = new DCTimeFunction(superlayer, thetaDeg, docaNorm, par);
			double calcTime = isLinear ? timeFunc.linearFit() : timeFunc.nonLinearFit();

			//if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.9) {
			if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.8) { //2/15/17
				delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
				chisq += delta * delta;
			}
		}

		return chisq;
	}
}

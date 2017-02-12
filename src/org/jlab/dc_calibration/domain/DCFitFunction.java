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
 *  `------'					based of the KrishnaFcn.java
 */
package org.jlab.dc_calibration.domain;

import java.util.HashMap;
import java.util.Map;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;

import org.freehep.math.minuit.FCNBase;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H2F;

public class DCFitFunction implements FCNBase {

    private GraphErrors profileX;
    private int sector;
    private int superlayer;
    private int thetaBin;
    private boolean isLinear;
    private Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();

    private DCTimeFunction timeFunc;

    public DCFitFunction(GraphErrors profileX, int superlayer, int thetaBin, boolean isLinear) {
        this.profileX = profileX;
        this.superlayer = superlayer;
        this.thetaBin = thetaBin;
        this.isLinear = isLinear;
    }

    //public DCFitFunction(Map<Coordinate, H2F> h2timeVtrkDoca, int sector, int superlayer, int thetaBin, boolean isLinear) {
    public DCFitFunction(Map<Coordinate, H2F> h2timeVtrkDoca, int sector, int superlayer, boolean isLinear) {
        this.h2timeVtrkDoca = h2timeVtrkDoca;
        this.sector = sector;
        this.superlayer = superlayer;
        this.isLinear = isLinear;
        //this.thetaBin = thetaBin; //To be removed later
        //this.profileX = h2timeVtrkDoca.get(new Coordinate(sector, superlayer, thetaBin)).getProfileX(); //To be removed later
    }

    public double errorDef() {
        return 1;
    }

    @Override
    public double valueOf(double[] par) {
        double delta = 0;
        double chisq = 0;
        double thetaDeg = 0;
        GraphErrors profileX;
        /*
                thetaDeg = 0.5 * (thEdgeVzL[thetaBin] + thEdgeVzH[thetaBin]);
		for (int i = 0; i < profileX.getDataSize(0); i++) {

			double docaNorm = profileX.getDataX(i);
			double measTime = profileX.getDataY(i);
			double measTimeErr = profileX.getDataEY(i);
			timeFunc = new DCTimeFunction(superlayer, thetaDeg, docaNorm, par);
			double calcTime = isLinear ? timeFunc.linearFit() : timeFunc.nonLinearFit();

			if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.9) {
				delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
				chisq += delta * delta;
			}
		}
         */
        int discardThBins = 4;
        for (int th = 0 + discardThBins; th < nThBinsVz - discardThBins; th++) {
            thetaDeg = 0.5 * (thEdgeVzL[th] + thEdgeVzH[th]);
            profileX = h2timeVtrkDoca.get(new Coordinate(sector, superlayer, th)).getProfileX();
            for (int i = 0; i < profileX.getDataSize(0); i++) {

                double docaNorm = profileX.getDataX(i);
                double measTime = profileX.getDataY(i);
                double measTimeErr = profileX.getDataEY(i);
                timeFunc = new DCTimeFunction(superlayer, thetaDeg, docaNorm, par);
                double calcTime = isLinear ? timeFunc.linearFit() : timeFunc.nonLinearFit();

                if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.9) {
                    delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
                    chisq += delta * delta;
                }
            }
        }
        return chisq;
    }
}

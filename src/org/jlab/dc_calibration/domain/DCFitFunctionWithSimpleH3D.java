/**
 *
 * @author kpadhikari & m.c. kunkel
 */

package org.jlab.dc_calibration.domain;

import java.util.HashMap;
import java.util.Map;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;

import org.freehep.math.minuit.FCNBase;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz2;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH2;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL2;
import static org.jlab.dc_calibration.domain.Constants.wpdist;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H2F;

public class DCFitFunctionWithSimpleH3D implements FCNBase {

    private GraphErrors profileX;
    private int iSector;
    private int iSuperlayer;
    private int thetaBin;
    private boolean isLinear;
    private Map<Coordinate, SimpleH3D> h3BvTvX = new HashMap<Coordinate, SimpleH3D>();
    private int nBinsX, nBinsY, nBinsZ;

    private DCTimeFunctionXTB timeFunc;

    public DCFitFunctionWithSimpleH3D(Map<Coordinate, SimpleH3D> h3BvTvX, int iSector, int iSuperlayer, boolean isLinear) {
        this.h3BvTvX = h3BvTvX;
        this.iSector = iSector;
        this.iSuperlayer = iSuperlayer;
        this.isLinear = isLinear;
        this.nBinsX = h3BvTvX.get(new Coordinate(iSector, iSuperlayer, 0)).getNBinsX();
        this.nBinsY = h3BvTvX.get(new Coordinate(iSector, iSuperlayer, 0)).getNBinsY();
        this.nBinsZ = h3BvTvX.get(new Coordinate(iSector, iSuperlayer, 0)).getNBinsZ();
     }

    public double errorDef() {
        return 1;
    }

    @Override
    public double valueOf(double[] par) {
        double delta = 0;
        double chisq = 0;
        double thetaDeg = 0;
        double dMax = 2 * wpdist[iSuperlayer];

        int discardThBins = 0;
        for (int th = 0 + discardThBins; th < nThBinsVz2 - discardThBins; th++) {
            //discard central bin (i.e. the bin around zero-degree) to avoid bad relolution events
            if(th == (nThBinsVz2/2)) {continue;}
            if(th == (nThBinsVz2/2) - 1 || th == (nThBinsVz2/2) + 1 ) {continue;}
            double[] XTB = new double[3]; 
            //XTB = h3BvTvX.get(new Coordinate(iSector, iSuperlayer, th)).getBinCenter();
            
            thetaDeg = 0.5 * (thEdgeVzL2[th] + thEdgeVzH2[th]);
            //profileX = h3BvTvX.get(new Coordinate(iSector, iSuperlayer, th)).getProfileX();
            
            //for (int i = 0; i < profileX.getDataSize(0); i++) {
            for (int i = 0; i < nBinsX; i++) {
                for (int j = 0; j < nBinsY; j++) {
                    for (int k = 0; k < nBinsZ; k++) {
                        XTB = h3BvTvX.get(new Coordinate(iSector,iSuperlayer,th)).getBinCenter(i, j, k);
                        double docaNorm = XTB[0]/dMax;
                        double measTime = XTB[1];
                        double Bfield = XTB[2];
                        double statErr = h3BvTvX.get(new Coordinate(iSector,iSuperlayer,th)).getBinError(i, j, k);
                        timeFunc = new DCTimeFunctionXTB(iSuperlayer, thetaDeg, docaNorm, Bfield, par);
                        double calcTime = isLinear ? timeFunc.linearFit() : timeFunc.nonLinearFit();

                        //if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.9) {
                        if (statErr == statErr && statErr > 0.0 && docaNorm < 0.8) { //2/15/17
                            delta = (measTime - calcTime) / statErr; // error weighted deviation
                            chisq += delta * delta;
                        }
                    }
                }
            }
        }
        return chisq;
    }
}

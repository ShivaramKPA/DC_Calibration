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
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;

public class DCFitFunction implements FCNBase {

    private GraphErrors profileX;
    private int sector;
    private int superlayer;
    private int thetaBin;
    private boolean isLinear;
    private int meanErrorType = 2; //0: RMS, 1=RMS/sqrt(N), 2 = 1.0 (giving equal weight to all profile means)
    private double docaNormMin = 0.0, docaNormMax = 0.8;
    private Map<Coordinate, H2F> h2timeVtrkDocaNorm = new HashMap<Coordinate, H2F>();

    private DCTimeFunction timeFunc;

    public DCFitFunction(GraphErrors profileX, int superlayer, int thetaBin, boolean isLinear) {
        this.profileX = profileX;
        this.superlayer = superlayer;
        this.thetaBin = thetaBin;
        this.isLinear = isLinear;
    }

    //public DCFitFunction(Map<Coordinate, H2F> h2timeVtrkDocaNorm, int sector, int superlayer, int thetaBin, boolean isLinear) {
    public DCFitFunction(Map<Coordinate, H2F> h2timeVtrkDocaNorm, int sector, int superlayer, boolean isLinear) {
        this.h2timeVtrkDocaNorm = h2timeVtrkDocaNorm;
        this.sector = sector;
        this.superlayer = superlayer;
        this.isLinear = isLinear;
    }
    
    public DCFitFunction(Map<Coordinate, H2F> h2timeVtrkDocaNorm, int sector, int superlayer, 
            int meanErrorType, double docaNormMin, double docaNormMax, boolean isLinear) {
        this.h2timeVtrkDocaNorm = h2timeVtrkDocaNorm;
        this.sector = sector;
        this.superlayer = superlayer;
        this.isLinear = isLinear;
        this.meanErrorType = meanErrorType;
        this.docaNormMin = docaNormMin;
        this.docaNormMax = docaNormMax;
        //this.thetaBin = thetaBin; //To be removed later
    }    

    public double errorDef() {
        return 1;
    }

    @Override
    public double valueOf(double[] par) {
        double delta = 0;
        double chisq = 0;
        double thetaDeg = 0;
        double measTimeErr = 0.0;
        GraphErrors profileX;
        H2F h2tvXnorm;
        H1F sliceX;

        int discardThBins = 4;
        for (int th = 0 + discardThBins; th < nThBinsVz - discardThBins; th++) {
            //discard central bin (i.e. the bin around zero-degree) to avoid bad relolution events
            if(th == (nThBinsVz/2)) {continue;}
            if(th == (nThBinsVz/2) - 1 || th == (nThBinsVz/2) + 1 ) {continue;} //Next bin on each side of the central one
            
            thetaDeg = 0.5 * (thEdgeVzL[th] + thEdgeVzH[th]);
            h2tvXnorm = h2timeVtrkDocaNorm.get(new Coordinate(sector, superlayer, th));
            //profileX = h2timeVtrkDocaNorm.get(new Coordinate(sector, superlayer, th)).getProfileX();
            profileX = h2tvXnorm.getProfileX();
            for (int i = 0; i < profileX.getDataSize(0); i++) {

                double docaNorm = profileX.getDataX(i);
                double measTime = profileX.getDataY(i);
                
                //Three different options for errors to weigh the chisq calculation
                if(meanErrorType == 0) 
                    measTimeErr = profileX.getDataEY(i);//4/5/17: Replaced this with the followign two lines                
                else if(meanErrorType == 1) {
                    sliceX = h2tvXnorm.sliceX(i);//Histogram of y-values for all data falling in the ith x-slice or x-bin.
                    measTimeErr = profileX.getDataEY(i) / Math.sqrt(sliceX.getEntries()); //Using Error = RMS/sqrt(N)
                }
                else if (meanErrorType == 2)
                    measTimeErr = 1.0; //Giving equal weight (to avoid having fit biased by heavily populated bins)
                
                timeFunc = new DCTimeFunction(superlayer, thetaDeg, docaNorm, par);
                double calcTime = isLinear ? timeFunc.linearFit() : timeFunc.nonLinearFit();

                //if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.9) {
                if (measTimeErr == measTimeErr && measTimeErr > docaNormMin && docaNorm < docaNormMax) { //2/15/17
                    delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
                    chisq += delta * delta;
                }
            }
        }
        return chisq;
    }
}

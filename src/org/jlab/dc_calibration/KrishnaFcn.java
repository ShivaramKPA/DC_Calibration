/**
 *
 * @author KPAdhikari
 */
package org.jlab.dc_calibration;

import static org.jlab.dc_calibration.domain.Constants.beta;
import static org.jlab.dc_calibration.domain.Constants.cos30;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.rad2deg;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;
import static org.jlab.dc_calibration.domain.Constants.wpdist;

import org.freehep.math.minuit.FCNBase;
import static org.jlab.dc_calibration.domain.Constants.nSectors;
import org.jlab.groot.data.GraphErrors;

public class KrishnaFcn implements FCNBase {
        
        private int Sector;
	private int SL;
	private int nThBins;
	private GraphErrors[][][] profileX;
	private boolean isLinear = false;

	public KrishnaFcn(int Sector, int SL, int nThBins, GraphErrors[][][] profileX, boolean isLinear) {
		this.Sector = Sector;
		this.SL = SL;
		this.nThBins = nThBins;
                this.profileX = profileX;
		this.isLinear = isLinear;
	}

	public double errorDef() {
		return 1;
	}

    public double valueOf(double[] par) {
        double delta = 0.;
        double chisq = 0.;
        double thetaDeg = 0.;
        double docaNorm = 0.;
        double measTime = 0.;
        double measTimeErr = 0.;
        double calcTime = 0.;
        for (int th = 1; th < nThBinsVz; th++) { // Using only some theta binsbetween 0 and 30 degrees
            thetaDeg = 0.5 * (thEdgeVzL[th] + thEdgeVzH[th]);// No 0.5 factor used before 9/20/16
            for (int i = 0; i < profileX[Sector][SL][th].getDataSize(0); i++) {
                docaNorm = profileX[Sector][SL][th].getDataX(i);
                measTime = profileX[Sector][SL][th].getDataY(i);
                measTimeErr = profileX[Sector][SL][th].getDataEY(i);
                calcTime = isLinear ? calcTimeFunc(0, Sector, SL + 1, docaNorm, par) : calcTimeFunc(0, Sector, SL + 1, thetaDeg, docaNorm, par);

                // 9/27/16: without docaNorm<0.9, the minimization was
                // very unstable. For example,
                // tmax for SL=2 (i.e. tmax2) came out around 150 when
                // the # of events used was N=20000 or 200000
                // where as it came out around 88 ns when N was
                // somewhere in between such as 80000, 100000 etc.
                // My guess was some of the bins with very low statistic
                // had unrealistic errors bars and biased
                // the minimization. When I used "delta = (measTime -
                // calcTime);", the tmax2 result was more
                // realistic (i.e., closer to 150 ns) than 88 ns.
                // if(measTimeErr==measTimeErr && measTimeErr>0.0 )
                if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.9) {
                    delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
                    chisq += delta * delta;
                }
            }
        }

        // System.out.println("chisq = " + chisq);
        return chisq;// fval;
    }

	protected double calcTimeFunc(int debug, int Sector, int SL, double docaByDocaMax, double[] par) {
		double dMax = 2 * wpdist[SL - 1];
		double x = docaByDocaMax * dMax;
		double v0Par = par[0];
		double calcTime = x / v0Par;
		if (debug == 1)
			System.out.println("v0Par = " + v0Par + " calcTime: " + calcTime);

		return calcTime;

	}

	protected double calcTimeFunc(int debug, int Sector, int SL, double thetaDeg, double docaByDocaMax, double[] par) // 9/4/16
	{
		// From one of M. Mestayer's email:
		// Double_t time = x/v0 + a0*pow(Xhat0, n) + b0*pow(Xhat0,m); //Here
		// X = x/(dMax*cos(30deg));
		// deltanm=2; m = n - deltanm; b = (tmax - dmax/v0)(1.0 - m/n); a =
		// -b* m/n; (see Mac's email)
		// tmax: Krishna's plots show tmax to be 165, 174 ns for superlayers
		// 1 and 2
		// For superlayers 3 and 4 let's use 300 and 320 (this is for zero
		// B-field)
		// and for superlayers 5 and 6 let's use 530 and 560 ns.
		// deltanm: let's start with a value of 2. double calcTime = 0;
		// //slope*xCoordinate + yIntercept;

		double dMax = 2 * wpdist[SL - 1], Dc = dMax * cos30;
		// double cos30 = Math.cos(30.0/rad2deg);//Now it's a global
		// constant to avoid repeated calc. (see above main())
		double x = docaByDocaMax * dMax;
		double v0Par = par[0];
		double deltanm = par[1];
		double tMax = par[2];		
		double distbeta = par[3]; // 8/3/16: initial value given by Mac is 0.050 cm.
		// Assume a functional form (time =
		// x/v0+a*(x/dmax)**n+b*(x/dmax)**m) for theta = 30 deg.
		// First, calculate n
		double nPar = (1.0 + (deltanm - 1.0) * Math.pow(0.615, deltanm)) / (1.0 - Math.pow(0.615, deltanm));

		// now, calculate m
		double mPar = nPar + deltanm;// Actually it should be named deltamn
		                             // and should be + in between
		                             // //7/21/16
		// determine b from the constraint that the time = tmax at dist=dmax
		double b = (tMax - dMax / v0Par) / (1.0 - mPar / nPar);

		// determine a from the requirement that the derivative at
		// d=dmax equal the derivative at d=0
		double a = -b * mPar / nPar; // From one of the constraints
		double alpha = thetaDeg; // = 0.0; //Local angle in degrees.
		double cos30minusalpha = Math.cos((30. - alpha) / rad2deg); // =Math.cos(Math.toRadians(30.-alpha));
		double xhat = x / dMax, dmaxalpha = dMax * cos30minusalpha, xhatalpha = x / dmaxalpha;

		// now calculate the dist to time function for theta = 'alpha' deg.
		// Assume a functional form with the SAME POWERS N and M and
		// coefficient a but a new coefficient 'balpha' to replace b.
		// Calculate balpha from the constraint that the value
		// of the function at dmax*cos30minusalpha is equal to tmax

		// parameter balpha (function of the 30 degree paramters a,n,m)
		double balpha = (tMax - dmaxalpha / v0Par - a * Math.pow(cos30minusalpha, nPar)) / Math.pow(cos30minusalpha, mPar);

		// now calculate function
		double xhatPowN = Math.pow(xhat, nPar), xhatPowM = Math.pow(xhat, mPar);
		double term1 = x / v0Par, term2 = a * xhatPowN, term3 = balpha * xhatPowM;
		double calcTime = term1 + term2 + term3;

		if (debug == 1)
			System.out.println("v0Par nPar  b  a  xhat xhatalpha  xhatPowN  xhatPowM  term1  term2  term3  time: " + v0Par + " " + nPar
			        + " " + b + " " + a + " " + xhat + " " + xhatalpha + " " + xhatPowN + " " + xhatPowM + " " + term1 + " " + term2 + " "
			        + term3 + " " + calcTime);

		// ===================== 8/3/16
		// double deltatime_beta=(sqrt(x**2+(distbeta*beta**2)**2)-x)/v0;
		// //where x is trkdoca
		double deltatime_beta = (Math.sqrt(x * x + Math.pow(distbeta * Math.pow(beta, 2), 2)) - x) / v0Par;
		calcTime = calcTime + deltatime_beta;
		// ===================== 8/3/16

		return calcTime;
	}
}

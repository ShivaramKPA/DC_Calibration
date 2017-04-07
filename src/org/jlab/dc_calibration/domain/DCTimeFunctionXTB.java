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

import static org.jlab.dc_calibration.domain.Constants.beta;
import static org.jlab.dc_calibration.domain.Constants.deg2rad;
import static org.jlab.dc_calibration.domain.Constants.rad2deg;
import static org.jlab.dc_calibration.domain.Constants.wpdist;

public class DCTimeFunctionXTB {

	private int iSuperlayer;
	private double thetaDeg;
	private double docaNorm;
	private double[] par;
        private double bfield;

	public DCTimeFunctionXTB(int iSuperlayer, double thetaDeg, double docaNorm, double bfield, double[] par) {
		this.iSuperlayer = iSuperlayer;
		this.thetaDeg = thetaDeg;
		this.docaNorm = docaNorm;
		this.par = par;                
		this.bfield = bfield; 
	}  
        
	public double linearFit() {
		double dMax = 2 * wpdist[iSuperlayer];
		double x = docaNorm * dMax;
		double v0Par = par[0];
		return x / v0Par;
	}

	public double nonLinearFit() {
		double dMax = 2 * wpdist[iSuperlayer];
		// constant to avoid repeated calc. (see above main())
		double x = docaNorm * dMax;
		double v0Par = par[0];
		double deltanm = par[1];
		double tMax = par[2];
		double distbeta = par[3]; // 8/3/16: initial value given by Mac is 0.050 cm.
                //Now the B-field parameters (applicable only to SL=3 & 4 i.e region-2)
                double delta_bfield_coefficient = 0.0; //par[4];
                double b1 = 0.0; //par[5];
                double b2 = 0.0; //par[6];
                double b3 = 0.0; //par[7];
                double b4 = 0.0; //par[8];
                
		// Assume a functional form (time =
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

		double alpha = isReducedAngle(thetaDeg); // = 0.0; //Local angle in degrees.
		//System.out.println("this is alpha " + alpha);
		double cos30minusalpha = Math.cos((30. - alpha) / rad2deg); // =Math.cos(Math.toRadians(30.-alpha));
		double xhat = x / dMax;
		double dmaxalpha = dMax * cos30minusalpha;
                double xhatalpha = x/dmaxalpha;
                
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

                //     and here's a parameterization of the change in time due to a non-zero
                //     bfield for where xhat=x/dmaxalpha where dmaxalpha is the 'dmax' for
                //           a track with local angle alpha (for local angle = alpha)
                double deltatime_bfield = 0.0;
                if (iSuperlayer == 3 || iSuperlayer == 4) {
                    deltatime_bfield = delta_bfield_coefficient * Math.pow(bfield, 2) * tMax *
                            ( b1 * xhatalpha
                            + b2 * Math.pow(xhatalpha, 2)
                            + b3 * Math.pow(xhatalpha, 3)
                            + b4 * Math.pow(xhatalpha, 4));
                }
                
                
		// //where x is trkdoca
		double deltatime_beta = (Math.sqrt(x * x + Math.pow(distbeta * Math.pow(beta, 2), 2)) - x) / v0Par;
		calcTime = calcTime + deltatime_bfield + deltatime_beta;

		return calcTime;
	}

	private double isReducedAngle(double alpha) {
		double ralpha;
		ralpha = Math.abs(alpha * deg2rad);

		while (ralpha > Math.PI / 3.0)
			ralpha -= Math.PI / 3.0;
		if (ralpha > Math.PI / 6.0)
			ralpha = Math.PI / 3.0 - ralpha;

		return ralpha * rad2deg;
	}

}

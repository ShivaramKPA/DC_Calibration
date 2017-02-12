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
package org.jlab.dc_calibration;

public class TimeToDistance {

	
	
	
	public TimeToDistance() {
	
	}
	/**
    *
    * @param x x = (double)(idist+1)*stepSize; idistmax = (int) (dmax/stepSize);
    * @param dmax dmax = CalibrationConstantsLoader.dmaxsuperlayer[r];
    * @param tmax tmax = CalibrationConstantsLoader.tmaxsuperlayer[s][r];
    * @param alpha alpha = -(Math.toDegrees(Math.acos(cos30minusalpha)) - 30);
    * @param bfield
    * @param s sector idx
    * @param r superlayer idx
    * @return returns time (ns) when given inputs of distance x (cm), local angle alpha (degrees) and magnitude of bfield (Tesla).
    */
    private static double calc_Time(double x, double dmax, double tmax, double alpha, double bfield, int s, int r) {

        // Assume a functional form (time=x/v0+a*(x/dmax)**n+b*(x/dmax)**m)
        // for time as a function of x for theta = 30 deg.
        // first, calculate n
        double n = ( 1.+ (CalibrationConstantsLoader.deltanm[s][r]-1.)*Math.pow(FracDmaxAtMinVel, CalibrationConstantsLoader.deltanm[s][r]) )/( 1.- Math.pow(FracDmaxAtMinVel, CalibrationConstantsLoader.deltanm[s][r]));
        //now, calculate m
        double m = n + CalibrationConstantsLoader.deltanm[s][r];
        // determine b from the requirement that the time = tmax at dist=dmax
        double b = (tmax - dmax/CalibrationConstantsLoader.v0[s][r])/(1.- m/n);
        // determine a from the requirement that the derivative at
        // d=dmax equal the derivative at d=0
        double a = -b*m/n;

        double cos30minusalpha=Math.cos(Math.toRadians(30.-alpha));
        double xhat = x/dmax;
        double dmaxalpha = dmax*cos30minusalpha;
        double xhatalpha = x/dmaxalpha;

        //     now calculate the dist to time function for theta = 'alpha' deg.
        //     Assume a functional form with the SAME POWERS N and M and
        //     coefficient a but a new coefficient 'balpha' to replace b.
        //     Calculate balpha from the constraint that the value
        //     of the function at dmax*cos30minusalpha is equal to tmax

        //     parameter balpha (function of the 30 degree paramters a,n,m)
        double balpha = ( tmax - dmaxalpha/CalibrationConstantsLoader.v0[s][r] - a*Math.pow(cos30minusalpha,n))/Math.pow(cos30minusalpha, m);

       //      now calculate function
        double time = x/CalibrationConstantsLoader.v0[s][r] + a*Math.pow(xhat, n) + balpha*Math.pow(xhat, m);

       //     and here's a parameterization of the change in time due to a non-zero
       //     bfield for where xhat=x/dmaxalpha where dmaxalpha is the 'dmax' for
       //       a track with local angle alpha (for local angle = alpha)
        double deltatime_bfield = CalibrationConstantsLoader.delt_bfield_coefficient[s][r]*Math.pow(bfield,2)*tmax*(CalibrationConstantsLoader.deltatime_bfield_par1[s][r]*xhatalpha+CalibrationConstantsLoader.deltatime_bfield_par2[s][r]*Math.pow(xhatalpha, 2)+
                CalibrationConstantsLoader.deltatime_bfield_par3[s][r]*Math.pow(xhatalpha, 3)+CalibrationConstantsLoader.deltatime_bfield_par4[s][r]*Math.pow(xhatalpha, 4));

        //calculate the time at alpha deg. and at a non-zero bfield
        time += deltatime_bfield;


        return time;
    }


//The constants are loaded from ccdb:
               int iSec = dbprovider.getInteger("/calibration/dc/time_to_distance/tvsx_devel_v2/Sector", i);
           int iSly = dbprovider.getInteger("/calibration/dc/time_to_distance/tvsx_devel_v2/Superlayer", i);
           double iv0 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/v0", i);
           double ideltanm = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/deltanm", i);
           double itmax = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/tmax", i);
           double idelta_bfield_coefficient = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v1/delta_bfield_coefficient", i);
           double ib1 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b1", i);
           double ib2 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b2", i);
           double ib3 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b3", i);
           double ib4 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b4", i);
           double idistbeta = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/distbeta", i);

           deltanm[iSec-1][iSly-1] = ideltanm;
           v0[iSec-1][iSly-1] = iv0;
           delt_bfield_coefficient[iSec-1][iSly-1] = idelta_bfield_coefficient;

           tmaxsuperlayer[iSec-1][iSly-1] = itmax;

           deltatime_bfield_par1[iSec-1][iSly-1] = ib1;
           deltatime_bfield_par2[iSec-1][iSly-1] = ib2;
           deltatime_bfield_par3[iSec-1][iSly-1] = ib3;
           deltatime_bfield_par4[iSec-1][iSly-1] = ib4;

           distbeta[iSec-1][iSly-1] = idistbeta;

           //
}

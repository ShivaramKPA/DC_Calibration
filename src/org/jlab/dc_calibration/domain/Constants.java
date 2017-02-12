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

public final class Constants {

	protected static final double rad2deg = 180.0 / Math.PI;
	protected static final double deg2rad = Math.PI / 180.0;

	protected static final double cos30 = Math.cos(30.0 / rad2deg);
	protected static final double beta = 1.0;

	// protected static final int nThBinsVz = 6; // [nThBinsVZ][2]
	// protected static final double[] thEdgeVzL = { -2.0, 8.0, 18.0, 28.0, 38.0, 48.0 };
	// protected static final double[] thEdgeVzH = { 2.0, 12.0, 22.0, 32.0, 42.0, 52.0 };

	//protected static final int nThBinsVz = 11; // [nThBinsVZ][2]
	//protected static final double[] thEdgeVzL = { -55.0, -45, -35.0, -25.0, -15.0, -5.0, 5.0, 15.0, 25.0, 35.0, 45.0 };
	//protected static final double[] thEdgeVzH = { -45.0, -35, -25.0, -15.0, -5.0, 5.0, 15.0, 25.0, 35.0, 45.0, 55.0 };

        protected static final int nThBinsVz = 17; // [nThBinsVZ][2]
	protected static final double[] thEdgeVzL = { -55, -45, -35, -25, -20, -15, -10, -6, -2, 2,  6, 10, 15, 20, 25, 35, 45};
	protected static final double[] thEdgeVzH = { -45, -35, -25, -20, -15, -10, -6,  -2,  2, 6, 10, 15, 20, 25, 35, 45, 55};

	protected static final double[] wpdist = { 0.3861, 0.4042, 0.6219, 0.6586, 0.9351, 0.9780 };
	protected static final int nSL = 6;
	protected static final int nSectors = 6;
	protected static final int nLayer = 6;
	protected static final double[] docaBins = { -0.8, -0.6, -0.4, -0.2, -0.0, 0.2, 0.4, 0.6, 0.8 };
	protected static final int nHists = 8;
	protected static final int nTh = 9;
	protected static final double[] thBins = { -60.0, -40.0, -20.0, -10.0, -1.0, 1.0, 10.0, 20.0, 40.0, 60.0 };

	//protected static final String parName[] = { "v0", "deltamn", "tmax1", "tmax2", "distbeta" };
	protected static final String parName[] = { "v0", "deltamn", "tmax", "distbeta" };
	//protected static final double prevFitPars[] = { 62.92e-04, 1.35, 137.67, 148.02, 0.055 };
	protected static final double prevFitPars[] = { 50e-04, 2.0, 137.67, 0.060 };

        //protected static final double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 525.0, 550.0 };
        protected static final double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 600.0, 650.0 };
        protected static final double timeAxisMax[] = {300.0, 300.0, 650.0, 650.0, 650.0, 650.0};
	private Constants() {}
}

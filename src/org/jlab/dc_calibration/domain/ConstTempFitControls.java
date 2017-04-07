/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration.domain;

/**
 *
 * @author kpadhikari
 */
//This file is for temp. purpose. When the GUI his well developed, it will be gone
public final class ConstTempFitControls {
    
    protected static final double[][] defParVal = {
        //v0      deltanm   tMax    distbeta  delta_bfield_coefficient  b1  b2  b3   b4
        {0.0043, 1.45, 155, 0.05, 0.16, 0.4, -2, 10, -6.5}, //SL=1
        {0.0051, 1.55, 175, 0.05, 0.15, 0.4, -2, 10, -6.5}, //SL=2
        {0.0047, 1.50, 300, 0.05, 0.16, 0.4, -2, 10, -6.5}, //SL=3
        {0.0047, 1.50, 320, 0.05, 0.16, 0.4, -2, 10, -6.5}, //SL=4
        {0.0045, 1.38, 479, 0.05, 0.16, 0.4, -2, 10, -6.5}, //SL=5
        {0.0048, 1.56, 506, 0.05, 0.15, 0.4, -2, 10, -6.5}  //SL=6
    };
    
    protected static final boolean[][] fixParStatus = {
        //v0      deltanm   tMax    distbeta  delta_bfield_coefficient  b1  b2  b3   b4
        {false, false, false, false, false, false, false, false, false}, //SL=1
        {false, false, false, false, false, false, false, false, false}, //SL=2
        {false, false, false, false, false, false, false, false, false}, //SL=3
        {false, false, false, false, false, false, false, false, false}, //SL=4
        {false, false, false, false, false, false, false, false, false}, //SL=5
        {false, false, false, false, false, false, false, false, false} //SL=6
    };
    
    protected static final double[][] defParLowLim = {
        //v0      deltanm   tMax    distbeta  delta_bfield_coefficient  b1  b2  b3   b4
        {0.0030, 1.00, 140, 0.03, 0.10, 0.2, -4.5, 1, -9.5}, //SL=1
        {0.0030, 1.00, 140, 0.03, 0.10, 0.2, -4.5, 1, -9.5}, //SL=2
        {0.0030, 1.00, 140, 0.03, 0.10, 0.2, -4.5, 1, -9.5}, //SL=3
        {0.0030, 1.00, 140, 0.03, 0.10, 0.2, -4.5, 1, -9.5}, //SL=4
        {0.0030, 1.00, 140, 0.03, 0.10, 0.2, -4.5, 1, -9.5}, //SL=5
        {0.0030, 1.00, 140, 0.03, 0.10, 0.2, -4.5, 1, -9.5}  //SL=6
    };
    protected static final double[][] defParUprLim = {
        //v0      deltanm   tMax    distbeta  delta_bfield_coefficient  b1  b2  b3   b4
        {0.0065, 2.5, 170, 0.07, 0.20, 0.7, -0.5, 20, -1.5}, //SL=1
        {0.0065, 2.5, 170, 0.07, 0.20, 0.7, -0.5, 20, -1.5}, //SL=2
        {0.0065, 2.5, 170, 0.07, 0.20, 0.7, -0.5, 20, -1.5}, //SL=3
        {0.0065, 2.5, 170, 0.07, 0.20, 0.7, -0.5, 20, -1.5}, //SL=4
        {0.0065, 2.5, 170, 0.07, 0.20, 0.7, -0.5, 20, -1.5}, //SL=5
        {0.0065, 2.5, 170, 0.07, 0.20, 0.7, -0.5, 20, -1.5}  //SL=6
    };    
    
    private ConstTempFitControls() {}
}

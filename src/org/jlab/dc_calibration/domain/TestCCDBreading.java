/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration.domain;

import java.util.Vector;
import static org.jlab.dc_calibration.domain.Constants.nFitPars;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nSectors;

/**
 *
 * @author KPAdhikari
 */
public class TestCCDBreading {
    
    public static void main(String[] args) {        
        ReadT2DparsFromCCDB rdTable = new ReadT2DparsFromCCDB();
        Vector<Integer> Sector, Superlayer;
        Vector <Double> v0, deltanm, tmax, distbeta; 
        Vector<Double> delta_bfield_coefficient, b1, b2, b3, b4;
        v0 = rdTable.v0;
        deltanm = rdTable.deltanm;
        tmax = rdTable.tmax;
        System.out.println("v0: " + v0);
        System.out.println("deltanm: " + deltanm);
        System.out.println("tmax: " + tmax);
        
        double [][][] parsFromCCDB = new double [nSectors][nSL][nFitPars];//nFitPars = 9
        parsFromCCDB = rdTable.parsFromCCDB;
        System.out.println("\n\n\n Now printing par values from the array (filled earlier) .. \n");
        for (int i = 0; i < nSectors; i++) { 
            for (int j = 0; j < nSL; j++) {
                System.out.print(i + " " + j + " "); 
                for (int k = 0; k < nFitPars; k++) {
                    System.out.print(parsFromCCDB[i][j][k] + " ");
                }
                System.out.println(""); 
            }                
        }
    }        
}

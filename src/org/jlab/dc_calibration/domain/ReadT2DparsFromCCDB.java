/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration.domain;

/**
 *
 * @author KPAdhikari
 */
import org.jlab.ccdb.*;
import org.jlab.ccdb.JDBCProvider;
import java.util.Vector;
import static org.jlab.dc_calibration.domain.Constants.nFitPars;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nSectors;

public class ReadT2DparsFromCCDB {

    //private int superlayer;    
    public Vector<Integer> Sector, Superlayer;
    public Vector<Double> v0, deltanm, tmax, distbeta; 
    public Vector<Double> delta_bfield_coefficient, b1, b2, b3, b4;
    public double [][][] parsFromCCDB = new double [nSectors][nSL][nFitPars];//nFitPars = 9
    
    public ReadT2DparsFromCCDB() {
        System.out.println("Hi ... ");
        //JDBCProvider provider = CcdbPackage.createProvider("mysql://localhost")  ;
        JDBCProvider provider = CcdbPackage.createProvider("mysql://clas12reader@clasdb.jlab.org/clas12")  ;
        provider.connect();

        //to check the table exists
        System.out.println("/calibration/dc/time_to_distance/tvsx_devel_v2 exists? - " 
                    + provider.isTypeTableAvailable("/calibration/dc/time_to_distance/tvsx_devel_v2"));
        
        provider.setDefaultVariation("dc_test1");
        Assignment asgmt = provider.getData("/calibration/dc/time_to_distance/tvsx_devel_v2");
        for(Vector<Double> row : asgmt.getTableDouble()){
            for(Double cell: row){
                                System.out.print(cell + " ");
                        }
                        System.out.println(); //next line after a row
        }
        Vector<Double> doubleValues; // System.out.println(doubleValues);
        
        doubleValues = asgmt.getColumnValuesDouble(0); //First column values
        System.out.println("First 2 in Sector column:" + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));
        doubleValues = asgmt.getColumnValuesDouble(1); //Second column values
        System.out.println("First 2 in Superlayer column: " + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));
        doubleValues = asgmt.getColumnValuesDouble(2); //Third column values
        System.out.println("First 2 in v0 column: " + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));
        
        //Now put all the columns in the corresponding Vector members.
        Sector     = asgmt.getColumnValuesInt(0);
        Superlayer = asgmt.getColumnValuesInt(1);
        v0         = asgmt.getColumnValuesDouble(2); 
        deltanm    = asgmt.getColumnValuesDouble(3);
        tmax       = asgmt.getColumnValuesDouble(4);  
        distbeta   = asgmt.getColumnValuesDouble(5); 
        delta_bfield_coefficient = asgmt.getColumnValuesDouble(6);
        b1 = asgmt.getColumnValuesDouble(7);
        b2 = asgmt.getColumnValuesDouble(8);
        b3 = asgmt.getColumnValuesDouble(9);
        b4 = asgmt.getColumnValuesDouble(10);
        
        
        for (int i = 0; i < nSectors; i++) { 
            for (int j = 0; j < nSL; j++) {
                parsFromCCDB[i][j][0] = v0.elementAt(6*i+j);
                parsFromCCDB[i][j][1] = deltanm.elementAt(6*i+j);
                parsFromCCDB[i][j][2] = tmax.elementAt(6*i+j);
                parsFromCCDB[i][j][3] = distbeta.elementAt(6*i+j);
                parsFromCCDB[i][j][4] = delta_bfield_coefficient.elementAt(6*i+j);
                parsFromCCDB[i][j][5] = b1.elementAt(6*i+j);
                parsFromCCDB[i][j][6] = b2.elementAt(6*i+j);
                parsFromCCDB[i][j][7] = b3.elementAt(6*i+j);
                parsFromCCDB[i][j][8] = b4.elementAt(6*i+j);
            }
        }
    }
}

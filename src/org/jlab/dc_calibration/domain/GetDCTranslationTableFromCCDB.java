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
import static org.jlab.dc_calibration.domain.Constants.nChannels;
import static org.jlab.dc_calibration.domain.Constants.nComponents;
import static org.jlab.dc_calibration.domain.Constants.nCrates;
import static org.jlab.dc_calibration.domain.Constants.nFitPars;
import static org.jlab.dc_calibration.domain.Constants.nLayers0to35;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nSectors;
import static org.jlab.dc_calibration.domain.Constants.nSlots;

public class GetDCTranslationTableFromCCDB {

    //private int superlayer;    
    public Vector<Integer> vCrate, vSlot, vChan, vSector, vLayer, vComp, vOrder;
    public int [][][] Crates = new int [nSectors][nLayers0to35][nComponents];
    public int [][][] Slots = new int [nSectors][nLayers0to35][nComponents];
    public int [][][] Channels = new int [nSectors][nLayers0to35][nComponents];
    public int [][][] Sectors = new int [nCrates][nSlots][nChannels];
    public int [][][] Layers = new int [nCrates][nSlots][nChannels];
    public int [][][] Components = new int [nCrates][nSlots][nChannels];
    
    public GetDCTranslationTableFromCCDB() {
        System.out.println("Hi ... from GetDCTranslationTableFromCCDB() constructor ");
        //JDBCProvider provider = CcdbPackage.createProvider("mysql://localhost")  ;
        JDBCProvider provider = CcdbPackage.createProvider("mysql://clas12reader@clasdb.jlab.org/clas12")  ;
        provider.connect();

        //to check the table exists
        System.out.println("/daq/tt/dc exists? - " 
                    + provider.isTypeTableAvailable("/daq/tt/dc"));
        
        provider.setDefaultVariation("default");//for default variation, this line not needed?
        Assignment asgmt = provider.getData("/daq/tt/dc");
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
        vCrate  = asgmt.getColumnValuesInt(0);
        vSlot   = asgmt.getColumnValuesInt(1);
        vChan   = asgmt.getColumnValuesInt(2); 
        vSector = asgmt.getColumnValuesInt(3);
        vLayer  = asgmt.getColumnValuesInt(4);
        vComp   = asgmt.getColumnValuesInt(5);
        vOrder  = asgmt.getColumnValuesInt(6); 
        
        int crate, slot, chan, sec, lay, comp; System.out.println("vector size = " + vCrate.size());
        
        for (int i = 0; i < vCrate.size(); i++) { 
            crate = vCrate.elementAt(i); slot = vSlot.elementAt(i); chan = vChan.elementAt(i);
            sec = vSector.elementAt(i);  lay = vLayer.elementAt(i); comp = vComp.elementAt(i);            
            Crates[sec-1][lay-1][comp-1] = vCrate.elementAt(i);
            Slots[sec-1][lay-1][comp-1] = vSlot.elementAt(i);
            Channels[sec-1][lay-1][comp-1] = vChan.elementAt(i);
            Sectors[crate-41][slot-1][chan] = vSector.elementAt(i); //Crate # starts from 41 & goes upto 58
            Layers[crate-41][slot-1][chan] = vLayer.elementAt(i);
            Components[crate-41][slot-1][chan] = vComp.elementAt(i);
        }

        System.out.println("Debug ..");
    }
}

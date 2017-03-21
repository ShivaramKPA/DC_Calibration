/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration.domain;

import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.JFileChooser;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.service.dc.DCHBEngineCalib;
//import org.jlab.service.dc.DCHBEngineT2DConfig;

/**
 *
 * @author KPAdhikari
 */
public class RunReconstructionCoatjava4 {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */

    
    public RunReconstructionCoatjava4() {
        String iDir = "/Users/kpadhikari/Desktop/BigFls/CLAS12/KPP/";
        String[] results = new String[2];
        results[0] = iDir + "kpp_Decoded_000806_FilesAllComb.hipo"; 
        results[1] = "0"; 
        RunReconstruction(results);
    }
    
    public RunReconstructionCoatjava4(String[] results) {
        RunReconstruction(results);
    }
    
    public void RunReconstruction(String[] results) {
        String iDir = "C:\\Users\\KPAdhikari\\Desktop\\BigFls\\CLAS12\\CalChal\\Cosmics\\";
        iDir = "/Users/kpadhikari/Desktop/BigFls/CLAS12/KPP/";
        //String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/gemc_generated.hipo";
        String inputFile = "C:\\Users\\KPAdhikari\\Desktop\\BigFls\\CLAS12\\FTonReal_3a.0.2_kpp_fulltorus_electron_fixed.hipo";
        inputFile = iDir + "kpp_Decoded_000809_Files1to6Comb.hipo";
 
        inputFile = results[0];

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        //DCHBEngineT2DConfig en = new DCHBEngineT2DConfig();
        DCHBEngineCalib en = new DCHBEngineCalib();//2/14/17
        en.init();
        DCTBEngine en2 = new DCTBEngine();
        en2.init();

        int counter = 0;

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer
        String outputFile = "src/files/DCRBREC.hipo";
        
        String fName = results[0];
        String runNum = fName.substring(41, 47);
        //outputFile = "src/files/kpp_000806_Iter" + results[1] + ".hipo";
        outputFile = "src/files/kpp_"+ runNum + "_Iter" + results[1] + ".hipo";
        outputFile = "src/files/kpp_Cooked_Iter" + results[1] + ".hipo";
        System.out.println("The output hipo file to be produced is: \n\t\t" + outputFile);
        writer.open(outputFile);
        long t1 = 0;
        while (reader.hasEvent()) {

            counter++;

             DataEvent event = reader.getNextEvent();
            if (counter > 0) {
                t1 = System.currentTimeMillis();
            }

            en.processDataEvent(event);

            // Processing TB  
            en2.processDataEvent(event);
            //System.out.println("  EVENT "+counter);
            //if (counter > 50000) {  break;   }
            if(counter%100==0) //%100==0)
                System.out.println("run " + counter + " events");
            writer.writeEvent(event);
        }
        writer.close();
        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));        
    }
    
    //public static void main(String[] args) throws FileNotFoundException, EvioException {
    public static void main(String[] args) throws FileNotFoundException {
        RunReconstructionCoatjava4 rec = new RunReconstructionCoatjava4();
    }

}

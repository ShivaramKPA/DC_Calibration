/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration;

import java.io.FileNotFoundException;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.service.dc.DCHBEngineT2DConfig;

/**
 *
 * @author KPAdhikari
 */
public class RunReconstructionCoatjava4 {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    //public static void main(String[] args) throws FileNotFoundException, EvioException {
    public static void main(String[] args) throws FileNotFoundException {

        //String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/gemc_generated.hipo";
        String inputFile = "C:\\Users\\KPAdhikari\\Desktop\\BigFls\\CLAS12\\FTonReal_3a.0.2_kpp_fulltorus_electron_fixed.hipo";
        //String inputFile = "C:\\Users\\KPAdhikari\\Desktop\\BigFls\\CLAS12\\CalChal\\GemcOp\\out_1234.hipo";
        inputFile = "C:\\Users\\KPAdhikari\\Desktop\\BigFls\\CLAS12\\CalChal\\Cosmics\\clas_000708_090_decodedHallB.hipo";
        inputFile = "C:\\Users\\KPAdhikari\\Desktop\\BigFls\\CLAS12\\CalChal\\Cosmics\\kpp_Decoded_000758_Files0to2Comb.hipo";
        //inputFile = "C:\\Users\\KPAdhikari\\Desktop\\BigFls\\CLAS12\\CalChal\\Cosmics\\kpp_Decoded_000761_Files0to2Comb.hipo";
        //String inputFile = args[0];
        //String outputFile = args[1];

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        DCHBEngineT2DConfig en = new DCHBEngineT2DConfig();
        en.init();
        DCTBEngine en2 = new DCTBEngine();
        en2.init();

        int counter = 0;

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer
        String outputFile = "src/files/DCRBREC.hipo";
        //String outputFile = "src/files/pythia1234.hipo";
        outputFile = "src/files/cosmic_000708_090.hipo";
        outputFile = "src/files/kpp_000758_0to2.hipo";
        //outputFile = "src/files/kpp_000761_0to2.hipo";
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
            if (counter > 25000) {
                break;
            }
            if(counter%100==0)
                System.out.println("run " + counter + " events");
            writer.writeEvent(event);
        }
        writer.close();
        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));

    }

}
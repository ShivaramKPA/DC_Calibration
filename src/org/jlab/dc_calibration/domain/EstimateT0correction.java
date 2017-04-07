/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration.domain;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import static org.jlab.dc_calibration.domain.Constants.nCables;
import static org.jlab.dc_calibration.domain.Constants.nCables6;
import static org.jlab.dc_calibration.domain.Constants.nChannels;
import static org.jlab.dc_calibration.domain.Constants.nComponents;
import static org.jlab.dc_calibration.domain.Constants.nCrates;
import static org.jlab.dc_calibration.domain.Constants.nLayers0to35;
import static org.jlab.dc_calibration.domain.Constants.nRegions;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nSectors;
import static org.jlab.dc_calibration.domain.Constants.nSlots;
import static org.jlab.dc_calibration.domain.Constants.nSlots7;
import static org.jlab.dc_calibration.domain.Constants.tHigh;
import static org.jlab.dc_calibration.domain.Constants.tLow4TmaxFits;
import static org.jlab.dc_calibration.domain.Constants.tMaxSL;
import static org.jlab.dc_calibration.domain.Constants.timeAxisMax;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.service.dc.DCHBEngineCalib;
//import org.jlab.service.dc.DCHBEngineT2DConfig;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
//https://github.com/KPAdhikari/groot/blob/master/src/main/java/org/jlab/groot/demo/MultiGaus.java
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
/**
 *
 * @author KPAdhikari
 */
public class EstimateT0correction {

    private DataBank bnkHits;
    private H1F h1timeAll;
    H1F[] h1timeSL = new H1F[nSL];
    H1F[] h1timeSLn = new H1F[nSL];
    H1F[] h1timeSLn2 = new H1F[nSL];    
    
    H1F[] h1tdcSL = new H1F[nSL];
    H1F[] h1tdcSLn = new H1F[nSL];
    
    H1F[] h1timeTbSL = new H1F[nSL];
    H1F[] h1timeTbSLn = new H1F[nSL];
    
    H1F[][] h1timeRC = new H1F[nRegions][nCables];
    H1F[][][][] h1timeSSSC = new H1F[nSectors][nSL][nSlots7][nCables6];
    
    H1F[] h1timeSLtmax = new H1F[nSL];
    H1F[] h1tdcSLtmax = new H1F[nSL];
    H1F[][][][] h1timeSSSCtmax = new H1F[nSectors][nSL][nSlots7][nCables6];
    
    //int [][][] Crates = new int [nSectors][nLayers0to35][nComponents];
    int[][][] Slots = new int[nSectors][nLayers0to35][nComponents];
    int[][][] Channels = new int[nSectors][nLayers0to35][nComponents];
    //int [][][] Sectors = new int [nCrates][nSlots][nChannels];
    //int [][][] Layers = new int [nCrates][nSlots][nChannels];
    //int [][][] Components = new int [nCrates][nSlots][nChannels];
    public GetDCTranslationTableFromCCDB tt;
    public T0SignalCableMap cableMap;
    FileOutputWriter file = null;
    
    public EstimateT0correction() {        
        String iDir = "/Users/kpadhikari/Desktop/BigFls/CLAS12/KPP/";        
        String[] results = new String[2];        
        results[0] = iDir + "kpp_Rec_000758_Files1to6Comb.hipo"; //"kpp_Decoded_000806_FilesAllComb.hipo"; 
        System.out.println("Debug 11");
        results[1] = "0"; 
        
        ArrayList<String> fileArray = null;
        fileArray.add(results[0]);//iDir + "kpp_Rec_000758_Files1to6Comb.hipo");
        createHists();
        GoEstimate(results, fileArray);
    }
    
    public EstimateT0correction(String[] results, ArrayList<String> fileArray) {
        System.out.println("The input data file is: " + results[1]);
        createHists();
        GoEstimate(results, fileArray);        
    }
    
    private void createHists() {
        int [] nTdcBins = {50, 50, 50, 50, 50, 50};
        int [] nTimeBins = {50, 50, 50, 50, 50, 50};
        double [] tLow = {80.0, 80.0, 80.0, 80.0, 80.0, 80.0};
        //double [] tLow4TmaxFits = {180.0, 180.0, 180.0, 280.0, 480.0, 480.0};
        //double [] tHigh = {380.0, 380.0, 680.0, 780.0, 1080.0, 1080.0};
        
        String hNm = String.format("timeAll");        
        h1timeAll = new H1F(hNm, 200, -200.0, 2000.0);
        String hTtl = String.format("time");
        h1timeAll.setTitleX(hTtl);    h1timeAll.setLineColor(4);
        
        for(int i=0; i<nSL; i++) {
            hNm = String.format("timeSL%d",i+1);
            h1timeSL[i]  = new H1F(hNm, 200, -200.0, 2000.0);
            hTtl = String.format("time for hits in SL=%d",i+1);
            h1timeSL[i].setTitleX(hTtl);
            h1timeSL[i].setLineColor(4);
            //Narrower range (zooming in closer to zero)
            hNm = String.format("timeSL%dn",i+1);
            h1timeSLn[i]  = new H1F(hNm, 100, -40.0, 60.0);
            hTtl = String.format("time for hits in SL=%d",i+1);
            h1timeSLn[i].setTitleX(hTtl);
            h1timeSLn[i].setLineColor(4);
            //Narrower range (zooming in closer to zero)
            hNm = String.format("timeSL%dn",i+1);
            h1timeSLn2[i]  = new H1F(hNm, 100, -10.0, 190.0);
            hTtl = String.format("time for hits in SL=%d",i+1);
            h1timeSLn2[i].setTitleX(hTtl);
            h1timeSLn2[i].setLineColor(4);
            
            hNm = String.format("timeTbSL%d",i+1);
            h1timeTbSL[i]  = new H1F(hNm, 200, -200.0, 2000.0);
            hTtl = String.format("time for hits in SL=%d",i+1);
            h1timeTbSL[i].setTitleX(hTtl);
            h1timeTbSL[i].setLineColor(4);
            //Narrower range (zooming in closer to zero)
            hNm = String.format("timeTbSL%dn",i+1);
            h1timeTbSLn[i]  = new H1F(hNm, 100, -40.0, 160.0);
            hTtl = String.format("time for hits in SL=%d",i+1);
            h1timeTbSLn[i].setTitleX(hTtl);
            h1timeTbSLn[i].setLineColor(4);
            
            hNm = String.format("tdcSL%d",i+1);
            h1tdcSL[i]  = new H1F(hNm, 200, -200.0, 2200.0);
            hTtl = String.format("tdc for hits in SL=%d",i+1);
            h1tdcSL[i].setTitleX(hTtl);
            h1tdcSL[i].setLineColor(4);
            //Narrower range (zooming in closer to zero)
            hNm = String.format("tdcSL%dn",i+1);
            h1tdcSLn[i]  = new H1F(hNm, 50, 0.0, 350.0);
            hTtl = String.format("tdc for hits in SL=%d",i+1);
            h1tdcSLn[i].setTitleX(hTtl);
            h1tdcSLn[i].setLineColor(4);

            
            hNm = String.format("timeSL%dtmax",i+1);
            h1timeSLtmax[i]  = new H1F(hNm, nTimeBins[i], tLow[i], tHigh[i]);
            hTtl = String.format("tdc for hits in SL=%d",i+1);
            h1timeSLtmax[i].setTitleX(hTtl);
            h1timeSLtmax[i].setLineColor(4);
            
            hNm = String.format("tdcSL%dtmax",i+1);
            h1tdcSLtmax[i]  = new H1F(hNm, nTdcBins[i], tLow[i], tHigh[i]);
            hTtl = String.format("tdc for hits in SL=%d",i+1);
            h1tdcSLtmax[i].setTitleX(hTtl);
            h1tdcSLtmax[i].setLineColor(4);
        }
        
        for(int i=0; i<nRegions; i++) {
            for(int j=0; j<nCables; j++) {                
                hNm = String.format("timeReg%dCbl%02d",i+1,j+1);
                //h1timeRC[i][j] = new H1F(hNm, 120, -20.0, 220.0); //Useful for time from TBHits
                h1timeRC[i][j] = new H1F(hNm, 120, 100.0, 340.0);   //Useful for time from HBHits
                hTtl = String.format("time (SL=%d Cable=%02d)",i+1,j+1);
                h1timeRC[i][j].setTitleX(hTtl);
                h1timeRC[i][j].setLineColor(4);
            }
        }
        
        for(int i=0; i<nSectors; i++) {
            for(int j=0; j<nSL; j++) {
                for(int k=0; k<nSlots7; k++) {
                    for(int l=0; l<nCables6; l++) {
                        hNm = String.format("timeS%dS%dS%dCbl%d",i+1,j+1,k+1,l+1);
                        //h1timeSSSC[i][j][k][l] = new H1F(hNm, 120, -20.0, 220.0); //Useful for time from TBHits
                        h1timeSSSC[i][j][k][l] = new H1F(hNm, 150, 50.0, 350.0);   //Useful for time from HBHits
                        hTtl = String.format("time (Sec%d SL%d Slot%d Cable%d)",i+1,j+1,k+1,l+1);
                        h1timeSSSC[i][j][k][l].setTitleX(hTtl);
                        h1timeSSSC[i][j][k][l].setLineColor(1);
                        
                        hNm = String.format("timeS%dS%dS%dCbl%dtmax",i+1,j+1,k+1,l+1);
                        h1timeSSSCtmax[i][j][k][l] = new H1F(hNm, nTimeBins[j], tLow4TmaxFits[j], tHigh[j]);   //Useful for time from HBHits
                        hTtl = String.format("time (Sec%d SL%d Slot%d Cable%d)",i+1,j+1,k+1,l+1);
                        h1timeSSSCtmax[i][j][k][l].setTitleX(hTtl);
                        h1timeSSSCtmax[i][j][k][l].setLineColor(1);
                    }
                }
            }
        }
        
    }
    
    public void GoEstimate(String[] results, ArrayList<String> fileArray) {
        boolean append_to_file = false;
        //FileOutputWriter file = null;

        try {
            file = new FileOutputWriter("src/files/fitParsForT0Estimation.txt", append_to_file);
            file.Write("Sec  SL  Slot  Cable  T0(BkgIntersect) T0err T0N p0  p1  p2  p3  p4(Bkg) xSigmHalf Err(p0  p1  p2  p3  p4)");
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }        
            
        cableMap = new T0SignalCableMap();//To call getCableID(slot, channel);
        /*
        tt = new GetDCTranslationTableFromCCDB();
        
        //Vector<Integer> vCrate, vSlot, vChan, vSector, vLayer, vComp, vOrder;
        //vSlot = tt.vSlot;  vChan = tt.vChan; 
        System.out.println("Slot Chan  (Krishna)"); 
        //for(int i=0;i<10; i++) System.out.println(vSlot.get(i) + " " + vChan.get(i));
         
      
        Slots = tt.Slots;
        Channels = tt.Channels;
        for(int i=0;i<10; i++) {
            for(int j=0;j<10; j++) {
                System.out.println(Slots[0][i][j] + " " + Channels[0][i][j]);
            }            
        }
        */

        String inputFile = results[0];
        System.err.println(" \n[PROCESSING FILE] : " + inputFile);
        int counter = 0;
        long t1 = 0;
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        //readerH.open("src/files/DCRBREC.hipo");
        //readerH.open("src/files/pythia1234.hipo");
        for (String str : fileArray) { //Now reading multiple hipo files.
            System.out.println("Ready to Open & read " + str);
            reader.open(str);
            //}

            t1 = 0;
            while (reader.hasEvent()) {

                counter++;

                DataEvent event = reader.getNextEvent();
                if (counter > 0) {
                    t1 = System.currentTimeMillis();
                }

                
                //processDC_TDC(event);
                
                //if (event.hasBank("TimeBasedTrkg::TBHits") && event.hasBank("TimeBasedTrkg::TBSegments")) {
                if (event.hasBank("HitBasedTrkg::HBHits") && event.hasBank("HitBasedTrkg::HBSegments")) { //For a quick test
                    ProcessTBTracks tbTracks = new ProcessTBTracks(event);
                    if (tbTracks.getNTrks() > 0) {
                        processTBhits(event);
                        //processTBSegments(event);
                        
                        if (event.hasBank("DC::tdc")) {
                            processDC_TDC(event);
                        }
                    }
                }
                //if (counter > 50000) {  break;   }
                if (counter % 100 == 0) {
                    System.out.println("run " + counter + " events");
                }

            }
        }

        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));
    }


    //private void processTBhits(EvioDataEvent event) {
    private void processTBhits(DataEvent event) {
        //timeMapTBHits = new HashMap<Integer, Double>();
        int sec = -1, sl = -1, lay = -1, wire = -1, lay0to35 = -1, cableID=-1; 
        int slot = -1, channel0to95 = -1, region0to2;
        int slot1to7 = -1, cable1to6 = -1;
        double time = 0.0;
        //bnkHits = (DataBank) event.getBank("TimeBasedTrkg::TBHits");
        bnkHits = (DataBank) event.getBank("HitBasedTrkg::HBHits");
        for (int j = 0; j < bnkHits.rows(); j++) {
            sec = bnkHits.getInt("sector", j);
            sl = bnkHits.getInt("superlayer", j);
            lay = bnkHits.getInt("layer", j);//layer goes from 1 to 6 in data
            wire = bnkHits.getInt("wire", j);//wire goes from 1 to 112 in data
            lay0to35 = (sl - 1)* 6 + lay -1;
            /*
            slot = Slots[sec -1 ][lay0to35][wire-1];
            channel0to95 = Channels[sec -1 ][lay0to35][wire-1];
            
            //System.out.println(slot + " " + channel0to95);
            cableID = cableMap.getCableID(slot, channel0to95); 
            */
            region0to2 = (int) ((sl-1)/2);
            slot1to7 = cableMap.getSlotID1to7(wire);
            cable1to6 = cableMap.getCableID1to6(lay, wire);
            
            //System.out.println("Region " + region0to2 + " cableID: " + cableID);
            //System.out.println("sec sl lay wire: " + sec + " " + sl + " " + lay + " " + wire);
            //System.out.println("sec sl slot cable: " + sec + " " + sl + " " + slot1to7 + " " + cable1to6);
            
            time = (double) bnkHits.getFloat("time", j);            
            
            h1timeAll.fill(time);
            h1timeSL[sl-1].fill(time); h1timeSLn[sl-1].fill(time); h1timeSLn2[sl-1].fill(time);
            //h1timeRC[region0to2][cableID-1].fill(time);   //depends on ccdb table         
            h1timeSSSC[sec-1][sl-1][slot1to7-1][cable1to6-1].fill(time); 
            
            h1timeSLtmax[sl-1].fill(time);
            h1timeSSSCtmax[sec-1][sl-1][slot1to7-1][cable1to6-1].fill(time); 
        }
        
        //Just for monitoring purpose
        bnkHits = (DataBank) event.getBank("TimeBasedTrkg::TBHits");
        //bnkHits = (DataBank) event.getBank("HitBasedTrkg::HBHits");
        for (int j = 0; j < bnkHits.rows(); j++) {
            sec = bnkHits.getInt("sector", j);
            sl = bnkHits.getInt("superlayer", j);
            lay = bnkHits.getInt("layer", j);//layer goes from 1 to 6 in data
            wire = bnkHits.getInt("wire", j);//wire goes from 1 to 112 in data
            lay0to35 = (sl - 1)* 6 + lay -1;
            
            region0to2 = (int) ((sl-1)/2);
            slot1to7 = cableMap.getSlotID1to7(wire);
            cable1to6 = cableMap.getCableID1to6(lay, wire);
          
            time = (double) bnkHits.getFloat("time", j);            
            
            h1timeTbSL[sl-1].fill(time); h1timeTbSLn[sl-1].fill(time); 
        }        
    }
    
    
    private void processDC_TDC(DataEvent event) {
        int sec = -1, sl = -1, lay1to36 = -1, lay1to6 = -1, lay0to35 = -1, wire = -1; 
        int region0to2 = -1, slot1to7 = -1, cable1to6 = -1;
        int tdc = 0;
        //bnkHits = (DataBank) event.getBank("TimeBasedTrkg::TBHits");
        bnkHits = (DataBank) event.getBank("DC::tdc");
        for (int j = 0; j < bnkHits.rows(); j++) {
            sec = bnkHits.getInt("sector", j);            
            lay1to36 = bnkHits.getInt("layer", j);//layer goes from 1 to 6 in data
            wire = bnkHits.getInt("component", j);//wire goes from 1 to 112 in data
            lay0to35 = lay1to36 -1;
            lay1to6 = lay0to35%6 + 1;
            sl = (lay0to35 - 1)/6 + 1;
    
            region0to2 = (int) ((sl-1)/2);
            slot1to7 = cableMap.getSlotID1to7(wire);
            cable1to6 = cableMap.getCableID1to6(lay1to6, wire);
            
            //System.out.println("Region " + region0to2 + " cableID: " + cableID);
            //System.out.println("sec sl lay wire: " + sec + " " + sl + " " + lay + " " + wire);
            //System.out.println("sec sl slot cable: " + sec + " " + sl + " " + slot1to7 + " " + cable1to6);
            
            tdc = (int)bnkHits.getInt("TDC", j);            
            
            //h1timeAll.fill(tdc);
            h1tdcSL[sl-1].fill(tdc); h1tdcSLn[sl-1].fill(tdc);     
            h1tdcSLtmax[sl-1].fill(tdc); 
            //h1tdcSSSC[sec-1][sl-1][slot1to7-1][cable1to6-1].fill(time); 
        }
    }

    
    
    public void DrawPlots() {
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.setSize(1 * 400, 1* 400); 
        canvas.divide(1,1);
        canvas.cd(0); 
        canvas.draw(h1timeAll);
        canvas.getPad(0).setTitle("Time");
        canvas.save("src/images/timeAll.png");
        
        EmbeddedCanvas canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);
        for(int i=0; i<nSL; i++) {
            canvas2.cd(i); 
            canvas2.draw(h1timeSL[i]);
            canvas2.getPad(i).setTitle("Time in SL="+(i+1));            
        }
        canvas2.save("src/images/timeSL.png");
        
        canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);
        for(int i=0; i<nSL; i++) {
            canvas2.cd(i); 
            canvas2.draw(h1timeSLn[i]);
            canvas2.getPad(i).setTitle("Time in SL="+(i+1));            
        }
        canvas2.save("src/images/timeSLnearZero.png");

        canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);
        for(int i=0; i<nSL; i++) {
            canvas2.cd(i); 
            canvas2.draw(h1timeTbSL[i]);
            canvas2.getPad(i).setTitle("Time in SL="+(i+1));            
        }
        canvas2.save("src/images/timeTbSL.png");
        
        canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);
        for(int i=0; i<nSL; i++) {
            canvas2.cd(i); 
            canvas2.draw(h1timeTbSLn[i]);
            canvas2.getPad(i).setTitle("Time in SL="+(i+1));            
        }
        canvas2.save("src/images/timeTbSLnearZero.png");

        canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);
        for(int i=0; i<nSL; i++) {
            canvas2.cd(i); 
            canvas2.draw(h1timeSLtmax[i]);
            canvas2.getPad(i).setTitle("Time in SL="+(i+1));            
        }
        canvas2.save("src/images/timeSLwdTmax.png");
       
        canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);
        for(int i=0; i<nSL; i++) {
            canvas2.cd(i); 
            canvas2.draw(h1timeSLn2[i]);
            canvas2.getPad(i).setTitle("Time in SL="+(i+1));            
        }
        canvas2.save("src/images/timeSLnearZeroWider.png");
        
        //TDC plots
        canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);
        for(int i=0; i<nSL; i++) {
            canvas2.cd(i); 
            canvas2.draw(h1tdcSL[i]);
            canvas2.getPad(i).setTitle("TDC in SL="+(i+1));            
        }
        canvas2.save("src/images/tdcSL.png");
        
        canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);
        for(int i=0; i<nSL; i++) {
            canvas2.cd(i); 
            canvas2.draw(h1tdcSLn[i]);
            canvas2.getPad(i).setTitle("Time in SL="+(i+1));            
        }
        canvas2.save("src/images/tdcSLnearZero.png");

        canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);
        for(int i=0; i<nSL; i++) {
            canvas2.cd(i); 
            canvas2.draw(h1tdcSLtmax[i]);
            canvas2.getPad(i).setTitle("Time in SL="+(i+1));            
        }  
        canvas2.save("src/images/tdcSLwdTmax.png");
    }
    
    
    public void DrawPlotsForAllCablesOld() { 
        String iName = null;
        int cableID = 0;
        int nGroupOfSix = 14; //14*6 = 84 cables
        //h1timeRC[region0to2][cableID-1] 
        //H1F[] h1timeSL = new H1F[nSL];
        for(int i=0;i<nRegions; i++) {
            for(int j=0;j<nGroupOfSix; j++) {                    
                    EmbeddedCanvas canvas = new EmbeddedCanvas ();
                    canvas.setSize(3 * 400, 2* 400); 
                    canvas.divide(3,2);
                    for(int pad=0; pad<6; pad++) {
                        cableID = j*7 + (pad+1);
                        canvas.cd(pad); 
                        canvas.draw(h1timeRC[i][j]);
                        canvas.getPad(pad).setTitle("Time in Reg="+(i+1) + " Cable="+ cableID);
                    }
                    iName = String.format("src/images/timeReg%dCable%02d.png",(i+1),cableID);
                    canvas.save(iName);
                }                
            }
    }

    public void DrawPlotsForAllCables() { 
        String iName = null, str = null;
        int cableID = 0;
        double [][][][][] fParSSSCP = new double [nSectors][nSL][nSlots7][nCables6][5];
        double [][][][][] fParSSSCPerr = new double [nSectors][nSL][nSlots7][nCables6][5];
        double [][][][] xSigmoidHalf = new double [nSectors][nSL][nSlots7][nCables6];
        double [][][][] ySigmoidHalf = new double [nSectors][nSL][nSlots7][nCables6];
        double [][][][] slopeSigmoidHalf = new double [nSectors][nSL][nSlots7][nCables6];
        double [][][][] T0atNoiseLevel = new double [nSectors][nSL][nSlots7][nCables6];
        double dX=0.0, dY=0.0, T0error = 0.0;
        //F1D f1 = new F1D("f1", "[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        //F1D f1 = new F1D("f1", "[amp]*gaus(x,[mean],[sigma])", -20.0, 230.0);
        //F1D *fSigmoid = new F1D("sigmoid", "",)
        //F1D f1 = new F1D("f1", "[p0]*gaus(x,[p1],[p2])", -20.0, 230.0);
        //f1.setParameter(0, 120.0);   f1.setParameter(1, 50.0);      f1.setParameter(2, 30.0);
        //TF1 *fa2 = new TF1("fa2","exp([0]+[1]*x)/(1.0+exp([2]+[3]*x)) + [4] ",lowerFitLimit,tMax4T0Hist);
        //F1D f1 = new F1D("f1", "Math.exp([p0]+[p1]*x)/(1.0 + Math.exp([p2] + [p3]*x)) + [p4]", -20.0, 100.0);
        //
        //T0FitFunction f1 = new T0FitFunction("T0FitFunction", -20.1, 140.1); //Useful for time from TBHits
        /*
        T0FitFunction f1 = new T0FitFunction("T0FitFunction", 100.1, 280.1);  //Useful for time from HBHits
        f1.addParameter("p0");        //f1.setParameter(0, 30.0);
        f1.addParameter("p1");        //f1.setParameter(1, -6.0);
        f1.addParameter("p2");        //f1.setParameter(2, 1.8);
        f1.addParameter("p3");        //f1.setParameter(3, .0001);
        f1.addParameter("p4");
        */
        //double [] iPars = {11.5239, -0.00939369, 15.689, -1.181029, 10.0};//For TBHits time
        //double [] iPars = {11.5239, -0.00939369, 150.689, -1.181029, 10.0}; //For HBHits time //didn't work
        double [] iPars = {11.5239, -0.00939369, 15.689, -0.1181029, 10.0}; //For HBHits time
        double [] fPars = {0.0, 0.0, 0.0, 0.0, 0.0};
        /*
        f1.setParameters(iPars);       

        f1.setLineColor(4);
        //DataFitter.fit(f1, h1[i], "Q"); //No options uses error for sigma
        */
        T0FitFunction f2;
        JFrame frame = new JFrame();
        JTabbedPane sectorPanes = new JTabbedPane();
        //h1timeSSSC = new H1F[nSectors][nSL][nSlots7][nCables6];
        for(int i=0;i<nSectors; i++) {
            if(!(i==1)) continue; //For now, only 2nd sector
            JTabbedPane superLayerPanes = new JTabbedPane();
            for(int j=0;j<nSL; j++) {
                JTabbedPane slotPanes = new JTabbedPane();
                for(int k=0;k<nSlots7; k++) {
                    EmbeddedCanvas canvas = new EmbeddedCanvas ();
                    canvas.setSize(3 * 400, 2* 400); 
                    canvas.divide(3,2); 
                    for(int pad=0; pad<nCables6; pad++) {
                                                
                        cableID = pad+1;
                        
                        T0FitFunction f1 = new T0FitFunction("T0FitFunction", 50.1, 280.1); 
                        f1.addParameter("p0");        
                        f1.addParameter("p1");        
                        f1.addParameter("p2");        
                        f1.addParameter("p3");        
                        f1.addParameter("p4");
                        f1.setParameters(iPars); 
                        f1.setLineColor(2);
                        f1.setParLimits(0, 6.5, 9.0);//iPar, min, max
                        
                        DataFitter.fit(f1, h1timeSSSC[i][j][k][pad], "Q");//"Q"); //kp: Q for quiet mode?                        
                        
                        canvas.cd(pad); 
                       
                        //Getting fit parameters to the fPars[] array.
                        System.out.print("Sec" + i + " SL" + j + " Slot" + k + " Cable" + pad + ": ");
                        for(int p=0; p<5; p++) {
                            fPars[p] = f1.getParameter(p); System.out.print(fPars[p] + " ");
                            fParSSSCP[i][j][k][pad][p] = fPars[p];
                            fParSSSCPerr[i][j][k][pad][p] = f1.parameter(p).error();
                        }
                        System.out.println("");                        //f1=null;
                        xSigmoidHalf[i][j][k][pad] = Math.abs(fPars[2]/fPars[3]);
                        ySigmoidHalf[i][j][k][pad] = f1.evaluate(xSigmoidHalf[i][j][k][pad]);
                        //Calculating slope in basic way = dY / dX = Slight change in Y / Slight change in X
                        dX = 0.001;
                        dY = f1.evaluate(dX + xSigmoidHalf[i][j][k][pad]) - ySigmoidHalf[i][j][k][pad];
                        slopeSigmoidHalf[i][j][k][pad] = dY/dX;
                        /*
                        //Writing fit parameters to an output file
                        if (!(file == null)) {
                                str = String.format("%d %d %d %d %4.3f %5.4f %4.3f %5.4f %4.3f",i+1,j+1,k+1,
                                        pad+1, fPars[0], fPars[1], fPars[2], fPars[3], fPars[4]);
                                file.Write(str);                            
                        }
                       */
                        
                        canvas.draw(h1timeSSSC[i][j][k][pad]);
                        //canvas.draw(f1,"same"); 
                        //f2.setParameters(fPars); f2.setLineColor(6); canvas.draw(f2,"same");
                        canvas.getPad(pad).setTitle("Time (S"+(i+1) + " SL" + (j+1) + "Slot" + (k+1) + " Cable"+ cableID + ")");  
                    }
                    iName = String.format("src/images/timeSec%dSL%dSlot%dCable%d.png",(i+1),(j+1),(k+1),cableID);
                    canvas.save(iName); 
                    slotPanes.add(canvas,"Slot "+(k+1));
                    
                }
                superLayerPanes.add(slotPanes,"SuperLayer "+(j+1));
            }
            sectorPanes.add(superLayerPanes,"Sector "+(i+1));
        }
        
       
        double ampExpAtSigHalf = 0.0, expAplusBx = 0.0, expCplusDx = 0.0;
        double constBackground = 0.0, slopeSigHalf = 0.0, T0_BckgIntersect = 0.0;
        double T0_BckgIntersectN = 0.0, denom = 0.0, slopeSigHalfExact = 0.0;
        int cableID1to42 = 0;
        GraphErrors[][] grT0 = new GraphErrors[nSectors][nSL];
        
        if (!(file == null)) {
            for (int i = 0; i < nSectors; i++) {
                if (!(i == 1))  continue; //For now, only 2nd sector
                    EmbeddedCanvas canvas = new EmbeddedCanvas ();
                    canvas.setSize(2 * 600, 3* 400); 
                    canvas.divide(2,3); 
                
                for (int j = 0; j < nSL; j++) {  
                    grT0[i][j] = new GraphErrors();
                    //grT0[i][j].setMarkerColor(j+1);
                    
                    for (int k = 0; k < nSlots7; k++) {                        
                        for (int c = 0; c < nCables6; c++) {                            
                            //sigmoidHalfwayPoint = xSigmoidHalf[i][j][k][c]; //Math.abs(fParSSSCP[i][j][k][c][2]/fParSSSCP[i][j][k][c][3]); 
                            ampExpAtSigHalf = Math.exp(fParSSSCP[i][j][k][c][0] + fParSSSCP[i][j][k][c][1]*xSigmoidHalf[i][j][k][c]);
                            //Slope of sigmoid = Derivative of 1/(1+exp(c+dx)) = -d*exp(c+dx)/(1 + exp(c+dx))^2
                            //What we need is the derivative or slope of the overall fit func., not just sigmoid
                            //Slop of the fit = b*exp(a+bx)* sigmoid + exp(a+bx)*slope_sigmoid
                            //                = exp(a+bx)*( b/ (1 + exp(c+dx)) - d* exp(c+dx)/(1 + exp(c+dx))^2 )
                            expAplusBx = Math.exp(fParSSSCP[i][j][k][c][0] + fParSSSCP[i][j][k][c][1]*xSigmoidHalf[i][j][k][c]);
                            expCplusDx = Math.exp(fParSSSCP[i][j][k][c][2] + fParSSSCP[i][j][k][c][3]*xSigmoidHalf[i][j][k][c]);
                            expCplusDx = 1.0; //Actually, by definition, this is equal to 1 at xSigmoidHalf = -c/d
                            //Realized later that I was doing complicated calculations unnecessarily.
                            
                            //slopeSigHalf = -fParSSSCP[i][j][k][c][3]*expCplusDx/Math.pow((1.0 + expCplusDx),2.0);
                            slopeSigHalfExact = expAplusBx*(fParSSSCP[i][j][k][c][1]/(1.0 + expCplusDx) 
                                    - fParSSSCP[i][j][k][c][3]*expCplusDx/Math.pow(1.0 + expCplusDx, 2.0));
                            slopeSigHalf = slopeSigmoidHalf[i][j][k][c];
                            constBackground = fParSSSCP[i][j][k][c][4];
                            System.out.println("Exact & approx slopes: " + slopeSigHalfExact + " " + slopeSigHalf);
                            //Intersection of the sigmoid slope-line & the horizontal line at y = constBackground is
                            //   given by:      x = xSigmoidHalf + (constBackground - ySigmoidHalf)/SlopeAtSigHalf
                            // And, for now we take that intersection value as T0 (rather than at 10% level)
                            T0_BckgIntersect = xSigmoidHalf[i][j][k][c] + (constBackground - ySigmoidHalf[i][j][k][c])/slopeSigHalf;
                            denom = (fParSSSCP[i][j][k][c][1] + (fParSSSCP[i][j][k][c][1] - fParSSSCP[i][j][k][c][3])*expCplusDx);
                            T0_BckgIntersectN = -(1.0+expCplusDx)/denom - fParSSSCP[i][j][k][c][2]/fParSSSCP[i][j][k][c][3];
                            T0_BckgIntersectN = - 2.0/(2.0*fParSSSCP[i][j][k][c][1] - fParSSSCP[i][j][k][c][3]) 
                                                - fParSSSCP[i][j][k][c][2]/fParSSSCP[i][j][k][c][3]; 
                            T0atNoiseLevel[i][j][k][c] = T0_BckgIntersectN;
                            //Realized later that I was doing complicated calculations unnecessarily.
                            T0error = GetErrorInT0(fParSSSCP[i][j][k][c][1], fParSSSCP[i][j][k][c][2], 
                                                   fParSSSCP[i][j][k][c][3], fParSSSCPerr[i][j][k][c][1], 
                                                   fParSSSCPerr[i][j][k][c][2], fParSSSCPerr[i][j][k][c][3]);
                            
                            cableID1to42 = k*6 + (c+1);
                            if(T0_BckgIntersect > 50.0 && T0_BckgIntersect < 250.0)
                                grT0[i][j].addPoint(cableID1to42, T0_BckgIntersect, 0,0);
                            
                            
                            //Writing fit parameters to an output file
                            str = String.format("%d %d %d %d %4.3f %4.3f %4.3f %4.3f %5.4f %4.3f %5.4f %4.3f %4.3f %4.3f %4.3f %4.3f %4.3f %4.3f",
                                        i+1,j+1,k+1,c+1, T0_BckgIntersectN, T0error, T0_BckgIntersect,
                                        fParSSSCP[i][j][k][c][0], fParSSSCP[i][j][k][c][1], fParSSSCP[i][j][k][c][2], 
                                        fParSSSCP[i][j][k][c][3], fParSSSCP[i][j][k][c][4], xSigmoidHalf[i][j][k][c],
                                        fParSSSCPerr[i][j][k][c][0], fParSSSCPerr[i][j][k][c][1], fParSSSCPerr[i][j][k][c][2], 
                                        fParSSSCPerr[i][j][k][c][3], fParSSSCPerr[i][j][k][c][4]);
                            file.Write(str); 
                        }
                    }
                    grT0[i][j].setMarkerColor(j+1);
                    grT0[i][j].setTitleX("Cable #");
                    grT0[i][j].setTitleY("T0");
                    canvas.cd(j); 
                    canvas.draw(grT0[i][j]);
                    canvas.getPad(j).setTitle("T0s for Sec="+(i+1)+" SL=" + (j+1));
                }
                iName = String.format("src/images/estimatedT0Sec%d.png",(i+1));
                canvas.save(iName); 
            }
            
            try {
                file.Close();
            } catch (IOException ex) {
                Logger.getLogger(EstimateT0correction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screensize.getWidth()*.9),(int)(screensize.getHeight()*.9));
        frame.setLocationRelativeTo(null);
        frame.add(sectorPanes);
        frame.setVisible(true);
    }    
   
    public double GetErrorInT0(double p1, double p2, double p3, double p1err, double p2err, double p3err) { 
        double error = 0.0;
        double dT0dp1Sq = Math.pow(4.0/Math.pow(2.0*p1 - p3, 2.0),    2.0); //dT0/dp1 squared
        double dT0dp2Sq = Math.pow(- 1.0/p3,    2.0);                       //dT0/dp2 squared
        double dT0dp3Sq = Math.pow(- 2.0/Math.pow(2.0*p1 - p3, 2.0) + p2/p3,    2.0);//dT0/dp3 squared
        error = Math.sqrt(dT0dp1Sq*Math.pow(p1err,2.0) + dT0dp2Sq*Math.pow(p1err,2.0) + dT0dp3Sq*Math.pow(p1err,2.0));
        return error;
    }

    public void DrawPlotsForTMaxAllCables() { 
        String iName = null, str = null;
        int cableID = 0;
        double [][][][][] fParSSSCP = new double [nSectors][nSL][nSlots7][nCables6][5];
        double [][][][] xSigmoidHalf = new double [nSectors][nSL][nSlots7][nCables6];
        double [][][][] ySigmoidHalf = new double [nSectors][nSL][nSlots7][nCables6];
        double [][][][] slopeSigmoidHalf = new double [nSectors][nSL][nSlots7][nCables6];
        double dX=0.0, dY=0.0;

        double [] iPars = {11.5239, -0.00939369, 15.689, -0.1181029, 10.0}; //For HBHits time
        double [] fPars = {0.0, 0.0, 0.0, 0.0, 0.0};

        T0FitFunction f2;
        JFrame frame = new JFrame();
        JTabbedPane sectorPanes = new JTabbedPane();
        //h1timeSSSC = new H1F[nSectors][nSL][nSlots7][nCables6];
        for(int i=0;i<nSectors; i++) {
            if(!(i==1)) continue; //For now, only 2nd sector
            JTabbedPane superLayerPanes = new JTabbedPane();
            for(int j=0;j<nSL; j++) {
                JTabbedPane slotPanes = new JTabbedPane();
                for(int k=0;k<nSlots7; k++) {
                    EmbeddedCanvas canvas = new EmbeddedCanvas ();
                    canvas.setSize(3 * 400, 2* 400); 
                    canvas.divide(3,2); 
                    for(int pad=0; pad<nCables6; pad++) {
                                                
                        cableID = pad+1;
                        TmaxFitFunction f1 = new TmaxFitFunction("TmaxFitFunction", tLow4TmaxFits[j], tHigh[j]);//, 50.1, 280.1); 
                        f1.addParameter("p0");        
                        f1.addParameter("p1");        
                        f1.addParameter("p2");        
                        f1.addParameter("p3");        
                        f1.addParameter("p4");
                        f1.setParameters(iPars); 
                        f1.setLineColor(2);
                        f1.setParLimits(0, 6.5, 9.0);//iPar, min, max
                        
                        //DataFitter.fit(f1, h1timeSSSCtmax[i][j][k][pad], "Q");//"Q"); //kp: Q for quiet mode?  
                        
                        canvas.cd(pad); 
                        
                        canvas.draw(h1timeSSSCtmax[i][j][k][pad]);
                    }
                    iName = String.format("src/images/timeTMaxSec%dSL%dSlot%dCable%d.png",(i+1),(j+1),(k+1),cableID);
                    canvas.save(iName); 
                    slotPanes.add(canvas,"Slot "+(k+1));
                    
                }
                superLayerPanes.add(slotPanes,"SuperLayer "+(j+1));
            }
            sectorPanes.add(superLayerPanes,"Sector "+(i+1));
        }
        
       
        
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screensize.getWidth()*.9),(int)(screensize.getHeight()*.9));
        frame.setLocationRelativeTo(null);
        frame.add(sectorPanes);
        frame.setVisible(true);
    }


    //public static void main(String[] args) throws FileNotFoundException, EvioException {
    public static void main(String[] args) throws FileNotFoundException {

        /*
        DialogForT0cor dlg = new DialogForT0cor();
        String[] results = dlg.run();
        System.out.println("resuts:" + results);
        if (results[0] != null) {            
            System.out.println("Input file: " + results[0] + "\nOutput file: " + results[1]);
        }
        */
        System.out.println("Debug 0");
        EstimateT0correction t0c = new EstimateT0correction();
        t0c.DrawPlots();  
        //t0c.DrawPlotsForAllCables();
        System.out.println("Finished drawing the T0 plots ..");
    }

}

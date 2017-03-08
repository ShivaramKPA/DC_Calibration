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
 *  `------'				 @author KPAdhikari
 */
package org.jlab.dc_calibration.domain;

import static org.jlab.dc_calibration.domain.Constants.nHists;
import static org.jlab.dc_calibration.domain.Constants.nLayer;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nSectors;
import static org.jlab.dc_calibration.domain.Constants.nTh;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.parName;
import static org.jlab.dc_calibration.domain.Constants.prevFitPars;
import static org.jlab.dc_calibration.domain.Constants.rad2deg;
import static org.jlab.dc_calibration.domain.Constants.thBins;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;
import static org.jlab.dc_calibration.domain.Constants.wpdist;
import static org.jlab.dc_calibration.domain.Constants.tMaxSL;
import static org.jlab.dc_calibration.domain.Constants.timeAxisMax;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnStrategy;
import org.freehep.math.minuit.MnUserParameters;
import org.jlab.dc_calibration.NTuple.NTuple;
import static org.jlab.dc_calibration.domain.Constants.iSecMax;
import static org.jlab.dc_calibration.domain.Constants.iSecMin;
import static org.jlab.dc_calibration.domain.Constants.nFitPars;
import org.jlab.groot.base.TStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;

public class TimeToDistanceFitter implements ActionListener, Runnable {

    private DataBank bnkHits;
    private DataBank bnkSegs;
    private DataBank bnkSegTrks;
    private DataBank bnkTrks;
    private int nTrks;

    private Map<Coordinate, H1F> hArrWire = new HashMap<Coordinate, H1F>();
    private Map<Coordinate, H1F> h1ThSL = new HashMap<Coordinate, H1F>();
    private Map<Coordinate, H1F> h1timeSlTh = new HashMap<Coordinate, H1F>();
    // Histograms to get ineff. as fn of trkDoca (NtrkDoca = trkDoca/docaMax)
    private Map<Coordinate, H1F> h1trkDoca2Dar = new HashMap<Coordinate, H1F>(); // #############################################################
    private Map<Coordinate, H1F> h1NtrkDoca2Dar = new HashMap<Coordinate, H1F>();// [3] for all good hits, only bad (matchedHitID == -1) and ratio
    private Map<Coordinate, H1F> h1NtrkDocaP2Dar = new HashMap<Coordinate, H1F>();// ############################################################
    private Map<Coordinate, H1F> h1trkDoca3Dar = new HashMap<Coordinate, H1F>(); // ############################################################
    private Map<Coordinate, H1F> h1NtrkDoca3Dar = new HashMap<Coordinate, H1F>();// [3] for all good hits, only bad (matchedHitID == -1) and ratio
    private Map<Coordinate, H1F> h1NtrkDocaP3Dar = new HashMap<Coordinate, H1F>();// ############################################################
    private Map<Coordinate, H1F> h1trkDoca4Dar = new HashMap<Coordinate, H1F>();
    private Map<Coordinate, H1F> h1wire4Dar = new HashMap<Coordinate, H1F>();// no ratio here
    private Map<Coordinate, H1F> h1avgWire4Dar = new HashMap<Coordinate, H1F>();// no ratio here
    private Map<Coordinate, H1F> h1fitChisqProbSeg4Dar = new HashMap<Coordinate, H1F>();
    private Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();
    private Map<Coordinate, H2F> h2timeVtrkDocaVZ = new HashMap<Coordinate, H2F>();

    private Map<Integer, Integer> layerMapTBHits;
    private Map<Integer, Integer> wireMapTBHits;
    private Map<Integer, Double> timeMapTBHits;
    private Map<Integer, Double> trkDocaMapTBHits;
    private Map<Integer, Double> timeResMapTBHits;
    private Map<Integer, Double> BMapTBHits;
    private Map<Integer, Integer> gSegmThBinMapTBSegments;
    private Map<Integer, Double> gSegmAvgWireTBSegments;
    private Map<Integer, Double> gFitChisqProbTBSegments;

    private EmbeddedCanvas sector1;
    private EmbeddedCanvas sector2;
    private EmbeddedCanvas sector3;
    private EmbeddedCanvas sector4;
    private EmbeddedCanvas sector5;
    private EmbeddedCanvas sector6;    
    
    private EmbeddedCanvas sector1n;
    private EmbeddedCanvas sector2n;
    private EmbeddedCanvas sector3n;
    private EmbeddedCanvas sector4n;
    private EmbeddedCanvas sector5n;
    private EmbeddedCanvas sector6n;
    
    private EmbeddedCanvas sector1Profiles;
    private EmbeddedCanvas sector2Profiles;
    private EmbeddedCanvas sector3Profiles;
    private EmbeddedCanvas sector4Profiles;
    private EmbeddedCanvas sector5Profiles;
    private EmbeddedCanvas sector6Profiles;
    private Map<Integer, EmbeddedCanvas> sectorsMap1 = new HashMap<Integer, EmbeddedCanvas>();
    private Map<Integer, EmbeddedCanvas> sectorsMap2 = new HashMap<Integer, EmbeddedCanvas>();
    private Map<Integer, EmbeddedCanvas> sectorsMap3 = new HashMap<Integer, EmbeddedCanvas>();
    private Map<Integer, EmbeddedCanvas> sectorsMapProfiles = new HashMap<Integer, EmbeddedCanvas>();
    private Map<Integer, EmbeddedCanvas> sectorsMapRes = new HashMap<Integer, EmbeddedCanvas>();

    private Map<Coordinate, GraphErrors> htime2DisDocaProfile = new HashMap<Coordinate, GraphErrors>();
    private Map<Coordinate, DCFitFunction> mapOfFitFunctions = new HashMap<Coordinate, DCFitFunction>();
    private Map<Coordinate, MnUserParameters> mapOfFitParameters = new HashMap<Coordinate, MnUserParameters>();
    private Map<Coordinate, double[]> mapOfUserFitParameters = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, double[]> mapOfUserFitParErrors = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, DCFitDrawer> mapOfFitLines = new HashMap<Coordinate, DCFitDrawer>();
    private Map<Coordinate, DCFitDrawerForXDoca> mapOfFitLinesX = new HashMap<Coordinate, DCFitDrawerForXDoca>();


    private Map<Coordinate, DCFitFunctionForEachThBin> mapOfFitFunctionsOld = new HashMap<Coordinate, DCFitFunctionForEachThBin>();
    private Map<Coordinate, MnUserParameters> mapOfFitParametersOld = new HashMap<Coordinate, MnUserParameters>();
    private Map<Coordinate, double[]> mapOfUserFitParametersOld = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, double[]> mapOfUserFitParErrorsOld = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, DCFitDrawer> mapOfFitLinesOld = new HashMap<Coordinate, DCFitDrawer>();
    private Map<Coordinate, DCFitDrawerForXDoca> mapOfFitLinesXOld = new HashMap<Coordinate, DCFitDrawerForXDoca>();

    //private H1F timeRes;
    private H2F testHist;//, timeResVsTrkDoca;
    private Map<Coordinate, H1F> h1timeRes = new HashMap<Coordinate, H1F>();
    private Map<Coordinate, H2F> h2timeResVsTrkDoca = new HashMap<Coordinate, H2F>();
    
    private boolean acceptorder = false;
    private boolean isLinearFit;

    private ArrayList<String> fileArray;
    private EvioDataChain reader;
    private HipoDataSource readerH;
    private OrderOfAction OAInstance;
    private DCTabbedPane dcTabbedPane;

    // MK testing
    private NTuple nTupletimeVtrkDocaVZ;
    double[] tupleVars;

    public TimeToDistanceFitter(ArrayList<String> files, boolean isLinearFit) {
        this.fileArray = files;
        this.reader = new EvioDataChain();
        this.readerH = new HipoDataSource();
        this.dcTabbedPane = new DCTabbedPane("PooperDooper");
        this.isLinearFit = isLinearFit;
        this.nTupletimeVtrkDocaVZ = new NTuple("testData", "Sector:SuperLayer:ThetaBin:Doca:Time");
        this.tupleVars = new double[5];
        createHists();
    }

    public TimeToDistanceFitter(OrderOfAction OAInstance, ArrayList<String> files, boolean isLinearFit) {
        this.fileArray = files;
        this.OAInstance = OAInstance;
        this.reader = new EvioDataChain();
        this.readerH = new HipoDataSource();
        this.dcTabbedPane = new DCTabbedPane("PooperDooper");
        this.nTupletimeVtrkDocaVZ = new NTuple("testData", "Sector:SuperLayer:ThetaBin:Doca:Time");
        this.tupleVars = new double[5];
        this.isLinearFit = isLinearFit;
        createHists();
    }

    private void createHists() {
        testHist = new H2F("A test of superlayer6 at thetabin6", 200, 0.0, 1.0, 150, 0.0, 200.0);
        TStyle.createAttributes();
        String hNm = "";
        String hTtl = "";
        for (int i = 0; i < nSL; i++) {
            for (int j = 0; j < nLayer; j++) {
                for (int k = 0; k < nHists; k++) {
                    hNm = String.format("wireS%dL%dDb%02d", i + 1, j + 1, k);
                    hArrWire.put(new Coordinate(i, j, k), new H1F(hNm, 120, -1.0, 119.0));
                    hTtl = String.format("wire (SL=%d, Layer%d, DocaBin=%02d)", i + 1, j + 1, k);
                    hArrWire.get(new Coordinate(i, j, k)).setTitleX(hTtl);
                    hArrWire.get(new Coordinate(i, j, k)).setLineColor(i + 1);
                }
            }
        }
        for (int i = 0; i < nSL; i++) {
            hNm = String.format("thetaSL%d", i + 1);
            hTtl = "#theta";
            h1ThSL.put(new Coordinate(i), new H1F(hNm, 120, -60.0, 60.0));
            h1ThSL.get(new Coordinate(i)).setTitle(hTtl);
            h1ThSL.get(new Coordinate(0)).setLineColor(i + 1);
        }

        for (int i = 0; i < nSL; i++) {
            for (int k = 0; k < nTh; k++) {
                hNm = String.format("timeSL%dThBn%d", i, k);
                h1timeSlTh.put(new Coordinate(i, k), new H1F(hNm, 200, -10.0, 190.0));
                hTtl = String.format("time (SL=%d, th(%.1f,%.1f)", i + 1, thBins[k], thBins[k + 1]);
                h1timeSlTh.get(new Coordinate(i, k)).setTitleX(hTtl);
                h1timeSlTh.get(new Coordinate(i, k)).setLineColor(i + 1);
            }
        }

        String[] hType = {"all hits", "matchedHitID==-1", "Ratio==Ineff."};// as
        // String[];

        for (int i = 0; i < nSL; i++) {
            for (int k = 0; k < 3; k++) { // These are for histos integrated
                // over all layers
                hNm = String.format("trkDocaS%dH%d", i + 1, k);
                h1trkDoca2Dar.put(new Coordinate(i, k), new H1F(hNm, 90, -0.9, 0.9));
                hNm = String.format("NtrkDocaS%dH%d", i + 1, k);
                h1NtrkDoca2Dar.put(new Coordinate(i, k), new H1F(hNm, 120, -1.2, 1.2));
                hNm = String.format("NtrkDocaPS%dH%d", i + 1, k);
                h1NtrkDocaP2Dar.put(new Coordinate(i, k), new H1F(hNm, 120, 0.0, 1.2));

                if (k == 0) {
                    hTtl = String.format("all hits (SL=%d)", i + 1);
                }
                if (k == 1) {
                    hTtl = String.format("matchedHitID==-1 (SL=%d)", i + 1);
                }
                if (k == 2) {
                    hTtl = String.format("Ineff. (SL=%d)", i + 1);
                }
                h1trkDoca2Dar.get(new Coordinate(i, k)).setTitle(hTtl);
                h1NtrkDoca2Dar.get(new Coordinate(i, k)).setTitle(hTtl);
                h1NtrkDocaP2Dar.get(new Coordinate(i, k)).setTitle(hTtl);

                h1trkDoca2Dar.get(new Coordinate(i, k)).setLineColor(i + 1);
                h1NtrkDoca2Dar.get(new Coordinate(i, k)).setLineColor(i + 1);
                h1NtrkDocaP2Dar.get(new Coordinate(i, k)).setLineColor(i + 1);

            }
            for (int j = 0; j < nLayer; j++) {
                for (int k = 0; k < 3; k++) { // These are for histos integrated
                    // over all theta

                    hNm = String.format("trkDocaS%dL%dH%d", i + 1, j + 1, k);
                    h1trkDoca3Dar.put(new Coordinate(i, j, k), new H1F(hNm, 90, -0.9, 0.9));

                    hNm = String.format("NtrkDocaS%dL%dH%d", i + 1, j + 1, k);
                    h1NtrkDoca3Dar.put(new Coordinate(i, j, k), new H1F(hNm, 120, -1.2, 1.2));

                    hNm = String.format("NtrkDocaPS%dL%dH%d", i + 1, j + 1, k);
                    h1NtrkDocaP3Dar.put(new Coordinate(i, j, k), new H1F(hNm, 120, 0.0, 1.2));

                    if (k == 0) {
                        hTtl = String.format("all hits (SL=%d, Layer%d)", i + 1, j + 1);
                    }
                    if (k == 1) {
                        hTtl = String.format("matchedHitID==-1 (SL=%d, Layer%d)", i + 1, j + 1);
                    }
                    if (k == 2) {
                        hTtl = String.format("Ineff. (SL=%d, Layer%d)", i + 1, j + 1);
                    }

                    h1trkDoca3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h1NtrkDoca3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h1NtrkDocaP3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);

                    h1trkDoca3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);
                    h1NtrkDoca3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);
                    h1NtrkDocaP3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);

                }

                for (int th = 0; th < nTh; th++) {
                    for (int k = 0; k < 3; k++) {
                        hNm = String.format("trkDocaS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
                        h1trkDoca4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 90, -0.9, 0.9));

                        if (k == 0) {
                            hTtl = String.format("all hits (SL=%d, Layer%d, th(%.1f,%.1f))", i + 1, j + 1, thBins[th], thBins[th + 1]);
                        }
                        if (k == 1) {
                            hTtl = String.format("matchedHitID==-1 (SL=%d, Layer%d, th(%.1f,%.1f))", i + 1, j + 1, thBins[th],
                                    thBins[th + 1]);
                        }
                        if (k == 2) {
                            hTtl = String.format("Ineff. (SL=%d, Layer%d, th(%.1f,%.1f))", i + 1, j + 1, thBins[th], thBins[th + 1]);
                        }
                        h1trkDoca3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);
                        h1trkDoca3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);

                    }
                    for (int k = 0; k < 2; k++) {
                        hNm = String.format("wireS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
                        h1wire4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 120, -1.0, 119.0));

                        hTtl = String.format("wire # for %s (SL=%d, Lay%d, th(%.1f,%.1f))", hType[k], i + 1, j + 1, thBins[th],
                                thBins[th + 1]);
                        h1wire4Dar.get(new Coordinate(i, j, th, k)).setTitle(hTtl);
                        h1wire4Dar.get(new Coordinate(i, j, th, k)).setLineColor(i + 1);

                        hNm = String.format("avgWireS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
                        h1avgWire4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 120, -1.0, 119.0));

                        hTtl = String.format("avgWire(SegBnk) for %s (SL=%d, Lay%d, th(%.1f,%.1f))", hType[k], i + 1, j + 1, thBins[th],
                                thBins[th + 1]);
                        h1avgWire4Dar.get(new Coordinate(i, j, th, k)).setTitle(hTtl);
                        h1avgWire4Dar.get(new Coordinate(i, j, th, k)).setLineColor(i + 1);

                        hNm = String.format("fitChisqProbS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
                        h1fitChisqProbSeg4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 90, -0.1, 0.1));

                        hTtl = String.format("fitChisqProbSeg(SegBnk) for %s (SL=%d, Lay%d, th(%.1f,%.1f))", hType[k], i + 1, j + 1,
                                thBins[th], thBins[th + 1]);
                        h1fitChisqProbSeg4Dar.get(new Coordinate(i, j, th, k)).setTitle(hTtl);
                        h1fitChisqProbSeg4Dar.get(new Coordinate(i, j, th, k)).setLineColor(i + 1);

                    }
                }
            }
        }
                
        double dMax;
        //for (int i = 0; i < nSectors; i++) {
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)            
            for (int j = 0; j < nSL; j++) {
                dMax = 2 * wpdist[j];
                for (int k = 0; k < nThBinsVz; k++) { // nThBinsVz theta bins +/-2
                    // deg around 0, 10, 20, 30,
                    // 40, and 50 degs
                    hNm = String.format("Sector %d timeVnormDocaS%dTh%02d", i, j, k);
                    //h2timeVtrkDocaVZ.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.0, 150, 0.0, 200.0));
                    h2timeVtrkDocaVZ.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.0, 150, 0.0, timeAxisMax[j]));

                    hTtl = String.format("time vs. |normDoca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).setTitleX("|normDoca|");
                    h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");

                    hNm = String.format("Sector %d timeVtrkDocaS%dTh%02d", i, j, k);
                    //h2timeVtrkDocaVZ.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.0, 150, 0.0, 200.0));
                    h2timeVtrkDoca.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.2*dMax, 150, 0.0, timeAxisMax[j]));

                    hTtl = String.format("time vs. |Doca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h2timeVtrkDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h2timeVtrkDoca.get(new Coordinate(i, j, k)).setTitleX("|Doca| (cm)");
                    h2timeVtrkDoca.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");                    
                }
            }
        }
        
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)            
            for (int j = 0; j < nSL; j++) {
                    dMax = 2 * wpdist[j];
                    hNm = String.format("timeResS%dSL%d", i, j);
                    h1timeRes.put(new Coordinate(i, j), new H1F(hNm, 200, -1.0, 1.0));
                    hTtl = String.format("residual (cm) (Sec=%d, SL=%d)", i, j + 1);
                    h1timeRes.get(new Coordinate(i, j)).setTitle(hTtl);
                    h1timeRes.get(new Coordinate(i, j)).setTitleX("residual");
                    
                    //h2timeResVsTrkDoca
                    hNm = String.format("timeResVsTrkDocaS%dSL%d", i, j);
                    h2timeResVsTrkDoca.put(new Coordinate(i, j), new H2F(hNm, 200, 0.0, 1.2*dMax, 200, -1.0, 1.0));
                    hTtl = String.format("residual (cm) (Sec=%d, SL=%d)", i, j + 1);
                    h2timeResVsTrkDoca.get(new Coordinate(i, j)).setTitle(hTtl);
                    h2timeResVsTrkDoca.get(new Coordinate(i, j)).setTitleX("|trkDoca|");
                    h2timeResVsTrkDoca.get(new Coordinate(i, j)).setTitleY("residual");
            }
        }
    }
    
    private void createCanvasMaps() {
        //private Map<Integer, EmbeddedCanvas> sectorsMap2 = new HashMap<Integer, EmbeddedCanvas>();
        for (int i = 0; i < nSectors; i++) {
            sectorsMap1.put(i,new EmbeddedCanvas());
            sectorsMap2.put(i,new EmbeddedCanvas());
            sectorsMap3.put(i,new EmbeddedCanvas());
            sectorsMapProfiles.put(i,new EmbeddedCanvas());
            sectorsMapRes.put(i,new EmbeddedCanvas());
            
            sectorsMap1.get(i).setSize(nThBinsVz * 400, nSL * 400);
            sectorsMap1.get(i).divide(nThBinsVz, nSL);
            sectorsMap2.get(i).setSize(nThBinsVz * 400, nSL * 400);
            sectorsMap2.get(i).divide(nThBinsVz, nSL);
            sectorsMap3.get(i).setSize(nThBinsVz * 400, nSL * 400);
            sectorsMap3.get(i).divide(nThBinsVz, nSL);
            sectorsMapProfiles.get(i).setSize(nThBinsVz * 400, nSL * 400);
            sectorsMapProfiles.get(i).divide(nThBinsVz, nSL);
            sectorsMapRes.get(i).setSize(2 * 400, nSL * 400); //col1 for Res, col2 for ResVsDoca
            sectorsMapRes.get(i).divide(2, nSL);
        }
    }
    
    private void createCanvas() {

        sector1 = new EmbeddedCanvas();
        sector2 = new EmbeddedCanvas();
        sector3 = new EmbeddedCanvas();
        sector4 = new EmbeddedCanvas();
        sector5 = new EmbeddedCanvas();
        sector6 = new EmbeddedCanvas();

        sector1n = new EmbeddedCanvas();
        sector2n = new EmbeddedCanvas();
        sector3n = new EmbeddedCanvas();
        sector4n = new EmbeddedCanvas();
        sector5n = new EmbeddedCanvas();
        sector6n = new EmbeddedCanvas();
        
        sector1Profiles = new EmbeddedCanvas();
        sector2Profiles = new EmbeddedCanvas();
        sector3Profiles = new EmbeddedCanvas();
        sector4Profiles = new EmbeddedCanvas();
        sector5Profiles = new EmbeddedCanvas();
        sector6Profiles = new EmbeddedCanvas();

        sector1.setSize(nThBinsVz * 400, nSL * 400);
        sector1.divide(nThBinsVz, nSL);
        sector2.setSize(nThBinsVz * 400, nSL * 400);
        sector2.divide(nThBinsVz, nSL);
        sector3.setSize(nThBinsVz * 400, nSL * 400);
        sector3.divide(nThBinsVz, nSL);
        sector4.setSize(nThBinsVz * 400, nSL * 400);
        sector4.divide(nThBinsVz, nSL);
        sector5.setSize(nThBinsVz * 400, nSL * 400);
        sector5.divide(nThBinsVz, nSL);
        sector6.setSize(nThBinsVz * 400, nSL * 400);
        sector6.divide(nThBinsVz, nSL);
        
        sector1n.setSize(nThBinsVz * 400, nSL * 400);
        sector1n.divide(nThBinsVz, nSL);
        sector2n.setSize(nThBinsVz * 400, nSL * 400);
        sector2n.divide(nThBinsVz, nSL);
        sector3n.setSize(nThBinsVz * 400, nSL * 400);
        sector3n.divide(nThBinsVz, nSL);
        sector4n.setSize(nThBinsVz * 400, nSL * 400);
        sector4n.divide(nThBinsVz, nSL);
        sector5n.setSize(nThBinsVz * 400, nSL * 400);
        sector5n.divide(nThBinsVz, nSL);
        sector6n.setSize(nThBinsVz * 400, nSL * 400);
        sector6n.divide(nThBinsVz, nSL);
        
        sector1Profiles.setSize(nThBinsVz * 400, nSL * 400);
        sector1Profiles.divide(nThBinsVz, nSL);
        sector2Profiles.setSize(nThBinsVz * 400, nSL * 400);
        sector2Profiles.divide(nThBinsVz, nSL);
        sector3Profiles.setSize(nThBinsVz * 400, nSL * 400);
        sector3Profiles.divide(nThBinsVz, nSL);
        sector4Profiles.setSize(nThBinsVz * 400, nSL * 400);
        sector4Profiles.divide(nThBinsVz, nSL);
        sector5Profiles.setSize(nThBinsVz * 400, nSL * 400);
        sector5Profiles.divide(nThBinsVz, nSL);
        sector6Profiles.setSize(nThBinsVz * 400, nSL * 400);
        sector6Profiles.divide(nThBinsVz, nSL);
    }

    protected void processData() {
        int counter = 0;
        int icounter = 0;
        /*
		for (String str : fileArray) {
			reader.addFile(str);
		}
                reader.open();
         */
        //readerH.open("src/files/DCRBREC.hipo");
        //readerH.open("src/files/pythia1234.hipo");
	for (String str : fileArray) {
            System.out.println("Ready to Open & read " + str);
            readerH.open(str);
	}
                
        while (readerH.hasEvent()) {// && icounter < 100

            icounter++;
            if (icounter % 2000 == 0) {
                System.out.println("Processed " + icounter + " events.");
            }
            //EvioDataEvent event = reader.getNextEvent();
            DataEvent event = readerH.getNextEvent();
            /*  //got 'bank not found' message for each event.
			ProcessTBSegmentTrajectory tbSegmentTrajectory = new ProcessTBSegmentTrajectory(event);
			if (tbSegmentTrajectory.getNsegs() > 0) {
				counter++;
			}
             */
            if (event.hasBank("TimeBasedTrkg::TBSegmentTrajectory")) {
                counter++;
            }

            if (event.hasBank("TimeBasedTrkg::TBHits") && event.hasBank("TimeBasedTrkg::TBSegments")) {// && event.hasBank("TimeBasedTrkg::TBSegmentTrajectory") &&
                // event.hasBank("TimeBasedTrkg::TBTracks")
                ProcessTBTracks tbTracks = new ProcessTBTracks(event);
                if (tbTracks.getNTrks() > 0) {
                    processTBhits(event);
                    processTBSegments(event);
                }

            }

        }
        System.out.println(
                "processed " + counter + " Events with TimeBasedTrkg::TBSegmentTrajectory entries from a total of " + icounter + " events");
        saveNtuple();
    }

    //private void processTBhits(EvioDataEvent event) {
    private void processTBhits(DataEvent event) {
        layerMapTBHits = new HashMap<Integer, Integer>();
        wireMapTBHits = new HashMap<Integer, Integer>();
        timeMapTBHits = new HashMap<Integer, Double>();
        trkDocaMapTBHits = new HashMap<Integer, Double>();
        timeResMapTBHits = new HashMap<Integer, Double>();
        BMapTBHits = new HashMap<Integer, Double>();

        bnkHits = (DataBank) event.getBank("TimeBasedTrkg::TBHits");
        for (int j = 0; j < bnkHits.rows(); j++) {
            layerMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("layer", j));
            wireMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("wire", j));
            timeMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("time", j));
            //trkDocaMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getDouble("trkDoca", j));
            //BMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getDouble("B", j));
            trkDocaMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("trkDoca", j));
            timeResMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("timeResidual", j));
            BMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("B", j));
            //System.out.println("B = " + BMapTBHits.get(j));
            int docaBin = (int) (((double) bnkHits.getFloat("trkDoca", j) - (-0.8)) / 0.2);
            if (bnkHits.getInt("sector", j) == 1 && (docaBin > -1 && docaBin < 8)) {
                hArrWire.get(new Coordinate(bnkHits.getInt("superlayer", j) - 1, bnkHits.getInt("layer", j) - 1, docaBin))
                        .fill(bnkHits.getInt("wire", j));
            }
        }
    }

    //private void processTBSegments(EvioDataEvent event) {
    private void processTBSegments(DataEvent event) {

        gSegmThBinMapTBSegments = new HashMap<Integer, Integer>();
        gSegmAvgWireTBSegments = new HashMap<Integer, Double>();
        gFitChisqProbTBSegments = new HashMap<Integer, Double>();

        bnkSegs = (DataBank) event.getBank("TimeBasedTrkg::TBSegments");
        int nHitsInSeg = 0;
        for (int j = 0; j < bnkSegs.rows(); j++) {
            int superlayer = bnkSegs.getInt("superlayer", j);
            int sector = bnkSegs.getInt("sector", j);
            //gSegmAvgWireTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("avgWire", j));
            //gFitChisqProbTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("fitChisqProb", j));

            double thDeg = rad2deg * Math.atan2((double) bnkSegs.getFloat("fitSlope", j), 1.0);
            h1ThSL.get(new Coordinate(bnkSegs.getInt("superlayer", j) - 1)).fill(thDeg);
            for (int h = 1; h <= 12; h++) {
                if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1) {
                    nHitsInSeg++;
                }
            }
            int thBn = -1;
            int thBnVz = -1;
            for (int th = 0; th < nTh; th++) {
                if (thDeg > thBins[th] && thDeg <= thBins[th + 1]) {
                    thBn = th;
                }
            }
            for (int th = 0; th < nThBinsVz; th++) {
                if (thDeg > thEdgeVzL[th] && thDeg <= thEdgeVzH[th]) {
                    thBnVz = th;
                }
            }
            gSegmThBinMapTBSegments.put(bnkSegs.getInt("id", j), thBn);
            double thTmp1 = thDeg;
            double thTmp2 = thDeg - 30.0;
            double docaMax = 2.0 * wpdist[superlayer - 1];
            for (int h = 1; h <= 12; h++) {
                if (nHitsInSeg > 5)// Saving only those with more than 5 hits
                {
                    Double gTime = timeMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
                    Double gTrkDoca = trkDocaMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
                    Double gTimeRes = timeResMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
                    if (gTime == null || gTrkDoca == null) {
                        continue;
                    }
                    if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBn > -1 && thBn < nTh) {
                        h1timeSlTh.get(new Coordinate(superlayer - 1, thBn)).fill(gTime);
                    }
                    
                    if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz > -1 && thBnVz < nThBinsVz) {// && thBnVz < nThBinsVz
                        double docaNorm = gTrkDoca / docaMax;
                        h2timeVtrkDocaVZ.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(Math.abs(docaNorm), gTime);
                        h2timeVtrkDoca.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(Math.abs(gTrkDoca), gTime);
                        if(Math.abs(thDeg)<30.0) {
                            h1timeRes.get(new Coordinate(sector - 1, superlayer - 1)).fill(gTimeRes);
                            h2timeResVsTrkDoca.get(new Coordinate(sector - 1, superlayer - 1)).fill(Math.abs(gTrkDoca), gTimeRes);
                        }
                    }
                    // here I will fill a test histogram of superlay6 and thetabin6
                    if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz == 5 && superlayer == 6) {
                        double docaNorm = gTrkDoca / docaMax;
                        tupleVars[0] = (double) sector;
                        tupleVars[1] = (double) superlayer;
                        tupleVars[2] = (double) thBnVz;
                        tupleVars[3] = Math.abs(docaNorm);
                        tupleVars[4] = gTime;
                        nTupletimeVtrkDocaVZ.addRow(tupleVars);
                        testHist.fill(Math.abs(docaNorm), gTime);
                    }

                }
            }
        }

    }

    protected void drawHistograms() {
        //createCanvas();
        createCanvasMaps();
        //for (int i = 0; i < nSectors; i++) {
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                for (int k = 0; k < nThBinsVz; k++) {
                    htime2DisDocaProfile.put(new Coordinate(i, j, k), h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX());
                    htime2DisDocaProfile.get(new Coordinate(i, j, k)).setTitle("Sector " + i + " timeVtrkDocaS " + j + " Th" + k);
                }
            }
        }

        try {
            // Lets Run the Fitter
            runFitter();    //This one does simultaneous fit over all theta bins
            runFitterOld(); //This one fits in individual theta bins
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Done running fitter
        // lets create lines we just fit
        createFitLines();
        //drawSectorWiseCanvases();
        drawSectorWiseCanvasMaps();
        
        // lets add the canvas's to the pane and draw it.
       addToPane();  //Temprarily disabled 

        // this is temp for testHist
        EmbeddedCanvas test = new EmbeddedCanvas();
        test.cd(0);
        test.draw(testHist);
        test.save("src/images/test.png");
        
        System.out.println("====================================");
        System.out.println("Done with the fitting & drawing ...`");
        System.out.println("====================================");
    }

    protected void drawSectorWiseCanvasMaps() {
        int canvasPlace = 0;
        String Title;
        for (int j = 0; j < nSL; j++) {
            for (int k = 0; k < nThBinsVz; k++) {                
                for (int i = iSecMin; i < iSecMax; i++) { 
                    //Title is common for all 3 types of canvases
                    Title = "Sec="+ (i+1) + " SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                    sectorsMap1.get(i).cd(canvasPlace);  
                    sectorsMap1.get(i).draw(h2timeVtrkDocaVZ.get(new Coordinate(i, j, k))); 
                    sectorsMap1.get(i).draw(mapOfFitLinesOld.get(new Coordinate(i, j, k)), "same");
                    sectorsMap1.get(i).draw(mapOfFitLines.get(new Coordinate(i, j, k)), "same");                    
                    sectorsMap1.get(i).getPad(j * nThBinsVz + k).setTitle(Title);
                    sectorsMap1.get(i).setPadTitlesX("trkDoca/docaMax");
                    sectorsMap1.get(i).setPadTitlesY("time (ns)");

                    sectorsMap2.get(i).cd(canvasPlace);  
                    sectorsMap2.get(i).draw(h2timeVtrkDoca.get(new Coordinate(i, j, k))); 
                    sectorsMap2.get(i).draw(mapOfFitLinesXOld.get(new Coordinate(i, j, k)), "same");
                    sectorsMap2.get(i).draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
                    sectorsMap2.get(i).getPad(j * nThBinsVz + k).setTitle(Title);
                    sectorsMap2.get(i).setPadTitlesX("trkDoca");
                    sectorsMap2.get(i).setPadTitlesY("time (ns)");
                    
                    //Save as SectorsMap2 but without drawing lines with individual theta-bin fits
                    sectorsMap3.get(i).cd(canvasPlace);  
                    sectorsMap3.get(i).draw(h2timeVtrkDoca.get(new Coordinate(i, j, k)));                     
                    sectorsMap3.get(i).draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
                    sectorsMap3.get(i).getPad(j * nThBinsVz + k).setTitle(Title);
                    sectorsMap3.get(i).setPadTitlesX("trkDoca");
                    sectorsMap3.get(i).setPadTitlesY("time (ns)");
                    
                    sectorsMapProfiles.get(i).cd(canvasPlace);  
                    sectorsMapProfiles.get(i).draw(h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX()); 
                    sectorsMapProfiles.get(i).draw(mapOfFitLines.get(new Coordinate(i, j, k)), "same");                                       
                    sectorsMapProfiles.get(i).getPad(j * nThBinsVz + k).setTitle(Title);
                    sectorsMapProfiles.get(i).setPadTitlesX("trkDoca/docaMax");
                    sectorsMapProfiles.get(i).setPadTitlesY("time (ns)");
                    canvasPlace++;
                }
            }   
        }
        
        canvasPlace = 0;
        for (int j = 0; j < nSL; j++) {                   
            for (int i = iSecMin; i < iSecMax; i++) {     
                    Title = "Sec="+ (i+1) + " SL=" + (j + 1);
                    canvasPlace = 2*j;
                    sectorsMapRes.get(i).cd(canvasPlace);  
                    sectorsMapRes.get(i).draw(h1timeRes.get(new Coordinate(i, j)));                   
                    sectorsMapRes.get(i).getPad(canvasPlace).setTitle(Title);
                    sectorsMapRes.get(i).setPadTitlesX("residual (cm)");
                    
                    canvasPlace = 2*j + 1;
                    sectorsMapRes.get(i).cd(canvasPlace);  
                    sectorsMapRes.get(i).getPad(canvasPlace).getAxisZ().setLog(true);
                    sectorsMapRes.get(i).draw(h2timeResVsTrkDoca.get(new Coordinate(i, j)));                   
                    sectorsMapRes.get(i).getPad(canvasPlace).setTitle(Title);
                    sectorsMapRes.get(i).setPadTitlesX("|trkDoca| (cm)");
                    sectorsMapRes.get(i).setPadTitlesY("residual (cm)"); 
                }
            }
        
        for (int i = iSecMin; i < iSecMax; i++) { 
            sectorsMap1.get(i).save("src/images/sector"+(i+1)+".png");
            sectorsMap2.get(i).save("src/images/sector"+(i+1)+"n2.png");
            sectorsMap3.get(i).save("src/images/sector"+(i+1)+"n.png");
            sectorsMapProfiles.get(i).save("src/images/sector"+(i+1)+"Profiles.png");
            sectorsMapRes.get(i).save("src/images/sector"+(i+1)+"Residuals.png");
        }
    }
    protected void drawSectorWiseCanvases() {
        int canvasPlace = 0;
        String Title;
        for (int j = 0; j < nSL; j++) {
            for (int k = 0; k < nThBinsVz; k++) {
                sector1.cd(canvasPlace);
                sector2.cd(canvasPlace);
                sector3.cd(canvasPlace);
                sector4.cd(canvasPlace);
                sector5.cd(canvasPlace);
                sector6.cd(canvasPlace);

                sector1.draw(h2timeVtrkDocaVZ.get(new Coordinate(0, j, k)));
                sector2.draw(h2timeVtrkDocaVZ.get(new Coordinate(1, j, k)));                
                sector3.draw(h2timeVtrkDocaVZ.get(new Coordinate(2, j, k)));
                sector4.draw(h2timeVtrkDocaVZ.get(new Coordinate(3, j, k)));
                sector5.draw(h2timeVtrkDocaVZ.get(new Coordinate(4, j, k)));
                sector6.draw(h2timeVtrkDocaVZ.get(new Coordinate(5, j, k)));
                
                sector1.draw(mapOfFitLinesOld.get(new Coordinate(0, j, k)), "same");
                sector2.draw(mapOfFitLinesOld.get(new Coordinate(1, j, k)), "same");
                sector3.draw(mapOfFitLinesOld.get(new Coordinate(2, j, k)), "same");
                sector4.draw(mapOfFitLinesOld.get(new Coordinate(3, j, k)), "same");
                sector5.draw(mapOfFitLinesOld.get(new Coordinate(4, j, k)), "same");
                sector6.draw(mapOfFitLinesOld.get(new Coordinate(5, j, k)), "same");                
                
                sector1.draw(mapOfFitLines.get(new Coordinate(0, j, k)), "same");
                sector2.draw(mapOfFitLines.get(new Coordinate(1, j, k)), "same");
                sector3.draw(mapOfFitLines.get(new Coordinate(2, j, k)), "same");
                sector4.draw(mapOfFitLines.get(new Coordinate(3, j, k)), "same");
                sector5.draw(mapOfFitLines.get(new Coordinate(4, j, k)), "same");
                sector6.draw(mapOfFitLines.get(new Coordinate(5, j, k)), "same");

                Title = "Sec=1, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector1.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=2, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector2.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=3, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector3.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=4, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector4.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=5, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector5.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=6, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector6.getPad(j * nThBinsVz + k).setTitle(Title);

                sector1.setPadTitlesX("trkDoca/docaMax");
                sector1.setPadTitlesY("time (ns)");
                sector2.setPadTitlesX("trkDoca/docaMax");
                sector2.setPadTitlesY("time (ns)");
                sector3.setPadTitlesX("trkDoca/docaMax");
                sector3.setPadTitlesY("time (ns)");
                sector4.setPadTitlesX("trkDoca/docaMax");
                sector4.setPadTitlesY("time (ns)");
                sector5.setPadTitlesX("trkDoca/docaMax");
                sector5.setPadTitlesY("time (ns)");
                sector6.setPadTitlesX("trkDoca/docaMax");
                sector6.setPadTitlesY("time (ns)");

                
                sector1n.cd(canvasPlace);
                sector2n.cd(canvasPlace);
                sector3n.cd(canvasPlace);
                sector4n.cd(canvasPlace);
                sector5n.cd(canvasPlace);
                sector6n.cd(canvasPlace);

                sector1n.draw(h2timeVtrkDoca.get(new Coordinate(0, j, k)));
                sector2n.draw(h2timeVtrkDoca.get(new Coordinate(1, j, k)));                
                sector3n.draw(h2timeVtrkDoca.get(new Coordinate(2, j, k)));
                sector4n.draw(h2timeVtrkDoca.get(new Coordinate(3, j, k)));
                sector5n.draw(h2timeVtrkDoca.get(new Coordinate(4, j, k)));
                sector6n.draw(h2timeVtrkDoca.get(new Coordinate(5, j, k)));
                
                sector1n.draw(mapOfFitLinesXOld.get(new Coordinate(0, j, k)), "same");
                sector2n.draw(mapOfFitLinesXOld.get(new Coordinate(1, j, k)), "same");
                sector3n.draw(mapOfFitLinesXOld.get(new Coordinate(2, j, k)), "same");
                sector4n.draw(mapOfFitLinesXOld.get(new Coordinate(3, j, k)), "same");
                sector5n.draw(mapOfFitLinesXOld.get(new Coordinate(4, j, k)), "same");
                sector6n.draw(mapOfFitLinesXOld.get(new Coordinate(5, j, k)), "same");                
                
                sector1n.draw(mapOfFitLinesX.get(new Coordinate(0, j, k)), "same");
                sector2n.draw(mapOfFitLinesX.get(new Coordinate(1, j, k)), "same");
                sector3n.draw(mapOfFitLinesX.get(new Coordinate(2, j, k)), "same");
                sector4n.draw(mapOfFitLinesX.get(new Coordinate(3, j, k)), "same");
                sector5n.draw(mapOfFitLinesX.get(new Coordinate(4, j, k)), "same");
                sector6n.draw(mapOfFitLinesX.get(new Coordinate(5, j, k)), "same");

                Title = "Sec=1, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector1n.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=2, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector2n.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=3, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector3n.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=4, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector4n.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=5, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector5n.getPad(j * nThBinsVz + k).setTitle(Title);
                Title = "Sec=6, SL=" + (j + 1) + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
                sector6n.getPad(j * nThBinsVz + k).setTitle(Title);

                sector1n.setPadTitlesX("trkDoca");
                sector1n.setPadTitlesY("time (ns)");
                sector2n.setPadTitlesX("trkDoca");
                sector2n.setPadTitlesY("time (ns)");
                sector3n.setPadTitlesX("trkDoca");
                sector3n.setPadTitlesY("time (ns)");
                sector4n.setPadTitlesX("trkDoca");
                sector4n.setPadTitlesY("time (ns)");
                sector5n.setPadTitlesX("trkDoca");
                sector5n.setPadTitlesY("time (ns)");
                sector6n.setPadTitlesX("trkDoca");
                sector6n.setPadTitlesY("time (ns)");
                
                
                sector1Profiles.cd(canvasPlace);
                sector2Profiles.cd(canvasPlace);
                sector3Profiles.cd(canvasPlace);
                sector4Profiles.cd(canvasPlace);
                sector5Profiles.cd(canvasPlace);
                sector6Profiles.cd(canvasPlace);

                sector1Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(0, j, k)).getProfileX());
                sector1Profiles.draw(mapOfFitLines.get(new Coordinate(0, j, k)), "same");
                sector2Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(1, j, k)).getProfileX());
                sector2Profiles.draw(mapOfFitLines.get(new Coordinate(1, j, k)), "same");
                sector3Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(2, j, k)).getProfileX());
                sector3Profiles.draw(mapOfFitLines.get(new Coordinate(2, j, k)), "same");
                sector4Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(3, j, k)).getProfileX());
                sector4Profiles.draw(mapOfFitLines.get(new Coordinate(3, j, k)), "same");
                sector5Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(4, j, k)).getProfileX());
                sector5Profiles.draw(mapOfFitLines.get(new Coordinate(5, j, k)), "same");
                sector6Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(5, j, k)).getProfileX());
                sector6Profiles.draw(mapOfFitLines.get(new Coordinate(5, j, k)), "same");

                canvasPlace++;

            }
        }
        sector1.save("src/images/sector1.png");
        sector2.save("src/images/sector2.png");
        sector3.save("src/images/sector3.png");
        sector4.save("src/images/sector4.png");
        sector5.save("src/images/sector5.png");
        sector6.save("src/images/sector6.png");

        sector1n.save("src/images/sector1n.png");
        sector2n.save("src/images/sector2n.png");
        sector3n.save("src/images/sector3n.png");
        sector4n.save("src/images/sector4n.png");
        sector5n.save("src/images/sector5n.png");
        sector6n.save("src/images/sector6n.png");

        sector1Profiles.save("src/images/sector1Profiles.png");
        sector2Profiles.save("src/images/sector2Profiles.png");
        sector3Profiles.save("src/images/sector3Profiles.png");
        sector4Profiles.save("src/images/sector4Profiles.png");
        sector5Profiles.save("src/images/sector5Profiles.png");
        sector6Profiles.save("src/images/sector6Profiles.png");
}
    
    protected void addToPane() {
/*
        dcTabbedPane.addCanvasToPane("Sector 1", sector1);
        dcTabbedPane.addCanvasToPane("Sector 2", sector2);
        dcTabbedPane.addCanvasToPane("Sector 3", sector3);
        dcTabbedPane.addCanvasToPane("Sector 4", sector4);
        dcTabbedPane.addCanvasToPane("Sector 5", sector5);
        dcTabbedPane.addCanvasToPane("Sector 6", sector6);
*/
        for (int i = iSecMin; i < iSecMax; i++) { 
            dcTabbedPane.addCanvasToPane("Sector " + (i+1), sectorsMap1.get(i));
        }
        dcTabbedPane.showFrame();

    }

    public void runFitter() throws IOException {

        boolean append_to_file = false;
        FileOutputWriter file = null;

        try {
            file = new FileOutputWriter("src/files/fitParameters.txt", append_to_file);
            file.Write("Sec  SL  v0  deltanm  tMax  distbeta  delta_bfield_coefficient  b1  b2  b3  b4");
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }

        final int nFreePars = 4;

        //Now get the previous fit parameters from CCDB 
        ReadT2DparsFromCCDB rdTable = new ReadT2DparsFromCCDB();
        double [][][] parsFromCCDB = new double [nSectors][nSL][nFitPars];//nFitPars = 9
        double [][][] pars2write = new double [nSectors][nSL][nFitPars];//nFitPars = 9
        parsFromCCDB = rdTable.parsFromCCDB;
        pars2write = rdTable.parsFromCCDB; //Initialize with the CCDB pars
        
        // initial guess of tMax for the 6 superlayers (cell sizes are different for each)
        // This is one of the free parameters (par[2], but fixed for now.)
        //double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 525.0, 550.0 }; //Moved to Constants.java
        // Now start minimization
        double parSteps[] = {0.00001, 0.001, 0.01, 0.0001};
        double pLow[] = {prevFitPars[0] * 0.01, prevFitPars[1] * 0.0, tMaxSL[0] * 0.4, prevFitPars[3] * 0.0};
        double pHigh[] = {prevFitPars[0] * 3.0, prevFitPars[1] * 5.0, tMaxSL[0] * 1.6, prevFitPars[3] * 1.6};

        Map<Coordinate, MnUserParameters> mapTmpUserFitParameters = new HashMap<Coordinate, MnUserParameters>();
        double prevFitPar = 0.0;
        //for (int i = 0; i < nSectors; i++) {
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                pLow[2] = tMaxSL[j] * 0.4;
                pHigh[2] = tMaxSL[j] * 1.6;
                //for (int k = 0; k < nThBinsVz; k++) {
                mapOfFitFunctions.put(new Coordinate(i, j),
                //new DCFitFunction(h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX(), j, k, isLinearFit));
                //new DCFitFunction(h2timeVtrkDocaVZ, i, j, k, isLinearFit));
                new DCFitFunction(h2timeVtrkDocaVZ, i, j, isLinearFit));
                mapOfFitParameters.put(new Coordinate(i, j), new MnUserParameters());
                for (int p = 0; p < nFreePars; p++) {
                    prevFitPar = prevFitPars[p]; //This is value set by hand (for now).
                    prevFitPar = parsFromCCDB[i][j][p]; //this comes from CCDB
                    mapOfFitParameters.get(new Coordinate(i, j)).add(parName[p], prevFitPar, parSteps[p], pLow[p], pHigh[p]);
                }
                //mapOfFitParameters.get(new Coordinate(i, j)).setValue(2, tMaxSL[j]);// tMax for SLth superlayer 
                //mapOfFitParameters.get(new Coordinate(i, j)).fix(0);
                mapOfFitParameters.get(new Coordinate(i, j)).fix(1);
                mapOfFitParameters.get(new Coordinate(i, j)).fix(2);
                //mapOfFitParameters.get(new Coordinate(i, j)).fix(3);
                
                MnMigrad migrad
                        = new MnMigrad(mapOfFitFunctions.get(new Coordinate(i, j)), mapOfFitParameters.get(new Coordinate(i, j)));
                FunctionMinimum min = migrad.minimize();

                if (!min.isValid()) {
                    // try with higher strategy
                    System.out.println("FM is invalid, try with strategy = 2.");
                    MnMigrad migrad2 = new MnMigrad(mapOfFitFunctions.get(new Coordinate(i, j)), min.userState(), new MnStrategy(2));
                    min = migrad2.minimize();
                }
                
                mapTmpUserFitParameters.put(new Coordinate(i, j), min.userParameters());
                double[] fPars = new double[nFreePars];
                double[] fErrs = new double[nFreePars];
                for (int p = 0; p < nFreePars; p++) {
                    fPars[p] = mapTmpUserFitParameters.get(new Coordinate(i, j)).value(parName[p]);
                    fErrs[p] = mapTmpUserFitParameters.get(new Coordinate(i, j)).error(parName[p]);
                }
                
                pars2write[i][j][0] = fPars[0]; pars2write[i][j][1] = fPars[1];
                pars2write[i][j][2] = fPars[2]; pars2write[i][j][3] = fPars[3];
                /*
                if (!(file == null)) {
                    file.Write((i + 1) + "  " + (j + 1) + "  " + fPars[0] + "  "
                            + fPars[1] + "  " + fPars[2] + "  " + fPars[3]);
                }
                */
                mapOfUserFitParameters.put(new Coordinate(i, j), fPars);
                //} // end of nThBinsVz loop
            } // end of superlayer loop
        } // end of sector loop

        for (int i = 0; i < nSectors; i++) { 
            for (int j = 0; j < nSL; j++) {
                if (!(file == null)) {
                    file.Write((i + 1) + "  " + (j + 1) + "  " + pars2write[i][j][0] + "  "
                    + pars2write[i][j][1] + "  " + pars2write[i][j][2] + "  " 
                    + pars2write[i][j][3] + "  " + pars2write[i][j][4] + "  " 
                    + pars2write[i][j][5] + "  " + pars2write[i][j][6] + "  "
                    + pars2write[i][j][7] + "  " + pars2write[i][j][8]);
                }        
            }
        }        
        if (!(file == null)) {
            file.Close();
        }
    }

    public void runFitterOld() throws IOException {

        boolean append_to_file = false;
        FileOutputWriter file = null;

        try {
            file = new FileOutputWriter("src/files/fitParametersForEachThBin.txt", append_to_file);
            file.Write("Sec  SL  ThBin v0  deltanm  tMax  distbeta  delta_bfield_coefficient  b1  b2  b3  b4");
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }

        final int nFreePars = 4;

        // initial guess of tMax for the 6 superlayers (cell sizes are different for each)
        // This is one of the free parameters (par[2], but fixed for now.)
        //double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 525.0, 550.0 }; //Moved to Constants.java
        // Now start minimization
        double parSteps[] = {0.00001, 0.001, 0.01, 0.0001};
        double pLow[] = {prevFitPars[0] * 0.7, prevFitPars[1] * 0.5, tMaxSL[0] * 0.4, prevFitPars[3] * 0.7};
        double pHigh[] = {prevFitPars[0] * 1.3, prevFitPars[1] * 2.5, tMaxSL[0] * 1.6, prevFitPars[3] * 1.3};

        Map<Coordinate, MnUserParameters> mapTmpUserFitParameters = new HashMap<Coordinate, MnUserParameters>();

        //for (int i = 0; i < nSectors; i++) {
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                pLow[2] = tMaxSL[j] * 0.8;
                pHigh[2] = tMaxSL[j] * 1.2;
                for (int k = 0; k < nThBinsVz; k++) {
                    mapOfFitFunctionsOld.put(new Coordinate(i, j, k),
                            new DCFitFunctionForEachThBin(h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX(), j, k, isLinearFit));
                    mapOfFitParametersOld.put(new Coordinate(i, j, k), new MnUserParameters());
                    for (int p = 0; p < nFreePars; p++) {
                        mapOfFitParametersOld.get(new Coordinate(i, j, k)).add(parName[p], prevFitPars[p], parSteps[p], pLow[p], pHigh[p]);
                    }
                    mapOfFitParametersOld.get(new Coordinate(i, j, k)).setValue(2, tMaxSL[j]);// tMax for SLth superlayer
                    mapOfFitParametersOld.get(new Coordinate(i, j, k)).fix(1);
                    MnMigrad migrad
                            = new MnMigrad(mapOfFitFunctionsOld.get(new Coordinate(i, j, k)), mapOfFitParametersOld.get(new Coordinate(i, j, k)));
                    FunctionMinimum min = migrad.minimize();

                    if (!min.isValid()) {
                        // try with higher strategy
                        System.out.println("FM is invalid, try with strategy = 2.");
                        MnMigrad migrad2 = new MnMigrad(mapOfFitFunctionsOld.get(new Coordinate(i, j, k)), min.userState(), new MnStrategy(2));
                        min = migrad2.minimize();
                    }
                    mapTmpUserFitParameters.put(new Coordinate(i, j, k), min.userParameters());
                    double[] fPars = new double[nFreePars];
                    double[] fErrs = new double[nFreePars];
                    for (int p = 0; p < nFreePars; p++) {
                        fPars[p] = mapTmpUserFitParameters.get(new Coordinate(i, j, k)).value(parName[p]);
                        fErrs[p] = mapTmpUserFitParameters.get(new Coordinate(i, j, k)).error(parName[p]);
                    }

                    if (!(file == null)) {
                        file.Write((i + 1) + "  " + (j + 1) + "  " + (k + 1) + "  " + fPars[0]
                                + "  " + fPars[1] + "  " + fPars[2] + "  " + fPars[3]);
                    }
                    mapOfUserFitParametersOld.put(new Coordinate(i, j, k), fPars);
                } // end of nThBinsVz loop
            } // end of superlayer loop
        } // end of sector loop

        if (!(file == null)) {
            file.Close();
        }
    }

    private void createFitLines() {
        double dMax;
        //for (int i = 0; i < nSectors; i++) {
          for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                dMax = 2 * wpdist[j];
                for (int k = 0; k < nThBinsVz; k++) {
                    String title = "timeVsNormDoca Sec=" + (i+1) + " SL=" + (j+1) + " Th=" + k;
                    double maxFitValue = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getDataX(getMaximumFitValue(i, j, k));
                    mapOfFitLines.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k, isLinearFit));
                    mapOfFitLines.get(new Coordinate(i, j, k)).setLineColor(2);
                    mapOfFitLines.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLines.get(new Coordinate(i, j, k)).setLineStyle(4);
                    //mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j, k)));
                    //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                    mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j)));
                    
                    //Now creating lines using parameters from individual theta-bin fits
                    mapOfFitLinesOld.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k, isLinearFit));
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setLineColor(1);
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setLineStyle(1);
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParametersOld.get(new Coordinate(i, j, k)));

                    title = "timeVsTrkDoca Sec=" + (i+1) + " SL=" + (j+1) + " Th=" + k;                    
                    mapOfFitLinesX.put(new Coordinate(i, j, k), new DCFitDrawerForXDoca(title, 0.0, 1.1*dMax, j, k, isLinearFit));
                    mapOfFitLinesX.get(new Coordinate(i, j, k)).setLineColor(2);
                    mapOfFitLinesX.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLinesX.get(new Coordinate(i, j, k)).setLineStyle(4);
                    //mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j, k)));
                    //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                    mapOfFitLinesX.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j)));
                    
                    //Now creating lines using parameters from individual theta-bin fits
                    mapOfFitLinesXOld.put(new Coordinate(i, j, k), new DCFitDrawerForXDoca(title, 0.0, 1.1*dMax, j, k, isLinearFit));
                    mapOfFitLinesXOld.get(new Coordinate(i, j, k)).setLineColor(1);
                    mapOfFitLinesXOld.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLinesXOld.get(new Coordinate(i, j, k)).setLineStyle(1);
                    mapOfFitLinesXOld.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParametersOld.get(new Coordinate(i, j, k)));
                }
            }
        }

    }

    public int getMaximumFitValue(int i, int j, int k) {
        int maxOutput = 0;
        int nX = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getXAxis().getNBins();
        int nY = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getYAxis().getNBins();
        double[][] mybuff = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getContentBuffer();
        for (int iX = 0; iX < nX; iX++) {
            for (int iY = 0; iY < nY; iY++) {
                if (mybuff[iX][iY] != 0.0) {
                    maxOutput = iX;
                }
            }
        }
        return maxOutput;

    }

    public void actionPerformed(ActionEvent e) {
        OAInstance.buttonstatus(e);
        acceptorder = OAInstance.isorderOk();
        JFrame frame = new JFrame("JOptionPane showMessageDialog example1");
        if (acceptorder) {
            JOptionPane.showMessageDialog(frame, "Click OK to start processing the time to distance fitting...");
            processData();
            drawHistograms();
            // DCTabbedPane test = new DCTabbedPane();
        } else {
            System.out.println("I am red and it is not my turn now ;( ");
        }
    }

    @Override
    public void run() {

        processData();
        drawHistograms();

    }

    private void saveNtuple() {
        nTupletimeVtrkDocaVZ.write("src/files/pionTest.evio");
    }

    public static void main(String[] args) {
        String fileName;
        String fileName2;

        fileName = "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.evio";
        // fileName2 =
        // "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.2.evio";
        ArrayList<String> fileArray = new ArrayList<String>();
        // fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/elec/cookedFiles/out_out_1.evio");
        // fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/elec/cookedFiles/out_out_10.evio");
        // fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/elec/cookedFiles/out_out_2.evio");
        // fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/elec/cookedFiles/out_out_3.evio");

        // fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_1.evio");
        // fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_10.evio");
        // fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_2.evio");
        // fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_4.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_1.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_10.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_2.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_4.evio");

        // fileArray.add(fileName);
        // fileArray.add(
        // "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.evio");
        // fileArray.add(
        // "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.3.evio");
        //
        // fileArray.add(
        // "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.4.evio");
        //
        // fileArray.add(
        // "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.5.evio");
        TimeToDistanceFitter rd = new TimeToDistanceFitter(fileArray, true);

        rd.processData();
        // System.out.println(rd.getMaximumFitValue(5, 5, 5) + " output");
        rd.drawHistograms();

    }
}

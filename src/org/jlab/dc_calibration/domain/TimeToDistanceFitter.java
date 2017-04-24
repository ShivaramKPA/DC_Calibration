/*              
 * 		@author KPAdhikari
 *              @author m.c.kunkel
 */
package org.jlab.dc_calibration.domain;

import java.awt.Dimension;
import java.awt.Toolkit;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnStrategy;
import org.freehep.math.minuit.MnUserParameters;
import org.jlab.dc_calibration.NTuple.NTuple;
import static org.jlab.dc_calibration.domain.Constants.iSecMax;
import static org.jlab.dc_calibration.domain.Constants.iSecMin;
import static org.jlab.dc_calibration.domain.Constants.nFitPars;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz2;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH2;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL2;
import org.jlab.groot.base.TStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.hipo.HipoDataSource;

public class TimeToDistanceFitter implements ActionListener, Runnable {

    private DataBank bnkHits;
    private DataBank bnkSegs;
    private DataBank bnkSegTrks;
    private DataBank bnkTrks;
    private int nTrks;
    private int colIndivFit = 1, colSimulFit = 4;
    int [][][] segmentIDs; //[nTrks][3][2] //3 for crosses per track, 2 for segms per cross.
    double [][][] trkChi2;//Size equals the # of tracks for the event
    int nTracks = 0;

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
    //private Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();
    public Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();//made it public - to be able to access it from SliceViewer
    private Map<Coordinate, H2F> h2timeVtrkDocaVZ = new HashMap<Coordinate, H2F>();
    private Map<Coordinate, H2F> h2timeFitResVtrkDoca = new HashMap<Coordinate, H2F>();//time - fitLine
    private Map<Coordinate, H1F> h1timeFitRes = new HashMap<Coordinate, H1F>();  //time - fitLine
    
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

    private Map<Coordinate, SimpleH3D> h3BTXmap = new HashMap<Coordinate, SimpleH3D>();
    private Map<Coordinate, H2F> h2TXprojOfh3BTX = new HashMap<Coordinate, H2F>(); 
    private Map<Coordinate, H2F> h2TXprojOfh3BTXinvErr = new HashMap<Coordinate, H2F>(); //To check 1/stat-err distribution on XT-plane
    private Map<Coordinate, DCFitFunctionWithSimpleH3D> mapOfFitFunctionsXTB = new HashMap<Coordinate, DCFitFunctionWithSimpleH3D>();
    private Map<Coordinate, MnUserParameters> mapOfFitParametersXTB = new HashMap<Coordinate, MnUserParameters>();
    private Map<Coordinate, double[]> mapOfUserFitParametersXTB = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, double[]> mapOfUserFitParErrorsXTB = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, DCFitDrawerForXDocaXTB> mapOfFitLinesXTB = new HashMap<Coordinate, DCFitDrawerForXDocaXTB>();
    
    
    private H1F h1bField;
    private H1F h1fitChisqProb, h1fitChi2Trk, h1fitChi2Trk2, h1ndfTrk, h1zVtx;
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
        h1bField = new H1F("Bfield",150,0.0,1.5);
        h1bField.setTitle("B field");        h1bField.setLineColor(2);
        h1fitChisqProb = new H1F("fitChisqProb",120,0.0,1.2);
        h1fitChisqProb.setTitle("fitChisqProb");        h1fitChisqProb.setLineColor(2);
        h1fitChi2Trk = new H1F("fitChi2Trk",100,0.0,8000);//1.2);
        h1fitChi2Trk.setTitle("fitChi2Trk");        h1fitChi2Trk.setLineColor(2);
        h1fitChi2Trk2 = new H1F("fitChi2Trk2",100,0.0,8000);//To see how a zVtx cut affects
        h1fitChi2Trk2.setTitle("fitChi2Trk2");        h1fitChi2Trk2.setLineColor(2);
        h1ndfTrk = new H1F("ndfTrk",80,5.0,45);
        h1ndfTrk.setTitle("ndfTrk");        h1ndfTrk.setLineColor(2);
        h1zVtx = new H1F("zVtx", 200, -50.0, 50.0);
        h1zVtx.setTitle("zVtx");        h1zVtx.setLineColor(2);
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
                    hNm = String.format("Sector %d timeFitResVtrkDocaS%dTh%02d", i, j, k);
                    h2timeFitResVtrkDoca.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.2*dMax, 150, -timeAxisMax[j]/2, timeAxisMax[j]/2));
                    hTtl = String.format("time - fit vs. |Doca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h2timeFitResVtrkDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h2timeFitResVtrkDoca.get(new Coordinate(i, j, k)).setTitleX("|Doca| (cm)");
                    h2timeFitResVtrkDoca.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");  

                    hNm = String.format("Sector %d timeFitResS%dTh%02d", i, j, k);
                    h1timeFitRes.put(new Coordinate(i, j, k), new H1F(hNm, 150, -timeAxisMax[j]/2, timeAxisMax[j]/2));
                    hTtl = String.format("time - fit (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h1timeFitRes.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h1timeFitRes.get(new Coordinate(i, j, k)).setTitleX("Time (ns)");  
                }
                //SimpleH3D> h3BTXmap
                for (int k = 0; k < nThBinsVz2; k++) { 
                    //hNm = String.format("Sector %d BvTvXS%dTh%02d", i, j, k);
                    h3BTXmap.put(new Coordinate(i, j, k), new SimpleH3D(100, 0.0, 1.2*dMax, 100, 0.0, timeAxisMax[j], 60, 0.0, 1.2));
                    hNm = String.format("TvXSec%dSL%dTh%02d", i, j, k);
                    h2TXprojOfh3BTX.put(new Coordinate(i, j, k), new H2F(hNm,100, 0.0, 1.2*dMax, 100, 0.0, timeAxisMax[j]));
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
    
    private void createCanvas() { //No more used ??

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
        int ndf = -1;
        double chi2 = -1.0, Vtx0_z = -10000.0;
        
//       
//		for (String str : fileArray) {
//			reader.addFile(str);
//		}
//                reader.open();
//         
//        //readerH.open("src/files/DCRBREC.hipo");

        for (String str : fileArray) { //Now reading multiple hipo files.
            System.out.println("Ready to Open & read " + str);
            readerH.open(str);

            while (readerH.hasEvent()) {// && icounter < 100

                icounter++;
                if (icounter % 2000 == 0) {
                    System.out.println("Processed " + icounter + " events.");
                }
                //EvioDataEvent event = reader.getNextEvent();
                DataEvent event = readerH.getNextEvent();
//                  //got 'bank not found' message for each event.
//			ProcessTBSegmentTrajectory tbSegmentTrajectory = new ProcessTBSegmentTrajectory(event);
//			if (tbSegmentTrajectory.getNsegs() > 0) {
//				counter++;
//			}
//                 
                if (event.hasBank("TimeBasedTrkg::TBSegmentTrajectory")) {
                    counter++;
                }

                if (event.hasBank("TimeBasedTrkg::TBHits") && event.hasBank("TimeBasedTrkg::TBSegments")//) {// && event.hasBank("TimeBasedTrkg::TBSegmentTrajectory") &&
                    && event.hasBank("TimeBasedTrkg::TBTracks") ) {
                    
                    processTBTracksAndCrosses(event); //Identify corresponding segments (4/13/17)
                    
                    ProcessTBTracks tbTracks = new ProcessTBTracks(event);
                    if (tbTracks.getNTrks() > 0) {
                        //processTBhits(event);
                        //processTBSegments(event);

                        //===========
                        bnkTrks = (DataBank) event.getBank("TimeBasedTrkg::TBTracks");
                        for (int j = 0; j < bnkTrks.rows(); j++) {
                            chi2 = (double) bnkTrks.getFloat("chi2", j);
                            ndf = bnkTrks.getInt("ndf", j);
                            Vtx0_z = (double) bnkTrks.getFloat("Vtx0_z", j);
                            h1fitChi2Trk.fill(chi2);
                            h1ndfTrk.fill(1.0*ndf);
                            h1zVtx.fill(Vtx0_z);
                            if(Vtx0_z > -3.0 && Vtx0_z<5.0) ///if(Math.abs(Vtx0_z)<10.0) 
                                h1fitChi2Trk2.fill(chi2);
                        }
                        //===========
                    }
                }

            }
        }
        System.out.println(
                "processed " + counter + " Events with TimeBasedTrkg::TBSegmentTrajectory entries from a total of " + icounter + " events");
        saveNtuple();
    }

    private void processTBTracksAndCrosses(DataEvent event) {
        int [][] crossIDs;//[nTrk][3], with [3] for 3 crosses from R1/2/3
        double [] trkFitChi2; //[nTrk]
        
        int [] crossID = {-1, -1, -1};
        //int [][][] segmentIDs; //[nTracks][3][2] //3 for crosses per track, 2 for segms per cross. //Now global
        //double [] trkChi2;//Size equals the # of tracks for the event //Now global
        //int nTracks = 0; //Now global
        int segmID1 = -1, segmID2 = -1, id = -1;
        
        double chi2 = 1000000.0;
        boolean hasTracks = event.hasBank("TimeBasedTrkg::TBTracks");
        boolean hasCrosses = event.hasBank("TimeBasedTrkg::TBCrosses");
        boolean hasSegments = event.hasBank("TimeBasedTrkg::TBSegments");
        DataBank bnkTracks, bnkCrosses, bnkSegments;
        if(hasTracks) {        
            bnkTracks = (DataBank) event.getBank("TimeBasedTrkg::TBTracks");
            nTracks = bnkTracks.rows();    
            crossIDs = new int [nTracks][3];
            trkFitChi2 = new double [nTracks];
            //# of valid segments: nTracks*3*2, because each will have 3 crosses, and each cross has 2 segments.
            segmentIDs = new int [nTracks][3][2];
            trkChi2 = new double [nTracks][3][2];
            for (int j = 0; j < bnkTracks.rows(); j++) {
                crossIDs[j][0] = bnkTracks.getInt("Cross1_ID", j);//Region 1
                crossIDs[j][1] = bnkTracks.getInt("Cross2_ID", j);//R2
                crossIDs[j][2] = bnkTracks.getInt("Cross3_ID", j);//R3
                trkFitChi2[j] = (double) bnkTracks.getFloat("chi2", j);
            }

            if (hasCrosses) {
                bnkCrosses = (DataBank) event.getBank("TimeBasedTrkg::TBCrosses");
                for (int i = 0; i < bnkCrosses.rows(); i++) {
                    id = bnkCrosses.getInt("id", i);
                    segmID1 = bnkCrosses.getInt("Segment1_ID", i);
                    segmID2 = bnkCrosses.getInt("Segment2_ID", i);
                    for (int j = 0; j < nTracks; j++) {
                        for (int k = 0; k < 3; k++) {                            
                            if (id == crossIDs[j][k]) {
//                                System.out.println("nTrks=" + nTracks
//                                        + " CrossIndex=" + k + " id = " + bnkCrosses.getInt("id", i)
//                                        + " region = " + bnkCrosses.getInt("region", i));
                                segmentIDs[j][k][0] = segmID1;
                                segmentIDs[j][k][1] = segmID2;
                                trkChi2[j][k][0] = trkFitChi2[j];
                                trkChi2[j][k][1] = trkFitChi2[j];
//                                System.out.println("crossID trkChi2  seg1id seg2id: " + k + " " + chi2 
//                                        + " " + segmentIDs[j][k][0] + " " + segmentIDs[j][k][1]);
                            }
                        }
                    }
                }
            }
            
            processTBhits(event);
            if(hasSegments)
                processTBSegments(segmentIDs, trkChi2, event);
        }
    }
    
    //private void processTBhits(EvioDataEvent event) {
    private void processTBhits(DataEvent event) {
        double bFieldVal = 0.0;
        int sector = -1, superlayer = -1;
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
            bFieldVal =  (double) bnkHits.getFloat("B", j);
            sector = bnkHits.getInt("sector", j);
            superlayer = bnkHits.getInt("superlayer", j);
            BMapTBHits.put(bnkHits.getInt("id", j), bFieldVal);
            if(superlayer==3 || superlayer==4) h1bField.fill(bFieldVal); //for a quick look
            //System.out.println("S = " + sector + "superlayer = " + superlayer + " B = " + bFieldVal);
            //System.out.println("B = " + BMapTBHits.get(j));
            int docaBin = (int) (((double) bnkHits.getFloat("trkDoca", j) - (-0.8)) / 0.2);
            if (bnkHits.getInt("sector", j) == 1 && (docaBin > -1 && docaBin < 8)) {
                hArrWire.get(new Coordinate(bnkHits.getInt("superlayer", j) - 1, bnkHits.getInt("layer", j) - 1, docaBin))
                        .fill(bnkHits.getInt("wire", j));
            }
        }
    }

    //private void processTBSegments(EvioDataEvent event) {
    private void processTBSegments(int [][][] segmIDs, double [][][] trkChi2, DataEvent event) {
        boolean validSegm = false;
        double trkChiSq = 1000000.0;//Just giving a very big value of trk-fit-chi-square (for bad fits, its a big #)
        gSegmThBinMapTBSegments = new HashMap<Integer, Integer>();
        gSegmAvgWireTBSegments = new HashMap<Integer, Double>();
        gFitChisqProbTBSegments = new HashMap<Integer, Double>();

        bnkSegs = (DataBank) event.getBank("TimeBasedTrkg::TBSegments");
        int nHitsInSeg = 0, idSeg = -1;
        for (int j = 0; j < bnkSegs.rows(); j++) {
            int superlayer = bnkSegs.getInt("superlayer", j);
            int sector = bnkSegs.getInt("sector", j);
            //System.out.println("superlayer sector" + superlayer + " " + sector);

            //Check if any of these segments matches with those associated with the available tracks
            //   Else continue
            idSeg = bnkSegs.getInt("id", j);
            validSegm = false;
            //System.out.println("nTracks: " + nTracks);
            for (int i = 0; i < nTracks; i++) {
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 2; l++) {  
                        //System.out.println("idSeg  segTrks: " + idSeg + " " + segmentIDs[i][k][l]);
                       if(idSeg == segmIDs[i][k][l]) { //segmentIDs[i][k][l]
                           validSegm = true;
                           trkChiSq = trkChi2[i][k][l];
                       }
                    }                    
                }                
            }
            if(validSegm == false) continue;
            if(trkChiSq > 2000.0) continue;
            
            //int [][][] segmentIDs; //[nTrks][3][2] //3 for crosses per track, 2 for segms per cross. //Now global
            //double [] trkChi2;//Size equals the # of tracks for the event //Now global
            //int nTracks = 0; //Now global
            
            
            //It turns out there were cases of sector==1 & superlayer ==1 in pass2 data (4/11/17)
            //   causing the program to crash. Therefore I had to put the following line.
            if(sector < 1 || superlayer < 1) continue;
            
            //gSegmAvgWireTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("avgWire", j));
            //gFitChisqProbTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("fitChisqProb", j));
            h1fitChisqProb.fill((double) bnkSegs.getFloat("fitChisqProb", j));
            
            double thDeg = rad2deg * Math.atan2((double) bnkSegs.getFloat("fitSlope", j), 1.0);
            //System.out.println("superlayer thDeg " + superlayer + " " + thDeg);
            h1ThSL.get(new Coordinate(bnkSegs.getInt("superlayer", j) - 1)).fill(thDeg);
            for (int h = 1; h <= 12; h++) {
                if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1) {
                    nHitsInSeg++;
                }
            }
            int thBn = -1, thBnVz = -1, thBnVz2 = -1;
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
            //3/26/17: following bins are of size 2 degrees each.
            for (int th = 0; th < nThBinsVz2; th++) {
                if (thDeg > thEdgeVzL2[th] && thDeg <= thEdgeVzH2[th]) {
                    thBnVz2 = th;
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
                    Double gBfield = BMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
                    if (gTime == null || gTrkDoca == null && gBfield == null) {
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
                    if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz2 > -1 && thBnVz2 < nThBinsVz2) {
                        //System.out.print("B-fill: "+ sector+" "+superlayer+" "+thBnVz2+" "+gTrkDoca+" "+gTime+" "+gBfield);
                        h3BTXmap.get(new Coordinate(sector - 1, superlayer - 1, thBnVz2)).fill(Math.abs(gTrkDoca), gTime, gBfield);
                        //System.out.println("  la la ..");
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

    protected void runFitterAndDrawPlots(JFrame frame, JTextArea textArea, int Sec, int SL, 
            int xMeanErrorType, double xNormLow, double xNormHigh,
            boolean [] fixIt, double [][] pLow, double [][] pInit, double [][] pHigh) {
        System.out.println(String.format("%s %d %d %d %2.1f %2.1f", 
                "Selected values of Sector Superlayer errorType xNorm(Min,Max) are:",
                Sec,SL,xMeanErrorType, xNormLow, xNormHigh));
        int iSL = SL - 1;
        System.out.println("parLow   parInit    parHigh    FixedStatus"); 
        for (int i = 0; i < nFitPars; i++) {
            System.out.println(String.format("%5.4f    %5.4f   %5.4f   %b",pLow[iSL][i],
                    pInit[iSL][i], pHigh[iSL][i], fixIt[i])); 
        }
        
        createCanvasMaps(); 
        System.out.println("Called createCanvasMaps(); ");
//        drawQuickTestPlots();
//        System.out.println("Called drawQuickTestPlots();");
        
        try {
            runFitterNew(textArea, Sec, SL, xMeanErrorType, xNormLow, xNormHigh, fixIt, pLow, pInit, pHigh);
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Called runFitterNew(Sec, SL, fixIt, pLow, pInit, pHigh);");
        createFitLinesNew(Sec, SL);
        System.out.println("Fit lines are prepared.");
        drawFitLinesNew(frame, Sec, SL);
        
    }
    
    protected void runFitterNew(JTextArea textArea, int Sec, int SL, 
            int xMeanErrorType, double xNormLow, double xNormHigh, boolean [] fixIt, 
            double [][] pLow, double [][] pInit, double [][] pHigh)  throws IOException {
        int iSec = Sec - 1, iSL = SL - 1;
        boolean append_to_file = false;
        FileOutputWriter file = null;
        String str = " ";
        try {
            file = new FileOutputWriter("src/files/fitParameters.txt", append_to_file);
            file.Write("#Sec  SL  v0  deltanm  tMax  distbeta  delta_bfield_coefficient  b1  b2  b3  b4");
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }

        final int nFreePars = 4;

        double [][][] pars2write = new double [nSectors][nSL][nFitPars];//nFitPars = 9

        // initial guess of tMax for the 6 superlayers (cell sizes are different for each)
        // This is one of the free parameters (par[2], but fixed for now.)
        //double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 525.0, 550.0 }; //Moved to Constants.java
        // Now start minimization
        double parSteps[] = {0.00001, 0.001, 0.01, 0.0001};

        Map<Coordinate, MnUserParameters> mapTmpUserFitParameters = new HashMap<Coordinate, MnUserParameters>();
        double prevFitPar = 0.0;
        //for (int i = 0; i < nSectors; i++) {
        //for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            //for (int j = 0; j < nSL; j++) {
                //for (int k = 0; k < nThBinsVz; k++) {
                mapOfFitFunctions.put(new Coordinate(iSec, iSL),
                //new DCFitFunction(h2timeVtrkDocaVZ, iSec, iSL, isLinearFit));
                new DCFitFunction(h2timeVtrkDocaVZ, iSec, iSL, xMeanErrorType, xNormLow, xNormHigh, isLinearFit));
                mapOfFitParameters.put(new Coordinate(iSec, iSL), new MnUserParameters());
                for (int p = 0; p < nFreePars; p++) {
                    //mapOfFitParameters.get(new Coordinate(iSec, iSL)).add(parName[p], prevFitPar, parSteps[p], pLow[iSL][p], pHigh[iSL][p]);
                    mapOfFitParameters.get(new Coordinate(iSec, iSL)).add(parName[p], pInit[iSL][p], parSteps[p], pLow[iSL][p], pHigh[iSL][p]);
                    if(fixIt[p]==true) mapOfFitParameters.get(new Coordinate(iSec, iSL)).fix(p);
                }
                
                MnMigrad migrad
                        = new MnMigrad(mapOfFitFunctions.get(new Coordinate(iSec, iSL)), mapOfFitParameters.get(new Coordinate(iSec, iSL)));
                FunctionMinimum min = migrad.minimize();

                if (!min.isValid()) {
                    // try with higher strategy
                    System.out.println("FM is invalid, try with strategy = 2.");
                    MnMigrad migrad2 = new MnMigrad(mapOfFitFunctions.get(new Coordinate(iSec, iSL)), min.userState(), new MnStrategy(2));
                    min = migrad2.minimize();
                }
                
                mapTmpUserFitParameters.put(new Coordinate(iSec, iSL), min.userParameters());
                double[] fPars = new double[nFreePars];
                double[] fErrs = new double[nFreePars];
                for (int p = 0; p < nFreePars; p++) {
                    fPars[p] = mapTmpUserFitParameters.get(new Coordinate(iSec, iSL)).value(parName[p]);
                    fErrs[p] = mapTmpUserFitParameters.get(new Coordinate(iSec, iSL)).error(parName[p]);
                }
                
                pars2write[iSec][iSL][0] = fPars[0]; pars2write[iSec][iSL][1] = fPars[1];
                pars2write[iSec][iSL][2] = fPars[2]; pars2write[iSec][iSL][3] = fPars[3];
                
                mapOfUserFitParameters.put(new Coordinate(iSec, iSL), fPars);
                //} // end of nThBinsVz loop
            //} // end of superlayer loop
        //} // end of sector loop

                if (!(file == null)) {                    
                    str = String.format("%d %d %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f",
                                iSec+1,iSL+1,pars2write[iSec][iSL][0],pars2write[iSec][iSL][1],pars2write[iSec][iSL][2],
                                pars2write[iSec][iSL][3],pars2write[iSec][iSL][4],pars2write[iSec][iSL][5],
                                pars2write[iSec][iSL][6],pars2write[iSec][iSL][7],pars2write[iSec][iSL][8]);
                    file.Write(str); 
                    textArea.append(str + "\n"); //Show the results in the text area of fitControlUI
        }        
        if (!(file == null)) {
            file.Close();
        }        
    }

    private void createFitLinesNew(int Sec, int SL) {
        int iSec = Sec - 1, iSL = SL - 1;
        double dMax;
        //for (int i = 0; i < nSectors; i++) {
          //for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            //for (int j = 0; j < nSL; j++) {
                dMax = 2 * wpdist[iSL];
                for (int k = 0; k < nThBinsVz; k++) {
                    String title = "timeVsNormDoca Sec=" + (iSec+1) + " SL=" + (iSL+1) + " Th=" + k;
                    double maxFitValue = h2timeVtrkDocaVZ.get(new Coordinate(iSec, iSL, k)).getDataX(getMaximumFitValue(iSec, iSL, k));
                    mapOfFitLines.put(new Coordinate(iSec, iSL, k), new DCFitDrawer(title, 0.0, 1.0, iSL, k, isLinearFit));
                    mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setLineColor(4);//(2);
                    mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setLineWidth(3);
                    mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setLineStyle(4);
                    //mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL, k)));
                    //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                    mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL)));

                    title = "timeVsTrkDoca Sec=" + (iSec+1) + " SL=" + (iSL+1) + " Th=" + k;                    
                    mapOfFitLinesX.put(new Coordinate(iSec, iSL, k), new DCFitDrawerForXDoca(title, 0.0, dMax, iSL, k, isLinearFit));
                    mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setLineColor(1);//(colSimulFit);//(2);
                    mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setLineWidth(2);
                    mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setLineStyle(3);
                    //mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL, k)));
                    //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                    mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL)));
                }
            //}
        //}

    }    
    
    private void makeTimeFitResiduals(int Sec, int SL) {
        int iSec = Sec - 1, iSL = SL - 1;
        for (int k = 0; k < nThBinsVz; k++) {
            H2F h2tmp = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k));
            //h2tmp.add
            for (int l = 0; l <10; l++) {
                
            } 
        }
        //h2timeFitResVtrkDoca, h1timeFitRes
    }
    
    private void drawFitLinesNew(JFrame fitControlFrame, int Sec, int SL) {
        int iSec = Sec - 1, iSL = SL - 1;
        int nSkippedThBins = 4; //Skipping marginal 4 bins from both sides
        String Title = "";
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.setSize(3 * 400, 3 * 400);
        canvas.divide(3, 3);
        for (int k = nSkippedThBins; k < nThBinsVz - nSkippedThBins; k++) {
            canvas.cd(k - nSkippedThBins);
            Title = "Sec=" + Sec + " SL=" + SL
                    + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")"
                    + " indvFitCol=" + colIndivFit;
            canvas.draw(h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k)));
            canvas.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)), "same");
            //canvas.draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
            canvas.getPad(k - nSkippedThBins).setTitle(Title);
            canvas.setPadTitlesX("trkDoca");
            canvas.setPadTitlesY("time (ns)");
        }        
        tabbedPane.add(canvas,"t vs x (fits)");
        
        EmbeddedCanvas canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 3 * 400);
        canvas2.divide(3, 3);
        for (int k = nSkippedThBins; k < nThBinsVz - nSkippedThBins; k++) {
            canvas2.cd(k - nSkippedThBins);
            Title = "Sec=" + Sec + " SL=" + SL
                    + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")"
                    + " indvFitCol=" + colIndivFit;
            canvas2.draw(h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k)).getProfileX());
            canvas2.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)), "same");
            //canvas.draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
            canvas2.getPad(k - nSkippedThBins).setTitle(Title);
            canvas2.setPadTitlesX("trkDoca");
            canvas2.setPadTitlesY("time (ns)");
        }        
        tabbedPane.add(canvas2,"X-profiles & fits");    
        
        JFrame frame = new JFrame();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screensize.getWidth()*.9),(int)(screensize.getHeight()*.9));
        //frame.setLocationRelativeTo(null); //Centers on the default screen
        //Following line makes the canvas or frame open in the same screen where the fitCtrolUI is.
        frame.setLocationRelativeTo(fitControlFrame);//centered w.r.t fitControlUI frame
        frame.add(tabbedPane);//(canvas);
        frame.setVisible(true);         
    }
    
    protected void drawHistograms() {
        //createCanvas();
        createCanvasMaps();
        drawQuickTestPlots();
        
        
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
            //runFitterUsing3DHists();
            
            runFitter();    //This one does simultaneous fit over all theta bins
            runFitterOld(); //This one fits in individual theta bins
            
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Done running fitter
        // lets create lines we just fit
        createFitLines();
        /*
        createFitLinesXTB();
        MakeAndDrawXTProjectionsOfXTBhists();
        */
        
        DrawResidualsInTabbedPanes();
        //drawSectorWiseCanvases();
        drawSectorWiseCanvasMaps();
        DrawInTabbedPanesOfSecSLTh();
        
        // lets add the canvas's to the pane and draw it.
       //addToPane();  //Temprarily disabled 
       

        
        System.out.println("====================================");
        System.out.println("Done with the fitting & drawing ...`");
        System.out.println("====================================");
    }

    protected void drawQuickTestPlots() {
        DrawResidualsInTabbedPanes();
        
        int nEntries = h1bField.getEntries();//.getDataSize(1);
        System.out.println("# of entries in h1bField = " + nEntries);
        // this is temp for testHist
        EmbeddedCanvas canv = new EmbeddedCanvas(); canv.setSize(500, 500);         
        canv.cd(0);
        canv.draw(h1bField);
        canv.save("src/images/test_bField.png");
        EmbeddedCanvas canv1 = new EmbeddedCanvas(); canv1.setSize(1000, 1000); 
        canv1.divide(2,2);
        canv1.cd(0);
        //canv1.draw(h1fitChisqProb);
        canv1.draw(h1fitChi2Trk);
        canv1.cd(1);
        canv1.draw(h1ndfTrk);
        canv1.cd(2);
        canv1.draw(h1zVtx);
        canv1.cd(3);
        canv1.draw(h1fitChi2Trk2);//(h1fitChisqProb);
        canv1.save("src/images/test_fitChisqProb.png");
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

    protected void DrawResidualsInTabbedPanes() {
        String Title = "";
        int iPad = 0;
        JFrame frame = new JFrame();
        JTabbedPane resPanes = new JTabbedPane();
        EmbeddedCanvas canvas = new EmbeddedCanvas ();
        canvas.setSize(3 * 400, 2* 400); 
        canvas.divide(3,2);          
        for (int j = 0; j < nSL; j++) {            
            for (int i = iSecMin; i < iSecMax; i++) {     
                    Title = "Sec="+ (i+1) + " SL=" + (j + 1);
                    iPad = j;
                    canvas.cd(iPad);  
                    canvas.draw(h1timeRes.get(new Coordinate(i, j)));                   
                    canvas.getPad(iPad).setTitle(Title);
                    canvas.setPadTitlesX("residual (cm)");
                }
            } 
        
        EmbeddedCanvas canvas2 = new EmbeddedCanvas ();
        canvas2.setSize(3 * 400, 2* 400); 
        canvas2.divide(3,2);          
        for (int j = 0; j < nSL; j++) {            
            for (int i = iSecMin; i < iSecMax; i++) {     
                    Title = "Sec="+ (i+1) + " SL=" + (j + 1);                    
                    iPad = j;
                    canvas2.cd(iPad);  
                    canvas2.getPad(iPad).getAxisZ().setLog(true);
                    canvas2.draw(h2timeResVsTrkDoca.get(new Coordinate(i, j)));                   
                    canvas2.getPad(iPad).setTitle(Title);
                    canvas2.setPadTitlesX("|trkDoca| (cm)");
                    canvas2.setPadTitlesY("residual (cm)");
                }
            }        
        
        resPanes.add(canvas,"Residual");
        resPanes.add(canvas2,"Residual vs trkDoca");
      
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screensize.getWidth()*.9),(int)(screensize.getHeight()*.9));
        frame.setLocationRelativeTo(null);
        frame.add(resPanes);
        frame.setVisible(true);        
    }
    
    protected void DrawInTabbedPanesOfSecSLTh() {
        
        DrawProfilesInTabbedPanesOfSecSLTh();
        
        String Title = "";
        JFrame frame = new JFrame();
        JTabbedPane sectorPanes = new JTabbedPane();
        for (int i = iSecMin; i < iSecMax; i++) { 
            JTabbedPane anglePanes = new JTabbedPane();
            for (int k = 0; k < nThBinsVz; k++) { 
                EmbeddedCanvas canvas = new EmbeddedCanvas ();
                canvas.setSize(3 * 400, 2* 400); 
                canvas.divide(3,2); 
                for (int j = 0; j < nSL; j++) {  
                    canvas.cd(j); 
                    Title = "Sec="+ (i+1) + " SL=" + (j + 1) 
                            + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")"
                            + " indvFitCol=" + colIndivFit;
                    canvas.draw(h2timeVtrkDoca.get(new Coordinate(i, j, k)));
                    canvas.draw(mapOfFitLinesXOld.get(new Coordinate(i, j, k)), "same");
                    //canvas.draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
                    canvas.getPad(j).setTitle(Title);
                    canvas.setPadTitlesX("trkDoca");
                    canvas.setPadTitlesY("time (ns)"); 
                    /*
                    PaveText stat1 = new PaveText(colSimulFit); stat1.addText("simulFit");
                    PaveText stat2 = new PaveText(colIndivFit); stat2.addText("indivFit");
                    */
                }
                anglePanes.add(canvas,"ThBin"+(k+1));
            }
            sectorPanes.add(anglePanes,"Sector "+ (i+1));
        } 
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screensize.getWidth()*.9),(int)(screensize.getHeight()*.9));
        frame.setLocationRelativeTo(null);
        frame.add(sectorPanes);
        frame.setVisible(true);  
    }
    
        protected void DrawProfilesInTabbedPanesOfSecSLTh() {
        String Title = "";
        JFrame frame = new JFrame();
        JTabbedPane sectorPanes = new JTabbedPane();
        for (int i = iSecMin; i < iSecMax; i++) { 
            JTabbedPane anglePanes = new JTabbedPane();
            for (int k = 0; k < nThBinsVz; k++) { 
                EmbeddedCanvas canvas = new EmbeddedCanvas ();
                canvas.setSize(3 * 400, 2* 400); 
                canvas.divide(3,2); 
                for (int j = 0; j < nSL; j++) {  
                    canvas.cd(j); 
                    Title = "Sec="+ (i+1) + " SL=" + (j + 1) 
                            + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")"
                            + " indvFitCol=" + colIndivFit;
                    canvas.draw(h2timeVtrkDoca.get(new Coordinate(i, j, k)).getProfileX());
                    //canvas.draw(mapOfFitLinesXOld.get(new Coordinate(i, j, k)), "same");
                    //canvas.draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
                    canvas.getPad(j).setTitle(Title);
                    canvas.setPadTitlesX("trkDoca");
                    canvas.setPadTitlesY("time (ns)");                     
                }
                anglePanes.add(canvas,"ThBin"+(k+1));
            }
            sectorPanes.add(anglePanes,"Sector "+ (i+1));
        } 
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screensize.getWidth()*.9),(int)(screensize.getHeight()*.9));
        frame.setLocationRelativeTo(null);
        frame.add(sectorPanes);
        frame.setVisible(true);        
    }

    //Cannot draw my SimpleH3D, so I want to check it by drawing its XT projections & errors separately
    public void MakeAndDrawXTProjectionsOfXTBhists() {
        int nXbins, nTbins, nBbins;
        //private Map<Coordinate, H2F> h2TXprojOfh3BTX = new HashMap<Coordinate, H2F>(); 
        //private Map<Coordinate, H2F> h2TXprojOfh3BTXinvErr = new HashMap<Coordinate, H2F>();     
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)            
            for (int j = 0; j < nSL; j++) {
                for (int k = 0; k < nThBinsVz2; k++) { 
                    nXbins = h3BTXmap.get(new Coordinate(i, j, k)).getNBinsX();
                    nTbins = h3BTXmap.get(new Coordinate(i, j, k)).getNBinsX();
                    for(int ii=0; ii<nXbins; ii++) {
                        for(int jj=0; jj<nTbins; jj++) {
                            h2TXprojOfh3BTX.get(new Coordinate(i, j, k)).setBinContent(ii,jj, 
                                    h3BTXmap.get(new Coordinate(i, j, k)).getXYBinProj(ii,jj));                            
                        }                    
                    }                    
                }
            }
        }
        
        //Now drawing these projections onto Tabbed Panes:
        String Title = "";
        JFrame frame = new JFrame();
        JTabbedPane sectorPanes = new JTabbedPane();
        for (int i = iSecMin; i < iSecMax; i++) { 
            JTabbedPane anglePanes = new JTabbedPane();
            for (int k = 0; k < nThBinsVz2; k++) { 
                EmbeddedCanvas canvas = new EmbeddedCanvas ();
                canvas.setSize(3 * 400, 2* 400); 
                canvas.divide(3,2); 
                for (int j = 0; j < nSL; j++) {  
                    canvas.cd(j); 
                    Title = "Sec="+ (i+1) + " SL=" + (j + 1) 
                            + " theta=(" + thEdgeVzL2[k] + "," + thEdgeVzH2[k] + ")"
                            + " indvFitCol=" + colIndivFit;
                    canvas.draw(h2TXprojOfh3BTX.get(new Coordinate(i, j, k)));
                    //canvas.draw(mapOfFitLinesXOld.get(new Coordinate(i, j, k)), "same");
                    //canvas.draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
                    canvas.getPad(j).setTitle(Title);
                    canvas.setPadTitlesX("trkDoca");
                    canvas.setPadTitlesY("time (ns)"); 
                }
                anglePanes.add(canvas,"ThBin"+(k+1));
            }
            sectorPanes.add(anglePanes,"Sector "+ (i+1));
        } 
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screensize.getWidth()*.9),(int)(screensize.getHeight()*.9));
        frame.setLocationRelativeTo(null);
        frame.add(sectorPanes);
        frame.setVisible(true);          
    }
    
    
    //Simultaneous fits over selected theta bins
    public void runFitterUsing3DHists() throws IOException {
        boolean append_to_file = false;
        FileOutputWriter file = null;
        String str = " ";
        try {
            file = new FileOutputWriter("src/files/fitParametersUsing3DHists.txt", append_to_file);
            file.Write("#Sec  SL  v0  deltanm  tMax  distbeta  delta_bfield_coefficient  b1  b2  b3  b4");
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

        Map<Coordinate, MnUserParameters> mapTmpUserFitParametersXTB = new HashMap<Coordinate, MnUserParameters>();
        double prevFitPar = 0.0;
        //for (int i = 0; i < nSectors; i++) {
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                pLow[2] = tMaxSL[j] * 0.4;
                pHigh[2] = tMaxSL[j] * 1.6;
                //for (int k = 0; k < nThBinsVz; k++) {
                mapOfFitFunctionsXTB.put(new Coordinate(i, j),  new DCFitFunctionWithSimpleH3D(h3BTXmap, i, j, isLinearFit));
                mapOfFitParametersXTB.put(new Coordinate(i, j), new MnUserParameters());
                for (int p = 0; p < nFreePars; p++) {
                    prevFitPar = prevFitPars[p]; //This is value set by hand (for now).
                    prevFitPar = parsFromCCDB[i][j][p]; //this comes from CCDB
                    if(p==1) prevFitPar = 1.5;
                    mapOfFitParametersXTB.get(new Coordinate(i, j)).add(parName[p], prevFitPar, parSteps[p], pLow[p], pHigh[p]);
                }
                //mapOfFitParameters.get(new Coordinate(i, j)).setValue(2, tMaxSL[j]);// tMax for SLth superlayer 
                //mapOfFitParameters.get(new Coordinate(i, j)).fix(0);
                mapOfFitParametersXTB.get(new Coordinate(i, j)).fix(1);
                //mapOfFitParameters.get(new Coordinate(i, j)).fix(2);
                //mapOfFitParameters.get(new Coordinate(i, j)).fix(3);
                
                MnMigrad migrad
                        = new MnMigrad(mapOfFitFunctionsXTB.get(new Coordinate(i, j)), mapOfFitParametersXTB.get(new Coordinate(i, j)));
                FunctionMinimum min = migrad.minimize();

                if (!min.isValid()) {
                    // try with higher strategy
                    System.out.println("FM is invalid, try with strategy = 2.");
                    MnMigrad migrad2 = new MnMigrad(mapOfFitFunctionsXTB.get(new Coordinate(i, j)), min.userState(), new MnStrategy(2));
                    min = migrad2.minimize();
                }
                
                mapTmpUserFitParametersXTB.put(new Coordinate(i, j), min.userParameters());
                double[] fPars = new double[nFreePars];
                double[] fErrs = new double[nFreePars];
                for (int p = 0; p < nFreePars; p++) {
                    fPars[p] = mapTmpUserFitParametersXTB.get(new Coordinate(i, j)).value(parName[p]);
                    fErrs[p] = mapTmpUserFitParametersXTB.get(new Coordinate(i, j)).error(parName[p]);
                }
                
                pars2write[i][j][0] = fPars[0]; pars2write[i][j][1] = fPars[1];
                pars2write[i][j][2] = fPars[2]; pars2write[i][j][3] = fPars[3];
                
                mapOfUserFitParametersXTB.put(new Coordinate(i, j), fPars);
                //} // end of nThBinsVz loop
            } // end of superlayer loop
        } // end of sector loop

        for (int i = 0; i < nSectors; i++) { 
            for (int j = 0; j < nSL; j++) {
                if (!(file == null)) {                    
                    str = String.format("%d %d %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f",
                                i+1,j+1,pars2write[i][j][0],pars2write[i][j][1],pars2write[i][j][2],
                                pars2write[i][j][3],pars2write[i][j][4],pars2write[i][j][5],
                                pars2write[i][j][6],pars2write[i][j][7],pars2write[i][j][8]);
                    file.Write(str); 
                }        
            }
        }        
        if (!(file == null)) {
            file.Close();
        }
    }
    
    //Simultaneous fits over selected theta bins
    public void runFitter() throws IOException {

        boolean append_to_file = false;
        FileOutputWriter file = null;
        String str = " ";
        try {
            file = new FileOutputWriter("src/files/fitParameters.txt", append_to_file);
            file.Write("#Sec  SL  v0  deltanm  tMax  distbeta  delta_bfield_coefficient  b1  b2  b3  b4");
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
                    if(p==1) prevFitPar = 1.5;
                    mapOfFitParameters.get(new Coordinate(i, j)).add(parName[p], prevFitPar, parSteps[p], pLow[p], pHigh[p]);
                }
                //mapOfFitParameters.get(new Coordinate(i, j)).setValue(2, tMaxSL[j]);// tMax for SLth superlayer 
                //mapOfFitParameters.get(new Coordinate(i, j)).fix(0);
                mapOfFitParameters.get(new Coordinate(i, j)).fix(1);
                //mapOfFitParameters.get(new Coordinate(i, j)).fix(2);
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
                
                mapOfUserFitParameters.put(new Coordinate(i, j), fPars);
                //} // end of nThBinsVz loop
            } // end of superlayer loop
        } // end of sector loop

        for (int i = 0; i < nSectors; i++) { 
            for (int j = 0; j < nSL; j++) {
                if (!(file == null)) {                    
                    str = String.format("%d %d %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f %5.4f",
                                i+1,j+1,pars2write[i][j][0],pars2write[i][j][1],pars2write[i][j][2],
                                pars2write[i][j][3],pars2write[i][j][4],pars2write[i][j][5],
                                pars2write[i][j][6],pars2write[i][j][7],pars2write[i][j][8]);
                    file.Write(str); 
                }        
            }
        }        
        if (!(file == null)) {
            file.Close();
        }
    }

    //Different fits in individual theta bins
    public void runFitterOld() throws IOException {

        boolean append_to_file = false;
        FileOutputWriter file = null;
        String str = "";

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
        double pLow[] = {prevFitPars[0] * 0.0, prevFitPars[1] * 0.0, tMaxSL[0] * 0.0, prevFitPars[3] * 0.0};
        double pHigh[] = {prevFitPars[0] * 3.0, prevFitPars[1] * 4.0, tMaxSL[0] * 4.0, prevFitPars[3] * 3.0};

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
                        if(p==0) prevFitPars[p] = 0.0053;
                        if(p==1) prevFitPars[p] = 1.5;
                        if(p==2) prevFitPars[p] = 627.8;
                        if(p==3) prevFitPars[p] = 0.1171;
                        //if(p==0) prevFitPars[p] = 0.007;
                        //if(p==2) prevFitPars[p] = 0.007;
                        mapOfFitParametersOld.get(new Coordinate(i, j, k)).add(parName[p], prevFitPars[p], parSteps[p], pLow[p], pHigh[p]);
                    }
                    mapOfFitParametersOld.get(new Coordinate(i, j, k)).setValue(2, tMaxSL[j]);// tMax for SLth superlayer
                    //mapOfFitParametersOld.get(new Coordinate(i, j, k)).fix(0);
                    //mapOfFitParametersOld.get(new Coordinate(i, j, k)).fix(1);
                    //mapOfFitParametersOld.get(new Coordinate(i, j, k)).fix(2);
                    //mapOfFitParametersOld.get(new Coordinate(i, j, k)).fix(3);
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

                    //Writing fit parameters to an output file
                    if (!(file == null)) {
                        /*file.Write((i + 1) + "  " + (j + 1) + "  " + (k + 1) + "  " + fPars[0]
                                + "  " + fPars[1] + "  " + fPars[2] + "  " + fPars[3]); */                            
                        str = String.format("%d %d %d %5.4f %5.4f %5.4f %5.4f",
                                        i+1,j+1,k+1,fPars[0],fPars[1],fPars[2],fPars[3]);
                        file.Write(str);                         
                    }
                    mapOfUserFitParametersOld.put(new Coordinate(i, j, k), fPars);
                } // end of nThBinsVz loop
            } // end of superlayer loop
        } // end of sector loop

        if (!(file == null)) {
            file.Close();
        }
    }

        private void createFitLinesXTB() {
        double dMax;
        //for (int i = 0; i < nSectors; i++) {
          for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                dMax = 2 * wpdist[j];
                for (int k = 0; k < nThBinsVz2; k++) {
                    String title = "timeVsNormDoca Sec=" + (i+1) + " SL=" + (j+1) + " Th=" + k;
                    double maxFitValue = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getDataX(getMaximumFitValue(i, j, k));
                    //mapOfFitLinesXTB.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k, isLinearFit));
                    mapOfFitLinesXTB.put(new Coordinate(i, j, k), new DCFitDrawerForXDocaXTB(title, 0.0, 1.1*dMax, j, k, isLinearFit));
                    mapOfFitLinesXTB.get(new Coordinate(i, j, k)).setLineColor(4);//(2);
                    mapOfFitLinesXTB.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLinesXTB.get(new Coordinate(i, j, k)).setLineStyle(4);
                    //mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j, k)));
                    //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                    mapOfFitLinesXTB.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParametersXTB.get(new Coordinate(i, j)));
                }
            }
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
                    mapOfFitLines.get(new Coordinate(i, j, k)).setLineColor(4);//(2);
                    mapOfFitLines.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLines.get(new Coordinate(i, j, k)).setLineStyle(4);
                    //mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j, k)));
                    //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                    mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j)));
                    
                    //Now creating lines using parameters from individual theta-bin fits
                    mapOfFitLinesOld.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k, isLinearFit));
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setLineColor(colIndivFit);
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setLineStyle(1);
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParametersOld.get(new Coordinate(i, j, k)));

                    title = "timeVsTrkDoca Sec=" + (i+1) + " SL=" + (j+1) + " Th=" + k;                    
                    mapOfFitLinesX.put(new Coordinate(i, j, k), new DCFitDrawerForXDoca(title, 0.0, 1.1*dMax, j, k, isLinearFit));
                    mapOfFitLinesX.get(new Coordinate(i, j, k)).setLineColor(colSimulFit);//(2);
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

    public void OpenFitControlUI(TimeToDistanceFitter fitter) {

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FitControlUI(fitter).setVisible(true); //Defined in FitControlUI.java
            }
        });        
    }    
    
    public void SliceViewer(TimeToDistanceFitter fitter) {
        //Create a frame and show it through SwingUtilities
        //   It doesn't require related methods and variables to be of static type
        SwingUtilities.invokeLater(() -> {
            new SliceViewer("Slice Viewer").create(fitter); 
        });        
    }
    
    @Override
    public void run() {

        processData();

        drawQuickTestPlots();
        System.out.println("Called drawQuickTestPlots();");
        
        SliceViewer(this);
        
        OpenFitControlUI(this);
        //drawHistograms(); //Disabled 4/3/17 - to control it by clicks in FitConrolUI.
    }

    private void saveNtuple() {
        nTupletimeVtrkDocaVZ.write("src/files/pionTest.evio");
    }

    public static void main(String[] args) {
        String fileName;
        String fileName2;

        fileName = "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.evio";
        ArrayList<String> fileArray = new ArrayList<String>();
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_1.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_10.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_2.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_4.evio");

        TimeToDistanceFitter rd = new TimeToDistanceFitter(fileArray, true);

        rd.processData();
        // System.out.println(rd.getMaximumFitValue(5, 5, 5) + " output");
        rd.drawHistograms();

    }
}

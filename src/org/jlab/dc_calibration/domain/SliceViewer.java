package org.jlab.dc_calibration.domain;

/**
 *
 * @author kpadhikari
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Toolkit;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.DataVector;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.FunctionFactory;

public class SliceViewer extends JPanel {

    private int kpCounter = 0;// 
    private static final Random random = new Random();
    private final String name;
    private H2F hist2d;
    public final int nBins = 200;
    public final double xLow = 0.4, xHigh = 7.6, binW = (xHigh - xLow) / nBins;
    String[] SecOrSLnum = {"1", "2", "3", "4", "5", "6"};
    String[] locAngBins = new String[nThBinsVz]; //To be used by jComboBoxThBins
    JComboBox jComboBoxSec, jComboBoxSL, jComboBoxThBins;// = new JComboBox(SecOrSLnum);
    int gSector = 2, gSL = 1, gThBin = 0;
    TimeToDistanceFitter fitter;

    public SliceViewer(String name) {
        this.name = name;
        this.setPreferredSize(new Dimension(1000, 500));
        //this.setBackground(new Color(random.nextInt()));//kp: Background gets a random color
        this.setBackground(Color.lightGray);//kp: Background gets a light gray color
        this.add(new JLabel(name));

        fillStringArrayForLocAngBins();

        //Preparing the 2D histo
        hist2d = FunctionFactory.randomGausian2D(nBins, xLow, xHigh, 800000, 3.3, 0.8);
        hist2d.setTitleX("Randomly Generated Function");
        hist2d.setTitleY("Randomly Generated Function");
        //hist2d.

//        //Following lines 
//        System.out.println("nBinsX = " + hist2d.getDataSize(0));//returned nBins
//        System.out.println("nBinsY = " + hist2d.getDataSize(1));//returned nBins
//        System.out.println("nBinsZ = " + hist2d.getDataSize(4));//whatever value (3,4, ..) gives the same #
//        H2F h2 = new H2F("h2",100,0.3,4.0,150,0.2,3.0);
//        System.out.println("h2 nBinsX = " + h2.getDataSize(0));//Returned 100
//        System.out.println("h2 nBinsY = " + h2.getDataSize(1));//returned 150
//        System.out.println("h2 nBinsZ = " + h2.getDataSize(5));//returned 150       
    }

    public void fillStringArrayForLocAngBins() {
        for (int i = 0; i < locAngBins.length; i++) {
            locAngBins[i] = String.format("%02d: (%2.1f,%2.1f)", i, thEdgeVzL[i], thEdgeVzH[i]);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public void updateSlicePad(int iSector, int iSL, int iThBin, EmbeddedCanvas c1, int padID, JLabel label, int xBin4Slice) {
        System.out.println("iSec iSL iThBin: " + iSector + " " + iSL + " " + iThBin);
        H2F h2d = this.hist2d;
        h2d = fitter.h2timeVtrkDoca.get(new Coordinate(iSector - 1, iSL - 1, iThBin));
        //h2d.get

        c1.cd(0);
        c1.getPad(0).setTitle("Histogram2D Demo");
        c1.draw(h2d);
        c1.cd(1);
        c1.getPad(1).setTitle("X-profile of hist2D");
        GraphErrors profileX = h2d.getProfileX();
        DataVector vector = profileX.getVectorX();
        //System.out.println(vector.getMin() + " " + vector.getMax());

        profileX.setTitleX("X-Profile");
        c1.draw(profileX);

        double xBinPos = xLow + binW * xBin4Slice - binW / 2; //x-position of current bin-center.
        c1.cd(padID);
        c1.getPad(padID).setTitle("x-Slice " + String.valueOf(xBin4Slice));
        if (xBin4Slice > 0) {
            label.setText("x-Bin # = " + String.valueOf(xBin4Slice));
            H1F xSlice = h2d.sliceX(xBin4Slice);
            //System.out.println("xbinPos = " + h2d.getDataX(xBin4Slice));
            xBinPos = h2d.getDataX(xBin4Slice);
            xSlice.setTitleX(xBin4Slice + String.format("th x-slice at x = %4.3f", xBinPos));
            c1.draw(xSlice);
        }
        c1.update();
    }

    public void addJComboBoxes(JPanel control) {
        //Add three JComboBoxes for Sec, SL and Th along with corresponding JLabels.
        control.add(new JLabel("<html><font color='black'> <b>Sector</b></font></html>"));
        jComboBoxSec = new JComboBox(SecOrSLnum);
        jComboBoxSec.setSelectedIndex(1); //Select second superlayer by default (KPP data is only on 2nd superlayer)
        control.add(jComboBoxSec);
        control.add(new JLabel("<html><font color='black'> <b>Superlayer</b></font></html>"));
        jComboBoxSL = new JComboBox(SecOrSLnum);
        jComboBoxSL.setSelectedIndex(0);
        control.add(jComboBoxSL);
        control.add(new JLabel("<html><font color='black'> <b>Theta-bin</b></font></html>"));
        jComboBoxThBins = new JComboBox(locAngBins);
        jComboBoxThBins.setSelectedIndex(5);
        control.add(jComboBoxThBins);
    }

    public void addListenersToComboBoxes(EmbeddedCanvas c1, JLabel jLabel) {
        //Initializing the global variables with whatever the combo-boxes are initialized to
        gSector = Integer.parseInt(jComboBoxSec.getSelectedItem().toString());
        gSL = Integer.parseInt(jComboBoxSL.getSelectedItem().toString());
        gThBin = jComboBoxThBins.getSelectedIndex();

        //Now add corresponding listeners to the combo-boxes.
        jComboBoxSec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //jComboBox1ActionPerformed(evt);
                gSector = Integer.parseInt(jComboBoxSec.getSelectedItem().toString());
                updateSlicePad(gSector, gSL, gThBin, c1, 2, jLabel, 0);
            }
        });

        jComboBoxSL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gSL = Integer.parseInt(jComboBoxSL.getSelectedItem().toString());
                updateSlicePad(gSector, gSL, gThBin, c1, 2, jLabel, 0);
            }
        });

        jComboBoxThBins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gThBin = jComboBoxThBins.getSelectedIndex();
                updateSlicePad(gSector, gSL, gThBin, c1, 2, jLabel, 0);
            }
        });
    }

    public void create(TimeToDistanceFitter fitter) {
        this.fitter = fitter;

        SliceViewer thisPanel = new SliceViewer("");
        //I can replace above line by simply having another function for all the initializations
        //   done in the constructor above. I am not really using the object created here.

        JPanel upPanel = new JPanel(new GridLayout(0, 1));
        upPanel.setPreferredSize(new Dimension(1200, 400));//this one only worked to control the size 

        JPanel control = new JPanel();
        JLabel jLabel = new JLabel("x-Bin # = " + String.valueOf(kpCounter));
        EmbeddedCanvas c1 = new EmbeddedCanvas();

        //Defining & Initializing a JTextField object to be added between the backward & forward bottons below.
        JTextField jTextField = new JTextField(3);
        jTextField.setText(String.format("%02d", kpCounter));
        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {                
                kpCounter = Integer.parseInt(jTextField.getText());
                updateSlicePad(gSector, gSL, gThBin, c1, 2, jLabel, kpCounter);
                System.out.println("Enter pressed after entering " + kpCounter);
            }
        });
        
        
        addJComboBoxes(control);
        addListenersToComboBoxes(c1, jLabel);

        //c1.setSize(1200, 400); //Didn't have any effect
        c1.divide(3, 1);
        //f.setSize(1000, 400);//Didn't have any effect
        updateSlicePad(gSector, gSL, gThBin, c1, 2, jLabel, kpCounter);

        //kp: defining a button, implementing the actionListener and adding to the panel all at the same time.
        control.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                kpCounter--;
                updateSlicePad(gSector, gSL, gThBin, c1, 2, jLabel, kpCounter);
                jTextField.setText(String.format("%02d", kpCounter)); //can be moved inside updateSlicePad(..) too
            }
        }));


        control.add(jTextField);

        control.add(new JButton(new AbstractAction("Next\u22b3") {

            @Override
            public void actionPerformed(ActionEvent e) {
                kpCounter++;
                updateSlicePad(gSector, gSL, gThBin, c1, 2, jLabel, kpCounter);
                jTextField.setText(String.format("%02d", kpCounter)); //can be moved inside updateSlicePad(..) too
            }
        }));

        control.add(jLabel);
        //upPanel.add(jLabel);
        upPanel.add(c1);

        JFrame f = new JFrame("Slice Viewer");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("(int)(screensize.getWidth()*.9 = " + (int) (screensize.getWidth() * .9));
        System.out.println("(int)(screensize.getHeight()*.9 = " + (int) (screensize.getHeight() * .9));
        // f.setSize((int)(screensize.getWidth()*.9),(int)(screensize.getHeight()*.9)); //Didn't have any effect

        f.add(upPanel, BorderLayout.CENTER);//This is like an overayed tabbed panes (like a stack of cards)
        f.add(control, BorderLayout.SOUTH);

        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void main(String[] args) {
        //This method requires all related methods and variables to be of static type
//        EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                create(); 
//            }
//        });

        OrderOfAction OA = null;
        boolean isLinearFit = true;
        ArrayList<String> fileArray = new ArrayList<String>();
        fileArray.add("/Users/kpadhikari/CLAS12/KPP/Pass2/cooked_7/out_clas12_000810_a00050.hipo");
        fileArray.add("/Users/kpadhikari/CLAS12/KPP/Pass2/cooked_7/out_clas12_000810_a00052.hipo");

        TimeToDistanceFitter fitter = new TimeToDistanceFitter(OA, fileArray, isLinearFit);
        fitter.processData();

        //Create a frame and show it through SwingUtilities
        //   It doesn't require related methods and variables to be of static type
        SwingUtilities.invokeLater(() -> {
            new SliceViewer("Slice Viewer").create(fitter);
        });
    }
}

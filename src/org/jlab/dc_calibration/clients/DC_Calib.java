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
 *  `------'
 */
 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration.clients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.Action;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.jlab.dc_calibration.domain.DialogForRec;
import org.jlab.dc_calibration.domain.DialogForT0cor;
import org.jlab.dc_calibration.domain.DialogFor_tvsxCCDBwriter;
import org.jlab.dc_calibration.domain.EstimateT0correction;

import org.jlab.dc_calibration.domain.OrderOfAction;
import org.jlab.dc_calibration.domain.RunReconstructionCoatjava4;
import org.jlab.dc_calibration.domain.TimeToDistanceFitter;

public class DC_Calib extends WindowAdapter implements WindowListener, ActionListener, Runnable {

    private JFrame frame;
    private JTextArea textArea;

    protected Thread reader, reader2;
    private boolean quit;

    private final PipedInputStream pin = new PipedInputStream();
    private final PipedInputStream pin2 = new PipedInputStream();

    static String linearFit = "Linear Fit";
    static String nonLinearFit = "Non-linear Fit";
    private boolean isLinearFit;

    private File file;
    Thread errorThrower; // just for testing (Throws an Exception at this
    // Console
    Thread mythread;
    // Banner
    private JLabel banner;
    // JPanels to be used
    private JPanel bannerPanel, panelForWelcomeAndOpenFile, panelForVariousControls, panelImg, centerPanel;
    private int gridSize = 1;
    private JPanel buttonPanel, radioPanel;
    // a file chooser to be used to open file to analyze
    JFileChooser fc;
    // file to be read and analyzed
    private String fileName;
    // buttons to be implemented
    JButton bT0Correction;
    JButton bFileChooser, bTestEvent, bReadRecDataIn, bReconstruction, bTimeToDistance, bCCDBwriter, buttonClear;
    Dimension frameSize;
    OrderOfAction OA = null;

    File[] fileList = null;
    ArrayList<String> fileArray = null;

    public DC_Calib() {
        createFrame();
        createFileChooser();
        createButtons();
        createPanels();
        initFrame();
    }

    private void createFrame() {
        // create all components and add them
        frame = new JFrame("DC Calibration Console");
        frame.setLayout(new BorderLayout());// kp

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frameSize = new Dimension((int) (screenSize.width / 1.25), (int) (screenSize.height / 1.5));
        int x = (int) (frameSize.width / 2);
        int y = (int) (frameSize.height / 2);
        frame.setBounds(x, y, frameSize.width, frameSize.height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(frameSize.width, frameSize.height);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createBanner() {
        banner = new JLabel("Welcome to DC Calibration for CLAS12!", JLabel.CENTER);
        banner.setForeground(Color.yellow);
        banner.setBackground(Color.gray);
        banner.setOpaque(true);
        banner.setFont(new Font("SansSerif", Font.BOLD, 20));
        banner.setPreferredSize(new Dimension(1000, 30));
    }

    private void createFileChooser() {
        fc = new JFileChooser();
    }

    private void createButtons() {
        bFileChooser = new JButton("Choose File", createImageIcon("/images/Open16.gif"));
        bT0Correction = new JButton();
        bTestEvent = new JButton();
        bReadRecDataIn = new JButton();
        bReconstruction = new JButton();
        bTimeToDistance = new JButton();
        bCCDBwriter = new JButton();
        buttonClear = new JButton("Clear");

        bTestEvent.setText("<html>" + "&emsp; &emsp; TestButton " + "<br>" + " Needs to be removed" + "</html>");
        bT0Correction.setText("<html>" + "Estimate T0s");
        bReadRecDataIn.setText("<html>" + "Run Decoder" + "</html>");
        bReconstruction.setText("<html>" + "Run Reconstruction" + "</html>");
        bTimeToDistance.setText("<html>" + "Run Time vs. Distance Fitter" + "</html>");
        bCCDBwriter.setText("<html>" + "Load xvst pars to CCDB" + "</html>");

        //bReconstruction.setPreferredSize(new Dimension(frameSize.width / 3, frameSize.height / 3));
        bTimeToDistance.setPreferredSize(new Dimension(frameSize.width / 3, frameSize.height / 3));

    }

    private void createPanels() {
        bannerPanel = new JPanel(new BorderLayout());
        addToBanner();

        panelForVariousControls = new JPanel(new BorderLayout());
        panelForVariousControls.setBorder(BorderFactory.createEtchedBorder());
        //addToOpenFilePanel(); //Moved below
        radioPanel = new JPanel(new GridLayout(0, 1));
        addToT0CorButton();
        addToRecoButton();
        addToRadioPanel();
        addToOpenFilePanel();//add File-chooser, radio panel etc to the control panel
        addToCCDBwriterButton(); //CCDB writer for tvsx parameters
        
        panelForWelcomeAndOpenFile = new JPanel(new BorderLayout());
        addToWelcomePanel();

        panelImg = new JPanel(new BorderLayout());
        addToPanelImage();

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(gridSize, gridSize, 1, 1));

        addToButtonPanel();

        centerPanel = new JPanel(new BorderLayout());
        addToCenterPanel();
    }

    private void addToBanner() {
        createBanner();
        bannerPanel.add(banner, BorderLayout.CENTER);
    }

    private void addToOpenFilePanel() {  
        
        //Pack bRec & radioPanel into subpanel1 //& add to the main panel @ start
        JPanel subControlPanel1 = new JPanel(new BorderLayout());
        subControlPanel1.add(bReconstruction, BorderLayout.LINE_START);
        subControlPanel1.add(radioPanel, BorderLayout.CENTER);
        //panelForVariousControls.add(subControlPanel1, BorderLayout.LINE_START);
        
        //Pack bT0Correction & subControlPanel1 into subpanel0 & add to the main panel @ start
        JPanel subControlPanel0 = new JPanel(new BorderLayout());
        subControlPanel0.add(bT0Correction, BorderLayout.LINE_START);
        subControlPanel0.add(subControlPanel1, BorderLayout.CENTER);
        panelForVariousControls.add(subControlPanel0, BorderLayout.LINE_START);        
        
        
     
        //Pack bFileChooser to another subpanel & add it to center of main Panel
        JPanel subControlPanel2 = new JPanel(new BorderLayout());
        subControlPanel2.add(bFileChooser, BorderLayout.LINE_START);
        panelForVariousControls.add(subControlPanel2, BorderLayout.CENTER);
        
        panelForVariousControls.add(bCCDBwriter, BorderLayout.LINE_END);
    }

    private void addToWelcomePanel() {
        panelForWelcomeAndOpenFile.add(bannerPanel, BorderLayout.NORTH);
        panelForWelcomeAndOpenFile.add(panelForVariousControls, BorderLayout.SOUTH);
    }

    private void addToPanelImage() {
        ImageIcon imageIcon = new ImageIcon(new ImageIcon(this.getClass().getResource("/images/CLAS12.jpg")).getImage()
                .getScaledInstance(320, 320, java.awt.Image.SCALE_SMOOTH));
        // ImageIcon(this.getClass().getResource("images/timeVsTrkDoca_and_Profiles.png"));
        JLabel imgLabel = new JLabel(imageIcon);
        panelImg.add(imgLabel, BorderLayout.CENTER);
    }

    private void addToPanelImage(String whoMadeMeWake) {
        ImageIcon imageIcon = new ImageIcon(new ImageIcon(this.getClass().getResource("/images/CLAS12.jpg")).getImage()
                .getScaledInstance(320, 320, java.awt.Image.SCALE_SMOOTH));
        // ImageIcon(this.getClass().getResource("images/timeVsTrkDoca_and_Profiles.png"));
        JLabel imgLabel = new JLabel(imageIcon);
        panelImg.add(imgLabel, BorderLayout.CENTER);
    }
    
    private void addToT0CorButton() {
            bT0Correction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                String choice = ae.getActionCommand();
                if (choice.equals("Quit")) {
                    System.exit(0);
                }   
                else {
                    createDialogForT0Correction();
                }
            }
        });        
    }
    private void addToCCDBwriterButton() {
            bCCDBwriter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                String choice = ae.getActionCommand();
                if (choice.equals("Quit")) {
                    System.exit(0);
                } else {
                    
                    createDialogForTvsX_CCDBwriter();                    
                }
            }
        });        
    }
    
    private void addToRecoButton() {
        bReconstruction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Reconstruction Button has been hit..");
                //createDialogForRecControls();
                //RunReconstructionCoatjava4 rec = new RunReconstructionCoatjava4();
                String choice = ae.getActionCommand();
                if (choice.equals("Quit")) {
                    System.exit(0);
                } //else if (choice.equals("Enter data")) {
                else {
                    createDialogForRecControls();
                }
            }
        });
    }
 
    private void createDialogForTvsX_CCDBwriter() {
        DialogFor_tvsxCCDBwriter dlg = new DialogFor_tvsxCCDBwriter(frame);
        String[] results = dlg.run();
        ArrayList<String> fileArray = dlg.getFileArray();
        if (results[0] != null) {
            
            String s = null;
            String command = null;

            try {
                System.out.println("File to be uploaded: " + results[0]
                        + "\nComments to be added: '" + results[1] + "'");
                //Process p = Runtime.getRuntime().exec("pwd");
                
                command = String.format("./src/files/loadFitParsToCCDB.csh %s '%s'", results[0],results[1]);
                System.out.println("The following command is being executed: \n " + command);
                command = "./src/files/justEchoHello.sh";
                Process p = Runtime.getRuntime().exec(command);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                // read the output from the command
                //System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }

                // read any errors from the attempted command
                //System.out.println("Here is the standard error of the command (if any):\n");
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }

                System.exit(0);
            } catch (IOException e) {
                System.out.println("exception happened - here's what I know: ");
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
    
    private void createDialogForT0Correction() {
        //DialogForRec dlg = new DialogForRec(frame);
        DialogForT0cor dlg = new DialogForT0cor(frame);
        String[] results = dlg.run();
        ArrayList<String> fileArray = dlg.getFileArray();
        if (results[0] != null) {
            JOptionPane.showMessageDialog(frame,
                    "Input file: " + results[0] + "\nOutput file: " + results[1]); 
            
                    System.out.println("Debug 0");
            EstimateT0correction t0c = new EstimateT0correction(results, fileArray);
            t0c.DrawPlots(); 
            t0c.DrawPlotsForAllCables();
            t0c.DrawPlotsForTMaxAllCables();
            System.out.println("Finished drawing the T0 plots ..");
        }
    }    
    
    private void chooseInputFiles(JFileChooser iFC, ActionEvent evt) {
        iFC.setMultiSelectionEnabled(true);
        iFC.showOpenDialog(null);
        fileList = iFC.getSelectedFiles();
        fileArray = new ArrayList<String>();
        for (File file : fileList) {
            System.out.println("Ready to read file " + file);
            fileArray.add(file.toString());
        }
    }
    
    private void createDialogForRecControls() {
        DialogForRec dlg = new DialogForRec(frame);
        String[] results = dlg.run();
        if (results[0] != null) {
            JOptionPane.showMessageDialog(frame,
                    "Input file: " + results[0] + "\nOutput file: " + results[1]); 
            
            //Now make RunReconstructionCoatjava4() take results as input arg & control IP & OP files
            RunReconstructionCoatjava4 rec = new RunReconstructionCoatjava4(results);
        }
    }
    
    private void addToButtonPanel() {
        // buttonPanel.add(bTestEvent);
        // buttonPanel.add(bReadRecDataIn);
        //buttonPanel.add(bReconstruction);
        buttonPanel.add(bTimeToDistance);
        //buttonPanel.add(bCCDBwriter);
        OrderOfAction(2); // this int in OrderOfAction is the number of buttons activated in this method
    }

    private void OrderOfAction(int NButtons) {
        OA = new OrderOfAction(NButtons);
        // OA.setbuttonorder(bReconstruction, 1);
        OA.setbuttonorder(bFileChooser, 1);

        OA.setbuttonorder(bTimeToDistance, 2);
    }

    private void addToCenterPanel() {
        int width = (int) (frameSize.width);
        int height = (int) (frameSize.height);
        addToTextArea();
        JScrollPane images = new JScrollPane(panelImg);
        images.setPreferredSize(new Dimension((int) (width / 3.5), (int) (height / 3.5)));
        centerPanel.add(images, BorderLayout.WEST);

        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(Box.createVerticalGlue(), BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension((int) (width / 2), (int) (height / 2)));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        centerPanel.add(scroll, BorderLayout.EAST);

    }

    private void addToTextArea() {
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    /**
     * This method is to get edit menu when right-clicked on the textfield 
     */
    private void AddEditOptionsMenu() {

        JPopupMenu menu = new JPopupMenu();
        Action cut = new DefaultEditorKit.CutAction();
        cut.putValue(Action.NAME, "Cut");
        cut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
        menu.add(cut);

        Action copy = new DefaultEditorKit.CopyAction();
        copy.putValue(Action.NAME, "Copy");
        copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
        menu.add(copy);

        Action paste = new DefaultEditorKit.PasteAction();
        paste.putValue(Action.NAME, "Paste");
        paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        menu.add(paste);

        //Action selectAll = new DefaultEditorKit.selectAllAction(); //kp: doesn't work
        Action selectAll = new SelectAll(); //kp: See this local clas defined below.       
        menu.add(selectAll);

        textArea.setComponentPopupMenu(menu);
    }

    static class SelectAll extends TextAction {

        public SelectAll() {
            super("Select All");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control A"));//kp:("control S"));
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent component = getFocusedComponent();
            component.selectAll();
            component.requestFocusInWindow();
        }
    }

    private void addToRadioPanel() {
        //super(new BorderLayout());

        //Create the radio buttons.    
        //JRadioButton pigButton = new JRadioButton(pigString);
        //pigButton.setMnemonic(KeyEvent.VK_P);
        //pigButton.setActionCommand(pigString);
        JRadioButton linearFitButton = new JRadioButton(linearFit);
        //linearFitButton.setMnemonic(KeyEvent.VK_L);
        linearFitButton.setActionCommand(linearFit);
        JRadioButton nonLinearFitButton = new JRadioButton(nonLinearFit);
        //nonLinearFitButton.setMnemonic(KeyEvent.VK_N);
        nonLinearFitButton.setActionCommand(nonLinearFit);

        //kp: If not grouped, more than one (even all buttons can be selected simulataneously)
        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(linearFitButton);
        group.add(nonLinearFitButton);

        //Register a listener for the radio buttons.        
        //linearFitButton.addActionListener(this);
        linearFitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isLinearFit = true;
                addListeners();
            }
        }
        );
        nonLinearFitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isLinearFit = false;
                addListeners();
            }
        }
        );

        //Put the radio buttons in a column in a panel.
        radioPanel.add(linearFitButton);
        radioPanel.add(nonLinearFitButton);
    }

    private void initFrame() {

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panelForWelcomeAndOpenFile, BorderLayout.NORTH);
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        frame.getContentPane().add(buttonClear, BorderLayout.SOUTH);
        frame.setVisible(true);

        //addListeners();

    }

    private void addListeners() {
        listen();

        System.out.println("isLinearFit = " + isLinearFit);
        if (isLinearFit) {
            System.out.println("You selected Linear Fit.");
        } else {
            System.out.println("You selected Non-Linear Fit.");
        }
        bFileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OA.buttonstatus(e);
                if (OA.isorderOk()) {
                    chooseFiles(e);
                    if (fileArray.size() == 0) {
                        System.err.println("There are no files selected ");
                        System.exit(1);
                    }

                    TimeToDistanceFitter e3 = new TimeToDistanceFitter(OA, fileArray, isLinearFit);
                    //ReadRecDataForMinuitNewFileOldWay e3 = new ReadRecDataForMinuitNewFileOldWay(OA, fileArray, isLinearFit);
                    bTimeToDistance.addActionListener(ee -> {
                        new Thread(e3).start();
                    });
                }
            }
        });
        //listen();
    }
    
    private void listen() {
        frame.addWindowListener(this);
        try {
            PipedOutputStream pout = new PipedOutputStream(this.pin);

            System.setOut(new PrintStream(pout, true));
        } catch (java.io.IOException io) {
            textArea.append("Couldn't redirect STDOUT to this console\n" + io.getMessage());
        } catch (SecurityException se) {
            textArea.append("Couldn't redirect STDOUT to this console\n" + se.getMessage());
        }
        try {
            PipedOutputStream pout2 = new PipedOutputStream(this.pin2);
            System.setErr(new PrintStream(pout2, true));
        } catch (java.io.IOException io) {
            textArea.append("Couldn't redirect STDERR to this console\n" + io.getMessage());
        } catch (SecurityException se) {
            textArea.append("Couldn't redirect STDERR to this console\n" + se.getMessage());
        }

        quit = false; // signals the Threads that they should exit
        reader = new Thread(this);
        reader.setDaemon(true); // kp: make this thread a process running in the
        // background (no interactive access)
        reader.start(); // kp: start this process
        //
        reader2 = new Thread(this);
        reader2.setDaemon(true);
        reader2.start();
    }

    private void chooseFiles(ActionEvent evt) {
        fc.setMultiSelectionEnabled(true);
        fc.showOpenDialog(null);
        fileList = fc.getSelectedFiles();
        fileArray = new ArrayList<String>();
        for (File file : fileList) {
            System.out.println("Readying file " + file);
            fileArray.add(file.toString());
        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon(String path) {
        // java.net.URL imgURL = FileChooserDemo.class.getResource(path);
        ImageIcon myImageIcon;
        java.net.URL imgURL = DC_Calib.class.getResource(path);
        if (imgURL != null) {
            myImageIcon = new ImageIcon(imgURL);
            return myImageIcon;
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public synchronized void windowClosed(WindowEvent evt) {
        quit = true;
        this.notifyAll(); // stop all threads (kp: notify All threads?)
        // kp:
        // https://docs.oracle.com/javase/tutorial/essential/concurrency/join.html
        try {
            reader.join(1000);
            pin.close();
        } catch (Exception e) {
        }
        try {
            reader2.join(1000);
            pin2.close();
        } catch (Exception e) {
        }
        System.exit(0);
    }

    public synchronized void windowClosing(WindowEvent evt) {
        frame.setVisible(false); // default behaviour of JFrame
        frame.dispose();
    }

    public synchronized void actionPerformed(ActionEvent evt) {
        // Handle open button action.
        // if (evt.getSource() == bFileChooser) {
        // int returnVal = fc.showOpenDialog(fc);
        // if (returnVal == JFileChooser.APPROVE_OPTION) {
        // file = fc.getSelectedFile();
        // try {
        // this.fileName = file.getCanonicalPath();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // System.out.println("Opening: " + fileName + "\n");
        // } else {
        // // log.append("Open command cancelled by user." + newline);
        // System.out.println("Open command cancelled by user.\n");
        // }
        // } else if (evt.getSource() == buttonClear) {
        if (evt.getSource() == buttonClear) {
            textArea.setText("");
        }
        OA.buttonstatus(evt);

        if (OA.isorderOk()) {
            System.out.println("I am green and now I should do something here...");
        } else {
            System.out.println("I am red and it is not my turn now ;( ");
        }

    }

    public synchronized void run() {
        try {
            while (Thread.currentThread() == reader) {
                try {
                    this.wait(100);
                } catch (InterruptedException ie) {
                }
                if (pin.available() != 0) {
                    String input = this.readLine(pin);
                    textArea.append(input);
                }
                if (quit) {
                    return;
                }
            }

            while (Thread.currentThread() == reader2) {
                try {
                    this.wait(100);
                } catch (InterruptedException ie) {
                }
                if (pin2.available() != 0) {
                    String input = this.readLine(pin2);
                    textArea.append(input);
                }
                if (quit) {
                    return;
                }
            }
        } catch (Exception e) {
            textArea.append("\nConsole reports an Internal error.");
            textArea.append("The error is: " + e);
        }

        /*
		 * // just for testing (Throw a Nullpointer after 1 second) if (Thread.currentThread()==errorThrower) { try { this.wait(1000); }catch(InterruptedException
		 * ie){} throw new NullPointerException( "Application test: throwing an NullPointerException It should arrive at the console" ); }
         */
    }

    public synchronized String readLine(PipedInputStream in) throws IOException {
        String input = "";
        do {
            /**
             * kp: PipedInputStream inherits from InputStream and available() is
             * one of its methods.
             * https://docs.oracle.com/javase/7/docs/api/java/io/InputStream.
             * html available(): Returns an estimate of the number of bytes that
             * can be read (or skipped over) from this input stream without
             * blocking by the next invocation of a method for this input
             * stream.
             *
             * read(byte[] b): Reads some number of bytes from the input stream
             * and stores them into the buffer array b.
             */
            int available = in.available();
            if (available == 0) {
                break;
            }
            byte b[] = new byte[available]; // kp: creating a 'byte' array of
            // size 'available'
            in.read(b);
            input = input + new String(b, 0, b.length);
        } while (!input.endsWith("\n") && !input.endsWith("\r\n") && !quit);
        return input;
    }

    // private void initMenu() {
    // JMenuBar bar = new JMenuBar();
    // JMenu fileMenu = new JMenu("File");
    // JMenuItem processItem = new JMenuItem("Process File");
    // processItem.addActionListener(this);
    // fileMenu.add(processItem);
    // bar.add(fileMenu);
    //
    // JMenu pluginMenu = new JMenu("Plugins");
    // JMenuItem loadPlugin = new JMenuItem("Tree Viewer");
    // loadPlugin.addActionListener(this);
    // pluginMenu.add(loadPlugin);
    //
    // bar.add(pluginMenu);
    //
    // this.setJMenuBar(bar);
    // }
}

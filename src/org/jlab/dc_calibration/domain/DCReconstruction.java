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
package org.jlab.dc_calibration.domain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataSync;
import org.jlab.io.evio.EvioSource;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;

public class DCReconstruction implements ActionListener, Runnable {

	private String inputFile;
	private String outputFile;
	private DCHBEngine hbEngine;
	private DCTBEngine tbEngine;

	private EvioSource reader;
	private EvioDataSync writer;

	private OrderOfAction OAInstance;
	private boolean acceptorder = false;

	public DCReconstruction(String inputFile) {
		this.inputFile = inputFile;
		this.outputFile = "src/files/recOutfile.evio";
	}

	public DCReconstruction(String inputFile, boolean debug) {
		this.inputFile = inputFile;
		this.outputFile = "src/files/recOutfile.evio";
		init();
		processEvents();
		finish();
		displayEnv();
	}

	public DCReconstruction(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		init();
		processEvents();
		finish();
		displayEnv();
	}

	public DCReconstruction(OrderOfAction OAInstance, String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.OAInstance = OAInstance;
	}

	public void addFile(String inputFile) {
		this.inputFile = inputFile;
	}

	private void init() {
		hbEngine = new DCHBEngine();
		tbEngine = new DCTBEngine();
		reader = new EvioSource();

		hbEngine.init();
		tbEngine.init();
		reader.open(inputFile);
		outputFileOpen();
	}

	private void outputFileOpen() {
		writer = new EvioDataSync();
		writer.open(outputFile);
	}

	private void runAction() {
		init();
		processEvents();
		finish();
		displayEnv();
	}

	private void processEvents() {// Eventually we want this to be multi-threaded
		int icounter = 0;
		while (reader.hasEvent()) {
			icounter++;
			if ((icounter % 200) == 0 && icounter < 201) {
				System.out.println("processed " + icounter + " Events");
			}
			EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
			// Process Hit Based
			hbEngine.processDataEvent(event);
			// Process Time Based
			tbEngine.processDataEvent(event);
			writer.writeEvent(event);
		}

	}

	private void finish() {
		reader.close();
		writer.close();
	}

	private void displayEnv() {
		System.out.println("JAVA_HOME = " + System.getenv("JAVA_HOME"));
		System.out.println("CLAS12DIR = " + System.getenv("CLAS12DIR"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// runAction();

		OAInstance.buttonstatus(e);
		acceptorder = OAInstance.isorderOk();
		JFrame frame = new JFrame("JOptionPane showMessageDialog example1");
		System.out.println("I am here do I go farther answer is ");
		if (acceptorder) {
			System.out.println("yes");
			JOptionPane.showMessageDialog(frame, "Click OK to start reconstructing the file ...");
			runAction();
		} else
			System.out.println("I am red and it is not my turn now ;( ");

	}

	@Override
	public void run() {
		runAction();
	}

	public static void main(String[] args) {
		// String fileName = "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/theDecodedFileR128T0corSec1_30k.0_header.evio";
		// String fileName = "/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/gemcData/out_phi120_oldcalctime_header.ev";
		String fileName = "/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/theDecodedFileR128T0corSec1_30k.0_header.evio";
		String outputName = "/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/gemcData/DCCalReconstructed.evio";

		// String fileName = "/Volumes/Seagate_Storage/Work_Data/CLAS12/DC_Calibration/data/theDecodedFileR128T0corSec1_allEv.0_header.evio";
		DCReconstruction dcReconstruction = new DCReconstruction(fileName, outputName);
		// // dcReconstruction.processEvents();
	}

}

/*
             * To change this license header, choose License Headers in Project Properties.
             * To change this template file, choose Tools | Templates
             * and open the template in the editor.
 */
package org.jlab.dc_calibration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.evio.EvioDataEvent;

/**
 *
 * @author KPAdhikari
 */
public class TestEvent implements ActionListener {

	int eventNr;
	static boolean debug = false; // for debugging
	protected static java.util.List<String> inputFiles;
	static long numEvents = 15000;// (long) 1e9; // events to process
	static long printEventNr = 20000; // display progress

	public TestEvent() {}

	public void actionPerformed(ActionEvent ev) {
		JFrame frame = new JFrame("JOptionPane showMessageDialog example1");

		// show a joptionpane dialog using showMessageDialog
		// JOptionPane.showMessageDialog(frame, myMessage);
		JOptionPane.showMessageDialog(frame, "Click OK to start reading the reconstructed file ...");
		processData();
	}

	public void processData() {
		int inFileNum = 1;
		int ievent = 0;
		String fileName = "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/reconstructedDataR128T0corT2DfromCCDBvarFit08.1.evio";

		EvioDataChain reader = new EvioDataChain();
		for (int fN = 0; fN < inFileNum; fN++) {
			reader.addFile(fileName);
		}
		reader.open();
		System.out.println("Opened the input data file!");

		int counter = 0, NumEv2process = 20, nTBHits = 0;
		EvioDataBank bnkHits = null;

		// Now loop over all the events
		while (reader.hasEvent() && ievent < 20) {
			ievent++;
			EvioDataEvent event = reader.getNextEvent();
			// if (counter < NumEv2process) {
			if (event.hasBank("TimeBasedTrkg::TBHits")) {
				bnkHits = (EvioDataBank) event.getBank("TimeBasedTrkg::TBHits");
				nTBHits = bnkHits.rows();
				System.out.println("# of hist in this " + counter + "th event = " + nTBHits);
				// }
				// counter++;
			}
		}
		System.out.println("Done");
	}
}

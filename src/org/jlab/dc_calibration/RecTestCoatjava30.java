/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration;

import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;

/**
 *
 * @author KPAdhikari
 */
public class RecTestCoatjava30 {
	public static void main(String[] args) {

		int inFileNum = 1;
		String inputFile = "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/theDecodedFileR128T0corSec1_30k.0_header.evio";

		String fDir = "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data"; // In Ubuntu
		String fName = "theDecodedFileR128T0corSec1_30k.0_header";

		String outputFile = String.format("%s/recOutDCRBRECub.evio", fDir);
		String line;

		// String COATJAVA = "C:\\Users\\KPAdhikari\\Box Sync\\VMWareSharedDocs\\Java\\COATJAVA\\coatjava-2.4\\coatjava";
		String COATJAVA = System.getenv("CLAS12DIR");
		System.out.println("JAVA_HOME = " + System.getenv("JAVA_HOME"));
		System.out.println("CLAS12DIR = " + System.getenv("CLAS12DIR"));

		System.err.println(" \n[PROCESSING FILE] : " + inputFile);

		DCHBEngine en = new DCHBEngine();
		en.init();
		DCTBEngine en2 = new DCTBEngine();
		// DCTBRasterEngine en2 = new DCTBRasterEngine();
		en2.init();
		org.jlab.io.evio.EvioSource reader = new org.jlab.io.evio.EvioSource();

		int counter = 0;

		reader.open(inputFile);
		long t1 = System.currentTimeMillis();

		// Writer
		// String outputFile="/Users/ziegler/Workdir/Distribution/coatjava-3.0.1/DCRBREC.evio";
		org.jlab.io.evio.EvioDataSync writer = new org.jlab.io.evio.EvioDataSync();
		writer.open(outputFile);

		while (reader.hasEvent()) {

			counter++;
			org.jlab.io.evio.EvioDataEvent event = (org.jlab.io.evio.EvioDataEvent) reader.getNextEvent();
			en.processDataEvent(event);

			// Processing TB
			en2.processDataEvent(event);

			// if(counter>5) break;
			// if(counter%100==0)
			// System.out.println("run "+counter+" events");
			writer.writeEvent(event);
		}
		writer.close();
		double t = System.currentTimeMillis() - t1;
		System.out.println("TOTAL  PROCESSING TIME = " + t);
	}
}

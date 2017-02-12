/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration.domain;

/**
 *
 * @author KPAdhikari
 */
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class FileOutputWriter {

    private String path;
    private boolean append_to_file = false;
    private FileWriter fWriter;
    private PrintWriter print_line;
    public FileOutputWriter(String file_path) throws IOException {
        path = file_path;
        OpenFileAndPrintWriter();
    }

    public FileOutputWriter(String file_path, boolean append_value) throws IOException {
        path = file_path;
        append_to_file = append_value;
        OpenFileAndPrintWriter();
    }

    public void OpenFileAndPrintWriter() throws IOException {
        fWriter = new FileWriter(path, append_to_file);
        print_line = new PrintWriter(fWriter);
    }
    
    public void Write(String textLine) {
        print_line.printf("%s" + "%n",textLine);
    }
    
    public void Close() throws IOException {
        print_line.close();
        fWriter.close();
    }
}

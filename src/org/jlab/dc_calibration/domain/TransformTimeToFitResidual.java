/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration.domain;

/**
 *
 * @author kpadhikari
 */
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;

public class TransformTimeToFitResidual {
    private H1F h1;
    private H2F h2;
    
    public TransformTimeToFitResidual(H2F h2) {
        this.h2 = h2;
    }
}

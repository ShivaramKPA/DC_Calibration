/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.dc_calibration.domain;

import org.jlab.groot.math.Func1D;

/**
 *
 * @author kpadhikari
 */
public class TmaxFitFunction extends Func1D {

    public TmaxFitFunction(String name, double min, double max) {
        super(name, min, max);
    }

    //Simple polynomial function of any order
    @Override
    public double evaluate(double x) {
        //F1D f1 = new F1D("f1", "Math.exp([p0]+[p1]*x)/(1.0 + Math.exp([p2] + [p3]*x)) + [p4]", -20.0, 100.0);
        //double [] fPars = {11.5239, -0.00939369, 158.689, -10.181029, 10.0};
        
        double exp1 = Math.exp(this.getParameter(0)+this.getParameter(1)*x);
        double exp2 = Math.exp(this.getParameter(2)+this.getParameter(3)*x);
        double baseline = this.getParameter(4);
        double value = exp1*(1.0 - 1.0/(1.0 + exp2)) + baseline;

        return value;
    }
}
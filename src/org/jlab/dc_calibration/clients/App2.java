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
package org.jlab.dc_calibration.clients;
import org.jlab.groot.base.GStyle;
public class App2 {

    public static void main(String[] args) {
        GStyle.getGraphErrorsAttributes().setMarkerStyle(0);
        GStyle.getGraphErrorsAttributes().setMarkerColor(3);
        GStyle.getGraphErrorsAttributes().setMarkerSize(7);
        GStyle.getGraphErrorsAttributes().setLineColor(3);
        GStyle.getGraphErrorsAttributes().setLineWidth(3);
        GStyle.getAxisAttributesX().setTitleFontSize(24);
        GStyle.getAxisAttributesX().setLabelFontSize(18);
        GStyle.getAxisAttributesY().setTitleFontSize(24);
        GStyle.getAxisAttributesY().setLabelFontSize(18);
        GStyle.getH1FAttributes().setLineWidth(2);
        GStyle.getH1FAttributes().setLineColor(21);
        GStyle.getH1FAttributes().setFillColor(34);
        GStyle.getH1FAttributes().setOptStat("10");
        GStyle.getFunctionAttributes().setLineWidth(2);
        GStyle.getFunctionAttributes().setLineColor(32);
        GStyle.getAxisAttributesZ().setLog(true);
        // ReadDataForMinuit rd = new ReadDataForMinuit(fileName);
        // ReadRecDataIn recDataIn = new ReadRecDataIn();
        // recDataIn.processData();
        DC_Calib mk = new DC_Calib();
        
    }

}

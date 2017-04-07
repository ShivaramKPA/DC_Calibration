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
// Krishna:
// Program made out of translating a fortran file given to me by Mac Mestayer on 8/12/15
// https://www.jlab.org/Hall-B//secure/eg4/adhikari/myHomeLinked/MyHm/PD1wk/CLAS12/DC/dc12-readout-to-wire-map-2.C
/*
  program elec_wire_map
  c
  c	author: Mac Mestayer
  c	date:  Nov. 6, 2015
  c       I modified dc12-readout-to-wire-map.for to work on
  c       an input of dcrb crate, slot, connector, pin.
  c 	purpose: for a given crate(1-18),slot(4-10,13-19),connector(1-6),
  c       and pin(1-16) return the sector(1-6), layer(1-36)
  c       and wire(1-112); i.e. the wire identification as well as the
  c       wire's readout status and its cable delay
  c
  implicit none
  c
  c
  c  input arguments: crate, slot, channel
  c  Note that crate, slot, connector, pin refers to the DCRB system
  c  When referring to the stbboard or stbconnector, they are written out
  c
  c
  c
  c  output arguments: sector,layer,wire
  *
 */
 /*
    Serguei's short response:
    
    first superlayer - slots 3 to 9
    second superlayer - slots 14 20

----- Original Message -----
From: "Mac Mestayer" <mestayer@jlab.org>
To: "Krishna Adhikari" <adhikari@jlab.org>
Cc: "Serguei Boiarinov" <boiarino@jlab.org>
Sent: Wednesday, February 22, 2017 2:25:53 PM
Subject: Re: Mapping of signal cables to (crate, slot, channel)

Hello Sergey;

What we want to know is the slot assignments for the first and second superlayer in
a DCRB crate.  I know that they each occupy 7 slots, but I think you and Ben may
have changed them from what they used to be.    - Mac
 */
public class T0SignalCableMap {

    int crate, slot, channel, chanindex, connector, pin;
    int sector, layer, wire;
    //c  other variables
    //c  loclayer (1-6), locsuplayer (1-2), suplayer (1-6), locwire (1-16)
    //c  nstb is the stbboard number, stbconnector the stb conn. number
    int region, loclayer, locsuplayer, suplayer, locwire, nstb, stbconnector;

    //the following parameters/constants (1st letter m) are used as array dimensions
    private final int msector = 6, mlayer = 36, mwire = 112, mcrate = 18,
            mlocsuplayer = 2, mloclayer = 6, mlocwire = 16, mstbboard = 7, mslot = 20,
            mconn = 6, mpin = 16, mdcrbslot = 14, mcable = 84; //mchannel=96, 

    //c map of crate to sector
    int[] crate_sector = {1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6}; //size [mcrate]

    //c map of crate to region
    int[] crate_region = {1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3};////[mcrate]

    //New map of pin to local layer for each local stb connector position
    int[][] chan_loclayer //[mconn][mpin] 
            = {
                {2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1},
                {3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4},
                {6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5},
                {2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1},
                {3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4},
                {6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5}
            };

    //int chan_locwire[mchannel] //c map of channel to local wire
    int[][] chan_locwire //[mconn][mpin]
            = {
                {1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3},
                {3, 3, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6},
                {6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8},
                {9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11},
                {11, 11, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 13, 14, 14},
                {14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16}
            };
    /*c
c  map of DCRB slot, connector to stb board number
c   the numbers in each row are the stb board number
c   for the DCRB connector in question (1,2,3,4,5 and 6)
c
     */
    int[][] stb //[mconn][mslot]
            = {
                //{0,0,0,1,2,3,4,5,6,7,0,0,1,2,3,4,5,6,7,0}, Old slot mapping

                {0, 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7},
                {0, 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7},
                {0, 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7},
                {0, 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7},
                {0, 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7},
                {0, 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7}
            };
    /*c
c  map of DCRB slot, connector to stb connector number
c   the numbers in each row are the stb connector number
c   for the DCRB connector in question (1,2,3,4,5 and 6)
c*/
    int[][] stbconn//[mconn][mslot]
            = {
                //{0,0,0,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,0}, //Old mapping
                {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1},
                {0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2},
                {0, 0, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3},
                {0, 0, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4},
                {0, 0, 5, 5, 5, 5, 5, 5, 5, 0, 0, 0, 0, 5, 5, 5, 5, 5, 5, 5},
                {0, 0, 6, 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 6, 6, 6, 6, 6, 6, 6}
            };

    //
    //c  map of DCRB slot to stb board number
    /**
     * *** int slot_stb[mslot]={0,0,0,1,2,3,4,5,6,7,0,0,1,2,3,4,5,6,7,0}; ****
     */
    //c  map of DCRB slot to local superlayer
    int[] slot_locsuplayer //={0,0,0,1,1,1,1,1,1,1,0,0,2,2,2,2,2,2,2,0}; //[mslot]
            = {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2};

    /*c  cableid is the cable id. number, status is the status of the
c  an individual crate,slot connector
     */
    int[][] cableid//[mconn][mslot]
            = {
                //{0,0,0,1,2,3,4,5,6,7,0,0,8,9,10,10,12,13,14,0}, //Old mapping
                {0, 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 8, 9, 10, 10, 12, 13, 14},
                {0, 0, 15, 16, 17, 18, 19, 20, 21, 0, 0, 0, 0, 22, 23, 24, 25, 26, 27, 28},
                {0, 0, 29, 30, 31, 32, 33, 34, 35, 0, 0, 0, 0, 36, 37, 38, 39, 40, 41, 42},
                {0, 0, 43, 44, 45, 46, 47, 48, 49, 0, 0, 0, 0, 50, 51, 52, 53, 54, 55, 56},
                {0, 0, 57, 58, 59, 60, 61, 62, 63, 0, 0, 0, 0, 64, 65, 66, 67, 68, 69, 70},
                {0, 0, 71, 72, 73, 74, 75, 76, 77, 0, 0, 0, 0, 78, 79, 80, 81, 82, 83, 84}
            };

//c  cabledelay is the delay time of the cable
//c status is the status of the cable, 1 if good
    double[] cabledelay//[mcable]
            = {5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0};
    int[] status//[mcable]
            = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    
    int[][] CrateID = { //[nSL][nSec], 18 crates in total (as many as DCs, one crate per chamber)
        {41, 41, 42, 42, 43, 43}, //Sector 1
        {44, 44, 45, 45, 46, 46}, //Sector 2
        {47, 47, 48, 48, 49, 49},
        {50, 50, 51, 51, 52, 52},
        {53, 53, 54, 54, 55, 55},
        {56, 56, 57, 57, 58, 58} //Sector 6
        };
    int [][] SlotID = { //[nSL][nSlots]
        {3, 4, 5, 6, 7, 8, 9},          //sl = 1
        {14, 15, 16, 17, 18, 19, 20},   //sl = 2
        {3, 4, 5, 6, 7, 8, 9},          //sl = 3
        {14, 15, 16, 17, 18, 19, 20},
        {3, 4, 5, 6, 7, 8, 9},
        {14, 15, 16, 17, 18, 19, 20}     //sl = 6
    };
    
    //Refer to fig. "Signal Cable Routing" in page 7 of 
    //  https://clasweb.jlab.org/wiki/images/8/84/DC12_Wire_Maps.pdf
    
    int [][] CableID = { //[nLayer][nLocWire] => nLocWire=16, 7 groups of 16 wires in each layer
        {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 1
        {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 2
        {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 3
        {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 4
        {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 5
        {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 6  
        //===> 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 (Local wire ID: 0 for 1st, 16th, 
        //      32th, 48th, 64th, 80th, 96th wires)
    };
    
    public T0SignalCableMap() {

    }

    public int getCrateID(int superlayer1to6, int sector) {
        int iSector = sector - 1; 
        int crateID = CrateID[superlayer1to6 -1][iSector];
        return crateID;
    }    
    
    public int getSlotID(int superlayer1to6, int wire1to112) {
        int slot1to7 = (int)((wire1to112 - 1) / 16) + 1; 
        int slotID = SlotID[superlayer1to6 -1][slot1to7 - 1];
        return slotID;
    }
    public int getSlotID1to7(int wire1to112) {
        int iSlot = (int)((wire1to112 - 1) / 16) + 1;         
        return iSlot;
    }
    
    public int getCableID1to6(int layer1to6, int wire1to112) {

        /*kp: 96 channels are grouped into 6 groups of 16 channels
            and each group joins with a connector & a corresponding cable (with IDs 1,2,3,4,& 6)*/
        int wire1to16 = (int)((wire1to112 - 1) % 16 + 1); 
        int cable_id = CableID[layer1to6 - 1][wire1to16 - 1];
        return cable_id;
    }    
    
    public int getCableID(int slot, int channel0to95) {

        /*kp: 96 channels are grouped into 6 groups of 16 channels
            and each group joins with a connector (with IDs 1,2,3,4,&6)*/
        connector = (int)(channel0to95 / 16) + 1; 
        int cable_id = cableid[connector - 1][slot - 1];
        return cable_id;
    }

    public void getVariousThings() { //Not used for now.
        //c channel index is equal to channel+1
        chanindex = channel + 1; //channel goes from 0 to 95
        //chanindex=channel;

        //c
        //c  look-up values based on input crate, slot, channel
        //c  first, turn channel into connector and pin
        //c
        sector = crate_sector[crate - 1];
        region = crate_region[crate - 1];

        /*kp: 96 channels are grouped into 6 groups of 16 channels
        and each group joins with a connector (with IDs 1,2,3,4,&6)*/
        connector = channel / 16 + 1; //Division: gives lower-round result of division
        pin = channel % 16 + 1;         //Modulus:  returns the remainder of the division

        int cable_id, istat;
        double delay;
        nstb = stb[connector - 1][slot - 1];
        stbconnector = stbconn[connector - 1][slot - 1];
        loclayer = chan_loclayer[connector - 1][pin - 1];
        locwire = chan_locwire[connector - 1][pin - 1];
        locsuplayer = slot_locsuplayer[slot - 1];
        cable_id = cableid[connector - 1][slot - 1];
        istat = status[cable_id - 1];
        delay = cabledelay[cable_id - 1];

        //c  now calculate layer and wire numbers
        suplayer = (region - 1) * 2 + locsuplayer;
        layer = (suplayer - 1) * 6 + loclayer;
        wire = (nstb - 1) * 16 + locwire;

        //c print out results
        String str = String.format("crate slot  %s: %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %3.2f\n",
                "chan conn pin sec locsuplay suplay localay lay wire stb stbconn cable_id istat delay",
                crate, slot, channel, connector, pin, sector, locsuplayer, suplayer, loclayer,
                layer, wire, nstb, stbconnector, cable_id, istat, delay);
        System.out.println(str);

    }
}

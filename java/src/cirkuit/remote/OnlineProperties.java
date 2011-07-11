package cirkuit.remote;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

import cirkuit.util.Configuration;
import cirkuit.game.Game;

/**
 * This class defines all the properties needed for the game.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class OnlineProperties implements Serializable {
    private int maxPlayer;
    private String circuitName;
    private int grid, minusSpeed, plusSpeed, crashRadius;
    private double maxAngle;
    private boolean redDots, isCrash, isRallyMode;
    private int turn, trace;
    
    public boolean modified = false;
    
    /** Creates a the default properties */
    public OnlineProperties() {
        circuitName = "default.ckt";

        grid = 10;
        minusSpeed = 2;
        plusSpeed = 2;
        crashRadius = 0;
        maxAngle = Math.PI/4;
        redDots = true;
        isCrash = true;
        isRallyMode = false;
        turn = 1;
        trace = 0;
        
        maxPlayer = 8;
    }
    
    public int getCrashRadius() {
        return this.crashRadius;
    }
    
    public void setCrashRadius(int cr) {
        this.crashRadius = cr;
    }
    
    public void setRedDots(boolean b) {
        this.redDots = b;
    }
    
    public boolean getRedDots() {
        return this.redDots;
    }
    
    public void setIsCrash(boolean b) {
        this.isCrash = b;
    }
    
    public boolean getIsCrash() {
        return this.isCrash;
    }
    
    public void setIsRallyMode(boolean b) {
        this.isRallyMode = b;
    }
    
    public boolean getIsRallyMode() {
        return this.isRallyMode;
    }
    
    public void setMode(int mode) {
        this.isRallyMode = (mode == Game.RALLY_MODE);
    }
    
    public int getMode() {
        return (this.isRallyMode)?Game.RALLY_MODE:Game.NORMAL_MODE;
    }
    
    public void setCircuitName(String s) {
        if (s != null)
            this.circuitName = s;
    }
    
    public String getCircuitName() {
        return this.circuitName;
    }
    
    public void setTrace(int t) {
        this.trace = t;
    }
    
    public int getTrace() {
        return this.trace;
    }
    
    public void setGrid(int g) {
        this.grid = g;
    }
    
    public int getGrid() {
        return this.grid;
    }
    
    public void setMaxPlayer(int m) {
        this.maxPlayer = m;
    }
    
    public int getMaxPlayer() {
        return this.maxPlayer;
    }
    
    public void setPlusSpeed(int s) {
        this.plusSpeed = s;
    }
    
    public int getPlusSpeed() {
        return this.plusSpeed;
    }
    
    public void setMinusSpeed(int s) {
        this.minusSpeed = s;
    }
    
    public int getMinusSpeed() {
        return this.minusSpeed;
    }
    
    public double getMaxAngle() {
        return this.maxAngle;
    }
    
    public void setMaxAngle(double a) {
        this.maxAngle = a;
    }

    /**
     * Set the turn number.
     * @param turn the turn number
     */
    public void setTurn(int turn) {
        this.turn = turn;
    }
    
    /**
     * Get the turn number.
     * @return the turn number
     */
    public int getTurn() {
        return turn;
    }
}

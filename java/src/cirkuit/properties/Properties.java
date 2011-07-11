package cirkuit.properties;

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
public class Properties implements Serializable {
    private Hashtable playerClass = new Hashtable();
    private Hashtable playerColor = new Hashtable();
    
    private String circuitFileName, nickname, serverIP;
    private Color outColor, inColor, startColor, onlineColor;
    private int grid, minusSpeed, plusSpeed, crashRadius;
    private double maxAngle;
    private boolean redDots, isCrash, isRallyMode, autoResize;
    private int turn, trace, localPort, serverPort;
    
    /** Creates a the default properties */
    public Properties() {
        circuitFileName = "./shared/circuits/complex.ckt";
        
        outColor = Color.black;
        inColor = Color.black;
        startColor = Color.black;
        
        grid = 10;
        minusSpeed = 2;
        plusSpeed = 2;
        crashRadius = 0;
        maxAngle = Math.PI/4;
        redDots = true;
        isCrash = true;
        autoResize = true;
        isRallyMode = false;
        turn = 1;
        trace = 0;
        
        serverPort = 2000;
        serverIP = "localhost";
        localPort = 2000;
        nickname = "Player";
        onlineColor = Color.green;
        
        playerClass.put("Bob","HumanPlayer");
        playerClass.put("Jean","HumanPlayer");
        
        playerColor.put("Bob",Color.red);
        playerColor.put("Jean",Color.blue);
    }
    
    /** Loads a Properties Object saved with the save method. */
    public static Properties binaryLoad(String fileName) {
        ObjectInputStream in = null;
        Properties p = null;
        try {
            in = new ObjectInputStream(new FileInputStream(fileName));
            p = (Properties)in.readObject();
        } catch(Exception e) { System.out.println(e); }
        finally {
            try {
                if (in != null)
                    in.close();
            } catch(Exception e) { System.out.println(e); }
        }
        
        return p;
    }
    
    /** Saves the Properties to fileName */
    public boolean binarySave(String fileName) {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(this);
        } catch(Exception e) { return false; }
        finally {
            try {
                if (out != null)
                    out.close();
            } catch(Exception e) { return false; }
        }
        
        return true;
    }
    
    /** Loads a Properties Object saved with the save method. */
    public static Properties load(String fileName) {
        Properties props = new Properties();
        Configuration conf = new Configuration(fileName);
        
        if (conf.read()) {
            props.setPlayerClass(getPlayerClassFromDescription(conf.get("playerClass")));
            props.setPlayerColor(getPlayerColorFromDescription(conf.get("playerColor")));
            
            props.setCircuitFileName(conf.get("circuitFileName"));
            
            props.setOutColor(getColorFromDescription(conf.get("outColor")));
            props.setInColor(getColorFromDescription(conf.get("inColor")));
            props.setStartColor(getColorFromDescription(conf.get("startColor")));
            
            props.setGrid((Integer.valueOf((conf.get("grid")==null)?"10":conf.get("grid"))).intValue());
            props.setPlusSpeed((Integer.valueOf((conf.get("plusSpeed")==null)?"2":conf.get("plusSpeed"))).intValue());
            props.setCrashRadius((Integer.valueOf((conf.get("crashRadius")==null)?"0":conf.get("crashRadius"))).intValue());
            props.setMinusSpeed((Integer.valueOf((conf.get("minusSpeed")==null)?"2":conf.get("minusSpeed"))).intValue());
            props.setMaxAngle((Double.valueOf((conf.get("maxAngle")==null)?""+Math.PI/4:conf.get("maxAngle"))).doubleValue());
            props.setRedDots((Boolean.valueOf((conf.get("redDots")==null)?"true":conf.get("redDots"))).booleanValue());
            props.setIsCrash((Boolean.valueOf((conf.get("isCrash")==null)?"true":conf.get("isCrash"))).booleanValue());
            props.setAutoResize((Boolean.valueOf((conf.get("autoResize")==null)?"true":conf.get("autoResize"))).booleanValue());
            props.setIsRallyMode((Boolean.valueOf((conf.get("isRallyMode")==null)?"false":conf.get("isRallyMode"))).booleanValue());
            props.setTurn((Integer.valueOf((conf.get("turn")==null)?"1":conf.get("turn"))).intValue());
            props.setTrace((Integer.valueOf((conf.get("trace")==null)?"0":conf.get("trace"))).intValue());
            
            props.setLocalPort((Integer.valueOf((conf.get("localPort")==null)?"2000":conf.get("localPort"))).intValue());
            props.setServerPort((Integer.valueOf((conf.get("serverPort")==null)?"2000":conf.get("serverPort"))).intValue());
            props.setServerIP((conf.get("serverIP")==null)?"localhost":conf.get("serverIP"));
            props.setNickname((conf.get("nickname")==null)?"Player":conf.get("nickname"));
            props.setOnlineColor(getColorFromDescription(conf.get("onlineColor")));
            
            return props;
        } else {
            return null;
        }
    }
    
    /** Saves the Properties to fileName */
    public boolean save(String fileName) {
        Configuration conf = new Configuration(fileName);
        conf.read();
        
        conf.set("playerColor", getPlayerColorDescription());
        conf.set("playerClass", getPlayerClassDescription());
        
        conf.set("circuitFileName", circuitFileName);
        
        conf.set("outColor", getColorDescription(outColor));
        conf.set("inColor", getColorDescription(inColor));
        conf.set("startColor", getColorDescription(startColor));
        
        conf.set("grid", ""+grid);
        conf.set("plusSpeed", ""+plusSpeed);
        conf.set("crashRadius", ""+crashRadius);
        conf.set("minusSpeed", ""+minusSpeed);
        conf.set("maxAngle", ""+maxAngle);
        conf.set("redDots", ""+redDots);
        conf.set("isCrash", ""+isCrash);
        conf.set("autoResize", ""+autoResize);
        conf.set("isRallyMode", ""+isRallyMode);
        conf.set("turn", ""+turn);
        conf.set("trace", ""+trace);
        
        conf.set("localPort", ""+localPort);
        conf.set("serverPort", ""+serverPort);
        conf.set("nickname", nickname);
        conf.set("serverIP", serverIP);
        conf.set("onlineColor", getColorDescription(onlineColor));
        
        return conf.write();
    }
    
    /** Gets the description of a color: R,G,B,Alpha,... */
    private static String getColorDescription(Color c) {
            return c.getRed()+","+c.getGreen()+","+c.getBlue()+","+c.getAlpha();
    }
    
    /** Gets a color from the description: R,G,B,Alpha,... */
    private static Color getColorFromDescription(String des) {
        try {
            String[] tmp = des.split(",");
            return new Color((Integer.valueOf(tmp[0])).intValue(), (Integer.valueOf(tmp[1])).intValue(), (Integer.valueOf(tmp[2])).intValue(), (Integer.valueOf(tmp[3])).intValue());
        } catch(Exception e) {
            return Color.black;
        }
    }
    
    /** Gets the description of the playerColor hash: playerName,R,G,B,Alpha,... */
    private String getPlayerColorDescription() {
        Color c;
        String playerName;
        String des = "";
        for (Enumeration e = playerColor.keys(); e.hasMoreElements();) {
            playerName = (String)e.nextElement();
            c = (Color)playerColor.get(playerName);
            des += playerName+","+c.getRed()+","+c.getGreen()+","+c.getBlue()+","+c.getAlpha()+",";
        }
        des = des.substring(0, des.length()-1);
        return des;
    }
    
    /** Gets the playerColor hash from the description: playerName,R,G,B,Alpha,... */
    private static Hashtable getPlayerColorFromDescription(String des) {
        Hashtable hash = new Hashtable();
        Color c;
        String playerName;
        try {
            String[] tmp = des.split(",");
            for (int i=0; i<tmp.length; i+=5) {
                playerName = tmp[i];
                c = new Color((Integer.valueOf(tmp[i+1])).intValue(), (Integer.valueOf(tmp[i+2])).intValue(), (Integer.valueOf(tmp[i+3])).intValue(), (Integer.valueOf(tmp[i+4])).intValue());
                hash.put(playerName, c);
            }
            if (hash.size() < 1) {
                hash.clear();
                hash.put("Bob",Color.red);
                hash.put("Jean",Color.blue);
            }
        } catch(Exception e) {
            hash.clear();
            hash.put("Bob",Color.red);
            hash.put("Jean",Color.blue);
        }
        return  hash;
    }
    
    /** Gets the description of the playerClass hash: playerName,className,... */
    private String getPlayerClassDescription() {
        String cls;
        String playerName;
        String des = "";
        for (Enumeration e = playerClass.keys(); e.hasMoreElements();) {
            playerName = (String)e.nextElement();
            cls = (String)playerClass.get(playerName);
            des += playerName+","+cls+",";
        }
        des = des.substring(0, des.length()-1);
        return des;
    }
    
    /** Gets the playerClass hash from the description: playerName,className,... */
    private static Hashtable getPlayerClassFromDescription(String des) {
        Hashtable hash = new Hashtable();
        try {
            String[] tmp = des.split(",");
            for (int i=0; i<tmp.length; i+=2) {
                hash.put(tmp[i], tmp[i+1]);
            }
            if (hash.size() < 1) {
                hash.clear();
                hash.put("Bob","HumanPlayer");
                hash.put("Jean","HumanPlayer");
            }
        } catch(Exception e) {
            hash.clear();
            hash.put("Bob","HumanPlayer");
            hash.put("Jean","HumanPlayer");
        }
        return  hash;
    }
    
    public int getCrashRadius() {
        return this.crashRadius;
    }
    
    public void setCrashRadius(int cr) {
        this.crashRadius = cr;
    }
    
    public String getNickname() {
        return this.nickname;
    }
    
    public void setNickname(String nn) {
        this.nickname = nn;
    }
    
    public String getServerIP() {
        return this.serverIP;
    }
    
    public void setServerIP(String ip) {
        this.serverIP = ip;
    }
    
    public int getLocalPort() {
        return this.localPort;
    }
    
    public void setLocalPort(int p) {
        this.localPort = p;
    }
    
    public int getServerPort() {
        return this.serverPort;
    }
    
    public void setServerPort(int p) {
        this.serverPort = p;
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
    
    public void setAutoResize(boolean b) {
        this.autoResize = b;
    }
    
    public boolean getAutoResize() {
        return this.autoResize;
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
    
    public void setOutColor(Color c) {
        this.outColor = c;
    }
    
    public void setInColor(Color c) {
        this.inColor = c;
    }
    
    public void setStartColor(Color c) {
        this.startColor = c;
    }
    
    public void setOnlineColor(Color c) {
        this.onlineColor = c;
    }
    
    public Color getOutColor() {
        return this.outColor;
    }
    
    public Color getInColor() {
        return this.inColor;
    }
    
    public Color getOnlineColor() {
        return this.onlineColor;
    }
    
    public Color getStartColor() {
        return this.startColor;
    }
    
    public void setCircuitFileName(String s) {
        if (s != null)
            this.circuitFileName = s;
    }
    
    public String getCircuitFileName() {
        return this.circuitFileName;
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
    
    public Hashtable getPlayerClass() {
        return this.playerClass;
    }
    
    public Hashtable getPlayerColor() {
        return this.playerColor;
    }
    
    public void setPlayerClass(Hashtable playerClass) {
        this.playerClass = playerClass;
    }
    
    public void setPlayerColor(Hashtable playerColor) {
        this.playerColor = playerColor;
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
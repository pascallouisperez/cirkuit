package cirkuit.remote;

import java.net.*;
import java.io.*;

import cirkuit.circuit.Circuit;
import cirkuit.properties.Properties;

/**
 * This object takes care of the communication with the server. It analyses the
 * commands received and makes sure the server respects the defined protocol. Then,
 * it tries to execute the related commands on the game managers.<br />
 * <br />
 * This object also enables to analyse the network flow and send the informations in a
 * a formated way to the user.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class ServerSocketAnalyser extends SocketAnalyser {
    // should the thread continue
    private volatile boolean run = true;
    
    // for all the special get commands
    private static final int SPECIALGETSTATE_NOGET          = -1;
    private static final int SPECIALGETSTATE_GETCIRCUIT_1    = 1;
    private static final int SPECIALGETSTATE_GETCIRCUIT_2    = 2;
    private static final int SPECIALGETSTATE_GETPROPERTIES_1 = 3;
    private static final int SPECIALGETSTATE_GETPROPERTIES_2 = 4;
    private int specialGetState = SPECIALGETSTATE_NOGET;
    private StringBuffer buffer = new StringBuffer();
    private Circuit circuit = null;
    private Properties properties = null;
    private boolean propertiesCompleted = false;
    
    /**
     * Constructs a new server socket analyser.
     * @param socket a server's socket to communicate
     */
    public ServerSocketAnalyser(CommandSocket socket) {
        super(socket);
    }
    
    /**
     */
    public void run() {
        try {
            String command = null;
            
            while (run) {
                getCommandLine();
                // analysing
                command = getNextCommand();
                System.out.println(command + getInBuffer().toString());
                if (command != null) {
                    if (command.equals("WELC")) {
                        setSocketState(STATE_WELC);
                    }
                    inform(command);
                }
                Thread.yield();
            }
        } catch (Exception e) {
            System.err.println("Unable to communicate");
            e.printStackTrace();       
        }
    }
    
    /**
     * Get the game properties. This method will send a GGIN command to the server and
     * analyse the ouptut. It will then send back a Properties object.
     * @return the game's properties
     */
    public Properties getProperties(String gameName) {
        propertiesCompleted = false;
        Properties tmp = null;
        try {
            properties = new Properties();
            setIsolated(true);
            specialGetState = SPECIALGETSTATE_GETPROPERTIES_1;
            reply("GGIN "+stringEncode(gameName));
            Thread.yield();
            while (!propertiesCompleted); // wait
            tmp = properties;
            properties = null;
            propertiesCompleted = false;
        } catch (Exception e) {}
        return tmp;
    }
    
    /**
     * Get the specified circuit. This method will send a GETC command to the server and
     * analyse the ouptut. It will then send back a Circuit object.
     * @return the circuit
     */
    public Circuit getCircuit(String circuitName) {
        circuit = null;
        Circuit tmp = null;
        try {
            specialGetState = SPECIALGETSTATE_GETCIRCUIT_1;
            setIsolated(true);
            reply("GETC "+stringEncode(circuitName));
            Thread.yield();
            while (circuit == null); // wait
            tmp = circuit;
            circuit = null;
        } catch (Exception e) {}
        return tmp;
    }
    
    public void dataArrived(String command) {
        switch (specialGetState) {
            case SPECIALGETSTATE_GETCIRCUIT_1:
                if (command.equals("CINF")) {
                    specialGetState = SPECIALGETSTATE_GETCIRCUIT_2;
                    buffer.delete(0, buffer.length());
                }
                break;
                
            case SPECIALGETSTATE_GETCIRCUIT_2:
                if (command.equals("CINE")) {
                    specialGetState = SPECIALGETSTATE_NOGET;
                    setIsolated(false);
                    circuit = new Circuit(buffer.toString());
                } else {
                    buffer.append(command);
                    buffer.append(getInBuffer());
                }
                break;
                
            case SPECIALGETSTATE_GETPROPERTIES_1:
                if (command.equals("GINF")) {
                    specialGetState = SPECIALGETSTATE_GETPROPERTIES_2;
                    buffer.delete(0, buffer.length());
                }
                break;
                
            case SPECIALGETSTATE_GETPROPERTIES_2:
                if (command.equals("GINE")) {
                    specialGetState = SPECIALGETSTATE_NOGET;
                    setIsolated(false);
                    propertiesCompleted = true;
                } else if (command.equals("PRCF")) {
                    String s = getNextString();
                    if (s != null)
                        properties.setCircuitFileName(s);
                } else if (command.equals("PRGR")) {
                    int i = getNextInt(-1);
                    if (i>=0)
                        properties.setGrid(i);
                } else if (command.equals("PRMS")) {
                    int i = getNextInt(-1);
                    if (i>=0)
                        properties.setMinusSpeed(i);
                } else if (command.equals("PRPS")) {
                    int i = getNextInt(-1);
                    if (i>=0)
                        properties.setPlusSpeed(i);
                } else if (command.equals("PRCR")) {
                    int i = getNextInt(-1);
                    if (i>=0)
                        properties.setCrashRadius(i);
                } else if (command.equals("PRMA")) {
                    int i = getNextInt(-1);
                    if (i>=0)
                        properties.setMaxAngle(Math.toRadians(i));
                } else if (command.equals("PRRD")) {
                    properties.setRedDots(getNextBoolean());
                } else if (command.equals("PRIC")) {
                    properties.setIsCrash(getNextBoolean());
                } else if (command.equals("PRMD")) {
                    int i = getNextInt(-1);
                    if (i>=0)
                        properties.setMode(i);
                } else if (command.equals("PRTN")) {
                    int i = getNextInt(-1);
                    if (i>0)
                        properties.setTurn(i);
                } else if (command.equals("PRTC")) {
                    int i = getNextInt(-1);
                    if (i>=0)
                        properties.setTrace(i);
                }
                break;
        }
    }
    
    public void setRun(boolean run) {
        this.run = run;
    }
}
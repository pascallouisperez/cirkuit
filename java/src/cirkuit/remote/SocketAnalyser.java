package cirkuit.remote;

import java.net.*;
import java.io.*;
import java.awt.Color;
import java.util.Vector;

/**
 * This object takes care of the communication with the client. It analyses the
 * commands received and makes sure the client respects the defined protocol. Then,
 * it tries to execute the related commands on the game managers.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public abstract class SocketAnalyser extends Thread implements SocketListener {
    /**
     * State variable. Normal state.
     */
    public static final int STATE_NORMAL = 0;
    /**
     * State variable. The server just welcomed the user using the WELC command,
     * we are now waiting for an INFO reply.
     */
    public static final int STATE_WELC = 1;
    /**
     * State variable. The client created a new game, the server is wainting for people to
     * join the game, for the game's creator to select the circuit and the game options and
     * for a start signal.
     */
    public static final int STATE_MAKE = 2;
    
    /**
     * Waiting for STRT
     */
    public static final int STATE_WAIT = 3;
    
    /**
     * Actually playing
     */
    public static final int STATE_PLAY = 4;
    
    public static final int STATE_MPLAY = 5;
    
    /**
     * Waiting for REDY command
     */
    public static final int STATE_WAITING = 6;
    
    public static final int STATE_MWAITING = 7;
    
    /**
     * Client state's
     */
    public static final int STATE_WAITINGFORJOIN = 8;
    
    public static final int STATE_WAITINGFORMAKE = 9;
    
    public static final int STATE_CSELBEFOREMAKE = 10;
    
    public static final int STATE_WAITINGFORQTGM = 11;
    
    public static final int STATE_WAITINGFORCSEL = 12;
    
    public static final int STATE_WAITINGFORCLIS = 13;
    
    public static final int STATE_INIT01 = 14;
    
    public static final int STATE_INIT02 = 15;
    
    // This analyser's state
    private int state = STATE_WELC;
    
    // a command socket to communicate
    private CommandSocket socket = null;
    
    // inputs and outputs buffer
    private StringBuffer inBuffer  = new StringBuffer(256);
    protected StringBuffer outBuffer = new StringBuffer(256);
    
    // client specific informations
    private String username = null;
    
    // listeners
    private boolean isolated = false;
    protected Vector socketListeners = new Vector();
    
    /**
     * Constructs a new socket analyser. This thread is a deamon.
     * @param socket a command socket to communicate
     */
    public SocketAnalyser(CommandSocket socket) {
        this.socket = socket;
        setDaemon(true);
    }
    
    /**
     * Core of the analyser.
     */
    public abstract void run();
    
    /**
     * Inform all listeners if the analyser is not isolated. If the socket is isolated,
     * the only listener informed will be itself, enabling the developpers to have a full
     * control on the way the network flow should be spread through the application.
     * @param command the command to pass to the <code>dataArrived</code> method which the
     * listeners implement
     * @see cirkuit.remote.SocketAnalyser#setIsolated
     * @see cirkuit.remote.SocketAnalyser#isIsolated
     */
    public void inform(String command) {
        if (!isolated) {
            int n = socketListeners.size();
            String str;
            for (int i=0; i<n; i++) {
                str = getInBuffer().toString();
                ((SocketListener)socketListeners.get(i)).dataArrived(command);
                setInBuffer(str);
            }
        } else {
            this.dataArrived(command);
        }
    }
    
    /**
     * Set wether the analyser is, or not, isolated. If an analyser is isolated, the listeners
     * will not hear it.
     * @param isolated true to isolate the analyser, false otherwise
     * @see cirkuit.remote.SocketAnalyser#inform
     * @see cirkuit.remote.SocketAnalyser#isIsolated
     */
    public void setIsolated(boolean isolated) {
        this.isolated = isolated;
    }
    
    /**
     * Tells wether the socket is isolated or not.
     * @return true is the socket is isolated, false otherwise
     * @see cirkuit.remote.SocketAnalyser#inform
     * @see cirkuit.remote.SocketAnalyser#setIsolated
     */
    public boolean isIsolated() {
        return this.isolated;
    }
    
    /**
     * Set the socket.
     * @param socket the new socket
     */
    public void setSocket(CommandSocket socket) {
        this.socket = socket;
    }
    
    /**
     * Reads the socket's input and places the next command line in the input buffer.<br>
     * The line feed '\n' is not appended to the command line but the carriage return is.
     */
    public void getCommandLine() throws IOException {
        System.out.println("getCommandLine, start");
        char c;
        // flushing the input buffer
        inBuffer.delete(0, inBuffer.length());
        
        // getting the command
        while((c = (char)socket.in.read()) != '\n' && c != (char)-1) {
            inBuffer.append(c);
            System.out.print("#");
        }
        System.out.println(".");
        System.out.println("getCommandLine, end");
    }
    
    /**
     * Gets the command from the input buffer.
     * @return a 4 caracter long command or null if the command buffer did not
     * contain a valid command
     */
    public String getNextCommand() {
        int l = inBuffer.length();
        if (l >= 4) {
            String command = inBuffer.substring(0, 4);
            inBuffer.delete(0, 4);
            return command;
        } else {
            return null;
        }
    }
    
    /**
     * Gets the current inBuffer.
     */
    public StringBuffer getInBuffer() {
        return inBuffer;
    }
     
    /**
     * Sets the current inBuffer.
     */
    public void setInBuffer(String str) {
        inBuffer.delete(0, inBuffer.length());
        inBuffer.append(str);
    }
    
    /**
     * Deletes the next separator present on the input buffer.
     * @return true if a separator was found, false otherwise
     */
    public boolean deleteNextSeparator() {
        int l = inBuffer.length();
        int end = -1;
        int i = 0;
        char c;
        
        // searching end
        while (end < 0 && i < l) {
            c = inBuffer.charAt(i);
            if (c == ',') {
                end = i+1;
            } else if (c == ' ') {
                i++;
            } else {
                return false;
            }
        }
        if (end < 0) {
            end = l;
        }
        inBuffer.delete(0, end);
        return true;
    }
    
    /**
     * Gets the next boolean from the input buffer.
     * @return the next boolean in the input buffer (0 - false, 1 - true)
     */
    public boolean getNextBoolean() {
        return (getNextInt(0) == 1);
    }
    
    /**
     * Encodes the boolean
     * @return 0 if false, 1 if true)
     */
    public int booleanEncode(boolean b) {
        return ((b)?1:0);
    }
    
    /**
     * Gets the next integer from the input buffer.
     * @param error the desired integer to return in case of error
     * @return the next integer in the input buffer, the error integer otherwise
     */
    public int getNextInt(int error) {
        int l = inBuffer.length();
        int result = error;
        int start = -1, end = -1;
        int i = 0;
        char c;
        
        // searching start
        while (start < 0 && i < l) {
            c = inBuffer.charAt(i);
            if (c == ' ' || c == ',') {
                i++;
            } else {
                start = i;
                i++;
            }
        }
        // searching end
        while (end < 0 && i < l) {
            c = inBuffer.charAt(i);
            if (c == ',' || c == (int)13) {
                end = i;
            } else if (Character.isDigit(c)) {
                i++;
            } else {
                return error;
            }
        }
        if (end < 0) {
            end = l;
        }
        // sending back the result
        try {
            result = Integer.parseInt(inBuffer.substring(start, end));
            inBuffer.delete(0, end);
            return result;
        } catch(Exception e) {
            return error;
        }
    }
    
    /**
     * Gets the next double from the input buffer.
     * @param error the desired double to return in case of error
     * @return the next double in the input buffer, the error double otherwise
     */
    public double getNextDouble(double error) {
        int l = inBuffer.length();
        double result = error;
        int start = -1, end = -1;
        int i = 0;
        char c;
        
        // searching start
        while (start < 0 && i < l) {
            c = inBuffer.charAt(i);
            if (c == ' ' || c == ',') {
                i++;
            } else {
                start = i;
                i++;
            }
        }
        // searching end
        while (end < 0 && i < l) {
            c = inBuffer.charAt(i);
            if (c == ',' || c == (int)13) {
                end = i;
            } else if (Character.isDigit(c) || c=='.') {
                i++;
            } else {
                return error;
            }
        }
        if (end < 0) {
            end = l;
        }
        // sending back the result
        try {
            result = Double.parseDouble(inBuffer.substring(start, end));
            inBuffer.delete(0, end);
            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return error;
        }
    }
    
    /**
     * Gets a string from the input buffer. If the buffer contains a valid string, it will
     * be modified in the process.<br>
     * A valid string complies with the following rules:<br>
     * - it starts and ends with a " (double-quotes)<br>
     * - the '"' character is represented by '\"' and the '\' character by '\\'
     * @return a string or null if the command buffer did not contain a valid string
     */
    public String getNextString() {
        int l = inBuffer.length();
        StringBuffer result = new StringBuffer(l);
        int start = -1, end = -1;
        int i = 0;
        char c;
        
        // searching the start
        while (start < 0 && i < l) {
            c = inBuffer.charAt(i);
            if (c == '"') {
                start = i;
                i++;
            } else if (c == ' ' || c == ',') {
                i++;
            } else {
                return null;
            }
        }
        
        // constructing the string
        while (end < 0 && i < l) {
            c = inBuffer.charAt(i);
            if (c == '"') {
                end = i+1;
            } else if (c == '\\') {
                i++;
                if (i < l) {
                    result.append(inBuffer.charAt(i));
                }
            } else {
                result.append(c);
            }
            i++;
        }
        if (end < 0) {
            return null;
        }
        inBuffer.delete(0, end);
        return result.toString();
    }
    
    /**
     * Gets a color from the input buffer.
     * @return a Color object, null if there wasn't an color on the input buffer
     */
    public Color getNextColor() {
        int r, g, b;
        r = getNextInt(-1);
        if (r >= 0) {
            deleteNextSeparator();
            g = getNextInt(-1);
            if (g >= 0) {
                deleteNextSeparator();
                b = getNextInt(-1);
                if (r < 256 && g < 256 && b < 256) {
                    return new Color(r, g, b);
                }
            }
        }
        return null;
    }
    
    /**
     * Encodes the string...
     * @param s the string to encore
     * @return the encoded string
     */
    public static String stringEncode(String s) {
        if (s != null) {
            int l = s.length();
            char c;
            StringBuffer sb = new StringBuffer((l<<1)+2);
            sb.append('"');
            for (int i = 0; i < l; i++) {
                c = s.charAt(i);
                if (c == '"' || c == '\\') {
                    sb.append('\\');
                }
                sb.append(c);
            }
            sb.append('"');
            return sb.toString();
        }
        return null;
    }
    
    /**
     * Reply.
     * @param s the string to reply (not including the leading line feed '\n')
     */
    public synchronized void reply(String s) throws IOException {
        socket.out.write((s+"\n").getBytes());
    }
    
    /**
     * Closes the connection.
     */
    public void close() {
        try {
            socket.in.close();
            socket.out.close();
        } catch(Exception e) {
            System.err.println("Unable to close the connection\n"+e);
        }
    }
    
    /**
     * Get the state.
     * @return the state
     */
    public int getSocketState() {
        return this.state;
    }
    
    /**
     * Set the state.
     * @param state a state variable
     */
    public void setSocketState(int state) {
        this.state = state;
    }

    /**
     * Add a socket listener.
     * @param sl the socket listener
     */
    public boolean addSocketListener(SocketListener sl) {
        return socketListeners.add(sl);
    }

    /**
     * Remove a socket listener
     * @param sl the socket listener to remove
     */
    public boolean removeSocketListener(SocketListener sl) {
        return socketListeners.remove(sl);
    }    
}
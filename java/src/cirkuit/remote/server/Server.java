package cirkuit.remote.server;

import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Hashtable;
import java.awt.Color;

import cirkuit.player.Player;
import cirkuit.remote.*;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class Server extends Thread {
    private ServerConnectionLaucher launcher = null;
    private ServerSocket socket = null;
    private Vector clientSocketAnalyser = new Vector(); // vector of ClientSocketAnalyser
    
    // namespaces
    public Hashtable usernames = new Hashtable(20);
    public Hashtable gamenames = new Hashtable(20);
    
    private static String circuitFolder = "./shared/circuits/";
    
    private volatile boolean run = true;
    
    /**
     * Creates a server class listening on the specified port.
     * @param port the port to listen on
     */
    public Server(int port) throws IOException {
        this.socket = new ServerSocket(port);
        System.out.println("Server started on port "+socket.getLocalPort()+"\t[ OK ]");
    }
    
    /**
     * Creates a server class listening on the first free port.
     */
    public Server() throws IOException {
        this(0);
    }
    
    public void run() {
        launcher = new ServerConnectionLaucher(this);
        launcher.start();
        while (run)
            Thread.yield();
        System.out.println("Server is shutting down");
    }
    
    /**
     * A call to this method will make the thread finish it's job and die.
     */
    public void poweroff() {
        run = false;
        int l = clientSocketAnalyser.size();
        for (int i = 0; i < l; i++)
            ((ClientSocketAnalyser)clientSocketAnalyser.get(i)).poweroff();
    }
    
    /**
     * Registers a client.
     * @param sa the SocketAnalyser
     * @param un the desired username
     * @return the user's accepted username
     */
    synchronized public String registerClient(SocketAnalyser sa, String un, Color c) {
        String username = null;
        if (un != null && c != null) {
            while (usernames.containsKey(un)) {
                un = un+"*";
            }
            username = un;
            usernames.put(username, new UserInfo(sa, c));
        }
        return username;
    }
    
    /**
     * Deletes the specified client.
     * @param username the client's username
     */
    synchronized public void deleteClient(String username) {
        if (username != null) {
            usernames.remove(username);
        }
    }
    
    /**
     * Registers a game.
     * @param un the user's username
     * @param gn the desired gamename
     * @return the accepted gamename
     */
    public synchronized String registerGame(String un, String gn) {
        String gamename = null;
        if (un != null && gn != null) {
            if (usernames.containsKey(un)) {
                while (gamenames.containsKey(gn)) {
                    gn = gn+"*";
                }
                gamenames.put(gn, new GameInfo());
                ((UserInfo)usernames.get(un)).setGameName(gn);
                gamename = gn;
            }
        }
        return gamename;
    }
    
    /**
     * Make a user join a game
     */
    public synchronized boolean join(ClientSocketAnalyser sa, String un, String gn) {
        if (gn != null && gamenames.containsKey(gn)) {
            GameInfo gi = (GameInfo)gamenames.get(gn);
            UserInfo ui = (UserInfo)usernames.get(un);
            RemotePlayer rp = new RemotePlayer(sa, un, ui.getColor());
            gi.addPlayer(rp);
            gi.addGameListener(sa);
            ui.setJoinedGame(gn);
            return true;
        }
        return false;
    }
    
    /**
     * Make a user quit a game
     */
    public synchronized boolean unjoin(ClientSocketAnalyser sa, String un) {
        if (un != null) {
            UserInfo ui = (UserInfo)usernames.get(un);
            String gn = ui.getJoinedGame();
            ui.setJoinedGame(null);
            if (gn != null) {
                GameInfo gi = (GameInfo)gamenames.get(gn);
                if (gi != null) {
                    gi.removePlayer(un);
                    if (sa != null)
                        gi.removeGameListener(sa);
                }
            }
            return true;
        } else {
            return false;   
        }
    }
    
    /** 
     * Get the gamename associated with this player
     */
    public String getUserGame(String un) {
        UserInfo ui = (UserInfo)usernames.get(un);
        if (ui != null)
            return ui.getJoinedGame();
        return null;
    }
    
    /**
     * Set a game's acceleration.
     */
    public synchronized boolean setGameAcceleration(String gamename, int acceleration) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setPlusSpeed(acceleration);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's deceleration.
     */
    public synchronized boolean setGameDeceleration(String gamename, int deceleration) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setMinusSpeed(deceleration);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's red dots.
     */
    public synchronized boolean setGameHints(String gamename, boolean rd) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setRedDots(rd);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's crash.
     */
    public synchronized boolean setGameCrash(String gamename, boolean c) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setIsCrash(c);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's crash radius.
     */
    public synchronized boolean setGameCrashRadius(String gamename, int cr) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setCrashRadius(cr);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's trace length.
     */
    public synchronized boolean setGameTrace(String gamename, int t) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setTrace(t);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's max angle.
     */
    public synchronized boolean setGameMaxAngle(String gamename, double angle) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setMaxAngle(angle);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's mode.
     */
    public synchronized boolean setGameMode(String gamename, int mode) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setMode(mode);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's grid size.
     */
    public synchronized boolean setGameGrid(String gamename, int grid) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setGrid(grid);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's number of laps.
     */
    public synchronized boolean setGameNumLaps(String gamename, int l) {
       if (gamenames.containsKey(gamename)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setTurn(l);
           return true;
       }
       return false;
    }
    
    /**
     * Set a game's circuit.
     */
    public synchronized boolean setGameCircuitName(String gamename, String cn) {
       if (gamenames.containsKey(gamename) && getCircuitVector().contains(cn)) {
           GameInfo gi = (GameInfo)(gamenames.get(gamename));
           gi.props.setCircuitFileName(circuitFolder+cn);
           return true;
       }
       return false;
    }
    
    /**
     * Destroy the game created by username.
     */
    public synchronized boolean destroyGame(String un) {
       if (un != null && usernames.containsKey(un)) {
           UserInfo ui = (UserInfo)(usernames.get(un));
           if (ui.getGameName() != null) {
               sendToHisPlayers(un, "KICK \"the game was destroyed\"");
               gamenames.remove(ui.getGameName());
               ui.setGameName(null);
               ui.setJoinedGame(null);
               return true;
           }
       }
       return false;
    }
    
    /**
     * Starts the game created by username
     */
    public synchronized boolean startGame(String un) {
        if (usernames.containsKey(un)) {
           UserInfo ui = (UserInfo)(usernames.get(un));
           if (ui.getJoinedGame() != null) {
               GameInfo gi = (GameInfo)gamenames.get(ui.getJoinedGame());
               return gi.startGame();
           }
       }
       return false;
    }
    
    /**
     * Sets the ready attribute in UserInfo
     * @return true if possible
     */
    public synchronized boolean setReady(String un, boolean b) {
        UserInfo ui = (UserInfo)(usernames.get(un));
        if (ui != null) {
            ui.setReady(b);
            return true;
        }
        return false;
    }
    
    /**
     * @return true if everyone in the game is ready
     */
    public synchronized boolean isEveryoneReady(String un) {
        if (un != null) {
            UserInfo ui = (UserInfo)(usernames.get(un));
            if (ui != null) {
                String gn = ui.getJoinedGame();
                if (gn != null) {
                    GameInfo gi = (GameInfo)gamenames.get(gn);
                    Vector vp = gi.getPlayers();
                    int n = vp.size();
                    String username;
                    for (int i=0; i<n; i++) {
                        username = ((Player)vp.get(i)).getName();
                        ui = (UserInfo)usernames.get(username);
                        if (!ui.isReady())
                            return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Sets All Other Players State in the game. Only a game creater can do this.
     * @return true if possible
     */
    public synchronized boolean setAOPState(String un, int state) {
        UserInfo ui = (UserInfo)(usernames.get(un));
        String gn = ui.getGameName();
        if (gn != null) {
            GameInfo gi = (GameInfo)gamenames.get(gn);
            Vector vp = gi.getPlayers();
            int n = vp.size();
            String username;
            SocketAnalyser sa;
            for (int i=0; i<n; i++) {
                username = ((Player)vp.get(i)).getName();
                if (!username.equals(un)) {
                    sa = ((UserInfo)usernames.get(username)).getSocketAnalyser();
                    sa.setSocketState(state);
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Sends a message to all players in un's game.
     * @return false if one or more client weren't reachable
     */
    public boolean sendToHisPlayers(String un, String command) {
        String gn = getUserGame(un);
        return sendToPlayers(gn, command);
    }
    
    /**
     * Sends a message to all players in gn.
     * @return false if one or more client weren't reachable
     */
    public boolean sendToPlayers(String gn, String command) {
        if (gn != null) {
            GameInfo gi = (GameInfo)gamenames.get(gn);
            Vector vp = gi.getPlayers();
            int n = vp.size();
            String username;
            SocketAnalyser sa;
            boolean ret = true;
            for (int i=0; i<n; i++) {
                username = ((Player)vp.get(i)).getName();
                sa = ((UserInfo)usernames.get(username)).getSocketAnalyser();
                try {
                    sa.reply(command);
                } catch(IOException e) {
                    ret = false;
                }
            }
            return ret;
        }
        return false;
    }
    
    /**
     * Sends the game info.
     * @return false if the client was not reachable
     */
    public boolean sendGameInfo(String gn, SocketAnalyser sa) {
        if (gn != null) {
            GameInfo gi = (GameInfo)gamenames.get(gn);
            try {
                gi.sendNetworkDescription(sa);
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Sends the game info for a starting game.
     * @return false if one or more client weren't reachable
     */
    public boolean sendGameInfo(String un) {
        String gn = getUserGame(un);
        if (gn != null) {
            GameInfo gi = (GameInfo)gamenames.get(gn);
            Vector vp = gi.getPlayers();
            int n = vp.size();
            String username;
            SocketAnalyser sa;
            boolean ret = true;
            for (int i=0; i<n; i++) {
                username = ((Player)vp.get(i)).getName();
                sa = ((UserInfo)usernames.get(username)).getSocketAnalyser();
                try {
                    sa.reply("STIG");
                    gi.sendNetworkDescription(sa);
                } catch (IOException e) {
                    ret = false;
                }
            }
            return ret;
        }
        return false;
    }
    
    /**
     * Get the circuit list from the 'circuitFolder' folder.
     * @return Array of circuits' filename
     */
    public String[] getCircuitList() {
        Vector v = getCircuitVector();
        int n = v.size();
        String[] s = new String[n];
        for (int i=0; i<n; i++)
            s[i] = (String)v.get(i);
        return s;
    }
    
    /**
     * Get the circuit list from the 'circuitFolder' folder.
     * @return Vector of circuits' filename
     */
    private Vector getCircuitVector() {
        File dir = new File(circuitFolder);
        if (!dir.exists())
            return null;
        String[] tFiles = dir.list();
        
        Vector circuitList = new Vector();
        int maxI = tFiles.length;
        for (int i=0; i<maxI; i++) { 
            if (tFiles[i].endsWith(".ckt"))
                circuitList.add(tFiles[i]);
        }
        Collections.sort(circuitList);
        return circuitList;
    }
    
    /**
     * Get the game list.
     * @return the game list
     */
    public String[] getGameList() {
        Vector v = getGameVector();
        int n = v.size();
        String[] s = new String[n];
        for (int i=0; i<n; i++) {
            s[i] = (String)v.get(i);
        }
        return s;
    }
    
    /**
     * Get the game list.
     * @return the game list
     */
    private Vector getGameVector() {
        Enumeration en = gamenames.keys();
        Vector v = new Vector();
        while (en.hasMoreElements()) {
            v.add(en.nextElement());
        }
        Collections.sort(v);
        return v;
    }
    
    /**
     * Get the user list.
     * @return the user list
     */
    public String[] getUserList() {
        Vector v = getUserVector();
        int n = v.size();
        String[] s = new String[n];
        for (int i=0; i<n; i++) {
            s[i] = (String)v.get(i);
        }
        return s;
    }
    
    /**
     * Get the user list.
     * @return the user list
     */
    private Vector getUserVector() {
        Enumeration en = usernames.keys();
        Vector v = new Vector();
        while (en.hasMoreElements()) {
            v.add(en.nextElement());
        }
        Collections.sort(v);
        return v;
    }
    
    /**
     * Get the number of player in the specified game.
     * @param gamename the game
     * @return the number of players
     */
    public int getGamePlayersNumber(String gamename) {
       if (gamenames.containsKey(gamename)) {
           return ((GameInfo)(gamenames.get(gamename))).getPlayersNumber();
       }
       return -1;
    }
    
    /**
     * Get the circuit folder of this server.
     * @return the circuit folder of this server
     */
    public static String getCircuitFolder() {
        return circuitFolder;
    }
    
    /**
     * This is a deamon which waits for a connection. If the server needs to power down,
     * this one will go down with it!
     */
    private class ServerConnectionLaucher extends Thread {
        private Server server = null;
        
        public ServerConnectionLaucher(Server server) {
            setDaemon(true);
            this.server = server;
        }
        
        public void run() {
            try {
                ClientSocketAnalyser tmpSocketAnalyser = new ClientSocketAnalyser(server, new CommandSocket(socket.accept()));
                clientSocketAnalyser.add(tmpSocketAnalyser);
                tmpSocketAnalyser.start();
            } catch(Exception e) {}
            launcher = new ServerConnectionLaucher(server);
            launcher.start();
        }
    }
}
package cirkuit.remote;

import java.net.*;
import java.io.*;
import java.awt.Color;
import java.awt.Point;
import java.util.Vector;

import cirkuit.circuit.Circuit;
import cirkuit.game.GameListener;
import cirkuit.game.Game;
import cirkuit.util.Trajectory;
import cirkuit.player.Player;
import cirkuit.remote.*;
import cirkuit.remote.server.Server;

/**
 * This object takes care of the communication with the client. It analyses the
 * commands received and makes sure the client respects the defined protocol. Then,
 * it tries to execute the related commands on the game managers.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class ClientSocketAnalyser extends SocketAnalyser implements GameListener {
    // link to the server
    private Server server = null;
    
    // client specific informations
    private String username = null;
    
    // hjahdsjas
    private volatile boolean run = true;
    
    /**
     * Constructs a new client socket analyser.
     * @param socket a client's socket to communicate
     */
    public ClientSocketAnalyser(Server server, CommandSocket socket) {
        super(socket);
        this.server = server;
    }
    
    /**
     * The client analyser's core. Manages the communication with the distant client, anyles it's commdands,
     * and forwards the commands to the server who instanciated this object.
     */
    public void run() {
        try {
            // needed variables for the command analysis and reply
            String command = null;
            
            // welcoming the client
            reply("WELC");
            
            while (run) {
                getCommandLine();
                // analysing
                command = getNextCommand();
                System.out.println(username+": "+command + getInBuffer().toString());
                if (command != null) {
                    if (command.equals("QUIT")) {
                        remove();
                        outBuffer.append("GBYE");
                        run = false;
                    } else {
                        switch(getSocketState()) {
                            case STATE_WELC:
                                if (command.equals("INFO")) {
                                    username = getNextString(); 
                                    deleteNextSeparator();
                                    Color color = getNextColor();
                                    username = server.registerClient(this, username, color);
                                    if (username != null) {
                                        setSocketState(STATE_NORMAL);
                                        outBuffer.append("COMC "+stringEncode(username));
                                    } else {
                                        outBuffer.append("EROR \"malformed username and/or color\"");
                                    }
                                } else {
                                    outBuffer.append("EROR \"an INFO command is required after a WELC message\"");
                                }
                                break;
                            
                            case STATE_NORMAL:
                                if (command.equals("CLID")) {
                                    outBuffer.append("CLIS ");
                                    String[] circuitList = server.getCircuitList();
                                    for (int i = 0; i < circuitList.length; i++) {
                                        if (i != 0) {
                                            outBuffer.append(',');
                                        }
                                        outBuffer.append(stringEncode(circuitList[i]));
                                    }
                                } else if (command.equals("GLID")) {
                                    outBuffer.append("GLIS ");
                                    String[] gameList = server.getGameList();
                                    for (int i = 0; i < gameList.length; i++) {
                                        if (i != 0) {
                                            outBuffer.append(',');
                                        }
                                        outBuffer.append(stringEncode(gameList[i]));
                                    }
                                } else if (command.equals("MAKE")) {
                                    String gamename = getNextString();
                                    gamename = server.registerGame(username, gamename);
                                    if (gamename != null && server.join(this, username, gamename)) {
                                        setSocketState(STATE_MAKE);
                                        outBuffer.append("COMC "+stringEncode(gamename));
                                    } else {
                                        outBuffer.append("EROR \"malformed gamename\"");
                                    }
                                } else if (command.equals("JOIN")) {
                                    String gn = getNextString();
                                    if (gn != null && server.join(this, username, gn)) {
                                        setSocketState(STATE_WAIT);
                                        outBuffer.append("COMC \"you joined "+gn+"\"");
                                    } else {
                                        outBuffer.append("EROR \"malformed join or game does not exist\"");
                                    }
                                } else if (command.equals("GETC")) {
                                    String c = getNextString();
                                    Circuit circuit = new Circuit();
                                    circuit.load("./shared/circuits/"+c);
                                    outBuffer.append("CINF\n"+circuit.toString()+"\nCINE");
                                } else if (command.equals("GGIN")) {
                                    if (!server.sendGameInfo(getNextString(), this)) {
                                        outBuffer.append("EROR \"this game does not exist\"");
                                    }
                                } else {
                                    System.out.println(command + getInBuffer());
                                    outBuffer.append("EROR \"malformed command 118\"");
                                }
                                break;

                            case STATE_MAKE:
                                if (command.equals("CLID")) {
                                    outBuffer.append("CLIS ");
                                    String[] circuitList = server.getCircuitList();
                                    for (int i = 0; i < circuitList.length; i++) {
                                        if (i != 0) {
                                            outBuffer.append(',');
                                        }
                                        outBuffer.append(stringEncode(circuitList[i]));
                                    }
                                } else if (command.equals("GETC")) {
                                    String c = getNextString();
                                    Circuit circuit = new Circuit();
                                    circuit.load("./shared/circuits/"+c);
                                    outBuffer.append("CINF\n"+circuit.toString()+"\nCINE");
                                } else if (command.equals("GLID")) {
                                    outBuffer.append("GLIS ");
                                    String[] gameList = server.getGameList();
                                    for (int i = 0; i < gameList.length; i++) {
                                        if (i != 0) {
                                            outBuffer.append(',');
                                        }
                                        outBuffer.append(stringEncode(gameList[i]));
                                    }
                                } else if (command.equals("DEST")) {
                                    server.destroyGame(username);
                                    setSocketState(STATE_NORMAL);
                                    //outBuffer.append("COMC \"destroying the game\"");
                                } else if (command.equals("STRT")) {
                                    // must send a GINF command (game info) and must wait for REDY command before starting the game
                                    server.setAOPState(username,STATE_WAITING);
                                    setSocketState(STATE_MWAITING);
                                    server.sendGameInfo(username);
                                } else if (command.equals("SELA")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        int a = getNextInt(-1);
                                        if (a > -1 && server.setGameAcceleration(gn, a)) {
                                            outBuffer.append("COMC \"acceleration set\"");
                                        } else {
                                            outBuffer.append("EROR \"acceleration could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELD")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        int a = getNextInt(-1);
                                        if (a > -1 && server.setGameDeceleration(gn, a)) {
                                            outBuffer.append("COMC \"deceleration set\"");
                                        } else {
                                            outBuffer.append("EROR \"deceleration could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELT")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        int a = getNextInt(-1);
                                        if (a > -1 && server.setGameMaxAngle(gn, Math.toRadians((double)a))) {
                                            outBuffer.append("COMC \"angle set\"");
                                        } else {
                                            outBuffer.append("EROR \"angle could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELM")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        int m = getNextInt(-1);
                                        if (m > -1 && server.setGameMode(gn, m)) {
                                            outBuffer.append("COMC \"mode set\"");
                                        } else {
                                            outBuffer.append("EROR \"mode could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELG")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        int m = getNextInt(-1);
                                        if (m > -1 && server.setGameGrid(gn, m)) {
                                            outBuffer.append("COMC \"grid size set\"");
                                        } else {
                                            outBuffer.append("EROR \"grid size could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELN")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        int m = getNextInt(-1);
                                        if (m > -1 && server.setGameNumLaps(gn, m)) {
                                            outBuffer.append("COMC \"number of laps set\"");
                                        } else {
                                            outBuffer.append("EROR \"number of laps could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELC")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        String c = getNextString();
                                        if (c != null && server.setGameCircuitName(gn, c)) {
                                            outBuffer.append("COMC \"circuit name set\"");
                                        } else {
                                            outBuffer.append("EROR \"circuit name could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELH")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        boolean b = getNextBoolean();
                                        if (server.setGameHints(gn, b)) {
                                            outBuffer.append("COMC \"hints set\"");
                                        } else {
                                            outBuffer.append("EROR \"hints could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELS")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        boolean b = getNextBoolean();
                                        if (server.setGameCrash(gn, b)) {
                                            outBuffer.append("COMC \"crash set\"");
                                        } else {
                                            outBuffer.append("EROR \"crash could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELR")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        int r = getNextInt(-1);
                                        if (r != -1 && server.setGameCrashRadius(gn, r)) {
                                            outBuffer.append("COMC \"crash radius set\"");
                                        } else {
                                            outBuffer.append("EROR \"crash radius could not be set\"");
                                        }
                                    }
                                } else if (command.equals("SELE")) {
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        int t = getNextInt(-1);
                                        if (t != -1 && server.setGameTrace(gn, t)) {
                                            outBuffer.append("COMC \"trace set\"");
                                        } else {
                                            outBuffer.append("EROR \"trace could not be set\"");
                                        }
                                    }
                                } else if (command.equals("CSEL")) {
                                    // CSEL circuit, mode, acceleration, deceleration, angle, grid, numlaps, crashradius, trace, crash, hint, maxplayer
                                    String gn = ((UserInfo)server.usernames.get(username)).getGameName();
                                    if (gn != null) {
                                        String circuit = getNextString();
                                        deleteNextSeparator();
                                        int mode = getNextInt(-1);
                                        deleteNextSeparator();
                                        int acc = getNextInt(-1);
                                        deleteNextSeparator();
                                        int dec = getNextInt(-1);
                                        deleteNextSeparator();
                                        double angle = getNextDouble(-1);
                                        deleteNextSeparator();
                                        int grid = getNextInt(-1);
                                        deleteNextSeparator();
                                        int nlaps = getNextInt(-1);
                                        deleteNextSeparator();
                                        int crad = getNextInt(-1);
                                        deleteNextSeparator();
                                        int trace = getNextInt(-1);
                                        deleteNextSeparator();
                                        boolean crash = getNextBoolean();
                                        deleteNextSeparator();
                                        boolean hints = getNextBoolean();
                                        deleteNextSeparator();
                                        int maxp = getNextInt(-1);
                                        if ((circuit != null && server.setGameCircuitName(gn, circuit)) &&
                                            (mode  > -1 && server.setGameMode(gn, mode)) &&
                                            (acc   > -1 && server.setGameAcceleration(gn, acc)) &&
                                            (dec   > -1 && server.setGameDeceleration(gn, dec)) &&
                                            (angle > -1 && server.setGameMaxAngle(gn, angle)) &&
                                            (grid  > -1 && server.setGameGrid(gn, grid)) &&
                                            (nlaps > -1 && server.setGameNumLaps(gn, nlaps)) &&
                                            (crad  > -1 && server.setGameCrashRadius(gn, crad)) &&
                                            (trace > -1 && server.setGameTrace(gn, trace)) &&
                                            (server.setGameCrash(gn, crash)) &&
                                            (server.setGameHints(gn, hints)) &&
                                            (maxp > -1 && maxp < 9)) { // add setGameMaxPlayer(gn, maxp)
                                            outBuffer.append("COMC \"everything could be set\"");
                                        } else {
                                            outBuffer.append("EROR \"one or more could not be set\"");
                                        }
                                    }
                                } else if (command.equals("QTGM")) {
                                    server.destroyGame(username);
                                    setSocketState(STATE_NORMAL);
                                    outBuffer.append("COMC \"quit accepted\"");
                                } else if (command.equals("CHAT")) {
                                    String mes = getNextString();
                                    if (mes != null)
                                        server.sendToHisPlayers(username, "MESG "+stringEncode(username)+","+stringEncode(mes));
                                } else if (command.equals("GGIN")) {
                                    if (!server.sendGameInfo(getNextString(), this))
                                        outBuffer.append("EROR \"this game does not exist\"");
                                } else {
                                    outBuffer.append("EROR \"malformed command 249\"");
                                }
                                break;
                                
                            case STATE_WAIT:
                                if (command.equals("CHAT")) {
                                    String mes = getNextString();
                                    if (mes != null)
                                        server.sendToHisPlayers(username, "MESG "+stringEncode(username)+","+stringEncode(mes));
                                } else if (command.equals("QTGM")) {
                                    server.unjoin(this, username);
                                    setSocketState(STATE_NORMAL);
                                    outBuffer.append("COMC \"quit accepted\"");
                                } else {
                                    outBuffer.append("EROR \"malformed command 263\"");
                                }
                                break;
                                
                            case STATE_MWAITING:
                                if (command.equals("REDY")) {
                                    setSocketState(STATE_MPLAY);
                                    server.setReady(username, true);
                                    if (server.isEveryoneReady(username)) {
                                        server.startGame(username);
                                    }
                                } else {
                                    outBuffer.append("EROR \"malformed command 274\"");
                                }
                                break;
                                
                            case STATE_WAITING:
                                if (command.equals("REDY")) {
                                    setSocketState(STATE_PLAY);
                                    server.setReady(username, true);
                                    if (server.isEveryoneReady(username)) {
                                        server.startGame(username);
                                    }
                                } else {
                                    outBuffer.append("EROR \"malformed command 283\"");
                                }
                                break;
                                
                            case STATE_MPLAY: // just to keep in mind who's the game master
                            case STATE_PLAY:
                                if (command.equals("MOVE")) {
                                    inform("MOVE");
                                } else if (command.equals("CHAT")) {
                                    server.sendToHisPlayers(username, "MESG "+stringEncode(username)+","+stringEncode(getNextString()));
                                } else if (command.equals("QTGM")) {
                                    if (getSocketState() == STATE_MPLAY) {
                                        server.destroyGame(username);
                                    } else {
                                        server.unjoin(this, username);
                                    }
                                    setSocketState(STATE_NORMAL);
                                    outBuffer.append("COMC \"quit accepted\"");
                                } else {
                                    outBuffer.append("EROR \"malformed command 302\"");
                                }
                                break;
                        }
                    }
                } else {
                    outBuffer.append("EROR \"malformed command 308\"");
                }
                
                if (outBuffer.length() > 0) {
                    reply(outBuffer.toString());
                    outBuffer.delete(0, outBuffer.length());
                }
                Thread.yield();
            }
        } catch(Exception e) {
            remove();
        }
        close();
    }
    
    /**
     * This method will never be invoke, it's not implemented.
     */
    public void dataArrived(String command) {}
    
    /**
     * A call to this method will make the thread finish it's job and die.
     */
    public void poweroff() {
        run = false;
    }
    
    /**
     * Remove the user.
     */
    private void remove() {
        server.unjoin(this, username);
        server.destroyGame(username);
        server.deleteClient(username);
    }
    
    /**
     * The game just started.
     */
    public void gameStarted() {
        System.out.println(username+": Game started");
        server.setReady(username, false);
        try {
            reply("GLGS");
        } catch(IOException e) { remove(); }
    }
    
    /**
     * The game just finished.
     */
    public void gameFinished() {
        System.out.println(username+": Game finished");
        if (getSocketState() == STATE_MPLAY) {
            setSocketState(STATE_MAKE);
        } else if (getSocketState() == STATE_MPLAY) {
            setSocketState(STATE_WAIT);
        }
        try {
            reply("GLGF");
        } catch(IOException e) { remove(); }
    }
    
    /**
     * A player arrived with the specified time.
     * @param p the player who arrived
     * @param time the time it took the player to complete the track
     */
    public void playerArrived(Player p, double time) {
        System.out.println(username+": "+p.getName()+" arrived with "+time);
        try {
            reply("GLPA "+stringEncode(p.getName())+","+time);
        } catch(IOException e) { remove(); }
    }
    
    /**
     * A player just went out.
     * @param p the player who went out
     */
    public void playerOut(Player p) {
        System.out.println(username+": "+p.getName()+" doesn't know how to drive");
        try {
            reply("GLPO "+stringEncode(p.getName()));
        } catch(IOException e) { remove(); }
    }
    
    /**
     * Some players crashed.
     * @param v the vector of involved players
     */
    public void crash(Vector v) {
        System.out.println(username+": "+v+" crashed");
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<v.size(); i++) {
            buffer.append(stringEncode(((Player)v.get(i)).getName()));
            if (i < v.size()-1) {
                buffer.append(',');
            }
        }
        try {
            reply("GLCH "+buffer.toString());
        } catch(IOException e) { remove(); }
    }
    
    /**
     * The game changed, this has to be called after nextPlayer.
     * @param g the game
     */
    public void gameChanged(Game g) {
        System.out.println(username+": Game changed");
        try {
            reply("GLGC");
        } catch(IOException e) { remove(); }
    }
    
    public void playerMoved(Player p) {
        System.out.println(username+": "+p.getName()+" moved to "+p.getPosition().x+","+p.getPosition().y);
        try {
            reply("GLMD "+stringEncode(p.getName())+","+p.getPosition().x+","+p.getPosition().y+","+p.getAngle()+","+p.getSpeed()+","+p.getMaximumSpeed());
        } catch(IOException e) { remove(); }
    }
    
    /**
     * The next player to play is player.
     * @param g the game
     * @param player the next player
     */
    public void nextPlayer(Game g, Player player) {
        System.out.println(username+": Waiting for "+player.getName());
        StringBuffer buffer = new StringBuffer();
        buffer.append("GUPD "+stringEncode(player.getName()));
        Vector playerPossibleNode = g.getValidMoves(player);
        Point p = null;
        Trajectory t = null;
        int n = playerPossibleNode.size();
        for (int i=0; i<n; i++) {
            buffer.append(',');
            p = (Point)playerPossibleNode.get(i);
            Color color = Color.orange;
            if (g.getProperties().getRedDots()) {
                t = g.getTrajectory(player, p, (g.getProperties().getIsRallyMode())?Game.RALLY_MODE:Game.NORMAL_MODE);
                if (g.crossed(player.getPosition(), t.getTrajectory())) {
                    color = Color.red;
                } else if (g.willCrash(player, p)) {
                    color = Color.magenta;
                }
            }
            buffer.append(p.x+","+p.y+","+color.getRed()+","+color.getGreen()+","+color.getBlue());
        }
        
        // GUPD : game update Sending player's name, his valid points and associated color
        try {
            reply(buffer.toString());
        } catch(IOException e) { }
    }
}
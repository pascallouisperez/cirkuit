package cirkuit.remote;

import cirkuit.properties.Properties;
import cirkuit.player.Player;
import cirkuit.game.GameListener;
import cirkuit.game.Game;
import cirkuit.circuit.Circuit;
import java.util.Vector;
import java.util.Random;
import java.util.Hashtable;
import java.io.IOException;
import java.awt.Color;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class GameInfo implements GameListener {
    /**
     * The propoerties to use when a game is started.
     */
    public Properties props = new Properties();
    
    private Vector players = new Vector();
    private Vector listeners = new Vector();
    private OnlineGame game = null;
    private boolean gamestarted = false;
    
    private Random random = new Random();
    private Circuit circuit = new Circuit();
    private String circuitFileName = null;
    
    /**
     * Creates a default game.
     */
    public GameInfo() {
        props.setPlayerClass(new Hashtable());
        props.setPlayerColor(new Hashtable());
    }
    
    public boolean startGame() {
        if (gamestarted == false) {
            gamestarted = true;
            // start the game
            // tell all the distant players to start playing (from ClientSocketAnalyser)
            game = new OnlineGame(props, players, listeners);
            game.addGameListener(this);
            game.setStartingPlayer(Math.abs(random.nextInt()%(players.size())));
            game.start();
            return true;
        } else {
            return false;
        }
    }
    
    /** The game just started */
    public void gameStarted() { }
    
    /** The game just finished */
    public void gameFinished() {
        gamestarted = false;
    }
    
    /** A player arrived with the specified time */
    public void playerArrived(Player p, double time) { }
    
    /** A player just went out */
    public void playerOut(Player p) { }
    
    /** Some players crashed */
    public void crash(Vector v) { }
    
    /** The game changed, this has to be called after nextPlayer */
    public void gameChanged(Game g) { }
    
    public void playerMoved(Player p) { }
    
    /** The next player to play is p */
    public void nextPlayer(Game g, Player player) { }
    
    public boolean addPlayer(Player p) {
        return players.add(p);
    }
    
    public void removePlayer(String un) {
        int n = players.size();
        Player p;
        for (int i=0; i<n; i++) {
            p = (Player)players.get(i);
            if (p.getName().equals(un)) {
                players.remove(i);
                if (game != null)
                    game.makeHimQuit(p);
                break;
            }
        }
    }
    
    public Vector getPlayers() {
        return this.players;
    }
    
    public int getPlayersNumber() {
        return this.players.size();
    }
    
    public boolean addGameListener(GameListener gl) {
        return listeners.add(gl);
    }
    
    public boolean removeGameListener(GameListener gl) {
        return listeners.remove(gl);
    }
    
    public Vector getGameListeners() {
        return this.listeners;
    }
    
    public OnlineGame getGame() {
        return this.game;
    }
    
    /**
     * Sends the game description over the net
     */
    public void sendNetworkDescription(SocketAnalyser sa) throws IOException {
        sa.reply("GINF");
        
        // sends if running
        sa.reply("GRUN "+((gamestarted)?"1":"0"));
    
        // sending players
        int n = players.size();
        Player p;
        Color c;
        for (int i=0; i<n; i++) {
            p = (Player)players.get(i);
            c = p.getColor();
            sa.reply("PLYR "+sa.stringEncode(p.getName())+","+c.getRed()+","+c.getGreen()+","+c.getBlue());
        }
        
        // sending props
        sa.reply("PRCF "+sa.stringEncode(props.getCircuitFileName().substring(cirkuit.remote.server.Server.getCircuitFolder().length())));
        sa.reply("PRGR "+props.getGrid());
        sa.reply("PRMS "+props.getMinusSpeed());
        sa.reply("PRPS "+props.getPlusSpeed());
        sa.reply("PRCR "+props.getCrashRadius());
        sa.reply("PRMA "+(int)Math.toDegrees(props.getMaxAngle()));
        sa.reply("PRRD "+((props.getRedDots())?"1":"0"));
        sa.reply("PRIC "+((props.getIsCrash())?"1":"0"));
        sa.reply("PRMD "+props.getMode());
        sa.reply("PRTN "+props.getTurn());
        sa.reply("PRTC "+props.getTrace());
        
        // sending circuit
        // I uncommented it because on the client side it wasn't implemented
        if (circuitFileName == null || !props.getCircuitFileName().equals(circuitFileName)) {
            circuit.load(props.getCircuitFileName());
        }
        sa.reply("CINF");
        sa.reply(circuit.toString());
        sa.reply("CINE");
        
        sa.reply("GINE");
    }
}
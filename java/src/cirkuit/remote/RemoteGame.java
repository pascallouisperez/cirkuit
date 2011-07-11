package cirkuit.remote;

import cirkuit.game.Game;
import cirkuit.game.GameListener;
import cirkuit.properties.Properties;
import cirkuit.player.Player;
import java.util.Vector;
import java.awt.Point;
import java.awt.Color;
import cirkuit.circuit.Circuit;
import cirkuit.player.HumanPlayer;
import cirkuit.util.Trajectory;

/**
 * This represents the remote game (client side).
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class RemoteGame extends Game implements SocketListener {
    
    private Vector validMoves = null;
    private Vector validMovesColor = null;
    private SocketAnalyser sa;
    private Player humanPlayer = null;
    
    /**
     * Create a remote game.
     * @param p a properties object
     * @param players the online players, the must already be instanciated
     */
    public RemoteGame(SocketAnalyser sa, Properties p, Vector players, Vector listeners, Circuit circuit) {
        super(p);
        
        int n = players.size();
        Player player;
        for (int i=0; i<n; i++) {
            player = (Player)players.get(i);
            player.setPosition((Point)null);
            player.setAngle(getCircuit().getStartingAngle());
            player.setSpeed(0);
            player.setMaximumSpeed(getCircuit().getMaximumSpeed());
        }
        
        setGameListeners(listeners);
        setPlayers(players);
        setAllPlayers((Vector)(players.clone()));
        
        circuit.setInnerColor(p.getInColor());
        circuit.setOuterColor(p.getOutColor());
        circuit.setStartingLineColor(p.getStartColor());
        setCircuit(circuit);
        
        validMoves = new Vector();
        validMovesColor = new Vector();
        humanPlayer = seekHumanPlayer();
        
        this.sa = sa;
    }
    
    private Player seekHumanPlayer() {
        Vector v = getPlayers();
        int n = v.size();
        Player p;
        for (int i=0; i<n; i++) {
            p = (Player)v.get(i);
            if (p instanceof HumanPlayer) {
                return p;
            }
        }
        return null;
    }
    
    public void setHumanPlayer(HumanPlayer p) {
        humanPlayer = p;
    }

    /**
     * Returns a vector of moves allowed for the player.
     * @param player the player
     * @return the allowed moves
     */
    public Vector getValidMoves(Player player) {
        return validMoves;
    }
    
    public Vector getMovesColor(Vector playerPossibleNode, Player currentPlayer) {
        return validMovesColor;
    }
    
    private Player getPlayer(String name) {
        if (name != null) {
            Vector v = getPlayers();
            int n = v.size();
            Player p;
            for (int i=0; i<n; i++) {
                p = (Player)v.get(i);
                if (p.getName().equals(name)) {
                    return p;
                }
            }
        }
        return null;
    }
    
    /** Socket Analyser */
    public void dataArrived(String command) {
        try {
            if (command.equals("GLGC")) {
                for (int i=0; i<gameListeners.size(); i++) {
                    ((GameListener)gameListeners.get(i)).gameChanged(this);
                }
            } else if (command.equals("ASKM")) {
                Point tmpPoint;
                if (humanPlayer!=null) {
                    tmpPoint = humanPlayer.play(this);
                    sa.reply("MOVE "+tmpPoint.x+","+tmpPoint.y);
                }
            } else if (command.equals("GUPD")) {
                Player p = getPlayer(sa.getNextString());
                if (p!=null) {
                    int x,y;
                    Color c;
                    validMoves.clear();
                    validMovesColor.clear();
                    while((x = sa.getNextInt(-1000)) != -1000 && (y = sa.getNextInt(-1000)) != -1000 && (c = sa.getNextColor()) != null) {
                        validMoves.add(new Point(x,y));
                        validMovesColor.add(c);
                    }
                    
                    for (int i=0; i<gameListeners.size(); i++) {
                        ((GameListener)gameListeners.get(i)).nextPlayer(this, p);
                    }
                }
            } else if (command.equals("GLGS")) {
                for (int i=0; i<gameListeners.size(); i++) {
                    ((GameListener)gameListeners.get(i)).gameStarted();
                }
            } else if (command.equals("GLGF")) {
                for (int i=0; i<gameListeners.size(); i++) {
                    ((GameListener)gameListeners.get(i)).gameFinished();
                }
            } else if (command.equals("GLPO")) {
                Player p = getPlayer(sa.getNextString());
                if (p!=null) {
                    getPlayers().remove(p);
                    for (int i=0; i<gameListeners.size(); i++) {
                        ((GameListener)gameListeners.get(i)).playerOut(p);
                    }
                }
            } else if (command.equals("GLMD")) {
                Player p = getPlayer(sa.getNextString());
                int x, y, ms;
                double a, s;
                if (p!=null && (x = sa.getNextInt(-1000))!= -1000 && (y = sa.getNextInt(-1000))!= -1000 && (a = sa.getNextDouble(-1000))!= -1000 && (s = sa.getNextDouble(-1000))!= -1000 && (ms = sa.getNextInt(-1000))!= -1000) {
                    //p.setPosition(new Point(x,y));
                    Trajectory t = getTrajectory(p, new Point(x, y), (getProperties().getIsRallyMode())?RALLY_MODE:NORMAL_MODE);
                    p.setPosition(t.getTrajectory());
                    p.setAngle(a);
                    p.setSpeed(s);
                    p.setMaximumSpeed(ms);
                    for (int i=0; i<gameListeners.size(); i++) {
                        ((GameListener)gameListeners.get(i)).playerMoved(p);
                    }
                }
            } else if (command.equals("GLPA")) {
                Player p = getPlayer(sa.getNextString());
                double t = sa.getNextDouble(-1);
                if (p!=null && t != -1) {
                    for (int i=0; i<gameListeners.size(); i++) {
                        ((GameListener)gameListeners.get(i)).playerArrived(p, t);
                    }
                }
            } else if (command.equals("GLCH")) {
                Vector v = new Vector();
                String n;
                while ((n = sa.getNextString()) != null) {
                    Player p = getPlayer(n);
                    v.add(p);
                }
                
                for (int i=0; i<gameListeners.size(); i++) {
                    ((GameListener)gameListeners.get(i)).crash(v);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
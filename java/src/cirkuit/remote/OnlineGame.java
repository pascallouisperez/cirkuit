package cirkuit.remote;

import cirkuit.game.Game;
import cirkuit.properties.Properties;
import cirkuit.player.Player;
import java.util.Vector;
import java.awt.Point;

/**
 * This represents the online game.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class OnlineGame  extends Game {
    /**
     * Create an online game.
     * @param p a properties object
     * @param players the online players, the must already be instanciated
     */
    public OnlineGame(Properties p, Vector players, Vector listeners) {
        super(p, true);
        
        int n = players.size();
        Player player;
        for (int i=0; i<n; i++) {
            player = (Player)players.get(i);
            player.setPosition((Point)null);
            player.setAngle(getCircuit().getStartingAngle());
            player.setSpeed(0);
            player.setMaximumSpeed(getCircuit().getMaximumSpeed());
            player.setTurn(0);
            // make sure that the trace is clear
            player.getTrace().clear();
        }
        
        setGameListeners(listeners);
        setPlayers((Vector)(players.clone()));
        setAllPlayers((Vector)(players.clone()));
    }
}
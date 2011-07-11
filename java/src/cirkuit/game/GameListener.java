package cirkuit.game;

import java.util.Vector;

import cirkuit.game.Game;
import cirkuit.player.Player;

/**
 * This class defines a Game Listener.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public interface GameListener {
    /** The game just started */
    public abstract void gameStarted();
    
    /** The game just finished */
    public abstract void gameFinished();
    
    /** A player arrived with the specified time */
    public abstract void playerArrived(Player p, double time);
    
    /** A player just went out */
    public abstract void playerOut(Player p);
    
    /** Some players crashed */
    public abstract void crash(Vector v);
    
    /** The game changed, this has to be called after nextPlayer */
    public abstract void gameChanged(Game g);
    
    /** The next player to play is p */
    public abstract void nextPlayer(Game g, Player p);
    
    /** What the player did */
    public abstract void playerMoved(Player p);
}

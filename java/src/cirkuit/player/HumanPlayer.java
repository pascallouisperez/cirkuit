package cirkuit.player;

import java.awt.Point;
import java.awt.Color;

import cirkuit.game.Game;

/**
 * This class defines a human player.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class HumanPlayer extends Player {
    private Point nextMove = null;
    private boolean ready = false;
    
    public HumanPlayer() {
        super();
    }
    
    public HumanPlayer(String name) {
        super(name);
    }
    
    public HumanPlayer(Color color) {
        super(color);
    }
    
    public HumanPlayer(String name, Color color) {
        super(name, color);
    }
    
    /**
     * The human player is special, the usage is:<br><ul>
     * <li>in Game.run() :<pre>
     * humanPlayer.play(this);
     * humanPlayer.setMove(null);
     * </pre>
     * <li>and in the MouseListener:<pre>
     * if (nextPlayer instanceof HumanPlayer && ((HumanPlayer)nextPlayer).isReady())
     *     nextPlayer.setMove(new Point(e.getX(), e.getY()));
     * </pre>
     */
    public Point play(Game g) {
        ready = true;
        nextMove=null;
        while(nextMove==null) {
            try {
                (Thread.currentThread()).sleep(25);
            } catch(Exception e) { }
        }
        ready = false;
        return nextMove;
    }
    
    public void setReady(boolean r) {
        this.ready = r;
    }
    
    public boolean isReady() {
        return this.ready;
    }
    
    public void setMove(Point p) {
        this.nextMove = p;
    }
    
    public Point getNextMove() {
        return this.nextMove;
    }
}

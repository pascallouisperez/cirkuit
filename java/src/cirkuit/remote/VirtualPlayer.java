package cirkuit.remote;

import java.awt.Point;
import java.awt.Color;

import cirkuit.game.Game;
import cirkuit.player.Player;

/**
 * This class defines a virtual player
 * @author Sven Gowal
 */
public class VirtualPlayer extends Player {
    
    public VirtualPlayer() {
        super();
    }
    
    public VirtualPlayer(String name) {
        super(name);
    }
    
    public VirtualPlayer(Color color) {
        super(color);
    }
    
    public VirtualPlayer(String name, Color color) {
        super(name, color);
    }
    
    public Point play(Game g) {
        return null;
    }
}

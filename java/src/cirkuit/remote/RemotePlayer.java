package cirkuit.remote;

import cirkuit.player.Player;
import cirkuit.game.Game;

import java.awt.Point;
import java.awt.Color;
import java.io.IOException;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class RemotePlayer extends Player implements SocketListener {
    private SocketAnalyser socketAnalyser = null;
    
    private Point nextMove = null;
    private boolean ready = false;
    private boolean kicked = false;
    
    public RemotePlayer(SocketAnalyser sa, String name, Color c) {
        super(name, c);
        this.socketAnalyser = sa;
        sa.addSocketListener(this);
    }

    public Point play(Game g) {
        ready = true;
        nextMove=null;
        // Sending ASKM : ask move, details on the game have been already sent via ClientSocketAnalyser.
        try {
            socketAnalyser.reply("ASKM");
        } catch(IOException e) {
            kicked = true;    
        }
        while(nextMove==null && kicked==false) {
            try {
                (Thread.currentThread()).sleep(100);
            } catch(Exception e) { }
        }
        ready = false;
        if (kicked)
            g.makeHimQuit(this);
        
        return nextMove;
    }
    
    public void dataArrived(String command) {
        if (command.equals("MOVE")) {
            int x = socketAnalyser.getNextInt(-1);
            socketAnalyser.deleteNextSeparator();
            int y = socketAnalyser.getNextInt(-1);
            try {
                if (x > 0 && y > 0) {
                    socketAnalyser.reply("COMC \"move done to "+x+","+y+"\"");
                    nextMove = new Point(x,y);
                } else {
                    socketAnalyser.setSocketState(SocketAnalyser.STATE_NORMAL);
                    socketAnalyser.reply("KICK \"wrong move\"");
                    kicked = true;
                }
            } catch (IOException e) {
                kicked = true;
            }
        }
    }
}

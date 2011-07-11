package cirkuit.game;

import java.util.Vector;
import java.awt.*;

import cirkuit.player.*;
import cirkuit.util.Cubic;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class Replay extends Thread {
    
    private final int diameter = 2;
    
    private Vector gameListeners = new Vector();
    private Player[] players;
    private Game game;
    private int cnt = 0;
    private int dtime = 50;
    
    public Replay(Game g) {
        this.game = g;
        players = new Player[game.getAllPlayers().size()];
        for (int i=0; i<players.length; i++) {
            try {
                players[i] = (Player)(((Player)game.getAllPlayers().get(i)).clone());
                players[i].setTrace(decompose(players[i].getTrace(), 6));
            } catch(Exception e) {
                players[i] = null;
            }
        }
    }
    
    public void setSpeed(double speed) {
        this.dtime = (int)(50/speed);
    }
    
    public boolean addGameListener(GameListener gl) {
        return gameListeners.add(gl);
    }
    
    public boolean removeGameListener(GameListener gl) {
        return gameListeners.remove(gl);
    }

    /** Draws a represantation of the player */
	private void drawPlayer(Graphics g, Player p, int length)  {
        if (p != null && p.getPosition() != null) {
            g.setColor(Color.black);
            g.fillOval(p.getPosition().x-(diameter/2)-1, p.getPosition().y-(diameter/2)-1, diameter+2, diameter+2);
            g.setColor(p.getColor());
            g.fillOval(p.getPosition().x-(diameter/2), p.getPosition().y-(diameter/2), diameter, diameter);
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2d.drawString(p.getName(), p.getPosition().x-(diameter/2)-1, p.getPosition().y+11);
            
            // drawing the trace
            Point s,e;
            int n = Math.min(p.getTrace().size(), cnt);
            int start = 1;
            if (length > 0)
                start = Math.max(1, n-length);
            for (int i = start; i < n; i++) {
                s = (Point)p.getTrace().get(i-1);
                e = (Point)p.getTrace().get(i);
                g.drawLine(s.x,s.y,e.x,e.y);
            }
        }
    }
    
    public void draw(Graphics g) {
        if (game != null && game.getCircuit() != null) {
            game.getCircuit().draw(g); 
            for (int i=0; i<players.length; i++)
                if (players[i] != null)
                    drawPlayer(g, players[i], game.getProperties().getTrace()*7);
        }
    }
    
    private Vector decompose(Vector v, int n) {
        Point[] p = new Point[2];
        int dx, dy;
        double dt = 1.0/n;
        Vector vtmp = new Vector();
        for (int i=1; i<v.size(); i++) {
            p[0] = (Point)v.get(i-1);
            p[1] = (Point)v.get(i);
            dx = p[1].x-p[0].x;
            dy = p[1].y-p[0].y;
            for (double t=0; t<1; t+=dt) {
                vtmp.add(new Point((int)(p[0].x+t*dx), (int)(p[0].y+t*dy)));
            }
        }
        vtmp.add(v.get(v.size()-1));
        return vtmp;
    }

    public void run() {
        cnt = 0;
        int maxCnt = 0;
        
        for (int i=0; i<players.length; i++) {
            maxCnt = Math.max(maxCnt, players[i].getTrace().size());
        }
        
        long time1 = System.currentTimeMillis();
        while (cnt < maxCnt) {
            for (int i=0; i<players.length; i++) {
                if (cnt < players[i].getTrace().size()) {
                    players[i].setFakePosition((Point)players[i].getTrace().get(cnt));
                }
            }
            cnt++;
            
            for (int i=0; i<gameListeners.size(); i++)
                ((GameListener)gameListeners.get(i)).gameChanged(null);
                
            while (System.currentTimeMillis() < time1+dtime) {
                try {
                    (Thread.currentThread()).sleep(10);
                } catch(Exception e) { }
            }
            time1 = System.currentTimeMillis();
        }
    }
}

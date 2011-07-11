package cirkuit.util;

import java.util.Random;
import java.util.Vector;
import java.util.Hashtable;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import cirkuit.game.Game;
import cirkuit.game.GameListener;
import cirkuit.player.Player;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class GameInfoBox implements GameListener {
    private Game game;
    
    // info box properties
    private boolean anchorMode = false;
    private int x;
    private int y;
    private int width  = 200;
    private int height = 200;
    private int bulletWidth = 5;
    private boolean gameFinished = false;
    
    // texts alignements
    private int xLeftAlign  = 5;
    private int xRightAlign = width-xLeftAlign;
    private int vSpacing    = 2;
    
    // fonts
    private Font fTitle     = new Font("SansSerif", Font.BOLD, 11);
    private Font fText      = new Font("SansSerif", Font.PLAIN, 11);
    private Font fTextSmall = new Font("SansSerif", Font.PLAIN, 10);
    
    // text and player informations
    private int nextplayer = -1;
    private Vector allPlayers;
    private Hashtable playerArrived = new Hashtable();
    private Vector messageBuffer = new Vector();
    
    // randomizer
    Random random = new Random();
    
    /**
     * @param game the game which this info box refers to
     * @param x x coordinate of the upper left corner of the info box
     * @param y y coordinate of the upper left corner of the info box
     */
    public GameInfoBox(Game game, int x, int y) {
        allPlayers = game.getAllPlayers();
        this.game = game;
        this.x = x;
        this.y = y;
    }
    
    public void draw(Graphics g) {
        // setting the anti-aliasing
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // doing the preliminary calculations to determinate
        // the height of the info box
        String circuitName        = game.getCircuit().getName();
        String circuitLength      = "average length";
        String circuitLengthValue = roundMilli((double)game.getCircuit().getLength()/(double)game.getProperties().getGrid())+"";
        String circuitSpeed       = "maximum speed";
        String circuitSpeedValue  = game.getCircuit().getMaximumSpeed()+"";

        FontRenderContext context = g2d.getFontRenderContext();
        Rectangle2D circuitNameBounds        = fTitle.getStringBounds(circuitName, context);
        Rectangle2D circuitLengthBounds      = fText.getStringBounds(circuitLength, context);
        Rectangle2D circuitLengthValueBounds = fText.getStringBounds(circuitLengthValue, context);
        Rectangle2D circuitSpeedBounds       = fText.getStringBounds(circuitSpeed, context);
        Rectangle2D circuitSpeedValueBounds  = fText.getStringBounds(circuitSpeedValue, context);
        
        double heightD = circuitNameBounds.getHeight()+vSpacing+circuitLengthBounds.getHeight()+vSpacing+circuitSpeedBounds.getHeight()+vSpacing;
        
        if (allPlayers != null) {
            int n = allPlayers.size();
            Rectangle2D playerNameBounds;
            Player pTmp;
            String pName;
            for (int i = 0; i < n; i++) {
                pTmp = (Player)allPlayers.get(i);
                pName = pTmp.getName();
                playerNameBounds = fText.getStringBounds(pName, context);
                heightD += playerNameBounds.getHeight()+vSpacing;
            }
        }
        int messageBufferSize = messageBuffer.size();
        int messageBufferI;
        int widthTmp = 0;
        String messageTmp;
        Rectangle2D messageTmpBounds;
        if (messageBufferSize > 2) {
            messageBufferI = messageBufferSize-3;
        } else {
            messageBufferI = 0;
        }
        while (messageBufferI < messageBufferSize) {
            messageTmp = (String)messageBuffer.get(messageBufferI);
            messageTmpBounds = fText.getStringBounds(messageTmp, context);
            widthTmp = (int)messageTmpBounds.getWidth()+xLeftAlign+xLeftAlign;
            if (width < widthTmp) {
                width = widthTmp;
            }
            heightD += messageTmpBounds.getHeight()+vSpacing;
            messageBufferI++;
        }
        
        // now we have the height and the width
        height = (int)heightD;
        xRightAlign = width-xLeftAlign;
        
        // drawing the borders and the background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x, y, width, height);
        if (anchorMode) {
            g2d.setColor(Color.RED);
            g2d.fillRect(x-5, y-5, 5, 5);
            g2d.drawRect(x, y, width, height);
            g2d.setColor(Color.BLACK);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, width, height);
        }
        
        // the texts preparation
        int xLeftAlignReal  = x+xLeftAlign;
        int xRightAlignReal = x+xRightAlign;
        int yLine           = 0;
            
        // outputing texts
        yLine = y+(int)circuitNameBounds.getHeight()+vSpacing;
        g2d.setFont(fTitle);
        g2d.drawString(circuitName, xLeftAlignReal, yLine);
        g2d.setFont(fText);
        yLine += circuitLengthBounds.getHeight()+vSpacing;
        g2d.drawString(circuitLength, xLeftAlignReal, yLine);
        g2d.drawString(circuitLengthValue, xRightAlignReal-(int)circuitLengthValueBounds.getWidth(), yLine);
        yLine += circuitSpeedBounds.getHeight()+vSpacing;
        g2d.drawString(circuitSpeed, xLeftAlignReal, yLine);
        g2d.drawString(circuitSpeedValue, xRightAlignReal-(int)circuitSpeedValueBounds.getWidth(), yLine);
        
        // players informations
        if (allPlayers != null) {
            int n = allPlayers.size();
            Rectangle2D playerNameBounds;
            Rectangle2D playerInfoBounds;
            Player tmp;
            String pName;
            String pInfo;
            int cnt = 0;
            int ptr = game.getStartingPlayer();
            while (cnt < n) {
                tmp = (Player)allPlayers.get(ptr);
                pName = tmp.getName();
                if (playerArrived.containsKey(pName)) {
                    pInfo = ((Double)(playerArrived.get(pName))).toString();
                } else {
                    pInfo = roundMilli(tmp.getSpeed())+" / "+tmp.getMaximumSpeed();
                }
                playerNameBounds = fText.getStringBounds(pName, context);
                playerInfoBounds = fText.getStringBounds(pInfo, context);
                yLine += playerNameBounds.getHeight()+vSpacing;
                g2d.setColor(tmp.getColor());
                if (nextplayer == ptr) {
                    int r = 30;
                    int d = 8;
                    double delta = 5*Math.PI/6;
                    int[] bx = new int[3];
                    int[] by = new int[3];
                    bx[0] = xLeftAlignReal;
                    by[0] = yLine+1;
                    bx[1] = xLeftAlignReal;
                    by[1] = yLine-bulletWidth-2;
                    bx[2] = xLeftAlignReal+bulletWidth;
                    by[2] = (int)(yLine-(double)(bulletWidth+2)/(double)2);
                    g.fillPolygon(bx, by, bx.length);
                } else {
                    g2d.fillOval(xLeftAlignReal, yLine-bulletWidth-2, bulletWidth, bulletWidth);
                }
                g2d.setColor(Color.BLACK);
                g2d.drawString(pName, xLeftAlignReal+bulletWidth+5, yLine);
                g2d.drawString(pInfo, xRightAlignReal-(int)playerInfoBounds.getWidth(), yLine);
                
                ptr = (ptr+1) % n;
                cnt++;
            }
        }
        
        // message buffer output - last 3 messages
        if (messageBufferSize > 2) {
            messageBufferI = messageBufferSize-3;
        } else {
            messageBufferI = 0;
        }
        while (messageBufferI < messageBufferSize) {
            messageTmp = (String)messageBuffer.get(messageBufferI);
            messageTmpBounds = fText.getStringBounds(messageTmp, context);
            yLine += messageTmpBounds.getHeight()+vSpacing;
            g2d.drawString(messageTmp, xLeftAlignReal, yLine);
            messageBufferI++;
        }
    }
    
    /** The game just started */
    public void gameStarted() {
        nextplayer = game.getStartingPlayer();
        int r = random.nextInt(2);
        switch (r) {
            case 0:
                messageBuffer.add("Gentlemens... start your engines");
                break;
                
            case 1:
                messageBuffer.add("Let's get ready to rumble");
                break;
        }
    }
    
    /** The game just finished */
    public void gameFinished() {
        messageBuffer.add("Game's over... but don't worry, you can start again");
    }
    
    /** A player arrived with the specified time */
    public void playerArrived(Player p, double time) {
        int r = random.nextInt(2);
        time = roundMilli(time);
        switch (r) {
            case 0:
                messageBuffer.add("Home run for "+p.getName()+", only "+time+" turns to complete the track");
                break;
                
            case 1:
                messageBuffer.add("Nice job "+p.getName()+", only "+time+" turns to complete the track");
                break;
        }
        playerArrived.put(p.getName(), new Double(time));
    }
    
    /** A player just went out */
    public void playerOut(Player p) {
        int r = random.nextInt(4);
        switch (r) {
            case 0:
                messageBuffer.add("What is "+p.getName()+" doing ?!?");
                break;
                
            case 1:
                messageBuffer.add("Don't fall asleep while driving "+p.getName());
                break;
                
            case 2:
                messageBuffer.add("Don't drink and drive "+p.getName()+", smoke and fly !");
                break;
                
            case 3:
                messageBuffer.add("Learn how to drive "+p.getName()+" before coming back");
                break;
        }
    }
    
    /** Some players crashed */
    public void crash(Vector v) {
        int n = v.size()-1;
        String names = "";
        for (int i = 0; i <= n; i++) {
            if (i > 0) {
                if (i == n) {
                    names += " and ";
                } else {
                    names += ", ";
                }
            }
            names += ((Player)v.get(i)).getName();
        }
        messageBuffer.add(names+" just crashed");
    }
    
    /** The game changed, this has to be called after nextPlayer */
    public void gameChanged(Game g) {
        //
    }
    
    /** The next player to play is p */
    public void nextPlayer(Game g, Player p) {
        nextplayer = allPlayers.indexOf(p);
        /*
        int r = random.nextInt(2);
        switch (r) {
            case 0:
                messageBuffer.add("It's "+p.getName()+"'s turn");
                break;
                
            case 1:
                messageBuffer.add("Everybody is waiting on you "+p.getName());
                break;
        }*/
    }
    
    public void playerMoved(Player p) { }
    
    private double roundMilli(double a) {
        return (double)((int)(a*1000))/1000.0;
    }
    
    public boolean getAnchorMode() {
        return anchorMode;
    }
    
    public void setAnchorMode(boolean anchorMode) {
        this.anchorMode = anchorMode;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}

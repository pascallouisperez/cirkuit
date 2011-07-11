package cirkuit.game;

import java.util.Enumeration;
import java.util.Vector;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;

import cirkuit.player.*;
import cirkuit.properties.*;
import cirkuit.game.*;
import cirkuit.circuit.Circuit;
import cirkuit.util.Configuration;
import cirkuit.util.Curves;
import cirkuit.util.Trajectory;

/**
 * This represents the game.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class Game  extends Thread implements Cloneable {
    public static final int RALLY_MODE = 0;
    public static final int NORMAL_MODE = 1;
    
    private Vector players = new Vector();
    private Vector allplayers;
    private Vector quitPlayers = new Vector();
    protected Vector gameListeners = new Vector();
    private Circuit circuit;
    private int startPlayer;
    private int counter;
    private int cnt;
    
    private Properties props;
    
    public Game(Properties p) {
        this.props = p;
        
        circuit = new Circuit();
        circuit.load(p.getCircuitFileName());
        circuit.setInnerColor(p.getInColor());
        circuit.setOuterColor(p.getOutColor());
        circuit.setStartingLineColor(p.getStartColor());
        
        String playerName;
        Color c;
        String cls;
        for (Enumeration e = p.getPlayerClass().keys(); e.hasMoreElements();) {
            playerName = (String)e.nextElement();
            c = (Color)p.getPlayerColor().get(playerName);
            cls = (String)p.getPlayerClass().get(playerName);
            if (cls != null && c != null) {
                Player player = newPlayerInstance(cls, playerName, c);
                if (player != null) {
                    addPlayer(player);
                    player.setPosition((Point)null);
                    player.setAngle(circuit.getStartingAngle());
                    player.setSpeed(0);
                    player.setMaximumSpeed(circuit.getMaximumSpeed());
                    player.setTurn(0);
                }
            }
        }
        allplayers = (Vector)players.clone();

        this.startPlayer = 0;
        // general turn counter
        counter = -1;
        // who's turn
        cnt = startPlayer-1;
    }
    
    /**
     * For optimisation reason
     */
    public Game(Properties p, boolean test) {
        this.props = p;
        
        circuit = new Circuit();
        circuit.load(p.getCircuitFileName());
        circuit.setInnerColor(p.getInColor());
        circuit.setOuterColor(p.getOutColor());
        circuit.setStartingLineColor(p.getStartColor());

        this.startPlayer = 0;
        // general turn counter
        counter = -1;
        // who's turn
        cnt = startPlayer-1;
    }
    
    private Player newPlayerInstance(String clsName, String playerName, Color color) {
        try {
            // getting class
            Class cls = Class.forName("cirkuit.player."+clsName);
            // getting constructor
            Class[] constructorArgsTypes = new Class[2];
            constructorArgsTypes[0] = String.class;
            constructorArgsTypes[1] = Color.class;
            Constructor clsConstructor = cls.getConstructor(constructorArgsTypes);
            // preparing args
            Object[] constructorArgs = new Object[2];
            constructorArgs[0] = playerName; 
            constructorArgs[1] = color;
            // new instance
            return (Player)clsConstructor.newInstance(constructorArgs);
        } catch(Exception e) {
            return null;
        }
    }

    public int getStartingPlayer() {
        return startPlayer;
    }
    
    public void setStartingPlayer(int s) {
        this.startPlayer = s;
        // who's turn
        cnt = startPlayer-1;
    }
    
    public void draw(Graphics g) {
        if (circuit != null)
            circuit.draw(g); 
        int l = (props.getIsRallyMode())?props.getTrace()*5:props.getTrace();
        for (int i=0; i<allplayers.size(); i++) {
            //System.out.println((Player)allplayers.get(i));
            if ((Player)allplayers.get(i) != null) {
                ((Player)allplayers.get(i)).draw(g, l);
            }
        }
    }

    /**
     * Draws the grid of the game in light gray. The grid is aligned on the first node of the starting line of the circuit.
     * @param g the graphics object
     * @param width the width of the grid
     * @param height the height of the grid
     */
    public void drawGrid(Graphics g, int width, int height) {
        g.setColor(new Color(220, 220, 220));
        Point[] p = circuit.getStartingLine();
        Point[] pt = {new Point(p[0].x,p[0].y), new Point(p[1].x,p[1].y)};
        drawLines(g, p, width, height);
        AffineTransform a = new AffineTransform();
        a.rotate(Math.PI/2, pt[0].x, pt[0].y);
        a.transform(pt[1], pt[1]);
        drawLines(g, pt, width, height);
    }
    
    /** Draws the half of the grid of the game : drawing lines all parallel to the one defined by the two points in p */
    private void drawLines(Graphics g, Point[] p, int width, int height) {
        double t0;
        int tmax = (int)(Math.sqrt((double)(width*width+height*height))/((double)(props.getGrid())));
        int tmin = -tmax;
        if (p[0].x != p[1].x) {
            if (p[0].y != p[1].y) {
                double a = ((double)(p[1].y - p[0].y))/((double)(p[1].x - p[0].x));
                double b = p[0].y-a*p[0].x;
                double w = ((double)props.getGrid())/(Math.sin(Math.PI/2-Math.atan(a)));
                double t1;
                for (int i=tmin; i<tmax; i++) {
                    t0 = -i*w-b;
                    t1 = ((double)height+t0)/a;
                    t0 /= a;
                    g.drawLine((int)t0, 0, (int)t1, height);
                }
            } else {
                for (int i=tmin; i<tmax; i++) {
                    t0 = p[0].y+i*props.getGrid();
                    g.drawLine(0, (int)t0, width, (int)t0);
                }
            }
        } else {
            for (int i=tmin; i<tmax; i++) {
                t0 = p[0].x+i*props.getGrid();
                g.drawLine((int)t0, 0, (int)t0, height);
            }
        }
    }
    
    public Properties getProperties() {
        return this.props;   
    }
    
    public void setProperties(Properties p) {
        this.props = p;
    }
    
    public boolean addPlayer(Player p) {
        return players.add(p);
    }
    
    public Vector getPlayers() {
        return this.players;
    }
    
    public Vector getAllPlayers() {
        return this.allplayers;
    }
    
    public void setPlayers(Vector v) {
        this.players = v;
    }
    
    public void setAllPlayers(Vector v) {
        this.allplayers = v;
    }
    
    public boolean addGameListener(GameListener gl) {
        return gameListeners.add(gl);
    }
    
    public void setGameListeners(Vector v) {
        this.gameListeners = v;
    }
    
    public boolean removePlayer(Player p) {
        return players.remove(p);
    }
    
    public boolean removeGameListener(GameListener gl) {
        return gameListeners.remove(gl);
    }    
    
    public void setCircuit(Circuit c) {
        this.circuit = c;
    }
    
    public Circuit getCircuit() {
        return this.circuit;
    }

    /**
     * Tells whether the player can move to the point p.
     * @param player the player
     * @param pt the point
     * @return true if the player can move to this point
     */
    public boolean isMoveValid(Player player, Point pt) {
        if (pt == null)
            return false;
        if (player.getPosition() == null) {
            Point[] p = circuit.getStartingLine();
            if (p[1].x == p[0].x) {
                int ymin = Math.min(p[0].y, p[1].y);
                int ymax = Math.max(p[0].y, p[1].y)+1;
                if (pt.y<=ymax && pt.y>=ymin && isOnGrid(pt.x, pt.y) && circuit.isInside(pt.x, pt.y)) {
                    return true;
                }
            } else {
                int xmin = Math.min(p[0].x, p[1].x);
                int xmax = Math.max(p[0].x, p[1].x)+1;
                double a = (double)(p[1].y-p[0].y)/(double)(p[1].x-p[0].x);
                double b = p[0].y-a*p[0].x;
                if (pt.x<=xmax && pt.x>=xmin && pt.y==(int)(a*pt.x+b) && isOnGrid(pt.x, pt.y) && circuit.isInside(pt.x, pt.y)) {
                    return true;
                }
            }
        } else {
            int dmin = (int)(Math.max(0.0, player.getSpeed()-props.getMinusSpeed())*props.getGrid());
            int dmax = (int)(Math.min(player.getSpeed()+props.getPlusSpeed(), (double)player.getMaximumSpeed())*props.getGrid());
            int xmin = player.getPosition().x-dmax;
            int xmax = player.getPosition().x+dmax;
            int ymin = player.getPosition().y-dmax;
            int ymax = player.getPosition().y+dmax;
            int xtmp, ytmp;
            double r, t;
            if (pt.x<=xmax && pt.x>=xmin && pt.y<=ymax && pt.y>=ymin) {
                xtmp = pt.x-player.getPosition().x;
                ytmp = pt.y-player.getPosition().y;
                r = Math.sqrt(xtmp*xtmp+ytmp*ytmp);
                if (r <= dmax && r >= dmin) {
                    if (xtmp == 0 && ytmp == 0) {
                        t = 0;
                    } else {
                        t = getAngle(xtmp, ytmp);
                        t -= player.getAngle();
                    }
                    t = normalizeAngle(t);
                    if (((player.getLastPosition() != null && player.getSpeed()==0) || (t >= -props.getMaxAngle() && t <= props.getMaxAngle())) && isOnGrid(pt.x,pt.y)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Normalizes the value of an angle, in radians, that is an angle in the range ]-&pi,&pi;].
     * @param a the angle to convert
     * @return an angle in the range ]-&pi,&pi;]
     */
    private double normalizeAngle(double a) {
        double PI2 = Math.PI+Math.PI;
        if (a > Math.PI) {
            do {
                a = a - PI2;
            } while (a > Math.PI);
        } else if (a <= -Math.PI) {
            do {
                a += PI2;
            } while (a <= -Math.PI);
        }
        return a;
    }
    
    /** Gets an angle for a vector */
    public static double getAngle(int x, int y) {
        double t;
        t = 0;
        if (x == 0) {
            if (y < 0) {
                t = -Math.PI/2;
            } else {
                t = Math.PI/2;                                
            }
        } else {
            if (x > 0 && y >= 0) {
                t = Math.atan((double)(y)/(double)(x));
            } else if (x > 0 && y < 0) {
                t = Math.atan((double)Math.abs(y)/(double)x);
                t = Math.PI+Math.PI - t;
            } else if (x < 0 && y >= 0) {
                t = Math.atan((double)y/(double)Math.abs(x));
                t = Math.PI - t;
            } else if (x < 0 && y < 0) {
                t = Math.atan((double)Math.abs(y)/(double)Math.abs(x));
                t += Math.PI;
            } else { // x == 0
                t = 0;
                if (y < 0) {
                    t += Math.PI;
                }
            }
        }
        return t;
    }
    
    /**
     * Returns a vector of moves allowed for the player.
     * @param player the player
     * @return the allowed moves
     */
    public Vector getValidMoves(Player player) {
        Vector validMoves = new Vector();
        if (player.getPosition() == null) {
            Point[] p = circuit.getStartingLine();
            if (p[1].x == p[0].x) {
                int ymin = Math.min(p[0].y, p[1].y);
                int ymax = Math.max(p[0].y, p[1].y)+1;
                int x;
                for (int y = ymin; y <= ymax; y++) {
                    x = p[0].x;
                    if (isOnGrid(x, y) && circuit.isInside(x, y) && !circuit.crossedBorder(x,y,x,y)) { // added crossed border for "rafik's bug"
                        validMoves.add(new Point(x, y));
                    }
                }
            } else {
                int y;
                int xmin = Math.min(p[0].x, p[1].x);
                int xmax = Math.max(p[0].x, p[1].x)+1;
                double a = (double)(p[1].y-p[0].y)/(double)(p[1].x-p[0].x);
                double b = p[0].y-a*p[0].x;
                for (int x = xmin; x <= xmax; x++) {
                    y = (int)(a*x+b);
                    if (isOnGrid(x, y) && circuit.isInside(x, y) && !circuit.crossedBorder(x,y,x,y)) {
                        validMoves.add(new Point(x, y));
                    }
                }
            }
        } else {
            int dmin = (int)(Math.max(0.0, player.getSpeed()-props.getMinusSpeed())*props.getGrid());
            int dmax = (int)(Math.min(player.getSpeed()+props.getPlusSpeed(), (double)player.getMaximumSpeed())*props.getGrid());
            int xmin = player.getPosition().x-dmax;
            int ymin = player.getPosition().y-dmax;
            double r, t;
            
            int grid = props.getGrid();
            int xstart = -1;
            int ystart = -1;
            int xmax = xmin+grid;
            int ymax = ymin+grid;
            // finding the first point in the rectangle xmin->xmax ; ymin->ymax crossing the border
            int x = xmin;
            int y = ymin;
            boolean startfound = false;
            while (!startfound) {
                if (isOnGrid(x, y)) {
                    xstart = x;
                    ystart = y;
                    startfound = true;
                } else {
                    x++;
                    if (x > xmax) {
                        x = xmin;
                        y++;
                        if (y > ymax) {
                            // error, there was no point !
                            return null;
                        }
                    }
                }
            }
            
            // searching valid moves
            xmax = player.getPosition().x+dmax;
            ymax = player.getPosition().y+dmax;
            int xtmp, ytmp;
            for (x = xstart; x <= xmax; x += grid) {
                for (y = ystart; y <= ymax; y += grid) {
                    xtmp = x-player.getPosition().x;
                    ytmp = y-player.getPosition().y;
                    r = Math.sqrt(xtmp*xtmp+ytmp*ytmp);
                    if (r <= dmax && r >= dmin) {
                        if (xtmp == 0 && ytmp == 0) {
                            t = 0;
                        } else {
                            t = getAngle(xtmp, ytmp);
                            t -= player.getAngle();
                        }
                        t = normalizeAngle(t);
                        if ((player.getLastPosition() != null && player.getSpeed()==0) || (t >= -props.getMaxAngle() && t <= props.getMaxAngle())) {
                            validMoves.add(new Point(x,y));
                        }
                    }
                }
            }
        }
        return validMoves;
    }
    
    public Vector getMovesColor(Vector playerPossibleNode, Player currentPlayer) {
        Vector playerPossibleNodeColor = new Vector();
        Point p = null;
        Trajectory t = null;
        for (int i=0; i<playerPossibleNode.size(); i++) {
            p = (Point)playerPossibleNode.get(i);
            Color color = Color.orange;
            if (props.getRedDots()) {
                t = getTrajectory(currentPlayer, p, (props.getIsRallyMode())?Game.RALLY_MODE:Game.NORMAL_MODE);
                if (crossed(currentPlayer.getPosition(), t.getTrajectory())) {
                    color = Color.red;
                } else if (willCrash(currentPlayer, p)) {
                    color = Color.magenta;
                }
            }
            playerPossibleNodeColor.add(color);
        }
        return playerPossibleNodeColor;
    }
    
    /**
     * Tells wether the point (x,y) is on the grid.
     * @param x x coordinate
     * @param y y coordinate
     */
    public boolean isOnGrid(int x, int y) {
        Point[] p = circuit.getStartingLine();
        Point tmpP = new Point(x,y);
        AffineTransform at = new AffineTransform();
        at.setToTranslation(-p[0].x, -p[0].y);
        if (p[1].x != p[0].x) {
            at.rotate(getAngle(p[1].x-p[0].x, p[1].y-p[0].y), p[0].x, p[0].y);
        }
        at.transform(tmpP, tmpP);
        if (tmpP.x%props.getGrid()==0 && tmpP.y%props.getGrid()==0) {
            return true;
        }
        return false;
    }
    
    /**
     * Checks if a player whose index is i is between istart and iend players included
     */
    private boolean isBetween(int i, int istart, int iend) {
        if (istart < iend) {
            return (i <= iend && i >= istart);
        } else if (istart > iend) {
            return (i >= istart || i <= iend);
        }
        return  false;
    }
    
    /**
     * Makes a player leave the game.
     */
    public void makeHimQuit(Player p) {
        quitPlayers.add(p);
    }

    /**
     * Runs the game.
     *
     * TODO: There should be a timer when the game asks for a move,
     * when the timer finishes this player should be kicked out of the game.
     */
    public void run() {
        Vector v = new Vector();
        Trajectory t;
        Player  tmpPlayer, tmpP;
        Point   tmpPoint, tmpPointStart;
        int kickCnt;
        boolean continueBool;

        // starting the game
        for (int i=0; i<gameListeners.size(); i++)
            ((GameListener)gameListeners.get(i)).gameStarted();
            
        while(players.size() > 0) {
            // cnt
            cnt++;
            if (players.size() != 0)
                cnt = cnt%players.size();
            // counter
            if (cnt == startPlayer) {
                counter++;
            }
            
            tmpPlayer = (Player)players.get(cnt);
            tmpPointStart = tmpPlayer.getPosition();
            
            for (int i=0; i<gameListeners.size(); i++) {
                ((GameListener)gameListeners.get(i)).nextPlayer(this, (Player)players.get(cnt));
                ((GameListener)gameListeners.get(i)).gameChanged(this);
            }
            
            tmpPoint = null;
            kickCnt = 0;
            continueBool = false;
            do {
                kickCnt++;
                if (quitPlayers.contains(tmpPlayer) || kickCnt == 4) { // allow 3 wrong moves before kicking
                    for (int i=0; i < gameListeners.size(); i++) {
                        ((GameListener)gameListeners.get(i)).playerOut(tmpPlayer);
                    }
                    
                    // removing a player
                    players.remove(cnt);
                    
                    // resetting the starting player && counter
                    if (cnt < startPlayer)
                        startPlayer--;
                    else if (cnt == startPlayer) {
                        counter--;
                        if (cnt == players.size())
                            startPlayer = 0;
                    }
                    // calculating the new cnt
                    if (cnt == players.size())
                        cnt = -1;
                    else
                        cnt--;
                    
                    quitPlayers.remove(tmpPlayer);
                    continueBool = true;
                }
                if (continueBool)
                    break;
                tmpPoint = tmpPlayer.play(this);
            } while (!isMoveValid(tmpPlayer, tmpPoint));
            if (continueBool)
                continue;
            
            t = getTrajectory(tmpPlayer, tmpPoint, (props.getIsRallyMode())?RALLY_MODE:NORMAL_MODE);
            tmpPlayer.setPosition(t.getTrajectory());
            if (tmpPointStart != null) {
                tmpPlayer.setAngle(t.getAngle());
                tmpPlayer.setSpeed(t.getSpeed());
            }
            for (int i=0; i < gameListeners.size(); i++) {
                ((GameListener)gameListeners.get(i)).playerMoved(tmpPlayer);
            }
            
            // out of the circuit ?
            if ((tmpPointStart == null && !circuit.isInside(tmpPoint)) || (tmpPointStart != null && crossed(tmpPointStart, t.getTrajectory()))) {
                for (int i=0; i < gameListeners.size(); i++) {
                    ((GameListener)gameListeners.get(i)).playerOut(tmpPlayer);
                }
                
                // removing a player
                players.remove(cnt);
                
                // resetting the starting player && counter
                if (cnt < startPlayer)
                    startPlayer--;
                else if (cnt == startPlayer) {
                    counter--;
                    if (cnt == players.size())
                        startPlayer = 0;
                }
                // calculating the new cnt
                if (cnt == players.size())
                    cnt = -1;
                else
                    cnt--;
                    
                continue;
            }
            
            // crash ? you cannot be responsible for a crash if your speed is already 0, if maxSpeed == 0 then remove player
            if (props.getIsCrash()) {
                v.clear();
                for (int i=0; i<players.size(); i++) {
                    tmpP = (Player)players.get(i);
                    if (tmpP!=tmpPlayer && isBetween(i, startPlayer, cnt) && tmpP.getPosition() != null && (tmpP.getPosition().x-tmpPoint.x)*(tmpP.getPosition().x-tmpPoint.x)+(tmpP.getPosition().y-tmpPoint.y)*(tmpP.getPosition().y-tmpPoint.y) <= props.getCrashRadius()*props.getGrid()*props.getCrashRadius()*props.getGrid()) {
                        v.add(tmpP);
                    }
                }
                if (v.size() > 0) {
                    v.add(tmpPlayer);
                    boolean b = false;
                    for (int i=0; i<v.size(); i++) {
                        tmpP = (Player)v.get(i);
                        if (tmpP.getSpeed()!=0) {
                            tmpP.setSpeed(0);
                            tmpP.setMaximumSpeed(tmpP.getMaximumSpeed()-1);
                        }
                        if (tmpP.getMaximumSpeed() < 1) {
                            int tmp = players.indexOf(tmpP);
                            
                            // removing a player
                            players.remove(tmp);
                            
                            // resetting the starting player &
                            // calculating the new cnt
                            if (tmp < startPlayer) {
                                startPlayer--;
                            }
                            if (tmp <= cnt) {
                                cnt--;
                            }
                            if (cnt == startPlayer)
                                b=true;
                            if (tmp == players.size()) {
                                if (tmp == startPlayer) {
                                    startPlayer = 0;
                                }
                                cnt = -1;
                            }
                        }
                        if (b)
                            counter--;
                    }
                    for (int i=0; i<gameListeners.size(); i++)
                        ((GameListener)gameListeners.get(i)).crash(v);
                }
            }
            
            // arrived ?
            if (tmpPointStart != null) {
                /* faire attention ici avec le mode rally */
                tmpPlayer.setTurn(tmpPlayer.getTurn() + circuit.crossedStartingLine(tmpPointStart, tmpPoint));
                if (props.getTurn() == tmpPlayer.getTurn()) { /* arrived */
                    Point[] startingLineNodes = circuit.getStartingLine();
                    /* Since segmentIntersection doesn't return the intersection point anymore */
                    // Point inter = Curves.segmentIntersection(tmpPointStart, tmpPoint, startingLineNodes[0], startingLineNodes[1]);
                    // double cntAdjustement = Math.sqrt((double)((inter.x-tmpPoint.x)*(inter.x-tmpPoint.x)+(inter.y-tmpPoint.y)*(inter.y-tmpPoint.y))/(double)((tmpPoint.x-tmpPointStart.x)*(tmpPoint.x-tmpPointStart.x)+(tmpPoint.y-tmpPointStart.y)*(tmpPoint.y-tmpPointStart.y)));
                    double cntAdjustement;
                    Point[] pts = circuit.getStartingLine();
                    if (pts[0].x == pts[1].x) { // starting line is vertical
                        cntAdjustement = Math.abs((double)(pts[0].x-tmpPoint.x))/Math.abs((double)(tmpPoint.x-tmpPointStart.x));
                    } else { // starting line is horizontal
                        cntAdjustement = Math.abs((double)(pts[0].y-tmpPoint.y))/Math.abs((double)(tmpPoint.y-tmpPointStart.y));
                    }
                    
                    for (int i=0; i<gameListeners.size(); i++) {
                        ((GameListener)gameListeners.get(i)).playerArrived(tmpPlayer, counter - cntAdjustement);
                    }
                    
                    // removing a player
                    players.remove(cnt);
                    
                    // resetting the starting player && counter
                    if (cnt < startPlayer)
                        startPlayer--;
                    else if (cnt == startPlayer) {
                        counter--;
                        if (cnt == players.size())
                            startPlayer = 0;
                    }
                    System.out.println("4");
                    // calculating the new cnt
                    if (cnt == players.size())
                        cnt = -1;
                    else
                        cnt--;
                        
                    continue;
                }
            }
        }
        
        for (int i=0; i<gameListeners.size(); i++)
            ((GameListener)gameListeners.get(i)).gameFinished();
    }
    
    // clones a game and players
    public Object clone() throws CloneNotSupportedException {
        Game g = (Game)super.clone();
        Vector v = new Vector();
        for (int i=0; i<players.size(); i++) {
            v.add(((Player)players.get(i)).clone());
        }
        g.setPlayers(v);
        return g;
    }
    
    public boolean save(String fileName) {
        // saving the circuit
        circuit.save(fileName);
        // saving the properties
        props.save(fileName);
        // saving allPlayers
        savePlayers(fileName, "allPlayers", allplayers);
        
        Configuration conf = new Configuration(fileName);
        conf.read();
        // saving startPlayer, counter and cnt
        conf.set("startPlayer", ""+startPlayer);
        conf.set("counter", ""+counter);
        conf.set("cnt", ""+cnt);
        Player tmpP;
        String str = "";
        for (int i=0; i<players.size(); i++) {
            tmpP = (Player)players.get(i);
            str += tmpP.getName();
            if (i<players.size()-1)
                str += ",";
        }
        conf.set("players", str);
        
        return conf.write();
    }
    
    private boolean savePlayers(String fileName, String des, Vector p) {
        Configuration conf = new Configuration(fileName);
        conf.read();
        
        Player tmpP;
        Color c;
        Vector t;
        String str = "";
        for (int i=0; i<p.size(); i++) {
            tmpP = (Player)p.get(i);
            c = tmpP.getColor();
            t = tmpP.getTrace();
            str += tmpP.getName()+","+c.getRed()+","+c.getGreen()+","+c.getBlue()+","+c.getAlpha()+","+tmpP.getClass().getName()+","+tmpP.getAngle()+","+tmpP.getMaximumSpeed()+","+tmpP.getSpeed()+","+tmpP.getPosition().x+","+tmpP.getPosition().y;
            if (i<p.size()-1)
                str += ",";
            conf.set(tmpP.getName()+"Trace", getEnvelopeDescription(tmpP.getTrace()));
        }
        conf.set(des, str);
        
        return conf.write();
    }
    
    private String getEnvelopeDescription(Vector e) {
        int n = e.size();
        Point p;
        String r = "";
        for (int i = 0; i < n; i++) {
            if (i != 0) {
                r += ",";
            }
            p = (Point)e.get(i);
            r += p.x+","+p.y;
        }
        return r;
    }
    
    private Vector getEnvelope(String s) {
        Vector e = new Vector();
        
        if (s==null)
            return e;

        int x   = -1;
        int tmp = -1;
        String[] envelopeS = s.split(",");
        for (int i = 0; i < envelopeS.length; i++) {
            try {
                tmp = (new Integer(Integer.parseInt(envelopeS[i]))).intValue();
                if (tmp >= 0) {
                    if (x < 0) {
                        x = tmp;
                    } else {
                        e.add(new Point(x,tmp));
                        x = -1;
                    }
                }
            } catch (Exception er) {
                // we will just ignore this value
            }
        }
        return e;
    }
    
        /** Loads a Properties Object saved with the save method. */
    public boolean load(String fileName) {
        props = Properties.load(fileName);
        circuit.load(fileName);
        loadPlayers(fileName, "allPlayers", allplayers);
        
        
        Configuration conf = new Configuration(fileName);
        if (conf.read()) {
            cnt = (Integer.valueOf((conf.get("cnt")==null)?"0":conf.get("cnt"))).intValue()-1;
            counter = (Integer.valueOf((conf.get("counter")==null)?"0":conf.get("counter"))).intValue()-1;
            startPlayer = (Integer.valueOf((conf.get("startPlayer")==null)?"0":conf.get("startPlayer"))).intValue();
            String tmpS = conf.get("players");
            if (tmpS == null)
                return false;
            String[] s = tmpS.split(",");
            Player tmpP;
            players.clear();
            for (int i=0; i<s.length; i++) {
                for (int j=0; j<allplayers.size(); j++) {
                    tmpP = (Player)allplayers.get(j);
                    if (s[i].equals(tmpP.getName())) {
                        players.add(tmpP);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    private boolean loadPlayers(String fileName, String des, Vector p) {
        Configuration conf = new Configuration(fileName);
        if (conf.read()) {
            String tmpS = conf.get(des);
            if (tmpS == null)
                return false;
            String[] s = tmpS.split(",");
            if (s.length % 11 != 0)
                return false;
            String name, cls;
            Color c;
            p.clear();
            for (int i=0; i<s.length; i+=11) {
                name = s[i];
                c = new Color(Integer.parseInt(s[i+1]), Integer.parseInt(s[i+2]), Integer.parseInt(s[i+3]), Integer.parseInt(s[i+4]));
                cls = s[i+5].substring(s[i+5].lastIndexOf('.')+1);
                
                Player player = newPlayerInstance(cls, name, c);
                if (player != null) {
                    p.add(player);
                    player.setPosition(new Point(Integer.parseInt(s[i+9]), Integer.parseInt(s[i+10])));
                    player.setAngle(Double.parseDouble(s[i+6]));
                    player.setSpeed(Double.parseDouble(s[i+8]));
                    player.setMaximumSpeed(Integer.parseInt(s[i+7]));
                    player.setTrace(getEnvelope(conf.get(name+"Trace")));
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Tells wether the current player crashes into another if he goes
     * to the point (x, y).
     * @param player the current player
     * @param point the point
     * @return true if the current player hits another player, false otherwise
     */
    public boolean willCrash(Player player, Point point) {
        Player tmpP = null;
        for (int i=0; i<players.size(); i++) {
            tmpP = (Player)players.get(i);
            //if (tmpP != player && isBetween(i, startPlayer, cnt) && tmpP.getPosition() != null && tmpP.getPosition().equals(point)) {
            if (tmpP != player && isBetween(i, startPlayer, cnt) && tmpP.getPosition() != null && (tmpP.getPosition().x-point.x)*(tmpP.getPosition().x-point.x)+(tmpP.getPosition().y-point.y)*(tmpP.getPosition().y-point.y) <= props.getCrashRadius()*props.getGrid()*props.getCrashRadius()*props.getGrid()) {
                return true;
            }
        }
        return false;
    }
    
    public Trajectory getTrajectory(Player p, Point end, int mode) {
        Vector t     = new Vector();
        double angle = 0;
        double speed = 0;
        
        Point pPos = p.getPosition();
        if (pPos == null) {
            // stating mode
            mode = -1;
        }
        switch (mode) {
            case -1:
                t.add(end);
                angle = p.getAngle();
                speed = p.getSpeed();
                break;
                
            case NORMAL_MODE:
                t.add(end);
                angle = getAngle(end.x - pPos.x, end.y - pPos.y);
                speed = Math.sqrt((end.y-pPos.y)*(end.y-pPos.y)+(end.x-pPos.x)*(end.x-pPos.x))/(double)(props.getGrid());
                break;
                
            case RALLY_MODE:
                // angle reducer
                double angleReducer = 2;
                
                // angle calculation
                double startAngle = p.getAngle();
                angle = getAngle(end.x - pPos.x, end.y - pPos.y);
                double delta = normalizeAngle(startAngle - angle);
                angle = angle - delta/angleReducer;
                //angle = angle - delta/angleReducer;
                
                // speed calculation
                double d = Math.sqrt((end.y-pPos.y)*(end.y-pPos.y)+(end.x-pPos.x)*(end.x-pPos.x))/(double)(props.getGrid());
                speed = p.getSpeed()+(d-p.getSpeed())*Math.cos(delta);
                
                // bezier calculation
                double r = 1.0/8.0*(speed+3*p.getSpeed());
                Point ctrl0 = new Point((int)(props.getGrid()*r*Math.cos(startAngle))+pPos.x, (int)(props.getGrid()*r*Math.sin(startAngle))+pPos.y);
                Point ctrl1 = new Point((int)(end.x+props.getGrid()*(speed-r)*Math.cos(angle+Math.PI)), (int)(end.y+props.getGrid()*(speed-r)*Math.sin(angle+Math.PI)));
                
                t = Curves.bezier(p.getPosition(), end, ctrl0, ctrl1);
                t.remove(0);
                break;
        }
        return new Trajectory(t, angle, speed);
    }
    
    public boolean crossed(Point p, Vector v) {
        if (v.size() > 0 && p != null) {
            if (circuit.crossedBorder(p, (Point)v.get(0)))
                return true;
                
            for(int i=1; i<v.size(); i++) {
                if (circuit.crossedBorder((Point)v.get(i-1), (Point)v.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
package cirkuit.player;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.util.Vector;

import cirkuit.game.Game;

/**
 * This class defines a typical player.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public abstract class Player implements Cloneable {
    private final int diameter = 2;
    
    private String name;
    private Color color;
    
    private double speed;
    private double angle;
    private Point position;
    private int maxSpeed;
    private Vector trace = new Vector();
    private int turn;
    
    public Player() {
        this("Bob", Color.black);
    }
    
    public Player(String name) {
        this(name, Color.black);
    }
    
    public Player(Color color) {
        this("Bob", color);
    }
    
    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        
        setSpeed(0);
        setPosition((Point)null);
        setAngle(0);
    }
    
    /** Sets the name of the player */
    public final void setName(String n) {
        this.name = n;
    }
    
    /** Gets the name of the player */
    public String getName() {
        return this.name;
    }
    
    /** Sets the speed of the player */
    public final void setSpeed(double s) {
        this.speed = s;
    }
    
    /** Gets the speed of the player */
    public double getSpeed() {
        return this.speed;
    }
    
    /** Sets the color of the player */
    public final void setColor(Color c) {
        this.color = c;
    }
    
    /** Gets the color of the player */
	public Color getColor() {
        return this.color;
    }
    
    /** Sets the angle of the player */
	public final void setAngle(double a) {
        this.angle = a;
    }
    
    /** Gets the angle speed of the player */
	public double getAngle() {
        return this.angle;
    }
    
    /** Sets the position of the player */
	public final void setPosition(Point p) {
        this.position = p;
        if (p != null) {
            trace.add(p);
        }
    }
    
    /** Sets the position of the player */
	public final void setPosition(Vector v) {
        this.position = (Point)v.get(v.size()-1);
        trace.addAll(v);
    }
    
    /** Gets the position of the player */
	public Point getPosition()  {
        return this.position;
    }
    
    /** Sets the maximum speed of the player */
    public final void setMaximumSpeed(int s) {
        this.maxSpeed = s;
    }
    
    /** Gets the maximum speed of the player */
    public int getMaximumSpeed() {
        return this.maxSpeed;
    }
    
    /** Gets the last position (position before the current position) */
    public Point getLastPosition() {
        if (trace.size() > 1) {
            return (Point)trace.get(trace.size()-2);
        } else {
            return null;
        }
    }
    
    /** Sets the position without changing the trace */
    public void setFakePosition(Point p) {
        this.position = p;
    }
    
    /** Sets the trace */
    public final void setTrace(Vector v) {
        this.trace = v;
    }
    
    /** Gets the trace */
    public final Vector getTrace() {
        return this.trace;
    }
    
    /**
     * Set the turn number.
     * @param turn the turn number
     */
    public void setTurn(int turn) {
        this.turn = turn;
    }
    
    /**
     * Get the turn number.
     * @return the turn number
     */
    public int getTurn() {
        return turn;
    }
    
    /** Draws a represantation of the player */
	public final void draw(Graphics g, int length)  {
        if (getPosition() != null) {
            g.setColor(Color.black);
            g.fillOval(getPosition().x-(diameter/2)-1, getPosition().y-(diameter/2)-1, diameter+2, diameter+2);
            g.setColor(getColor());
            g.fillOval(getPosition().x-(diameter/2), getPosition().y-(diameter/2), diameter, diameter);
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2d.drawString(getName(), getPosition().x-(diameter/2)-1, getPosition().y+11);
            
            // drawing the trace
            Point s,e;
            int n = trace.size();
            int start = 1;
            if (length > 0)
                start = Math.max(1, n-length);
            for (int i = start; i < n; i++) {
                s = (Point)trace.get(i-1);
                e = (Point)trace.get(i);
                g.drawLine(s.x,s.y,e.x,e.y);
            }
        }
    }

	public abstract Point play(Game g);
    
    public String toString() {
        return "cirkuit.player.Player [name: "+getName()+", position: "+getPosition()+", speed: "+getSpeed()+", max speed: "+getMaximumSpeed()+", angle: "+getAngle()+"]";
    }
    
    public Object clone() throws CloneNotSupportedException {
        Player p = (Player)super.clone();
        p.setTrace((Vector)trace.clone());
        return p;
    }
}
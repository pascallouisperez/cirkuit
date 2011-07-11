package cirkuit.circuit;

import java.io.*;
import java.lang.*;
import java.util.Vector;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import cirkuit.util.Configuration;
import cirkuit.util.Cubic;
import cirkuit.util.Curves;

/**
 * The Circuit class manages a circuit for the game CirKuit 2D. It stores the shape of the circuit, enables
 * the user to draw it and to make some calculations on it.
 * TODO: implement normalize
 *
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class Circuit {
    // circuit properties
    private String name         = "";
    private int    maximumSpeed = 5;
    private int    tolerance    = 10;
    
    // envelopes
    private Envelope innerBorder  = new Envelope(Envelope.TYPE_INNER_BORDER, Color.GREEN);
    private Envelope outerBorder  = new Envelope(Envelope.TYPE_OUTER_BORDER, Color.RED);
    private Envelope startingLine = new Envelope(Envelope.TYPE_STARTING_LINE, new Color(146, 0, 230));
    
    /**
     * Creates a new circuit.
     */
    public Circuit() {
        startingLine.addPoint(100, 100);
        startingLine.addPoint(200, 100);
    }
    
    /**
     * Creates a new circuit using the specified string representation.
     * @param representation a string representation of the circuit
     * @see cirkuit.circuit.Circuit#toString
     */
    public Circuit(String representation) {
        // parsing the representation
        String[] rpa = representation.split("@");
        try {
            name         = rpa[0];
            maximumSpeed = Integer.parseInt(rpa[1]);
            tolerance    = Integer.parseInt(rpa[2]);
            innerBorder  = new Envelope(rpa[3]);
            outerBorder  = new Envelope(rpa[4]);
            startingLine = new Envelope(rpa[5]);
        } catch(Exception e) {
            // too bad...
        }
    }
    
    /**
     * Draws the circuit.
     * @param g the graphics object
     */
    public void draw(Graphics g) {
        draw(g, false);
    }

    /**
     * Draws the circuit in edit mode.
     * @param g the graphics object
     */
    public void draw(Graphics g, boolean editMode) {
        // envelopes
        innerBorder.draw(g, editMode);
        outerBorder.draw(g, editMode);
        startingLine.draw(g, editMode);
        
        // starting arrow
        g.setColor(startingLine.getColor());
        int r = 30;
        int d = 8;
        double alpha = getStartingAngle();
        double delta = 5*Math.PI/6;
        int[] x = new int[3];
        int[] y = new int[3];
        x[0] = (int)(r*Math.cos(alpha))+startingLine.xpoints[0];
        y[0] = (int)(r*Math.sin(alpha))+startingLine.ypoints[0];
        x[1] = (int)(d*Math.cos(alpha-delta))+x[0];
        y[1] = (int)(d*Math.sin(alpha-delta))+y[0];
        x[2] = (int)(d*Math.cos(alpha+delta))+x[0];
        y[2] = (int)(d*Math.sin(alpha+delta))+y[0];
        g.drawLine(startingLine.xpoints[0], startingLine.ypoints[0], x[0], y[0]);
        g.fillPolygon(x, y, x.length);
    }
    
    /**
     * Draws a preview of this circuit which fits in the rectangle defined by width/height.
     * @param g the graphics object
     * @param width the maximum width of the preview
     * @param height the maximum height of the preview
     */
    public void drawPreview(Graphics g, int width, int height) {
        double factor = Math.min((double)(width)/(double)(getWidth()), (double)(height)/(double)(getHeight()));
        innerBorder.drawResized(g, factor);
        outerBorder.drawResized(g, factor);
    }
    
    /**
     * Returns the width of the circuit.
     * @return the width
     */
    public int getWidth() {
        return Math.max(innerBorder.getWidth(), outerBorder.getWidth());
    }

    /**
     * Returns the height of the circuit.
     * @return the height
     */
    public int getHeight() {
        return Math.max(innerBorder.getHeight(), outerBorder.getHeight());
    }
    
    /**
     * Calculate the average length of the track.
     * @return the length (in pixels)
     */
    public int getLength() {
        return (int)((innerBorder.getLength() + outerBorder.getLength())/2);
    }
    
    /**
     * Tells wether the specified coordinates are inside or outside of the circuit.
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the coordinates are on the circuit
     */
    public boolean isInside(int x, int y) {
        return (outerBorder.contains(x, y) && !innerBorder.contains(x, y));
    }
    
    /**
     * Tells wether the specified point is inside or outside of the circuit.
     * @param p the point
     * @return true if the point is on the circuit
     */    
    public boolean isInside(Point p) {
        return isInside(p.x, p.y);
    }
    
    /**
     * Tells wether the segement defined by the two points (x0,y0), (x1,y1) crosses a border.
     * @return true if the segment crosses a border
     */
    public boolean crossedBorder(int x0, int y0, int x1, int y1) {
        return (innerBorder.crosses(x0, y0, x1, y1) || outerBorder.crosses(x0, y0, x1, y1));
    }
    
    /**
     * Tells wether the segement defined by the two points p0, p1 crosses a border.
     * @return true if the segment crosses a border
     */
    public boolean crossedBorder(Point p0, Point p1) {
        return crossedBorder(p0.x, p0.y, p1.x, p1.y);
    }
    
    /**
     * Tells wether the segement defined by the two points (x0,y0), (x1,y1) crosses the starting line.
     * @return 1 if the starting line was crossed in the wright way (depending on starting angle), -1 if the line was crossed in the wrong way and 0 if the line wasn't crossed.
     */
    public int crossedStartingLine(int x0, int y0, int x1, int y1) {
        return crossedStartingLine(new Point(x0, y0), new Point(x1, y1));
    }

    /**
     * Tells wether the segement defined by the two points p0, p1 crosses the starting line.
     * @return true if the segment crosses the starting line
     */
    public int crossedStartingLine(Point p0, Point p1) {
        Point inter = Curves.segmentIntersection(p0,
                                                 p1,
                                                 new Point(startingLine.xpoints[0], startingLine.ypoints[0]),
                                                 new Point(startingLine.xpoints[1], startingLine.ypoints[1]));
        if (inter != null) {
            if (!(p0.x == inter.x && p0.y == inter.y)) {
                double angle = Curves.getAngle(p1.x-p0.x, p1.y-p0.y);
                angle = Math.abs(angle-getStartingAngle());
                // nice job, you finished...
                if (angle < Math.PI/2 || angle > 3*Math.PI/2) {
                    return 1;
                } else {
                    // parrallel ?
                    if (angle == 0 || angle == Math.PI*2) {
                        return 0;
                    }
                    // wrong way !
                    return -1;
                }
            }
        }
        return 0;
    }

    /**
     * Adds a node on the inner border next to the existing node which is nearest to the point (x,y).
     * @param x x coordinate
     * @param y y coordinate
     */    
    public void addInnerNodeNearest(int x, int y) { innerBorder.addNearestPoint(x, y); }
    
    /**
     * Adds a node on the outer border next to the existing node which is nearest to the point (x,y).
     * @param x x coordinate
     * @param y y coordinate
     */   
    public void addOuterNodeNearest(int x, int y) { outerBorder.addNearestPoint(x, y); }
    
    /**
     * Adds a node, on the inner border, after the specified node. The new node will have it's index equal to <tt>n+1</tt>.
     * @param n the index of the node after wich the new node should be added
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addInnerNodeAfter(int n, int x, int y) { innerBorder.addPoint(x, y, n); }

    /**
     * Adds a node, on the outer border, after the specified node. The new node will have it's index equal to <tt>n+1</tt>.
     * @param n the index of the node after wich the new node should be added
     * @param x x coordinate
     * @param y y coordinate
     */    
    public void addOuterNodeAfter(int n, int x, int y) { outerBorder.addPoint(x, y, n); }
    
    /**
     * Returns the index of the node on the inner border.
     * @param x x coordinate
     * @param y y coordinate
     * @return index of the node on the inner border, -1 if there is none
     */
    public int getInnerNodeIndex(int x, int y) { return innerBorder.getPointIndex(x, y); }
    
    /**
     * Returns the index of the node on the outer border.
     * @param x x coordinate
     * @param y y coordinate
     * @return index of the node on the outer envelope, -1 if there is none
     */
    public int getOuterNodeIndex(int x, int y) { return outerBorder.getPointIndex(x, y); }

    /**
     * Returns the index of the node on the starting line.
     * @param x x coordinate
     * @param y y coordinate
     * @return index of the node on the starting line, -1 if there is none
     */
    public int getStartingLineNodeIndex(int x, int y) { return startingLine.getPointIndex(x, y); }
    
    /**
     * Returns the index of the node, on the inner border, near the point identified by (x&plusmn;tolerance,y&plusmn;tolerance).<br>
     * @param x x coordinate
     * @param y y coordinate
     * @return index of the node on the inner border, -1 if there is none
     * @see cirkuit.circuit.Circuit#setTolerance
     * @see cirkuit.circuit.Circuit#getTolerance
     */
    public int getInnerNodeIndexApproximate(int x, int y) { return innerBorder.getPointIndexApproximate(x, y, tolerance); }
    
    /**
     * Returns the index of the node, on the outer border, near the point identified by (x&plusmn;tolerance,y&plusmn;tolerance).
     * @param x x coordinate
     * @param y y coordinate
     * @return index of the node on the outer envelope, -1 if there is none
     * @see cirkuit.circuit.Circuit#setTolerance
     * @see cirkuit.circuit.Circuit#getTolerance
     */
    public int getOuterNodeIndexApproximate(int x, int y) { return outerBorder.getPointIndexApproximate(x, y, tolerance); }

    /**
     * Returns the index of the node, on the starting line, near the point identified by (x&plusmn;tolerance,y&plusmn;tolerance).
     * @param x x coordinate
     * @param y y coordinate
     * @return index of the node on the starting line, -1 if there is none
     * @see cirkuit.circuit.Circuit#setTolerance
     * @see cirkuit.circuit.Circuit#getTolerance
     */
    public int getStartingLineNodeIndexApproximate(int x, int y) { return startingLine.getPointIndexApproximate(x, y, tolerance); }
    
    /**
     * Resets the value of the inner node.
     * @param index the index of the inner node
     * @param x coordinate
     * @param y y coordinate
     */
    public void setInnerNode(int index, int x, int y) { innerBorder.modifyPoint(x, y, index); }

    /**
     * Resets the value of the outer node.
     * @param index the index of the outer node
     * @param x coordinate
     * @param y y coordinate
     */    
    public void setOuterNode(int index, int x, int y) { outerBorder.modifyPoint(x, y, index); }

    /**
     * Resets the value of the starting line node. The starting line can only be horizontal or vertical
     * thus, this function finds the nearest point to (x,y) which satisfies these conditions.
     * @param index the index of the starting line node
     * @param x coordinate
     * @param y y coordinate
     */    
    public void setStartingLineNode(int index, int x, int y) {
        int other = 1-index;
        int xnew,ynew;
        if (Math.abs(startingLine.xpoints[other]-x) > Math.abs(startingLine.ypoints[other]-y)) {
            xnew = x;
            ynew = startingLine.ypoints[other];
        } else {
            xnew = startingLine.xpoints[other];
            ynew = y;
        }
        startingLine.modifyPoint(xnew, ynew, index);
    }
    
    /**
     * Deletes the node.
     * @param index the index of the inner node
     */
    public void deleteInnerNode(int index) { innerBorder.deletePoint(index); }
    
    /**
     * Deletes the node.
     * @param index the index of the outer node
     */
    public void deleteOuterNode(int index) { outerBorder.deletePoint(index); }
    
    public void reset() {
        innerBorder.reset();
        outerBorder.reset();
    }
    
    
    /**
     * Gets a string representation of the inner border. The string consists of comma separated integers which represent the x,y coordinate of the nodes.
     * @return a string representation of the inner border
     */
    public String getInnerBorderDescription() { return innerBorder.getEnvelopeDescription(); }

    /**
     * Gets a string representation of the outer border. The string consists of comma separated integers which represent the x,y coordinate of the nodes.
     * @return a string representation of the outer border
     */
    public String getOuterBorderDescription() { return outerBorder.getEnvelopeDescription(); }
    
    /**
     * Gets a string representation of the starting line. The string consists of comma separated integers which represent the x,y coordinate of two nodes.
     * @return a string representation of the starting line
     */
    public String getStartingLineDescription() { return startingLine.getEnvelopeDescription(); }
    
    /**
     * Returns a <tt>Point</tt> array containing the nodes of the inner border of the circuit.
     * @return a <tt>Point</tt> array containing the nodes of the inner border of the circuit
     */
    public Point[] getInnerBorder() { return innerBorder.getEnvelope(); }
    
    /**
     * Returns a <tt>Point</tt> array containing the nodes of the outer border of the circuit.
     * @return a <tt>Point</tt> array containing the nodes of the outer border of the circuit
     */
    public Point[] getOuterBorder() { return outerBorder.getEnvelope(); }
    
    /**
     * Returns a <tt>Point</tt> array containing the two points of the starting line.
     * @return a <tt>Point</tt> array containing the two points of the starting line
     */
    public Point[] getStartingLine() { return startingLine.getEnvelope(); }
    
    /**
     * Sets the inner border using a string representation. The string should be a comma separated list of integers which represent the x,y coordinate of the nodes.
     * If a value is not an integer it will be ignored. If the list does not represent an even value of integers, the last one will be igmored.<br>
     * <b>e.g.: </b> the string "10,10,10,200,kl,200,200,200,10,45" represents the nodes (10,10), (10,200), (200,200), (200,10)
     * @param s the string representation of the inner border
     */
    public void setInnerBorder(String s) { innerBorder.setEnvelopeDescription(s); }

    /**
     * Sets the outer border using a string representation. The string should be a comma separated list of integers which represent the x,y coordinate of the nodes.
     * If a value is not an integer it will be ignored. If the list does not represent an even value of integers, the last one will be igmored.<br>
     * <b>e.g.: </b> the string "10,10,10,200,kl,200,200,200,10,45" represents the nodes (10,10), (10,200), (200,200), (200,10)
     * @param s the string representation of the outer border
     */
    public void setOuterBorder(String s) { outerBorder.setEnvelopeDescription(s); }
    
    /**
     * Sets the starting line using a string representation. The string should be a comma separated list of integers which represent the x,y coordinate of the nodes.
     * If a value is not an integer it will be ignored. If the list does not represent two nodes (four integer values), describing a horizontal or vertical starting line,
     * the operation will fail and the starting line will keep it's original value.
     * @param s the string representation of the starting line
     */
    public void setStartingLineBorder(String s) {
        int[] coord = new int[4];
        int tmp;
        int i = 0;
        String[] envelopeS = s.split(",");
        while (i < 4 && i < envelopeS.length) {
            try {
                tmp = (new Integer(Integer.parseInt(envelopeS[i]))).intValue();
                if (tmp >= 0) {
                    coord[i] = tmp;
                    i++;
                }
            } catch (Exception er) {
                // we will just ignore this value
            }
        }
        if (i == 4) {
            startingLine.reset();
            startingLine.addPoint(coord[0], coord[1]);
            startingLine.addPoint(coord[2], coord[3]);
        }
    }
    
    /**
     * Gets the circuit's name.
     * @return the circuit's name
     */
    public String getName() { return name; }

    /**
     * Sets the circuit's name.
     * @param name the circuit's name
     */
    public void setName(String name) { this.name = name; }
    
    /**
     * Sets the inner border color.
     * @param c the color
     */
    public void setInnerColor(Color c) { innerBorder.setColor(c); }
    
    /**
     * Sets the outer border color.
     * @param c the color
     */
    public void setOuterColor(Color c) { outerBorder.setColor(c); }
    
    /**
     * Sets the starting line color.
     * @param c the color
     */
    public void setStartingLineColor(Color c) { startingLine.setColor(c); }
    
    /**
     * Returns the starting angle in radians.
     * @return the starting angle in radians
     */
    public double getStartingAngle() {
        if (startingLine.xpoints[0] == startingLine.xpoints[1]) {
            if (startingLine.ypoints[0] < startingLine.ypoints[1]) {
                return 0;
            } else {
                return Math.PI;
            }
        } else {
            if (startingLine.xpoints[0] < startingLine.xpoints[1]) {
                return Math.PI/2;
            } else {
                return 3*Math.PI/2;
            }
        }
    }
    
    /**
     * Returns the maximum speed allowed for this circuit.
     * @return the maximum speed
     */
    public int getMaximumSpeed() { return maximumSpeed; }

    /**
     * Sets the maximum speed allowed for this circuit.
     * @param v the maximum speed
     */
    public void setMaximumSpeed(int v) { this.maximumSpeed = v; }
    
    /**
     * Returns the tolerance. 
     * @return the tolerance
     */
    public int getTolerance() { return tolerance; }
    
    /**
     * Sets the tolerance.
     * @param t the tolerance
     */
    public void setTolerance(int t) { this.tolerance = t; }
    
    /**
     * Resize the circuit by a certain factor.
     * @param factor the resizing factor
     */
    public void resize(double factor) {
        innerBorder.resize(factor);
        outerBorder.resize(factor);
        startingLine.resize(factor);
    }
    
    /**
     * Rotate a circuit.
     * @param angle the angle of rotation in radians
     */
    public void rotate(double angle) {
        int xCenter = getWidth()/2;
        int yCenter = getHeight()/2;
        
        innerBorder.rotate(angle, xCenter, yCenter);
        outerBorder.rotate(angle, xCenter, yCenter);
        
        normalize();
    }

    /**
     * Repositions a circuit to the origin.
     */
    public void normalize() {
        /*
        //Point p0 = getNormalizingVector(innerBorder);
        Point p1 = getNormalizingVector(outerBorder);
        Point p0 = p1;
        Point tmp = null;
        if (p0.x == -1 || p1.x == -1) {
            tmp = new Point(Math.max(p0.x, p1.x), Math.max(p0.y, p1.y));
        } else {
            tmp = new Point(Math.min(p0.x, p1.x), Math.min(p0.y, p1.y));
        }
        
        innerBorder.translate(-tmp.x, -tmp.y);
        outerBorder.translate(-tmp.x, -tmp.y);
        startingLine.translate(-tmp.x, -tmp.y);*/
    }
    
    private Point getNormalizingVector(Vector e) {
        int n = e.size();
        int dx = -1;
        int dy = -1;
        Point tmp;
        for (int i = 0; i < n; i++) {
            tmp = (Point)e.get(i);
            if (dx == -1 || dx > tmp.x) {
                dx = tmp.x;
            }
            if (dy == -1 || dy > tmp.y) {
                dy = tmp.y;
            }
        }
        return new Point(dx, dy);
    }
    
    /**
     * Reduces the precision of the circuit.
     * @param p the percentage of precision loss (p &isin; ]0,1[)
     */
    public void reducePrecision(float p) {
        innerBorder.reduce(p);
        outerBorder.reduce(p);
    }
    
    /**
     * Interpolates the existing nodes using the closed natural cubic splines technique.
     * Mathematical references can be found at <a href="http://mathworld.wolfram.com/CubicSpline.html">mathworld's cubic spline</a>
     * and impletation references at <a href="http://www.cse.unsw.edu.au/~lambert/splines/source.html">Java Source code of spline applets</a>.
     * @param p add p nodes between two existing node
     */
    public void spline(int p) {
        innerBorder.spline(p);
        outerBorder.spline(p);
    }
    
    /**
     * Saves the current circuit in a plain text file. The extension <i>should</i> be <b>ckt</b>.
     * @param url location of the file. If it does not exists, it will be created
     * @return the result of the saving operation
     */
    public boolean save(String url) {
        Configuration configuration = new Configuration(url);
        configuration.set("name",         name);
        configuration.set("innerBorder",  innerBorder.getEnvelopeDescription());
        configuration.set("outerBorder",  outerBorder.getEnvelopeDescription());
        configuration.set("startingLine", startingLine.getEnvelopeDescription());
        configuration.set("maximumSpeed", ""+maximumSpeed);
        configuration.set("tolerance",    ""+tolerance);
        return configuration.write();
    }

    /**
     * Loads a circuit from a file into the current object.
     * @param url location of the file from which we load the circuit
     * @return the result of the loading operation
     */
    public boolean load(String url) {
        Configuration configuration = new Configuration(url);
        if (configuration.read()) {
            name = configuration.get("name");
            innerBorder.setEnvelopeDescription(configuration.get("innerBorder"));
            outerBorder.setEnvelopeDescription(configuration.get("outerBorder"));
            startingLine.setEnvelopeDescription(configuration.get("startingLine"));
            try {
                maximumSpeed = Integer.parseInt(configuration.get("maximumSpeed"));
                tolerance    = Integer.parseInt(configuration.get("tolerance"));
            } catch (Exception e) {
                // too bad... you'll have the default values
            }
            return true;
        }
        return false;
    }
    
    /**
     * Returns a string representation of this Circuit. The string is composed of fields separated by '@'. It can be used
     * to create a new object using the <code>Circuit(String representation)</code> constructor.
     * @return a string representation of this Circuit
     */
    public String toString() {    
        return this.name+"@"+
               this.maximumSpeed+"@"+
               this.tolerance+"@"+
               this.innerBorder.toString()+"@"+
               this.outerBorder.toString()+"@"+
               this.startingLine.toString();
    }
}
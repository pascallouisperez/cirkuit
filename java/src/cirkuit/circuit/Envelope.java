package cirkuit.circuit;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.Vector;

import cirkuit.util.Curves;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class Envelope extends Polygon {
    /**
     * Inner border.
     */
    public final static int TYPE_INNER_BORDER = 0;
    /**
     * Outer border.
     */
    public final static int TYPE_OUTER_BORDER = 1;
    /**
     * Starting line.
     */
    public final static int TYPE_STARTING_LINE = 2;
    /**
     * Obstacle.
     */
    public final static int TYPE_OBSTACLE = 3;
    /**
     * Pit stop.
     */
    public final static int TYPE_PITSTOP = 4;
    /**
     * Closed path.
     */
    public final static int TYPE_CLOSED_PATH = 5;
    
    /**
     * This class manages two arrays xpoints and ypoints which are much bigger
     * than the number of points they contain. When the arrays are full they will
     * be enlarged by a certain amount which is defined by this variable.
     */
    private static int ENLARGE_BUFFER = 100;
    
    // type of this envelope
    private int type;
    // color of this envelope
    private Color color = null;
    
    /**
     * Creates an empty Envelope of the specified type.
     * @param type this Envelope's type
     */
    public Envelope(int type) {
        super();
        this.type = type;
    }
    
    /**
     * Creates an empty Envelope of the specified type with a specific color.
     * @param type this Envelope's type
     * @param color the envelope's color
     */
    public Envelope(int type, Color color) {
        this(type);
        this.color = color;
    }
    
    /**
     * Constructs and initializes an Envelope from the specified parameters.
     * @param xpoints an array of x coordinates
     * @param ypoints an array of y coordinates
     * @param npoints the total number of points in the Envelope
     * @param type this Envelope's type
     */
    public Envelope(int[] xpoints, int[] ypoints, int npoints, int type) {
        this(xpoints, ypoints, npoints, type, null);
    }
    
    /**
     * Constructs and initializes an Envelope from the specified parameters.
     * @param xpoints an array of x coordinates
     * @param ypoints an array of y coordinates
     * @param npoints the total number of points in the Envelope
     * @param type this Envelope's type
     * @param color the envelope's color
     */
    public Envelope(int[] xpoints, int[] ypoints, int npoints, int type, Color color) {
        super(xpoints, ypoints, npoints);
        this.type = type;
        this.color = color;
    }
    
    /**
     * Constructs and initializes an Envelope from the specified parameters.
     * @param points the points array
     * @param type this Envelope's type
     */
    public Envelope(Point[] points, int type) {
        this(points, type, null);
    }
    
    /**
     * Constructs and initializes an Envelope from the specified parameters.
     * @param points the points array
     * @param type this Envelope's type
     * @param color the envelope's color
     */
    public Envelope(Point[] points, int type, Color color) {
        this(type, color);
        for (int i = 0; i < points.length; i++)
            addPoint(points[i].x, points[i].y);
    }
    
    /**
     * Constructs and initializes an Envelope using the specified representation.
     * @param representation a string representation of the Envelope
     * @see cirkuit.circuit.Envelope#toString
     */
    public Envelope(String representation) {
        super();
        
        // default values
        type = TYPE_INNER_BORDER;
        color = Color.black;
        
        // parsing the representation
        String[] rpa = representation.split("#");
        try {
            type = Integer.parseInt(rpa[0]);
            String[] colora = rpa[1].split(",");
            color = new Color(Integer.parseInt(colora[0]), Integer.parseInt(colora[1]), Integer.parseInt(colora[2]));
            setEnvelopeDescription(rpa[2]);
        } catch(Exception e) {
            // too bad...
        }
    }
    
    /**
     * Appends the specified point to this Envelope.
     * @param p the specified point
     */
    public void addPoint(Point p) {
        addPoint(p.x, p.y);
    }
    
    /**
     * Appends the specified point to this Envelope at the specified index.
     * @param p the specified point
     * @param index the index at wich the point should be added
     */
    public void addPoint(Point p, int index) {
        addPoint(p.x, p.y, index);
    }
    
    /**
     * Appends the specified point to this Envelope at the specified index.
     * @param x the specified x coordinate
     * @param y the specified y coordinate
     * @param index the index at wich the point should be added
     */
    public void addPoint(int x, int y, int index) {
        if (index >= 0 && index < npoints) {
            // the two arrays should have the same size but we are never too sure
            int size = Math.min(xpoints.length, ypoints.length);
            if (npoints < size) {
                // moving values up
                for (int i = npoints, j = npoints-1; i > index; i--, j--) {
                    xpoints[i] = xpoints[j];
                    ypoints[i] = ypoints[j];
                }
                // adding the new node
                xpoints[index] = x;
                ypoints[index] = y;
            } else {
                size += ENLARGE_BUFFER;
                int[] xt = new int[size];
                int[] yt = new int[size];
                int i;
                // lower part
                for (i = 0; i < index; i++) {
                    xt[i] = xpoints[i];
                    yt[i] = ypoints[i];
                }
                // placing the (x, y) at the specified index
                xt[i] = x;
                yt[i] = y;
                // upper part
                for (int j = i+1; i < npoints; i++, j++) {
                    xt[j] = xpoints[i];
                    yt[j] = ypoints[i];
                }
                xpoints = xt;
                ypoints = yt;
            }
            // we increase the number of points
            npoints++;
        }
        invalidate();
    }
    
    /**
     * Appends the specified point to this Envelope after the existing point which is nearest to (x,y).
     * @param p the specified point
     */
    public void addNearestPoint(Point p) {
        addNearestPoint(p.x, p.y);
    }
    
    /**
     * Appends the specified point to this Envelope after the existing point which is nearest to (x,y).
     * @param x the specified x coordinate
     * @param y the specified y coordinate
     */
    public void addNearestPoint(int x, int y) {
        if (npoints == 0)
            addPoint(x, y);
        else
            addPoint(x, y, getNearestPointIndex(x, y));
    }
    
    /**
     * Modify the specified point of this Envelope.
     * @param x the new x coordinate
     * @param y the new y coordinate
     * @param index the index point's index
     */
    public void modifyPoint(int x, int y, int index) {
        if (index >= 0 && index < npoints) {
            xpoints[index] = x;
            ypoints[index] = y;
        }
        invalidate();
    }
    
    /**
     * Deletes the specified point to this Envelope at the specified index.
     * @param index the index point's index
     */
    public void deletePoint(int index) {
        if (index >= 0 && index < npoints) {
            // moving the upper part
            for (int i = index+1, j = index; i < npoints; i++, j++) {
                xpoints[j] = xpoints[i];
                ypoints[j] = ypoints[i];
            }
            npoints--;
        }
        invalidate();
    }
    
    /**
     * Deletes the point which is nearest to (x,y).
     * @param x the specified x coordinate
     * @param y the specified y coordinate
     */
    public void deleteNearestPoint(int x, int y) {
        deletePoint(getNearestPointIndex(x, y));
    }
    
    /**
     * Get the point (xpoints[index], ypoints[index]) as a point object.
     * @return the point, null is the index is out of range
     */
    public Point getPoint(int index) {
        if (0 <= index && index < npoints)
            return new Point(xpoints[index], ypoints[index]);
        return null;
    }
    
    /**
     * Returns the index of the point.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return the index of the point, -1 if it wasn't found. If two or more points are located
     * at (x, y), the first index will be returned
     */
    public int getPointIndex(int x, int y) {
        for (int i = 0; i < npoints; i++)
            if (xpoints[i] == x && ypoints[i] == y)
                return i;
        return -1;
    }

    /**
     * Returns the index of the point which is nearest to (x, y).
     * @param x the specified x coordinate
     * @param y the specified y coordinate
     * @return the index of the nearest point
     */
    public int getNearestPointIndex(int x, int y) {
        double length = -1;
        double l = -1;
        int dx, dy;
        int index = 0;
        for (int i = 0; i < npoints; i++) {
            dx = xpoints[i]-x;
            dy = ypoints[i]-y;
            l = dx*dx+dy*dy; // we are working with length squares...
            if (length < 0 || l < length) {
                length = l;
                index = i;
            }
        }
        return index;
    }
    
    /**
     * Returns the index of point on this Envelope which is nearest to the point (x, y) and in
     * the domain (x&plusmn;tolerance,y&plusmn;tolerance).
     * @param x x coordinate
     * @param y y coordinate
     * @param tolerance the tolerance value
     * @return index of the point on the starting line, -1 if there is none
     */
    public int getPointIndexApproximate(int x, int y, int tolerance) {
        int i = getNearestPointIndex(x, y);
        if (xpoints[i] >= x-tolerance && xpoints[i] <= x+tolerance && ypoints[i] >= y-tolerance && ypoints[i] <= y+tolerance)
            return i;
        return -1;
    }
    
    /**
     * Get the Envelope.
     * @return a <tt>Point</tt> array containing the nodes of this Envelope
     */
    public Point[] getEnvelope() {
        Point[] points = new Point[npoints];
        for (int i = 0; i < npoints; i++)
            points[i] = new Point(xpoints[i], ypoints[i]);
        return points;
    }
    
    /**
     * Set the Envelope using a string representation. The string should be a comma separated list of integers
     * which represent the x,y coordinate of the nodes.
     * If a value is not an integer it will be ignored. If the list does not represent an even value of integers,
     * the last one will be igmored.<br>
     * <b>e.g.: </b> the string "10,10,10,200,kl,200,200,200,10,45" represents the nodes (10,10), (10,200), (200,200), (200,10)
     * @param s the string representation of the inner border
     */
    public void setEnvelopeDescription(String s) {
        // we put all the positive values in a table
        int tmp = -1;
        String[] envelopeS = s.split(",");
        int[] temporarys = new int[envelopeS.length];
        int temporarysIndex = 0;
        for (int i = 0; i < envelopeS.length; i++) {
            try {
                tmp = Integer.parseInt(envelopeS[i]);
                if (tmp >= 0) {
                    temporarys[temporarysIndex] = tmp;
                    temporarysIndex++;
                }
            } catch (Exception er) {
                // we will just ignore this value
            }
        }
        
        // now we can copy the values in xpoints and ypoints
        npoints = temporarysIndex>>1;
        if (npoints > Math.min(xpoints.length, ypoints.length)) {
            tmp = npoints+ENLARGE_BUFFER;
            xpoints = new int[tmp];
            ypoints = new int[tmp];
        }
        for (int i = 0, j = 0; j < npoints; i++, j++) {
            xpoints[j] = temporarys[i];
            i++;
            ypoints[j] = temporarys[i];
        }
        invalidate();
    }
    
    /**
     * Get a string representation of this Envelope. The string consists of comma separated integers which represent
     * the x,y coordinate of two nodes.
     * @return a string representation of the starting line
     */
    public String getEnvelopeDescription() {
        StringBuffer description = new StringBuffer(npoints<<3);
        for (int i = 0; i < npoints; i++) {
            if (i != 0) {
                description.append(',');
            }
            description.append(xpoints[i]+","+ypoints[i]);
        }
        return description.toString();
    }
    
    /**
     * Computes wether the segment intersect this Envelop or not.
     * @param x0 x coordinate of the first point of the egment
     * @param y0 y coordinate of the first point of the segment
     * @param x1 x coordinate of the second point of the segment
     * @param y1 y coordinate of the second point of the segment
     * @return true if the segment intersect this Envelop
     */
    public boolean crosses(int x0, int y0, int x1, int y1) {
        if (npoints > 1) {
            Point p;
            int j = 1;
            for (int i = 0; i < npoints; i++) {
                if (j >= npoints) {
                    j = 0;
                }
                p = Curves.segmentIntersection(x0, y0, x1, y1, xpoints[i], ypoints[i], xpoints[j], ypoints[j]);
                if (p != null) {
                    return true;
                }
                j++;
            }
        }
        return false;
    }
    
    /**
     * Resize this Envelope.
     * @param factor the resizing factor
     */
    public void resize(double factor) {
        for (int i = 0; i < npoints; i++) {
            xpoints[i] = (int)(xpoints[i]*factor);
            ypoints[i] = (int)(ypoints[i]*factor);
        }
        invalidate();
    }
    
    /**
     * Rotate this Envelope relative to the (x, y) point.
     * @param angle the angle of rotation in radians
     * @param x the x coordinate of the center
     * @param y the y coordinate of the center
     */
    public void rotate(double angle, int x, int y) {
        AffineTransform t = new AffineTransform();
        t.rotate(angle, x, y);
        Point tmp;
        for (int i = 0; i < npoints; i++) {
            tmp = new Point(xpoints[i], ypoints[i]);
            t.transform(tmp, tmp);
            xpoints[i] = tmp.x;
            ypoints[i] = tmp.y;
        }
        invalidate();
    }
    
    /**
     * Translates the Envelope.
     * @param tx the distance by which the Envelope is translated in the X axis direction
     * @param ty the distance by which the Envelope is translated in the Y axis direction
     */
    public void translate(int tx, int ty) {
        for (int i = 0; i < npoints; i++) {
            xpoints[i] += tx;
            ypoints[i] += ty;
        }
        invalidate();
    }
    
    /**
     * Reduces the precision of the circuit.
     * @param p the percentage of precision loss (p &isin; ]0,1[)
     */
    public void reduce(float p) {
        if (0 < p && p < 1) {
            int step = Math.round((float)(1.0)/(1-p));
            if (1 < step) {
                int j = 1;
                for (int i = step; i < npoints; i += step, j++) {
                    xpoints[j] = xpoints[i];
                    ypoints[j] = ypoints[i];
                }
                npoints = j;
            }
        }
        invalidate();
    }

    /**
     * Interpolates the existing nodes using the closed natural cubic splines technique.
     * @param p add p nodes between two existing node
     * @see cirkuit.util.Curves#closedSplineInterpolation
     */
    public void spline(int p) {
        if (npoints > 2) {
            int i;
            Vector e = new Vector(npoints);
            for (i = 0; i < npoints; i++) {
                e.add(new Point(xpoints[i], ypoints[i]));
            }
            e = Curves.closedSplineInterpolation(e, p);
            npoints = e.size();
            xpoints = new int[npoints];
            ypoints = new int[npoints];
            Point tmp;

            for (i = 0; i < npoints; i++) {
                tmp = (Point)(e.get(i));
                xpoints[i] = tmp.x;
                ypoints[i] = tmp.y;
            }
            
            invalidate();
        }
    }
    
    /**
     * Draw this envelope without the nodes.
     * @param g the graphics object
     */
    public void draw(Graphics g) {
        draw(g, false);
    }
    
    /**
     * Draw this envelope with or without the nodes.
     * @param g the graphics object
     * @param nodes if true, the nodes will be drawn
     */
    public void draw(Graphics g, boolean nodes) {
        Color gcolor = g.getColor();
        if (color != null)
            g.setColor(color);
        for (int i = 0, j = 1; i < npoints; i++, j++) {
            if (j == npoints) j = 0;
            if (nodes) {
                g.fillRect(xpoints[i]-3, ypoints[i]-3, 7, 7);
            }
            g.drawLine(xpoints[i], ypoints[i], xpoints[j], ypoints[j]);
        }
        g.setColor(gcolor);
    }
    
    /**
     * Draw this a resized envelope with or without the nodes.
     * @param g the graphics object
     * @param factor resize factor
     */
    public void drawResized(Graphics g, double factor) {
        Color gcolor = g.getColor();
        g.setColor(color);
        int i = 0;
        int x, y;
        for (int j = 1; j < npoints; j++) {
            x = (int)(factor*xpoints[i]);
            y = (int)(factor*ypoints[i]);
            g.drawLine(x, y, (int)(factor*xpoints[j]), (int)(factor*ypoints[j]));
            i = j;
        }
        g.setColor(gcolor);
    }
    
    /**
     * Calculate this Envelope's width.
     * @return this Envelope's width
     */
    public double getLength() {
        double l = 0;
        int x, y;
        for (int i = 0, j = 1; j < npoints; i++, j++) {
            x = xpoints[i]-xpoints[j];
            y = ypoints[i]-ypoints[j];
            l += Math.sqrt(x*x+y*y);
        }
        x = xpoints[npoints - 1]-xpoints[0];
        y = ypoints[npoints - 1]-ypoints[0];
        return l + Math.sqrt(x*x+y*y);
    }
    
    /**
     * Calculate this Envelope's width.
     * @return this Envelope's width
     */
    public int getWidth() {
        int width = -1;
        for (int i = 0; i < npoints; i++) {
            if (width < xpoints[i]) {
                width = xpoints[i];
            }
        }
        return width;
    }
    
    /**
     * Calculate this Envelope's height.
     * @return this Envelope's height
     */
    public int getHeight() {
        int height = -1;
        for (int i = 0; i < npoints; i++) {
            if (height < ypoints[i]) {
                height = ypoints[i];
            }
        }
        return height;
    }
    
    /**
     * Sets this Envelope's color.
     * @param color this Envelope's color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Gets this Envelope's color.
     * @return this Envelope's color
     */
    public Color getColor() {
        return this.color;
    }
    
    /**
     * Returns a string representation of this Envelope. The string is composed of fields separated by '#'. It can be used
     * to create a new object using the <code>Envelope(String representation)</code> constructor.
     * @return a string representation of this Envelope
     */
    public String toString() {
        return this.type+"#"+this.color.getRed()+","+this.color.getGreen()+","+this.color.getBlue()+"#"+getEnvelopeDescription();
    }
}

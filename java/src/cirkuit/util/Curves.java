package cirkuit.util;

import java.util.Vector;
import java.awt.Point;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class Curves {
    /**
     * Interpolates the trajectory using the natural cubic splines technique.<br>
     * Mathematical references can be found at <a href="http://mathworld.wolfram.com/CubicSpline.html">mathworld's cubic spline</a>
     * and impletation references at <a href="http://www.cse.unsw.edu.au/~lambert/splines/source.html">Java Source code of spline applets</a>.
     * @param e the trajectory
     * @param p add p nodes between two existing node
     */
    public static Vector splineInterpolation(Vector e, int p) {
        // just a counter
        int k;
        
        // we construct an array containing all x coordinates 
        // of the point of the curve
        int n = e.size()-1;
        int[] x = new int[n+1];
        int[] y = new int[n+1];
        for (k = 0; k <= n;k++) {
            x[k] = ((Point)(e.get(k))).x;
            y[k] = ((Point)(e.get(k))).y;
        }
        
        Cubic[] X = naturalCubicSpline(n, x);
        Cubic[] Y = naturalCubicSpline(n, y);
        
        // now compute new points
        float tstep = (float)1/(float)p;
        float tmax  = 1-tstep;
        float t;
        
        Vector en = new Vector();
        for (k = 0; k <= n; k++) {
            en.add(new Point(x[k], y[k]));
            for (t = tstep; t <= tmax; t += tstep) {
                en.add(new Point(Math.round(X[k].eval(t)), Math.round(Y[k].eval(t))));
            }
        }
        
        return en;
    }
    
    private static Cubic[] naturalCubicSpline(int n, int[] x) {
        float[] gamma = new float[n+1];
        float[] delta = new float[n+1];
        float[] D = new float[n+1];
        int i;
        /*
          We solve the equation
          [2 1       ] [D[0]]   [3(x[1] - x[0])  ]
          |1 4 1     | |D[1]|   |3(x[2] - x[0])  |
          |  1 4 1   | | .  | = |      .         |
          |    ..... | | .  |   |      .         |
          |     1 4 1| | .  |   |3(x[n] - x[n-2])|
          [       1 2] [D[n]]   [3(x[n] - x[n-1])]
        
          by using row operations to convert the matrix to upper triangular
          and then back sustitution.  The D[i] are the derivatives at the knots.
        */
        
        gamma[0] = 1.0f/2.0f;
        for ( i = 1; i < n; i++) {
            gamma[i] = 1/(4-gamma[i-1]);
        }
        gamma[n] = 1/(2-gamma[n-1]);
        
        delta[0] = 3*(x[1]-x[0])*gamma[0];
        for ( i = 1; i < n; i++) {
            delta[i] = (3*(x[i+1]-x[i-1])-delta[i-1])*gamma[i];
        }
        delta[n] = (3*(x[n]-x[n-1])-delta[n-1])*gamma[n];
        
        D[n] = delta[n];
        for ( i = n-1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i]*D[i+1];
        }
        
        /* now compute the coefficients of the cubics */
        Cubic[] C = new Cubic[n];
        for ( i = 0; i < n; i++) {
            C[i] = new Cubic((float)x[i], D[i], 3*(x[i+1] - x[i]) - 2*D[i] - D[i+1], 2*(x[i] - x[i+1]) + D[i] + D[i+1]);
        }
        return C;
    }
    
    /**
     * Interpolates the envelope using the closed natural cubic splines technique.<br>
     * Mathematical references can be found at <a href="http://mathworld.wolfram.com/CubicSpline.html">mathworld's cubic spline</a>
     * and impletation references at <a href="http://www.cse.unsw.edu.au/~lambert/splines/source.html">Java Source code of spline applets</a>.
     * @param e the envelope
     * @param p add p nodes between two existing node
     */
    public static Vector closedSplineInterpolation(Vector e, int p) {
        // just a counter
        int k;
        
        // we construct an array containing all x coordinates 
        // of the point of the envelope
        int n = e.size()-1;
        int[] x = new int[n+1];
        int[] y = new int[n+1];
        for (k = 0; k <= n;k++) {
            x[k] = ((Point)(e.get(k))).x;
            y[k] = ((Point)(e.get(k))).y;
        }
        
        Cubic[] X = naturalClosedCubicSpline(n, x);
        Cubic[] Y = naturalClosedCubicSpline(n, y);
        
        // now compute new points
        float tstep = (float)1/(float)p;
        float tmax  = 1-tstep;
        float t;
        
        Vector en = new Vector();
        for (k = 0; k <= n; k++) {
            en.add(new Point(x[k], y[k]));
            for (t = tstep; t <= tmax; t += tstep) {
                en.add(new Point(Math.round(X[k].eval(t)), Math.round(Y[k].eval(t))));
            }
        }
        
        return en;
    }
    
    private static Cubic[] naturalClosedCubicSpline(int n, int[] x) {
        float[] w = new float[n+1];
        float[] v = new float[n+1];
        float[] y = new float[n+1];
        float[] D = new float[n+1];
        float z, F, G, H;
        int k;
        /*
          We solve the equation
          [4 1      1] [D[0]]   [3(x[1] - x[n])  ]
          |1 4 1     | |D[1]|   |3(x[2] - x[0])  |
          |  1 4 1   | | .  | = |      .         |
          |    ..... | | .  |   |      .         |
          |     1 4 1| | .  |   |3(x[n] - x[n-2])|
          [1      1 4] [D[n]]   [3(x[0] - x[n-1])]
          
          by decomposing the matrix into upper triangular and lower matrices
          and then back sustitution.  See Spath "Spline Algorithms for Curves
          and Surfaces" pp 19--21. The D[i] are the derivatives at the knots.
        */
        w[1] = v[1] = z = 1.0f/4.0f;
        y[0] = z * 3 * (x[1] - x[n]);
        H = 4;
        F = 3 * (x[0] - x[n-1]);
        G = 1;
        for ( k = 1; k < n; k++) {
            v[k+1] = z = 1/(4 - v[k]);
            w[k+1] = -z * w[k];
            y[k] = z * (3*(x[k+1]-x[k-1]) - y[k-1]);
            H = H - G * w[k];
            F = F - G * y[k-1];
            G = -v[k] * G;
        }
        H = H - (G+1)*(v[n]+w[n]);
        y[n] = F - (G+1)*y[n-1];
        
        D[n] = y[n]/H;
        D[n-1] = y[n-1] - (v[n]+w[n])*D[n]; /* This equation is WRONG! in my copy of Spath */
        for ( k = n-2; k >= 0; k--) {
            D[k] = y[k] - v[k+1]*D[k+1] - w[k+1]*D[n];
        }
        
        /* now compute the coefficients of the cubics */
        Cubic[] C = new Cubic[n+1];
        for ( k = 0; k < n; k++) {
            C[k] = new Cubic((float)x[k], D[k], 3*(x[k+1] - x[k]) - 2*D[k] - D[k+1], 2*(x[k] - x[k+1]) + D[k] + D[k+1]);
        }
        C[n] = new Cubic((float)x[n], D[n], 3*(x[0] - x[n]) - 2*D[n] - D[0], 2*(x[n] - x[0]) + D[n] + D[0]);
        return C;
    }
    
    public static Vector bezier(Point start, Point end, Point ctrl0, Point ctrl1) {
        Point[] curve = new Point[4];
        curve[0] = start;
        curve[1] = ctrl0;
        curve[2] = ctrl1;
        curve[3] = end;
        
        int STEPS = 6;
        Vector e = new Vector();
        e.add(p(0,0, curve));
        for (int j = 1; j <= STEPS; j++) {
            e.add(p(0,j/(float)STEPS, curve));
        }
        return e;
    }
    
    // the basis function for a Bezier spline
    private static float b(int i, float t) {
        switch (i) {
            case 0:
                return (1-t)*(1-t)*(1-t);
                
            case 1:
                return 3*t*(1-t)*(1-t);
                
            case 2:
                return 3*t*t*(1-t);
                
            case 3:
                return t*t*t;
        }
        return 0; //we only get here if an invalid i is specified
    }
    
    //evaluate a point on the B spline
    private static Point p(int i, float t, Point[] curve) {
        float px=0;
        float py=0;
        for (int j = 0; j<=3; j++){
            px += b(j,t)*curve[i+j].x;
            py += b(j,t)*curve[i+j].y;
        }
        return new Point((int)Math.round(px),(int)Math.round(py));
    }
    
    private static int implicitLine(int x0, int y0, int x1, int y1, int x, int y) {
        return x*(y0-y1) + y*(x1-x0) + (x0*y1 - x1*y0);
    }
    
    /**
     * Calculates the instersection point of two segments s0 : p00 - p01 and s1 : p10 - p11.
     * @param p00 the first point of the first segment
     * @param p01 the second point of the first segment
     * @param p10 the first point of the second segment
     * @param p11 the second point of the second segment
     * @return the intersection point, null if there is no intersection
     */
    public static Point segmentIntersection(Point p00, Point p01, Point p10, Point p11) {
        return segmentIntersection(p00.x, p00.y, p01.x, p01.y, p10.x, p10.y, p11.x, p11.y);
    }
    
    /**
     * Calculates the instersection point of two segments s0 : (x0, y0) - (x0d, y0d) and s1 : (x1, y1) - (x1d, y1d).
     * @param x0 x coordinate of the first point of the s0 segment
     * @param y0 y coordinate of the first point of the s0 segment
     * @param x0d x coordinate of the second point of the s0 segment
     * @param y0d y coordinate of the second point of the s0 segment
     * @param x1 x coordinate of the first point of the s1 segment
     * @param y1 y coordinate of the first point of the s1 segment
     * @param x1d x coordinate of the second point of the s1 segment
     * @param y1d y coordinate of the second point of the s1 segment
     * @return the intersection point, null if there is no intersection
     */
    public static Point segmentIntersection(int x0, int y0, int x0d, int y0d, int x1, int y1, int x1d, int y1d) {
        // implicit equation of a line :
        // x(y0-y1) + y(x1-x0) + (x0y1 - x1y0) = 0
        // (x0,y0) - (x0d, y0d) is player trace
        int w = implicitLine(x0, y0, x0d, y0d, x1, y1);
        int x = implicitLine(x0, y0, x0d, y0d, x1d, y1d);
        
        int y = implicitLine(x1, y1, x1d, y1d, x0, y0);
        int z = implicitLine(x1, y1, x1d, y1d, x0d, y0d);
        
        if (((w >= 0 && x <= 0) || (w <= 0 && x >= 0)) &&
            ((y > 0 && z <= 0) || (y < 0 && z >= 0))) return new Point(0,0);
        return null;
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
}
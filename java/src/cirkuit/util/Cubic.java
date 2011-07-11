package cirkuit.util;

/**
 * This class represents a cubic polynomial.<br>
 * http://www.cse.unsw.edu.au/~lambert/splines/source.html
 * @author lambert@cse.unsw.edu.au
 */
public class Cubic {
    /* a + b*t + c*t^2 +d*t^3 */
    float a,b,c,d;         
    
    public Cubic(float a, float b, float c, float d){
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    
    
    /**
     * Evaluate cubic polynomial.
     */
    public float eval(float t) {
        return (((d*t) + c)*t + b)*t + a;
    }
}

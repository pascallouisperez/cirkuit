package cirkuit.ai;

import java.awt.Point;
import java.util.Vector;

import cirkuit.circuit.*;
import cirkuit.util.Curves;

/**
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class CircuitAnalyser {
    private final Circuit circuit;
    
    /**
     * Constructs a new circuit analyser for the specified circuit.
     * @param circuit the circuit to analyse
     */
    public CircuitAnalyser(Circuit circuit) {
        this.circuit = circuit;
    }
    
    /**
     * Computes the optimal path for the circuit to which this analyser is
     * binded.
     */
    public Envelope optimalPath() {
        Envelope innerEnvelope = new Envelope(circuit.getInnerBorder(), Envelope.TYPE_CLOSED_PATH);
        Envelope outerEnvelope = new Envelope(circuit.getOuterBorder(), Envelope.TYPE_CLOSED_PATH);
        if (innerEnvelope.npoints > 0 && outerEnvelope.npoints > 0) {
            Envelope e = new Envelope(Envelope.TYPE_CLOSED_PATH);
            Point p0 = innerEnvelope.getPoint(0), p1 = null, intersection = null;
            int p0_index = 0, i, j;
            e.addPoint(p0);
            for (i = 1; i < innerEnvelope.npoints; i++) {
                // init
                p1 = innerEnvelope.getPoint(i);
                intersection = null;
                // intersection
                for (j = p0_index; intersection == null && j < i; j++)
                    intersection = Curves.segmentIntersection(innerEnvelope.getPoint(j), innerEnvelope.getPoint(j+1), p0, p1);
                // if there was an intersection, we should add the point
                if (intersection != null) {
                    p0_index = i;
                    i += 4;
                    p0 = p1;
                    e.addPoint(p0);
                }
            }
            /*
            // index initialisation
            int outer_start = outerEnvelope.getNearestPointIndex(innerEnvelope.xpoints[0], innerEnvelope.ypoints[0]);
            int outer_order = 0;
            int outer_index = outer_start, inner_index;
            // calculating the outer_order
            for (inner_index = 0; outer_order == 0 && inner_index < innerEnvelope.npoints; inner_index++) {
                outer_index = outerEnvelope.getNearestPointIndex(innerEnvelope.xpoints[inner_index], innerEnvelope.ypoints[inner_index]);
                if (outer_index != outer_start)
                    if (outer_index > outer_start)
                        outer_order = 1;
                    else
                        outer_order = -1;
            }
            for (inner_index = 0; inner_index < innerEnvelope.npoints; inner_index++, outer_index += outer_order) {
                // outer index
                if (outer_index == outerEnvelope.npoints)
                    outer_index = 0;
                if (outer_index < 0)
                    outer_index = outerEnvelope.npoints;
                // testing
                e.addPoint(innerEnvelope.xpoints[inner_index], innerEnvelope.ypoints[inner_index]);
                e.addPoint(outerEnvelope.xpoints[outer_index], outerEnvelope.ypoints[outer_index]);
            }
            int p0_index = 1, j;
            Vector v = new Vector();
            Point p0 = new Point(innerEnvelope.xpoints[0], innerEnvelope.ypoints[0]), p1 = new Point(0, 0), intersection = null;
            boolean innerIntersect = false;
            v.add(p0);
            /*
            for (inner_index = 1;
                 inner_index < innerEnvelope.npoints;
                 inner_index++,
                 outer_index = outerEnvelope.getNearestPointIndex(innerEnvelope.xpoints[inner_index], innerEnvelope.ypoints[inner_index])) {
                // ...
                p1.x = innerEnvelope.xpoints[inner_index];
                p1.y = innerEnvelope.ypoints[inner_index];
                for (j = p0_index; intersection == null && j < inner_index; j++, outer_index += outer_order) {
                    // outer index
                    if (outer_index == outerEnvelope.npoints)
                        outer_index = 0;
                    if (outer_index < 0)
                        outer_index = outerEnvelope.npoints;
                    
                    
                    
                    intersection = Curves.segmentIntersection(new Point(innerEnvelope.xpoints[j], innerEnvelope.ypoints[j])
                                                              new Point(innerEnvelope.xpoints[j+1], innerEnvelope.ypoints[j+1]),
                                                              innerBorder[j+1], p0, p1);
                    if (intersection == null)
                        intersection = Curves.segmentIntersection(innerBorder[j], innerBorder[j+1], p0, p1);
                    else
                        innerIntersect = true;
                }
                if (intersection != null) {
                    intersection = null;
                    p0_index = inner_index;
                    p0 = p1;
                    v.add(p0);
                }
            }
            for (int i = 0; i < v.size(); i++)
                e.addPoint(((Point)v.get(i)).x, ((Point)v.get(i)).y);*/
            return e;
        }
        return null;
    }
}

package cirkuit.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

import cirkuit.circuit.Circuit;

/**
 * This class implements a panel which enables fast previewing of a circuit.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class PreviewPanel extends JPanel {
    /** Height and Width of the panel */
    private int h,w;
    private Circuit circuit;
    
    /**
     * @param h height of the panel
     * @param w width of the panel
     */
    public PreviewPanel(int w, int h) {
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.WHITE);
        setSize(w, h);
        this.h = h;
        this.w = w;
    }
    
    /**
     * Draws a "mini" circuit using actual circuit variable
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (circuit != null) {
            circuit.drawPreview(g, w, h);
        }
    }
    
    public void setCircuit(Circuit circuit) {
        this.circuit = circuit;
    }
}

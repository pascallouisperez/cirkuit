package cirkuit.edit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Color.*;
import cirkuit.circuit.*;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class CirKuitEditPanel extends JPanel {
    private ImageIcon bgImage = null;

    // parent
    private CirKuitEditInternalFrame parent = null;
    
    // circuit properties
    Circuit circuit;

    // Optimal path
    private Envelope optimalPath = null;
    
	public CirKuitEditPanel(CirKuitEditInternalFrame parent, Circuit circuit) {
        this.parent = parent;
        this.circuit = circuit;
        CirKuitEditMouseAdapter mouseManagement = new CirKuitEditMouseAdapter(this, circuit);
        this.addMouseListener((MouseAdapter)mouseManagement);
        this.addMouseMotionListener((MouseMotionListener)mouseManagement);

        // panel properties
        setOpaque(false);
	}

	public void paint(Graphics g) {
		// super call
		super.paint(g);

        // bg image
        if (bgImage != null) {
            int w = bgImage.getIconWidth();
            int h = bgImage.getIconHeight();
            g.drawImage(bgImage.getImage(), (int)((double)(getWidth()-w)/2.0), (int)((double)(getHeight()-h)/2.0), w, h, null);
        }
        
		// drawing the circuit
        circuit.draw(g, true);
        
        // optimal path
        if (optimalPath != null) {
            g.setColor(Color.BLACK);
            optimalPath.draw(g);
        }
    }
    
    public void setOptimalPath(Envelope optimalPath) {
        this.optimalPath = optimalPath;
    }
    
    public void setBackgroundImage(ImageIcon image) {
        this.bgImage = image;
        repaint();
    }
    
    public void unsetBackgroundImage() {
        this.bgImage = null;
        repaint();
    }
    
    public int getMode() {
        return parent.getMode();
    }
}

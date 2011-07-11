package cirkuit.edit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;
import java.awt.Color.*;
import java.util.Vector;

import cirkuit.circuit.*;
import cirkuit.ai.CircuitAnalyser;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class CirKuitEditInternalFrame extends JInternalFrame implements InternalFrameListener {
    /**
     * Normal mode.
     */
    final public static int MODE_NORMAL = 0;
    /**
     * Add nodes on the outer border on every click.
     */
    final public static int MODE_ADD_NODE_OUTER = 1;
    /**
     * Add nodes on the outer border after the last inserted node on every click.
     */
    final public static int MODE_ADD_NODE_OUTER_AFTER = 2;
    /**
     * Add nodes on the inner border on every click.
     */
    final public static int MODE_ADD_NODE_INNER = 3;
    /**
     * Add nodes on the inner border after the last inserted node on every click.
     */
    final public static int MODE_ADD_NODE_INNER_AFTER = 4;
    /**
     * Delete nodes on every click.
     */
    final public static int MODE_DELETE_NODE = 5;
    
    private static int mode = MODE_NORMAL;
    
    // the selected frame
    public static CirKuitEditInternalFrame mainFrame = null;
    
    // storing all the instances
    private Vector allFrames = new Vector();
    
    // container
    private JSplitPane splitPane = null;
    
    // the circuit properties
    private Circuit circuit = new Circuit();
    private String  circuitUrl = null;
    
    // panels
    private CirKuitEditPanel mainPanel = null;
    private JPanel optionsPanel = null;
    private JSpinner speedSpinner = null;
    private JTextField nameField = null;
    
    public CirKuitEditInternalFrame() {
        super("", true, true, true, true);
        
        // updating "all frames" vector
        allFrames.add(this);
        
        // setting frame properties
		setSize(250, 150);
		setResizable(true);
		setTitle(circuit.getName());
		setLocation(0,0);
        addInternalFrameListener(this);
        
        // main panel
        mainPanel = new CirKuitEditPanel(this ,circuit);
        
        // options panel
        optionsPanel = new JPanel(new BorderLayout());
        JPanel tmp, tmp1;
        Dimension size = null;
        optionsPanel.add(tmp = new JPanel(new GridLayout(2,2)), BorderLayout.NORTH);
        tmp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Circuit options"));
        tmp.add(tmp1 = new JPanel(new FlowLayout(FlowLayout.LEFT)));
        tmp1.add(new JLabel("Name :"));
        tmp.add(tmp1 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
        tmp1.add(nameField = new JTextField());
        size = nameField.getPreferredSize();
        size.width = 100;
        nameField.setMaximumSize(size);
        nameField.setMinimumSize(size);
        nameField.setPreferredSize(size);
        tmp.add(tmp1 = new JPanel(new FlowLayout(FlowLayout.LEFT)));
        tmp1.add(new JLabel("Maximum speed :"));
        tmp.add(tmp1 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
        tmp1.add(speedSpinner = new JSpinner(new SpinnerNumberModel(new Integer(circuit.getMaximumSpeed()), new Integer(1), new Integer(10), new Integer(1))));
        size = speedSpinner.getPreferredSize();
        speedSpinner.setMaximumSize(size);
        speedSpinner.setMinimumSize(size);
        speedSpinner.setPreferredSize(size);
        
        // container
        splitPane = new JSplitPane();
        setContentPane(splitPane);
        splitPane.setDividerLocation(getWidth());
        splitPane.setOneTouchExpandable(true);
        splitPane.setBackground(Color.white);
        splitPane.setLeftComponent(mainPanel);
        splitPane.setRightComponent(optionsPanel);
    }
    
    public void setBackgroundImage(ImageIcon image) {
        if (mainPanel != null) {
            mainPanel.setBackgroundImage(image);
        }
    }
    
    public void unsetBackgroundImage() {
        if (mainPanel != null) {
            mainPanel.unsetBackgroundImage();
        }
    }
    
    public void loadCircuit() {
        if (circuitUrl != null) {
            circuit.load(circuitUrl);
            // update buttons
            speedSpinner.setValue(new Integer(circuit.getMaximumSpeed()));
            nameField.setText(circuit.getName());
            // frame resizing
            int width = Math.max(250, circuit.getWidth()+30);
            int height = Math.max(150, circuit.getHeight()+50);
            setSize(width, height);
            splitPane.setDividerLocation(width);
        }
    }
    
    public boolean saveCircuit() {
        if (circuitUrl != null) {
            if (nameField.getText() != null) {
                circuit.setName(nameField.getText());
            }
            if (speedSpinner.getValue() != null) {
                circuit.setMaximumSpeed(((Integer)(speedSpinner.getValue())).intValue());
            }
            return circuit.save(circuitUrl);
        }
        return false;
    }
    
    public void resizeCircuit(double factor) {
        circuit.resize(factor);
        repaint();
    }
    
    public void rotateCircuit(double angle) {
        circuit.rotate(angle);
        repaint();
    }
    
    public void normalizeCircuit() {
        circuit.normalize();
        repaint();
    }
    
    public void reducePrecisionCircuit(float p) {
        circuit.reducePrecision(p);
        repaint();
    }
    
    public void splineCircuit(int p) {
        circuit.spline(p);
        repaint();
    }
    
    public void analyseCircuit() {
        CircuitAnalyser ca = new CircuitAnalyser(circuit);
        mainPanel.setOptimalPath(ca.optimalPath());
        repaint();
    }
    
    public String getUrl() {
        return circuitUrl;
    }
    
    public void setUrl(String url) {
        circuitUrl = url;
    }
    
    /**
     * Set the mode of the internal frames.
     * @param mode the mode
     */
    public void setMode(int mode) {
        this.mode = mode;
    }
    
    /**
     * Get the mode of the internal frames.
     * @return the mode
     */
    public int getMode() {
        return mode;
    }
    
    public String getOpennedFileNames() {
        StringBuffer openedFilesBuffer = new StringBuffer();
        int allFramesSize = allFrames.size();
        CirKuitEditInternalFrame tmp;
        for (int i = 0; i < allFramesSize; i++) {
            if (i > 0) {
                openedFilesBuffer.append("|");
            }
            openedFilesBuffer.append(((CirKuitEditInternalFrame)allFrames.get(i)).getUrl());
        }
        return openedFilesBuffer.toString();
    }
    
    /**
     * Window Listener
     */
    public void internalFrameClosing(InternalFrameEvent e) {
        allFrames.remove(this);
        if (allFrames.size() > 0) {
            mainFrame = (CirKuitEditInternalFrame)allFrames.get(0);
        } else {
            mainFrame = null;
        }
        dispose();
    }
    /** Window Listener */
    public void internalFrameClosed(InternalFrameEvent e) { }
    /** Window Listener */
    public void internalFrameOpened(InternalFrameEvent e) { }
    /** Window Listener */
    public void internalFrameIconified(InternalFrameEvent e) { }
    /** Window Listener */
    public void internalFrameDeiconified(InternalFrameEvent e) { }
    /** Window Listener */
    public void internalFrameActivated(InternalFrameEvent e) {
        mainFrame = this;
    }
    /** Window Listener */
    public void internalFrameDeactivated(InternalFrameEvent e) { }
}

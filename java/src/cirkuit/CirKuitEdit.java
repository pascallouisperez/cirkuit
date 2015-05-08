package cirkuit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Color.*;
import java.util.Vector;
import java.io.File;
import java.net.URL;

import cirkuit.edit.*;
import cirkuit.util.AboutDialog;
import cirkuit.util.Configuration;
import cirkuit.properties.*;
import cirkuit.util.SplashScreen;

/**
 * This class defines the main frame for the circuit editor developped for the
 * CirKuit 2D game.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 2.0
 */
public class CirKuitEdit extends JFrame implements WindowListener, ActionListener {
    private final static String VERSION = "1.5";
    
    // properties
    private Configuration config = new Configuration(CirKuit.CONFIGURATION_FILE);
    
    // frame properties
    private int posX   = 0;
    private int posY   = 0;
    private int width  = 400;
    private int height = 400;
    private String title = "CirKuit2D - circuit editor";
    
    private JDesktopPane desktopPane = new JDesktopPane();
    private JMenuBar menubar = new JMenuBar();
    JFileChooser fileChooser = new JFileChooser();
    
    private boolean wasLaunchedAsSubProgram = false;
    
    // internal frames
    //Vector internalFrames = new Vector();
    
    public CirKuitEdit() {
        this(false);
    }
    
    public CirKuitEdit(String fileName) {
        this(true);
        openCircuit(fileName, 0, 0);
        wasLaunchedAsSubProgram = true;
    }
    
	public CirKuitEdit(boolean wasLaunchedAsSubProgram) {
        SplashScreen splash = null;
        if (!wasLaunchedAsSubProgram)
            splash = new SplashScreen();
        
        // properties
        if ((new File(CirKuit.CONFIGURATION_FILE)).exists()) config.read();

        // setting frame properties
        if (config.get("editor_maximised") != null && config.get("editor_maximised").equals("true")) {
            posX   = 0;
            posY   = 0;
            width  = 800;
            height = 600;
            /*
            Dimension bounds = getMaximumSize();
            posX   = 0;
            posY   = 0;
            width  = bounds.width-50;
            height = bounds.height-50;*/
            //setExtendedState(JFrame.MAXIMIZED_BOTH);
            //setMaximised(true);
        } else {
            try {
                posX   = Integer.parseInt(config.get("editor_pos_x"));
                posY   = Integer.parseInt(config.get("editor_pos_y"));
                width  = Integer.parseInt(config.get("editor_width"));
                height = Integer.parseInt(config.get("editor_height"));
            } catch (Exception e) {
                posX   = 0;
                posY   = 0;
                width  = 400;
                height = 400;
            }
        }
        setLocation(posX, posY);
        setSize(width,height);
		setResizable(true);
		setTitle(title);
        setIconImage((new ImageIcon("./shared/image/icon.gif")).getImage());
        addWindowListener(this);
        getContentPane().add(desktopPane, BorderLayout.CENTER);
        
        if (splash != null)
            splash.advance();
        
        // menu
        JMenu     menu;
        JMenu     menuTmp, menuTmp0;
        JMenuItem menuItem;
        
        menu = new JMenu("File");
        
        menuItem = new JMenuItem("New");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { newCircuit(); }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke('N', Event.CTRL_MASK));
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Open");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { openCircuit(); }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { saveCircuit(); }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK));
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save As...");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { saveAsCircuit(); }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke("F12"));
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menu.add(menuItem = new JMenuItem("Exit"));
        menuItem.setAccelerator(KeyStroke.getKeyStroke('X', Event.CTRL_MASK));
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { exit(); }});
        menubar.add(menu);
        
        menubar.add(menu);
        
        menu = new JMenu("View");
        
        menuItem = new JMenuItem("Set background imgage");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { setBackgroundImage(); }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke('B', Event.CTRL_MASK));
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Unset background imgage");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { unsetBackgroundImage(); }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke('U', Event.CTRL_MASK));
        menu.add(menuItem);
        
        menubar.add(menu);
        
        menu = new JMenu("Edit");
        
        menuTmp0 = new JMenu("Resize");
        menuItem = new JMenuItem("50%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { resizeCircuit(0.5); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("75%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { resizeCircuit(0.75); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("150%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { resizeCircuit(1.5); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("200%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { resizeCircuit(2); }});
        menuTmp0.add(menuItem);
        menu.add(menuTmp0);
        
        menuTmp0 = new JMenu("Rotate");
        menuItem = new JMenuItem("30\u0176");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { rotateCircuit(Math.PI/6); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("90\u0176");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { rotateCircuit(Math.PI/2); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("180\u0176");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { rotateCircuit(Math.PI); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("270\u0176");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { rotateCircuit(3*Math.PI/2); }});
        menuTmp0.add(menuItem);
        menu.add(menuTmp0);
        
        menuItem = new JMenuItem("Normalize");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { normalizeCircuit(); }});
        menu.add(menuItem);
        
        menuTmp0 = new JMenu("Reduce precision");
        menuItem = new JMenuItem("40%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { reducePrecisionCircuit((float)0.4); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("70%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { reducePrecisionCircuit((float)0.7); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("75%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { reducePrecisionCircuit((float)0.75); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("80%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { reducePrecisionCircuit((float)0.8); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("85%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { reducePrecisionCircuit((float)0.85); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("90%");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { reducePrecisionCircuit((float)0.9); }});
        menuTmp0.add(menuItem);
        menu.add(menuTmp0);
        
        menuTmp0 = new JMenu("Spline interpolation");
        menuItem = new JMenuItem("5");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { splineCircuit(5); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("10");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { splineCircuit(10); }});
        menuTmp0.add(menuItem);
        menuItem = new JMenuItem("15");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { splineCircuit(15); }});
        menuTmp0.add(menuItem);
        menu.add(menuTmp0);
        
        menuItem = new JMenuItem("Analyse");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { analyseCircuit(); }});
        menu.add(menuItem);
        
        menubar.add(menu);
        
        menu = new JMenu("Help");
        
        menuItem = new JMenuItem("About");
        menuItem.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { about(); }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke('A', Event.CTRL_MASK));
        menu.add(menuItem);
        
        menubar.add(menu);
        
        setJMenuBar(menubar);
        
        if (splash != null)
            splash.advance();
        
        // toolbar
        JToolBar toolBar = new JToolBar("Main toolbar");
        toolBar.setFloatable(false);
        JToggleButton normal = makeToolbarButton("normal_16x16", CirKuitEditInternalFrame.MODE_NORMAL+"", "Normal mode", "normal mode");
        JToggleButton addNodeOuter      = makeToolbarButton("addNodeOuter_16x16", CirKuitEditInternalFrame.MODE_ADD_NODE_OUTER+"", "Add a node on the outer border (or SHIFT + click)", "SHIFT + click");
        JToggleButton addNodeOuterAfter = makeToolbarButton("addNodeOuterAfter_16x16", CirKuitEditInternalFrame.MODE_ADD_NODE_OUTER_AFTER+"", "Add a node on the outer border after the last inserted node (or SHIFT + ALT + click)", "SHIFT + ALT + click");
        JToggleButton addNodeInner      = makeToolbarButton("addNodeInner_16x16", CirKuitEditInternalFrame.MODE_ADD_NODE_INNER+"", "Add a node on the inner border (or CTRL + click)", "CTRL + click");
        JToggleButton addNodeInnerAfter = makeToolbarButton("addNodeInnerAfter_16x16", CirKuitEditInternalFrame.MODE_ADD_NODE_INNER_AFTER+"", "Add a node on the inner border after the last inserted node (or CTRL + ALT + click)", "CTRL + ALT + click");
        JToggleButton deleteNode        = makeToolbarButton("deleteNode_16x16", CirKuitEditInternalFrame.MODE_DELETE_NODE+"", "Delete a node (or 2 x click)", "2 x click");
        ButtonGroup group = new ButtonGroup();
        group.add(normal);
        group.add(addNodeOuter);
        group.add(addNodeOuterAfter);
        group.add(addNodeInner);
        group.add(addNodeInnerAfter);
        group.add(deleteNode);
        toolBar.add(normal);
        toolBar.add(addNodeOuter);
        toolBar.add(addNodeOuterAfter);
        toolBar.add(addNodeInner);
        toolBar.add(addNodeInnerAfter);
        toolBar.add(deleteNode);
        getContentPane().add(toolBar, BorderLayout.PAGE_START);
        
        if (splash != null)
            splash.advance();
        
        // opening circuits
        String openedFilesPaths = config.get("editor_circuits_paths");
        if (openedFilesPaths != null && !openedFilesPaths.equals("")) {
            int pos = 0;
            String[] openedFiles = openedFilesPaths.split("\\|");
            for (int i = 0; i < openedFiles.length; i++) {
                if (!openedFiles[i].equals("null")) {
                    openCircuit(openedFiles[i], pos, pos);
                    pos += 20;
                }
            }
        }
        
        if (splash != null) {
            splash.advance();
            splash.dispose();
        }
	}
    
    private void newCircuit() {
       CirKuitEditInternalFrame frame = new CirKuitEditInternalFrame();
       frame.setVisible(true);
       desktopPane.add(frame);
       try {
           frame.setSelected(true);
       } catch (Exception e) {}
    }
    
    private void openCircuit() {
        int fileChooserVal = fileChooser.showOpenDialog(this);
        if (fileChooserVal == JFileChooser.APPROVE_OPTION) {
           CirKuitEditInternalFrame frame = new CirKuitEditInternalFrame();
           frame.setUrl(fileChooser.getSelectedFile().getPath());
           frame.loadCircuit();
           frame.setVisible(true);
           desktopPane.add(frame);
           try {
               frame.setSelected(true);
           } catch (Exception e) {}
        }
    }
    
    private void openCircuit(String url, int x, int y) {
       if (url != null)  {
           CirKuitEditInternalFrame frame = new CirKuitEditInternalFrame();
           frame.setLocation(x, y);
           frame.setUrl(url);
           frame.loadCircuit();
           frame.setVisible(true);
           desktopPane.add(frame);
           try {
               frame.setSelected(true);
           } catch (Exception e) {}
       }
    }
    
    private void saveCircuit() {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            if (!CirKuitEditInternalFrame.mainFrame.saveCircuit()) {
                saveAsCircuit();
            }
        }
    }
    
    private void saveAsCircuit() {
        int fileChooserVal = fileChooser.showSaveDialog(this);
        if (fileChooserVal == JFileChooser.APPROVE_OPTION) {
            CirKuitEditInternalFrame.mainFrame.setUrl(fileChooser.getSelectedFile().getPath());
            CirKuitEditInternalFrame.mainFrame.saveCircuit();
        }
    }
    
    private void setMode(int mode) {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            CirKuitEditInternalFrame.mainFrame.setMode(mode);
        }
    }
    
    private void setBackgroundImage() {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            int fileChooserVal = fileChooser.showOpenDialog(this);
            if (fileChooserVal == JFileChooser.APPROVE_OPTION) {
                CirKuitEditInternalFrame.mainFrame.setBackgroundImage(new ImageIcon(fileChooser.getSelectedFile().getPath()));
            }
        }
    }
    
    private void unsetBackgroundImage() {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            CirKuitEditInternalFrame.mainFrame.unsetBackgroundImage();
        }
    }
    
    private void resizeCircuit(double factor) {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            CirKuitEditInternalFrame.mainFrame.resizeCircuit(factor);
        }
    }
    
    private void rotateCircuit(double angle) {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            CirKuitEditInternalFrame.mainFrame.rotateCircuit(angle);
        }
    }
    
    private void normalizeCircuit() {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            CirKuitEditInternalFrame.mainFrame.normalizeCircuit();
        }
    }
    
    private void reducePrecisionCircuit(float p) {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            CirKuitEditInternalFrame.mainFrame.reducePrecisionCircuit(p);
        }
    }
    
    private void splineCircuit(int p) {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            CirKuitEditInternalFrame.mainFrame.splineCircuit(p);
        }
    }
    
    private void analyseCircuit() {
        if (CirKuitEditInternalFrame.mainFrame != null) {
            CirKuitEditInternalFrame.mainFrame.analyseCircuit();
        }
    }
    
    private void about() {
        (new AboutDialog(this, "./shared/image/logo.gif", "CirKuit 2D - Editor " + VERSION, "Sven Gowal - Pascal Perez", "svenadrian.gowal@epfl.ch - pascal.perez@epfl.ch")).show();
    }
    
    private void exit() {
        // opened files in internal frames
        if (CirKuitEditInternalFrame.mainFrame != null) {
            config.set("editor_circuits_paths", CirKuitEditInternalFrame.mainFrame.getOpennedFileNames());
        } else {
            config.set("editor_circuits_paths", "");
        }
        // window position and size
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
            config.set("editor_maximised","true");
        } else {
            config.set("editor_maximised","false");
            config.set("editor_pos_x", getLocation().x+"");
            config.set("editor_pos_y", getLocation().y+"");
            config.set("editor_width", getSize().width+"");
            config.set("editor_height", getSize().height+"");
        }
        config.write();
        dispose();
        if (!wasLaunchedAsSubProgram)
            System.exit(0);
    }
    
    public static void main(String args[]) {
        // Windows look and feel
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			// it's not a big deal...
		}
		
        CirKuitEdit frame = new CirKuitEdit();
        frame.show();
    }
    
    /**
     * Window Listener
     */
    public void windowClosing(WindowEvent e) {
        exit();
    }
    public void windowClosed(WindowEvent e) { }
    public void windowOpened(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowActivated(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    
    /**
     * Action listener
     */
    public void actionPerformed(ActionEvent e) {
        try {
            setMode(Integer.parseInt(e.getActionCommand()));
        } catch (Exception ex) {}
    }
    
    /**
     * 
     */
    private JToggleButton makeToolbarButton(String imageName, String actionCommand, String toolTipText, String altText) {
        // look for the image
        String imageLocation = "./shared/image/toolbar/"+ imageName + ".gif";

        // create and initialize the button.
        Dimension dim = new Dimension(24, 24);
        JToggleButton button = new JToggleButton();
        button.setBackground(Color.white);
        button.setActionCommand(actionCommand);
        button.setPreferredSize(dim);
        button.setMaximumSize(dim);
        button.setMinimumSize(dim);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        button.setIcon(new ImageIcon("./shared/image/toolbar/"+ imageName + ".gif", altText));

        return button;
    }
}

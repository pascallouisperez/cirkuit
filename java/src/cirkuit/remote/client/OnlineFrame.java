package cirkuit.remote.client;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.io.IOException;

import cirkuit.CirKuit;
import cirkuit.circuit.Circuit;
import cirkuit.util.AboutDialog;
import cirkuit.util.AttachedFrame;
import cirkuit.util.GUIActions;
import cirkuit.util.HelpDialog;
import cirkuit.util.PreviewPanel;
import cirkuit.remote.*;
import cirkuit.properties.Properties;
import cirkuit.texts.GUITexts;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class OnlineFrame extends AttachedFrame implements WindowListener, SocketListener, ActionListener {
    private int oldState;
    
    private ServerSocketAnalyser socketAnalyser = null;
    private Vector linkedFrame = null;
    private String[] serverCircuitList = new String[0];
    
    // Interfaces' elements
    private JComboBox availGamesCB = null;
    private JButton joinButton = new JButton(GUITexts.BUTTON_JOIN);
    private JTabbedPane jtp = new JTabbedPane();
    private GameModifier gameCreatorPanel = new GameModifier(this);
    private GameModifier gameModifierPanel = null;
    private GameInfoPanel gameInfoPanel = new GameInfoPanel();
    
    public OnlineFrame(Window owner, ServerSocketAnalyser socketAnalyser) {
        this(owner, socketAnalyser, new Vector());   
    }
    
    /**
     * @param linkedFrame A Vector of Windows which will be closed by OnlineFrame.
     */
    public OnlineFrame(Window owner, ServerSocketAnalyser socketAnalyser, Vector linkedFrame) {
        // super call
        super(owner);
        // socket instanciation
        this.socketAnalyser = socketAnalyser;
        socketAnalyser.addSocketListener(this);
        oldState = socketAnalyser.getSocketState();
        // linked frames
        this.linkedFrame = linkedFrame;
        
        // window
        setTitle(GUITexts.TITLE_CIRKUITONLINE);   
        setSize(300,500);
        setIconImage((new ImageIcon("./shared/image/icon.gif")).getImage());
        addWindowListener(this);
        
        // some temporaries pointers...
        JPanel panel, tmp01, tmp02, tmp03;
        JButton button;
        Dimension size;
        
        // content
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());
        
        // "Join a game" panel
        panel = new JPanel(new BorderLayout());
        jtp.add(panel, GUITexts.TITLE_JOINAGAME, 0);
        panel.add(tmp01 = new JPanel(new GridLayout(1,2)), BorderLayout.NORTH);
        panel.add(gameInfoPanel, BorderLayout.CENTER);
        tmp01.add(new JLabel(GUITexts.INFO_AVAILGAMES+GUITexts.MISC_ENDING));
        tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
        tmp02.add(availGamesCB = new JComboBox());
        availGamesCB.setActionCommand(GUIActions.CB_AVAILGAMES+"");
        availGamesCB.addActionListener(this);
        tmp01 = new JPanel(new BorderLayout());
        tmp01.add(tmp02 = new JPanel(new GridLayout(1, 2)), BorderLayout.EAST);
        tmp02.add(tmp03 = new JPanel(new FlowLayout()));
        tmp03.add(button = new JButton(GUITexts.BUTTON_REFRESH));
        button.addActionListener(this);
        button.setActionCommand(GUIActions.BUTTON_REFRESH+"");
        tmp02.add(tmp03 = new JPanel(new FlowLayout()));
        tmp03.add(joinButton);
        joinButton.addActionListener(this);
        joinButton.setActionCommand(GUIActions.BUTTON_JOIN+"");
        joinButton.setEnabled(false);
        panel.add(tmp01, BorderLayout.SOUTH);
        
        // "Create a game" panel
        jtp.add(gameCreatorPanel, GUITexts.TITLE_CREATEAGAME, 1);
        
        // buttons on the bottom
        tmp02 = new JPanel(new BorderLayout());
        panel = new JPanel(new GridLayout(1,1));
        tmp02.add(panel, BorderLayout.EAST);
        panel.add(tmp01 = new JPanel(new FlowLayout()));
        tmp01.add(button = new JButton(GUITexts.BUTTON_CLOSE));
        button.addActionListener(this);
        button.setActionCommand(GUIActions.BUTTON_CLOSE+"");
        
        content.add(jtp, BorderLayout.CENTER);
        content.add(tmp02, BorderLayout.SOUTH);
        show();
    }
    
    /**
     * Socket Listener.
     */
    public void dataArrived(String command) {
        try {
            if (command.equals("COMC")) {
                String gameName;
                switch (socketAnalyser.getSocketState()) {
                    case SocketAnalyser.STATE_WELC:
                        socketAnalyser.setSocketState(SocketAnalyser.STATE_INIT01);
                        socketAnalyser.reply("GLID");
                        break;
                        
                    case SocketAnalyser.STATE_WAITINGFORJOIN:
                        gameName = (String)(availGamesCB.getSelectedItem());
                        if (gameName != null) {
                            socketAnalyser.setSocketState(SocketAnalyser.STATE_WAITING);
                            jtp.add(new JPanel(), gameName, 2);
                            jtp.setSelectedIndex(2);
                            jtp.setEnabledAt(0, false);
                            jtp.setEnabledAt(1, false);
                        }
                        break;
                        
                    case SocketAnalyser.STATE_WAITINGFORMAKE:
                        OnlineProperties properties = gameCreatorPanel.getOnlineProperties();
                        socketAnalyser.setSocketState(SocketAnalyser.STATE_CSELBEFOREMAKE);
                        gameName = socketAnalyser.getNextString();
                        gameModifierPanel = new GameModifier(this, gameName, properties, serverCircuitList);
                        jtp.add(gameModifierPanel, gameName, 2);
                        jtp.setSelectedIndex(2);
                        jtp.setEnabledAt(0, false);
                        jtp.setEnabledAt(1, false);
                        socketAnalyser.reply("CSEL "+socketAnalyser.stringEncode(properties.getCircuitName())+","
                                                    +properties.getMode()+","
                                                    +properties.getPlusSpeed()+","
                                                    +properties.getMinusSpeed()+","
                                                    +properties.getMaxAngle()+","
                                                    +properties.getGrid()+","
                                                    +properties.getTurn()+","
                                                    +properties.getCrashRadius()+","
                                                    +properties.getTrace()+","
                                                    +socketAnalyser.booleanEncode(properties.getIsCrash())+","
                                                    +socketAnalyser.booleanEncode(properties.getRedDots())+","
                                                    +properties.getMaxPlayer());
                        break;
                        
                    case SocketAnalyser.STATE_CSELBEFOREMAKE:
                        socketAnalyser.setSocketState(SocketAnalyser.STATE_MAKE);
                        break;
                        
                    case SocketAnalyser.STATE_WAITINGFORQTGM:
                        //
                        break;
                        
                    case SocketAnalyser.STATE_WAITINGFORCSEL:
                        socketAnalyser.setSocketState(SocketAnalyser.STATE_MAKE);
                        System.out.println("You modified the game");
                        break;
                }
            } else if (command.equals("GLIS")) {
                int state = socketAnalyser.getSocketState();
                switch (state) {
                    case SocketAnalyser.STATE_INIT01:
                        socketAnalyser.setSocketState(SocketAnalyser.STATE_INIT02);
                    default:
                        // emptying the combo box
                        availGamesCB.removeAllItems();
                        // adding the first value
                        availGamesCB.addItem("select a game");
                        // adding new values
                        String buffer = socketAnalyser.getNextString();
                        while(buffer != null) {
                            availGamesCB.addItem(buffer);
                            buffer = socketAnalyser.getNextString();
                        }
                        // unbinding the gameInfoPanel
                        gameInfoPanel.unbind();
                        // unabling the join button
                        joinButton.setEnabled(false);
                        // repainting
                        this.repaint();
                        if (state == SocketAnalyser.STATE_INIT01) {
                            try {
                                socketAnalyser.reply("CLID");
                            } catch(Exception ex) {};
                        }
                        break;
                }
            } else if (command.equals("CLIS") && socketAnalyser.getSocketState() == SocketAnalyser.STATE_INIT02) {
                Vector circuitsVector = new Vector();
                String c = socketAnalyser.getNextString();
                while (c != null) {
                    circuitsVector.add(c);
                    c = socketAnalyser.getNextString();
                }
                serverCircuitList = new String[circuitsVector.size()];
                for (int i=0; i<serverCircuitList.length; i++) {
                    serverCircuitList[i] = (String)circuitsVector.get(i);
                }
                socketAnalyser.setSocketState(SocketAnalyser.STATE_NORMAL);
            } else if (command.equals("KICK")) {
                gameModifierPanel = null;
                jtp.remove(2);
                jtp.setEnabledAt(0, true);
                jtp.setEnabledAt(1, true);
                jtp.setSelectedIndex(0);
                socketAnalyser.setSocketState(SocketAnalyser.STATE_NORMAL);
            } else if (command.equals("EROR")) {
                switch (socketAnalyser.getSocketState()) {
                    case SocketAnalyser.STATE_WAITINGFORJOIN:
                        socketAnalyser.setSocketState(oldState);
                        System.out.println("Unable to join");
                        break;
                        
                    case SocketAnalyser.STATE_WAITINGFORMAKE:
                        socketAnalyser.setSocketState(oldState);
                        System.out.println("Unable to create");
                        break;
                        
                    case SocketAnalyser.STATE_WAITINGFORQTGM:
                        socketAnalyser.setSocketState(oldState);
                        System.out.println("Unable to quit");
                        break;
                        
                    case SocketAnalyser.STATE_WAITINGFORCSEL:
                        socketAnalyser.setSocketState(oldState);
                        System.out.println("Unable to modify the game");
                        break;
                        
                    case SocketAnalyser.STATE_WAITINGFORCLIS:
                        socketAnalyser.setSocketState(oldState);
                        System.out.println("Unable to modify the game");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Action Listener.
     */
    public void actionPerformed(ActionEvent e) {
        try {
            int action = Integer.parseInt(e.getActionCommand());
            switch (action) {
                case GUIActions.BUTTON_CLOSE:
                    socketAnalyser.setRun(false);
                    windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSED));
                    break;
                    
                case GUIActions.BUTTON_CREATEGAME:
                    try {
                        if (gameCreatorPanel.getGameName() != null) {
                            socketAnalyser.setSocketState(SocketAnalyser.STATE_WAITINGFORMAKE);
                            socketAnalyser.reply("MAKE "+socketAnalyser.stringEncode(gameCreatorPanel.getGameName()));
                        }
                    } catch (IOException ex) { socketAnalyser.setSocketState(oldState); }
                    break;
                    
                case GUIActions.BUTTON_DESTROYGAME:
                    try {
                        socketAnalyser.setSocketState(SocketAnalyser.STATE_WAITINGFORQTGM);
                        socketAnalyser.reply("QTGM");
                    } catch (IOException ex) { socketAnalyser.setSocketState(oldState); }
                    break;
                    
                case GUIActions.BUTTON_JOIN:
                    String gameName = (String)(availGamesCB.getSelectedItem());
                    if (gameName != null) {
                        try {
                            socketAnalyser.setSocketState(socketAnalyser.STATE_WAITINGFORJOIN);
                            socketAnalyser.reply("JOIN "+socketAnalyser.stringEncode(gameName));
                        } catch (Exception ex) { socketAnalyser.setSocketState(oldState); }
                    }
                    break;
                    
                case GUIActions.BUTTON_MODIFYGAME:
                    try {
                        OnlineProperties properties = null;
                        if (gameModifierPanel != null)
                            properties = gameModifierPanel.getOnlineProperties();
                        else
                            properties = gameCreatorPanel.getOnlineProperties();
                        socketAnalyser.reply("CSEL "+socketAnalyser.stringEncode(properties.getCircuitName())+","
                                                    +properties.getMode()+","
                                                    +properties.getPlusSpeed()+","
                                                    +properties.getMinusSpeed()+","
                                                    +properties.getMaxAngle()+","
                                                    +properties.getGrid()+","
                                                    +properties.getTurn()+","
                                                    +properties.getCrashRadius()+","
                                                    +properties.getTrace()+","
                                                    +socketAnalyser.booleanEncode(properties.getIsCrash())+","
                                                    +socketAnalyser.booleanEncode(properties.getRedDots())+","
                                                    +properties.getMaxPlayer());
                    } catch (Exception ex) { socketAnalyser.setSocketState(oldState); }
                    break;
                    
                case GUIActions.BUTTON_REFRESH:
                    try {
                        socketAnalyser.reply("GLID");
                    } catch (IOException ex) { }
                    break;
                    
                case GUIActions.BUTTON_STARTGAME:
                    try {
                        socketAnalyser.reply("STRT");
                    } catch (IOException ex) { }
                    break;
                    
                case GUIActions.CB_AVAILGAMES:
                    if (availGamesCB.getSelectedIndex() > 0) {
                        gameInfoPanel.bind((String)(availGamesCB.getSelectedItem()));
                        joinButton.setEnabled(true);
                        this.repaint();
                    }
                    break;
            }
        } catch(Exception err) {}
    }
    
    /** Window Listener */
    public void windowClosing(WindowEvent e) {
        try {
            socketAnalyser.reply("QUIT");
        } catch (IOException ex) { }
        socketAnalyser.removeSocketListener(this);
        Window win;
        for (int i=0; i<linkedFrame.size(); i++) {
            win = (Window)linkedFrame.get(i);
            if (win instanceof SocketListener) {
                socketAnalyser.removeSocketListener((SocketListener)win);
            }
            win.dispose();
        }
        dispose();
    }
    /** Window Listener */
    public void windowClosed(WindowEvent e) {
    }
    /** Window Listener */
    public void windowOpened(WindowEvent e) { }
    /** Window Listener */
    public void windowIconified(WindowEvent e) { }
    /** Window Listener */
    public void windowDeiconified(WindowEvent e) { }
    /** Window Listener */
    public void windowActivated(WindowEvent e) { }
    /** Window Listener */
    public void windowDeactivated(WindowEvent e) { }
    
    /**
     * GameModifier : shows forms to modify or create a game.
     */
    private class GameModifier extends JPanel {
        private boolean binded = false;
        private String gameName = null;
        private OnlineProperties properties = null;
        
        private JTextField gameNameTF = null;
        private JSpinner plusSpeedSpinner, minusSpeedSpinner, maxAngleSpinner, gridSpinner, turnSpinner;
        private boolean isRallyModeSelectedLast;
        private JCheckBox isRallyMode;
        private JComboBox circuitListCB;
        
        /**
         * Create a panel wich is not binded to a game.
         * @param al action listener which should handle this panel's actions
         */
        GameModifier(ActionListener al) {
            //this(al, null, new OnlineProperties(), circuitList);
            this(al, null, new OnlineProperties(), null);
        }
        
        /**
         * Create a panel wich is binded to a game.
         * @param gn game name, if null, it wont be binded.
         * @param al action listener which should handle this panel's actions
         */
        GameModifier(ActionListener al, String gn, OnlineProperties properties, String[] circuitList) {
            if (gn != null) {
                binded = true;
            }
            gameName = gn;
            
            JPanel tmp01, tmp02, tmp03;
            JButton button;
            
            // - top part
            this.setLayout(new BorderLayout());
            if (!binded) {
                this.add(tmp01 = new JPanel(new BorderLayout()), BorderLayout.NORTH);
                tmp01.add(new JLabel(GUITexts.INFO_GAMENAME+GUITexts.MISC_ENDING), BorderLayout.WEST);
                tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.CENTER)), BorderLayout.EAST);
                tmp02.add(gameNameTF = new JTextField(15));
            }
            // - center part
            this.add(tmp01 = new JPanel(new GridLayout(7,2)), BorderLayout.CENTER);
            tmp01.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), GUITexts.TITLE_GAMEOPTIONS));
            if (circuitList != null) {
                tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.LEFT)));
                tmp02.add(new JLabel(GUITexts.INFO_CIRCUITFILEPATH+GUITexts.MISC_ENDING));
                tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
                tmp02.add(circuitListCB = new JComboBox(circuitList));
            }
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.LEFT)));
            tmp02.add(new JLabel(GUITexts.INFO_NLAPS+GUITexts.MISC_ENDING));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
            tmp02.add(turnSpinner = new JSpinner(new SpinnerNumberModel(new Integer(properties.getTurn()),new Integer(1),new Integer(10),new Integer(1))));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.LEFT)));
            tmp02.add(new JLabel(GUITexts.INFO_HIGHESTACCELERATION+GUITexts.MISC_ENDING));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
            tmp02.add(plusSpeedSpinner = new JSpinner(new SpinnerNumberModel(new Integer(properties.getMinusSpeed()),new Integer(1),new Integer(10),new Integer(1))));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.LEFT)));
            tmp02.add(new JLabel(GUITexts.INFO_HIGHESTDECCELERATION+GUITexts.MISC_ENDING));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
            tmp02.add(minusSpeedSpinner = new JSpinner(new SpinnerNumberModel(new Integer(properties.getMinusSpeed()),new Integer(1),new Integer(10),new Integer(1))));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.LEFT)));
            tmp02.add(new JLabel(GUITexts.INFO_HIGHESTTURNINGANGLE+GUITexts.MISC_ENDING));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
            tmp02.add(maxAngleSpinner = new JSpinner(new SpinnerNumberModel(new Integer((int)Math.toDegrees(properties.getMaxAngle())), new Integer(10), new Integer(120), new Integer(1))));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.LEFT)));
            tmp02.add(new JLabel(GUITexts.INFO_GRIDSIZE+GUITexts.MISC_ENDING));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
            tmp02.add(gridSpinner = new JSpinner(new SpinnerNumberModel(new Integer(properties.getGrid()),new Integer(5),new Integer(30),new Integer(1))));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.LEFT)));
            tmp02.add(new JLabel(GUITexts.INFO_RALLYMODE+GUITexts.MISC_ENDING));
            tmp01.add(tmp02 = new JPanel(new FlowLayout(FlowLayout.CENTER)));
            tmp02.add(isRallyMode = new JCheckBox("", properties.getIsRallyMode()));
            isRallyModeSelectedLast = properties.getIsRallyMode();
            isRallyMode.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (isRallyMode.isSelected() != isRallyModeSelectedLast) {
                        isRallyModeSelectedLast = isRallyMode.isSelected();
                        int i = ((Integer)maxAngleSpinner.getValue()).intValue();
                        if(isRallyMode.isSelected()) {
                            maxAngleSpinner.setValue(new Integer(Math.min(i << 1, ((Integer)(((SpinnerNumberModel)maxAngleSpinner.getModel()).getMaximum())).intValue()))); // x*2
                        } else {
                            maxAngleSpinner.setValue(new Integer(Math.max(i >> 1, ((Integer)(((SpinnerNumberModel)maxAngleSpinner.getModel()).getMinimum())).intValue()))); // x/2
                        }
                    }
                }
            });
            // - bottom part
            if (binded) {
                tmp01 = new JPanel(new BorderLayout());
                tmp01.add(tmp02 = new JPanel(new FlowLayout()), BorderLayout.WEST);
                tmp02.add(button = new JButton(GUITexts.BUTTON_DESTROYGAME));
                button.addActionListener(al);
                button.setActionCommand(GUIActions.BUTTON_DESTROYGAME+"");
                tmp01.add(tmp02 = new JPanel(new GridLayout(1, 2)), BorderLayout.EAST);
                tmp02.add(tmp03 = new JPanel(new FlowLayout()));
                tmp03.add(button = new JButton(GUITexts.BUTTON_MODIFYGAME));
                button.addActionListener(al);
                button.setActionCommand(GUIActions.BUTTON_MODIFYGAME+"");
                tmp02.add(tmp03 = new JPanel(new FlowLayout()));
                tmp03.add(button = new JButton(GUITexts.BUTTON_STARTGAME));
                button.addActionListener(al);
                button.setActionCommand(GUIActions.BUTTON_STARTGAME+"");
            } else {
                tmp01 = new JPanel(new BorderLayout());
                tmp01.add(tmp02 = new JPanel(new GridLayout(1, 1)), BorderLayout.EAST);
                tmp02.add(tmp03 = new JPanel(new FlowLayout()));
                tmp03.add(button = new JButton(GUITexts.BUTTON_CREATEGAME));
                button.addActionListener(al);
                button.setActionCommand(GUIActions.BUTTON_CREATEGAME+"");
            }
            this.add(tmp01, BorderLayout.SOUTH);
        }
        
        /**
         * @return this panel's game name. The one contained in a text box (if unbinded) or the
         * one given when the panel was binded.
         */
        public String getGameName() {
            if (binded)
                return gameName;
            else
                return gameNameTF.getText();
        }
        
        /**
         * @return the properties selcted by the user via this panel.
         */
        public OnlineProperties getOnlineProperties() {
            OnlineProperties p = new OnlineProperties();
            if (circuitListCB != null)
                p.setCircuitName((String)circuitListCB.getSelectedItem());
            p.setGrid(((Integer)gridSpinner.getValue()).intValue());
            p.setIsRallyMode(isRallyMode.isSelected());
            p.setMaxAngle(Math.toRadians((double)((Integer)maxAngleSpinner.getValue()).intValue()));
            p.setMinusSpeed(((Integer)minusSpeedSpinner.getValue()).intValue());
            p.setPlusSpeed(((Integer)plusSpeedSpinner.getValue()).intValue());
            p.setTurn(((Integer)turnSpinner.getValue()).intValue());
            return p;
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
        }
    }
    
    /**
     * GameInfoPanel : shows the important informations about a game.
     */
    private class GameInfoPanel extends JPanel {
        private String gameName = null;
        private PreviewPanel previewPanel = new PreviewPanel(200, 100);
        private Properties properties = null;
        
        /**
         * Create a panel wich is not binded to a game.
         */
        GameInfoPanel() {
            // container
            this.setLayout(new BorderLayout());
            
            // preview panel
            JPanel tmp1;
            this.add(tmp1 = new JPanel(new FlowLayout(FlowLayout.CENTER)), BorderLayout.NORTH);
            tmp1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), GUITexts.TITLE_PREVIEW));
            tmp1.setBackground(Color.WHITE);
            tmp1.add(previewPanel);
            
            // unbinding
            unbind();
        }
        
        /**
         * Bind to a game.
         * @param gn game name
         */
        void bind(String gn) {
            if (gn != null) {
                this.gameName = gn;
                this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), gameName));
                properties = socketAnalyser.getProperties(gameName);
                previewPanel.setCircuit(socketAnalyser.getCircuit(properties.getCircuitFileName()));
            }
        }
        
        /**
         * Unbind.
         */
        void unbind() {
            this.gameName = null;
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), GUITexts.INFO_NOGAMESELECTED));
            properties = null;
            previewPanel.setCircuit(null);
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
        }
    }
}
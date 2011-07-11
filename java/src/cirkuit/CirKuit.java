package cirkuit;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Random;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.awt.print.*;
import java.lang.Runtime;

import cirkuit.util.*;
import cirkuit.util.SplashScreen;
import cirkuit.player.*;
import cirkuit.properties.*;
import cirkuit.game.*;
import cirkuit.remote.*;
import cirkuit.remote.client.*;
import cirkuit.circuit.*;
import cirkuit.texts.GUITexts;

/**
 * This is the main class of the program, it defines the main gui.
 * TODO : Replace of occurences of show() with setVisible(true)
 *
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class CirKuit extends JFrame implements ActionListener, WindowListener, GameListener, MouseListener, MouseMotionListener, SocketListener {
    public final static String CONFIGURATION_FILE = "cirkuit.conf";
    public final static String VERSION = "2.0 alpha";
    
    private Random random;
    private GamePanel gamePanel;
    private JMenu replayItem;
    
    // game info box variables
    private GameInfoBox gameInfoBox = null;
    
    //private OnlineFrame onlineFrame = null;
    private cirkuit.remote.client.OnlineFrame onlineFrame = null;
    //private ChatFrame chatFrame = null;
    
    private Game currentGame = null;
    private Replay replay = null;
    private Properties props = null;
    private Player currentPlayer = null;
    private Vector playerPossibleNode = new Vector();
    private Vector playerPossibleNodeColor = new Vector();
    private Hashtable playerPosition = new Hashtable(); // for undo's but not used for the moment
    
    ServerSocketAnalyser socketAnalyser = null;
    String nickname = null;
    
    // nodes
    private int nodeSelectedIndex = -1;
    private Color nodeSelectedColor = new Color(0, 204, 0);
    
    public static void main(String[] args) {
        try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			//It is not a big deal...
		}
        new CirKuit();
    }
    
    /**
     * This constructor initialize the main window
     */
    public CirKuit() {
        SplashScreen splash = new SplashScreen();
        
        if ((new File(CONFIGURATION_FILE)).exists())
            props = Properties.load(CONFIGURATION_FILE);
        if (props == null)
            props = new Properties();
            
        random = new Random();
        splash.advance();
        
        // window
        setTitle(GUITexts.TITLE_CIRKUIT);
        setBounds(100, 100, 500, 500);
        setIconImage((new ImageIcon("./shared/image/icon.gif")).getImage());
        addWindowListener(this);
        
        // menu
        JMenuBar menubar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        
        menu = new JMenu(GUITexts.MENU_FILE);
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_STARTGAME));
        menuItem.setActionCommand("localgame");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('N', Event.CTRL_MASK));
        menuItem.addActionListener(this);
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_OPTIONS));
        menuItem.setActionCommand("options");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
        menuItem.addActionListener(this);
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_EDITCIRCUIT));
        menuItem.setActionCommand("edit");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('E', Event.CTRL_MASK));
        menuItem.addActionListener(this);
        menu.addSeparator();
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_SAVE));
        menuItem.setActionCommand("save");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK));
        menuItem.addActionListener(this);
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_LOAD));
        menuItem.setActionCommand("load");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('L', Event.CTRL_MASK));
        menuItem.addActionListener(this);
        
        menu.add(replayItem = new JMenu(GUITexts.MENU_REPLAY));
        replayItem.setEnabled(false);
        replayItem.add(menuItem = new JMenuItem(GUITexts.MENU_SPEED025));
        menuItem.setActionCommand("replay0.25");
        menuItem.addActionListener(this);
        replayItem.add(menuItem = new JMenuItem(GUITexts.MENU_SPEED050));
        menuItem.setActionCommand("replay0.5");
        menuItem.addActionListener(this);
        replayItem.add(menuItem = new JMenuItem(GUITexts.MENU_SPEED100));
        menuItem.setAccelerator(KeyStroke.getKeyStroke('R', Event.CTRL_MASK));
        menuItem.setActionCommand("replay1");
        menuItem.addActionListener(this);
        replayItem.add(menuItem = new JMenuItem(GUITexts.MENU_SPEED200));
        menuItem.setActionCommand("replay2");
        menuItem.addActionListener(this);
        
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_PRINT));
        menuItem.setActionCommand("print");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('P', Event.CTRL_MASK));
        menuItem.addActionListener(this);
        menu.addSeparator();
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_PLAYONLINE));
        menuItem.setActionCommand("onlinegame");
        menuItem.setAccelerator(KeyStroke.getKeyStroke('I', Event.CTRL_MASK));
        menuItem.addActionListener(this);
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_LAUNCHSERVER));
        menuItem.setActionCommand("server");
        menuItem.addActionListener(this);
        menu.addSeparator();
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_EXIT));
        menuItem.setAccelerator(KeyStroke.getKeyStroke('X', Event.CTRL_MASK));
        menuItem.setActionCommand("exit");
        menuItem.addActionListener(this);
        menubar.add(menu);
        
        menu = new JMenu(GUITexts.MENU_HELP);
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_HELP));
        menuItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
        menuItem.setActionCommand("help");
        menuItem.addActionListener(this);
        menu.add(menuItem = new JMenuItem(GUITexts.MENU_ABOUT));
        menuItem.setAccelerator(KeyStroke.getKeyStroke('A', Event.CTRL_MASK));
        menuItem.setActionCommand("about");
        menuItem.addActionListener(this);
        menubar.add(menu);
        
        setJMenuBar(menubar);

        splash.advance();
        
        // content
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());
        
        // Game Panel
        content.add(new JScrollPane(gamePanel = new GamePanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        gamePanel.addMouseListener(this);
        gamePanel.addMouseMotionListener(this);
        
        splash.advance();
        splash.dispose();
        
        show();
    }
    
    private void exit() {
        props.save(CirKuit.CONFIGURATION_FILE);
        dispose();
        System.exit(0);
    }
    
    /** Action Listener */
    public void actionPerformed(ActionEvent e) {
        String com = e.getActionCommand();
        if (com.equals("localgame")) {
            replayItem.setEnabled(false);
            if (currentGame != null)
                currentGame.interrupt();
            if (replay != null) {
                replay.interrupt();
                replay = null;
            }
            
            playerPosition.clear();

            currentGame = new Game(props);
            currentGame.addGameListener(this);
            gameInfoBox = new GameInfoBox(currentGame, currentGame.getCircuit().getWidth()+50, 50);
            currentGame.addGameListener(gameInfoBox);
            currentGame.setStartingPlayer(Math.abs(random.nextInt()%(props.getPlayerColor().size())));
            // check if window is already maximised
            if ((this.getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
                this.setSize(currentGame.getCircuit().getWidth()+300, currentGame.getCircuit().getHeight()+100);
                this.show(); // to make the setSize effective
            }
            currentGame.start();
        } else if (com.equals("options")) {
            if (currentGame != null)
                currentGame.interrupt();
            if (replay != null) {
                replay.interrupt();
                replay = null;
            }
            (new PropertiesDialog(this, props)).show();
            repaint();
        } else if (com.equals("exit")) {
            exit();
        } else if (com.equals("onlinegame")) {
            try {
                socketAnalyser = new ServerSocketAnalyser(new CommandSocket(new Socket(props.getServerIP(), props.getServerPort())));
                socketAnalyser.addSocketListener(this);
                socketAnalyser.start();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to communicate with the server.\nPlease check your settings.", "Communication error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (com.equals("server")) {
            /* String[] arg = {"cmd", "\\K", "java -cp classes cirkuit.CirKuitServer", ""+props.getServerPort()};
            try {
                Process pr = Runtime.getRuntime().exec(arg);
            } catch (IOException ex) { } */
        } else if (com.equals("edit")) {
            if (currentGame != null && new File(props.getCircuitFileName()).exists())
                (new CirKuitEdit(props.getCircuitFileName())).show();
        } else if (com.equals("save")) {
            if (currentGame != null) {
                String f = showBrowseDialog("Save a  game", "Save", "saves the current game", 'S', null);
                if (f != null)
                    currentGame.save(f);
            }
        } else if (com.equals("load")) {
            
            replayItem.setEnabled(false);
            if (currentGame != null)
                currentGame.interrupt();
            if (replay != null) {
                replay.interrupt();
                replay = null;
            }
            currentGame = new Game(props);
            
            String f = showBrowseDialog("Load a  game","Load","load the specified game",'S',null);
            if (f != null) {
                currentGame.load(f);
                currentGame.addGameListener(this);
                gameInfoBox = new GameInfoBox(currentGame, currentGame.getCircuit().getWidth()+50, 50);
                currentGame.addGameListener(gameInfoBox);
                // check if window is already maximised
                if ((this.getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
                    this.setSize(currentGame.getCircuit().getWidth()+300, currentGame.getCircuit().getHeight()+100);
                    this.show(); // to make the setSize effective
                }
                currentGame.start();
            }
        } else if (com.startsWith("replay")) {
             if (replay != null)
                replay.interrupt();
            if (currentGame != null) {
                currentGame.interrupt();
                replay = new Replay(currentGame);
                double speed = Double.parseDouble(com.substring(6));
                if (props.getIsRallyMode())
                    replay.setSpeed(speed*5);
                else
                    replay.setSpeed(speed);
                replay.addGameListener(this);
                replay.start();
            }
        } else if (com.equals("print")) {
            if (replay != null) {
                replay.interrupt();
                replay = null;
            }
                
            PrinterJob printJob = PrinterJob.getPrinterJob();
            PageFormat pageFormat = printJob.defaultPage();
            //pageFormat = printJob.pageDialog(pageFormat);
            
            printJob.setPrintable(gamePanel, pageFormat);
            if (printJob.printDialog()) {
                try {
                    printJob.print();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Printer Error", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } else if (com.equals("help")) {
            (new HelpDialog(this, "./man/help.html")).show();
        } else if (com.equals("about")) {
            (new AboutDialog(this, "./shared/image/logo.gif", "CirKuit 2D " + VERSION, "Sven Gowal - Pascal Perez", "svenadrian.gowal@epfl.ch - pascal.perez@epfl.ch")).show();
        }
    }
    
    /** Socket Analyser */
    public void dataArrived(String command) {
        System.out.println("CirKuit : data arrived, start");
        try {
            if (command.equals("WELC")) {
                Vector linkedFrame = new Vector();
                //onlineFrame = new OnlineFrame(this, socketAnalyser, linkedFrame);
                onlineFrame = new cirkuit.remote.client.OnlineFrame(this, socketAnalyser, linkedFrame);
                //chatFrame = new ChatFrame(this, socketAnalyser);
                //linkedFrame.add(chatFrame);
                Color c = props.getOnlineColor();
                socketAnalyser.reply("INFO "+socketAnalyser.stringEncode(props.getNickname())+","+c.getRed()+","+c.getGreen()+","+c.getBlue());
            } else if (command.equals("COMC")) {
                switch (socketAnalyser.getSocketState()) {
                    case SocketAnalyser.STATE_WELC:
                        nickname = socketAnalyser.getNextString();
                }
            } else if (command.equals("STIG")) {
                socketAnalyser.getCommandLine();
                command = socketAnalyser.getNextCommand();
                if (command.equals("GINF")) {
                    Properties onlineProps = new Properties();
                    Vector onlinePlayers = new Vector();
                    Circuit circuit = null;

                    socketAnalyser.getCommandLine();
                    command = socketAnalyser.getNextCommand();
                    while (!command.equals("GINE")) {
                        System.out.println(command+socketAnalyser.getInBuffer());
                        if (command.equals("PLYR")) {
                            String name = socketAnalyser.getNextString();
                            Color color = socketAnalyser.getNextColor();
                            Player player;
                            if (name.equals(nickname)) {
                                player = new HumanPlayer(name, color);
                            } else {
                                player = new VirtualPlayer(name, color);
                            }
                            onlinePlayers.add(player);
                        } else if (command.equals("PRGR")) {
                            int i = socketAnalyser.getNextInt(-1);
                            if (i>=0) {
                                onlineProps.setGrid(i);
                            }
                        } else if (command.equals("PRMS")) {
                            int i = socketAnalyser.getNextInt(-1);
                            if (i>=0) {
                                onlineProps.setMinusSpeed(i);
                            }
                        } else if (command.equals("PRPS")) {
                            int i = socketAnalyser.getNextInt(-1);
                            if (i>=0) {
                                onlineProps.setPlusSpeed(i);
                            }
                        } else if (command.equals("PRCR")) {
                            int i = socketAnalyser.getNextInt(-1);
                            if (i>=0) {
                                onlineProps.setCrashRadius(i);
                            }
                        } else if (command.equals("PRMA")) {
                            int i = socketAnalyser.getNextInt(-1);
                            if (i>=0) {
                                onlineProps.setMaxAngle(Math.toRadians(i));
                            }
                        } else if (command.equals("PRRD")) {
                            onlineProps.setRedDots(socketAnalyser.getNextBoolean());
                        } else if (command.equals("PRIC")) {
                            onlineProps.setIsCrash(socketAnalyser.getNextBoolean());
                        } else if (command.equals("PRMD")) {
                            int i = socketAnalyser.getNextInt(-1);
                            if (i>=0) {
                                onlineProps.setMode(i);
                            }
                        } else if (command.equals("PRTN")) {
                            int i = socketAnalyser.getNextInt(-1);
                            if (i>0) {
                                onlineProps.setTurn(i);
                            }
                        } else if (command.equals("PRTC")) {
                            int i = socketAnalyser.getNextInt(-1);
                            if (i>=0) {
                                onlineProps.setTrace(i);
                            }
                        } else if (command.equals("CINF")) {
                            StringBuffer buffer = new StringBuffer();
                            socketAnalyser.getCommandLine();
                            command = socketAnalyser.getNextCommand();
                            while (!command.equals("CINE")) {
                                buffer.append(command);
                                buffer.append(socketAnalyser.getInBuffer());
                                // buffer.append('\n');
                                socketAnalyser.getCommandLine();
                                command = socketAnalyser.getNextCommand();
                            }
                            circuit = new Circuit(buffer.toString());
                        }
                        socketAnalyser.getCommandLine();
                        command = socketAnalyser.getNextCommand();
                    }

                    onlineProps.setInColor(props.getInColor());
                    onlineProps.setOutColor(props.getOutColor());
                    onlineProps.setStartColor(props.getStartColor());
                    
                    replayItem.setEnabled(false);
                    if (currentGame != null)
                        currentGame.interrupt();
                    if (replay != null) {
                        replay.interrupt();
                        replay = null;
                    }
                    
                    playerPosition.clear();
                    
                    System.out.println(socketAnalyser+" "+onlineProps+" "+onlinePlayers+" "+circuit);

                    currentGame = new RemoteGame(socketAnalyser, onlineProps, onlinePlayers, new Vector(), circuit);
                    gameInfoBox = new GameInfoBox(currentGame, currentGame.getCircuit().getWidth()+50, 50);
                    currentGame.addGameListener(this);
                    currentGame.addGameListener(gameInfoBox);
                    socketAnalyser.addSocketListener((RemoteGame)currentGame);
                    
                    // check if window is already maximised
                    if ((this.getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
                        this.setSize(currentGame.getCircuit().getWidth()+300, currentGame.getCircuit().getHeight()+100);
                        this.show(); // to make the setSize effective
                    }
                    socketAnalyser.reply("REDY");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("CirKuit : data arrived, end");
    }
    
    /** Window Listener */
    public void windowClosing(WindowEvent e) {
        exit();
    }
    /** Window Listener */
    public void windowClosed(WindowEvent e) { }
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
    
    /** Game Listener */
    public void gameStarted() { }
    /** Game Listener */
    public void gameFinished() {
        if (currentGame instanceof RemoteGame) {
            socketAnalyser.removeSocketListener((RemoteGame)currentGame);
        }
        playerPossibleNode.clear();
        replayItem.setEnabled(true);
        repaint();
    }
    /** Game Listener */
    public void playerArrived(Player p, double time) { }
    /** Game Listener */
    public void playerOut(Player p) { }
    /** Game Listener */
    public void crash(Vector v) { }
    
    /** Game Listener */
    public void gameChanged(Game g) {
        repaint();
    }
    /** Game Listener */
    public void nextPlayer(Game g, Player player) {
        currentPlayer = player;
        if (currentPlayer instanceof HumanPlayer) {
            playerPossibleNode = currentGame.getValidMoves(currentPlayer);
            playerPossibleNodeColor = currentGame.getMovesColor(playerPossibleNode, currentPlayer);
        }
    }
    
    /** Game Listener */
    public void playerMoved(Player player) { }
    
    /** Mouse Listener */
    public void mouseClicked(MouseEvent e) {
        /*Runtime rt = java.lang.Runtime.getRuntime();
        long free = rt.freeMemory();
        long total = rt.totalMemory();
        System.out.println(free/1024+"Kb/"+total/1024+"Kb ("+(int)(((double)free/(double)total)*1000)/10+"% used)");*/
        
        int i = getNearestNodeIndex(e.getX(), e.getY());
        if (i > -1) {
            ((HumanPlayer)currentPlayer).setMove(((Point)playerPossibleNode.get(i)));
        }
    }
    /** Mouse Listener */
    public void mouseEntered(MouseEvent e) { }
    /** Mouse Listener */
    public void mouseExited(MouseEvent e) {
        if (gameInfoBox != null) {
            gameInfoBox.setAnchorMode(false);
        }
    }
    /** Mouse Listener */
    public void mousePressed(MouseEvent e) {
        // does the guy wanted to move the game info box ?
        if (gameInfoBox != null) {
            int posX = e.getX();
            int posY = e.getY();
            int gameInfoBoxX = gameInfoBox.getX();
            int gameInfoBoxY = gameInfoBox.getY();
            gameInfoBox.setAnchorMode(posX >= gameInfoBoxX && posX <= gameInfoBoxX+gameInfoBox.getWidth() && posY >= gameInfoBoxY && posY <= gameInfoBoxY+gameInfoBox.getHeight());
        }
    }
    /** Mouse Listener */
    public void mouseReleased(MouseEvent e) {
        if (gameInfoBox != null) {
            gameInfoBox.setAnchorMode(false);
            repaint();
        }
    }
    
    
    /** Mouse motion litener */
    public void mouseDragged(MouseEvent e) {
        if (gameInfoBox != null && gameInfoBox.getAnchorMode()) {
            gameInfoBox.setX(e.getX());
            gameInfoBox.setY(e.getY());
            repaint();
        }
    }
    /** Mouse motion litener */
    public void mouseMoved(MouseEvent e) {
        int i = getNearestNodeIndex(e.getX(), e.getY());
        if (i != nodeSelectedIndex) {
            nodeSelectedIndex = i;
            repaint();
        }
    }
    
    /** Shows a FileChooser */
    private String showBrowseDialog(String title, String buttonText, String tooltip, char approveChar, String oldFile) {
        JFileChooser files = new JFileChooser(props.getCircuitFileName());
        files.setDialogTitle(title);
        files.setApproveButtonText(buttonText);
        files.setApproveButtonToolTipText(tooltip);
        files.setApproveButtonMnemonic(approveChar);
        files.setFileSelectionMode(JFileChooser.FILES_ONLY);
        files.setMultiSelectionEnabled(false);
        files.rescanCurrentDirectory();
        files.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory() || (f.isFile() && f.getPath().endsWith(".gkt"))) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return "gkt files";
            }
        } );
        int result = files.showDialog(this, null);
        File file = (result == files.APPROVE_OPTION)? files.getSelectedFile() : null;
        if (file != null) {
            return file.getAbsolutePath();
        } else
            return oldFile;
    }
    
    /**
     * @return the node index, -1 in case of error
     */
    private int getNearestNodeIndex(int posX, int posY) {
        int pindex = -1;
        if (currentPlayer instanceof HumanPlayer && ((HumanPlayer)currentPlayer).isReady() && playerPossibleNode.size()>0) {
            // Play the closest point
            int min = -1;
            
            int tmp;
            Point p;
            
            
            for (int i=0; i<playerPossibleNode.size(); i++) {
                p = (Point)playerPossibleNode.get(i);
                tmp = (p.x-posX)*(p.x-posX)+(p.y-posY)*(p.y-posY);
                if (min==-1 || tmp < min) {
                    min = tmp;
                    pindex = i;
                }
            }
        }
        return pindex;
    }
    
    /** Class designed to draw the game */
    class GamePanel extends JPanel implements Printable {
        GamePanel() {
            setDoubleBuffered(true);
            setBackground(Color.white);
            setOpaque(true);
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (replay != null && !replay.isInterrupted())
                replay.draw(g);
            else {
                if (currentGame != null) {
                    currentGame.drawGrid(g, this.getWidth(), this.getHeight());
                    currentGame.draw(g);
                    drawNode(g);
                }
            }
            
            if (gameInfoBox != null) {
                gameInfoBox.draw(g);
            }
        }
        
        /** Draws all the possible nodes that a human player can use. */
        private void drawNode(Graphics g) {
            Point p;
            Color c;
            int diameter = currentGame.getProperties().getGrid()/3+1;
            int imax = Math.min(playerPossibleNode.size(), playerPossibleNodeColor.size());
            for (int i=0; i < imax; i++) {
                p = (Point)playerPossibleNode.get(i);
                g.setColor(Color.black);
                g.fillOval(p.x-(diameter/2)-1, p.y-(diameter/2)-1, diameter+2, diameter+2);
                c = (Color)(playerPossibleNodeColor.get(i));
                if (nodeSelectedIndex == i) {
                    if (c.equals(Color.red)) {
                        g.setColor(Color.black);
                    } else {
                        g.setColor(nodeSelectedColor);
                    }
                } else {
                    g.setColor(c);
                }
                g.fillOval(p.x-(diameter/2), p.y-(diameter/2), diameter, diameter);
            }
        }
        
        /** Printable */
        public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0)
                return NO_SUCH_PAGE;
            
            Graphics2D g2d = (Graphics2D)g;
            double scaleX = pageFormat.getImageableWidth()/this.getWidth();
            double scaleY = pageFormat.getImageableHeight()/this.getHeight();
            double scale = Math.min(scaleX, scaleY);
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            if (scale < 1)
                g2d.scale(scale, scale);
            paint(g2d);
            return PAGE_EXISTS;
        }
    }
}
package cirkuit.remote.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.IOException;

import cirkuit.CirKuit;
import cirkuit.util.*;
import cirkuit.remote.ServerSocketAnalyser;
import cirkuit.remote.SocketAnalyser;
import cirkuit.remote.SocketListener;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class ChatFrame extends AttachedFrame implements ActionListener, SocketListener {
 
    private ServerSocketAnalyser socketAnalyser = null;
    private JTextField sendField = null;
    private JTextArea dialogArea = null;
    
    public ChatFrame(Window owner, ServerSocketAnalyser socketAnalyser) {
        super(owner);
        this.socketAnalyser = socketAnalyser;
        socketAnalyser.addSocketListener(this);
        
        // window
        setStyle(AttachedFrame.BOTTOM);
        setTitle("CirKuit 2D Chat");   
        setSize(500,120);
        setIconImage((new ImageIcon("./shared/image/icon.gif")).getImage());
        addWindowListener(this);
        
        // content
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(dialogArea = new JTextArea() , JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel bPanel = new JPanel(new BorderLayout());
        bPanel.add(sendField = new JTextField(), BorderLayout.CENTER);
        JButton button;
        Dimension size = new Dimension(60,20);
        bPanel.add(button = new JButton("Send"), BorderLayout.EAST);
        button.setMaximumSize(size);
        button.setMinimumSize(size);
        button.setPreferredSize(size);
        button.setActionCommand("send");
        button.addActionListener(this);
        
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(bPanel, BorderLayout.SOUTH);
        
        show();
    }
    
    /** Socket Analyser */
    public void dataArrived(String command) {
        try {
            if (command.equals("MESG")) {
                String username = socketAnalyser.getNextString();
                String mesg = socketAnalyser.getNextString();
                dialogArea.append(username+"> "+mesg+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** Action Listener */
    public void actionPerformed(ActionEvent e) {
        String com = e.getActionCommand();
        if (com.equals("send")) {
            try {
                socketAnalyser.reply("CHAT "+socketAnalyser.stringEncode(sendField.getText()));
                sendField.setText("");
                sendField.requestFocus();
            } catch (IOException ex) { }
        }
    }
}

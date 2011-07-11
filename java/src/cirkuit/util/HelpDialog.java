package cirkuit.util;

import java.io.*;
import java.net.*;
import javax.swing.text.html.*;
import javax.swing.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * This class defines the help dialog.<br>
 * It's in fact a primitive web browser.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class HelpDialog extends JDialog implements ActionListener,HyperlinkListener {
    JEditorPane navPane;
    
    public HelpDialog(Frame owner, String url) {
        // dialog properties
        super(owner);
        Point s = owner.getLocation();
        setBounds(s.x+20 , s.y+5, 600, 600);
        setTitle("Help");
        setResizable(false);
        setModal(true);
    
        // the center panel is in fact a web browser and is used to display the help
        navPane = new JEditorPane();
        navPane.setEditable(false);
        try {
            navPane.setPage((new File(url)).toURL());
            navPane.addHyperlinkListener(this);
        }
        catch (Exception e) {
            navPane.setText("<html><body><h1>No Help Available</h1></body></html>");
        }
        
        //bottom panel
        JButton okButton = new JButton("OK");
        Dimension d = okButton.getPreferredSize();
        okButton.setMaximumSize(d);
        okButton.setMinimumSize(d);
        okButton.addActionListener(this);
                
        JPanel tmpPanel = new JPanel(new FlowLayout());
        tmpPanel.setBackground(Color.white);
        tmpPanel.add(okButton);
                
        //contentPane
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());
        content.add(new JScrollPane(navPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ),BorderLayout.CENTER);
        content.add(tmpPanel, BorderLayout.SOUTH);
    }

    /** Hyperlink Listener */
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) e.getSource();
            if (e instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
                HTMLDocument doc = (HTMLDocument)pane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            } else {
                try {
                    pane.setPage(e.getURL());
                } catch (Throwable t) { System.out.println("Could not find URL"); }
            }
        }
    }
    
    /** Action Listener */
    public void actionPerformed(ActionEvent e) {
        dispose();
    }
    
    /** Sets a page if possible */
    public void setPage(String url) {
        try {
           navPane.setPage((new File(url)).toURL());
        } catch(Exception e) {}
    }
}

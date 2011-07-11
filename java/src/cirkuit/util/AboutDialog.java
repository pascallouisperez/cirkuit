package cirkuit.util;

import javax.swing.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * This class defines the About dialog.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class AboutDialog extends JDialog implements ActionListener{
    int w = 300;
    int h = 300;
        
    ImageIcon logo = null;
    String txtTitle, txtNames, txtEMails;
    String[] thanks;
    
    /**
     * Constructor using an empty thanks array.
     */
    public AboutDialog(Frame owner, String image, String title, String names, String emails) {
        this(owner, image, title, names, emails, new String[0]);
    }
    
    /**
     * Constructor that specifies the owner, an image, a title, an author, his e-mail and an additionnal array of String to be displayed.
     */
    public AboutDialog(Frame owner, String image, String title, String names, String emails, String[] thanks) {
       super(owner);
       
       logo = new ImageIcon(image);
       txtTitle = title;
       txtNames = names;
       txtEMails = emails;
       this.thanks = thanks;
       
       Point s = owner.getLocation();
       setBounds(s.x+20 , s.y+20, w, h);
       setTitle("About");
       setResizable(false);
       setModal(true);
       Color c = Color.white;
            
       // the main panel
       AboutPanel mainPanel = new AboutPanel(w,h);
       mainPanel.setBackground(c);
            
       JButton okButton = new JButton("OK");
	   Dimension d = okButton.getPreferredSize();
       okButton.setMaximumSize(d);
       okButton.setMinimumSize(d);
       okButton.addActionListener(this);
            
       JPanel tmpPanel = new JPanel(new FlowLayout());
       tmpPanel.setBackground(c);
       tmpPanel.add(okButton);
          
       Container content = this.getContentPane();
       content.setLayout(new BorderLayout());
       content.add(mainPanel,BorderLayout.CENTER);
       content.add(tmpPanel, BorderLayout.SOUTH);
    }
    
    /** Setting the size of this about dialog */
    public void setSize(int w, int h) {
        super.setSize(w,h);
        this.w = w;
        this.h = h;
    }
    
    /** Action Listener */
    public void actionPerformed(ActionEvent e) {
        dispose();
    }
    
    /** The panel used to draw the content of the about dialog */
    class AboutPanel extends JPanel {
        int h,w;
           
        AboutPanel(int w, int h) {
            this.w = w;
            this.h = h;
        }
            
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
                
            // 2D graphics
            Graphics2D g2 = (Graphics2D)g;
            
            // Font to use
            Font fTitle = new Font("SansSerif", Font.BOLD, 20);
            Font fNames = new Font("SansSerif", Font.BOLD, 12);
            Font fEMails = new Font("SansSerif", Font.BOLD, 10);
            
            // font render context
            FontRenderContext context = g2.getFontRenderContext();
            Rectangle2D boundsTitle = fTitle.getStringBounds(txtTitle,context);
            Rectangle2D boundsNames = fNames.getStringBounds(txtNames,context);
            Rectangle2D boundsEMails = fEMails.getStringBounds(txtEMails,context);
                
            // title
            g2.setFont(fTitle);
            g2.drawString(txtTitle,(int)((double)(w-boundsTitle.getWidth())/2.0),logo.getIconHeight()+50);

            // names
            g2.setFont(fNames);
            g2.drawString(txtNames,(int)((double)(w-boundsNames.getWidth())/2.0),logo.getIconHeight()+70);
                
            // e-mails
            g2.setFont(fEMails);
            g2.drawString(txtEMails,(int)((double)(w-boundsEMails.getWidth())/2.0),logo.getIconHeight()+80);
                
            // EPFL logo ;)
            g.drawImage(logo.getImage(),(w-logo.getIconWidth())/2,10,logo.getIconWidth(),logo.getIconHeight(),null);
            
            // Thanks
            Rectangle2D bounds;
            for (int i=0; i<thanks.length; i++) {
                bounds = fEMails.getStringBounds(thanks[i], context);
                g2.drawString(thanks[i], (int)((double)(w-bounds.getWidth())/2.0), (int)(200+i*bounds.getHeight()));
            }
        }
    }    
}

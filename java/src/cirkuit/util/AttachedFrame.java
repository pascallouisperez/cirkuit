package cirkuit.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class AttachedFrame extends JFrame implements WindowListener, ComponentListener {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    
    private boolean wasOpened = false;
    private Window owner = null;
    private int style = RIGHT;
    private int addedSize = 0;
    private int offset = 0;
    
    public AttachedFrame(Window owner) {
        super();
        construct(owner);
    }
    
    public AttachedFrame(Window owner, GraphicsConfiguration gc) {
        super(gc);
        construct(owner);
    }
    
    public AttachedFrame(Window owner, String title) {
        super(title);
        construct(owner);
    }
    
    public AttachedFrame(Window owner, String title, GraphicsConfiguration gc) {
        super(title, gc);
        construct(owner);
    }
    
    public void setStyle(int s) {
        this.style = s;
    }
    
    public int getStyle() {
        return this.style;
    }
    
    public void setAddedSize(int s) {
        this.addedSize = s;
    }
    
    public int getAddedSize() {
        return this.addedSize;
    }
    
    public void setOffset(int s) {
        this.offset = s;
    }
    
    public int getOffset() {
        return this.offset;
    }
    
    private void construct(Window owner) {
        if (owner != null) {
            this.owner = owner;
            owner.addWindowListener(this);
            owner.addComponentListener(this);
        }
    }
    
    private void adjustPosition() {
        if (owner != null) {
            if (style == RIGHT) {
                this.setLocation(owner.getX()+owner.getWidth(), owner.getY()+offset);
                this.setSize(this.getWidth(), owner.getHeight()+addedSize);
            } else if (style == LEFT) {
                this.setLocation(owner.getX()-this.getWidth(), owner.getY()+offset);
                this.setSize(this.getWidth(), owner.getHeight()+addedSize);
            } else if (style == TOP) {
                this.setLocation(owner.getX()+offset, owner.getY()-this.getHeight());
                this.setSize(owner.getWidth()+addedSize, this.getHeight());
            } else if (style == BOTTOM) {
                this.setLocation(owner.getX()+offset, owner.getY()+owner.getHeight());
                this.setSize(owner.getWidth()+addedSize, this.getHeight());
            }
            //this.show();
        }
    }
    
    public void show() {
        super.show();
        adjustPosition();
    }
    
    /** Window Listener */
    public void windowClosing(WindowEvent e) {
        dispose();
    }
    /** Window Listener */
    public void windowClosed(WindowEvent e) { }
    /** Window Listener */
    public void windowOpened(WindowEvent e) { }
    /** Window Listener */
    public void windowIconified(WindowEvent e) {
        wasOpened = isShowing();
        hide();
    }
    /** Window Listener */
    public void windowDeiconified(WindowEvent e) {
        if (wasOpened)
            show();
    }
    /** Window Listener */
    public void windowActivated(WindowEvent e) { }
    /** Window Listener */
    public void windowDeactivated(WindowEvent e) { }
    
    /** Component Listener */
    public void componentHidden(ComponentEvent e) {
        wasOpened = isShowing();
        hide();
    }
    /** Component Listener */        
    public void componentMoved(ComponentEvent e) {
        adjustPosition();
    }
    /** Component Listener */
    public void componentResized(ComponentEvent e) {
        adjustPosition();
    }
    /** Component Listener */
    public void componentShown(ComponentEvent e) {
        if (wasOpened)
            show();
    }
}

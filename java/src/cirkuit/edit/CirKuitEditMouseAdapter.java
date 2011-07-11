package cirkuit.edit;

import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import cirkuit.circuit.*;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class CirKuitEditMouseAdapter extends MouseAdapter implements MouseMotionListener {
    Circuit circuit;
    CirKuitEditPanel parent;
    int xPos,yPos;
    int innerNodeIndex     = -1;
    int innerNodeLastIndex = -1;
    int outerNodeIndex     = -1;
    int outerNodeLastIndex = -1;
    int startLineNodeIndex = -1;
    
    public CirKuitEditMouseAdapter(CirKuitEditPanel parent, Circuit circuit) {
        this.parent = parent;
        this.circuit = circuit;
    }
    
    public void mousePressed(MouseEvent e) {
        xPos = e.getX();
        yPos = e.getY();
        // setting the nodes index
        innerNodeIndex     = circuit.getInnerNodeIndexApproximate(xPos,yPos);
        outerNodeIndex     = circuit.getOuterNodeIndexApproximate(xPos,yPos);
        startLineNodeIndex = circuit.getStartingLineNodeIndexApproximate(xPos,yPos);
        
        switch (parent.getMode()) {
            case CirKuitEditInternalFrame.MODE_ADD_NODE_OUTER:
                addOuterNode(xPos,yPos);
                break;
            
            case CirKuitEditInternalFrame.MODE_ADD_NODE_OUTER_AFTER:
                if (outerNodeLastIndex == -1) {
                    addOuterNode(xPos,yPos);
                } else {
                    addOuterNodeAfter(outerNodeLastIndex, xPos,yPos);
                }
                break;
            
            case CirKuitEditInternalFrame.MODE_ADD_NODE_INNER:
                addInnerNode(xPos,yPos);
                break;
            
            
            case CirKuitEditInternalFrame.MODE_ADD_NODE_INNER_AFTER:
                if (innerNodeLastIndex == -1) {
                    addInnerNode(xPos,yPos);
                } else {
                    addInnerNodeAfter(innerNodeLastIndex, xPos,yPos);
                }
                break;
            
            case CirKuitEditInternalFrame.MODE_DELETE_NODE:
                // delete the node
                if (innerNodeIndex != -1) {
                    deleteInnerNode(innerNodeIndex);
                } else if (outerNodeIndex != -1) {
                    deleteOuterNode(outerNodeIndex);
                }
                break;
            
            default:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 1) {
                        if (((e.getModifiersEx() & e.SHIFT_DOWN_MASK) != 0) && ((e.getModifiersEx() & e.CTRL_DOWN_MASK) == 0)) {
                            if ((e.getModifiersEx() & e.ALT_DOWN_MASK) != 0 && outerNodeLastIndex != -1) {
                                addOuterNodeAfter(outerNodeLastIndex, xPos, yPos);
                            } else {
                                // add node outer border
                                addOuterNode(xPos,yPos);
                            }
                        } else if (((e.getModifiersEx() & e.SHIFT_DOWN_MASK) == 0) && ((e.getModifiersEx() & e.CTRL_DOWN_MASK) != 0)) {
                            if ((e.getModifiersEx() & e.ALT_DOWN_MASK) != 0 && innerNodeLastIndex != -1) {
                                addInnerNodeAfter(innerNodeLastIndex, xPos, yPos);
                            } else {
                                // add node outer border
                                addInnerNode(xPos,yPos);
                            }
                        }
                    } else {
                        // delete the node
                        if (innerNodeIndex != -1) {
                            circuit.deleteInnerNode(innerNodeIndex);
                        } else if (outerNodeIndex != -1) {
                            circuit.deleteOuterNode(outerNodeIndex);
                        }
                    }
                }
                break;
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            xPos = e.getX();
            yPos = e.getY();
        }
        
        parent.paintImmediately(0,0,parent.getWidth(),parent.getHeight());
        
        // unsetting the nodes index
        innerNodeIndex     = -1;
        outerNodeIndex     = -1;
        startLineNodeIndex = -1;
    }
    
    public void mouseDragged(MouseEvent e) {
        if (innerNodeIndex != -1) {
            circuit.setInnerNode(innerNodeIndex,e.getX(),e.getY());
        } else if (outerNodeIndex != -1) {
            circuit.setOuterNode(outerNodeIndex,e.getX(),e.getY());
        } else if (startLineNodeIndex != -1) {
            circuit.setStartingLineNode(startLineNodeIndex,e.getX(),e.getY());
        }
        parent.paintImmediately(0,0,parent.getWidth(),parent.getHeight());
    }
    
    public void mouseMoved(MouseEvent e) { };
    
    public void addInnerNode(int x, int y) {
        circuit.addInnerNodeNearest(xPos, yPos);
        innerNodeLastIndex = circuit.getInnerNodeIndex(xPos, yPos);
        parent.paintImmediately(0,0,parent.getWidth(),parent.getHeight());
    }

    public void addOuterNode(int x, int y) {
        circuit.addOuterNodeNearest(xPos, yPos);
        outerNodeLastIndex = circuit.getOuterNodeIndex(xPos, yPos);
        parent.paintImmediately(0,0,parent.getWidth(),parent.getHeight());
    }
    
    public void addInnerNodeAfter(int n, int x, int y) {
        circuit.addInnerNodeAfter(n, xPos, yPos);
        innerNodeLastIndex++;
        parent.paintImmediately(0,0,parent.getWidth(),parent.getHeight());
    }

    public void addOuterNodeAfter(int n, int x, int y) {
        circuit.addOuterNodeAfter(n, xPos, yPos);
        outerNodeLastIndex++;
        parent.paintImmediately(0,0,parent.getWidth(),parent.getHeight());
    }
    
    public void deleteInnerNode(int index) {
        if (innerNodeLastIndex >= index) {
            innerNodeLastIndex--;
        }
        circuit.deleteInnerNode(index);
    }
    
    public void deleteOuterNode(int index) {
        if (outerNodeLastIndex >= index) {
            outerNodeLastIndex--;
        }
        circuit.deleteOuterNode(index);
    }
}

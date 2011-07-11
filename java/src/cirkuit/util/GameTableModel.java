package cirkuit.util;

import javax.swing.table.*;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class GameTableModel extends DefaultTableModel {
    
    public GameTableModel(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
    }
    
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}

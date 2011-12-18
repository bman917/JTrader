/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.ui;

import java.awt.event.KeyEvent;
import javax.swing.JTable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;

/**
 *
 * @author Mr Jacky
 */
public class ModelUtil {

    public static void addRow(JTraderDataModel model, JTable jTable) {
        model.addRow();
        int row = model.getRowCount() - 1;
        int column = 0;
        jTable.clearSelection();
        jTable.requestFocus();
        jTable.changeSelection(row, column, false, false);
        jTable.editCellAt(row, column);
    }

    public static void deleteRow(JTraderDataModel model, JTable jTable) {
        int[] rows = jTable.getSelectedRows();

        if (rows == null || rows.length == 0) {
            return;
        }

        final Confirmation msg = new NotifyDescriptor.Confirmation(
                "Delete " + rows.length + " Trade Entries?",
                "Confirm Delete",
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE);

        Object result = DialogDisplayer.getDefault().notify(msg);

        if (NotifyDescriptor.YES_OPTION.equals(result)) {
            model.deleteSelectedRows(jTable.getSelectedRows());
        }
    }
    
    public static void handlTabEvent(KeyEvent evt, JTraderDataModel tableModel, JTable jTable1){
                if (KeyEvent.VK_TAB == evt.getKeyCode()) {

            int col = jTable1.getSelectedColumn();
            int row = jTable1.getSelectedRow();

            int maxCol = tableModel.getColumnCount();
            int maxRow = tableModel.getRowCount();

            if (col + 1 == maxCol && row + 1 == maxRow) {
                ModelUtil.addRow(tableModel, jTable1);
            }
        }
    }
}

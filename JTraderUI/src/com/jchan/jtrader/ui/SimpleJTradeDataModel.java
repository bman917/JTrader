/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.ui;

import com.jchan.jtrader.Util;
import com.jchan.jtrader.model.Mode;
import com.jchan.jtrader.model.Trade;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Mr Jacky
 */
public class SimpleJTradeDataModel extends AbstractTableModel implements TableModelListener, JTraderDataModel {

    static final String nDATE = "Date";
    static final String nMODE = "Mode";
    static final String nSTOCK = "Stock";
    static final String nVOLUME = "Volume";
    static final String nPRICE = "Price";
    static final String nNETPRICE = "Net Price";
    private static InputOutput io = IOProvider.getDefault().getIO(Constants.OUTPUT, false);
    private String[] columnNames = {nDATE, nMODE, nSTOCK, nVOLUME, nPRICE, nNETPRICE};
    private List<Trade> trades = new ArrayList<Trade>();
    Filter filter = null;
    String filterToken = null;

    enum Filter {
        STOCK
    };

    public SimpleJTradeDataModel(String[] columnNames, Filter filter) {
        this.columnNames = columnNames;
        this.filter = filter;
        init();
    }

    public SimpleJTradeDataModel() {
        init();
    }

    private void init() {
        this.addTableModelListener(this);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e == null) {
            return;
        }

        switch (e.getType()) {
            case TableModelEvent.UPDATE:
                io.getOut().println("Table Data Updated");

                if (e.getColumn() > -1 && e.getFirstRow() > -1) {

                    Trade ce = trades.get(e.getFirstRow());
                    String data = String.valueOf(getValueAt(e.getFirstRow(), e.getColumn()));
                    io.getOut().println("Table Data Changed. FirstRow: " + e.getFirstRow() + ", LastRow: " + e.getLastRow() + ", Column: " + e.getColumn() + " Value: " + data + ", ID: " + ce.getId());
                    io.getOut().println("Update complete for ID: " + ce.getId());

                }
            case TableModelEvent.DELETE:
            case TableModelEvent.INSERT:
                break;
        }
    }

    @Override
    public String getColumnName(int index) {
        return columnNames[index];
    }

    @Override
    public int getRowCount() {
        return (trades == null) ? 0 : trades.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        Trade t = trades.get(rowIndex);

        String cn = this.getColumnName(columnIndex);

        if (t != null) {
            if (nDATE.equals(cn)) {
                return t.getDateAsString();
            } else if (nSTOCK.equals(cn)) {
                return t.getStock();
            } else if (nVOLUME.equals(cn)) {
                return t.getVolume();
            } else if (nMODE.equals(cn)) {
                return t.getMode();
            } else if (nPRICE.equals(cn)) {
                return t.getPrice();
            } else if (nNETPRICE.equals(cn)) {
                return t.getNetPrice();
            }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {

        if (row > trades.size() - 1) {
            return;
        }

        String newVal = String.valueOf(value).toUpperCase();
        String oldVal = null;

        Trade t = trades.get(row);
        oldVal = String.valueOf(getValueAt(row, column));
        int columnIndex = column;
        String cn = this.getColumnName(columnIndex);

        io.getOut().println("Updating ColumnName: " + cn + ", newVal: " + newVal);

        if (t != null && newVal != null && !newVal.equals(oldVal)) {
            if (nDATE.equals(cn)) {
                t.setDateAsString(newVal);
            } else if (nSTOCK.equals(cn)) {
                t.setStock(newVal);
            } else if (nVOLUME.equals(cn)) {
                Util.updateVol(t, Integer.parseInt(newVal));
            } else if (nMODE.equals(cn)) {
                if (newVal.trim().length() == 0) {
                    t.setMode(null);
                } else {
                    t.setMode(Mode.valueOf(newVal));
                }
            } else if (nPRICE.equals(cn)) {
                if (newVal.trim().length() == 0) {
                    t.setPrice(BigDecimal.ZERO);
                } else {
                    Util.setPrice(t, new BigDecimal(newVal));
                }
            } else if (nNETPRICE.equals(cn)) {
                if (newVal.trim().length() == 0) {
                    t.setNetPrice(BigDecimal.ZERO);
                } else {
                    t.setNetPrice(new BigDecimal(newVal));
                }
            }

            fireTableCellUpdated(row, column);
        }
    }

    public void deleteSelectedRows(int[] rows) {
        List<Trade> deleteList = new ArrayList<Trade>();
        for (int i : rows) {
            deleteList.add(trades.get(i));
        }
        trades.removeAll(deleteList);
        fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
    }

    public void rowsInserted() {
        fireTableRowsInserted(0, 0);
    }

    public void addRow() {
        Trade ce = new Trade();
        this.trades.add(ce);
        if (filter != null && filterToken != null) {
            switch (filter) {
                case STOCK:
                    ce.setStock(filterToken);
                    break;
            }
        }
        rowsInserted();
    }

    public void addRows(List<Trade> trades) {
        this.trades.addAll(trades);
        rowsInserted();
    }
    
    public List<Trade> getAllTrades() {
        return trades;
    }
}

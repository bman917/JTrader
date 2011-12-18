/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.ui;

import com.jchan.jtrader.JTraderDatabaseSv;
import com.jchan.jtrader.Logger;
import com.jchan.jtrader.Util;
import com.jchan.jtrader.model.Mode;
import com.jchan.jtrader.model.Trade;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Mr Jacky
 */
public class TradeDataModel extends AbstractTableModel implements TableModelListener, JTraderDataModel {

    static List<TradeDataModel> instanceList = new ArrayList<TradeDataModel>();
    static final String nDATE = "Date";
    static final String nMODE = "Mode";
    static final String nSTOCK = "Stock";
    static final String nVOLUME = "Volume";
    static final String nPRICE = "Price";
    static final String nNETPRICE = "Net Price";
    private static InputOutput io = IOProvider.getDefault().getIO(Constants.OUTPUT, false);
    private String[] columnNames = {nDATE, nMODE, nSTOCK, nVOLUME, nPRICE, nNETPRICE};
    private List<Trade> trades = null;
    JTraderDatabaseSv sv = null;
    Filter filter = null;
    String filterToken = null;
    private static Logger log = Logger.getInstance();

    enum Filter {

        STOCK
    };

    public TradeDataModel(String[] columnNames, Filter filter) {
        this.columnNames = columnNames;
        this.filter = filter;
        init();
    }

    public TradeDataModel() {
        init();
    }

    private void init() {
        sv = ServiceLoader.load(JTraderDatabaseSv.class).iterator().next();
        io.getOut().println("JTraderDatabaseSv: " + sv);
        this.addTableModelListener(this);

        if (instanceList.contains(this) == false) {
            instanceList.add(this);
        }
    }
    
    public void loadData() {
        loadData(false);
    }

    public void loadData(boolean propagateChange) {
        log.debug("TradeDataModel loading data. Filter: " + filter + "/" + filterToken);
        if (filter == null) {
            trades = sv.getAllTrades();
        } else {
            switch (filter) {
                case STOCK:
                    String[] s = {filterToken};
                    trades = sv.getTradeByStock(s);
                    break;
            }
        }
        
        if (propagateChange) {
            for (TradeDataModel model : instanceList) {
                model.loadData(false);
            }
        } else {
            fireTableDataChanged();
        }
    }

    public void load(Filter f, String value) {
        filter = f;
        filterToken = value;
        loadData(false);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e == null) {
            return;
        } else {

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

            sv.updateTrade(t);
            loadData(true);
            fireTableCellUpdated(row, column);
        }
    }

    public void deleteSelectedRows(int[] rows) {
        List<Trade> deleteList = new ArrayList<Trade>();

        List<Trade> currentList = new ArrayList<Trade>();
        currentList.addAll(trades);

        for (int i : rows) {
            Trade t = trades.get(i);
            io.getOut().println("Deleting: " + t);
            deleteList.add(t);

            if (t == null) {
                currentList.remove(i);
            }
        }
        sv.deleteTrades(deleteList);
        //trades = currentList;
        loadData(true);
        fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
    }

    public void rowsInserted() {
        fireTableRowsInserted(0, 0);
    }

    public void addRow() {
        Trade ce = new Trade();

        if (filter != null && filterToken != null) {
            switch (filter) {
                case STOCK:
                    ce.setStock(filterToken);
                    break;
            }
        }
        sv.saveTrade(ce);
        this.trades.add(ce);
        rowsInserted();
    }
}

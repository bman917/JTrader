/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.ui;

import com.jchan.jtrader.JTraderDatabaseSv;
import com.jchan.jtrader.model.Stock;
import com.jchan.jtrader.model.StockStats;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Mr Jacky
 */
public class SummaryDataModel extends AbstractTableModel implements TableModelListener {

    static final String nSTOCK = "Stock";
    static final String nMARKET_PRICE = "Market Price";
    static final String nVOLUME = "Owned Volume";
    static final String nCURRENT_GAIN = "Current Gain";
    static final String nAVE_BUY = "Ave. Buy Price";
    static final String nAVE_SELL = "Ave. Sell Price";
    static final String nGAIN = "Overall Gain";
    private String[] columnNames = {nSTOCK, nMARKET_PRICE, nVOLUME, nCURRENT_GAIN, nAVE_BUY, nAVE_SELL, nGAIN};
    List<StockStats> stockStasList = new ArrayList<StockStats>();

    public SummaryDataModel() {
        JTraderDatabaseSv sv = ServiceLoader.load(JTraderDatabaseSv.class).iterator().next();
        List<Stock> stocks = sv.getAllStocks();

        for (Stock s : stocks) {
            StockStats stats = StockStats.getStats(s.getCode());
            stats.calculateStats(s.getCode());
            stockStasList.add(stats);
        }
    }

    @Override
    public int getRowCount() {
        return stockStasList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        StockStats t = stockStasList.get(rowIndex);

        String cn = this.getColumnName(columnIndex);

        if (t != null) {
            if (nSTOCK.equals(cn)) {
                return t.getStockCode();
            } else if (nMARKET_PRICE.equals(cn)) {
                return t.getMarketValue();
            } else if (nVOLUME.equals(cn)) {
                return t.getVolumeInHand();
            } else if (nCURRENT_GAIN.equals(cn)) {
                return t.getCurrentNetPrice();
            } else if (nAVE_BUY.equals(cn)) {
                return t.getAveBuyPrice();
            } else if (nAVE_SELL.equals(cn)) {
                return t.getAveSellPrice();
            } else if (nGAIN.equals(cn)) {
                return t.getOverAllGain();
            }
        }
        return null;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
    }

    public BigDecimal calculateSum() {

        BigDecimal total = BigDecimal.ZERO;

        for (StockStats stat : stockStasList) {
            if (stat != null && stat.getOverAllGain() != null) {
                total = total.add(stat.getOverAllGain());
            }
        }
        return total;
    }
}

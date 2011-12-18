/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.ui;

/**
 *
 * @author Mr Jacky
 */
public interface JTraderDataModel {
    
    public void addRow();
    public int getRowCount();
    public int getColumnCount();
    public void deleteSelectedRows(int[] rows);
}

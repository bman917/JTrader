/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.ui;

import com.jchan.jtrader.model.Trade;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mr Jacky
 */
public class StatsService {
    
    private static List<StatsListener> listeners = new ArrayList<StatsListener>();
    
    public static void addListener(StatsListener listener) {
        listeners.add(listener);
    }
    
    public static void updateStats(String stockCode) {
        for(StatsListener l : listeners) {
            l.updateStats(stockCode);
        }
    }
    
    public static void updateStats(List<Trade> trades) {
        
    }
}

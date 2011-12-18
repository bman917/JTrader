/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader;

import com.jchan.jtrader.model.Trade;
import java.util.List;

/**
 *
 * @author Mr Jacky
 */
public interface JTraderCitisec {
    
    public List<Trade> downloadTradeHistory(String id1, String id2, String password) throws Exception;
    public void add(CitisecListener c);
}

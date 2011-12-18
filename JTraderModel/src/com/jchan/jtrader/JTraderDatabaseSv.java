/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader;

import com.jchan.jtrader.model.Mode;
import com.jchan.jtrader.model.Stock;
import com.jchan.jtrader.model.Trade;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Mr Jacky
 */
public interface JTraderDatabaseSv {
    
    public void saveTrade(Trade trade);
    public void saveTrades(List<Trade> trades);
    public void updateTrade(Trade trade);
    public List<Trade> getAllTrades();
    public void deleteAllTrades();
    public List<Trade> getTradeByStock(String[] stock);
    public void deleteTrade(Long id);
    public void deleteTrades(List<Trade> trades);
    
    public void saveStock(Stock stock);
    public List<Stock> getAllStocks();
    public void deleteAllStocks();
    public void updateStock(Stock stock);
    public Stock getStock(String stockCode);
    
    public BigDecimal getNetPriceSum(String stock, Mode mode);
    public int getVolumeSum(String stock, Mode mode);
    
    public int getAvailVolume(String stock);
    public BigDecimal getPurchaseAmount(String stock);
    public BigDecimal getAvePrice(String stock);
    public BigDecimal getAveBuyAmount(String stock);
    public BigDecimal getAveSellAmount(String stock);
    public List<String> getStocks();
 }

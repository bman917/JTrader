/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.hibernate;

import java.util.ArrayList;
import com.jchan.jtrader.JTraderSvBase;
import com.jchan.jtrader.model.Stock;
import com.jchan.jtrader.JTraderDatabaseSv;
import com.jchan.jtrader.model.Mode;
import com.jchan.jtrader.model.Trade;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mr Jacky
 */
public class JTraderHibernateSvTest {
    
    final double sumDelta = 0.02d;

    public JTraderHibernateSvTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    public JTraderDatabaseSv getSv() {
        return ServiceLoader.load(JTraderDatabaseSv.class).iterator().next();
    }

    @Test
    public void testBasicCreate() {

        JTraderDatabaseSv sv = getSv();

        sv.deleteAllTrades();
        sv.deleteAllStocks();

        Trade trade = new Trade("AGI", new Date(), Mode.SELL, 100);
        sv.saveTrade(trade);

        List<Trade> list = sv.getAllTrades();
        assertEquals("Number of trades is wrong.", 1, list.size());
        
        System.out.println("All Trades 0: " + list.get(0));

        sv.saveTrade(new Trade("SMC", new Date(), Mode.SELL, 100));
        sv.saveTrade(new Trade("DMC", new Date(), Mode.SELL, 100));
        sv.saveTrade(new Trade("SMC", new Date(), Mode.SELL, 100));

        List<Trade> list2 = sv.getAllTrades();

        assertEquals("Number of trades is wrong.", 4, list2.size());

        String[] code = {"SMC"};
        List<Trade> smc = sv.getTradeByStock(code);

        assertEquals("Number of trades is wrong.", 2, smc.size());
        
        System.out.println("SMC 0: " + smc.get(0));

        sv.deleteTrade(smc.get(0).getId());

        assertEquals("Number of trades is wrong.", 1, sv.getTradeByStock(code).size());
        
        List<String> stocks = sv.getStocks();
        
        System.out.println("Stocks: " + stocks);




    }

    @Test
    public void testBasicStocks() {

        JTraderDatabaseSv sv = ServiceLoader.load(JTraderDatabaseSv.class).iterator().next();

        sv.deleteAllStocks();

        Stock stock1 = new Stock("Alliance Global Group", "AGI");
        sv.saveStock(stock1);
        
        List<Stock> stockList = sv.getAllStocks();
        assertEquals("Number of Stocks is wrong.", 1, stockList.size());
        
        Stock stock2 = stockList.get(0);
        stock2.setMarketValue(new BigDecimal("12"));
        sv.updateStock(stock2);
        
        Stock stock3 = sv.getStock("AGI");
        assertEquals("Stock Market Price is wrong", 12d, stock3.getMarketValue().doubleValue(), 0d);
    }

    @Test
    public void testJTraderSv() {
        JTraderSvBase sv = new JTraderSvBase();
        String PWR = "PWR";
        String MEG = "MEG";
        
        List<Trade> trades = new ArrayList<Trade>();
        trades.add(sv.createTrade(new Date(), PWR, Mode.SELL, 3000, new BigDecimal("2.35")));
        trades.add(sv.createTrade(new Date(), PWR, Mode.SELL, 3000, new BigDecimal("2.29")));
        trades.add(sv.createTrade(new Date(), PWR, Mode.SELL, 5000, new BigDecimal("2.58")));
        trades.add(sv.createTrade(new Date(), PWR, Mode.SELL, 6000, new BigDecimal("2.58")));
        trades.add(sv.createTrade(new Date(), PWR, Mode.SELL, 1000, new BigDecimal("2.59")));
        
        trades.add(sv.createTrade(new Date(), MEG, Mode.SELL, 20000, new BigDecimal("1.97")));
        trades.add(sv.createTrade(new Date(), MEG, Mode.BUY, 10000, new BigDecimal("2.00")));
        trades.add(sv.createTrade(new Date(), MEG, Mode.BUY, 10000, new BigDecimal("2.14")));
        trades.add(sv.createTrade(new Date(), MEG, Mode.BUY, 10000, new BigDecimal("2.17")));

        JTraderDatabaseSv db = ServiceLoader.load(JTraderDatabaseSv.class).iterator().next();
        for(Trade t : trades)
        {
            db.saveTrade(t);
        }

        /*PWR*/
        BigDecimal sellSum = db.getNetPriceSum(PWR, Mode.SELL);
        assertEquals("Sell sum is incorrect.", 44512.15d, sellSum.doubleValue(), sumDelta);
        
        /*MEG*/
        BigDecimal buySum = db.getNetPriceSum(MEG, Mode.BUY);
        assertEquals("Buy sum is incorrect.", 63286.15d, buySum.doubleValue(), sumDelta);

        int volume = db.getVolumeSum(MEG, Mode.SELL);
        assertEquals("Sell volume is incorrect.", 20000, volume);

        volume = db.getVolumeSum(MEG, Mode.BUY);
        assertEquals("Buy volume is incorrect.", 30000, volume);
        
        volume = db.getAvailVolume(MEG);
        assertEquals("Avail volume is incorrect.", 10000, volume);
        
        BigDecimal purchangeAmt = db.getPurchaseAmount(MEG);
        assertEquals("Purchase Amt is incorrect.", 24199.38d, purchangeAmt.doubleValue(), sumDelta);
        
        BigDecimal avePrice = db.getAvePrice(MEG);
        assertEquals("Ave Price is incorrect.", 2.42d, avePrice.doubleValue(), sumDelta);
    }
    
    @Test
    public void testDeleteNull()
    {
        Trade t = new Trade();
        List list = new ArrayList();
        list.add(t);
        list.add(null);
        
        JTraderDatabaseSv sv = getSv();
        sv.deleteTrades(list);
        assertTrue("JTraderDatabaseSv.delete should remove trades will null IDs", (list.isEmpty()));
    }
}

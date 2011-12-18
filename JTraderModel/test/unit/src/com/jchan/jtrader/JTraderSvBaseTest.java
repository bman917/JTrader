/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader;

import java.math.BigDecimal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mr Jacky
 */
public class JTraderSvBaseTest {

    public JTraderSvBaseTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testAddTrade() {
    }

    @Test
    public void testCalcNetSellPrice() {

        JTraderSvBase sv = new JTraderSvBase();
        BigDecimal netSellPrice = sv.calcNetSellPrice(2000, new BigDecimal("12.00"));
        assertEquals("Net Selling Price Incorrect.", 23809.20d, netSellPrice.doubleValue(), 0d);

        BigDecimal netBuyPrice = sv.calcNetBuyPrice(10000, new BigDecimal("2.00"));
        assertEquals("Net Buy Price Incorrect.", 20059.00, netBuyPrice.doubleValue(), 0d);
        
        checkBuyPrice(20000,"3.16", 63386.44);
    }
    
    JTraderSvBase sv = new JTraderSvBase();

    private void checkBuyPrice(int vol, String price, double expected) {

        BigDecimal netBuyPrice = sv.calcNetBuyPrice(vol, new BigDecimal(price));
        assertEquals("Net Buy Price Incorrect.", expected, netBuyPrice.doubleValue(), 0d);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.pse;

import java.math.BigDecimal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 *
 * @author Mr Jacky
 */
public class JTraderPSETest {
    
    public JTraderPSETest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetStockQuote() throws Exception {
        ProgressHandle ph = ProgressHandleFactory.createHandle("Test");
        BigDecimal ac = JTraderPSE.getStockQuote("AC",ph);
        System.out.println(ac);
        ph = ProgressHandleFactory.createHandle("Test");
        System.out.println(JTraderPSE.getStockQuote("PNX",ph));
    }
}

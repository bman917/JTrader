/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.citisec;

import java.io.FileReader;
import java.util.Scanner;
import org.junit.Test;

/**
 *
 * @author Mr Jacky
 */
public class CitisecInvoiceParserTest {
    
    @Test
    public void testLogin() throws Exception {
        CitisecInvoiceParser sv = new CitisecInvoiceParser();
        
        //System.out.println("Cookie: " + sv.getCookie().getValue());
        
        //String tradeHistory = sv.getTradeHistory();
        
        FileReader fr = new FileReader("trades.txt");
        String txt = new Scanner(fr).useDelimiter("\\A").next();
        sv.parseHistoricalInvoice(txt);
    }
}

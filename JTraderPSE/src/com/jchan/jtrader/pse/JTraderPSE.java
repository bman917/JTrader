/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.pse;

import java.math.BigDecimal;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Mr Jacky
 */
public class JTraderPSE {

    private static HttpHost targetHost = new HttpHost("www2.pse.com.ph", 80, "http");

    public static BigDecimal getStockQuote(String stock, ProgressHandle ph) throws Exception {

        debug(ph, "Preparing Http Request.", 10);
        HttpPost get = new HttpPost("/html/MarketInformation/stockinfo.jsp?securitySymbol=" + stock);
        HttpParams params = new BasicHttpParams();
        params.setParameter("securitySymbol", stock);
        get.setParams(params);

        DefaultHttpClient client = new DefaultHttpClient();

        debug(ph, "Sending Http Request to " + targetHost.toURI() + get.getURI(), 20);
        HttpResponse response = client.execute(targetHost, get);
        debug(ph, "Parsing HTTP Response.", 50);

        Pattern priceMarker = Pattern.compile(".+>Last Sale<.+");
        Pattern pricePattern = Pattern.compile(".+>\\s*([0123456789,.]{1,})\\s*<.+", Pattern.DOTALL);

        boolean markerFound = false;
        
        Scanner s = new Scanner(response.getEntity().getContent());
        s.useDelimiter("<td");
        String line = s.next();
        
        while (line != null) {

            if (markerFound) {
                System.out.println("Processing: ----" + line + "----");
                Matcher m = pricePattern.matcher(line);
                if (m.matches()) {
                    System.out.println("Price found on: " + line);
                    return new BigDecimal(m.group(1));
                }
            }

            if (line.contains(">Last Sale<")) {
                debug(ph, "Found Last Sale Marker.", 80);
                markerFound = true;
            }
            
            line = s.next();
        }

        return null;
    }

    private static void debug(ProgressHandle ph, String message, int unit) {
        ph.progress(message, unit);
        System.out.println(message);
    }
}

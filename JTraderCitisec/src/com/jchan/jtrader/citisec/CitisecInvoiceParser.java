package com.jchan.jtrader.citisec;

import com.jchan.jtrader.CitisecListener;
import com.jchan.jtrader.JTraderCitisec;
import com.jchan.jtrader.Util;
import com.jchan.jtrader.model.Mode;
import com.jchan.jtrader.model.Trade;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class CitisecInvoiceParser {

    private boolean loggedIn = false;

    public List<Trade> parseHistoricalInvoice(String text) throws Exception {

        debug("Starting parser....");

        SimpleDateFormat sdf = new SimpleDateFormat("MMMMM dd, yyyy");

        StringReader sr = new StringReader(text);
        BufferedReader br = new BufferedReader(sr);

        Pattern stockPattern = Pattern.compile("\\| STOCK\\s*:\\s*(\\w*)\\s*.+");
        Pattern datePattern = Pattern.compile("\\| TRX DATE\\s*:\\s*(.+)\\s*\\|");
        Pattern volPattern = Pattern.compile("\\|\\s*([0123456789,.]{1,})\\s*([0123456789,.]{1,}).+");
        //Pattern invoicePattern = Pattern.compile("\\| INVOICE\\s*:\\s*(.+)\\s*PRINT NO");
        Pattern invoicePattern = Pattern.compile("\\| INVOICE\\s*:\\s*(.+)\\s*PRINT NO.*");
        Pattern endVolPattern = Pattern.compile("\\|-----------.+");

        List<Trade> trades = new ArrayList<Trade>();
        Set<String> invoices = new HashSet<String>();

        String invoice = null;
        String date = null;
        String mode = null;
        String stock = null;
        String vol = null;
        String unitPrice = null;
        String line = br.readLine();
        do {
            Matcher stockMatcher = stockPattern.matcher(line);
            Matcher volMatcher = volPattern.matcher(line);
            Matcher dateMatcher = datePattern.matcher(line);
            Matcher invoiceMatcher = invoicePattern.matcher(line);
            Matcher endVolMatcher = endVolPattern.matcher(line);

            if (invoiceMatcher.matches()) {
                invoice = invoiceMatcher.group(1);

                if (invoices.contains(invoice) == false) {
                    invoices.add(invoice);
                    debug("Processing Invoice: " + invoice);
                } else {
                    debug("Skipping duplicate Invoice: " + invoice);
                    invoice = null;
                }
            }

            if (invoice != null && dateMatcher.matches()) {
                date = dateMatcher.group(1).trim();
                //debug(date);
            }

            if (invoice != null && date != null && line.matches("\\| TRANSACTION\\s*:\\s*BOUGHT.*")) {
                mode = "BUY";
               //  debug(line);
            }

            if (invoice != null && date != null && line.matches("\\| TRANSACTION\\s*:\\s*SOLD.*")) {
                mode = "SELL";
             //   debug(line);
            }

            if (invoice != null && mode != null && stockMatcher.matches()) {
                stock = stockMatcher.group(1);
             //    debug(invoice + " - " + stock);
            }

            if (invoice != null && volMatcher.matches()) {
                vol = volMatcher.group(1).replaceAll(",", "");
                unitPrice = volMatcher.group(2).replaceAll(",", "");
             //    debug("Vol: " + vol + ", Unit Price: " + unitPrice);
//                lastInvoice = invoice;
            }


            if (mode != null && stock != null && vol != null && unitPrice != null) {
                debug(date + " - " + mode + " - " + stock + " - " + vol + " @ " + unitPrice);

                Trade trade = new Trade();
                trade.setStock(stock);
                trade.setMode(Mode.valueOf(mode));
                trade.setDate(sdf.parse(date));
                Util.updateVol(trade, Integer.parseInt(vol));
                Util.setPrice(trade, new BigDecimal(unitPrice));
                trades.add(trade);
                unitPrice = null;
            }

            if (invoice != null && date != null && stock != null && vol != null && endVolMatcher.matches()) {
                debug("End Of Invoice " + invoice);
                invoice = null;
                date = null;
                stock = null;
                vol = null;
                unitPrice = null;
            }


            line = br.readLine();
        } while (line != null);

        return trades;
    }
    HttpHost targetHost = new HttpHost("www.citiseconline.com", 443, "https");
    CookieStore cookieStore = new BasicCookieStore();
    HttpContext localContext = new BasicHttpContext();

    public CitisecInvoiceParser() {
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    public Header getCookie() throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpPost post = new HttpPost("https://www.citiseconline.com/Final2/login/l_login_small.asp?CitiTradePage=");
        HttpResponse response = httpclient.execute(post, localContext);
        Header h1 = response.getFirstHeader("Set-Cookie");
        // System.out.println("cookieStore: " + cookieStore.getCookies());
        return h1;
    }

    public boolean login(String id1, String id2, String pwd) throws Exception {

        debug("Attempting to login to " + targetHost.toURI());

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost post = new HttpPost("/Final2/login/l_login_small.asp?CitiTradePage=");

        post.addHeader("Content-Type", "application/x-www-form-urlencoded");

        StringEntity e = new StringEntity("txtUser1=" + id1 + "&txtUser2=" + id2 + "&txtPassword=" + pwd + "&cmdLogOn=Log+In");
        post.setEntity(e);

        debug("Sending HTTP Login request....");

        HttpResponse response = httpclient.execute(targetHost, post, localContext);

        debug("Response Recieved...");

        String text = new Scanner(response.getEntity().getContent()).useDelimiter("\\A").next();
        log(text);
        debug("CookieStore: " + cookieStore.getCookies());
        for (Header h : response.getAllHeaders()) {
            debug("Header: " + h);
        }

        boolean loginResult = false;

        int status = response.getStatusLine().getStatusCode();

        Header firstHeader = response.getFirstHeader("Location");

        if (firstHeader != null) {
            String loc = "/Final2/" + firstHeader.getValue();
            while (status == 302) {

                debug("Redirect Requested by Host. Redirecting to " + loc);

                DefaultHttpClient client = new DefaultHttpClient();
                HttpPost rPost = new HttpPost(loc);
                HttpResponse rResp = client.execute(targetHost, rPost, localContext);
                status = rResp.getStatusLine().getStatusCode();

                if (rResp.getFirstHeader("Location") != null) {
                    loc = "/Final2/" + rResp.getFirstHeader("Location").getValue();
                    loginResult = loc.contains("welcome");
                } else {
                    debug("No Location Header Found. Aborting redirect.");
                    break;
                }
            }
        }

        debug("Login Successful: " + loginResult);
        loggedIn = loginResult;
        return loginResult;

        // login/h_Logindb.asp?CitiTradePage=
    }

    public String getHistoricalInvoice() throws Exception {

        if (loggedIn) {

            HttpGet post = new HttpGet("https://www.citiseconline.com/invoices/Historical/invoices.asp");

            DefaultHttpClient httpclient = new DefaultHttpClient();

            HttpResponse response = httpclient.execute(targetHost, post, localContext);

            String text = new Scanner(response.getEntity().getContent()).useDelimiter("\\A").next();
            log(text);

            FileWriter fw = new FileWriter("trades.txt");
            fw.write(text);
            fw.close();

            return text;
        } else {
            return "You must Login first";
        }
    }

    public List<Trade> downloadTradeHistory(String id1, String id2, String password) throws Exception {
        String txt = getHistoricalInvoice();
        if (txt != null) {
            return parseHistoricalInvoice(txt);
        } else {
            return null;
        }
    }

    public void log(String o) {
        System.out.println(o);
    }

    public void debug(String s) {
        System.out.println(s);
    }
}
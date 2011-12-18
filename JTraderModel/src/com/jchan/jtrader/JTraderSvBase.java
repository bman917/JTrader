/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader;

import com.jchan.jtrader.model.Mode;
import com.jchan.jtrader.model.Trade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.ServiceLoader;

/**
 *
 * @author Mr Jacky
 */
public class JTraderSvBase {

    int scale = 5;
    BigDecimal citisecCommision = new BigDecimal("0.0025");
    BigDecimal sccpFee = new BigDecimal("0.0001");
    BigDecimal transactionTax = new BigDecimal("0.005");
    BigDecimal vat = new BigDecimal("0.12");
    BigDecimal pseCharge = new BigDecimal("0.01");
    BigDecimal pseTranseFee = new BigDecimal("0.00005");

    public Trade createTrade(Date date, String stock, Mode mode, int volume, BigDecimal price) {

        Trade trade = new Trade(stock, date, mode, volume);

        BigDecimal gross = calcGrossPrice(volume, price);
        BigDecimal fees = calcFees(gross, mode);

        trade.setNetPrice(gross.add(fees));
        trade.setGrossPrice(gross);

        return trade;
    }

    public BigDecimal calcGrossPrice(int volume, BigDecimal marketPrice) {
        if (marketPrice != null) {
            return marketPrice.multiply(new BigDecimal(volume)).setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal calcNetBuyPrice(int volume, BigDecimal price) {

        BigDecimal grossPrice = calcGrossPrice(volume, price);
        BigDecimal fees = calcFees(grossPrice, Mode.BUY);
        return grossPrice.add(fees);
    }

    public BigDecimal calcNetSellPrice(int volume, BigDecimal price) {

        BigDecimal grossPrice = calcGrossPrice(volume, price);
        BigDecimal fees = calcFees(grossPrice, Mode.SELL);
        return grossPrice.add(fees);
    }

    public BigDecimal calcFees(BigDecimal grossPrice, Mode mode) {

        BigDecimal fees = BigDecimal.ZERO;

        BigDecimal commision = grossPrice.multiply(citisecCommision);
        
        if (commision.doubleValue() < 20d)
        {
            commision = new BigDecimal("20.00");
        }
        BigDecimal commisionVat = commision.multiply(vat);
        BigDecimal sccp = grossPrice.multiply(sccpFee);
        BigDecimal transTax = grossPrice.multiply(transactionTax);
        BigDecimal pse = grossPrice.multiply(pseTranseFee);
        
        
        if (Mode.SELL == mode) {
            fees = fees.subtract(commision).subtract(commisionVat).subtract(sccp).subtract(transTax).subtract(pse);

        } else if (Mode.BUY == mode) {
            fees = fees.add(commision).add(commisionVat).add(sccp).add(pse);
        }

       // System.out.println("Sell: gross    : " + grossPrice);
       // System.out.println("Sell: commision: " + commision);
       // System.out.println("Sell: VAT      : " + commisionVat);
       // System.out.println("Sell: PSE      : " + pse);
      //  System.out.println("Sell: SCCP     : " + sccp);

        return fees.setScale(2, RoundingMode.HALF_UP);
    }
}

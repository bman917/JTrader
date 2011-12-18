/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader;

import com.jchan.jtrader.model.Mode;
import com.jchan.jtrader.model.Stock;
import com.jchan.jtrader.model.Trade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ServiceLoader;

/**
 *
 * @author Mr Jacky
 */
public final class Util {

    static JTraderSvBase sv = new JTraderSvBase();

    public static void setPrice(Trade t, BigDecimal marketPrice) {
        int vol = t.getVolume();
        BigDecimal gross = sv.calcGrossPrice(vol, marketPrice);
        BigDecimal fees = sv.calcFees(gross, t.getMode());

        t.setPrice(marketPrice);
        t.setGrossPrice(gross);
        t.setNetPrice(gross.add(fees));
    }

    public static void updateVol(Trade t, int vol) {
        t.setVolume(vol);
        if (t.getPrice() != null) {

            BigDecimal gross = sv.calcGrossPrice(vol, t.getPrice());
            BigDecimal fees = sv.calcFees(gross, t.getMode());

            t.setGrossPrice(gross);
            t.setNetPrice(gross.add(fees));
        }
    }

    public static JTraderDatabaseSv getJTraderDatabaseSv() {
        return ServiceLoader.load(JTraderDatabaseSv.class).iterator().next();
    }

    public static BigDecimal getOverAllGain(String stockCode) {

        BigDecimal sellTotal = getJTraderDatabaseSv().getNetPriceSum(stockCode, Mode.SELL);
        BigDecimal buyTotal = getJTraderDatabaseSv().getNetPriceSum(stockCode, Mode.BUY);

        return sellTotal.subtract(buyTotal);
    }

    public static BigDecimal getCurrentNetPrice(String stockCode) {

        Stock stock = getJTraderDatabaseSv().getStock(stockCode);

        BigDecimal marketPrice = stock.getMarketValue();
        int vol = getCurrentVolume(stockCode);

        BigDecimal grossSellAmt = sv.calcGrossPrice(vol, marketPrice);
        BigDecimal sellFeeAmt = sv.calcFees(grossSellAmt, Mode.BUY);


        BigDecimal sellTotal = getJTraderDatabaseSv().getNetPriceSum(stockCode, Mode.SELL);
        BigDecimal buyTotal = getJTraderDatabaseSv().getNetPriceSum(stockCode, Mode.BUY);

        BigDecimal purchaseAmount = buyTotal.subtract(sellTotal);

        return grossSellAmt.subtract(purchaseAmount).subtract(sellFeeAmt);

    }

    /**
     * Calculate the number of stock currently owned.
     * 
     * @param stockCode
     * @param sv
     * @return 
     */
    public static int getCurrentVolume(String stockCode) {

        int buy = getJTraderDatabaseSv().getVolumeSum(stockCode, Mode.BUY);
        int sell = getJTraderDatabaseSv().getVolumeSum(stockCode, Mode.SELL);

        return (buy - sell);
    }
}

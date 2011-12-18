/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.model;

import com.jchan.jtrader.JTraderDatabaseSv;
import com.jchan.jtrader.JTraderSvBase;
import com.jchan.jtrader.Util;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mr Jacky
 */
public class StockStats {

    String stockCode;
    int totalSellVolume;
    int totalBuyVolume;
    int volumeInHand;
    BigDecimal marketValue;
    BigDecimal sellNetPrice;
    BigDecimal buyNetPrice;
    BigDecimal aveBuyPrice;
    BigDecimal aveSellPrice;
    BigDecimal overAllGain;
    BigDecimal currentNetPrice;
    private static Map<String, StockStats> cache = new HashMap<String, StockStats>();
    static JTraderSvBase svBase = new JTraderSvBase();
    
    public StockStats(String stockCode) {
        this.stockCode = stockCode;
    }

    public static StockStats getStats(String stockCode) {

        StockStats stats = cache.get(stockCode);

        if (stats == null) {
            stats = new StockStats(stockCode);
            cache.put(stockCode, stats);
        }
        
        return stats;
    }

    public void calculateStats(String stockCode) {

        JTraderDatabaseSv sv = Util.getJTraderDatabaseSv();

        Stock stock = sv.getStock(stockCode);

        /*
         * Volume Calculations
         */
        totalBuyVolume = sv.getVolumeSum(stockCode, Mode.BUY);
        totalSellVolume = sv.getVolumeSum(stockCode, Mode.SELL);
        volumeInHand = totalBuyVolume - totalSellVolume;

        marketValue = stock.getMarketValue();
        sellNetPrice = sv.getNetPriceSum(stockCode, Mode.SELL);
        buyNetPrice = sv.getNetPriceSum(stockCode, Mode.BUY);

        BigDecimal buyVol = new BigDecimal(totalBuyVolume);
        aveBuyPrice = divide(buyNetPrice, buyVol);

        BigDecimal sellVol = new BigDecimal(totalSellVolume);
        aveSellPrice = divide(sellNetPrice, sellVol);

        overAllGain = sellNetPrice.subtract(buyNetPrice);

        BigDecimal currentPurchaseAmount = BigDecimal.ZERO;
        currentNetPrice = BigDecimal.ZERO;
        if (volumeInHand > 0) {
            currentPurchaseAmount = buyNetPrice.subtract(sellNetPrice);
            BigDecimal grossSellAmt = svBase.calcGrossPrice(volumeInHand, marketValue);
            BigDecimal sellFeeAmt = svBase.calcFees(grossSellAmt, Mode.BUY);
            currentNetPrice = grossSellAmt.subtract(currentPurchaseAmount).subtract(sellFeeAmt);


            BigDecimal onHandMarketPrice = grossSellAmt.subtract(sellFeeAmt);
            overAllGain = sellNetPrice.subtract(buyNetPrice).add(onHandMarketPrice);
        }
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public BigDecimal getAveBuyPrice() {
        return aveBuyPrice;
    }

    public BigDecimal getAveSellPrice() {
        return aveSellPrice;
    }

    public BigDecimal getBuyNetPrice() {
        return buyNetPrice;
    }

    public BigDecimal getCurrentNetPrice() {
        return currentNetPrice;
    }

    public BigDecimal getOverAllGain() {
        return overAllGain;
    }

    public BigDecimal getSellNetPrice() {
        return sellNetPrice;
    }

    public String getStockCode() {
        return stockCode;
    }

    public int getTotalBuyVolume() {
        return totalBuyVolume;
    }

    public int getTotalSellVolume() {
        return totalSellVolume;
    }

    public int getVolumeInHand() {
        return volumeInHand;
    }

    private BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        if (dividend.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        } else {
            return dividend.divide(divisor, 2, RoundingMode.HALF_UP);
        }
    }
}

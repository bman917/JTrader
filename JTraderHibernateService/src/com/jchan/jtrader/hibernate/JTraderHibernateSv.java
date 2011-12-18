/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.hibernate;

import com.bman917.jpersonic.Jpersonic;
import com.jchan.jtrader.JTraderDatabaseSv;
import com.jchan.jtrader.model.Mode;
import com.jchan.jtrader.model.Stock;
import com.jchan.jtrader.model.Trade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Mr Jacky
 */
public class JTraderHibernateSv implements JTraderDatabaseSv {

    String dataDir = "target/data";
    String dbName = "JTraderDB";
    private static SessionFactory sessionFactory = null;

    public JTraderHibernateSv() {
        try {

            dataDir = System.getProperty("com.jchan.jtrader.JTraderHibernateSv.dataDir", "target/data");
            dbName = System.getProperty("com.jchan.jtrader.JTraderHibernateSv.dbName", "JTraderDB");
            Jpersonic.startHypersonicDB(dataDir, dbName);

            try {
                // Create the SessionFactory from standard (hibernate.cfg.xml) 
                // config file.
                sessionFactory = new Configuration().configure().buildSessionFactory();

            } catch (Throwable ex) {
                // Log the exception. 
                System.err.println("Initial SessionFactory creation failed." + ex);
                throw new ExceptionInInitializerError(ex);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void saveTrades(List<Trade> trades) {
        Session session = sessionFactory.getCurrentSession();

        session.beginTransaction();

        for (int i = 0; i < trades.size(); i++) {
            session.save(trades.get(i));
            updateStock(session, trades.get(i));

            if (i % 20 == 0) {
                session.flush();
                session.clear();
            }
        }
        session.getTransaction().commit();
    }

    @Override
    public void saveTrade(Trade trade) {

        Session session = sessionFactory.getCurrentSession();

        session.beginTransaction();
        session.save(trade);

        updateStock(session, trade);

        session.getTransaction().commit();
    }

    private void updateStock(Session session, Trade trade) {
        if (trade.getStock() != null && trade.getStock().length() > 0) {
            Stock s = (Stock) session.get(Stock.class, trade.getStock());

            if (s == null) {
                System.out.println("Adding Stock: " + trade.getStock());
                s = new Stock();
                s.setCode(trade.getStock());
                s.setMarketValue(trade.getPrice());
                session.save(s);
            } else if (trade.getPrice() != null && trade.getPrice().doubleValue() > 0) {
                System.out.println("Updating Stock: " + s.getCode());
                s.setMarketValue(trade.getPrice());
                session.update(s);
            }
        }
    }

    @Override
    public List<Trade> getAllTrades() {

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        Criteria c = session.createCriteria(Trade.class);
        List<Trade> list = c.list();
        session.close();
        return list;
    }

    @Override
    public void deleteAllTrades() {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        int deleted = session.createQuery("delete from Trade").executeUpdate();
        session.getTransaction().commit();
    }

    @Override
    public void deleteTrade(Long id) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        int deleted = session.createQuery("delete from Trade where id = " + id).executeUpdate();
        session.getTransaction().commit();
    }

    @Override
    public List<Trade> getTradeByStock(String[] stock) {

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria c = session.createCriteria(Trade.class);
        c.add(Restrictions.in("stock", stock));
        List<Trade> list = c.list();
        session.close();
        return list;
    }

    @Override
    public void saveStock(Stock stock) {
        Session session = sessionFactory.getCurrentSession();

        session.beginTransaction();
        session.save(stock);

        session.getTransaction().commit();
    }

    @Override
    public List<Stock> getAllStocks() {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria c = session.createCriteria(Stock.class);
        List<Stock> list = c.list();
        session.close();
        return list;
    }

    @Override
    public void deleteAllStocks() {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        int deleted = session.createQuery("delete from Stock").executeUpdate();
        session.getTransaction().commit();
    }

    @Override
    public BigDecimal getNetPriceSum(String stock, Mode mode) {
        return getSum(stock, "netPrice", mode);
    }

    @Override
    public int getVolumeSum(String stock, Mode mode) {
        return getSum(stock, "volume", mode).intValue();
    }

    public BigDecimal getSum(String stock, String field, Mode mode) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        final String sql = "select sum(" + field + ") from Trade trade where trade.mode = :mode and trade.stock = :stock";

        Query q = session.createQuery(sql);
        q.setParameter("mode", mode);
        q.setParameter("stock", stock);

        List l = q.list();

        if (l.get(0) == null) {
            return BigDecimal.ZERO;
        } else {
            System.out.println("Net " + mode + " sume is : " + l.get(0).toString());
            BigDecimal sum = new BigDecimal(l.get(0).toString());
            return sum.setScale(2, RoundingMode.HALF_EVEN);
        }
    }

    @Override
    public int getAvailVolume(String stock) {
        BigDecimal buy = getSum(stock, "volume", Mode.BUY);
        BigDecimal sell = getSum(stock, "volume", Mode.SELL);
        return buy.subtract(sell).intValue();
    }

    @Override
    public BigDecimal getPurchaseAmount(String stock) {
        BigDecimal buy = getSum(stock, "netPrice", Mode.BUY);
        BigDecimal sell = getSum(stock, "netPrice", Mode.SELL);
        return buy.subtract(sell);
    }

    @Override
    public BigDecimal getAvePrice(String stock) {
        BigDecimal amt = getPurchaseAmount(stock);
        if (amt != null) {
            amt = amt.setScale(4, RoundingMode.HALF_UP);
            BigDecimal vol = new BigDecimal(getAvailVolume(stock));
            vol = vol.setScale(4, RoundingMode.HALF_UP);
            if (vol.intValue() > 0) {
                return amt.divide(vol, 2, RoundingMode.HALF_UP);
            } else {
                return BigDecimal.ZERO;
            }
        } else {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public BigDecimal getAveBuyAmount(String stock) {
        return getAve(stock, Mode.BUY);
    }

    @Override
    public BigDecimal getAveSellAmount(String stock) {
        return getAve(stock, Mode.SELL);
    }

    public BigDecimal getAve(String stock, Mode mode) {
        BigDecimal amt = getSum(stock, "netPrice", mode);
        if (amt != null) {
            amt = amt.setScale(4, RoundingMode.HALF_UP);
            BigDecimal vol = new BigDecimal(getVolumeSum(stock, mode));
            vol = vol.setScale(4, RoundingMode.HALF_UP);
            if (vol.intValue() > 0) {
                return amt.divide(vol, 2, RoundingMode.HALF_UP);
            } else {
                return BigDecimal.ZERO;
            }
        } else {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public void deleteTrades(List<Trade> trades) {

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        for (Trade t : trades) {
            if (t != null && t.getId() != null) {
                session.createQuery("delete from Trade where id = " + t.getId()).executeUpdate();
            }
        }

        session.getTransaction().commit();
    }

    @Override
    public void updateTrade(Trade trade) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        session.update(trade);
        updateStock(session, trade);
        session.getTransaction().commit();
    }

    @Override
    public List<String> getStocks() {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<String> stocks = session.createQuery("select distinct trade.stock from Trade trade").list();
        session.getTransaction().commit();
        return stocks;
    }

    @Override
    public void updateStock(Stock stock) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        session.update(stock);
        session.getTransaction().commit();
    }

    @Override
    public Stock getStock(String stockCode) {
        Stock stock = null;
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        stock = (Stock) session.get(Stock.class, stockCode);

        if (stock == null) {
            stock = new Stock();
            stock.setCode(stockCode);
            session.save(stock);
        }

        return stock;
    }
}
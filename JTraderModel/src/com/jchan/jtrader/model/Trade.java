/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.model;


import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 *
 * @author Mr Jacky
 */
@Entity
public class Trade implements Serializable {
    
    Long id;
    int volume;
    Date date;
    String stock;
    Mode mode;
    BigDecimal price;
    BigDecimal netPrice;
    BigDecimal grossPrice;
    
    final SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyyyy");
    
    public Trade(){
        
    }
    
    public Trade(String stock, Date date, Mode mode, int volume) {
        this.stock = stock;
        this.date = date;
        this.mode = mode;
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getGrossPrice() {
        return grossPrice;
    }

    public void setGrossPrice(BigDecimal grossPrice) {
        this.grossPrice = grossPrice;
    }

    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Enumerated (EnumType.STRING)
    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public BigDecimal getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(BigDecimal netPrice) {
        this.netPrice = netPrice;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }
    
    public String getDateAsString() {
        return (this.getDate() != null) ? sdf.format(this.getDate()).toUpperCase() : "";
    }
    
    public void setDateAsString(String date) {
        try {
        this.setDate(sdf.parse(date));
        } catch (ParseException pe) {
            throw new IllegalArgumentException(pe);
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String toString() {
        return this.getId() + "-" + this.getStock() + "-" + getMode() + "-" + getVolume() + "-" + getPrice();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader;

import java.util.ServiceLoader;

/**
 *
 * @author Mr Jacky
 */
public abstract class Logger {
    
    public abstract void debug(Object o);
    
    public static Logger getInstance(){
        return ServiceLoader.load(Logger.class).iterator().next();
    }
 }

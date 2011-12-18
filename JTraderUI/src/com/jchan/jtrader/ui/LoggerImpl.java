/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jchan.jtrader.ui;

import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Mr Jacky
 */
public class LoggerImpl extends com.jchan.jtrader.Logger {
    
    private static InputOutput io = IOProvider.getDefault().getIO(Constants.OUTPUT, false);

    @Override
    public void debug(Object o) {
        io.getOut().println(o);
    }
    
}

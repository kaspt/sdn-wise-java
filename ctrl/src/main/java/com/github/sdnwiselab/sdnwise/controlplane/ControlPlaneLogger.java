/*
 * Copyright (C) 2016 Seby
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.sdnwiselab.sdnwise.controlplane;

import com.github.sdnwiselab.sdnwise.util.FileLogFormatter;
import com.github.sdnwiselab.sdnwise.util.SimplerFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.logging.*;

/**
 * Models a logger for each ControlPlane layer.
 *
 * @author Sebastiano Milardo
 */
public final class ControlPlaneLogger {

    /**
     * Private constructor.
     */
    private ControlPlaneLogger() {
        // Nothing to do here
    }

    /**
     * To avoid garbage collector.
     */
    private final static Logger logger = setupFileLogger();


    /**
     * Creates a logger using the SimplerFormatter formatter.
     *
     * @param prefix the name of the ControlPlane class
     */
    public static void setupLogger(final String prefix) {

        Logger logger = Logger.getLogger(prefix);
        logger.setUseParentHandlers(false);
        SimplerFormatter f = new SimplerFormatter(prefix);
        StreamHandler h = new StreamHandler(System.out, f) {
            @Override
            public synchronized void publish(final LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        if(logger.getHandlers().length == 0) {
            logger.addHandler(h);
        }
    }

    public static synchronized void LogTimeStamp(String message){
        logger.log(Level.INFO, message);
    }

    private static Logger setupFileLogger(){
        Logger logger = Logger.getLogger("timestamp");
        try {
            FileHandler fh = new FileHandler(
                    Paths.get("logs")
                            + File.separator
                            + "webpacketsinout-"
                            + LocalDate.now().toString()
                            + ".log");
            fh.setFormatter(new FileLogFormatter());
            logger.addHandler(fh);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }


}

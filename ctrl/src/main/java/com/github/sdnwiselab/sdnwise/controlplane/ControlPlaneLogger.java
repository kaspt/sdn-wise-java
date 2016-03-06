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

import com.github.sdnwiselab.sdnwise.util.SimplerFormatter;
import java.util.logging.*;

/**
 * @author Sebastiano Milardo
 */
public class ControlPlaneLogger {

    public static void setupLogger(String prefix) {

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
        logger.addHandler(h);
    }
}

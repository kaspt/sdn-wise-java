package com.github.sdnwiselab.sdnwise.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class FileLogFormatter extends Formatter {

    private final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:ms");

    @Override
    public final String format(final LogRecord record) {

        StringBuilder sb = new StringBuilder(formatter
                .format(new Date(record.getMillis())));

        sb.append(" ns: ")
                .append(System.nanoTime())
                .append(" : ")
                .append(formatMessage(record));

        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            sb.append(sw.toString());
        }
        return sb.append("\n").toString();
    }
}

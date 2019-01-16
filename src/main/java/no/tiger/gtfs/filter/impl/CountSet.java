package no.tiger.gtfs.filter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;

class CountSet<T> extends HashSet<T> {
    private static final Logger LOG = LoggerFactory.getLogger(HashSet.class);

    private final String name;

    private int lastChangeSize = 0;


    CountSet(String name) {
        this.name = name;
    }

    boolean logChanged() {
        int size = size();
        if(lastChangeSize == size) return false;
        LOG.info(
                "  - {}: {} of {} => {}",
                String.format("%-14s", name),
                format(lastChangeSize-size),
                format(lastChangeSize),
                format(size)
        );
        lastChangeSize = size;
        return true;
    }

    private String format(int number) {
        NumberFormat df = DecimalFormat.getIntegerInstance();

        if(number >= 100_000_000) {
            return df.format(number/1_000_000) + "\"";
        }
        if(number >= 100_000) {
            return df.format(number/1_000) + "\'";
        }
        return df.format(number);
    }

    void resetChangeTracking() {
        lastChangeSize = size();
    }
}

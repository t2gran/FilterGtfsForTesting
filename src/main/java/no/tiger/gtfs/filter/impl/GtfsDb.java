package no.tiger.gtfs.filter.impl;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs.services.GtfsDao;

import java.io.File;
import java.io.IOException;

public class GtfsDb {
    public static GtfsDao loadGtfs(File inputFile) throws IOException {
        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(inputFile);

        GtfsDaoImpl dao = new GtfsDaoImpl();
        reader.setEntityStore(dao);
        reader.run();
        return dao;
    }

    public static void save(GtfsDao dao, File outputFile) throws IOException {
        GtfsWriter writer = new GtfsWriter();
        writer.setOutputLocation(outputFile);
        writer.run(dao);
        writer.close();
    }
}

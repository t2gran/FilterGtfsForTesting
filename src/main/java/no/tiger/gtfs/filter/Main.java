package no.tiger.gtfs.filter;

import no.tiger.gtfs.filter.impl.Box;
import no.tiger.gtfs.filter.impl.FileUtils;
import no.tiger.gtfs.filter.impl.GtfsDb;
import no.tiger.gtfs.filter.impl.GtfsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static no.tiger.gtfs.filter.impl.GtfsDb.loadGtfs;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String INPUT_FILE = "gtfs.zip";
    private static final String OUTPUT_DIR = "out";
    private static final Box OSLO_RING2 = new Box("oslo-ring2", 59.90, 10.70, 59.94, 10.79);

    private final String rootDir;
    private final GtfsModel gtfs;
    private final Box mainArea;
    private final File outputDir;

    static public void main(String... args) throws Exception {
        assertOneArg(args);
        Main main = new Main(args[0], OSLO_RING2);
        main.filter();
        main.save();
    }

    private Main(String rootDir, Box mainArea) throws IOException {
        this.rootDir = rootDir;
        this.mainArea = mainArea;
        this.outputDir = new File(rootDir, OUTPUT_DIR);
        File inputFile = new File(rootDir, INPUT_FILE);

        this.gtfs = new GtfsModel(loadGtfs(inputFile));
        FileUtils.createOutputDirectory(outputDir);
    }

    /**
     * Change this method to filter the GTFS file set.
     */
    private void filter() {
        LOG.info("FILTER [start]");
        gtfs.retainAgencies("RuterBuss", "RuterTrikk", "RuterTBane", "Tog");
        gtfs.retainRoutes("11", "12", "13", "17", "4", "5");
        gtfs.retainStops(mainArea);
        gtfs.cleanupAll();
        gtfs.setServiceEndDate(2049, 12,31);
        LOG.info("FILTER [end]");
    }

    private void save() throws IOException {
        // Save gtfs files to output directory
        GtfsDb.save(gtfs, outputDir);

        // Create GTFS Zip file
        String targetZipFilename = new File(rootDir, "gtfs-" + mainArea.getName() + ".zip" ).getAbsolutePath();
        FileUtils.compress(outputDir.getAbsolutePath(), targetZipFilename);
    }

    private static void assertOneArg(String[] args) {
        if(args.length != 1) {
            System.err.println("Use the input data folder as argument to this program.");
            System.exit(-1);
        }
    }
}

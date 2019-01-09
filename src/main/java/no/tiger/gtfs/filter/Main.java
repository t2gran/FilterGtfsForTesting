package no.tiger.gtfs.filter;

import no.tiger.gtfs.filter.impl.Box;
import no.tiger.gtfs.filter.impl.GtfsDaoFilter;
import no.tiger.gtfs.filter.impl.GtfsDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static no.tiger.gtfs.filter.impl.GtfsDb.loadGtfs;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String INPUT_FILE = "gtfs.zip";
    private static final String OUTPUT_DIR = "out";
    private static final Box OSLO_RING2 = new Box("Oslo-Ring2", 59.90, 10.70, 59.94, 10.79);

    private final String rootDir;
    private final GtfsDaoFilter gtfs;

    static public void main(String... args) throws Exception {
        assertOneArg(args);
        Main main = new Main(args[0]);
        main.setup();
        main.filter();
        main.save();
    }

    private Main(String rootDir) {
        this.rootDir = rootDir;
        this.gtfs = new GtfsDaoFilter();
    }

    private void setup() throws IOException {
        gtfs.init(loadGtfs(inputFile()));
        createOutputDirectory();
    }

    /**
     * Change this method to filter the GTFS file set.
     */
    private void filter() {
        gtfs.retainAgencies("RuterBuss", "RuterTrikk", "RuterTBane", "Tog");
        gtfs.retainRoutes("11", "12", "13", "17", "4", "5");
        gtfs.retainStops(OSLO_RING2);
        gtfs.setServiceEndDate(2049, 12,31);
        gtfs.cleanupAll();
    }

    private void save() throws IOException {
        GtfsDb.save(gtfs, outputDir());
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    private void createOutputDirectory() {
        File dir = outputDir();
        dir.mkdirs();
        for (File file : dir.listFiles()) {
            LOG.info("File exist - delete: " + file.getName());
            file.delete();
        }
    }

    private static void assertOneArg(String[] args) {
        if(args.length != 1) {
            System.err.println("Use the input data folder as argument to this program.");
            System.exit(-1);
        }
    }

    private File inputFile() {
        return new File(rootDir, INPUT_FILE);
    }

    private File outputDir() {
        return new File(rootDir, OUTPUT_DIR);
    }
}

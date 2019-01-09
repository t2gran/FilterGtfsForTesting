package no.tiger.gtfs.filter.impl;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static no.tiger.gtfs.filter.impl.Functions.mapCount;
import static no.tiger.gtfs.filter.impl.Functions.noMatch;
import static no.tiger.gtfs.filter.impl.Functions.setOf;

public class GtfsDaoFilter implements GtfsDao {
    private static final Logger LOG = LoggerFactory.getLogger(GtfsDaoFilter.class);
    private static final int QUAY_TYPE = 0;
    private static final int STATION_TYPE = 1;

    private Set<Agency> agencies = new HashSet<>();
    private Set<ServiceCalendar> calendars = new HashSet<>();
    private Set<ServiceCalendarDate> calendarDates = new HashSet<>();
    private Set<FeedInfo> feedInfos = new HashSet<>();
    private Set<Route> routes = new HashSet<>();
    private Set<Stop> stops = new HashSet<>();
    private Set<StopTime> stopTimes = new HashSet<>();
    private Set<Transfer> transfers = new HashSet<>();
    private Set<Trip> trips = new HashSet<>();

    public void init(GtfsDao dao) {
        this.agencies.addAll(dao.getAllAgencies());
        this.calendars.addAll(dao.getAllCalendars());
        this.calendarDates.addAll(dao.getAllCalendarDates());
        this.feedInfos.addAll(dao.getAllFeedInfos());
        this.routes.addAll(dao.getAllRoutes());
        this.stops.addAll(dao.getAllStops());
        this.stopTimes.addAll(dao.getAllStopTimes());
        this.transfers.addAll(dao.getAllTransfers());
        this.trips.addAll(dao.getAllTrips());
    }

    /**
     * Set end date for all calendar services
     */
    public void setServiceEndDate(int year, int mnd, int day) {
        ServiceDate endDate = new ServiceDate(year, mnd, day);
        calendars.forEach(c -> c.setEndDate(endDate));
    }

    /**
     * Keep the only the included agencies,
     * cascade removal: Agency > Route > Trip > StopTimes
     */
    public void retainAgencies(String ... includeNames) {
        LOG.info("Remove all routes except: " + Arrays.toString(includeNames));
        agencies.removeIf(noMatch(Agency::getName, includeNames));
        cascadeAgenciesDeleted();
    }

    /**
     * Keep the only the included agencies,
     * cascade removal to Route > Trip > StopTimes
     */
    public void retainRoutes(String ... includeShortNames) {
        LOG.info("Remove all routes except: " + Arrays.toString(includeShortNames));
        routes.removeIf(noMatch(Route::getShortName, includeShortNames));
        cascadeRoutesDeleted();
    }

    /**
     * Keep the only stops within the bounding box,
     */
    public void retainStops(Box box) {
        LOG.info("Remove stops outside box: " + box);
        stops.removeIf(box::outside);
    }

    /**
     * - Remove all StopTimes where there is no Stops
     * - Remove all Trips with 0 or 1 StopTime (cascade to StopTimes)
     * - Remove Routes without Trips
     * - Remove Services (Calendar and Dates) without Trips
     * - Remove all Stops without StopTimes
     */
    public void cleanupAll() {
        LOG.info("Remove all StopTimes where there is no Stops");
        stopTimes.removeIf(st -> !stops.contains(st.getStop()));

        LOG.info("Remove all Trips with 0 or 1 StopTime (cascade to StopTimes)");
        Map<Trip, Integer> tripCount = mapCount(stopTimes, StopTime::getTrip);
        trips.removeIf(t -> tripCount.getOrDefault(t, 0) < 2);

        LOG.info("Remove Routes without Trips");
        Set<Route> routesInTrips = setOf(trips, Trip::getRoute);
        routes.removeIf(r -> !routesInTrips.contains(r));

        LOG.info("Remove Services without Trips");
        Set<AgencyAndId> serviceIdsInTrips = setOf(trips, Trip::getServiceId);
        calendars.removeIf(c -> !serviceIdsInTrips.contains(c.getServiceId()));
        calendarDates.removeIf(c -> !serviceIdsInTrips.contains(c.getServiceId()));

        LOG.info("Remove all Stops without StopTimes");
        Set<Stop> stopInTrips = setOf(stopTimes, StopTime::getStop);
        stops.removeIf(s -> isQuay(s) && !stopInTrips.contains(s));
        Set<String> parentStops = setOf(stops, Stop::getParentStation);
        stops.removeIf(s -> isStation(s) && !parentStops.contains(s.getId().getId()));
    }

    private void cascadeAgenciesDeleted() {
        routes.removeIf(t -> !agencies.contains(t.getAgency()));
        cascadeRoutesDeleted();
    }

    private void cascadeRoutesDeleted() {
        trips.removeIf(t -> !routes.contains(t.getRoute()));
        cascadeTripsDeleted();
    }

    private void cascadeTripsDeleted() {
        stopTimes.removeIf(s -> !trips.contains(s.getTrip()));
    }

    private boolean isQuay(Stop stop) { return stop.getLocationType() == QUAY_TYPE; }
    private boolean isStation(Stop stop) { return stop.getLocationType() == STATION_TYPE; }

    @Override public <T> Collection<T> getAllEntitiesForType(Class<T> type) {
        if(type == Agency.class) return (Collection<T>) agencies;
        if(type == ServiceCalendar.class) return (Collection<T>) calendars;
        if(type == ServiceCalendarDate.class) return (Collection<T>) calendarDates;
        if(type == FeedInfo.class) return (Collection<T>) feedInfos;
        if(type == Route.class) return (Collection<T>) routes;
        if(type == Stop.class) return (Collection<T>) stops;
        if(type == StopTime.class) return (Collection<T>) stopTimes;
        if(type == Transfer.class) return (Collection<T>) transfers;
        if(type == Trip.class) return (Collection<T>) trips;
        return Collections.emptyList();
    }

    @Override public Collection<Agency> getAllAgencies() { return agencies; }
    @Override public Collection<ServiceCalendar> getAllCalendars() { return calendars; }
    @Override public Collection<ServiceCalendarDate> getAllCalendarDates() { return calendarDates; }
    @Override public Collection<FeedInfo> getAllFeedInfos() { return feedInfos; }
    @Override public Collection<Route> getAllRoutes() { return routes; }
    @Override public Collection<Stop> getAllStops() { return stops; }
    @Override public Collection<StopTime> getAllStopTimes() { return stopTimes; }
    @Override public Collection<Trip> getAllTrips() { return trips; }
    @Override public Collection<FareAttribute> getAllFareAttributes() { return Collections.emptyList(); }
    @Override public Collection<FareRule> getAllFareRules() { return Collections.emptyList(); }
    @Override public Collection<Frequency> getAllFrequencies() { return Collections.emptyList(); }
    @Override public Collection<Pathway> getAllPathways() { return Collections.emptyList(); }
    @Override public Collection<ShapePoint> getAllShapePoints() { return Collections.emptyList(); }
    @Override public Collection<Transfer> getAllTransfers() { return Collections.emptyList(); }
    @Override public Agency getAgencyForId(String id)  { throw new IllegalStateException(); }
    @Override public ServiceCalendar getCalendarForId(int id) { throw new IllegalStateException(); }
    @Override public ServiceCalendarDate getCalendarDateForId(int id) { throw new IllegalStateException(); }
    @Override public FareAttribute getFareAttributeForId(AgencyAndId id) { throw new IllegalStateException(); }
    @Override public FareRule getFareRuleForId(int id) { throw new IllegalStateException(); }
    @Override public FeedInfo getFeedInfoForId(int id) { throw new IllegalStateException(); }
    @Override public Frequency getFrequencyForId(int id) { throw new IllegalStateException(); }
    @Override public Pathway getPathwayForId(AgencyAndId id) { throw new IllegalStateException(); }
    @Override public Route getRouteForId(AgencyAndId id) { throw new IllegalStateException(); }
    @Override public ShapePoint getShapePointForId(int id) { throw new IllegalStateException(); }
    @Override public Stop getStopForId(AgencyAndId id) { throw new IllegalStateException(); }
    @Override public StopTime getStopTimeForId(int id) { throw new IllegalStateException(); }
    @Override public Transfer getTransferForId(int id) { throw new IllegalStateException(); }
    @Override public Trip getTripForId(AgencyAndId id) { throw new IllegalStateException(); }
    @Override public <T> T getEntityForId(Class<T> type, Serializable id) { throw new IllegalStateException(); }
}

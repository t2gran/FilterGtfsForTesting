# FilterGtfsForTesting
A small Java program to read in a GTFS file set, filter it based on Agencies, Routes and Stops to create a small GTFS file for testing.

Excample code

```java
Box OSLO_RING2 = new Box("Oslo-Ring2", 59.90, 10.70, 59.94, 10.79);

gtfs.retainAgencies("RuterBuss", "RuterTrikk", "RuterTBane", "Tog");
gtfs.retainRoutes("11", "12", "13", "17", "4", "5");
gtfs.retainStops(OSLO_RING2);
gtfs.setServiceEndDate(2049, 12, 31);
gtfs.cleanupAll();
```
This creates a GTFS file set with only the given Agencies, Routes and Stops. Dangeling Trips, StopTimes, CalendarServices and CalendarServiceDates are deleted.

The tool uses the One Bus Away GTFS library for parsing and writing. I started with the Transformer also, but it was so slow that it to less time to code the logic than waiting for the transformer to comleate ;-)


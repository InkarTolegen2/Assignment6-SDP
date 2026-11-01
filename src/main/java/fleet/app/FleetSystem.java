package fleet.app;

import fleet.core.EventPublisher;
import fleet.event.DepotSummaryEvent;
import fleet.event.FleetEvent;
import fleet.event.FuelLevelEvent;
import fleet.event.GeofenceAlertEvent;
import fleet.event.GpsPositionEvent;
import fleet.event.InspectionReportEvent;
import fleet.event.LowFuelAlertEvent;
import fleet.geo.GeoPoint;
import fleet.integration.InspectionLog;
import fleet.integration.InspectionService;
import fleet.listener.DepotAggregator;
import fleet.listener.DepotDashboard;
import fleet.listener.EtaCalculator;
import fleet.listener.EventCounter;
import fleet.listener.FleetMap;
import fleet.listener.FuelMonitor;
import fleet.listener.GeofenceMonitor;
import fleet.model.Fleet;
import fleet.sim.FleetSimulator;

import java.util.Map;
import java.util.function.Consumer;

/**
 * The one composition root: this is the only place where objects are created and wired.
 * Everything else receives its collaborators through its constructor.
 */
public record FleetSystem(Fleet fleet, FleetSimulator simulator, EventPublisher<FleetEvent> alerts,
                          FleetMap map, EtaCalculator eta, DepotDashboard dashboard, InspectionLog inspections,
                          EventCounter<GpsPositionEvent> gpsCount, EventCounter<FuelLevelEvent> fuelCount,
                          EventCounter<GeofenceAlertEvent> geofenceAlerts,
                          EventCounter<LowFuelAlertEvent> lowFuelAlerts,
                          EventCounter<DepotSummaryEvent> summaries,
                          EventCounter<InspectionReportEvent> reportCount) {

    public static final double GEOFENCE_RADIUS_KM = 5.0;
    public static final double LOW_FUEL_LITERS = 20.0;
    public static final double SERVICE_THRESHOLD_KM = 8_000;
    public static final int SUMMARY_WINDOW = 10;
    private static final long SIMULATION_SEED = 2026;

    public static FleetSystem assemble(Consumer<Throwable> onListenerError) {
        Fleet fleet = DemoData.fleet();
        Map<String, GeoPoint> depots = DemoData.depotLocations();

        EventPublisher<FleetEvent> gpsFeed = new EventPublisher<>(onListenerError);
        EventPublisher<FleetEvent> fuelFeed = new EventPublisher<>(onListenerError);
        EventPublisher<FleetEvent> alerts = new EventPublisher<>(onListenerError);
        EventPublisher<FleetEvent> reports = new EventPublisher<>(onListenerError);

        FleetMap map = new FleetMap();
        EtaCalculator eta = new EtaCalculator(depots);
        GeofenceMonitor geofence = new GeofenceMonitor(depots, GEOFENCE_RADIUS_KM, alerts);
        FuelMonitor fuelMonitor = new FuelMonitor(LOW_FUEL_LITERS, alerts);
        DepotAggregator aggregator = new DepotAggregator(SUMMARY_WINDOW, new EventPublisher<>(onListenerError));
        DepotDashboard dashboard = new DepotDashboard();
        InspectionService inspection = new InspectionService(fleet, SERVICE_THRESHOLD_KM, reports);
        InspectionLog inspections = new InspectionLog();

        EventCounter<GpsPositionEvent> gpsCount = new EventCounter<>();
        EventCounter<FuelLevelEvent> fuelCount = new EventCounter<>();
        EventCounter<GeofenceAlertEvent> geofenceAlerts = new EventCounter<>();
        EventCounter<LowFuelAlertEvent> lowFuelAlerts = new EventCounter<>();
        EventCounter<DepotSummaryEvent> summaries = new EventCounter<>();
        EventCounter<InspectionReportEvent> reportCount = new EventCounter<>();

        gpsFeed.subscribe(GpsPositionEvent.class, map);
        gpsFeed.subscribe(GpsPositionEvent.class, eta);
        gpsFeed.subscribe(GpsPositionEvent.class, geofence);
        gpsFeed.subscribe(GpsPositionEvent.class, aggregator);
        gpsFeed.subscribe(GpsPositionEvent.class, gpsCount);
        fuelFeed.subscribe(FuelLevelEvent.class, fuelMonitor);
        fuelFeed.subscribe(FuelLevelEvent.class, fuelCount);
        aggregator.subscribe(DepotSummaryEvent.class, dashboard);
        aggregator.subscribe(DepotSummaryEvent.class, summaries);
        alerts.subscribe(GeofenceAlertEvent.class, geofenceAlerts);
        alerts.subscribe(LowFuelAlertEvent.class, lowFuelAlerts);
        alerts.subscribe(LowFuelAlertEvent.class, inspection);
        reports.subscribe(InspectionReportEvent.class, inspections);
        reports.subscribe(InspectionReportEvent.class, reportCount);

        FleetSimulator simulator = new FleetSimulator(depots, gpsFeed, fuelFeed, SIMULATION_SEED);
        return new FleetSystem(fleet, simulator, alerts, map, eta, dashboard, inspections,
                gpsCount, fuelCount, geofenceAlerts, lowFuelAlerts, summaries, reportCount);
    }
}
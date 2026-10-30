package fleet.listener;

import fleet.core.EventPublisher;
import fleet.core.Observer;
import fleet.event.FleetEvent;
import fleet.event.GeofenceAlertEvent;
import fleet.event.GpsPositionEvent;
import fleet.geo.GeoPoint;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GeofenceMonitor implements Observer<GpsPositionEvent> {
    private final Map<String, GeoPoint> depots;
    private final double radiusKm;
    private final EventPublisher<FleetEvent> alerts;
    private final Set<String> vehiclesOutside = ConcurrentHashMap.newKeySet();

    public GeofenceMonitor(Map<String, GeoPoint> depots, double radiusKm, EventPublisher<FleetEvent> alerts) {
        this.depots = Map.copyOf(depots);
        this.radiusKm = radiusKm;
        this.alerts = alerts;
    }

    @Override
    public void onEvent(GpsPositionEvent event) {
        GeoPoint depot = depots.get(event.depotId());
        if (depot == null) {
            return;
        }
        double km = new GeoPoint(event.latitude(), event.longitude()).distanceKmTo(depot);
        if (km <= radiusKm) {
            vehiclesOutside.remove(event.vehicleId());
            return;
        }
        boolean justLeft = vehiclesOutside.add(event.vehicleId());
        if (justLeft) {
            alerts.publish(new GeofenceAlertEvent(event.vehicleId(), event.depotId(), km, event.occurredAt()));
        }
    }
}
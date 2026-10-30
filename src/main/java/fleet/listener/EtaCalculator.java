package fleet.listener;

import fleet.core.Observer;
import fleet.event.GpsPositionEvent;
import fleet.geo.GeoPoint;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

public class EtaCalculator implements Observer<GpsPositionEvent> {
    private static final double MIN_SPEED_KMH = 5.0;

    private final Map<String, GeoPoint> depots;
    private final Map<String, Double> etaMinutes = new ConcurrentHashMap<>();

    public EtaCalculator(Map<String, GeoPoint> depots) {
        this.depots = Map.copyOf(depots);
    }

    @Override
    public void onEvent(GpsPositionEvent event) {
        GeoPoint depot = depots.get(event.depotId());
        if (depot == null) {
            return;
        }
        double km = new GeoPoint(event.latitude(), event.longitude()).distanceKmTo(depot);
        double speed = Math.max(event.speedKmh(), MIN_SPEED_KMH);
        etaMinutes.put(event.vehicleId(), km / speed * 60);
    }

    public OptionalDouble etaToDepotMinutes(String vehicleId) {
        Double eta = etaMinutes.get(vehicleId);
        return eta == null ? OptionalDouble.empty() : OptionalDouble.of(eta);
    }
}
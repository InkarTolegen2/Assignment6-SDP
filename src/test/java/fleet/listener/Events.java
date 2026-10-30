package fleet.listener;

import fleet.event.FuelLevelEvent;
import fleet.event.GpsPositionEvent;

import java.time.Instant;

final class Events {
    private Events() {}

    static GpsPositionEvent gps(String vehicleId, String depotId, double lat, double lon, double speed) {
        return new GpsPositionEvent(vehicleId, depotId, lat, lon, speed, Instant.EPOCH);
    }

    static FuelLevelEvent fuel(String vehicleId, double liters) {
        return new FuelLevelEvent(vehicleId, "D1", liters, Instant.EPOCH);
    }
}
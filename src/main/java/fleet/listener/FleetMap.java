package fleet.listener;

import fleet.core.Observer;
import fleet.event.GpsPositionEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FleetMap implements Observer<GpsPositionEvent> {
    private final Map<String, GpsPositionEvent> positions = new ConcurrentHashMap<>();

    @Override
    public void onEvent(GpsPositionEvent event) {
        positions.put(event.vehicleId(), event);
    }

    public Optional<GpsPositionEvent> lastPosition(String vehicleId) {
        return Optional.ofNullable(positions.get(vehicleId));
    }

    public int trackedVehicles() {
        return positions.size();
    }
}
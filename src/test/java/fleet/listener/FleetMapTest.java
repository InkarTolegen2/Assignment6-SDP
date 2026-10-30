package fleet.listener;

import org.junit.jupiter.api.Test;

import static fleet.listener.Events.gps;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetMapTest {
    private final FleetMap map = new FleetMap();

    @Test
    void unknownVehicleHasNoPosition() {
        assertTrue(map.lastPosition("T1").isEmpty());
    }

    @Test
    void remembersLastPositionOfVehicle() {
        map.onEvent(gps("T1", "D1", 51.0, 71.0, 50));
        map.onEvent(gps("T1", "D1", 51.2, 71.3, 55));

        assertEquals(51.2, map.lastPosition("T1").get().latitude(), 1e-9);
    }

    @Test
    void countsDistinctVehicles() {
        map.onEvent(gps("T1", "D1", 51.0, 71.0, 50));
        map.onEvent(gps("T1", "D1", 51.1, 71.0, 50));
        map.onEvent(gps("V1", "D1", 51.0, 71.0, 40));

        assertEquals(2, map.trackedVehicles());
    }
}
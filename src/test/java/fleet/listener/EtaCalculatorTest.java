package fleet.listener;

import fleet.geo.GeoPoint;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static fleet.listener.Events.gps;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EtaCalculatorTest {
    private final EtaCalculator eta = new EtaCalculator(Map.of("D1", new GeoPoint(51.0, 71.0)));

    @Test
    void etaIsDistanceDividedBySpeed() {
        eta.onEvent(gps("T1", "D1", 51.1, 71.0, 60));

        assertEquals(11.12, eta.etaToDepotMinutes("T1").getAsDouble(), 0.01);
    }

    @Test
    void standingVehicleUsesMinimumSpeed() {
        eta.onEvent(gps("T1", "D1", 51.1, 71.0, 0));

        assertEquals(133.43, eta.etaToDepotMinutes("T1").getAsDouble(), 0.01);
    }

    @Test
    void vehicleOfUnknownDepotGetsNoEta() {
        eta.onEvent(gps("T1", "OTHER", 51.1, 71.0, 60));

        assertTrue(eta.etaToDepotMinutes("T1").isEmpty());
    }
}
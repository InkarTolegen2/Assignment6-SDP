package fleet.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InspectionReportEventTest {
    @Test
    void eventKeepsItsOwnCopyOfTheVehicleList() {
        List<String> due = new ArrayList<>(List.of("T1"));
        InspectionReportEvent event = new InspectionReportEvent("D1", "T1", 100.0, due, Instant.EPOCH);

        due.add("T2");

        assertEquals(List.of("T1"), event.vehiclesDueForService());
    }
}
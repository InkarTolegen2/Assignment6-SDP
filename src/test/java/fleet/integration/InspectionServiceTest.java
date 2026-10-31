package fleet.integration;

import fleet.core.EventPublisher;
import fleet.event.FleetEvent;
import fleet.event.InspectionReportEvent;
import fleet.event.LowFuelAlertEvent;
import fleet.visitor.SampleFleet;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InspectionServiceTest {
    private final EventPublisher<FleetEvent> reports = new EventPublisher<>(error -> { });
    private final InspectionLog log = new InspectionLog();
    private final InspectionService service = new InspectionService(SampleFleet.create(), 8_000, reports);

    InspectionServiceTest() {
        reports.subscribe(InspectionReportEvent.class, log);
    }

    private static LowFuelAlertEvent alert(String vehicleId, String depotId) {
        return new LowFuelAlertEvent(vehicleId, depotId, 5.0, Instant.EPOCH);
    }

    @Test
    void alertInDepotTwoInspectsOnlyDepotTwo() {
        service.onEvent(alert("T2", "D2"));

        InspectionReportEvent report = log.reports().get(0);
        assertEquals("D2", report.depotId());
        assertEquals(2_725.0, report.maintenanceCost(), 1e-6);   // not 6925 of the whole fleet
        assertEquals(List.of("T2"), report.vehiclesDueForService());
    }

    @Test
    void alertInDepotOneInspectsOnlyDepotOne() {
        service.onEvent(alert("V1", "D1"));

        InspectionReportEvent report = log.reports().get(0);
        assertEquals(4_200.0, report.maintenanceCost(), 1e-6);
        assertEquals(List.of("T1"), report.vehiclesDueForService());
    }

    @Test
    void reportNamesTheVehicleThatCausedTheAlert() {
        service.onEvent(alert("V1", "D1"));

        assertEquals("V1", log.reports().get(0).triggeredByVehicleId());
    }

    @Test
    void alertFromUnknownDepotProducesNoReport() {
        service.onEvent(alert("X1", "NOPE"));

        assertTrue(log.reports().isEmpty());
    }

    @Test
    void inspectCanBeCalledDirectlyForAnyOtherTrigger() {
        service.inspect("D1", "T1", Instant.EPOCH);

        assertEquals(1, log.reports().size());
    }
}
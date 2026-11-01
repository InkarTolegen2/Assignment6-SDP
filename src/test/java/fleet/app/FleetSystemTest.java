package fleet.app;

import fleet.event.InspectionReportEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetSystemTest {
    private final List<Throwable> errors = new CopyOnWriteArrayList<>();
    private final FleetSystem system = FleetSystem.assemble(errors::add);

    @Test
    void fourProducersThousandEventsReachEveryListenerExactly() throws Exception {
        system.simulator().run(4, 1_000);

        assertEquals(4_000, system.gpsCount().count());
        assertEquals(4_000, system.fuelCount().count());
        assertEquals(4, system.map().trackedVehicles());
        assertEquals(400, system.summaries().count());
        assertTrue(errors.isEmpty());
    }

    @Test
    void everyLowFuelAlertEndsAsExactlyOneInspectionReport() throws Exception {
        system.simulator().run(4, 500);

        assertTrue(system.lowFuelAlerts().count() > 0);
        assertEquals(system.lowFuelAlerts().count(), system.reportCount().count());
        assertEquals(system.reportCount().count(), system.inspections().reports().size());
    }

    @Test
    void reportsContainOnlyTheDepotOfTheAlertingVehicle() throws Exception {
        system.simulator().run(2, 300);

        for (InspectionReportEvent report : system.inspections().reports()) {
            boolean evenVehicle = Integer.parseInt(report.triggeredByVehicleId().substring(1)) % 2 == 0;
            assertEquals(evenVehicle ? "D1" : "D2", report.depotId());
        }
    }

    @Test
    void dashboardShowsBothDepotsAfterARun() throws Exception {
        system.simulator().run(2, 100);

        assertTrue(system.dashboard().latest("D1").isPresent());
        assertTrue(system.dashboard().latest("D2").isPresent());
    }

    @Test
    void everyAssembleGivesAnIndependentEmptySystem() throws Exception {
        system.simulator().run(2, 50);

        FleetSystem fresh = FleetSystem.assemble(errors::add);

        assertEquals(0, fresh.gpsCount().count());
        assertEquals(100, system.gpsCount().count());
    }
}
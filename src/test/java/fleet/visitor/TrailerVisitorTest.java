package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Fleet;
import fleet.model.Trailer;
import fleet.model.Truck;
import fleet.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrailerVisitorTest {
    private final Trailer trailer = new Trailer("TR1", 4_000, 300);

    @Test
    void trailerWearsOutAtItsOwnMaintenanceRate() {
        assertEquals(400.0, trailer.accept(new MaintenanceCostVisitor()), 1e-6);   // 4000 * 0.10
    }

    @Test
    void trailerAddsToTheCostOfItsDepot() {
        Depot depot = new Depot("D", List.of(new Truck("T", 1_000, 0, 0), trailer));

        assertEquals(700.0, depot.accept(new MaintenanceCostVisitor()), 1e-6);   // 300 + 400
    }

    @Test
    void trailerBurnsNothingAndEmitsNothing() {
        Co2Visitor co2 = new Co2Visitor();

        assertEquals(0.0, trailer.accept(new FuelCostVisitor()), 1e-6);
        assertEquals(0.0, trailer.accept(co2), 1e-6);
        assertEquals(0.0, co2.totalKg(), 1e-6);
    }

    @Test
    void trailerOverTheThresholdIsDueForService() {
        Fleet fleet = new Fleet("F", List.of(new Depot("D", List.of(trailer, new Truck("T", 1_000, 0, 0)))));

        List<String> due = fleet.accept(new ServiceDueVisitor(3_000)).stream().map(Vehicle::id).toList();

        assertEquals(List.of("TR1"), due);
    }

    @Test
    void trailerIsExportedAsJsonWithoutConsumptionField() {
        assertEquals("{\"type\":\"trailer\",\"id\":\"TR1\",\"kmSinceService\":4000.0,\"kmThisMonth\":300.0}",
                trailer.accept(new JsonExportVisitor()));
    }
}
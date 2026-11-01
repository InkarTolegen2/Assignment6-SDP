package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Fleet;
import fleet.model.Truck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaintenanceCostVisitorTest {
    private final Fleet fleet = SampleFleet.create();

    @Test
    void depotOneCostsTruckPlusVan() {
        assertEquals(4_200.0, fleet.depots().get(0).accept(new MaintenanceCostVisitor()), 1e-6);
    }

    @Test
    void depotTwoCostsDronePlusTruck() {
        assertEquals(2_725.0, fleet.depots().get(1).accept(new MaintenanceCostVisitor()), 1e-6);
    }

    @Test
    void wholeFleetSumsAllDepots() {
        assertEquals(6_925.0, fleet.accept(new MaintenanceCostVisitor()), 1e-6);
    }

    @Test
    void emptyDepotCostsNothing() {
        Fleet empty = new Fleet("E", List.of(new Depot("D", List.of())));
        assertEquals(0.0, empty.accept(new MaintenanceCostVisitor()), 1e-6);
    }

    @Test
    void singleLeafWorksWithoutAnyContainer() {
        assertEquals(300.0, new Truck("T", 1_000, 0, 0).accept(new MaintenanceCostVisitor()), 1e-6);
    }
}
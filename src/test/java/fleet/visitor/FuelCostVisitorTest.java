package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Fleet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FuelCostVisitorTest {
    private final Fleet fleet = SampleFleet.create();

    @Test
    void depotOneBurnsDieselOfTruckAndVan() {
        assertEquals(105_000.0, fleet.depots().get(0).accept(new FuelCostVisitor()), 1e-6);
    }

    @Test
    void depotTwoPaysDieselAndElectricity() {
        assertEquals(60_300.0, fleet.depots().get(1).accept(new FuelCostVisitor()), 1e-6);
    }

    @Test
    void wholeFleetSumsAllDepots() {
        assertEquals(165_300.0, fleet.accept(new FuelCostVisitor()), 1e-6);
    }

    @Test
    void emptyDepotCostsNothing() {
        Fleet empty = new Fleet("E", List.of(new Depot("D", List.of())));
        assertEquals(0.0, empty.accept(new FuelCostVisitor()), 1e-6);
    }
}
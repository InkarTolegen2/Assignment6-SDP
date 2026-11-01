package fleet.visitor;

import fleet.model.Fleet;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Co2VisitorTest {
    private final Fleet fleet = SampleFleet.create();

    @Test
    void returnsMonthlyCo2OfTheWholeFleet() {
        assertEquals(1_479.0, fleet.accept(new Co2Visitor()), 1e-6);
    }

    @Test
    void accumulatesTotalDuringTheTraversal() {
        Co2Visitor visitor = new Co2Visitor();

        fleet.accept(visitor);

        assertEquals(1_479.0, visitor.totalKg(), 1e-6);
    }

    @Test
    void accumulatesCo2OfEveryDepot() {
        Co2Visitor visitor = new Co2Visitor();

        fleet.accept(visitor);

        Map<String, Double> byDepot = visitor.kgByDepot();
        assertEquals(938.0, byDepot.get("D1"), 1e-6);   // 804 + 134
        assertEquals(541.0, byDepot.get("D2"), 1e-6);   // 5 + 536
    }

    @Test
    void visitingOnlyOneDepotAccumulatesOnlyThatDepot() {
        Co2Visitor visitor = new Co2Visitor();

        fleet.depots().get(1).accept(visitor);

        assertEquals(541.0, visitor.totalKg(), 1e-6);
        assertEquals(1, visitor.kgByDepot().size());
    }
}
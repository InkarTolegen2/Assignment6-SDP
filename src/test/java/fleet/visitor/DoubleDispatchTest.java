package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Drone;
import fleet.model.Fleet;
import fleet.model.FuelStation;
import fleet.model.Truck;
import fleet.model.Van;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleDispatchTest {
    private static final Visitor<String> WHO_IS_VISITED = new Visitor<>() {
        public String visitFleet(Fleet fleet) { return "fleet"; }
        public String visitDepot(Depot depot) { return "depot"; }
        public String visitTruck(Truck truck) { return "truck"; }
        public String visitVan(Van van) { return "van"; }
        public String visitDrone(Drone drone) { return "drone"; }
        public String visitFuelStation(FuelStation station) { return "station"; }
    };

    @Test
    void everyElementCallsItsOwnVisitMethod() {
        assertEquals("truck", new Truck("T", 0, 0, 0).accept(WHO_IS_VISITED));
        assertEquals("van", new Van("V", 0, 0, 0).accept(WHO_IS_VISITED));
        assertEquals("drone", new Drone("D", 0, 0, 0).accept(WHO_IS_VISITED));
        assertEquals("station", new FuelStation("S", 0, 0).accept(WHO_IS_VISITED));
    }

    @Test
    void containersCallTheirOwnVisitMethod() {
        Depot depot = new Depot("D", List.of());
        assertEquals("depot", depot.accept(WHO_IS_VISITED));
        assertEquals("fleet", new Fleet("F", List.of(depot)).accept(WHO_IS_VISITED));
    }
}
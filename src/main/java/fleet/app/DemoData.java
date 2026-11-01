package fleet.app;

import fleet.geo.GeoPoint;
import fleet.model.Depot;
import fleet.model.Drone;
import fleet.model.Fleet;
import fleet.model.FuelStation;
import fleet.model.Truck;
import fleet.model.Van;

import java.util.List;
import java.util.Map;

/** Sample data of the demo. Vehicle ids V0..V7 match the producers of the simulator. */
final class DemoData {
    private DemoData() {}

    static Map<String, GeoPoint> depotLocations() {
        return Map.of("D1", new GeoPoint(51.10, 71.40), "D2", new GeoPoint(43.20, 76.90));
    }

    static Fleet fleet() {
        Depot astana = new Depot("D1", List.of(
                new Truck("V0", 12_000, 1_000, 30),
                new Van("V2", 3_000, 500, 10),
                new Drone("V4", 500, 200, 5),
                new Truck("V6", 9_000, 800, 25),
                new FuelStation("S1", 20_000, 300)));
        Depot almaty = new Depot("D2", List.of(
                new Van("V1", 8_500, 600, 11),
                new Truck("V3", 2_000, 900, 28),
                new Drone("V5", 300, 150, 4),
                new Van("V7", 15_000, 700, 12),
                new FuelStation("S2", 15_000, 310)));
        return new Fleet("Kazakhstan fleet", List.of(astana, almaty));
    }
}
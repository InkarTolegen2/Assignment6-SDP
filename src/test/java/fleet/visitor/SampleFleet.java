package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Drone;
import fleet.model.Fleet;
import fleet.model.FuelStation;
import fleet.model.Truck;
import fleet.model.Van;

import java.util.List;

public final class SampleFleet {
    private SampleFleet() {}

    public static Fleet create() {
        Depot d1 = new Depot("D1", List.of(
                new Truck("T1", 12_000, 1_000, 30),
                new Van("V1", 3_000, 500, 10),
                new FuelStation("S1", 5_000, 450)));
        Depot d2 = new Depot("D2", List.of(
                new Drone("DR1", 500, 200, 5),
                new Truck("T2", 9_000, 800, 25)));
        return new Fleet("KZ", List.of(d1, d2));
    }
}
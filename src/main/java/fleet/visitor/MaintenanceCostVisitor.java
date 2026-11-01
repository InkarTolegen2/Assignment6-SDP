package fleet.visitor;

import fleet.model.Drone;
import fleet.model.FuelStation;
import fleet.model.Trailer;
import fleet.model.Truck;
import fleet.model.Van;

public class MaintenanceCostVisitor extends SumVisitor {
    private static final double TRUCK_RATE = 0.30;
    private static final double VAN_RATE = 0.20;
    private static final double DRONE_RATE = 0.05;
    private static final double TRAILER_RATE = 0.10;

    @Override
    public Double visitTruck(Truck truck) {
        return truck.kmSinceService() * TRUCK_RATE;
    }

    @Override
    public Double visitVan(Van van) {
        return van.kmSinceService() * VAN_RATE;
    }

    @Override
    public Double visitDrone(Drone drone) {
        return drone.kmSinceService() * DRONE_RATE;
    }

    @Override
    public Double visitTrailer(Trailer trailer) {
        return trailer.kmSinceService() * TRAILER_RATE;
    }

    @Override
    public Double visitFuelStation(FuelStation station) {
        return 0.0;
    }
}
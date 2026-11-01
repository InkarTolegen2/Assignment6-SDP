package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Drone;
import fleet.model.Fleet;
import fleet.model.FuelStation;
import fleet.model.Trailer;
import fleet.model.Truck;
import fleet.model.Van;

public interface Visitor<R> {
    R visitFleet(Fleet fleet);

    R visitDepot(Depot depot);

    R visitTruck(Truck truck);

    R visitVan(Van van);

    R visitDrone(Drone drone);

    R visitTrailer(Trailer trailer);

    R visitFuelStation(FuelStation station);
}
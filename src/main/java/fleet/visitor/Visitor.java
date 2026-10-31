package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Drone;
import fleet.model.Fleet;
import fleet.model.FuelStation;
import fleet.model.Truck;
import fleet.model.Van;

/**
 * Double dispatch: element.accept(visitor) picks the element type, the call
 * visitor.visitXxx(this) then picks the operation. Visitors do the traversal.
 */
public interface Visitor<R> {
    R visitFleet(Fleet fleet);

    R visitDepot(Depot depot);

    R visitTruck(Truck truck);

    R visitVan(Van van);

    R visitDrone(Drone drone);

    R visitFuelStation(FuelStation station);
}
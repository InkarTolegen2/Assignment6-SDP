package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Drone;
import fleet.model.Fleet;
import fleet.model.FuelStation;
import fleet.model.Truck;
import fleet.model.Van;
import fleet.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class ServiceDueVisitor implements Visitor<List<Vehicle>> {
    private final double thresholdKm;

    public ServiceDueVisitor(double thresholdKm) {
        this.thresholdKm = thresholdKm;
    }

    @Override
    public List<Vehicle> visitFleet(Fleet fleet) {
        List<Vehicle> due = new ArrayList<>();
        fleet.depots().forEach(depot -> due.addAll(depot.accept(this)));
        return due;
    }

    @Override
    public List<Vehicle> visitDepot(Depot depot) {
        List<Vehicle> due = new ArrayList<>();
        depot.children().forEach(child -> due.addAll(child.accept(this)));
        return due;
    }

    @Override
    public List<Vehicle> visitTruck(Truck truck) {
        return dueOrNothing(truck);
    }

    @Override
    public List<Vehicle> visitVan(Van van) {
        return dueOrNothing(van);
    }

    @Override
    public List<Vehicle> visitDrone(Drone drone) {
        return dueOrNothing(drone);
    }

    @Override
    public List<Vehicle> visitFuelStation(FuelStation station) {
        return List.of();
    }

    private List<Vehicle> dueOrNothing(Vehicle vehicle) {
        return vehicle.kmSinceService() >= thresholdKm ? List.of(vehicle) : List.of();
    }
}
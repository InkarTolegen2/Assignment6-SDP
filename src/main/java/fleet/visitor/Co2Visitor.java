package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Drone;
import fleet.model.FuelStation;
import fleet.model.Trailer;
import fleet.model.Truck;
import fleet.model.Van;

import java.util.LinkedHashMap;
import java.util.Map;

public class Co2Visitor extends SumVisitor {
    private static final double DIESEL_KG_PER_LITER = 2.68;
    private static final double GRID_KG_PER_KWH = 0.5;

    private final Map<String, Double> kgByDepot = new LinkedHashMap<>();
    private double totalKg;

    @Override
    public Double visitDepot(Depot depot) {
        double depotKg = super.visitDepot(depot);
        kgByDepot.put(depot.id(), depotKg);
        return depotKg;
    }

    @Override
    public Double visitTruck(Truck truck) {
        return add(truck.kmThisMonth() * truck.litersPer100Km() / 100 * DIESEL_KG_PER_LITER);
    }

    @Override
    public Double visitVan(Van van) {
        return add(van.kmThisMonth() * van.litersPer100Km() / 100 * DIESEL_KG_PER_LITER);
    }

    @Override
    public Double visitDrone(Drone drone) {
        return add(drone.kmThisMonth() * drone.kwhPer100Km() / 100 * GRID_KG_PER_KWH);
    }

    @Override
    public Double visitTrailer(Trailer trailer) {
        return 0.0; 
    }

    @Override
    public Double visitFuelStation(FuelStation station) {
        return 0.0;
    }

    public double totalKg() {
        return totalKg;
    }

    public Map<String, Double> kgByDepot() {
        return Map.copyOf(kgByDepot);
    }

    private double add(double kg) {
        totalKg += kg;
        return kg;
    }
}
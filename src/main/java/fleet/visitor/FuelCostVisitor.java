package fleet.visitor;

import fleet.model.Drone;
import fleet.model.FuelStation;
import fleet.model.Trailer;
import fleet.model.Truck;
import fleet.model.Van;

public class FuelCostVisitor extends SumVisitor {
    private static final double DIESEL_PRICE_PER_LITER = 300.0;
    private static final double ELECTRICITY_PRICE_PER_KWH = 30.0;

    @Override
    public Double visitTruck(Truck truck) {
        return truck.kmThisMonth() * truck.litersPer100Km() / 100 * DIESEL_PRICE_PER_LITER;
    }

    @Override
    public Double visitVan(Van van) {
        return van.kmThisMonth() * van.litersPer100Km() / 100 * DIESEL_PRICE_PER_LITER;
    }

    @Override
    public Double visitDrone(Drone drone) {
        return drone.kmThisMonth() * drone.kwhPer100Km() / 100 * ELECTRICITY_PRICE_PER_KWH;
    }

    @Override
    public Double visitTrailer(Trailer trailer) {
        return 0.0;
    }

    @Override
    public Double visitFuelStation(FuelStation station) {
        return 0.0;
    }
}
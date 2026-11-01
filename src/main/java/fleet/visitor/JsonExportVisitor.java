package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Drone;
import fleet.model.Fleet;
import fleet.model.FleetElement;
import fleet.model.FuelStation;
import fleet.model.Trailer;
import fleet.model.Truck;
import fleet.model.Van;
import fleet.model.Vehicle;

import java.util.List;
import java.util.stream.Collectors;

public class JsonExportVisitor implements Visitor<String> {
    @Override
    public String visitFleet(Fleet fleet) {
        return "{\"type\":\"fleet\",\"name\":" + quote(fleet.name())
                + ",\"depots\":[" + join(fleet.depots()) + "]}";
    }

    @Override
    public String visitDepot(Depot depot) {
        return "{\"type\":\"depot\",\"id\":" + quote(depot.id())
                + ",\"children\":[" + join(depot.children()) + "]}";
    }

    @Override
    public String visitTruck(Truck truck) {
        return vehicleStart("truck", truck) + ",\"litersPer100Km\":" + truck.litersPer100Km() + "}";
    }

    @Override
    public String visitVan(Van van) {
        return vehicleStart("van", van) + ",\"litersPer100Km\":" + van.litersPer100Km() + "}";
    }

    @Override
    public String visitDrone(Drone drone) {
        return vehicleStart("drone", drone) + ",\"kwhPer100Km\":" + drone.kwhPer100Km() + "}";
    }

    @Override
    public String visitTrailer(Trailer trailer) {
        return vehicleStart("trailer", trailer) + "}";
    }

    @Override
    public String visitFuelStation(FuelStation station) {
        return "{\"type\":\"fuelStation\",\"id\":" + quote(station.id())
                + ",\"capacityLiters\":" + station.capacityLiters()
                + ",\"pricePerLiter\":" + station.pricePerLiter() + "}";
    }

    private String vehicleStart(String type, Vehicle vehicle) {
        return "{\"type\":\"" + type + "\",\"id\":" + quote(vehicle.id())
                + ",\"kmSinceService\":" + vehicle.kmSinceService()
                + ",\"kmThisMonth\":" + vehicle.kmThisMonth();
    }

    private String join(List<? extends FleetElement> elements) {
        return elements.stream().map(element -> element.accept(this)).collect(Collectors.joining(","));
    }

    private static String quote(String text) {
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }
}
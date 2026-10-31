package fleet.model;

import fleet.visitor.Visitor;

public record FuelStation(String id, double capacityLiters, double pricePerLiter) implements FleetElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitFuelStation(this);
    }
}
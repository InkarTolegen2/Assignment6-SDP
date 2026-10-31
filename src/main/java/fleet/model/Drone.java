package fleet.model;

import fleet.visitor.Visitor;

public record Drone(String id, double kmSinceService, double kmThisMonth, double kwhPer100Km)
        implements Vehicle {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitDrone(this);
    }
}
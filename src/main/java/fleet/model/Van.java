package fleet.model;

import fleet.visitor.Visitor;

public record Van(String id, double kmSinceService, double kmThisMonth, double litersPer100Km)
        implements Vehicle {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitVan(this);
    }
}
package fleet.model;

import fleet.visitor.Visitor;

public record Trailer(String id, double kmSinceService, double kmThisMonth) implements Vehicle {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitTrailer(this);
    }
}
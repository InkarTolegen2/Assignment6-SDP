package fleet.model;

import fleet.visitor.Visitor;

import java.util.List;

public record Depot(String id, List<FleetElement> children) implements FleetElement {
    public Depot {
        children = List.copyOf(children);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitDepot(this);
    }
}
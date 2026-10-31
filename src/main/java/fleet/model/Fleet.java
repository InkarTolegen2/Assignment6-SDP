package fleet.model;

import fleet.visitor.Visitor;

import java.util.List;

public record Fleet(String name, List<Depot> depots) implements FleetElement {
    public Fleet {
        depots = List.copyOf(depots);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitFleet(this);
    }
}
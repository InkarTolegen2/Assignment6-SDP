package fleet.model;

import fleet.visitor.Visitor;

public interface FleetElement {
    <R> R accept(Visitor<R> visitor);
}
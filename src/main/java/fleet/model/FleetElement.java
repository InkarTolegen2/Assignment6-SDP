package fleet.model;

import fleet.visitor.Visitor;

/** A node of the fleet tree. Elements hold data only; every operation lives in a visitor. */
public interface FleetElement {
    <R> R accept(Visitor<R> visitor);
}
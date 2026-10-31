package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Fleet;

/** Base for visitors that add numbers: containers sum their children, leaves are up to the subclass. */
public abstract class SumVisitor implements Visitor<Double> {
    @Override
    public Double visitFleet(Fleet fleet) {
        return fleet.depots().stream().mapToDouble(depot -> depot.accept(this)).sum();
    }

    @Override
    public Double visitDepot(Depot depot) {
        return depot.children().stream().mapToDouble(child -> child.accept(this)).sum();
    }
}
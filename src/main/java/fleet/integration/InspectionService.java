package fleet.integration;

import fleet.core.EventPublisher;
import fleet.core.Observer;
import fleet.event.FleetEvent;
import fleet.event.InspectionReportEvent;
import fleet.event.LowFuelAlertEvent;
import fleet.model.Depot;
import fleet.model.Fleet;
import fleet.model.Vehicle;
import fleet.visitor.MaintenanceCostVisitor;
import fleet.visitor.ServiceDueVisitor;

import java.time.Instant;
import java.util.Optional;

/**
 * Observer meets Visitor: an alert event starts two visitors on the subtree of the depot
 * that raised the alert, and the answer goes back into the event system as a new event.
 * The fleet is immutable and every visit uses fresh visitors, so concurrent alerts are safe.
 */
public class InspectionService implements Observer<LowFuelAlertEvent> {
    private final Fleet fleet;
    private final double serviceThresholdKm;
    private final EventPublisher<FleetEvent> reports;

    public InspectionService(Fleet fleet, double serviceThresholdKm, EventPublisher<FleetEvent> reports) {
        this.fleet = fleet;
        this.serviceThresholdKm = serviceThresholdKm;
        this.reports = reports;
    }

    @Override
    public void onEvent(LowFuelAlertEvent alert) {
        inspect(alert.depotId(), alert.vehicleId(), alert.occurredAt());
    }

    /** Public so that another alert type can be wired to the same inspection with one line. */
    public void inspect(String depotId, String triggeredByVehicleId, Instant occurredAt) {
        findDepot(depotId).ifPresent(depot -> {
            double cost = depot.accept(new MaintenanceCostVisitor());
            var due = depot.accept(new ServiceDueVisitor(serviceThresholdKm)).stream()
                    .map(Vehicle::id)
                    .toList();
            reports.publish(new InspectionReportEvent(depotId, triggeredByVehicleId, cost, due, occurredAt));
        });
    }

    private Optional<Depot> findDepot(String depotId) {
        return fleet.depots().stream().filter(depot -> depot.id().equals(depotId)).findFirst();
    }
}
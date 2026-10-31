package fleet.event;

import java.time.Instant;
import java.util.List;

/** Result of a depot inspection: the answer that the visitors produced, sent back as an event. */
public record InspectionReportEvent(String depotId, String triggeredByVehicleId, double maintenanceCost,
                                    List<String> vehiclesDueForService, Instant occurredAt)
        implements FleetEvent {
    public InspectionReportEvent {
        vehiclesDueForService = List.copyOf(vehiclesDueForService);
    }
}
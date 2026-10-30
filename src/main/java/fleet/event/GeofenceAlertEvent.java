package fleet.event;

import java.time.Instant;

public record GeofenceAlertEvent(String vehicleId, String depotId, double distanceKm, Instant occurredAt) implements FleetEvent {}

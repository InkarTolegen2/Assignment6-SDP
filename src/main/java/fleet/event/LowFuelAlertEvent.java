package fleet.event;

import java.time.Instant;

public record LowFuelAlertEvent(String vehicleId, String depotId, double liters, Instant occurredAt) implements FleetEvent {}

package fleet.event;

import java.time.Instant;

public record DepotSummaryEvent(String depotId, int vehicles, double averageSpeedKmh, Instant occurredAt) implements FleetEvent {}
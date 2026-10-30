package fleet.event;

import java.time.Instant;

public record GpsPositionEvent(String vehicleId, String depotId, double latitude, 
                               double longitude, double speedKmh, Instant occurredAt) 
        implements FleetEvent {}

package fleet.event;

import java.time.Instant;

public record GpsPositionEvent(String vehicleId, String depodId, double latitude, 
                               double longitude, double speedKmh, Instant occurredAt) 
        implements FleetEvent {}

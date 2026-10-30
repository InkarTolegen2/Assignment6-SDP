package fleet.event;

import java.time.Instant;

public interface FleetEvent {
    Instant occurredAt();
}

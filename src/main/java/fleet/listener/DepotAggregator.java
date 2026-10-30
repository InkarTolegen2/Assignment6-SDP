package fleet.listener;

import fleet.core.EventPublisher;
import fleet.core.Observer;
import fleet.core.Subject;
import fleet.event.DepotSummaryEvent;
import fleet.event.FleetEvent;
import fleet.event.GpsPositionEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DepotAggregator implements Observer<GpsPositionEvent>, Subject<FleetEvent> {
    private final int windowSize;
    private final EventPublisher<FleetEvent> summaries;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public DepotAggregator(int windowSize, EventPublisher<FleetEvent> summaries) {
        this.windowSize = windowSize;
        this.summaries = summaries;
    }

    @Override
    public void onEvent(GpsPositionEvent event) {
        Window window = windows.computeIfAbsent(event.depotId(), depotId -> new Window());
        window.add(event, windowSize).ifPresent(summaries::publish);
    }

    @Override
    public <T extends FleetEvent> void subscribe(Class<T> eventType, Observer<? super T> observer) {
        summaries.subscribe(eventType, observer);
    }

    @Override
    public <T extends FleetEvent> void unsubscribe(Class<T> eventType, Observer<? super T> observer) {
        summaries.unsubscribe(eventType, observer);
    }

    private static final class Window {
        private double speedSum;
        private int count;
        private final Set<String> vehicles = new HashSet<>();

        synchronized Optional<DepotSummaryEvent> add(GpsPositionEvent event, int windowSize) {
            speedSum += event.speedKmh();
            count++;
            vehicles.add(event.vehicleId());
            if (count < windowSize) {
                return Optional.empty();
            }
            DepotSummaryEvent summary = new DepotSummaryEvent(event.depotId(), vehicles.size(),
                    speedSum / count, event.occurredAt());
            speedSum = 0;
            count = 0;
            vehicles.clear();
            return Optional.of(summary);
        }
    }
}
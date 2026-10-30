package fleet.listener;

import fleet.core.Observer;
import fleet.event.DepotSummaryEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DepotDashboard implements Observer<DepotSummaryEvent> {
    private final Map<String, DepotSummaryEvent> latest = new ConcurrentHashMap<>();

    @Override
    public void onEvent(DepotSummaryEvent event) {
        latest.put(event.depotId(), event);
    }

    public Optional<DepotSummaryEvent> latest(String depotId) {
        return Optional.ofNullable(latest.get(depotId));
    }
}
package fleet.listener;

import fleet.core.EventPublisher;
import fleet.core.Observer;
import fleet.event.FleetEvent;
import fleet.event.FuelLevelEvent;
import fleet.event.LowFuelAlertEvent;

public class FuelMonitor implements Observer<FuelLevelEvent> {
    private final double thresholdLiters;
    private final EventPublisher<FleetEvent> alerts;

    public FuelMonitor(double thresholdLiters, EventPublisher<FleetEvent> alerts) {
        this.thresholdLiters = thresholdLiters;
        this.alerts = alerts;
    }

    @Override
    public void onEvent(FuelLevelEvent event) {
        if (event.liters() < thresholdLiters) {
            alerts.publish(new LowFuelAlertEvent(event.vehicleId(), event.depotId(),
                    event.liters(), event.occurredAt()));
        }
    }
}
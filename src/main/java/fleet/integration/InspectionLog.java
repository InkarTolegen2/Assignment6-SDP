package fleet.integration;

import fleet.core.Observer;
import fleet.event.InspectionReportEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Collects every inspection report that comes back from the visitors. */
public class InspectionLog implements Observer<InspectionReportEvent> {
    private final List<InspectionReportEvent> reports = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(InspectionReportEvent event) {
        reports.add(event);
    }

    public List<InspectionReportEvent> reports() {
        return List.copyOf(reports);
    }
}
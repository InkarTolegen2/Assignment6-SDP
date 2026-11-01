package fleet.app;

import fleet.core.Observer;
import fleet.event.DepotSummaryEvent;
import fleet.event.GpsPositionEvent;
import fleet.event.InspectionReportEvent;
import fleet.event.LowFuelAlertEvent;
import fleet.model.Fleet;
import fleet.model.Vehicle;
import fleet.sim.FleetSimulator;
import fleet.visitor.Co2Visitor;
import fleet.visitor.JsonExportVisitor;
import fleet.visitor.MaintenanceCostVisitor;
import fleet.visitor.ServiceDueVisitor;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Console demo of the fleet system. */
public class Main {
    public static void main(String[] args) {
        Consumer<Throwable> onListenerError = error -> System.err.println("Listener failed: " + error);
        FleetSystem system = FleetSystem.assemble(onListenerError);
        Observer<LowFuelAlertEvent> alertPrinter = alert -> System.out.println(
                "  ALERT low fuel: " + alert.vehicleId() + " has " + format(alert.liters()) + " L");
        boolean printingAlerts = false;

        Scanner in = new Scanner(System.in);
        while (true) {
            printMenu(printingAlerts);
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> runSimulation(system, in);
                    case "2" -> showDashboard(system);
                    case "3" -> showPositions(system);
                    case "4" -> showInspections(system);
                    case "5" -> showVisitorReports(system.fleet());
                    case "6" -> System.out.println(system.fleet().accept(new JsonExportVisitor()));
                    case "7" -> {
                        printingAlerts = !printingAlerts;
                        if (printingAlerts) {
                            system.alerts().subscribe(LowFuelAlertEvent.class, alertPrinter);
                        } else {
                            system.alerts().unsubscribe(LowFuelAlertEvent.class, alertPrinter);
                        }
                        System.out.println("Live alert printer is " + (printingAlerts ? "ON" : "OFF")
                                + " (subscribed or unsubscribed at runtime)");
                    }
                    case "8" -> {
                        system = FleetSystem.assemble(onListenerError);
                        printingAlerts = false;
                        System.out.println("New empty system created");
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Unknown option");
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException failure) {
                System.out.println("Error: " + failure.getMessage());
            }
        }
    }

    private static void printMenu(boolean printingAlerts) {
        System.out.println();
        System.out.println("1  Run simulation (producer threads, events per thread)");
        System.out.println("2  Depot dashboard (chain: aggregator -> dashboard)");
        System.out.println("3  Vehicle positions and ETA to depot");
        System.out.println("4  Inspection reports (alert -> visitors on the depot -> report)");
        System.out.println("5  Visitor reports: maintenance cost, CO2, vehicles due for service");
        System.out.println("6  Export fleet as JSON");
        System.out.println("7  Live alert printer: " + (printingAlerts ? "ON" : "OFF") + " (toggle)");
        System.out.println("8  Reset (new empty system)");
        System.out.println("0  Exit");
        System.out.print("> ");
    }

    private static void runSimulation(FleetSystem system, Scanner in) throws InterruptedException {
        System.out.print("Producer threads: ");
        int threads = Integer.parseInt(in.nextLine().trim());
        System.out.print("Events per thread: ");
        int events = Integer.parseInt(in.nextLine().trim());

        FleetSimulator.Result result = system.simulator().run(threads, events);

        System.out.println("Published: " + result.gpsEvents() + " GPS, " + result.fuelEvents() + " fuel readings");
        System.out.println("Received:  " + system.gpsCount().count() + " GPS, " + system.fuelCount().count() + " fuel readings (total so far)");
        System.out.println("Geofence alerts: " + system.geofenceAlerts().count()
                + ", low fuel alerts: " + system.lowFuelAlerts().count());
        System.out.println("Depot summaries: " + system.summaries().count()
                + ", inspection reports: " + system.reportCount().count());
    }

    private static void showDashboard(FleetSystem system) {
        for (String depotId : List.of("D1", "D2")) {
            Optional<DepotSummaryEvent> latest = system.dashboard().latest(depotId);
            System.out.println(depotId + ": " + latest
                    .map(summary -> summary.vehicles() + " vehicles, average speed "
                            + format(summary.averageSpeedKmh()) + " km/h")
                    .orElse("no summary yet"));
        }
    }

    private static void showPositions(FleetSystem system) {
        for (Vehicle vehicle : system.fleet().accept(new ServiceDueVisitor(0))) {
            Optional<GpsPositionEvent> position = system.map().lastPosition(vehicle.id());
            String eta = system.eta().etaToDepotMinutes(vehicle.id()).isPresent()
                    ? format(system.eta().etaToDepotMinutes(vehicle.id()).getAsDouble()) + " min to depot"
                    : "";
            System.out.println(vehicle.id() + ": " + position
                    .map(p -> format(p.latitude()) + ", " + format(p.longitude()) + " at " + format(p.speedKmh()) + " km/h")
                    .orElse("no position yet") + "  " + eta);
        }
    }

    private static void showInspections(FleetSystem system) {
        List<InspectionReportEvent> reports = system.inspections().reports();
        System.out.println(reports.size() + " reports");
        reports.stream().limit(3).forEach(report -> System.out.println(
                "  depot " + report.depotId() + " after alert of " + report.triggeredByVehicleId()
                        + ": maintenance cost " + format(report.maintenanceCost())
                        + ", due for service " + report.vehiclesDueForService()));
    }

    private static void showVisitorReports(Fleet fleet) {
        System.out.println("Maintenance cost: " + format(fleet.accept(new MaintenanceCostVisitor())));
        Co2Visitor co2 = new Co2Visitor();
        fleet.accept(co2);
        String byDepot = co2.kgByDepot().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + format(entry.getValue()))
                .collect(Collectors.joining(", "));
        System.out.println("CO2 per month: " + format(co2.totalKg()) + " kg (" + byDepot + ")");
        List<String> due = fleet.accept(new ServiceDueVisitor(FleetSystem.SERVICE_THRESHOLD_KM))
                .stream().map(Vehicle::id).toList();
        System.out.println("Due for service (>= " + format(FleetSystem.SERVICE_THRESHOLD_KM) + " km): " + due);
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
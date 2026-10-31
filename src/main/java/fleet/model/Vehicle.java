package fleet.model;

public interface Vehicle extends FleetElement {
    String id();

    double kmSinceService();

    double kmThisMonth();
}
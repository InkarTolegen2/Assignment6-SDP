package fleet.visitor;

import fleet.model.Fleet;
import fleet.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceDueVisitorTest {
    private final Fleet fleet = SampleFleet.create();

    private static List<String> ids(List<Vehicle> vehicles) {
        return vehicles.stream().map(Vehicle::id).toList();
    }

    @Test
    void returnsVehiclesOverTheThresholdInTreeOrder() {
        assertEquals(List.of("T1", "T2"), ids(fleet.accept(new ServiceDueVisitor(8_000))));
    }

    @Test
    void thresholdIsInclusive() {
        assertEquals(List.of("T1", "T2"), ids(fleet.accept(new ServiceDueVisitor(9_000))));
    }

    @Test
    void highThresholdReturnsOnlyTheWorstVehicle() {
        assertEquals(List.of("T1"), ids(fleet.accept(new ServiceDueVisitor(10_000))));
    }

    @Test
    void nothingIsDueWhenThresholdIsAboveEveryVehicle() {
        assertTrue(fleet.accept(new ServiceDueVisitor(50_000)).isEmpty());
    }

    @Test
    void filterCanStartAtOneDepot() {
        assertEquals(List.of("T2"), ids(fleet.depots().get(1).accept(new ServiceDueVisitor(8_000))));
    }
}
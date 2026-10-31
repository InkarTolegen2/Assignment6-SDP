package fleet.visitor;

import fleet.model.Depot;
import fleet.model.Fleet;
import fleet.model.FuelStation;
import fleet.model.Truck;
import fleet.model.Van;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonExportVisitorTest {
    @Test
    void exportsNestedFleetDepotAndLeaves() {
        Fleet fleet = new Fleet("Mini", List.of(new Depot("D1", List.of(
                new Van("V1", 3_000, 500, 10),
                new FuelStation("S1", 5_000, 450)))));

        String expected = "{\"type\":\"fleet\",\"name\":\"Mini\",\"depots\":["
                + "{\"type\":\"depot\",\"id\":\"D1\",\"children\":["
                + "{\"type\":\"van\",\"id\":\"V1\",\"kmSinceService\":3000.0,\"kmThisMonth\":500.0,\"litersPer100Km\":10.0},"
                + "{\"type\":\"fuelStation\",\"id\":\"S1\",\"capacityLiters\":5000.0,\"pricePerLiter\":450.0}"
                + "]}]}";

        assertEquals(expected, fleet.accept(new JsonExportVisitor()));
    }

    @Test
    void emptyDepotExportsEmptyArray() {
        Fleet fleet = new Fleet("E", List.of(new Depot("D1", List.of())));

        assertEquals("{\"type\":\"fleet\",\"name\":\"E\",\"depots\":["
                + "{\"type\":\"depot\",\"id\":\"D1\",\"children\":[]}]}", fleet.accept(new JsonExportVisitor()));
    }

    @Test
    void quotesInNamesAreEscaped() {
        String json = new Truck("T\"1", 1, 2, 3).accept(new JsonExportVisitor());

        assertEquals("{\"type\":\"truck\",\"id\":\"T\\\"1\",\"kmSinceService\":1.0,"
                + "\"kmThisMonth\":2.0,\"litersPer100Km\":3.0}", json);
    }

    @Test
    void severalDepotsAreSeparatedByCommas() {
        String json = SampleFleet.create().accept(new JsonExportVisitor());

        assertEquals(2, json.split("\\{\"type\":\"depot\"", -1).length - 1);
        assertTrue(json.contains("]},{\"type\":\"depot\",\"id\":\"D2\""));
    }
}
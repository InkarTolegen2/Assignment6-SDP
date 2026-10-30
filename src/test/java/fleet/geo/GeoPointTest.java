package fleet.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeoPointTest {
    @Test
    void distanceToItselfIsZero() {
        GeoPoint point = new GeoPoint(51.0, 71.0);
        assertEquals(0.0, point.distanceKmTo(point), 1e-9);
    }

    @Test
    void tenthOfADegreeOfLatitudeIsAboutElevenPointOneTwoKilometres() {
        GeoPoint a = new GeoPoint(51.0, 71.0);
        GeoPoint b = new GeoPoint(51.1, 71.0);
        assertEquals(11.12, a.distanceKmTo(b), 0.01);
    }
}
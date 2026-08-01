package dev.discscout.geodesy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.LocalPoint;
import org.junit.jupiter.api.Test;

final class GeoCalculatorTest {
  @Test
  void roundTripLocalCoordinatePreservesShortDistance() {
    var origin = new GeoPoint(39.7392, -104.9903);
    var local = new LocalPoint(42.0, 105.0, 0.0);
    var point = GeoCalculator.fromLocal(origin, local);
    var roundTrip = GeoCalculator.toLocal(origin, point);
    assertEquals(local.eastMeters(), roundTrip.eastMeters(), 0.01);
    assertEquals(local.northMeters(), roundTrip.northMeters(), 0.01);
  }

  @Test
  void bearingNorthEastIsAboutFortyFiveDegrees() {
    var origin = new GeoPoint(39.0, -105.0);
    var target = GeoCalculator.fromLocal(origin, new LocalPoint(100.0, 100.0, 0.0));
    assertEquals(45.0, GeoCalculator.bearingDegrees(origin, target), 0.1);
    assertTrue(GeoCalculator.distanceMeters(origin, target) > 141.0);
  }
}


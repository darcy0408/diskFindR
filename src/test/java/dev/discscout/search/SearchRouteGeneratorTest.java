package dev.discscout.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.discscout.domain.GeoPoint;
import dev.discscout.simulation.ProbabilityEllipse;
import org.junit.jupiter.api.Test;

final class SearchRouteGeneratorTest {
  @Test
  void generatesSpiralAndLawnMowerRoutes() {
    var generator = new SearchRouteGenerator();
    var origin = new GeoPoint(39.7392, -104.9903);
    var ellipse = new ProbabilityEllipse(50, 100, 35, 18, 30, 0.80);
    assertFalse(generator.spiral(origin, ellipse, 5).waypoints().isEmpty());
    assertEquals("lawn-mower", generator.lawnMower(origin, ellipse, 5).strategy());
  }
}


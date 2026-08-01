package dev.discscout.search;

import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.LocalPoint;
import dev.discscout.geodesy.GeoCalculator;
import dev.discscout.simulation.ProbabilityEllipse;
import java.util.ArrayList;
import java.util.List;

public final class SearchRouteGenerator {
  public enum Vegetation {
    OPEN_GRASS(8.0),
    LIGHT_BRUSH(5.0),
    HEAVY_BRUSH(3.0),
    WOODED(4.0);

    private final double spacingMeters;
    Vegetation(double spacingMeters) { this.spacingMeters = spacingMeters; }
    public double spacingMeters() { return spacingMeters; }
  }

  public SearchRoute spiral(GeoPoint origin, ProbabilityEllipse ellipse, double spacingMeters) {
    var points = new ArrayList<GeoPoint>();
    var maxRadius = Math.max(ellipse.majorAxisMeters(), ellipse.minorAxisMeters());
    for (double radius = 0.0; radius <= maxRadius; radius += spacingMeters) {
      var steps = Math.max(12, (int) Math.ceil(2.0 * Math.PI * Math.max(1.0, radius) / spacingMeters));
      for (var i = 0; i < steps; i++) {
        var angle = i * 2.0 * Math.PI / steps;
        var east = ellipse.centerEastMeters() + Math.sin(angle) * radius;
        var north = ellipse.centerNorthMeters() + Math.cos(angle) * radius;
        points.add(GeoCalculator.fromLocal(origin, new LocalPoint(east, north, 0.0)));
      }
    }
    return new SearchRoute("Expanding spiral", "spiral", spacingMeters, List.copyOf(points));
  }

  public SearchRoute lawnMower(GeoPoint origin, ProbabilityEllipse ellipse, double spacingMeters) {
    var points = new ArrayList<GeoPoint>();
    var theta = Math.toRadians(ellipse.orientationDegrees());
    var alongEast = Math.sin(theta);
    var alongNorth = Math.cos(theta);
    var crossEast = Math.cos(theta);
    var crossNorth = -Math.sin(theta);
    var reverse = false;
    for (double offset = -ellipse.minorAxisMeters(); offset <= ellipse.minorAxisMeters(); offset += spacingMeters) {
      var span = ellipse.majorAxisMeters() * Math.sqrt(Math.max(0.0, 1.0 - Math.pow(offset / ellipse.minorAxisMeters(), 2.0)));
      var a = reverse ? span : -span;
      var b = reverse ? -span : span;
      points.add(toGeo(origin, ellipse, alongEast, alongNorth, crossEast, crossNorth, a, offset));
      points.add(toGeo(origin, ellipse, alongEast, alongNorth, crossEast, crossNorth, b, offset));
      reverse = !reverse;
    }
    return new SearchRoute("Lawn-mower grid", "lawn-mower", spacingMeters, List.copyOf(points));
  }

  private GeoPoint toGeo(GeoPoint origin, ProbabilityEllipse ellipse, double alongEast, double alongNorth, double crossEast, double crossNorth, double along, double cross) {
    var east = ellipse.centerEastMeters() + alongEast * along + crossEast * cross;
    var north = ellipse.centerNorthMeters() + alongNorth * along + crossNorth * cross;
    return GeoCalculator.fromLocal(origin, new LocalPoint(east, north, 0.0));
  }
}


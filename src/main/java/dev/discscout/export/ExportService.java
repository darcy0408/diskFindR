package dev.discscout.export;

import dev.discscout.domain.GeoPoint;
import dev.discscout.search.SearchRoute;
import dev.discscout.simulation.SimulationOutcome;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ExportService {
  public void exportGeoJson(Path target, SimulationOutcome.Success outcome, SearchRoute route) throws IOException {
    Files.createDirectories(target.getParent());
    var sb = new StringBuilder();
    sb.append("{\"type\":\"FeatureCollection\",\"features\":[");
    appendPoint(sb, "median", outcome.medianCoordinate());
    sb.append(',');
    appendLine(sb, route.name(), route);
    sb.append("]}");
    Files.writeString(target, sb.toString());
  }

  public void exportCsv(Path target, SearchRoute route) throws IOException {
    Files.createDirectories(target.getParent());
    var sb = new StringBuilder("name,sequence,latitude,longitude\n");
    for (var i = 0; i < route.waypoints().size(); i++) {
      var p = route.waypoints().get(i);
      sb.append(route.name()).append(',').append(i + 1).append(',').append(p.latitude()).append(',').append(p.longitude()).append('\n');
    }
    Files.writeString(target, sb.toString());
  }

  public void exportPrintableSummary(Path target, SimulationOutcome.Success outcome, SearchRoute route) throws IOException {
    Files.createDirectories(target.getParent());
    var text = """
        # DiscScout Search Summary

        The predicted area is an estimate, not a guarantee.

        Median search anchor: %.6f, %.6f
        Confidence label: %s
        Route: %s, %.1f m spacing, %d waypoints

        Respect private property, traffic, water, cliffs, restricted areas, and other hazards.
        Search safely and remain aware of other players.
        """.formatted(
        outcome.medianCoordinate().latitude(),
        outcome.medianCoordinate().longitude(),
        outcome.confidenceLabel(),
        route.name(),
        route.spacingMeters(),
        route.waypoints().size());
    Files.writeString(target, text);
  }

  private void appendPoint(StringBuilder sb, String name, GeoPoint point) {
    sb.append("{\"type\":\"Feature\",\"properties\":{\"name\":\"").append(name)
        .append("\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[")
        .append(point.longitude()).append(',').append(point.latitude()).append("]}}");
  }

  private void appendLine(StringBuilder sb, String name, SearchRoute route) {
    sb.append("{\"type\":\"Feature\",\"properties\":{\"name\":\"").append(name)
        .append("\"},\"geometry\":{\"type\":\"LineString\",\"coordinates\":[");
    for (var i = 0; i < route.waypoints().size(); i++) {
      if (i > 0) sb.append(',');
      var p = route.waypoints().get(i);
      sb.append('[').append(p.longitude()).append(',').append(p.latitude()).append(']');
    }
    sb.append("]}}");
  }
}


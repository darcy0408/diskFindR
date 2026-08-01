package dev.discscout.search;

import dev.discscout.domain.GeoPoint;
import java.util.List;

public record SearchRoute(String name, String strategy, double spacingMeters, List<GeoPoint> waypoints) {
}


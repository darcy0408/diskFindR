package dev.discscout.simulation;

import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.LocalPoint;

public record LandingSample(LocalPoint localPoint, GeoPoint geoPoint, boolean valid) {
}


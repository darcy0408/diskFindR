package dev.discscout.mobile;

import dev.discscout.domain.GeoPoint;

public record PhoneLocationUpdate(String sessionCode, GeoPoint coordinate, double accuracyMeters, String label) {
}
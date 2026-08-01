package dev.discscout.simulation;

public record ProbabilityEllipse(
    double centerEastMeters,
    double centerNorthMeters,
    double majorAxisMeters,
    double minorAxisMeters,
    double orientationDegrees,
    double probability) {
}


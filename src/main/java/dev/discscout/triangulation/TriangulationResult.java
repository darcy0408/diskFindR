package dev.discscout.triangulation;

public record TriangulationResult(boolean valid, Vector3 midpoint, double raySeparationMeters, String reason) {
}


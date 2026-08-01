package dev.discscout.domain;

public record LocalPoint(double eastMeters, double northMeters, double upMeters) {
  public double horizontalDistance() {
    return Math.hypot(eastMeters, northMeters);
  }
}


package dev.discscout.domain;

public record Wind(double speedMps, double directionFromDegrees, double gustMps, WindSource source) {
  public Wind {
    if (speedMps < 0.0 || gustMps < 0.0 || !Double.isFinite(speedMps) || !Double.isFinite(gustMps)) {
      throw new IllegalArgumentException("Wind speed and gust must be finite non-negative values.");
    }
    directionFromDegrees = ((directionFromDegrees % 360.0) + 360.0) % 360.0;
  }

  public double eastMps() {
    var toward = Math.toRadians(directionFromDegrees + 180.0);
    return speedMps * Math.sin(toward);
  }

  public double northMps() {
    var toward = Math.toRadians(directionFromDegrees + 180.0);
    return speedMps * Math.cos(toward);
  }
}


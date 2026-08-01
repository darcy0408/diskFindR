package dev.discscout.domain;

public record MeasurementUncertainty(
    double speedStdDevMps,
    double bearingStdDevDegrees,
    double launchStdDevDegrees,
    double hyzerStdDevDegrees,
    double windStdDevMps,
    double windDirectionStdDevDegrees,
    double stabilityStdDev,
    double videoPositionStdDevMeters) {
  public static MeasurementUncertainty soloDefault() {
    return new MeasurementUncertainty(2.2, 8.0, 4.0, 6.0, 1.8, 18.0, 0.22, 11.0);
  }

  public static MeasurementUncertainty precisionDefault() {
    return new MeasurementUncertainty(1.4, 4.0, 2.5, 4.0, 1.5, 14.0, 0.18, 5.0);
  }
}


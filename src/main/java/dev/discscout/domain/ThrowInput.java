package dev.discscout.domain;

public record ThrowInput(
    GeoPoint releasePoint,
    double bearingDegrees,
    double releaseSpeedMps,
    double launchAngleDegrees,
    double hyzerAngleDegrees,
    double releaseHeightMeters,
    DiscProfile disc,
    ThrowType throwType,
    Handedness handedness,
    Wind wind,
    MeasurementUncertainty uncertainty) {
}

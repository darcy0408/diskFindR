package dev.discscout.physics;

import dev.discscout.domain.DiscProfile;

public final class SimplifiedAerodynamicModel implements AerodynamicModel {
  @Override
  public double dragDecayPerSecond(DiscProfile disc, double speedMps) {
    var massFactor = massSensitivity(disc);
    var driverEfficiency = Math.max(0.72, 1.45 - disc.speed() * 0.060);
    return (0.035 + Math.max(0.0, speedMps - 18.0) * 0.0012) * massFactor * driverEfficiency;
  }

  @Override
  public double liftAcceleration(DiscProfile disc, double speedMps, double launchAngleDegrees) {
    var clampedSpeed = Math.max(0.0, Math.min(32.0, speedMps));
    var cruise = Math.pow(clampedSpeed / 24.0, 1.35);
    var glideFactor = 0.72 + disc.glide() * 0.075;
    var speedClassFactor = 0.70 + disc.speed() * 0.025;
    var noseAngleFactor = 0.92 + Math.cos(Math.toRadians(Math.max(-20.0, Math.min(35.0, launchAngleDegrees)))) * 0.10;
    var massFactor = massSensitivity(disc);
    return Math.min(7.45 * massFactor, 6.85 * cruise * glideFactor * speedClassFactor * noseAngleFactor * massFactor);
  }

  @Override
  public double lateralTurnFadeAcceleration(DiscProfile disc, double speedMps, double hyzerDegrees, int handednessSign) {
    var highSpeedTurn = speedMps > 16.0 ? disc.turn() * 0.11 * (speedMps - 16.0) : 0.0;
    var lowSpeedFade = speedMps < 14.0 ? disc.fade() * 0.18 * (14.0 - speedMps) / 14.0 : 0.0;
    var hyzerEffect = hyzerDegrees * 0.035;
    var stability = disc.stabilityAdjustment() * 0.35;
    return handednessSign * (lowSpeedFade + stability + hyzerEffect + highSpeedTurn) * massSensitivity(disc);
  }

  public static double massSensitivity(DiscProfile disc) {
    return Math.max(0.85, Math.min(1.18, 173.0 / disc.massGrams()));
  }
}

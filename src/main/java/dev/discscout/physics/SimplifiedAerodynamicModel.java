package dev.discscout.physics;

import dev.discscout.domain.DiscProfile;

public final class SimplifiedAerodynamicModel implements AerodynamicModel {
  @Override
  public double dragCoefficient(DiscProfile disc, double speedMps) {
    var massFactor = massSensitivity(disc);
    return (0.010 + disc.speed() * 0.0007 + Math.max(0.0, speedMps - 20.0) * 0.00025) * massFactor;
  }

  @Override
  public double liftAcceleration(DiscProfile disc, double speedMps, double launchAngleDegrees) {
    var glideLift = disc.glide() * 0.08;
    var angleLift = Math.cos(Math.toRadians(Math.max(-20.0, Math.min(35.0, launchAngleDegrees)))) * 0.18;
    return Math.min(6.0, (glideLift + angleLift) * speedMps / 3.0 * massSensitivity(disc));
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


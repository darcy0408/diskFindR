package dev.discscout.physics;

import dev.discscout.domain.DiscProfile;

public interface AerodynamicModel {
  double dragCoefficient(DiscProfile disc, double speedMps);
  double liftAcceleration(DiscProfile disc, double speedMps, double launchAngleDegrees);
  double lateralTurnFadeAcceleration(DiscProfile disc, double speedMps, double hyzerDegrees, int handednessSign);
}


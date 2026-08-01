package dev.discscout.physics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.discscout.domain.DiscProfile;
import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.Handedness;
import dev.discscout.domain.MeasurementUncertainty;
import dev.discscout.domain.ThrowInput;
import dev.discscout.domain.ThrowType;
import dev.discscout.domain.Wind;
import dev.discscout.domain.WindSource;
import org.junit.jupiter.api.Test;

final class FlightSimulatorTest {
  @Test
  void simulatedDiscEventuallyIntersectsGround() {
    var input = new ThrowInput(
        new GeoPoint(39.7392, -104.9903),
        30.0,
        20.0,
        6.0,
        0.0,
        DiscProfile.builtIns().get(1),
        ThrowType.BACKHAND,
        Handedness.RIGHT,
        new Wind(0, 0, 0, new WindSource.Assumed("test")),
        MeasurementUncertainty.soloDefault());
    var trajectory = new FlightSimulator(new SimplifiedAerodynamicModel()).simulate(input);
    assertTrue(trajectory.valid(), trajectory.warning());
    assertTrue(trajectory.landingPoint().horizontalDistance() > 20.0);
  }

  @Test
  void lighterDiscIsMoreWindSensitiveThanHeavierDisc() {
    var simulator = new FlightSimulator(new SimplifiedAerodynamicModel());
    var base = DiscProfile.builtIns().get(2);
    var light = new DiscProfile(base.displayName(), base.speed(), base.glide(), base.turn(), base.fade(), base.diameterCm(), 160, base.stabilityAdjustment(), base.notes());
    var heavy = new DiscProfile(base.displayName(), base.speed(), base.glide(), base.turn(), base.fade(), base.diameterCm(), 180, base.stabilityAdjustment(), base.notes());
    var lightTrajectory = simulator.simulate(inputWithDiscAndWind(light, 8.0));
    var heavyTrajectory = simulator.simulate(inputWithDiscAndWind(heavy, 8.0));

    assertTrue(lightTrajectory.valid(), lightTrajectory.warning());
    assertTrue(heavyTrajectory.valid(), heavyTrajectory.warning());
    assertTrue(lightTrajectory.landingPoint().eastMeters() > heavyTrajectory.landingPoint().eastMeters());
  }

  private ThrowInput inputWithDiscAndWind(DiscProfile disc, double windMps) {
    return new ThrowInput(
        new GeoPoint(39.7392, -104.9903),
        0.0,
        22.0,
        8.0,
        0.0,
        disc,
        ThrowType.BACKHAND,
        Handedness.RIGHT,
        new Wind(windMps, 270.0, windMps, new WindSource.Manual("test")),
        MeasurementUncertainty.soloDefault());
  }
}


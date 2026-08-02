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
  private final FlightSimulator simulator = new FlightSimulator(new SimplifiedAerodynamicModel());

  @Test
  void simulatedDiscEventuallyIntersectsGround() {
    var trajectory = simulator.simulate(input(DiscProfile.builtIns().get(1), 20.0, 6.0, 1.4, calm()));
    assertTrue(trajectory.valid(), trajectory.warning());
    assertTrue(trajectory.landingPoint().horizontalDistance() > 20.0);
  }

  @Test
  void distanceDriverAtRealisticArmSpeedHasPlausibleDistanceAndHangTime() {
    var trajectory = simulator.simulate(input(DiscProfile.builtIns().get(3), 26.0, 12.0, 1.4, calm()));
    var distance = trajectory.landingPoint().horizontalDistance();
    var hangSeconds = trajectory.points().getLast().timeSeconds();

    assertTrue(trajectory.valid(), trajectory.warning());
    assertTrue(distance > 95.0 && distance < 125.0, "distance " + distance + " m");
    assertTrue(hangSeconds > 3.5 && hangSeconds < 6.5, "hang " + hangSeconds + " s");
  }

  @Test
  void discSelectionMateriallyChangesLandingDistance() {
    var putter = simulator.simulate(input(DiscProfile.builtIns().get(0), 26.0, 12.0, 1.4, calm()));
    var driver = simulator.simulate(input(DiscProfile.builtIns().get(3), 26.0, 12.0, 1.4, calm()));
    var difference = driver.landingPoint().horizontalDistance() - putter.landingPoint().horizontalDistance();

    assertTrue(putter.valid(), putter.warning());
    assertTrue(driver.valid(), driver.warning());
    assertTrue(difference >= 50.0, "distance difference " + difference + " m");
  }

  @Test
  void strongWindMateriallyChangesLandingPoint() {
    var driver = DiscProfile.builtIns().get(3);
    var calmTrajectory = simulator.simulate(input(driver, 26.0, 12.0, 1.4, calm()));
    var crosswindTrajectory = simulator.simulate(input(driver, 26.0, 12.0, 1.4, new Wind(10.0, 270.0, 10.0, new WindSource.Manual("test"))));
    var shift = Math.hypot(
        crosswindTrajectory.landingPoint().eastMeters() - calmTrajectory.landingPoint().eastMeters(),
        crosswindTrajectory.landingPoint().northMeters() - calmTrajectory.landingPoint().northMeters());

    assertTrue(calmTrajectory.valid(), calmTrajectory.warning());
    assertTrue(crosswindTrajectory.valid(), crosswindTrajectory.warning());
    assertTrue(shift >= 20.0, "wind shift " + shift + " m");
  }

  @Test
  void releaseHeightAffectsLandingDistance() {
    var disc = DiscProfile.builtIns().get(2);
    var low = simulator.simulate(input(disc, 22.0, 8.0, 0.6, calm()));
    var high = simulator.simulate(input(disc, 22.0, 8.0, 2.2, calm()));

    assertTrue(low.valid(), low.warning());
    assertTrue(high.valid(), high.warning());
    assertTrue(high.landingPoint().horizontalDistance() > low.landingPoint().horizontalDistance() + 3.0);
  }

  @Test
  void lighterDiscIsMoreWindSensitiveThanHeavierDisc() {
    var base = DiscProfile.builtIns().get(2);
    var light = new DiscProfile(base.displayName(), base.speed(), base.glide(), base.turn(), base.fade(), base.diameterCm(), 160, base.stabilityAdjustment(), base.notes());
    var heavy = new DiscProfile(base.displayName(), base.speed(), base.glide(), base.turn(), base.fade(), base.diameterCm(), 180, base.stabilityAdjustment(), base.notes());
    var wind = new Wind(8.0, 270.0, 8.0, new WindSource.Manual("test"));
    var lightTrajectory = simulator.simulate(input(light, 22.0, 8.0, 1.4, wind));
    var heavyTrajectory = simulator.simulate(input(heavy, 22.0, 8.0, 1.4, wind));

    assertTrue(lightTrajectory.valid(), lightTrajectory.warning());
    assertTrue(heavyTrajectory.valid(), heavyTrajectory.warning());
    assertTrue(lightTrajectory.landingPoint().eastMeters() > heavyTrajectory.landingPoint().eastMeters());
  }

  private ThrowInput input(DiscProfile disc, double speedMps, double launchDegrees, double releaseHeightMeters, Wind wind) {
    return new ThrowInput(
        new GeoPoint(39.7392, -104.9903),
        0.0,
        speedMps,
        launchDegrees,
        0.0,
        releaseHeightMeters,
        disc,
        ThrowType.BACKHAND,
        Handedness.RIGHT,
        wind,
        MeasurementUncertainty.soloDefault());
  }

  private Wind calm() {
    return new Wind(0, 0, 0, new WindSource.Assumed("test"));
  }
}

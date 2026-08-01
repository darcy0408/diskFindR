package dev.discscout.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.discscout.domain.DiscProfile;
import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.Handedness;
import dev.discscout.domain.MeasurementUncertainty;
import dev.discscout.domain.ThrowInput;
import dev.discscout.domain.ThrowType;
import dev.discscout.domain.Wind;
import dev.discscout.domain.WindSource;
import dev.discscout.physics.FlightSimulator;
import dev.discscout.physics.SimplifiedAerodynamicModel;
import org.junit.jupiter.api.Test;

final class MonteCarloSimulatorTest {
  @Test
  void seededMonteCarloIsReproducible() {
    var sim = simulator();
    var a = assertInstanceOf(SimulationOutcome.Success.class, sim.run(input(0), 120, 1234L));
    var b = assertInstanceOf(SimulationOutcome.Success.class, sim.run(input(0), 120, 1234L));
    assertEquals(a.medianCoordinate().latitude(), b.medianCoordinate().latitude(), 0.0);
    assertEquals(a.medianCoordinate().longitude(), b.medianCoordinate().longitude(), 0.0);
  }

  @Test
  void strongerCrosswindShiftsDistribution() {
    var sim = simulator();
    var calm = assertInstanceOf(SimulationOutcome.Success.class, sim.run(input(0), 140, 77L));
    var westWind = assertInstanceOf(SimulationOutcome.Success.class, sim.run(input(8), 140, 77L));
    assertTrue(westWind.meanCoordinate().longitude() > calm.meanCoordinate().longitude());
  }

  @Test
  void probabilityEllipseGrowsWithUncertainty() {
    var low = MonteCarloSimulator.ellipse(0, 0, 25, 0, 16, 0.50);
    var high = MonteCarloSimulator.ellipse(0, 0, 100, 0, 64, 0.50);
    assertTrue(high.majorAxisMeters() > low.majorAxisMeters());
  }

  private MonteCarloSimulator simulator() {
    return new MonteCarloSimulator(new FlightSimulator(new SimplifiedAerodynamicModel()));
  }

  private ThrowInput input(double windMps) {
    return new ThrowInput(
        new GeoPoint(39.7392, -104.9903),
        0.0,
        22.0,
        8.0,
        0.0,
        DiscProfile.builtIns().get(2),
        ThrowType.BACKHAND,
        Handedness.RIGHT,
        new Wind(windMps, 270.0, windMps, new WindSource.Manual("test")),
        MeasurementUncertainty.soloDefault());
  }
}


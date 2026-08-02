package dev.discscout.simulation;

import dev.discscout.domain.DiscProfile;
import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.ThrowInput;
import dev.discscout.domain.Wind;
import dev.discscout.domain.WindSource;
import dev.discscout.geodesy.GeoCalculator;
import dev.discscout.physics.FlightSimulator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class MonteCarloSimulator {
  private final FlightSimulator simulator;

  public MonteCarloSimulator(FlightSimulator simulator) {
    this.simulator = simulator;
  }

  public SimulationOutcome run(ThrowInput input, int trajectories, long seed) {
    var random = new Random(seed);
    var samples = new ArrayList<LandingSample>(trajectories);
    for (var i = 0; i < trajectories; i++) {
      var perturbed = perturb(input, random);
      var trajectory = simulator.simulate(perturbed);
      if (trajectory.valid()) {
        samples.add(new LandingSample(
            trajectory.landingPoint(),
            GeoCalculator.fromLocal(input.releasePoint(), trajectory.landingPoint()),
            true));
      }
    }
    if (samples.size() < Math.max(30, trajectories / 5)) {
      return new SimulationOutcome.TooFewValidTrajectories(trajectories, samples.size(), "Too few simulated flights reached the ground with valid values.");
    }
    return summarize(input.releasePoint(), trajectories, seed, samples);
  }

  private ThrowInput perturb(ThrowInput input, Random random) {
    var u = input.uncertainty();
    var disc = input.disc();
    var perturbedDisc = new DiscProfile(
        disc.displayName(),
        disc.speed(),
        disc.glide(),
        disc.turn(),
        disc.fade(),
        disc.diameterCm(),
        disc.massGrams(),
        disc.stabilityAdjustment() + random.nextGaussian() * u.stabilityStdDev(),
        disc.notes());
    var wind = new Wind(
        Math.max(0.0, input.wind().speedMps() + random.nextGaussian() * u.windStdDevMps()),
        input.wind().directionFromDegrees() + random.nextGaussian() * u.windDirectionStdDevDegrees(),
        Math.max(0.0, input.wind().gustMps() + random.nextGaussian() * u.windStdDevMps()),
        new WindSource.Combined(input.wind().source().label(), "Monte Carlo perturbation"));
    return new ThrowInput(
        input.releasePoint(),
        input.bearingDegrees() + random.nextGaussian() * u.bearingStdDevDegrees(),
        Math.max(4.0, input.releaseSpeedMps() + random.nextGaussian() * u.speedStdDevMps()),
        input.launchAngleDegrees() + random.nextGaussian() * u.launchStdDevDegrees(),
        input.hyzerAngleDegrees() + random.nextGaussian() * u.hyzerStdDevDegrees(),
        Math.max(0.2, input.releaseHeightMeters() + random.nextGaussian() * 0.15),
        perturbedDisc,
        input.throwType(),
        input.handedness(),
        wind,
        input.uncertainty());
  }

  private SimulationOutcome.Success summarize(GeoPoint origin, int requested, long seed, List<LandingSample> samples) {
    var east = samples.stream().mapToDouble(s -> s.localPoint().eastMeters()).average().orElse(0.0);
    var north = samples.stream().mapToDouble(s -> s.localPoint().northMeters()).average().orElse(0.0);
    var anchorSample = samples.stream()
        .min(Comparator.comparingDouble(s -> Math.hypot(s.localPoint().eastMeters() - east, s.localPoint().northMeters() - north)))
        .orElse(samples.getFirst());
    var median = anchorSample.geoPoint();
    var mean = GeoCalculator.fromLocal(origin, new dev.discscout.domain.LocalPoint(east, north, 0.0));

    var cxx = 0.0;
    var cxy = 0.0;
    var cyy = 0.0;
    var maxSpread = 0.0;
    for (var sample : samples) {
      var dx = sample.localPoint().eastMeters() - east;
      var dy = sample.localPoint().northMeters() - north;
      cxx += dx * dx;
      cxy += dx * dy;
      cyy += dy * dy;
      maxSpread = Math.max(maxSpread, Math.hypot(dx, dy));
    }
    cxx /= samples.size() - 1.0;
    cxy /= samples.size() - 1.0;
    cyy /= samples.size() - 1.0;
    var ellipse50 = ellipse(east, north, cxx, cxy, cyy, 0.50);
    var ellipse80 = ellipse(east, north, cxx, cxy, cyy, 0.80);
    var ellipse95 = ellipse(east, north, cxx, cxy, cyy, 0.95);
    var confidence = maxSpread < 45.0 ? "Moderate" : maxSpread < 85.0 ? "Wide" : "Very wide";
    var explanations = List.of(
        "The search area is widened by release speed, bearing, launch angle, wind, and video measurement uncertainty.",
        "Solo Mode cannot uniquely recover the full three-dimensional flight, so depth uncertainty remains significant.",
        "Median and mean coordinates are search anchors, not guaranteed landing points.");
    return new SimulationOutcome.Success(seed, requested, samples.size(), median, mean, cxx, cxy, cyy, ellipse50, ellipse80, ellipse95, maxSpread, confidence, explanations, List.copyOf(samples));
  }

  public static ProbabilityEllipse ellipse(double centerEast, double centerNorth, double cxx, double cxy, double cyy, double probability) {
    var trace = cxx + cyy;
    var determinantTerm = Math.sqrt(Math.pow(cxx - cyy, 2.0) + 4.0 * cxy * cxy);
    var lambdaMajor = Math.max(0.0, (trace + determinantTerm) / 2.0);
    var lambdaMinor = Math.max(0.0, (trace - determinantTerm) / 2.0);
    var chiSquareScale = -2.0 * Math.log(1.0 - probability);
    var orientation = 0.5 * Math.toDegrees(Math.atan2(2.0 * cxy, cxx - cyy));
    return new ProbabilityEllipse(centerEast, centerNorth,
        Math.sqrt(lambdaMajor * chiSquareScale),
        Math.sqrt(lambdaMinor * chiSquareScale),
        orientation,
        probability);
  }
}


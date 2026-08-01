package dev.discscout.physics;

import dev.discscout.domain.Handedness;
import dev.discscout.domain.LocalPoint;
import dev.discscout.domain.ThrowInput;
import java.util.ArrayList;

public final class FlightSimulator {
  private static final double GRAVITY = 9.80665;
  private static final double DT = 0.04;
  private static final double MAX_TIME_SECONDS = 12.0;

  private final AerodynamicModel model;

  public FlightSimulator(AerodynamicModel model) {
    this.model = model;
  }

  public Trajectory simulate(ThrowInput input) {
    var bearing = Math.toRadians(input.bearingDegrees());
    var launch = Math.toRadians(input.launchAngleDegrees());
    var forward = Math.max(1.0, input.releaseSpeedMps()) * Math.cos(launch);
    var east = forward * Math.sin(bearing);
    var north = forward * Math.cos(bearing);
    var up = Math.max(-2.0, input.releaseSpeedMps() * Math.sin(launch));
    var x = 0.0;
    var y = 0.0;
    var z = Math.max(0.2, 1.4);
    var points = new ArrayList<TrajectoryPoint>();
    var handednessSign = input.handedness() == Handedness.RIGHT ? 1 : -1;
    var windCarryFactor = 0.25 * SimplifiedAerodynamicModel.massSensitivity(input.disc());
    var previous = new LocalPoint(x, y, z);

    for (var t = 0.0; t <= MAX_TIME_SECONDS; t += DT) {
      var relativeEast = east - input.wind().eastMps();
      var relativeNorth = north - input.wind().northMps();
      var horizontalSpeed = Math.hypot(relativeEast, relativeNorth);
      var speed = Math.sqrt(horizontalSpeed * horizontalSpeed + up * up);
      if (!Double.isFinite(speed) || speed > 120.0) {
        return new Trajectory(points, previous, false, "Simulation produced unstable velocity.");
      }

      points.add(new TrajectoryPoint(t, new LocalPoint(x, y, z), speed));
      var drag = model.dragCoefficient(input.disc(), speed);
      east -= drag * relativeEast * DT;
      north -= drag * relativeNorth * DT;
      up += (model.liftAcceleration(input.disc(), horizontalSpeed, input.launchAngleDegrees()) - GRAVITY - drag * up) * DT;

      var lateral = model.lateralTurnFadeAcceleration(input.disc(), horizontalSpeed, input.hyzerAngleDegrees(), handednessSign);
      var rightEast = Math.cos(bearing);
      var rightNorth = -Math.sin(bearing);
      east += rightEast * lateral * DT;
      north += rightNorth * lateral * DT;

      previous = new LocalPoint(x, y, z);
      x += (east + input.wind().eastMps() * windCarryFactor) * DT;
      y += (north + input.wind().northMps() * windCarryFactor) * DT;
      z += up * DT;
      if (z <= 0.0 && t > 0.08) {
        var frac = previous.upMeters() / (previous.upMeters() - z);
        var landing = new LocalPoint(
            previous.eastMeters() + (x - previous.eastMeters()) * frac,
            previous.northMeters() + (y - previous.northMeters()) * frac,
            0.0);
        return new Trajectory(points, landing, true, "");
      }
    }
    return new Trajectory(points, new LocalPoint(x, y, Math.max(0.0, z)), false, "Maximum simulation time reached before ground contact.");
  }
}


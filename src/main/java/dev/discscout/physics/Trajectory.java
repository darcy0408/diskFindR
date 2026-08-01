package dev.discscout.physics;

import dev.discscout.domain.LocalPoint;
import java.util.List;

public record Trajectory(List<TrajectoryPoint> points, LocalPoint landingPoint, boolean valid, String warning) {
}


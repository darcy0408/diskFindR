package dev.discscout.physics;

import dev.discscout.domain.LocalPoint;

public record TrajectoryPoint(double timeSeconds, LocalPoint position, double speedMps) {
}


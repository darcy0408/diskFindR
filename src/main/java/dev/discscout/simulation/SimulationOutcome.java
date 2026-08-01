package dev.discscout.simulation;

import dev.discscout.domain.GeoPoint;
import java.util.List;

public sealed interface SimulationOutcome permits SimulationOutcome.Success, SimulationOutcome.TooFewValidTrajectories {
  record Success(
      long seed,
      int requestedTrajectories,
      int validTrajectories,
      GeoPoint medianCoordinate,
      GeoPoint meanCoordinate,
      double covarianceEastEast,
      double covarianceEastNorth,
      double covarianceNorthNorth,
      ProbabilityEllipse probability50,
      ProbabilityEllipse probability80,
      ProbabilityEllipse probability95,
      double maxSpreadMeters,
      String confidenceLabel,
      List<String> explanations,
      List<LandingSample> samples) implements SimulationOutcome {}

  record TooFewValidTrajectories(int requestedTrajectories, int validTrajectories, String reason) implements SimulationOutcome {}
}


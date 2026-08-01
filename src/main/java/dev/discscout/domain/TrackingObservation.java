package dev.discscout.domain;

public sealed interface TrackingObservation permits TrackingObservation.ManualPoint, TrackingObservation.AssistedPoint {
  int frame();
  double confidence();

  record ManualPoint(int frame, double x, double y, double confidence) implements TrackingObservation {}
  record AssistedPoint(int frame, double x, double y, double confidence, String method) implements TrackingObservation {}
}


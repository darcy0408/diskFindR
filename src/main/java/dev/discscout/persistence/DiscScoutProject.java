package dev.discscout.persistence;

import dev.discscout.domain.CaptureMode;
import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.TrackingObservation;
import dev.discscout.domain.VideoMetadata;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class DiscScoutProject {
  public String id = "sample";
  public String name = "Untitled DiscScout Project";
  public Instant createdAt = Instant.now();
  public Instant updatedAt = Instant.now();
  public String captureMode = new CaptureMode.Solo().displayName();
  public GeoPoint releasePoint = new GeoPoint(39.7392, -104.9903);
  public double bearingDegrees = 45.0;
  public VideoMetadata primaryVideo;
  public List<TrackingObservation.ManualPoint> trackingPoints = new ArrayList<>();
  public long simulationSeed = 20260731L;
}


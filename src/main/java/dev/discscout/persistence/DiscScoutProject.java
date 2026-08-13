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
  public GeoPoint releasePoint = new GeoPoint(40.027676, -105.237863);
  public double bearingDegrees = 330.0;
  public VideoMetadata primaryVideo;
  public List<TrackingObservation.ManualPoint> trackingPoints = new ArrayList<>();
  public long simulationSeed = 20260731L;
}


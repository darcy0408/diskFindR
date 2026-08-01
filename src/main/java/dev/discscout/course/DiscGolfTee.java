package dev.discscout.course;

import dev.discscout.domain.GeoPoint;

public record DiscGolfTee(String osmType, long osmId, String ref, GeoPoint coordinate) {
  @Override
  public String toString() {
    return ref == null || ref.isBlank() ? "Tee near course" : "Tee " + ref;
  }
}
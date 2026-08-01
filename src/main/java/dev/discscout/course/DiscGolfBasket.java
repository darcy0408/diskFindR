package dev.discscout.course;

import dev.discscout.domain.GeoPoint;

public record DiscGolfBasket(String osmType, long osmId, String ref, GeoPoint coordinate) {
  @Override
  public String toString() {
    return ref == null || ref.isBlank() ? "Basket near course" : "Basket " + ref;
  }
}
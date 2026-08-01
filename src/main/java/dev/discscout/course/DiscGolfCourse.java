package dev.discscout.course;

import dev.discscout.domain.GeoPoint;
import dev.discscout.geodesy.GeoCalculator;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record DiscGolfCourse(
    String osmType,
    long osmId,
    String name,
    GeoPoint coordinate,
    List<DiscGolfTee> tees,
    List<DiscGolfBasket> baskets,
    String source) {
  public DiscGolfCourse {
    tees = List.copyOf(tees);
    baskets = List.copyOf(baskets);
  }

  public Optional<DiscGolfBasket> bestBasketFor(DiscGolfTee tee) {
    if (baskets.isEmpty()) {
      return Optional.empty();
    }
    if (tee.ref() != null && !tee.ref().isBlank()) {
      var matching = baskets.stream()
          .filter(basket -> tee.ref().equalsIgnoreCase(basket.ref()))
          .findFirst();
      if (matching.isPresent()) {
        return matching;
      }
    }
    return baskets.stream().min(Comparator.comparingDouble(basket -> GeoCalculator.distanceMeters(tee.coordinate(), basket.coordinate())));
  }

  public Optional<Double> suggestedBearingFor(DiscGolfTee tee) {
    return bestBasketFor(tee).map(basket -> GeoCalculator.bearingDegrees(tee.coordinate(), basket.coordinate()));
  }

  @Override
  public String toString() {
    return "%s (%d tees)".formatted(name == null || name.isBlank() ? "Unnamed OSM course" : name, tees.size());
  }
}
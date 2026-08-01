package dev.discscout.course;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.discscout.domain.GeoPoint;
import dev.discscout.geodesy.GeoCalculator;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class OverpassDiscGolfClient {
  private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
  private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  private final ObjectMapper mapper = new ObjectMapper();

  public List<DiscGolfCourse> nearbyCourses(GeoPoint point, int radiusMeters) {
    var query = overpassQuery(point, radiusMeters);
    var body = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    var request = HttpRequest.newBuilder(URI.create(OVERPASS_URL))
        .timeout(Duration.ofSeconds(15))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
    try {
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IllegalStateException("Overpass returned HTTP " + response.statusCode());
      }
      return parse(response.body(), point);
    } catch (IOException e) {
      throw new IllegalStateException("Course lookup failed: " + e.getMessage(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Course lookup was interrupted.", e);
    }
  }

  public List<DiscGolfCourse> parse(String json, GeoPoint searchCenter) throws IOException {
    var root = mapper.readTree(json);
    var courses = new ArrayList<CourseSeed>();
    var tees = new ArrayList<DiscGolfTee>();
    var baskets = new ArrayList<DiscGolfBasket>();

    for (var element : root.path("elements")) {
      var tags = element.path("tags");
      var coordinate = coordinate(element);
      if (coordinate == null) {
        continue;
      }
      var type = element.path("type").asText("element");
      var id = element.path("id").asLong();
      var discGolf = tags.path("disc_golf").asText("");
      if ("disc_golf_course".equals(tags.path("leisure").asText())) {
        courses.add(new CourseSeed(type, id, tags.path("name").asText("Unnamed OSM course"), coordinate));
      } else if ("tee".equals(discGolf)) {
        tees.add(new DiscGolfTee(type, id, ref(tags), coordinate));
      } else if ("basket".equals(discGolf)) {
        baskets.add(new DiscGolfBasket(type, id, ref(tags), coordinate));
      }
    }

    if (courses.isEmpty() && (!tees.isEmpty() || !baskets.isEmpty())) {
      courses.add(new CourseSeed("derived", 0, "Nearby OSM disc golf features", searchCenter));
    }

    var result = new ArrayList<DiscGolfCourse>();
    for (var course : courses) {
      var courseTees = nearbyTees(course.coordinate(), tees);
      var courseBaskets = nearbyBaskets(course.coordinate(), baskets);
      result.add(new DiscGolfCourse(course.osmType(), course.osmId(), course.name(), course.coordinate(), courseTees, courseBaskets, "OpenStreetMap via Overpass"));
    }
    result.sort(Comparator.comparingDouble(course -> GeoCalculator.distanceMeters(searchCenter, course.coordinate())));
    return List.copyOf(result);
  }

  public static String overpassQuery(GeoPoint point, int radiusMeters) {
    var radius = Math.max(250, Math.min(10_000, radiusMeters));
    return """
        [out:json][timeout:12];
        (
          node(around:%d,%.6f,%.6f)[leisure=disc_golf_course];
          way(around:%d,%.6f,%.6f)[leisure=disc_golf_course];
          relation(around:%d,%.6f,%.6f)[leisure=disc_golf_course];
          node(around:%d,%.6f,%.6f)[disc_golf~\"^(tee|basket)$\"];
          way(around:%d,%.6f,%.6f)[disc_golf~\"^(tee|basket)$\"];
        );
        out center tags;
        """.formatted(
        radius, point.latitude(), point.longitude(),
        radius, point.latitude(), point.longitude(),
        radius, point.latitude(), point.longitude(),
        radius, point.latitude(), point.longitude(),
        radius, point.latitude(), point.longitude());
  }

  private static GeoPoint coordinate(JsonNode element) {
    if (element.has("lat") && element.has("lon")) {
      return new GeoPoint(element.path("lat").asDouble(), element.path("lon").asDouble());
    }
    var center = element.path("center");
    if (center.has("lat") && center.has("lon")) {
      return new GeoPoint(center.path("lat").asDouble(), center.path("lon").asDouble());
    }
    return null;
  }

  private static String ref(JsonNode tags) {
    var ref = tags.path("ref").asText("");
    if (!ref.isBlank()) {
      return ref;
    }
    return tags.path("hole").asText("");
  }

  private static List<DiscGolfTee> nearbyTees(GeoPoint coursePoint, List<DiscGolfTee> tees) {
    return tees.stream()
        .filter(tee -> GeoCalculator.distanceMeters(coursePoint, tee.coordinate()) <= 1_500.0)
        .sorted(Comparator.comparing(DiscGolfTee::ref, Comparator.nullsLast(String::compareToIgnoreCase)))
        .toList();
  }

  private static List<DiscGolfBasket> nearbyBaskets(GeoPoint coursePoint, List<DiscGolfBasket> baskets) {
    return baskets.stream()
        .filter(basket -> GeoCalculator.distanceMeters(coursePoint, basket.coordinate()) <= 1_500.0)
        .sorted(Comparator.comparing(DiscGolfBasket::ref, Comparator.nullsLast(String::compareToIgnoreCase)))
        .toList();
  }

  private record CourseSeed(String osmType, long osmId, String name, GeoPoint coordinate) {}
}
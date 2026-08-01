package dev.discscout.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.Wind;
import dev.discscout.domain.WindSource;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class OpenMeteoWindClient {
  private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
  private final ObjectMapper mapper = new ObjectMapper();

  public WeatherResult currentWind(GeoPoint point) {
    var uri = URI.create("https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&current=wind_speed_10m,wind_direction_10m,wind_gusts_10m&wind_speed_unit=ms"
        .formatted(point.latitude(), point.longitude()));
    var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(8)).GET().build();
    try {
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        return unavailable("Open-Meteo returned HTTP " + response.statusCode());
      }
      JsonNode current = mapper.readTree(response.body()).path("current");
      if (current.isMissingNode()) {
        return unavailable("Open-Meteo response did not include current wind.");
      }
      var wind = new Wind(
          current.path("wind_speed_10m").asDouble(0.0),
          current.path("wind_direction_10m").asDouble(0.0),
          current.path("wind_gusts_10m").asDouble(current.path("wind_speed_10m").asDouble(0.0)),
          new WindSource.Online("Open-Meteo model", current.path("time").asText("unknown time")));
      return new WeatherResult(true, wind, "Open-Meteo wind loaded. Treat it as model-derived, not exact fairway wind.");
    } catch (IOException e) {
      return unavailable("Weather retrieval failed: " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return unavailable("Weather retrieval was interrupted.");
    }
  }

  private WeatherResult unavailable(String reason) {
    return new WeatherResult(false, new Wind(0.0, 0.0, 0.0, new WindSource.Assumed("Weather unavailable; zero wind assumption")), reason);
  }
}


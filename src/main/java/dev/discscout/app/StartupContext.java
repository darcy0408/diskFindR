package dev.discscout.app;

import dev.discscout.config.AppConfig;
import dev.discscout.domain.GeoPoint;
import dev.discscout.weather.WeatherResult;

public record StartupContext(AppConfig config, WeatherResult sampleWeather, String javaVersion) {
  public GeoPoint samplePoint() {
    return new GeoPoint(39.7392, -104.9903);
  }
}


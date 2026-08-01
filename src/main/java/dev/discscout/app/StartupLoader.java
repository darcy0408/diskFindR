package dev.discscout.app;

import dev.discscout.config.AppConfig;
import dev.discscout.domain.GeoPoint;
import dev.discscout.weather.OpenMeteoWindClient;
import dev.discscout.weather.WeatherResult;
import java.util.concurrent.StructuredTaskScope;

public final class StartupLoader {
  public StartupContext load() throws InterruptedException {
    try (var scope = StructuredTaskScope.open()) {
      var config = scope.fork(AppConfig::load);
      var weather = scope.fork(() -> new OpenMeteoWindClient().currentWind(new GeoPoint(39.7392, -104.9903)));
      var javaVersion = scope.fork(() -> System.getProperty("java.version"));
      scope.join();
      return new StartupContext(config.get(), weather.get(), javaVersion.get());
    }
  }
}


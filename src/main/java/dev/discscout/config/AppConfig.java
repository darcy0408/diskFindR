package dev.discscout.config;

import dev.discscout.mapping.MapProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record AppConfig(MapProvider mapProvider, boolean aerialConfigured, String message) {
  public static AppConfig load() {
    var envProvider = System.getenv("MAP_PROVIDER");
    var envKey = System.getenv("MAPTILER_KEY");
    var local = Path.of("config", "local.properties");
    var props = new Properties();
    if (Files.exists(local)) {
      try (var in = Files.newInputStream(local)) {
        props.load(in);
      } catch (IOException ignored) {
        return fallback("Could not read config/local.properties; using OpenStreetMap fallback.");
      }
    }
    var provider = firstNonBlank(envProvider, props.getProperty("MAP_PROVIDER"));
    var key = firstNonBlank(envKey, props.getProperty("MAPTILER_KEY"));
    if ("maptiler".equalsIgnoreCase(provider) && key != null) {
      return new AppConfig(new MapProvider.MapTiler(key), true, "Aerial imagery configured with MapTiler.");
    }
    return fallback("No aerial key configured. Using OpenStreetMap fallback; overlays still work.");
  }

  private static AppConfig fallback(String message) {
    return new AppConfig(new MapProvider.OpenStreetMap(), false, message);
  }

  private static String firstNonBlank(String a, String b) {
    if (a != null && !a.isBlank()) return a;
    if (b != null && !b.isBlank()) return b;
    return null;
  }
}


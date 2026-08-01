package dev.discscout.mapping;

public sealed interface MapProvider permits MapProvider.OpenStreetMap, MapProvider.MapTiler {
  String name();
  String tileUrl();
  String attribution();

  record OpenStreetMap() implements MapProvider {
    @Override public String name() { return "OpenStreetMap fallback"; }
    @Override public String tileUrl() { return "https://tile.openstreetmap.org/{z}/{x}/{y}.png"; }
    @Override public String attribution() { return "© OpenStreetMap contributors"; }
  }

  record MapTiler(String key) implements MapProvider {
    @Override public String name() { return "MapTiler satellite"; }
    @Override public String tileUrl() { return "https://api.maptiler.com/tiles/satellite-v2/{z}/{x}/{y}.jpg?key=" + key; }
    @Override public String attribution() { return "© MapTiler © OpenStreetMap contributors"; }
  }
}


package dev.discscout.domain;

public sealed interface WindSource permits WindSource.Online, WindSource.Manual, WindSource.Combined, WindSource.Assumed {
  String label();

  record Online(String provider, String timestamp) implements WindSource {
    @Override public String label() { return provider + " " + timestamp; }
  }

  record Manual(String note) implements WindSource {
    @Override public String label() { return "Manual: " + note; }
  }

  record Combined(String provider, String note) implements WindSource {
    @Override public String label() { return "Combined: " + provider + " + " + note; }
  }

  record Assumed(String reason) implements WindSource {
    @Override public String label() { return "Assumed: " + reason; }
  }
}


package dev.discscout.domain;

public sealed interface CaptureMode permits CaptureMode.Solo, CaptureMode.Precision {
  String displayName();

  record Solo() implements CaptureMode {
    @Override public String displayName() { return "Solo Mode - One Phone"; }
  }

  record Precision() implements CaptureMode {
    @Override public String displayName() { return "Precision Mode - Two Phones"; }
  }
}


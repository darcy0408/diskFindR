package dev.discscout.domain;

public sealed interface AnalysisWarning permits AnalysisWarning.Info, AnalysisWarning.Caution, AnalysisWarning.Blocking {
  String message();

  record Info(String message) implements AnalysisWarning {}
  record Caution(String message) implements AnalysisWarning {}
  record Blocking(String message) implements AnalysisWarning {}
}


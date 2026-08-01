# Java 26 Features

DiscScout targets Java release 26 in `pom.xml` and enables preview features for compilation, tests, and JavaFX runtime.

Visible usage:

- Records for immutable value objects such as `GeoPoint`, `Wind`, `ThrowInput`, and `ProbabilityEllipse`.
- Sealed interfaces for bounded hierarchies such as `CaptureMode`, `WindSource`, `TrackingObservation`, `SimulationOutcome`, `MapProvider`, and `AnalysisWarning`.
- Pattern matching for `SimulationOutcome` handling in the JavaFX workflow.
- Java 26 structured concurrency preview in `StartupLoader`, which concurrently loads map configuration, sample weather, and runtime metadata before the UI opens.

Why structured concurrency is used:

Startup has independent tasks with a shared lifetime. A structured scope keeps those tasks bounded to the startup operation and ensures they are joined before the UI consumes the context.

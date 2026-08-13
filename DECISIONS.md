# DiscScout Decisions

## 2026-07-31: MVP Accuracy Language

DiscScout will display probability regions and warnings instead of exact landing claims. Median and mean coordinates are presented as search anchors only.

## 2026-07-31: Solo Mode First

Solo Mode is the first complete vertical slice because it is the convenience-first workflow and does not require second-phone synchronization or calibration.

## 2026-07-31: Dependency Pinning

JavaFX is pinned to the Java 26 line. Maven Central currently lists JavaFX `26` and `26.0.1`; the project uses `26.0.1` to stay on the latest stable 26 update while still targeting Java 26.

## 2026-07-31: Physics Model Scope

The first model is deterministic and simplified. Aerodynamic behavior is isolated behind an interface so later work can replace it without changing UI or persistence contracts.
## 2026-08-01: OpenStreetMap Tee Data

DiscScout should use OpenStreetMap/Overpass as the public course and tee-coordinate source. OSM has established tags for `leisure=disc_golf_course`, `disc_golf=tee`, `disc_golf=basket`, and `disc_golf=hole`. The app should cache only public map features and should use device/browser location only after explicit user permission.

## 2026-08-01: Review-Driven Physics Calibration

A six-hats review found the original lift model was too ballistic and under-predicted realistic throws. The MVP model is now calibrated to plausibility guardrails rather than laboratory aerodynamics: a generic distance driver at 26 m/s and 12 degrees should land in the 95-125 m range with 3.5-6.5 s hang time; putter and distance-driver profiles must differ materially; strong wind must move the landing point materially. These are regression targets until field-thrown calibration data replaces them.

The Monte Carlo search anchor is now an actual simulated landing sample closest to the cloud center, avoiding an independent east/north median that could fall away from the simulated landing cloud.

## 2026-08-13: Screenshot-Driven UX Pass

A fresh-user walkthrough via screenshots surfaced fixes applied before submission media:

- The disc ComboBox rendered the raw `DiscProfile` record `toString`, which also crushed every simple-mode label to an ellipsis. It now renders `displayName · speed | glide | turn | fade` via a shared cell factory. Flight numbers are the vocabulary disc golfers already know.
- The map no longer draws placeholder probability zones before an estimate has run. `mapJson` sends `hasEstimate`, and the map hides the ellipses, median marker, and throw arrow until a simulation succeeds, labeling the coordinate pill "run Estimate to draw zones". Unrun zones read as real results, which violates the honest-uncertainty rule.
- Probability zones are distinguishable without color: 50% solid, 80% dashed, 95% dotted, matched in both legends.
- The Mark Disc canvas text now acknowledges loaded sample marks instead of claiming "No video yet" underneath a visible mark trail.
- Wind is shown in m/s and mph (US players think in mph); the bearing field is labeled in degrees.

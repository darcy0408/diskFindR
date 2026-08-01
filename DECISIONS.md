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
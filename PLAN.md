# DiscScout Plan

Last updated: 2026-08-01

## Current Status

- Repository initialized and scaffolded as a Java 26 Maven/JavaFX application.
- Portable Oracle JDK 26.0.2 is available under ignored `.jdk/` for local verification.
- JavaFX 26.0.1, JavaCV/Bytedeco 1.5.13, Jackson 2.20.1, JUnit 6.0.0, Maven Surefire 3.5.5, Compiler Plugin 3.15.0, and JaCoCo 0.8.15 are pinned.
- Solo Mode vertical slice is implemented with a six-step guided flow, manual tracking table, JavaCV video metadata, release-coordinate Open-Meteo/manual wind, disc weight classes, deterministic physics, seeded Monte Carlo, probability ellipses, WebView map overlay, configurable route preview, persistence, and exports.

## Completed

- Created `AGENTS.md`, `PLAN.md`, `DECISIONS.md`, README, scripts, Maven Wrapper, and `.gitignore`.
- Implemented domain records and sealed hierarchies.
- Implemented geodesy, wind conversion, simplified flight physics, Monte Carlo uncertainty, covariance ellipses, search-route generation, triangulation math, weather client, map provider abstraction, exports, and project persistence.
- Implemented JavaFX welcome, Solo Mode inputs, video tracking workspace, results map, recording instructions, privacy/limitations, and sample project behavior.
- Reworked the interface into a six-step guided workflow: Setup, Video, Mark Disc, Wind, Estimate, Search. The sample project now guides the demo through synthetic marks, wind, estimate, and search; the results screen leads with a plain-language "Search this zone first" summary, and map overlays include probability ellipses plus configurable route preview lines.
- Added docs, calibration placeholder, sample project JSON, and `submission/` deliverables.
- Fixed Maven Wrapper exit-code propagation.

## Verification Log

- `java -version`: original PATH reports OpenJDK 21.0.10.
- Downloaded portable Oracle JDK 26.0.2 to ignored `.jdk/`.
- `.\mvnw.cmd clean verify` with Java 21: failed as expected, `release version 26 not supported`.
- `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; java -version; .\\mvnw.cmd clean verify`: succeeded on Java 26.0.2.
- After UX/map pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded.
- After mission-card UX pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded.
- After live tee-wind and disc-weight pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded. Test result: 12 tests run, 0 failures, 0 errors, 0 skipped.
- After Simple/Advanced Estimate UI pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded. Test result: 12 tests run, 0 failures, 0 errors, 0 skipped.
- Automatic Wind step: wind now fetches from the tee coordinate when the user reaches the step; raw speed/direction fields are hidden under `Advanced wind override`.
- Nearby course picker pass: added OSM/Overpass course, tee, and basket parser with fixture tests; Setup can search public OSM features and fill tee coordinate/bearing from selected data.
- Click-to-mark pass: Mark Disc now displays the imported video, accepts click marks, draws marker/trail overlays, supports undo/delete, and adjusts simulation uncertainty from mark count.
- Phone helper location pass: added local HTTP helper with six-digit session code, browser geolocation request, session-checked location callback, tests, and Setup controls. HTTPS requirements may block geolocation on some phone LAN browsers.
- Phone helper polish pass: added local ZXing QR-code generation and manual pasted-GPS fallback on the helper page.
- Sample walkthrough polish pass: Open Sample Project now starts on Mark Disc with a persistent Sample Mode banner instead of skipping directly to results.
- Search-route control pass: Search now lets users choose Walk grid or Search spiral and select vegetation spacing before export.
- Confidence legend pass: Search now explains the 50, 80, and 95 percent regions next to the result summary.
- Map fallback pass: WebView map now shows an intentional field-sketch background and tile-status message when raster tiles fail, while overlays remain visible.
- After map-fallback pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded. Test result: 19 tests run, 0 failures, 0 errors, 0 skipped.
- After confidence-legend pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded. Test result: 19 tests run, 0 failures, 0 errors, 0 skipped.
- After search-route control pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded. Test result: 19 tests run, 0 failures, 0 errors, 0 skipped.
- After phone-helper polish pass, `.\mvnw.cmd test`: succeeded. Test result: 11 tests run, 0 failures, 0 errors, 0 skipped.
- JaCoCo report generated at `target/site/jacoco/index.html`.


## User-Friendly UX Plan

Goal: make DiscScout feel like a guided lost-disc rescue assistant for beginners while keeping advanced controls available for serious players.

### Phase 1: Language and Flow Polish

- [x] Rename the guided steps from `Record, Import, Mark, Wind, Simulate, Search` to `Setup, Video, Mark Disc, Wind, Estimate, Search`.
- [x] Rename technical buttons to user-goal language:
  - `Run 500 Trajectories` -> `Estimate Landing Zone`
  - `Use Online Wind` -> `Get Wind Near Tee`
  - `Assume Calm Wind` -> `Continue Without Wind`
  - `Add Disc Point` -> `Mark Disc Here`
- [ ] Keep Solo Mode visually primary and move Precision Mode into an advanced/coming-next area.
- [x] Replace status-log-first messaging with a friendly `What happened` panel that summarizes the last action in plain language.

### Phase 2: Per-Step Mission Cards

- [x] Add a consistent mission card at the top of each step with:
  - Current step number.
  - One-sentence task.
  - Why the step matters.
  - A clear next action.
- [x] Add beginner-friendly empty states:
  - No video yet: show import and sample options.
  - No disc marks yet: explain that 3-5 visible marks are enough.
  - No wind yet: offer online, manual, and calm-wind choices.
  - No estimate yet: show what inputs are still needed.
- [x] Add a persistent sample walkthrough banner that explains the app is using synthetic demo data.

### Phase 3: Simple Mode First, Advanced Later

- [x] Add a Simple/Advanced toggle, defaulting to Simple.
- [x] In Simple Mode, show plain controls:
  - Disc type.
  - Disc weight class.
  - Throw style.
  - Throw direction.
  - Wind feel.
  - Search terrain.
- [x] Move numeric fields such as meters per second, launch angle, hyzer angle, and detailed uncertainty into an Advanced section.
- [ ] Add metric/U.S. customary display labels where values are user-facing.

### Phase 4: Tracking Interaction Honesty

- [x] Make the current tracking placeholder explicit: `Use Sample Marks` as a fallback while true click-to-mark exists for imported videos.
- [x] Implement real click-to-mark on the displayed video frame.
- [ ] Add release-frame selection and frame-step buttons.
- [x] Show point count feedback: `3 marks is enough to estimate; more marks can improve confidence`.
- [x] Add undo/delete controls for tracking points; selected-point visual feedback remains pending.

### Phase 5: Map and Search Confidence

- [x] Make map-tile failure look intentional by showing a useful field-style fallback canvas instead of an error-like blank state.
- [x] Keep release point, route, probability regions, and summary visible when tiles fail.
- [x] Add a visible confidence legend next to the result summary.
- [x] Add route selector: `Search spiral` and `Walk grid`.
- [x] Add vegetation spacing choices on the Search screen.

### Phase 6: Accessibility and Age-Group Review

- [ ] Check the app at common laptop sizes and ensure text does not overflow.
- [ ] Increase hit targets for older users and outdoor touchpad use.
- [ ] Add keyboard navigation for primary step actions.
- [ ] Review copy for teens, adult casual players, older beginners, and serious players.
- [ ] Keep warnings clear but not alarming.

### UX Acceptance Criteria

- [ ] A beginner can open the app and understand the next action within 10 seconds.
- [ ] The sample project reaches the Search screen in one click.
- [ ] A nontechnical user can explain what the colored probability zones mean.
- [ ] The app never appears broken when map tiles or wind lookup fail.
- [ ] Advanced users can still access the numeric controls used by the model.

## Nearby Courses and Tee Coordinates Plan

- [x] Add a geolocation permission flow in the optional phone upload page or browser-based helper. Use location only when the user grants permission.
- [x] Add an OpenStreetMap/Overpass course lookup service for nearby `leisure=disc_golf_course` features.
- [x] Add tee and basket lookup using `disc_golf=tee` and `disc_golf=basket` around the selected course.
- [x] Let the user pick a course and tee instead of typing latitude/longitude.
- [x] Use the selected tee coordinate as the release-coordinate starting point; manual correction remains available through Advanced details.
- [ ] Cache public OSM feature data with attribution, timestamp, and source URL; do not cache user location by default.
- [x] Add fallback behavior for unmapped courses: public course lookup/manual tee placement remains available when phone geolocation fails.
## Remaining Work

- Launch and visually inspect the revised stepper UI on the user's desktop.
- Add true frame-forward/frame-back controls and timeline scrubbing over decoded video frames.
- Add real optical-flow assisted tracking.
- Replace calibration placeholder with generated/detected OpenCV ArUco marker board.
- Implement mobile upload session code/QR page; video upload remains pending.
- Add Precision Mode UI for synchronized two-video marking and fallback comparison.
- Add diagnostic bundle creation and privacy scrubbing.
- Capture screenshots and record the 90-120 second demonstration video.

## Known Limitations

- The map now uses a dependency-free local slippy-tile renderer in WebView with OSM fallback, but Leaflet/MapLibre assets are not bundled yet.
- Video display uses JavaFX MediaView and JavaCV metadata; frame-accurate stepping is not complete.
- Physics is intentionally simplified and validation-oriented, not laboratory aerodynamics.
- `projects/` output is ignored to avoid committing user media and exact private coordinates.

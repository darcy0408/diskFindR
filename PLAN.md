# DiscScout Plan

Last updated: 2026-08-01

## Current Status

- Repository initialized and scaffolded as a Java 26 Maven/JavaFX application.
- Portable Oracle JDK 26.0.2 is available under ignored `.jdk/` for local verification.
- JavaFX 26.0.1, JavaCV/Bytedeco 1.5.13, Jackson 2.20.1, JUnit 6.0.0, Maven Surefire 3.5.5, Compiler Plugin 3.15.0, and JaCoCo 0.8.15 are pinned.
- Solo Mode vertical slice is implemented with a six-step guided flow, manual tracking table, JavaCV video metadata, release-coordinate Open-Meteo/manual wind, disc weight classes, review-calibrated deterministic physics, seeded Monte Carlo, probability ellipses, WebView map overlay, configurable route preview, session-hardened phone helper, persistence, exports, MIT license, and Java 26 CI workflow.

## Audit follow-ups (2026-10-05 hostile audit)

Follow-up tasks from the 2026-10-05 hostile audit (grade C+): hygiene is good, but map geometry and several advertised behaviours need fixing.

- **MT-001** [open] CODE / P1 High — **Project probability ellipses geographically on the map** (finding H-1) — `src/main/resources/dev/discscout/mapping/map.html:200-207` sizes ellipses as `radiusMeters * 2.4` px with a fixed `.58` aspect, independent of zoom, while markers and route use `lonLatToPixel` (map.html:178-180); `DiscScoutApplication.java:782-785` passes only `majorAxisMeters()`, so the minor axis is dropped. **Action:** Convert semi-major/semi-minor through the Mercator scale (m/px = 156543.03 * cos(lat) / 2^z), pass both axes and the orientation from DiscScoutApplication.java, and re-render on zoom. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-002** [open] CODE / P1 High — **Fix search-route angle convention and test route-in-ellipse** (finding H-2) — `MonteCarloSimulator.java:114` returns orientation as an angle from east (counter-clockwise), but `SearchRouteGenerator.java:49-53` treats it as a compass bearing (`alongEast = sin`, `alongNorth = cos`), misaligning the sweep by (90 - 2*theta) degrees; `SearchRouteGeneratorTest.java:12-18` only checks non-empty output. **Action:** Use `along = (cos, sin)`, `cross = (-sin, cos)` in east/north terms, and add a test with an elongated, rotated covariance asserting the waypoints lie inside the ellipse (it must fail on the current code). Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-003** [open] CODE / P1 High — **Make throw style reach FlightSimulator or remove the control** (finding H-3a) — `ThrowType` is carried in `ThrowInput` but `FlightSimulator.java` only reads `handedness` (line 30), so a forehand is simulated as a backhand with the fade on the wrong side. **Action:** Flip the lateral/fade sign for forehand in FlightSimulator and add a test that RH forehand and RH backhand fade to opposite sides; otherwise remove the Throw style control from Simple Mode. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-004** [open] CODE / P1 High — **Make video marks meaningful and stop sample marks inflating confidence** (finding H-3b, H-3c) — Mark x/y are stored but unused; only the mark count changes uncertainty (`DiscScoutApplication.java:706-715`), while the Video card claims "The video helps DiscScout estimate speed" (line 447); "Use Sample Marks" (lines 931-937) adds synthetic points in a real Solo project, reaching the tightest uncertainty tier with no real observations. **Action:** Either derive speed/direction from the marks or correct the Video-step and README copy; disable "Use Sample Marks" outside Sample Mode and exclude synthetic marks from the uncertainty tier. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-005** [open] CODE / P1 High — **Handle weather failure honestly and widen the zone** (finding H-4) — `OpenMeteoWindClient.java:46-48` returns `available=false` with zero wind, but `DiscScoutApplication.java:746-755` never checks `available()`, writes 0.0 into the fields and shows "Nearby wind loaded: 0.0 m/s"; no code path widens uncertainty although README and the Wind card promise "a wider search zone". **Action:** Branch on `WeatherResult.available()`, show a clear failure message, increase `windStdDevMps` whenever wind is assumed, and add a test for the unavailable path. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-006** [open] CODE / P1 High — **Stop auto-sending exact coordinates; make the startup network call offline-safe** (finding M-7) — Open-Meteo (`OpenMeteoWindClient.java:20`) and Overpass (`OverpassDiscGolfClient.java:86-101`) receive `%.6f` (~10 cm) coordinates, the Wind step fetches automatically on entry (`DiscScoutApplication.java:189`), and `StartupLoader.java:13` calls Open-Meteo synchronously inside `start()` (`DiscScoutApplication.java:133`), blocking an offline launch for up to ~12 s. Raised above the audit's Medium because it conflicts with the AGENTS.md rule to process coordinates locally by default. **Action:** Round outbound coordinates to 2-3 decimals (or ask before sending), make the wind fetch an explicit user action, drop the startup weather call (or run it asynchronously without blocking the UI), and correct the "Internet access optional" / "locally by default" wording in README and `submission/privacy-and-limitations.md`. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-007** [open] CODE / P2 Medium — **Harden the phone helper against LAN brute force** (finding M-5) — `PhoneHelperServer.java:44` binds `0.0.0.0`, and `:142`/`:266` accept any request with a matching 6-digit code with no rate limit or lockout, so any LAN host can guess the code and overwrite the release coordinate (`DiscScoutApplication.java:317-329`); the compare is not constant-time and `newSessionCode` (`:300-305`) has modulo bias. **Action:** Bind to the selected LAN interface only, add per-IP attempt counting with lockout/session invalidation, use `MessageDigest.isEqual`, generate codes without modulo bias, and add tests. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-008** [open] OWNER / P2 Medium — **Decide on phone GPS: ship TLS or drop the button** (finding M-6) — The helper is plain HTTP on a LAN IP (`PhoneHelperServer.java:44, 71-77`) and mobile browsers require a secure context for `navigator.geolocation`, so "Use My Location For Tee" cannot work on a phone; only the manual paste path is live. **Action:** Choose between self-signed TLS with a QR-delivered fingerprint or removing the GPS button and keeping paste-only, then update the README wording ("many mobile browsers" is in practice all of them). Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-009** [open] CODE / P2 Medium — **Use Locale.ROOT for numbers in URLs and queries** (finding M-8) — `OpenMeteoWindClient.java:20` and `OverpassDiscGolfClient.java:86-101` format `%.6f` with the default locale, so a de-DE/fr-FR machine emits `39,739200` and both services break. **Action:** Use `String.format(Locale.ROOT, ...)` for every number that reaches a URL or query, and add a test that runs under a comma-decimal default locale. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-010** [open] CODE / P2 Medium — **Guard input parsing on the FX thread** (finding M-9) — `refreshMap()` (`DiscScoutApplication.java:764-768`) calls `input()` (`:689-701`), which parses six free-text fields and constructs throwing `Wind`/`GeoPoint` records, unguarded from `start()` (`:152`), the step listener (`:190`), the phone callback (`:322`) and tee selection (`:399`). **Action:** Validate inputs into a result type (or catch and report) so bad text produces a field-level message instead of an exception escaping the listeners. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-011** [open] CODE / P2 Medium — **Fix project directory litter and sample id/name reuse** (finding M-10) — `projectDir` starts at `projects/sample` (`DiscScoutApplication.java:88`), which never has a `project.json`, so `saveProject()` (`:838-840`) creates a new `discscout-sample-project-<uuid>` on the first save of every launch; real Solo throws keep `id="sample"` and the sample name because `project` is only replaced in the sample flow (`:87`, `:227`). **Action:** Create a fresh project id, name and directory when a Solo throw starts and reuse it for later saves. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-012** [open] OWNER / P2 Medium — **Trim the javacv-platform dependency** (finding M-11) — `pom.xml:52-56` pulls `javacv-platform` 1.5.13 (FFmpeg + OpenCV natives for every OS/arch) only to read four metadata fields in `VideoMetadataReader.java:11-15`. **Action:** Decide whether to keep JavaCV (restricted to the needed platform classifiers/modules) or read metadata via JavaFX `Media`, and record the choice in DECISIONS.md. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-013** [open] CODE / P3 Low — **Low-severity correctness and privacy nits** (finding L-12, L-13, L-16, L-17, L-19) — The log says exact coordinates are not logged (`DiscScoutApplication.java:327`) while `:650-651` logs the median at 6 dp and `:745` the tee at 5 dp (README repeats the claim); `json()` (`:1033-1035`) escapes only backslash and quote before `executeScript`; `StartupLoader.java:15` `scope.join()` may fail startup with no user message on a subtask RuntimeException (suspected); `attemptedWindFetch` (`:189`, `:741`) is never reset on manual lat/lon edits; GeoJSON names are unescaped in `ExportService.java:56, 62`. **Action:** Round or drop coordinates in logs (or fix the claim), use Jackson for the `executeScript` payload and GeoJSON, surface startup failures, and reset the wind-fetch flag when the location changes. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).
- **MT-014** [open] HYGIENE / P3 Low — **Remove dead code and review model smells** (finding L-14, L-15, L-18) — `AnalysisWarning` is unused, `ProjectStore.load` is never called and `samples/sample-project/project.json` is never read (sample hard-coded at `DiscScoutApplication.java:978-987`), `demo.ps1` sets an unread `-Ddiscscout.demo=true`, `Triangulator` is unwired and `CaptureMode` is only a display string; `confidenceLabel` keys off the single-outlier `maxSpread` (`MonteCarloSimulator.java:100`); wind is applied twice in `FlightSimulator.java:35-36` and `:56-57`. **Action:** Delete or wire up the dead pieces (load the sample from JSON), base the confidence label on a percentile spread, and revisit the double wind application in the next calibration pass. Full finding: private audit report hostile-audit-2026-10-05.md in this project's Claude memory (C:\Users\Darcy\.claude\projects\C--dev-disk-golf\memory\).

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
- Physics calibration pass: lift/drag tuning now produces plausible distance-driver distance and hang-time guardrails, material disc and wind effects, release-height input, and a spatial Monte Carlo anchor from an actual simulated landing sample.
- Submission hardening pass: added MIT license, Java 26 GitHub Actions workflow, phone-helper session-code checks for page/QR endpoints, `submission/model-validation.md`, and refreshed Java 26 build proof.
- After map-fallback pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded. Test result: 19 tests run, 0 failures, 0 errors, 0 skipped.
- After submission hardening pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded. Test result: 26 tests run, 0 failures, 0 errors, 0 skipped.
- After physics calibration pass, `$env:JAVA_HOME=(Resolve-Path .jdk\\jdk-26.0.2).Path; $env:Path="$env:JAVA_HOME\\bin;$env:Path"; .\\mvnw.cmd clean verify`: succeeded. Test result: 24 tests run, 0 failures, 0 errors, 0 skipped.
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

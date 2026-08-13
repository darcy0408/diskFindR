# DiscScout — Final Hackster Entry (copy-paste fields)

Everything below maps 1:1 to a field in Hackster's project editor. Paste each block
into the matching field. `[IMAGE n]` markers show where to drop each screenshot —
capture them fresh from the current app (the Jul 31 screenshots in `docs/screenshots/`
show the old tab UI and no longer match the story text).

---

## Field: Project name

DiscScout: Probabilistic Disc-Golf Landing Search

## Field: Elevator pitch (140-char limit — this is 129)

Threw your disc into the wild? DiscScout turns one phone video, wind, and disc physics into a probability map and a search route.

## Field: Contest category

Best Hobby Solution (+ BYOD bonus — uses only phones you already own)

## Field: Cover image

Per `cover-image-brief.md`: fairway from above, translucent search ellipses, phone
near the tee, DiscScout name visible. A clean screenshot of the Search map with the
50/80/95% zones over OSM tiles also works if time is short.

---

## Field: Story (main body)

### The wild ate my disc. Again.

Every disc-golf player knows the moment: a good drive, a gust of wind, and your disc
vanishes into brush, trees, glare, or plain distance. The most important part of the
flight — the last few seconds — is exactly the part you couldn't see. Players lose
discs worth $15–25 all the time, and searches can eat 20 minutes of a round while
the group behind you waits.

DiscScout tames that particular wild. It is a Java 26 JavaFX desktop app that
combines a phone video of your throw, your release location, throwing direction,
disc profile, and live wind data into a **probability map** of where the disc
actually landed — plus a practical walking route to search it.

`[IMAGE 1 — Welcome screen with the Setup → Video → Mark Disc → Wind → Estimate →
Search steps and the "Open Sample Project" button. Caption: "The guided wizard.
Judges: Open Sample Project runs the whole flow with synthetic data."]`

### Honest by design: a search area, not a fake X

One phone cannot fully reconstruct a 3D flight, and trees, skips, rolls, and wind
shifts can all move a disc after the camera loses it. So DiscScout deliberately
refuses to claim an exact landing point. Instead it runs a seeded 500-trajectory
Monte Carlo simulation and draws **50%, 80%, and 95% probability regions**, a median
search anchor, and a confidence label — with a plain-language warning that the
anchor is not guaranteed. I believe showing calibrated uncertainty is better UX
than showing a confident wrong answer.

### The five-minute flow

1. **Setup** — enter your release point, or let the optional local phone helper fill
   it in: the desktop app shows a six-digit session code and QR, your phone opens a
   local page, and only after you tap "Use My Location For Tee" does the browser ask
   for location. A paste-your-GPS fallback covers browsers that block geolocation on
   local URLs. DiscScout can also search public OpenStreetMap data for nearby
   courses and pre-fill the tee and throw direction from basket positions.
2. **Video** — import the throw video from your normal phone camera. No app install
   on the phone, no account, no cloud.
3. **Mark Disc** — pause on frames where the disc is visible and click it. Three
   marks are enough; more marks shrink the video-measurement uncertainty.
4. **Wind** — DiscScout queries Open-Meteo automatically from your release
   coordinate. Raw wind numbers stay hidden behind "Advanced wind override" because
   most players don't know them. No weather? It continues with a wider zone.
5. **Estimate** — pick disc type, weight class, throw style, and handedness in
   Simple Mode, or open Advanced model details for release speed, launch angle,
   hyzer/anhyzer, and release height.
6. **Search** — the map shows the probability zones and generates a **walk-grid or
   search-spiral route** tuned to vegetation spacing. Export GeoJSON, CSV, or a
   printable summary and go find your disc.

`[IMAGE 2 — Mark Disc step with click marks and trail overlay]`
`[IMAGE 3 — Wind step with auto-fetched wind]`
`[IMAGE 4 — Estimate step, Simple Mode]`
`[IMAGE 5 — Search map with 50/80/95% zones and route controls]`
`[IMAGE 6 — Phone helper with session code and QR]`

### Use of Modern Java: Java 26 ⭐

Java 26 is not decoration here — it is the whole stack, and the repository proves it.

- **Maven targets release 26 explicitly** (`pom.xml`), and a GitHub Actions workflow
  (`.github/workflows/java-26-verify.yml`) reruns `mvnw.cmd clean verify` on
  `windows-latest` with Java 26 on every push. A committed
  `submission/build-proof-java26.txt` shows the latest local run: **26/26 tests,
  BUILD SUCCESS on JDK 26.0.2 (August 12, 2026)**.
- **Structured concurrency (Java 26 preview)** loads startup context concurrently —
  map configuration, sample weather, and runtime metadata are independent tasks with
  a shared lifetime, which is exactly the structured-scope use case:

```java
try (var scope = StructuredTaskScope.open()) {
  var config = scope.fork(AppConfig::load);
  var weather = scope.fork(() -> new OpenMeteoWindClient().currentWind(releasePoint));
  var javaVersion = scope.fork(() -> System.getProperty("java.version"));
  scope.join();
  return new StartupContext(config.get(), weather.get(), javaVersion.get());
}
```

- **Records + sealed interfaces + pattern matching** model the domain as bounded,
  exhaustively-switchable hierarchies — `SimulationOutcome`, `CaptureMode`,
  `WindSource`, `TrackingObservation`, `MapProvider`, `AnalysisWarning`:

```java
public sealed interface SimulationOutcome
    permits SimulationOutcome.Success, SimulationOutcome.TooFewValidTrajectories {
  record Success(long seed, int requestedTrajectories, int validTrajectories,
      GeoPoint medianCoordinate, /* covariance, 50/80/95 ellipses, samples ... */
      List<String> explanations, List<LandingSample> samples) implements SimulationOutcome {}
  record TooFewValidTrajectories(int requestedTrajectories, int validTrajectories,
      String reason) implements SimulationOutcome {}
}
```

  The JavaFX workflow pattern-matches on the outcome, so "not enough valid
  trajectories" is a first-class UI state, not an exception dialog.
- **Virtual threads** serve the local phone helper's HTTP sessions cheaply.
- **JavaFX 26** with a Java Platform Module System `module-info.java` for the UI.

### Under the hood

The pipeline is: domain records → time-stepped simplified lift/drag flight model →
seeded Monte Carlo (500 trajectories) → covariance ellipses on a geodesic frame →
search-route generation → WebView map overlay (OSM tiles by default, MapTiler
satellite if you add a key, and an intentional field-sketch fallback if tiles fail —
the zones and route always stay visible). Projects persist as plain `project.json`
folders.

The physics model is deliberately simplified — and honestly labeled. After an early
review found it "too ballistic," I added **regression guardrails as tests**: a
26 m/s distance-driver throw must land 95–125 m out with 3.5–6.5 s of hang time; a
driver must out-fly a putter by ≥50 m at equal speed; a 10 m/s crosswind must shift
the estimate ≥20 m; release height must matter. The full table with current values
is in `submission/model-validation.md`. 26 unit tests cover physics, Monte Carlo
behavior, triangulation, geodesy, search routes, the phone-helper server, and
persistence, with JaCoCo reporting across all 58 classes.

### Privacy and safety

Videos and exact coordinates are processed locally by default — no account, no
cloud upload, and the phone helper requires the active session code in the URL, so
nothing is exposed from a bare LAN address. Exact coordinates are kept out of the
diagnostic log. And prominently, in-app: the predicted area is an estimate. Do not
enter roads, water, cliffs, private property, or unsafe terrain. DiscScout is not a
navigation or emergency-location system.

### Limits and what's next

Solo Mode is the working, convenience-first MVP you see in the demo. Precision Mode
(two phones + printed calibration marker + triangulation) is scaffolded and falls
back to Solo Mode when calibration is unusable; real ArUco detection, optical flow,
and QR video upload remain experimental. OpenStreetMap course data and Open-Meteo
wind are approximations, so every auto-filled value stays editable. The next
credibility step is field calibration: throw real discs in an open field and
measure model error against predictions.

### Build it yourself

1. Install JDK 26 and check `java -version` reports 26.
2. `git clone https://github.com/darcy0408/diskFindR` and run
   `.\mvnw.cmd clean verify` from the repo root (CI runs the identical command).
3. `.\run.ps1` to launch, then click **Open Sample Project** to see the entire flow
   with synthetic data — or import a real throw video and find your disc.

`[VIDEO — 90–120 s demo per submission/demo-video-script.md]`

---

## Field: Bill of Materials (add as "things" / hardware list)

- Windows laptop or desktop (any machine that runs JDK 26)
- JDK 26 (free download)
- Your existing phone camera (Solo Mode) — BYOD
- Optional: second phone for Precision Mode experiments — BYOD
- Optional: tripod, bag, or bench to steady the phone
- Optional: printed calibration marker (SVG included in repo)
- Optional: MapTiler key for satellite imagery (OSM fallback works without it)

Software (list in BOM notes or story): JavaFX 26, ZXing 3.5.4 (local QR
generation), JavaCV (video metadata), Jackson, JUnit 5, Maven wrapper.

## Field: Code

- GitHub: https://github.com/darcy0408/diskFindR (MIT license)

## Field: Schematics / CAD

- None required — pure software + BYOD phones. Optionally attach
  `docs/calibration/marker-board.svg` as the printable calibration marker.

---

## Before you click Submit (from final-submission-checklist.md)

1. Register as a participant on the contest page (required for judging).
2. Create the project **via the contest page** ("Create new project") so it attaches
   to the contest.
3. Capture 6 fresh screenshots matching IMAGE 1–6 (run `.\run.ps1`, use Open Sample
   Project). Old `docs/screenshots/` images show the outdated tab UI — don't use.
4. Record the 90–120 s video (`submission/demo-video-script.md`); show
   `java -version` or the build-proof file on screen for the Java 26 proof beat.
5. Commit & push the refreshed `submission/build-proof-java26.txt` (updated
   2026-08-12) so the repo matches the story's claim.
6. Paste the blocks above; upload cover image, screenshots, video; add the GitHub
   link; submit before **August 16, 2026, 11:59 PM PDT**.

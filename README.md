# DiscScout

DiscScout is a Java 26 JavaFX desktop application for estimating the probable landing area of a disc-golf throw from phone video, release location, throw direction, wind, and disc profile.

The application deliberately shows probability regions instead of claiming an exact coordinate. Trees, skips, rolls, wind shifts, camera angle, and disc behavior can all move the actual disc.

## Requirements

- JDK 26 on `PATH`
- Windows PowerShell
- Internet access for first Maven dependency download and optional Open-Meteo wind lookup

This workspace includes an ignored portable Oracle JDK 26.0.2 under `.jdk/` for local verification. The scripts use it automatically when present; otherwise install/select JDK 26 before running the authoritative verification command.

## Build

```powershell
.\build.ps1
```

## Run

```powershell
.\run.ps1
```

or:

```powershell
.\mvnw.cmd javafx:run
```


## Guided Demo Flow

The main screen is organized as **Setup -> Video -> Mark Disc -> Wind -> Estimate -> Search**. For the fastest walkthrough, choose **Open Sample Project**; DiscScout loads synthetic non-personal data, opens Mark Disc with sample observations, and shows a Sample Mode banner that guides the judge through Wind, Estimate, and Search.

The map uses OpenStreetMap tiles by default. If tiles fail or aerial credentials are missing, DiscScout switches to an intentional field-sketch fallback while keeping the release point, probability zones, route, and safety notes visible. On the Search step, users can switch between a walk-grid route and a search-spiral route, then choose vegetation spacing before exporting.
## Aerial Map Key

DiscScout works without aerial credentials by falling back to an OpenStreetMap-compatible development basemap. To use MapTiler satellite tiles, create `config/local.properties` or set environment variables:

```text
MAP_PROVIDER=maptiler
MAPTILER_KEY=your-key
```

Do not commit local credentials.

## Sample Project

Use **Open Sample Project** from the welcome screen. The sample uses synthetic, non-personal coordinates and generated tracking observations.





## Video Marking

After importing a video, the Mark Disc step shows the video with a click overlay. Pause on frames where the disc is visible and click the disc. Marks appear as yellow points with a trail, and the table supports undo/delete. Three marks is enough to continue; four or more marks reduce the model's video-measurement uncertainty. If no usable video is available, **Use Sample Marks** keeps the demo path working.

## Phone Helper

The Setup step can start a local phone helper page with a six-digit session code and locally generated QR code. The page asks the browser for location only after the user taps **Use My Location For Tee**, then sends the coordinate back to the desktop app to fill the release point.

The helper runs locally and does not create an account. Exact coordinates are not written to the diagnostic log. If browser geolocation is blocked, the helper page also accepts pasted latitude/longitude from a phone map app. Many mobile browsers require HTTPS for geolocation on local-network URLs, so the manual GPS fallback remains available.
## Public Course Lookup

The Setup step can search public OpenStreetMap data near the current release coordinate. When mapped data exists, choose a course and tee; DiscScout fills the tee coordinate and uses a matching or nearest basket to suggest the throw direction.

OSM data may be incomplete or slightly wrong, so every filled value remains editable. The desktop app does not store device location; future phone-helper geolocation should request explicit browser permission and use the result only to suggest nearby courses.
## Simple and Advanced Estimate

The Estimate step defaults to Simple Mode with disc type, disc weight, throw style, handedness, and throw direction. Open **Advanced model details** to edit release coordinates, release speed, launch angle, and hyzer/anhyzer angle.
## Wind and Disc Details

On the Wind step, DiscScout automatically uses the current release latitude and longitude to query Open-Meteo. Raw wind speed, direction, and gust fields are hidden in **Advanced wind override** because most players will not know those values. If weather is unavailable, the app can continue with a wider search zone.

On the Estimate step, disc type and disc weight class both feed the simulation. Lighter discs are modeled as slightly more wind-sensitive; heavier discs are modeled as slightly less wind-sensitive. These are approximate inputs, not full aerodynamic measurements.
## Safety

The predicted area is an estimate, not a guarantee. Do not enter roads, water, cliffs, private property, restricted areas, or unsafe terrain. DiscScout is not a navigation or emergency-location system.

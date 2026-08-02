# DiscScout Hackster Draft

Disc-golf players lose discs because the most important part of the flight is often hidden by distance, brush, trees, glare, or a bad viewing angle. DiscScout narrows the search by combining a phone video, release location, throwing direction, disc profile, and wind into a probability zone on a map.

The result is probabilistic because one phone cannot fully reconstruct a three-dimensional flight and wind or obstacles can change the outcome. DiscScout shows 50, 80, and 95 percent regions, a median search anchor, and a practical route, while warning that the anchor is not guaranteed.

Solo Mode is convenience-first: record with the normal phone camera, import the video, mark release and visible disc positions, enter location and wind, then run a seeded Monte Carlo simulation. Precision Mode is designed for two phones and calibration, improving estimates when camera geometry is usable and falling back to Solo Mode when it is not.

The BYOD angle is simple: the user already owns the phones. No custom hardware, accounts, or paid cloud service are required for the MVP. The optional phone helper now requires the active session code in the helper URL and QR endpoint, so the landing page is not exposed from a bare LAN root URL.

Java 26 is central through a JavaFX 26 desktop app, records, sealed interfaces, pattern matching, virtual threads for the local phone helper, and structured concurrency preview for startup loading. Maven explicitly targets Java release 26, and the repository includes a GitHub Actions workflow that runs `mvnw.cmd clean verify` on Windows with Java 26.

Current status: the repository contains the JavaFX wizard, review-calibrated deterministic physics, Monte Carlo uncertainty, resilient map overlays, weather client, JavaCV metadata reader, search routes, exports, persistence, tests, CI configuration, MIT license, and documentation. The physics model now has regression guardrails for realistic driver distance and hang time, disc separation, wind shift, release-height effects, and Monte Carlo anchor behavior. Advanced optical flow, real ArUco detection, QR video upload, robust triangulation UI, and real field calibration remain experimental.

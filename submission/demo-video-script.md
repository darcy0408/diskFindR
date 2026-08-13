# 90-120 Second Demo Video Script

Length is a contest rule: 90-120 seconds. This script paces to roughly 110.

Prep before recording: the app starts at Valmont Disc Golf Course (Boulder) by
default; click Find Nearby Courses once so the Overpass lookup is warm, then start
the recording on the Welcome screen.

1. Lost-disc problem: discs disappear into brush, trees, glare, or distance.
2. Java proof: point at the footer's Java 26 runtime line (or show
   `submission/build-proof-java26.txt`).
3. Mention Start Solo Search as the real workflow: film with the normal camera app,
   import, mark what you can see. Then click **Open Sample Project** for the demo.
4. Mark Disc: synthetic marks and trail; three marks is enough, more marks honestly
   shrink the search area.
5. Wind step: fetched automatically for the tee, shown in m/s and mph; no user typing.
6. Estimate step: plain disc-golf vocabulary — flight numbers, weight, style,
   handedness; advanced details stay collapsed.
7. Estimate Landing Zone: 500 Monte Carlo throws guarded by plausibility regression
   tests (distance, hang time, wind response).
8. Search map: solid 50 / dashed 80 / dotted 95 percent zones over the real course —
   "not a fake exact dot". Route controls, safety language, export.
9. Course lookup: Find Nearby Courses pulls Valmont's real tees from OpenStreetMap;
   Use Selected Tee aims the throw direction at the nearest mapped basket — map data,
   never the video. Unmapped courses stay fully manual and editable.
10. Phone helper: Start Phone Helper runs a local web server; the in-app QR is that
    server's address. Scan, tap Use My Location, and the phone's GPS becomes the tee
    position. No app install, no account, nothing leaves the network.
11. Close: Precision Mode (second phone, side-view triangulation) as roadmap; GitHub
    repo, MIT license, Java 26 build/test proof.

# 90-120 Second Demo Video Script

1. Lost-disc problem: discs disappear into brush, trees, glare, or distance.
2. One-phone setup: normal phone camera behind the tee, no special hardware.
3. Java proof: show `java -version` or `submission/build-proof-java26.txt` with Java 26.
4. Launch DiscScout and choose **Open Sample Project**.
5. Show Sample Mode banner and Mark Disc with synthetic marks/trail.
6. Show Wind step: DiscScout fetches model wind automatically; no user wind-speed typing.
7. Show Estimate step: simple disc/throw controls, advanced model details collapsed.
8. Choose **Estimate Landing Zone** and show Monte Carlo result; mention that regression tests now guard realistic driver distance, hang time, wind shift, and disc differences.
9. Show Search map: probability zones, Walk grid/Search spiral controls, vegetation spacing, safety warning, and export button.
10. Show public course lookup with a mapped course prepared in advance (e.g., Valmont
    Disc Golf Course, Boulder — release coordinate 40.0278, -105.2373; 18 OSM tees and
    18 baskets). Show the found course and real tees, click Use Selected Tee, and say
    the throw direction comes from the nearest mapped basket in OpenStreetMap — map
    data, never the video — and that unmapped courses stay fully manual and editable.
    Recording note: run Find Nearby Courses once BEFORE recording; Open Sample Project
    resets the release coordinate to downtown Denver, where a live re-search returns
    nothing and clears the dropdown.
11. Show BYOD bonus: phone helper with session code, QR code, browser location, and pasted GPS fallback.
12. End on GitHub repo, MIT license, Java 26 GitHub Actions workflow, `submission/model-validation.md`, and build/test proof.

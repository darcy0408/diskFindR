# DiscScout Agent Notes

## Project Goal

Build DiscScout, a Java 26 JavaFX desktop application that estimates a probabilistic disc-golf landing area from one-phone Solo Mode inputs, with Precision Mode treated as optional after the Solo vertical slice works.

## Working Rules

- Preserve a working vertical slice before adding advanced computer vision or two-phone features.
- Keep calculations deterministic, transparent, and uncertainty-aware.
- Never describe a median or mean landing point as guaranteed.
- Keep videos, exact personal coordinates, API keys, generated diagnostic bundles, and local machine paths out of version control.
- Do not publish, push, deploy, or expose the application publicly without explicit user authorization.
- Update `PLAN.md` after major phases with completed work, blockers, and verified commands.
- Record implementation assumptions in `DECISIONS.md`.

## Build And Test

- Authoritative command: `.\mvnw.cmd clean verify`
- Launch command: `.\run.ps1`
- Maven Java release: 26
- Preview flags are enabled for structured concurrency experiments and documented in `docs/java-26-features.md`.

## Privacy

- Process videos and coordinates locally by default.
- Do not implement face recognition, identity matching, hidden uploads, public location sharing, or background location tracking.
- Logs should avoid exact coordinates by default and never include API keys or full private media paths.


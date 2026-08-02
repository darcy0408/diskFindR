# Model Validation

DiscScout uses a simplified deterministic lift/drag model plus seeded Monte Carlo uncertainty. These checks are not a claim of laboratory-grade aerodynamics. They are regression guardrails added after review found the first model was too ballistic and under-predicted realistic throws.

## Current Regression Guardrails

These values were generated from the Java simulator with generic built-in disc profiles, calm wind unless noted, right-hand backhand, release height 1.4 m, and Java 26.

| Scenario | Guardrail | Current result | Purpose |
| --- | ---: | ---: | --- |
| Distance driver, 26 m/s, 12 degree launch | 95-125 m | 109.8 m | Keeps realistic arm-speed driver throws in a plausible range. |
| Distance driver, 26 m/s, 12 degree launch | 3.5-6.5 s hang | 4.6 s | Prevents the model from becoming a short ballistic arc. |
| Distance driver vs putter at same speed/angle | at least 50 m difference | 50.4 m | Makes disc selection materially affect the result. |
| 10 m/s crosswind vs calm | at least 20 m shift | 26.4 m | Makes wind visibly affect the landing estimate. |
| Fairway driver, release height 0.6 m vs 2.2 m | at least 3 m distance change | 7.2 m | Confirms release height is a real model input. |
| Monte Carlo search anchor | actual simulated landing sample | passing test | Avoids an independent east/north median that can fall outside the landing cloud. |

## Remaining Validation Work

The next credibility step is field calibration: throw a small set of real discs in an open field, record estimated speed, disc class, wind, predicted result, and measured landing distance, then tune or document model error. Until that exists, DiscScout should describe its output as a search-area estimate, not a guaranteed landing prediction.

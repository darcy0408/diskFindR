package dev.discscout.domain;

import java.util.List;

public record DiscProfile(
    String displayName,
    double speed,
    double glide,
    double turn,
    double fade,
    double diameterCm,
    double massGrams,
    double stabilityAdjustment,
    String notes) {
  public DiscProfile {
    if (displayName == null || displayName.isBlank()) {
      throw new IllegalArgumentException("Disc profile needs a display name.");
    }
  }

  public static List<DiscProfile> builtIns() {
    return List.of(
        new DiscProfile("Putter", 3, 3, 0, 1, 21.2, 173, 0.0, "Slow, controllable baseline profile."),
        new DiscProfile("Midrange", 5, 4, -1, 1, 21.7, 177, 0.0, "Neutral midrange profile."),
        new DiscProfile("Fairway driver", 7, 5, -1, 2, 21.2, 172, 0.1, "Controlled driver profile."),
        new DiscProfile("Distance driver", 12, 5, -1, 3, 21.1, 173, 0.2, "High-speed driver profile."),
        new DiscProfile("Overstable driver", 10, 4, 0, 4, 21.1, 175, 0.6, "Fades earlier and harder."),
        new DiscProfile("Understable driver", 9, 5, -3, 1, 21.1, 168, -0.5, "Turns more at high speed."));
  }
}


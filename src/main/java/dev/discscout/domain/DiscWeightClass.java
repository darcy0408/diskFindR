package dev.discscout.domain;

public enum DiscWeightClass {
  LIGHT("Light", -8.0, -0.08),
  NORMAL("Normal", 0.0, 0.0),
  HEAVY("Heavy", 6.0, 0.08);

  private final String label;
  private final double massOffsetGrams;
  private final double stabilityOffset;

  DiscWeightClass(String label, double massOffsetGrams, double stabilityOffset) {
    this.label = label;
    this.massOffsetGrams = massOffsetGrams;
    this.stabilityOffset = stabilityOffset;
  }

  public DiscProfile applyTo(DiscProfile profile) {
    var adjustedMass = Math.max(130.0, Math.min(180.0, profile.massGrams() + massOffsetGrams));
    return new DiscProfile(
        profile.displayName(),
        profile.speed(),
        profile.glide(),
        profile.turn(),
        profile.fade(),
        profile.diameterCm(),
        adjustedMass,
        profile.stabilityAdjustment() + stabilityOffset,
        profile.notes() + " Weight class: " + label + ".");
  }

  @Override
  public String toString() {
    return label;
  }
}
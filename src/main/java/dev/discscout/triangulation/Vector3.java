package dev.discscout.triangulation;

public record Vector3(double x, double y, double z) {
  public Vector3 add(Vector3 other) { return new Vector3(x + other.x, y + other.y, z + other.z); }
  public Vector3 subtract(Vector3 other) { return new Vector3(x - other.x, y - other.y, z - other.z); }
  public Vector3 multiply(double scalar) { return new Vector3(x * scalar, y * scalar, z * scalar); }
  public double dot(Vector3 other) { return x * other.x + y * other.y + z * other.z; }
  public double magnitude() { return Math.sqrt(dot(this)); }
  public Vector3 normalize() {
    var m = magnitude();
    if (m == 0.0) throw new IllegalArgumentException("Cannot normalize zero vector.");
    return multiply(1.0 / m);
  }
}


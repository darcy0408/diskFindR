package dev.discscout.triangulation;

public final class Triangulator {
  private static final double PARALLEL_EPSILON = 1.0e-6;

  public TriangulationResult closestPoint(Ray a, Ray b) {
    var w0 = a.origin().subtract(b.origin());
    var dot = a.direction().dot(b.direction());
    var denom = 1.0 - dot * dot;
    if (Math.abs(denom) < PARALLEL_EPSILON) {
      return new TriangulationResult(false, a.origin(), Double.POSITIVE_INFINITY, "Camera rays are nearly parallel.");
    }
    var s = (dot * b.direction().dot(w0) - a.direction().dot(w0)) / denom;
    var t = (b.direction().dot(w0) - dot * a.direction().dot(w0)) / denom;
    if (s < 0.0 || t < 0.0) {
      return new TriangulationResult(false, a.origin(), Double.POSITIVE_INFINITY, "Triangulated point is behind at least one camera.");
    }
    var pA = a.origin().add(a.direction().multiply(s));
    var pB = b.origin().add(b.direction().multiply(t));
    var midpoint = pA.add(pB).multiply(0.5);
    var separation = pA.subtract(pB).magnitude();
    if (separation > 8.0) {
      return new TriangulationResult(false, midpoint, separation, "Camera rays diverge too much for a reliable 3D point.");
    }
    return new TriangulationResult(true, midpoint, separation, "OK");
  }
}


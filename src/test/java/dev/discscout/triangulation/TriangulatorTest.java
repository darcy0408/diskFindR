package dev.discscout.triangulation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class TriangulatorTest {
  @Test
  void closestPointAcceptsIntersectingRays() {
    var a = new Ray(new Vector3(0, 0, 1), new Vector3(1, 1, 0));
    var b = new Ray(new Vector3(2, 0, 1), new Vector3(-1, 1, 0));
    var result = new Triangulator().closestPoint(a, b);
    assertTrue(result.valid());
  }

  @Test
  void closestPointRejectsNearParallelRays() {
    var a = new Ray(new Vector3(0, 0, 1), new Vector3(1, 0, 0));
    var b = new Ray(new Vector3(0, 2, 1), new Vector3(1, 0, 0));
    var result = new Triangulator().closestPoint(a, b);
    assertFalse(result.valid());
  }
}


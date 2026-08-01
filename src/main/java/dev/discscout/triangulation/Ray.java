package dev.discscout.triangulation;

public record Ray(Vector3 origin, Vector3 direction) {
  public Ray {
    direction = direction.normalize();
  }
}


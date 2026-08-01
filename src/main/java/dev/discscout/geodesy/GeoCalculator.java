package dev.discscout.geodesy;

import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.LocalPoint;

public final class GeoCalculator {
  private static final double EARTH_RADIUS_METERS = 6_371_008.8;

  private GeoCalculator() {}

  public static LocalPoint toLocal(GeoPoint origin, GeoPoint point) {
    var lat0 = Math.toRadians(origin.latitude());
    var dLat = Math.toRadians(point.latitude() - origin.latitude());
    var dLon = Math.toRadians(point.longitude() - origin.longitude());
    var east = dLon * Math.cos(lat0) * EARTH_RADIUS_METERS;
    var north = dLat * EARTH_RADIUS_METERS;
    return new LocalPoint(east, north, 0.0);
  }

  public static GeoPoint fromLocal(GeoPoint origin, LocalPoint local) {
    var lat0 = Math.toRadians(origin.latitude());
    var lat = Math.toRadians(origin.latitude()) + local.northMeters() / EARTH_RADIUS_METERS;
    var lon = Math.toRadians(origin.longitude()) + local.eastMeters() / (EARTH_RADIUS_METERS * Math.cos(lat0));
    return new GeoPoint(Math.toDegrees(lat), normalizeLongitude(Math.toDegrees(lon)));
  }

  public static double distanceMeters(GeoPoint a, GeoPoint b) {
    var lat1 = Math.toRadians(a.latitude());
    var lat2 = Math.toRadians(b.latitude());
    var dLat = lat2 - lat1;
    var dLon = Math.toRadians(b.longitude() - a.longitude());
    var h = Math.pow(Math.sin(dLat / 2.0), 2.0)
        + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2.0), 2.0);
    return 2.0 * EARTH_RADIUS_METERS * Math.asin(Math.min(1.0, Math.sqrt(h)));
  }

  public static double bearingDegrees(GeoPoint from, GeoPoint to) {
    var lat1 = Math.toRadians(from.latitude());
    var lat2 = Math.toRadians(to.latitude());
    var dLon = Math.toRadians(to.longitude() - from.longitude());
    var y = Math.sin(dLon) * Math.cos(lat2);
    var x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
    return normalizeDegrees(Math.toDegrees(Math.atan2(y, x)));
  }

  public static double normalizeDegrees(double degrees) {
    return ((degrees % 360.0) + 360.0) % 360.0;
  }

  private static double normalizeLongitude(double degrees) {
    var normalized = ((degrees + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    return normalized == -180.0 ? 180.0 : normalized;
  }
}


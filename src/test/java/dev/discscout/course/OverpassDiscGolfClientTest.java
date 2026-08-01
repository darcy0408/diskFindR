package dev.discscout.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.discscout.domain.GeoPoint;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class OverpassDiscGolfClientTest {
  @Test
  void parsesPublicCourseTeesAndBasketsFromOverpassJson() throws Exception {
    var json = new String(getClass().getResourceAsStream("/course/overpass-disc-golf-fixture.json").readAllBytes(), StandardCharsets.UTF_8);
    var courses = new OverpassDiscGolfClient().parse(json, new GeoPoint(39.7392, -104.9903));

    assertEquals(1, courses.size());
    var course = courses.getFirst();
    assertEquals("Fixture Park Disc Golf", course.name());
    assertEquals(2, course.tees().size());
    assertEquals(1, course.baskets().size());
    assertEquals("OpenStreetMap via Overpass", course.source());
  }

  @Test
  void suggestsBearingFromMatchingTeeToBasket() throws Exception {
    var json = new String(getClass().getResourceAsStream("/course/overpass-disc-golf-fixture.json").readAllBytes(), StandardCharsets.UTF_8);
    var course = new OverpassDiscGolfClient().parse(json, new GeoPoint(39.7392, -104.9903)).getFirst();
    var teeOne = course.tees().stream().filter(tee -> "1".equals(tee.ref())).findFirst().orElseThrow();

    var bearing = course.suggestedBearingFor(teeOne);

    assertFalse(bearing.isEmpty());
    assertTrue(bearing.get() > 25.0 && bearing.get() < 40.0);
  }

  @Test
  void overpassQueryIncludesPublicDiscGolfTags() {
    var query = OverpassDiscGolfClient.overpassQuery(new GeoPoint(39.0, -105.0), 2_000);

    assertTrue(query.contains("leisure=disc_golf_course"));
    assertTrue(query.contains("disc_golf~"));
    assertTrue(query.contains("out center tags"));
  }
}
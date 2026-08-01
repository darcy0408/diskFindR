package dev.discscout.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class WindTest {
  @Test
  void windFromWestBlowsEast() {
    var wind = new Wind(5.0, 270.0, 7.0, new WindSource.Manual("test"));
    assertEquals(5.0, wind.eastMps(), 0.0001);
    assertEquals(0.0, wind.northMps(), 0.0001);
  }
}


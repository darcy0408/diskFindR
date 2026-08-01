package dev.discscout.weather;

import dev.discscout.domain.Wind;

public record WeatherResult(boolean available, Wind wind, String message) {
}


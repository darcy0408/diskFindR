package dev.discscout.domain;

import java.nio.file.Path;

public record VideoMetadata(Path path, double frameRate, int width, int height, double durationSeconds, String codec) {
}


package dev.discscout.video;

import dev.discscout.domain.VideoMetadata;
import java.nio.file.Path;
import org.bytedeco.javacv.FFmpegFrameGrabber;

public final class VideoMetadataReader {
  public VideoMetadata read(Path path) {
    try (var grabber = new FFmpegFrameGrabber(path.toFile())) {
      grabber.start();
      var frameRate = grabber.getFrameRate();
      var width = grabber.getImageWidth();
      var height = grabber.getImageHeight();
      var duration = grabber.getLengthInTime() / 1_000_000.0;
      var codec = grabber.getVideoCodecName();
      grabber.stop();
      return new VideoMetadata(path, frameRate, width, height, duration, codec == null ? "unknown" : codec);
    } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
      throw new IllegalArgumentException("Unsupported or unreadable video: " + e.getMessage(), e);
    }
  }
}

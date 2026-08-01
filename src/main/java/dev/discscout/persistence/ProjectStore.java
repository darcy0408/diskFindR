package dev.discscout.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public final class ProjectStore {
  private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

  public Path createProjectDirectory(Path root, String name) throws IOException {
    var safeName = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    if (safeName.isBlank()) safeName = "discscout-project";
    var dir = root.resolve(safeName + "-" + UUID.randomUUID().toString().substring(0, 8));
    Files.createDirectories(dir.resolve("captures"));
    Files.createDirectories(dir.resolve("tracking"));
    Files.createDirectories(dir.resolve("simulation"));
    Files.createDirectories(dir.resolve("exports"));
    Files.createDirectories(dir.resolve("logs"));
    return dir;
  }

  public void save(Path projectDir, DiscScoutProject project) throws IOException {
    Files.createDirectories(projectDir);
    project.updatedAt = Instant.now();
    mapper.writerWithDefaultPrettyPrinter().writeValue(projectDir.resolve("project.json").toFile(), project);
  }

  public DiscScoutProject load(Path projectDir) throws IOException {
    return mapper.readValue(projectDir.resolve("project.json").toFile(), DiscScoutProject.class);
  }
}


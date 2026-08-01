package dev.discscout.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

final class ProjectStoreTest {
  @TempDir Path temp;

  @Test
  void savesAndReloadsProjectDirectory() throws Exception {
    var store = new ProjectStore();
    var dir = store.createProjectDirectory(temp, "My Throw");
    var project = new DiscScoutProject();
    project.name = "My Throw";
    store.save(dir, project);
    var loaded = store.load(dir);
    assertEquals(project.name, loaded.name);
    assertTrue(Files.exists(dir.resolve("captures")));
    assertTrue(Files.exists(dir.resolve("exports")));
  }
}


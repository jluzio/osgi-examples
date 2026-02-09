package com.example.osgi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BundleTestHelper {

  public static Path bundlePath(String buildDirectory, String prefix) throws IOException {
    try (var files = Files.list(Path.of(buildDirectory))) {
      var jarOpt = files
          .filter(p -> {
            String n = p.getFileName().toString();
            return n.startsWith(prefix) && n.endsWith(".jar");
          }).findFirst();
      if (jarOpt.isEmpty()) {
        throw new FileNotFoundException("Could not find bundle jar");
      }
      return jarOpt.get().toAbsolutePath();
    }
  }

}
